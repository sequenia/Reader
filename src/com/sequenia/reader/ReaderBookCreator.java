package com.sequenia.reader;

import java.util.ArrayList;

public class ReaderBookCreator {
	public static ReaderBook createReaderBook(Book book, ReaderSettings settings, float x, float y) {
		ReaderBook readerBook = new ReaderBook(settings);
		readerBook.setPosition(x, y);
		readerBook.setTitle(book.titles, settings);
		readerBook.setCreator(book.creators, settings);
		readerBook.setYear(book.dates, settings);

		ArrayList<ReaderPage> pages = new ArrayList<ReaderPage>();
		
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
		readerBook.createBorders(settings.bookBorderPaint);
		
		return readerBook;
	}
}
