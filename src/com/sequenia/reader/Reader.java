package com.sequenia.reader;

import android.graphics.Canvas;

public class Reader {
	private ReaderSettings settings;
	
	public Reader() {
		settings = new ReaderSettings();
	}
	
	public void draw(Canvas canvas, long delta) {
		canvas.drawPaint(settings.bgPaint);
		canvas.drawText("Привет!", 10.0f, 40.0f, settings.textPaint);
	}
}
