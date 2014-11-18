package com.sequenia.reader;

import android.graphics.Color;
import android.graphics.Paint;

public class ReaderSettings {
	Paint bgPaint;
	private int bgColor = Color.WHITE;
	
	Paint textPaint;
	private int textColor = Color.BLACK;
	private float textSize = 30.0f;
	
	float stopAccel = -3000.0f;
	float scaleAccel = 0.01f;
	float accelScaleSensivity = 1.5f;
	
	float maxScaleVelocity = 30.0f;
	float minScaleVelocity = 1.0f / maxScaleVelocity;
	
	//float[] zoomByLevels = {1.0f, 0.75f, 0.5f, 0.25f, 0.1f, 0.075f, 0.05f, 0.025f, 0.01f, 0.0075f};

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
