package com.sequenia.reader_objects;

import java.util.ArrayList;

import com.sequenia.reader.ReaderBookCreator;
import com.sequenia.reader.ReaderSettings;

import android.graphics.Canvas;
import android.graphics.Paint;

/*
 * Страница книги. Рисует страницу, состоящую из множества элементов:
 *  - Строки текста
 *  - Рамки
 *  - Другая информация
 *  
 * Страница и ее элементы могут иметь различный вид в зависимости от
 * уровня зума.
 */
public class ReaderPage extends ReaderGroupWithSize {
	private ArrayList<ReaderText> lines;
	private ReaderPage fake;
	private ReaderPage next;
	private ReaderPage previous;
	
	private Paint bgPaint;
	private Paint readBgPaint;
	private Paint currentBgPaint;
	private Paint currentBorderPaint;
	private Paint fakeTextPaint;
	
	private Interval bgInterval;
	private Interval textInterval;
	private Interval fakeTextInterval;
	
	private boolean isRead = false;
	private boolean isCurrent = false;
	
	public ReaderPage(float _width, float _height, ReaderSettings settings) {
		super(_width, _height);

		lines = new ArrayList<ReaderText>();
		next = null;
		previous = null;
		fake = null;
		
		bgInterval = new Interval(0.05f, 0.6f, false, true);
		fakeTextInterval = new Interval(0.1f, 0.4f, false, true);
		textInterval = new Interval(0.4f, 100.0f, false, true);
		
		createBorders(settings.pageBorderPaint);

		bgPaint = settings.pageBgPaint;
		currentBgPaint = settings.currentPageBgPaint;
		readBgPaint = settings.readPageBgPaint;
		currentBorderPaint = settings.currentPageBorderPaint;
		fakeTextPaint = settings.fakeTextPaint;
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		if(bgInterval.isIn(zoom)) {
			drawBackground(canvas, zoom);
			drawBorders(canvas, zoom);
		} else {
			drawBorders(canvas, zoom, true);
		}

		if(textInterval.isIn(zoom)) {
			drawText(canvas, zoom);
		}
		
		if(fakeTextInterval.isIn(zoom)) {
			drawFakeText(canvas, zoom);
		}

		return true;
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
	
	public boolean drawFakeText(Canvas canvas, float zoom) {
		for(int i = 0; i < lines.size(); i++) {
			ReaderText line = lines.get(i);
			
			Paint textPaint = line.getPaint();
			Paint paint = fakeTextPaint;
			float textSize = textPaint.getTextSize();
			float x = line.getAbsoluteX();
			float y = line.getAbsoluteY();
			float width = ReaderBookCreator.getTextWidth(line.getText(), textPaint);
			float height = textSize / 2.0f;
			
			canvas.drawRect(x, y, x + width, y + height, paint);
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
	
	public ReaderPage createFake(ReaderSettings settings, float positionX, float positionY) {
		ReaderPage copy = new ReaderPage(getWidth(), getHeight(), settings);
		
		copy.setParent(getParent());
		copy.setPosition(positionX, positionY);
		
		for(int i = 0; i < lines.size(); i++) {
			copy.addLine(lines.get(i).clone());
		}
		
		copy.setNext(next);
		copy.setPrevious(previous);
		
		this.setFake(copy);
		
		return copy;
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
	
	public void setNext(ReaderPage page) {
		next = page;
	}
	
	public ReaderPage getNext() {
		return next;
	}
	
	public void setPrevious(ReaderPage page) {
		previous = page;
	}
	
	public ReaderPage getPrevious() {
		return previous;
	}
	
	public void setFake(ReaderPage page) {
		fake = page;
	}
	
	public ReaderPage getFake() {
		return fake;
	}
}