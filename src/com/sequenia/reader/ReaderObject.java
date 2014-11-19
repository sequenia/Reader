package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ReaderObject {
	private float x;
	private float y;
	private float absX;
	private float absY;
	private ReaderGroup parent;
	
	public ReaderObject() {
		x = 0.0f;
		y = 0.0f;
		absX = 0.0f;
		absY = 0.0f;
		parent = null;
	}
	
	public boolean draw(Canvas canvas, float zoom) {
		return true;
	}
	
	public void setPosition(float _x, float _y) {
		x = _x;
		y = _y;
		
		setAbsolutePosition();
	}
	
	public float getPositionX() {
		return x;
	}
	
	public float getPositionY() {
		return y;
	}
	
	public void setAbsolutePosition() {
		if(parent != null) {
			absX = parent.getAbsoluteX() + getPositionX();
			absY = parent.getAbsoluteY() + getPositionY();
		} else {
			absX = getPositionX();
			absY = getPositionY();
		}
	}
	
	public float getAbsoluteX() {
		return absX;
	}
	
	public float getAbsoluteY() {
		return absY;
	}
	
	public void setParent(ReaderGroup _parent) {
		parent = _parent;
		
		setAbsolutePosition();
	}
	
	public ReaderGroup getParent() {
		return parent;
	}
}

class ReaderGroup extends ReaderObject {
	private ArrayList<ReaderObject> children;
	
	public ReaderGroup() {
		super();
		children = new ArrayList<ReaderObject>();
	}
	
	public void addChild(ReaderObject _child) {
		_child.setParent(this);
		children.add(_child);
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		for(int i = 0; i < children.size(); i++) {
			children.get(i).draw(canvas, zoom);
		}
		return super.draw(canvas, zoom);
	}
	
	@Override
	public void setAbsolutePosition() {
		super.setAbsolutePosition();
		
		for(int i = 0; i < children.size(); i++) {
			children.get(i).setAbsolutePosition();
		}
	}
}

class ReaderText extends ReaderObject {
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
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		canvas.drawText(text, getAbsoluteX(), getAbsoluteY(), paint);
		return true;
	}
}

class ReaderLine extends ReaderObject {
	private Paint paint;
	private float endX;
	private float endY;
	
	public ReaderLine() {
		super();
		paint = new Paint();
		endX = 0.0f;
		endY = 0.0f;
	}
	
	public ReaderLine(float _startX, float _startY, float _endX, float _endY) {
		super();
		paint = new Paint();
		setPosition(_startX, _startY);
		endX = _endX;
		endY = _endY;
	}
	
	public void setEnd(float _endX, float _endY) {
		endX = _endX;
		endY = _endY;
	}
	
	public float getEndX() {
		return endX;
	}
	
	public float getEndY() {
		return endY;
	}
	
	public void setPaint(Paint _paint) {
		paint = _paint;
	}
	
	public Paint getPaint() {
		return paint;
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		float absX = getAbsoluteX();
		float absY = getAbsoluteY();
		canvas.drawLine(absX, absY, absX - getPositionX() + endX, absY - getPositionY() + endY, paint);
		return true;
	}
	
	public boolean draw(Canvas canvas, float zoom, Paint _paint) {
		float absX = getAbsoluteX();
		float absY = getAbsoluteY();
		canvas.drawLine(absX, absY, absX - getPositionX() + endX, absY - getPositionY() + endY, _paint);
		return true;
	}
}

class ReaderGroupWithSize extends ReaderGroup {
	private float width = 0.0f;
	private float height = 0.0f;
	private ReaderLine[] borders;
	
	public ReaderGroupWithSize() {
		borders = new ReaderLine[4];
	}
	
	public ReaderGroupWithSize(float _width, float _height) {
		width = _width;
		height = _height;
		borders = new ReaderLine[4];
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		return isInScreen(canvas);
	}
	
	public boolean drawBorders(Canvas canvas, float zoom) {
		for(int i = 0; i < borders.length; i++) {
			borders[i].draw(canvas, zoom);
		}
		return true;
	}
	
	public void createBorders(Paint borderPaint) {
		ReaderLine borderTop = new ReaderLine(0.0f, 0.0f, width, 0.0f);
		ReaderLine borderRight = new ReaderLine(width, 0.0f, width, height);
		ReaderLine borderBottom = new ReaderLine(width, height, 0.0f, height);
		ReaderLine borderLeft = new ReaderLine(0.0f, height, 0.0f, 0.0f);
		
		borderTop.setPaint(borderPaint);
		borderRight.setPaint(borderPaint);
		borderBottom.setPaint(borderPaint);
		borderLeft.setPaint(borderPaint);
		
		addChild(borderTop);
		addChild(borderRight);
		addChild(borderBottom);
		addChild(borderLeft);
		
		borders[0] = borderTop;
		borders[1] = borderRight;
		borders[2] = borderBottom;
		borders[3] = borderLeft;
	}
	
	private boolean isInScreen(Canvas canvas) {
		Rect rect = canvas.getClipBounds();
		return !(getAbsoluteX() > rect.right ||
				getAbsoluteX() + width < rect.left ||
				getAbsoluteY() > rect.bottom ||
				getAbsoluteY() + height < rect.top);
	}
	
	public void setWidth(float _width) {
		width = _width;
	}
	
	public float getWidth() {
		return width;
	}
	
	public void setHeight(float _height) {
		height = _height;
	}
	
	public float getHeight() {
		return height;
	}
	
	public ReaderLine[] getBorders() {
		return borders;
	}
}
