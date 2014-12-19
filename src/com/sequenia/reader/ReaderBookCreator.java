package com.sequenia.reader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;

import com.sequenia.reader.db.DbBook;
import com.sequenia.reader.reader_objects.ReaderBook;
import com.sequenia.reader.reader_objects.ReaderPage;
import com.sequenia.reader.reader_objects.ReaderText;

public class ReaderBookCreator {
	
	public static ReaderBook createReaderBook(Context context, DbBook book, ReaderSettings settings, float x, float y) {
		System.out.println("Преобразование книги к отображаемому формату...");
		
		ReaderBook readerBook = new ReaderBook(settings);

		readerBook.setPosition(x, y);
		System.out.println("Задание информации на обложке...");
		setBookInfo(readerBook, book, settings);
		System.out.println("Задание информации на обложке завершено");

		System.out.println("Создание страниц...");
		ArrayList<ReaderPage> pages = createPages(book, settings, context);
		System.out.println("Создание страниц завершено");

		if(pages != null) {
			System.out.println("Добавление страниц в книгу...");
			addPagesToReaderBook(readerBook, pages, settings);
			System.out.println("Добавление страниц в книгу завершено");
	
			readerBook.createBorders(settings.bookBorderPaint);
			System.out.println("Преобразование книги к отображаемому формату завершено!");
		} else {
			return null;
		}
		
		return readerBook;
	}
	
	/*
	 * Заполненяет заголовочную информцию о книге (Писатель, Название и т.д.)
	 */
	private static void setBookInfo(ReaderBook readerBook, DbBook book, ReaderSettings settings) {
		readerBook.setTitle(book.titles, settings);
		readerBook.setCreator(book.creators, settings);
		readerBook.setYear(book.dates, settings);
	}
	
	/*
	 * Дробит страницы книги на небольщие страницы, помещающиеся в экран.
	 */
	private static ArrayList<ReaderPage> createPages(DbBook book, ReaderSettings settings, Context context) {
		ArrayList<ReaderPage> readerPages = new ArrayList<ReaderPage>();
		
		float pageWidth = settings.getScreenWidth();
		float pageHeight = settings.getScreenHeight();
		float textStartY = settings.pagePadding + settings.textSize;
		float pageContentHeight = pageHeight - settings.pagePadding * 2.0f - settings.textSize;
		float doubleLinesMargin = settings.linesMargin * 2.0f;
		float currentContentHeight;
		
		InputStream is = null;
		try {
			is = new BufferedInputStream(context.openFileInput(book.parsedTextPath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			int c;
			
			ReaderPage readerPage = new ReaderPage(pageWidth, pageHeight, settings);
			currentContentHeight = 0.0f;
			try {
				while((c = reader.read()) != -1) {
					line = reader.readLine();
					if(line == null) {
						break;
					}
					
					switch ((char)c) {
					case 's':
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
						break;
						
					case 'p':
						readerPages.add(readerPage);
						readerPage = new ReaderPage(pageWidth, pageHeight, settings);
						currentContentHeight = 0.0f;
						break;
						
					default:
						break;
					}
				}
				
				readerPages.add(readerPage);
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return null;
		}

		
		return readerPages;
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
}
