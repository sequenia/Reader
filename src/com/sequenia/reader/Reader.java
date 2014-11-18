package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;

public class Reader {
	private ReaderSettings settings;
	private ArrayList<ReaderPage> pages;
	
	public Reader() {
		settings = new ReaderSettings();
		pages = new ArrayList<ReaderPage>();
		initObjects();
	}
	
	public Reader(ReaderSettings _settings) {
		settings = _settings;
		pages = new ArrayList<ReaderPage>();
		initObjects();
	}
	
	public void initObjects() {
		for(int i = 0; i < 10; i++) {
			ReaderPage page = new ReaderPage(settings.screenWidth, settings.screenHeight, settings.pageBorderPaint);
			
			page.setPosition(i * settings.screenWidth, 0.0f);
			
			for(int j = 0; j < 10; j++) {
				ReaderText text = new ReaderText("Привет! Как дела!?");
				text.setPosition(10.0f, j * 50.0f + 50.0f);
				text.setPaint(settings.textPaint);
				page.addChild(text);
			}
			
			pages.add(page);
		}
	}
	
	public void draw(Canvas canvas, long delta, float zoom) {
		canvas.drawPaint(settings.bgPaint);
		
		for(int i = 0; i < pages.size(); i++) {
			pages.get(i).draw(canvas, zoom);
		}
	}
}
