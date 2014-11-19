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

class ReaderBook extends ReaderGroupWithSize {
	private ArrayList<ReaderPage> pages;
	
	private ArrayList<ReaderText> title;
	private ArrayList<ReaderText> creator;
	private ArrayList<ReaderText> year;
	
	private ArrayList<ReaderText> fullTitle;
	private ArrayList<ReaderText> fullCreator;
	private ArrayList<ReaderText> fullYear;
	
	private Interval pagesInterval;
	private Interval minInfoInterval;
	private Interval fullInfoInterval;
	
	private Paint borderPaintWhenPagesShown;
	
	public ReaderBook(ReaderSettings settings) {
		super(settings.screenWidth, settings.screenHeight);
		pages = new ArrayList<ReaderPage>();
		pagesInterval = new Interval(0.05f, 100.0f, false, true);
		fullInfoInterval = new Interval(0.02f, 0.05f, false, true);
		minInfoInterval = new Interval(0.0001f, 0.02f, false, true);
		
		title = new ArrayList<ReaderText>();
		creator = new ArrayList<ReaderText>();
		year = new ArrayList<ReaderText>();
		
		fullTitle = new ArrayList<ReaderText>();
		fullCreator = new ArrayList<ReaderText>();
		fullYear = new ArrayList<ReaderText>();
		
		borderPaintWhenPagesShown = settings.bookPagesBorderPaint;
	}
	
	@Override
	public boolean draw(Canvas canvas, float zoom) {
		boolean inScreen = super.draw(canvas, zoom);
		
		if(inScreen) {
			if(minInfoInterval.isIn(zoom)) {
				System.out.println("min");
				drawMinInfo(canvas, zoom);
				drawBorders(canvas, zoom);
			}

			if(fullInfoInterval.isIn(zoom)) {
				System.out.println("max");
				drawFullInfo(canvas, zoom);
				drawBorders(canvas, zoom);
			}

			if(pagesInterval.isIn(zoom)) {
				drawPages(canvas, zoom);
				drawBordersWhenPagesShown(canvas, zoom);
			}
		}

		return inScreen;
	}
	
	public boolean drawMinInfo(Canvas canvas, float zoom) {
		drawTextArray(title, canvas, zoom);
		drawTextArray(creator, canvas, zoom);
		drawTextArray(year, canvas, zoom);
		return true;
	}
	
	public boolean drawFullInfo(Canvas canvas, float zoom) {
		drawTextArray(fullTitle, canvas, zoom);
		drawTextArray(fullCreator, canvas, zoom);
		drawTextArray(fullYear, canvas, zoom);
		return true;
	}
	
	public boolean drawPages(Canvas canvas, float zoom) {
		for(int i = 0; i < pages.size(); i++) {
			pages.get(i).draw(canvas, zoom);
		}
		return true;
	}
	
	public boolean drawBordersWhenPagesShown(Canvas canvas, float zoom) {
		ReaderLine[] borders = getBorders();
		for(int i = 0; i < borders.length; i++) {
			borders[i].draw(canvas, zoom, borderPaintWhenPagesShown);
		}
		return true;
	}
	
	private boolean drawTextArray(ArrayList<ReaderText> array, Canvas canvas, float zoom) {
		for(int i = 0; i < array.size(); i++) {
			array.get(i).draw(canvas, zoom);
		}
		return true;
	}
	
	public void setTitle(ArrayList<String> _title, ReaderSettings settings) {
		title = new ArrayList<ReaderText>();
		fullTitle = new ArrayList<ReaderText>();

		int length = _title.size();
		if(length > 0) {
			title.add(createText(_title.get(0), 20.0f, 500.0f, settings.minInfoPaint));
		}
		
		for(int i = 0; i < length; i++) {
			fullTitle.add(createText(_title.get(i), 20.0f, 500.0f + i * 500.0f, settings.fullInfoPaint));
		}
	}
	
	public void setCreator(ArrayList<String> _creator, ReaderSettings settings) {
		creator = new ArrayList<ReaderText>();
		fullCreator = new ArrayList<ReaderText>();

		int length = _creator.size();
		if(length > 0) {
			creator.add(createText(_creator.get(0), 20.0f, 2500.0f, settings.minInfoPaint));
		}
		
		for(int i = 0; i < length; i++) {
			fullCreator.add(createText(_creator.get(i), 20.0f, 2500.0f + i * 500.0f, settings.fullInfoPaint));
		}
	}
	
	public void setYear(ArrayList<String> _year, ReaderSettings settings) {
		year = new ArrayList<ReaderText>();
		fullYear = new ArrayList<ReaderText>();

		int length = _year.size();
		if(length > 0) {
			year.add(createText(_year.get(0), 20.0f, 4500.0f, settings.minInfoPaint));
		}
		
		for(int i = 0; i < length; i++) {
			fullYear.add(createText(_year.get(i), 20.0f, 4500.0f + i * 500.0f, settings.fullInfoPaint));
		}
	}
	
	private ReaderText createText(String text, float x, float y, Paint paint) {
		ReaderText t = new ReaderText(text);
		t.setPosition(x, y);
		t.setPaint(paint);
		addChild(t);
		return t;
	}
	
	public ArrayList<ReaderPage> getPages() {
		return pages;
	}
	
	public void addPage(ReaderPage page) {
		pages.add(page);
		addChild(page);
	}
}

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
