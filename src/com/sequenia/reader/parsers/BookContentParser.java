package com.sequenia.reader.parsers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Paint;

import com.sequenia.reader.ReaderSettings;
import com.sequenia.reader.LibraryManager.AddToLibraryTask;
import com.sequenia.reader.parsers.Book.BookPage;
import com.sequenia.reader.parsers.Book.PageElem;
import com.sequenia.reader.parsers.Book.PageText;

/**
 * @author chybakut2004
 *
 * Используется для приведения книги к формату хранения и записи ее в текстовый файл.
 * Формат имеет следующий вид:
 * 
 * aLine1\n
 * aLine2\n
 * aLine3\n
 * ...
 * ...
 * 
 * где a - тип строки. Указывает на то, какую нагрузку несет следующая строка
 *     Line1, Line2, Line3 - строки
 *     
 * a может иметь значения:
 * 's' - Строка текста, помешающаяся в экран
 * 'p' - Сигнал о создании новой страницы
 * 
 */
public class BookContentParser {

	public static enum LexemeType {
		WORD, SPACES, NEW_LINE, SIGN, EMPTY
	}

	/**
	 * @param book
	 * @param parsedTextPath
	 * @param settings
	 * @param task
	 * @param context
	 * 
	 * Преобразует книгу к формату хранения и записывает ее в файл
	 */
	public static void createParsedTextFile(Book book, String parsedTextPath,
			ReaderSettings settings, AddToLibraryTask task, Context context) {
		float pageWidth = settings.getScreenWidth();
		float pageContentWidth = pageWidth - settings.pagePadding * 2.0f;
		
		int pagesCount = book.pages.size();
		int updatePeriod = pagesCount / 10;
		if(updatePeriod == 0) {
			updatePeriod = 1;
		}
		
		FileOutputStream outputStream;
		try {
			outputStream = context.openFileOutput(parsedTextPath, Context.MODE_PRIVATE);
			try {
				for(int i = 0; i < pagesCount; i++) {
					outputStream.write("p\n".getBytes());
					BookPage page = book.pages.get(i);
	
					// Каждая страница состоит из разных элементов.
					// В зависимости от его типа производим различные дейсвтия
					for(int j = 0; j < page.elements.size(); j++) {
						PageElem elem = page.elements.get(j);
						switch (elem.type) {
						case Text:
							PageText pageText = (PageText) elem;
							StringBuilder text = new StringBuilder(pageText.text);
							
							// Достаем по строчке, которая вмещается в экран
							while(text.length() != 0) {
								String line = getNextLine(text, settings.textPaint, pageContentWidth).toString();
								outputStream.write(line.getBytes());
							}
							break;
	
						default:
							break;
						}
					}
					
					if(i % updatePeriod == 0) {
						task.publish(100 * i / pagesCount);
					}
				}
				
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
		StringBuilder line = new StringBuilder("s");
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
		
		line.append('\n');
		
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
	
	public static class Lexeme {
		public int length;
		public LexemeType type;
		
		public Lexeme(int _length, LexemeType _type) {
			length = _length;
			type = _type;
		}
	}
}
