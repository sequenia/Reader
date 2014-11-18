package com.sequenia.reader;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class ReaderObject {
	private PointF position;
	private PointF absolutePosition;
	private ArrayList<ReaderObject> children;
	private ReaderObject parent;
	
	public ReaderObject() {
		position = new PointF(0.0f, 0.0f);
		absolutePosition = new PointF(0.0f, 0.0f);
		children = new ArrayList<ReaderObject>();
		parent = null;
	}
	
	public void draw(Canvas canvas) {
		for(int i = 0; i < children.size(); i++) {
			children.get(i).draw(canvas);
		}
	}
	
	public void setPosition(PointF _position) {
		position.x = _position.x;
		position.y = _position.y;
		
		setAbsolutePosition();
	}
	
	public void setPosition(float x, float y) {
		position.x = x;
		position.y = y;
		
		setAbsolutePosition();
	}
	
	public PointF getPosition() {
		return new PointF(position.x, position.y);
	}
	
	public float getPositionX() {
		return position.x;
	}
	
	public float getPositionY() {
		return position.y;
	}
	
	public void setParent(ReaderObject _parent) {
		parent = _parent;
		
		setAbsolutePosition();
	}
	
	public ReaderObject getParent() {
		return parent;
	}
	
	public void addChild(ReaderObject _child) {
		_child.setParent(this);
		children.add(_child);
	}
	
	public ReaderObject getChild(int _index) {
		return children.get(_index);
	}
	
	public void setAbsolutePosition() {
		if(parent != null) {
			absolutePosition.x = parent.getAbsoluteX() + position.x;
			absolutePosition.y = parent.getAbsoluteY() + position.y;
		} else {
			absolutePosition.x = position.x;
			absolutePosition.y = position.y;
		}
		
		for(int i = 0; i < children.size(); i++) {
			children.get(i).setAbsolutePosition();
		}
	}
	
	public PointF getAbsolutePosition() {
		return new PointF(absolutePosition.x, absolutePosition.y);
	}
	
	public float getAbsoluteX() {
		return absolutePosition.x;
	}
	
	public float getAbsoluteY() {
		return absolutePosition.y;
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
	public void draw(Canvas canvas) {
		canvas.drawText(text, getAbsoluteX(), getAbsoluteY(), paint);
	}
}

class ReaderLine extends ReaderObject {
	private Paint paint;
	private PointF end;
	
	public ReaderLine() {
		super();
		paint = new Paint();
		end = new PointF();
	}
	
	public ReaderLine(PointF _start, PointF _end) {
		super();
		paint = new Paint();
		setPosition(_start.x, _start.y);
		end = new PointF(_end.x, _end.y);
	}
	
	public void setEnd(PointF _end) {
		end = new PointF(_end.x, _end.y);
	}
	
	public PointF getEnd() {
		return new PointF(end.x, end.y);
	}
	
	public void setPaint(Paint _paint) {
		paint = _paint;
	}
	
	public Paint getPaint() {
		return paint;
	}
	
	@Override
	public void draw(Canvas canvas) {
		float absX = getAbsoluteX();
		float absY = getAbsoluteY();
		canvas.drawLine(absX, absY, absX - getPositionX() + end.x, absY - getPositionY() + end.y, paint);
	}
}

class ReaderPage extends ReaderObject {
	private float width = 0.0f;
	private float height = 0.0f;
	
	public ReaderPage(float _width, float _height, Paint borderPaint) {
		super();
		width = _width;
		height = _height;
		createBorders(borderPaint);
	}
	
	private void createBorders(Paint borderPaint) {
		ReaderLine borderTop = new ReaderLine(new PointF(0.0f, 0.0f), new PointF(width, 0.0f));
		ReaderLine borderRight = new ReaderLine(new PointF(width, 0.0f), new PointF(width, height));
		ReaderLine borderBottom = new ReaderLine(new PointF(width, height), new PointF(0.0f, height));
		ReaderLine borderLeft = new ReaderLine(new PointF(0.0f, height), new PointF(0.0f, 0.0f));
		
		borderTop.setPaint(borderPaint);
		borderRight.setPaint(borderPaint);
		borderBottom.setPaint(borderPaint);
		borderLeft.setPaint(borderPaint);
		
		addChild(borderTop);
		addChild(borderRight);
		addChild(borderBottom);
		addChild(borderLeft);
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
