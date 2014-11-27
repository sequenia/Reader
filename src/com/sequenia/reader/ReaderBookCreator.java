package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Paint;

import com.sequenia.reader.Book.BookPage;
import com.sequenia.reader.Book.PageElem;
import com.sequenia.reader.Book.PageText;
import com.sequenia.reader.LibraryManager.AddToLibraryTask;

/*
 * Используется для преобразования книги класса Book в формат, отображаемый на экране.
 * 
 * Основная идея состоит в том, чтобы разбить существующие страницы книги на более мелкие,
 * которые поместятся на экране устройства.
 */
public class ReaderBookCreator {
	public static enum LexemeType {
		WORD, SPACES, NEW_LINE, SIGN, EMPTY
	}
	
	public static ReaderBook createReaderBook(Book book, ReaderSettings settings, float x, float y, AddToLibraryTask task) {
		ReaderBook readerBook = new ReaderBook(settings);

		readerBook.setPosition(x, y);
		setBookInfo(readerBook, book, settings);

		ArrayList<ReaderPage> pages = createPages(book, settings, task);
		addPagesToReaderBook(readerBook, pages, settings);

		readerBook.createBorders(settings.bookBorderPaint);
		
		return readerBook;
	}
	
	/*
	 * Заполненяет заголовочную информцию о книге (Писатель, Название и т.д.)
	 */
	private static void setBookInfo(ReaderBook readerBook, Book book, ReaderSettings settings) {
		readerBook.setTitle(book.titles, settings);
		readerBook.setCreator(book.creators, settings);
		readerBook.setYear(book.dates, settings);
	}
	
	/*
	 * Дробит страницы книги на небольщие страницы, помещающиеся в экран.
	 */
	private static ArrayList<ReaderPage> createPages(Book book, ReaderSettings settings, AddToLibraryTask task) {
		ArrayList<ReaderPage> readerPages = new ArrayList<ReaderPage>();
		
		float pageWidth = settings.getScreenWidth();
		float pageHeight = settings.getScreenHeight();
		float textStartY = settings.pagePadding + settings.textSize;
		float pageContentHeight = pageHeight - settings.pagePadding * 2.0f - settings.textSize;
		float pageContentWidth = pageWidth - settings.pagePadding * 2.0f;
		float doubleLinesMargin = settings.linesMargin * 2.0f;
		float currentContentHeight;
		
		int pagesCount = book.pages.size();
		for(int i = 0; i < pagesCount; i++) {
			ReaderPage readerPage = new ReaderPage(pageWidth, pageHeight, settings);
			BookPage page = book.pages.get(i);
			currentContentHeight = 0.0f;

			// Каждая страница состоит из разных элементов.
			// В зависимости от его типа производим различные дейсвтия
			for(int j = 0; j < page.elements.size(); j++) {
				PageElem elem = page.elements.get(j);
				switch (elem.type) {
				case Text:
					// Разбиваем текст на мелкие странички
					PageText pageText = (PageText) elem;
					StringBuilder text = new StringBuilder(pageText.text);
					
					while(text.length() != 0) {
						String line = getNextLine(text, settings.textPaint, pageContentWidth).toString();
						
						ReaderText readerText = new ReaderText(line);
						readerText.setPaint(settings.textPaint);
						readerText.setPosition(settings.pagePadding, currentContentHeight + textStartY);
						currentContentHeight += settings.textSize + doubleLinesMargin;
						readerPage.addLine(readerText);
						
						// Если место на странице кончилось, создаем новую
						if(currentContentHeight >= pageContentHeight) {
							readerPages.add(readerPage);
							readerPage = new ReaderPage(pageWidth, pageHeight, settings);
							currentContentHeight = 0.0f;
						}
					}
					break;

				default:
					break;
				}
			}
			
			if(i % 20 == 0) {
				task.publish(100 * i / pagesCount);
			}
			
			readerPages.add(readerPage);
		}
		
		return readerPages;
	}
	
