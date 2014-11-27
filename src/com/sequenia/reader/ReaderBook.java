package com.sequenia.reader;

import java.util.ArrayList;

import com.sequenia.reader.ReaderSurface.ReaderMode;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/*
 * Книга читалки. Представляет собой книгу, рисуемую на экране.
 * Содержит в себе страницы.
 * Рисует только те страницы, которые были помещены в список pagesToDraw.
 * 
 * Книга является совокупностью множества фигур, рисуемых на экране:
 *  - Страницы
 *  - Рамки
 *  - Информация о книге (Название, Автор, Год и т.д.)
 *  
 * В зависимости от внешний условий, рисуются только необходимые элементы.
 * Главное из таких условий - уровень зума.
 * От него зависит, в каком виде отображать информацию о книге (Полный или краткий),
 * и нужно ли показывать страницы.
 */
class ReaderBook extends ReaderGroupWithSize {
	private ArrayList<ReaderPage> pages;
	private ArrayList<ReaderPage> pagesToDraw;
	private ArrayList<ReaderPage> fakePages;
	private ArrayList<ReaderPage> fakePagesToDraw;
	private ReaderPage currentPage;
	
	private ArrayList<ReaderText> title;
	private ArrayList<ReaderText> creator;
	private ArrayList<ReaderText> year;
	
	private ArrayList<ReaderText> fullTitle;
	private ArrayList<ReaderText> fullCreator;
	private ArrayList<ReaderText> fullYear;
	
	private Interval pagesInterval;
	private Interval minInfoInterval;
	private Interval fullInfoInterval;
	private Interval fakePagesInterval;
	
	private Paint borderPaintWhenPagesShown;
	
	public ReaderBook(ReaderSettings settings) {
		super(settings.getScreenWidth(), settings.getScreenHeight());
		pages = new ArrayList<ReaderPage>();
		pagesToDraw = new ArrayList<ReaderPage>();
		fakePages = new ArrayList<ReaderPage>();
		fakePagesToDraw = new ArrayList<ReaderPage>();
		currentPage = null;
		
		pagesInterval = new Interval(0.05f, 100.0f, false, true);
		fullInfoInterval = new Interval(0.02f, 0.05f, false, true);
		minInfoInterval = new Interval(0.0001f, 0.02f, false, true);
		fakePagesInterval = new Interval(1.0f, 100.0f, true, true);
		
		title = new ArrayList<ReaderText>();
		creator = new ArrayList<ReaderText>();
		year = new ArrayList<ReaderText>();
		
		fullTitle = new ArrayList<ReaderText>();
		fullCreator = new ArrayList<ReaderText>();
		fullYear = new ArrayList<ReaderText>();
		
		borderPaintWhenPagesShown = settings.bookPagesBorderPaint;
	}
	
	public void update(Canvas canvas, ReaderMode mode, float zoom, ReaderSettings settings) {
		switch(mode) {
		case OVERVIEW:
			pagesToDraw = findPagesToDraw(canvas, pages, zoom, settings);
			break;
			
		case READING:
			pagesToDraw = new ArrayList<ReaderPage>();
			fakePagesToDraw = new ArrayList<ReaderPage>();
			addPageToDrawArray(currentPage);
			if(currentPage != null) {
				addPageToDrawArray(currentPage.getPrevious());
				addPageToDrawArray(currentPage.getNext());
			}
			break;
			
		default:
			break;
		}
		
	}
	
	private ArrayList<ReaderPage> findPagesToDraw(Canvas canvas, ArrayList<ReaderPage> p, float zoom, ReaderSettings settings) {
		ArrayList<ReaderPage> toDraw = new ArrayList<ReaderPage>();
		
		if(pagesInterval.isIn(zoom)) {
			int pagesCount = pages.size();
			int pagesPerLine = (int) Math.ceil(Math.sqrt(pagesCount));
			
			Rect rect = canvas.getClipBounds();
			
			float pageWidth = settings.getScreenWidth();
			float pageHeight = settings.getScreenHeight();
			float x = getAbsoluteX();
			float y = getAbsoluteY();
			
			int startX = (int) Math.floor((rect.left - x) / pageWidth);
			int endX = (int) Math.ceil((rect.right - x) / pageWidth);
			
			int startY = (int) Math.floor((rect.top - y) / pageHeight);
			int endY = (int) Math.ceil((rect.bottom - y) / pageHeight);
			
			for(int i = startY; i < endY; i++) {
				for(int j = startX; j < endX; j++) {
					int index = pagesPerLine * i + j;
					if(index < pagesCount && index >= 0) {
						toDraw.add(pages.get(index));
					}
				}
			}
		}
		
		return toDraw;
	}
	
