package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

class ReaderPage extends ReaderGroupWithSize {
	private ArrayList<ReaderText> lines;
	
	private Paint bgPaint;
	private Paint readBgPaint;
	private Paint currentBgPaint;
	private Paint currentBorderPaint;
	
	private Interval bgInterval;
	private Interval textInterval;
	
	private boolean isRead = false;
	private boolean isCurrent = false;
	
	public ReaderPage(float _width, float _height, ReaderSettings settings) {
		super(_width, _height);
		lines = new ArrayList<ReaderText>();
		bgInterval = new Interval(0.05f, 0.25f, false, true);
		textInterval = new Interval(0.1f, 100.0f, false, true);
		createBorders(settings.pageBorderPaint);
		bgPaint = settings.pageBgPaint;
		currentBgPaint = settings.currentPageBgPaint;
		readBgPaint = settings.readPageBgPaint;
		currentBorderPaint = settings.currentPageBorderPaint;
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		boolean inScreen = super.draw(canvas, zoom);
		
		if(inScreen) {
			if(bgInterval.isIn(zoom)) {
				drawBackground(canvas, zoom);
				drawBorders(canvas, zoom);
			} else {
				drawBorders(canvas, zoom, true);
			}

			if(textInterval.isIn(zoom)) {
				drawText(canvas, zoom);
			}
		}

		return inScreen;
	}
	
	public boolean drawBorders(Canvas canvas, float zoom, boolean reactToCurrent) {
		if(reactToCurrent && isCurrent) {
			ReaderLine[] borders = getBorders();
			for(int i = 0; i < borders.length; i++) {
				borders[i].draw(canvas, zoom, currentBorderPaint);
			}
		} else {
			drawBorders(canvas, zoom);
		}
		return true;
	}
	
	public boolean drawText(Canvas canvas, float zoom) {
		for(int i = 0; i < lines.size(); i++) {
			lines.get(i).draw(canvas, zoom);
		}
		return true;
	}
	
	private boolean drawBackground(Canvas canvas, float zoom) {
		Paint paint;
		if(isCurrent) {
			paint = currentBgPaint;
		} else {
			if(isRead) {
				paint = readBgPaint;
			} else {
				paint = bgPaint;
			}
		}
		canvas.drawRect(getAbsoluteX(), getAbsoluteY(), getAbsoluteX() + getWidth(), getAbsoluteY() + getHeight(), paint);
		return true;
	}
	
	public void addLine(ReaderText line) {
		lines.add(line);
		addChild(line);
	}
	
	public void setIsRead(boolean _isRead) {
		isRead = _isRead;
	}
	
	public boolean getIsRead() {
		return isRead;
	}
	
	public void setIsCurrent(boolean _isCurrent) {
		isCurrent = _isCurrent;
	}
	
	public boolean getIsCurrent() {
		return isCurrent;
	}
}