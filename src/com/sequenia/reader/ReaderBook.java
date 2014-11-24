package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

class ReaderBook extends ReaderGroupWithSize {
	private ArrayList<ReaderPage> pages;
	private ArrayList<ReaderPage> pagesToDraw;
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
	
	private Paint borderPaintWhenPagesShown;
	
	public ReaderBook(ReaderSettings settings) {
		super(settings.getScreenWidth(), settings.getScreenHeight());
		pages = new ArrayList<ReaderPage>();
		pagesToDraw = new ArrayList<ReaderPage>();
		currentPage = null;
		
		pagesInterval = new Interval(0.05f, 100.0f, false, true);
		fullInfoInterval = new Interval(0.02f, 0.05f, false, true);
		minInfoInterval = new Interval(0.0001f, 0.02f, false, true);
		
		title = new ArrayList<ReaderText>();
		creator = new ArrayList<ReaderText>();
		year = new ArrayList<ReaderText>();
		
		fullTitle = new ArrayList<ReaderText>();
		fullCreator = new ArrayList<ReaderText>();
		fullYear = new ArrayList<ReaderText>();
		
		borderPaintWhenPagesShown = settings.bookPagesBorderPaint;
	}
	
	public void update(Canvas canvas) {
		pagesToDraw = findPagesToDraw(canvas);
	}
	
	private ArrayList<ReaderPage> findPagesToDraw(Canvas canvas) {
		ArrayList<ReaderPage> toDraw = new ArrayList<ReaderPage>();
		
		for(int i = 0; i < pages.size(); i++) {
			ReaderPage page = pages.get(i);
			
			if(page.isInScreen(canvas)) {
				toDraw.add(page);
			}
		}
		
		return toDraw;
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
	
	public void addPage(ReaderPage page) {
		pages.add(page);
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
