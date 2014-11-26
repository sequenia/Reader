package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Paint;

import com.sequenia.reader.Book.BookPage;
import com.sequenia.reader.Book.PageElem;
import com.sequenia.reader.Book.PageText;
import com.sequenia.reader.LibraryManager.AddToLibraryTask;

public class ReaderBookCreator {
	public static ReaderBook createReaderBook(Book book, ReaderSettings settings, float x, float y, AddToLibraryTask task) {
		ReaderBook readerBook = new ReaderBook(settings);

		readerBook.setPosition(x, y);
		setBookInfo(readerBook, book, settings);

		ArrayList<ReaderPage> pages = createPages(book, settings, task);
		addPagesToReaderBook(readerBook, pages, settings);

		readerBook.createBorders(settings.bookBorderPaint);
		
		return readerBook;
	}
	
	private static void setBookInfo(ReaderBook readerBook, Book book, ReaderSettings settings) {
		readerBook.setTitle(book.titles, settings);
		readerBook.setCreator(book.creators, settings);
		readerBook.setYear(book.dates, settings);
	}
	
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

			for(int j = 0; j < page.elements.size(); j++) {
				PageElem elem = page.elements.get(j);
				switch (elem.type) {
				case Text:
					PageText pageText = (PageText) elem;
					StringBuilder text = new StringBuilder(pageText.text);
					
					while(text.length() != 0) {
						String line = getNextLine(text, settings.textPaint, pageContentWidth).toString();
						
						ReaderText readerText = new ReaderText(line);
						readerText.setPaint(settings.textPaint);
						readerText.setPosition(settings.pagePadding, currentContentHeight + textStartY);
						currentContentHeight += settings.textSize + doubleLinesMargin;
						readerPage.addLine(readerText);
						
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
	
	private static StringBuilder getNextLine(StringBuilder text, Paint paint, float maxWidth) {
		StringBuilder line = new StringBuilder("");
		StringBuilder newLine = new StringBuilder("");
		boolean lineCompleted = false;
		int currentCharIndex = 0;
		int textLength = text.length();
		
		while(!lineCompleted && currentCharIndex < textLength) {
			char c = text.charAt(currentCharIndex);
			
			switch(c) {
			case '\n':
				lineCompleted = true;
				currentCharIndex++;
				break;
			default:
				newLine.append(c);
				
				float width = line.length() * paint.getTextSize() / 1.8f;
				
				if(width > maxWidth) {
					lineCompleted = true;
				} else {
					line.append(c);
					currentCharIndex++;
				}
				
				break;
			}
		}
		
		text.delete(0, currentCharIndex);
		
		return line;
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