	/*
	 * Возвращает стоку, влезающую на экран.
	 * Удаляет ее из text.
	 * 
	 * В реализации алгоритма используется класс StringBuilder, а не String,
	 * так как он является более быстродействующим.
	 * 
	 * Идея алгоритма:
	 * Добавляем к строке по одному слову из текста.
	 * Если сточка не вмещается в экран, завершаем алгоритм.
	 * 
	 * Хак:
	 * Я толком не генерирую новую строку во время действия алгоритма.
	 * Я лишь рассчитываю, какой длины нужно взять строку, чтобы она поместилась на экране,
	 * а потом вырезаю ее из text.
	 */
	private static StringBuilder getNextLine(StringBuilder text, Paint paint, float maxWidth) {
		StringBuilder line = new StringBuilder("");
		Lexeme lexeme;
		int lexemesCount = 0;
		boolean lineCompleted = false;
		
		while(!lineCompleted) {
			// Получаем информацию о следующей лексеме (Длина и тип)
			// Лексемой может быть слово из букв, строка из пробелов, перевод коретки или пустота
			lexeme = getNextLexeme(text);
			lexemesCount++;
			
			int newLineLength;
			float lineWidth;
			
			switch(lexeme.type) {
			case EMPTY:
				lineCompleted = true;
				break;

			case WORD:
				newLineLength = line.length() + lexeme.length;
				lineWidth = getTextWidth(newLineLength, paint);
				if(lineWidth < maxWidth) {
					for(int i = 0; i < lexeme.length; i++) {
						line.append(text.charAt(i));
					}
					text.delete(0, lexeme.length);
				} else {
					if(lexemesCount == 1) {
						for(int i = 0; i < lexeme.length; i++) {
							line.append(text.charAt(i));
						}
						text.delete(0, lexeme.length);
					}
					lineCompleted = true;
				}
				break;
				
			case SPACES:
				newLineLength = line.length() + lexeme.length;
				lineWidth = getTextWidth(newLineLength, paint);
				if(lineWidth < maxWidth) {
					for(int i = 0; i < lexeme.length; i++) {
						line.append(text.charAt(i));
					}
					text.delete(0, lexeme.length);
				} else {
					if(lexemesCount == 1) {
						for(int i = 0; i < lexeme.length; i++) {
							line.append(text.charAt(i));
						}
						text.delete(0, lexeme.length);
					}
					lineCompleted = true;
				}
				break;
				
			case NEW_LINE:
				text.delete(0, lexeme.length);
				lineCompleted = true;
				break;
				
			default:
				break;
			}
		}
		
		return line;
	}

	/*
	 * Возвращает информацию о следующей лексеме: длину и тип.
	 */
	private static Lexeme getNextLexeme(StringBuilder text) {
		Lexeme lexeme;
		int currentCharIndex = 0;
		int textLength = text.length();
		
		if(textLength > 0) {
			char c = text.charAt(currentCharIndex);
			
			switch (c) {
			case '\n':
				currentCharIndex++;
				lexeme = new Lexeme(currentCharIndex, LexemeType.NEW_LINE);
				break;
				
			case ' ':
				while(c == ' ') {
					currentCharIndex++;
					if(currentCharIndex < textLength) {
						c = text.charAt(currentCharIndex);
					} else {
						break;
					}
				}
				lexeme = new Lexeme(currentCharIndex, LexemeType.SPACES);
				break;

			default:
				while(c != ' ' && c != '\n') {
					currentCharIndex++;
					if(currentCharIndex < textLength) {
						c = text.charAt(currentCharIndex);
					} else {
						break;
					}
				}
				lexeme = new Lexeme(currentCharIndex, LexemeType.WORD);
				break;
			}
		} else {
			lexeme = new Lexeme(0, LexemeType.EMPTY);
		}
		
		return lexeme;
	}
	
	public static float getTextWidth(String text, Paint paint) {
		return text.length() * paint.getTextSize() / 1.8f;
	}
	
	public static float getTextWidth(StringBuilder text, Paint paint) {
		return text.length() * paint.getTextSize() / 1.8f;
	}
	
	public static float getTextWidth(int textLength, Paint paint) {
		return textLength * paint.getTextSize() / 1.8f;
	}
	
	private static void addPagesToReaderBook(ReaderBook readerBook, ArrayList<ReaderPage> pages, ReaderSettings settings) {
		int pagesCount = pages.size();
		int pagesPerLine = (int) Math.ceil(Math.sqrt(pagesCount));
		for(int i = 0; i < pagesCount; i++) {
			ReaderPage page = pages.get(i);
			
			float pageX = (float)(i % pagesPerLine) * settings.getScreenWidth();
			float pageY = (float)(i / pagesPerLine) * settings.getScreenHeight();
			float pageWidth = page.getWidth();
			float pageHeight = page.getHeight();
			
			page.setPosition(pageX, pageY);
			
			readerBook.addPage(page);
			
			if(i % pagesPerLine == 0 && i != 0) {
				ReaderPage fake = page.createFake(settings, pageWidth * pagesPerLine, pageY - pageHeight);
				readerBook.addFakePage(fake);
			}
			
			if(i % pagesPerLine == pagesPerLine - 1 && i != pagesCount - 1) {
				ReaderPage fake = page.createFake(settings, - pageWidth, pageY + pageHeight);
				readerBook.addFakePage(fake);
			}
		}
		
		float width = pagesPerLine * settings.getScreenWidth();
		float height = (float) (Math.ceil(((float)pagesCount / (float)pagesPerLine)) * settings.getScreenHeight());
		readerBook.setWidth(width);
		readerBook.setHeight(height);
	}
	
	private static class Lexeme {
		public int length;
		public LexemeType type;
		
		public Lexeme(int _length, LexemeType _type) {
			length = _length;
			type = _type;
		}
	}
}
