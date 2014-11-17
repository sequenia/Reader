package com.sequenia.reader;

import android.graphics.Color;
import android.graphics.Paint;

public class ReaderSettings {
	Paint bgPaint;
	private int bgColor = Color.WHITE;
	
	Paint textPaint;
	private int textColor = Color.BLACK;
	private float textSize = 30.0f;

	public ReaderSettings() {
		initPaint();
	}
	
	private void initPaint() {
		bgPaint = new Paint();
		bgPaint.setColor(bgColor);
		bgPaint.setStyle(Paint.Style.FILL);
		
		textPaint = new Paint();
		textPaint.setColor(textColor);
		textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint.setTextSize(textSize);
		textPaint.setAntiAlias(true);
	}
}
