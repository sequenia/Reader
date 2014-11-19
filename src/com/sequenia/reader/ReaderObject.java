package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

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
	
	public void draw(Canvas canvas, int zoomLevel) {
		
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
	public void draw(Canvas canvas, int zoomLevel) {
		super.draw(canvas, zoomLevel);
		for(int i = 0; i < children.size(); i++) {
			children.get(i).draw(canvas, zoomLevel);
		}
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
	public void draw(Canvas canvas, int zoomLevel) {
		canvas.drawText(text, getAbsoluteX(), getAbsoluteY(), paint);
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
	public void draw(Canvas canvas, int zoomLevel) {
		float absX = getAbsoluteX();
		float absY = getAbsoluteY();
		canvas.drawLine(absX, absY, absX - getPositionX() + endX, absY - getPositionY() + endY, paint);
	}
}

class ReaderPage extends ReaderGroup {
	private float width = 0.0f;
	private float height = 0.0f;
	private ReaderLine[] borders;
	private ArrayList<ReaderText> lines;
	
	public ReaderPage(float _width, float _height, Paint borderPaint) {
		super();
		width = _width;
		height = _height;
		borders = new ReaderLine[4];
		lines = new ArrayList<ReaderText>();
		createBorders(borderPaint);
	}
	
	@Override
	public void draw(Canvas canvas, int zoomLevel) {
		for(int i = 0; i < borders.length; i++) {
			borders[i].draw(canvas, zoomLevel);
		}
		
		for(int i = 0; i < lines.size(); i++) {
			lines.get(i).draw(canvas, zoomLevel);
		}
	}
	
	private void createBorders(Paint borderPaint) {
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
	
	public void addLine(ReaderText line) {
		lines.add(line);
		addChild(line);
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
}
