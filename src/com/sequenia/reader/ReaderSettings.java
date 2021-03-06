package com.sequenia.reader;

import android.graphics.Color;
import android.graphics.Paint;

/*
 * Настройки читалки и элементы по умолчанию
 */
public class ReaderSettings {
	public Paint bgPaint;
	private int bgColor = Color.WHITE;
	
	public Paint textPaint;
	private int textColor = Color.BLACK;
	public float textSize = 25.0f;
	public float pagePadding = 25.0f;
	public float linesMargin = 5.0f;
	
	public Paint fakeTextPaint;
	private int fakeTextColor = Color.rgb(159, 199, 159);
	
	public Paint minInfoPaint;
	private int minInfoColor = Color.BLACK;
	private float minInfoSize = 700.0f;
	
	public Paint fullInfoPaint;
	private int fullInfoColor = Color.BLACK;
	private float fullInfoSize = 350.0f;
	
	public Paint pageBorderPaint;
	private int pageBorderColor = Color.GRAY;
	public float pageBorderSize = 1.0f;
	
	public Paint currentPageBorderPaint;
	private int currentPageBorderColor = Color.GRAY;
	public float currentPageBorderSize = 3.0f;
	
	public Paint bookPagesBorderPaint;
	private int bookPagesBorderColor = Color.GRAY;
	public float bookPagesBorderSize = 3.0f;
	
	public Paint bookBorderPaint;
	private int bookBorderColor = Color.GRAY;
	public float bookBorderSize = 1.0f;
	
	public Paint pageBgPaint;
	private int pageBgColor = Color.rgb(228, 255, 233);
	
	public Paint readPageBgPaint;
	private int readPageBgColor = Color.rgb(179, 255, 191);
	
	public Paint currentPageBgPaint;
	private int currentPageBgColor = Color.rgb(112, 221, 129);
	
	public float stopAccel = -3000.0f;
	public float scaleAccel = 0.01f;
	public float accelScaleSensivity = 1.5f;
	public float toReadModeSensivity = 0.6f;
	public float toReadTime = 0.2f;
	public float pageSlideTime = 0.1f;
	public float pageSlideSensivity = 0.2f;
	
	public float maxScaleVelocity = 30.0f;
	public float minScaleVelocity = 1.0f / maxScaleVelocity;
	
	private float screenWidth = 0.0f;
	private float screenHeight = 0.0f;
	public float halfScreenWidth = 0.0f;
	public float halfScreenHeight = 0.0f;

	public ReaderSettings() {
		initPaint();
	}
	
	private void initPaint() {
		bgPaint = new Paint();
		bgPaint.setColor(bgColor);
		bgPaint.setStyle(Paint.Style.FILL);
		
		pageBgPaint = new Paint();
		pageBgPaint.setColor(pageBgColor);
		pageBgPaint.setStyle(Paint.Style.FILL);
		
		readPageBgPaint = new Paint();
		readPageBgPaint.setColor(readPageBgColor);
		readPageBgPaint.setStyle(Paint.Style.FILL);
		
		currentPageBgPaint = new Paint();
		currentPageBgPaint.setColor(currentPageBgColor);
		currentPageBgPaint.setStyle(Paint.Style.FILL);
		
		textPaint = new Paint();
		textPaint.setColor(textColor);
		textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		textPaint.setTextSize(textSize);
		textPaint.setAntiAlias(true);
		
		fakeTextPaint = new Paint();
		fakeTextPaint.setColor(fakeTextColor);
		fakeTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		fakeTextPaint.setAntiAlias(true);
		
		minInfoPaint = new Paint();
		minInfoPaint.setColor(minInfoColor);
		minInfoPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		minInfoPaint.setTextSize(minInfoSize);
		minInfoPaint.setAntiAlias(true);
		
		fullInfoPaint = new Paint();
		fullInfoPaint.setColor(fullInfoColor);
		fullInfoPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		fullInfoPaint.setTextSize(fullInfoSize);
		fullInfoPaint.setAntiAlias(true);
		
		pageBorderPaint = new Paint();
		pageBorderPaint.setColor(pageBorderColor);
		pageBorderPaint.setStrokeWidth(pageBorderSize);
		pageBorderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		pageBorderPaint.setAntiAlias(true);
		
		currentPageBorderPaint = new Paint();
		currentPageBorderPaint.setColor(currentPageBorderColor);
		currentPageBorderPaint.setStrokeWidth(currentPageBorderSize);
		currentPageBorderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		currentPageBorderPaint.setAntiAlias(true);
		
		bookPagesBorderPaint = new Paint();
		bookPagesBorderPaint.setColor(bookPagesBorderColor);
		bookPagesBorderPaint.setStrokeWidth(bookPagesBorderSize);
		bookPagesBorderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		bookPagesBorderPaint.setAntiAlias(true);
		
		bookBorderPaint = new Paint();
		bookBorderPaint.setColor(bookBorderColor);
		bookBorderPaint.setStrokeWidth(bookBorderSize);
		bookBorderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		bookBorderPaint.setAntiAlias(true);
	}
	
	public float getScreenWidth() {
		return screenWidth;
	}
	
	public void setScreenWidth(float _screenWidth) {
		screenWidth = _screenWidth;
		halfScreenWidth = screenWidth / 2.0f;
	}
	
	public float getScreenHeight() {
		return screenHeight;
	}
	
	public void setScreenHeight(float _screenHeight) {
		screenHeight = _screenHeight;
		halfScreenHeight = screenHeight / 2.0f;
	}
}
