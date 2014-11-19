package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

class ReaderBook extends ReaderGroupWithSize {
	private ArrayList<ReaderPage> pages;
	
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
		super(settings.screenWidth, settings.screenHeight);
		pages = new ArrayList<ReaderPage>();
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
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		boolean inScreen = super.draw(canvas, zoom);
		
		if(inScreen) {
			if(minInfoInterval.isIn(zoom)) {
				System.out.println("min");
				drawMinInfo(canvas, zoom);
				drawBorders(canvas, zoom);
			}

			if(fullInfoInterval.isIn(zoom)) {
				System.out.println("max");
				drawFullInfo(canvas, zoom);
				drawBorders(canvas, zoom);
			}

			if(pagesInterval.isIn(zoom)) {
				drawPages(canvas, zoom);
				drawBordersWhenPagesShown(canvas, zoom);
			}
		}

		return inScreen;
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
		for(int i = 0; i < pages.size(); i++) {
			pages.get(i).draw(canvas, zoom);
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
	
	public void addPage(ReaderPage page) {
		pages.add(page);
		addChild(page);
	}
}