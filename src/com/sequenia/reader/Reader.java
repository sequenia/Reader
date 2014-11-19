package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;

public class Reader {
	private ReaderSettings settings;
	private ArrayList<ReaderBook> books;
	
	public Reader() {
		settings = new ReaderSettings();
		books = new ArrayList<ReaderBook>();
		initObjects();
	}
	
	public Reader(ReaderSettings _settings) {
		settings = _settings;
		books = new ArrayList<ReaderBook>();
		initObjects();
	}
	
	public void initObjects() {
		int booksCount = 5;
		float newBookX = 0.0f;
		float newBookY = 0.0f;
		
		for(int b = 0; b < booksCount; b++) {
			Book book = new Book();
			book.titles.add("Колобок.");
			book.titles.add("Народная сказка.");
			book.creators.add("Народ");
			book.creators.add("Именно он написал эту книгу");
			book.dates.add("1000");
			book.dates.add("2014");
			
			ReaderBook readerBook = new ReaderBook(settings);
			readerBook.setPosition(newBookX, newBookY);
			readerBook.setTitle(book.titles, settings);
			readerBook.setCreator(book.creators, settings);
			readerBook.setYear(book.dates, settings);

			int pagesCount = (b + 1) * 100;
			int pagesPerLine = (int) Math.ceil(Math.sqrt(pagesCount));
			for(int i = 0; i < pagesCount; i++) {
				ReaderPage page = new ReaderPage(settings.screenWidth, settings.screenHeight, settings);
				
				page.setPosition((float)(i % pagesPerLine) * settings.screenWidth, (float)(i / pagesPerLine) * settings.screenHeight);
				if(i < 74) {
					page.setIsRead(true);
				}
				if(i == 13) {
					page.setIsCurrent(true);
				}
				
				for(int j = 0; j < 10; j++) {
					ReaderText text = new ReaderText("Привет! Как дела!?");
					text.setPosition(10.0f, j * 50.0f + 50.0f);
					text.setPaint(settings.textPaint);
					page.addLine(text);
				}
				
				readerBook.addPage(page);
			}
			
			float width = pagesPerLine * settings.screenWidth;
			float height = (float) (Math.ceil(((float)pagesCount / (float)pagesPerLine)) * settings.screenHeight);
			readerBook.setWidth(width);
			readerBook.setHeight(height);
			readerBook.createBorders(settings.bookBorderPaint);
			books.add(readerBook);
			
			newBookX = newBookX + width + 150.0f;
		}
	}
	
	public void draw(Canvas canvas, long delta, float zoom) {
		canvas.drawPaint(settings.bgPaint);
		
		for(int i = 0; i < books.size(); i++) {
			books.get(i).draw(canvas, zoom);
		}
	}
}