	private void addPageToDrawArray(ReaderPage page) {
		if(page != null) {
			pagesToDraw.add(page);
			ReaderPage fake = page.getFake();
			if(fake != null) {
				fakePagesToDraw.add(fake);
			}
		}
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		if(minInfoInterval.isIn(zoom)) {
			drawMinInfo(canvas, zoom);
			drawBorders(canvas, zoom);
		}

		if(fullInfoInterval.isIn(zoom)) {
			drawFullInfo(canvas, zoom);
			drawBorders(canvas, zoom);
		}

		if(pagesInterval.isIn(zoom)) {
			drawPages(canvas, zoom);
			drawBordersWhenPagesShown(canvas, zoom);
		}
		
		if(fakePagesInterval.isIn(zoom)) {
			drawFakePages(canvas, zoom);
		}
		return true;
	}
	
	public boolean drawMinInfo(Canvas canvas, float zoom) {
		drawTextArray(title, canvas, zoom);
		drawTextArray(creator, canvas, zoom);
		drawTextArray(year, canvas, zoom);
		return true;
	}
	
	public boolean drawFullInfo(Canvas canvas, float zoom) {
		drawTextArray(fullTitle, canvas, zoom);
		drawTextArray(fullCreator, canvas, zoom);
		drawTextArray(fullYear, canvas, zoom);
		return true;
	}
	
	public boolean drawPages(Canvas canvas, float zoom) {
		for(int i = 0; i < pagesToDraw.size(); i++) {
			pagesToDraw.get(i).draw(canvas, zoom);
		}
		return true;
	}
	
	public boolean drawFakePages(Canvas canvas, float zoom) {
		for(int i = 0; i < fakePagesToDraw.size(); i++) {
			fakePagesToDraw.get(i).draw(canvas, zoom);
		}
		return true;
	}
	
	public boolean drawBordersWhenPagesShown(Canvas canvas, float zoom) {
		ReaderLine[] borders = getBorders();
		for(int i = 0; i < borders.length; i++) {
			borders[i].draw(canvas, zoom, borderPaintWhenPagesShown);
		}
		return true;
	}
	
	private boolean drawTextArray(ArrayList<ReaderText> array, Canvas canvas, float zoom) {
		for(int i = 0; i < array.size(); i++) {
			array.get(i).draw(canvas, zoom);
		}
		return true;
	}
	
	public void setTitle(ArrayList<String> _title, ReaderSettings settings) {
		title = new ArrayList<ReaderText>();
		fullTitle = new ArrayList<ReaderText>();

		int length = _title.size();
		if(length > 0) {
			title.add(createText(_title.get(0), 20.0f, 500.0f, settings.minInfoPaint));
		}
		
		for(int i = 0; i < length; i++) {
			fullTitle.add(createText(_title.get(i), 20.0f, 500.0f + i * 500.0f, settings.fullInfoPaint));
		}
	}
	
	public void setCreator(ArrayList<String> _creator, ReaderSettings settings) {
		creator = new ArrayList<ReaderText>();
		fullCreator = new ArrayList<ReaderText>();

		int length = _creator.size();
		if(length > 0) {
			creator.add(createText(_creator.get(0), 20.0f, 2500.0f, settings.minInfoPaint));
		}
		
		for(int i = 0; i < length; i++) {
			fullCreator.add(createText(_creator.get(i), 20.0f, 2500.0f + i * 500.0f, settings.fullInfoPaint));
		}
	}
	
	public void setYear(ArrayList<String> _year, ReaderSettings settings) {
		year = new ArrayList<ReaderText>();
		fullYear = new ArrayList<ReaderText>();

		int length = _year.size();
		if(length > 0) {
			year.add(createText(_year.get(0), 20.0f, 4500.0f, settings.minInfoPaint));
		}
		
		for(int i = 0; i < length; i++) {
			fullYear.add(createText(_year.get(i), 20.0f, 4500.0f + i * 500.0f, settings.fullInfoPaint));
		}
	}
	
	private ReaderText createText(String text, float x, float y, Paint paint) {
		ReaderText t = new ReaderText(text);
		t.setPosition(x, y);
		t.setPaint(paint);
		addChild(t);
		return t;
	}
	
	public ArrayList<ReaderPage> getPages() {
		return pages;
	}
	
	public ArrayList<ReaderPage> getPagesToDraw() {
		return pagesToDraw;
	}
	
	public ArrayList<ReaderPage> getFakePages() {
		return fakePages;
	}
	
	public ArrayList<ReaderPage> getFakePagesToDraw() {
		return fakePagesToDraw;
	}
	
	public void addPage(ReaderPage page) {
		int pagesCount = pages.size();
		if(pagesCount > 0) {
			ReaderPage lastPage = pages.get(pagesCount - 1);
			lastPage.setNext(page);
			page.setPrevious(lastPage);
		}
		
		pages.add(page);
		addChild(page);
	}
	
	public void addFakePage(ReaderPage page) {
		fakePages.add(page);
		addChild(page);
	}
	
	public void setCurrentPage(ReaderPage page) {
		if(currentPage != null) {
			currentPage.setIsCurrent(false);
		}
		
		page.setIsRead(true);
		page.setIsCurrent(true);
		currentPage = page;
	}
	
	public ReaderPage getCurrentPage() {
		return currentPage;
	}
}
