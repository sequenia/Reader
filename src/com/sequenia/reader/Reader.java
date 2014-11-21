package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;

public class Reader {
	private ReaderSettings settings;
	private ArrayList<ReaderBook> books;
	
	private ArrayList<ReaderBook> booksToDraw;
	
	public Reader() {
		settings = new ReaderSettings();
		books = new ArrayList<ReaderBook>();
		booksToDraw = new ArrayList<ReaderBook>();
		initObjects();
	}
	
	public Reader(ReaderSettings _settings) {
		settings = _settings;
		books = new ArrayList<ReaderBook>();
		booksToDraw = new ArrayList<ReaderBook>();
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
				ReaderPage page = new ReaderPage(settings.getScreenWidth(), settings.getScreenHeight(), settings);
				
				page.setPosition((float)(i % pagesPerLine) * settings.getScreenWidth(), (float)(i / pagesPerLine) * settings.getScreenHeight());
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
			
			float width = pagesPerLine * settings.getScreenWidth();
			float height = (float) (Math.ceil(((float)pagesCount / (float)pagesPerLine)) * settings.getScreenHeight());
			readerBook.setWidth(width);
			readerBook.setHeight(height);
			readerBook.createBorders(settings.bookBorderPaint);
			books.add(readerBook);
			
			newBookX = newBookX + width + 150.0f;
		}
	}
	
	public void update(Canvas canvas) {
		booksToDraw = findBooksToDraw(canvas);
	}
	
	private ArrayList<ReaderBook> findBooksToDraw(Canvas canvas) {
		ArrayList<ReaderBook> toDraw = new ArrayList<ReaderBook>();
		
		for(int i = 0; i < books.size(); i++) {
			ReaderBook book = books.get(i);
			
			if(book.isInScreen(canvas)) {
				toDraw.add(book);
				book.update(canvas);
			}
		}
		
		return toDraw;
	}
	
	public void draw(Canvas canvas, long delta, float zoom) {
		canvas.drawPaint(settings.bgPaint);
		
		for(int i = 0; i < booksToDraw.size(); i++) {
			booksToDraw.get(i).draw(canvas, zoom);
		}
	}
	
	public ReaderPage getNearestPage() {
		/*ReaderPage nearestPage = null;
		
		for(int i = 0; i < booksToDraw.size(); i++) {
			ReaderBook book = booksToDraw.get(i);
			ArrayList<ReaderPage> pages = book.getPages();
			for(int j = 0; j < pages.size(); j++) {
				
			}
		}*/
		
		return books.get(0).getPages().get(13);
	}
}
