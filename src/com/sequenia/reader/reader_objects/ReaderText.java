package com.sequenia.reader.reader_objects;

import android.graphics.Canvas;
import android.graphics.Paint;

/*
 * Рисуемый текст
 */
public class ReaderText extends ReaderObject {
	private String text;
	private Paint paint;
	
	public ReaderText() {
		super();
		text = "";
		paint = new Paint();
	}
	
	public ReaderText(String _text) {
		super();
		text = _text;
		paint = new Paint();
	}
	
	public void setText(String _text) {
		text = _text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setPaint(Paint _paint) {
		paint = _paint;
	}
	
	public Paint getPaint() {
		return paint;
	}
	
	public ReaderText clone() {
		ReaderText copy = new ReaderText(text);
		copy.setPaint(this.paint);
		copy.setParent(this.getParent());
		copy.setPosition(getPositionX(), getPositionY());
		return copy;
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		canvas.drawText(text, getAbsoluteX(), getAbsoluteY(), paint);
		return true;
	}
}
