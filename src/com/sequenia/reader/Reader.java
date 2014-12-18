package com.sequenia.reader;

import java.util.ArrayList;

import com.sequenia.reader.parsers.Book;
import com.sequenia.reader.reader_objects.ReaderBook;
import com.sequenia.reader.reader_objects.ReaderPage;
import com.sequenia.reader.reader_objects.ReaderText;
import com.sequenia.reader.surface.ReaderSurface.ReaderMode;

import android.graphics.Canvas;

/*
 * Читалка.
 * 
 * Объект этого класса содержит в себе книги, которые нужно показать на экране, и умеет их рисовать.
 * Рисует только те книги, которые попали в экран.
 * 
 * Предоставляет информацию о текущей книге.
 */
public class Reader {
	private ReaderSettings settings;
	private ArrayList<ReaderBook> books;        // Книги
	private ArrayList<ReaderBook> booksToDraw;  // Книги, которые будут отрисованы
	
	private ReaderBook currentBook;
	
	public Reader() {
		settings = new ReaderSettings();
		books = new ArrayList<ReaderBook>();
		booksToDraw = new ArrayList<ReaderBook>();
		currentBook = null;
		initObjects();
	}
	
	public Reader(ReaderSettings _settings) {
		settings = _settings;
		books = new ArrayList<ReaderBook>();
		booksToDraw = new ArrayList<ReaderBook>();
		currentBook = null;
		initObjects();
	}
	
	public void initObjects() {
		int booksCount = 2;
		float newBookX = 0.0f;
		float newBookY = 0.0f;
		
		float pageWidth = settings.getScreenWidth();
		float pageHeight = settings.getScreenHeight();
		
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

			int pagesCount = (b + 1) * 10;
			int pagesPerLine = (int) Math.ceil(Math.sqrt(pagesCount));
			for(int i = 0; i < pagesCount; i++) {
				ReaderPage page = new ReaderPage(pageWidth, pageHeight, settings);
				
				float pageX = (float)(i % pagesPerLine) * settings.getScreenWidth();
				float pageY = (float)(i / pagesPerLine) * settings.getScreenHeight();
				
				page.setPosition(pageX, pageY);
				if(i < 74) {
					page.setIsRead(true);
				}
				if(i == 0) {
					readerBook.setCurrentPage(page);
				}
				
				for(int j = 0; j < 15; j++) {
					ReaderText text = new ReaderText("Привет! Как дела!? У меня норм!");
					text.setPosition(settings.pagePadding, j * (settings.textSize + 2.0f * settings.linesMargin) + settings.pagePadding + settings.textSize);
					text.setPaint(settings.textPaint);
					page.addLine(text);
				}
				
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
			books.add(readerBook);
			
			if(b == 0) {
				this.setCurrentBook(readerBook);
			}
			
			newBookX = newBookX + width + pageWidth;
		}
	}

	/*
	 * Обновление читалки.
	 * Ищет книги, которые необходимо отрисовать.
	 * Необходимо запускать перед отрисовкой.
	 */
	public void update(Canvas canvas, ReaderMode mode, float zoom) {
		booksToDraw = findBooksToDraw(canvas, mode, zoom);
	}
	
	/*
	 * Возвращает книги, которые находятся в пределах экрана
	 */
	private ArrayList<ReaderBook> findBooksToDraw(Canvas canvas, ReaderMode mode, float zoom) {
		ArrayList<ReaderBook> toDraw = new ArrayList<ReaderBook>();
		
		for(int i = 0; i < books.size(); i++) {
			ReaderBook book = books.get(i);
			
			if(book.isInScreen(canvas)) {
				toDraw.add(book);
				book.update(canvas, mode, zoom, settings);
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
	
	/*
	 * Возвращает ближайшую к центру экрана страницу.
	 * 
	 * Расстояние считается от центра экрана до центра страницы.
	 * 
	 * Поиск идет по страницам, которые находятся в пределах экрана,
	 * по этому данный метод следует выполнять после вызова метода update,
	 * который обновляет соответствующий массив.
	 */
	public ReaderPage getNearestPage(float canvasX, float canvasY) {
		ReaderPage nearestPage = null;
		float minDistance = 1000000000.0f;
		
		for(int i = 0; i < booksToDraw.size(); i++) {
			ReaderBook book = booksToDraw.get(i);
			ArrayList<ReaderPage> pages = book.getPagesToDraw();
			for(int j = 0; j < pages.size(); j++) {
				ReaderPage page = pages.get(j);
				
				float pageCenterX = page.getAbsoluteX() + page.getWidth() / 2.0f;
				float pageCenterY = page.getAbsoluteY() + page.getHeight() / 2.0f;
				
				float x = canvasX - pageCenterX;
				float y = canvasY - pageCenterY;
				
				float distance = (float) Math.sqrt(x * x + y * y);
				
				if(distance < minDistance) {
					minDistance = distance;
					nearestPage = page;
				}
			}
		}
		
		return nearestPage;
	}
	
	public void setCurrentBook(ReaderBook book) {
		currentBook = book;
	}
	
	public ReaderBook getCurrentBook() {
		return currentBook;
	}
	
	public ArrayList<ReaderBook> getBooks() {
		return books;
	}
	
	public void addBook(ReaderBook book) {
		books.add(book);
	}
}
