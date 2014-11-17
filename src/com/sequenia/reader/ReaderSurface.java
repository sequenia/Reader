package com.sequenia.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ReaderSurface extends GestureSurface {
	public static enum ReaderState {
		NOTHING, TRANSLATION, SCALING
	}

	private ReaderState state;
	private Reader reader;
	
	float currentX = 0.0f;
	float currentY = 0.0f;
	float prevX = 0.0f;
	float prevY = 0.0f;
	
	float scaleFactor = 1.0f;
	
	private int mActivePointerId = 0;

	public ReaderSurface(Context _context) {
		super(_context);
		
		state = ReaderState.NOTHING;
		reader = new Reader();
	}
	
	@Override
	public void draw(Canvas canvas, long delta) {
		update(delta);
		
		canvas.translate(currentX, currentY);
		canvas.scale(scaleFactor, scaleFactor);

		reader.draw(canvas, delta);
	}
	
	private void update(long delta) {
		
	}
	
	private void scaleCanvas(float dScale, PointF focus) {
		scaleFactor *= dScale;
		
		// Ищем вектор, указывающий из фокуса в позицию канваса
		PointF vecToPos = new PointF(currentX - focus.x, currentY - focus.y);
		
		float vecMultiplyer = dScale - 1;
		currentX += vecToPos.x * vecMultiplyer;
		currentY += vecToPos.y * vecMultiplyer;
	}
	
	@Override
	public void onSurfaceTouch(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		boolean activePointerIdChanged = activePointerChanged(event);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if(state != ReaderState.SCALING && !activePointerIdChanged) {
				state = ReaderState.TRANSLATION;

				float dx = x - prevX;
				float dy = y - prevY;
				
				currentX += dx;
				currentY += dy;
			}

			break;
			
		case MotionEvent.ACTION_UP:
			state = ReaderState.NOTHING;			
			break;

		default:
			break;
		}
		
		prevX = x;
		prevY = y;
	}
	
	@Override
	public void onSurfaceScale(ScaleGestureDetector detector) {
		float dScale = detector.getScaleFactor();
		PointF focus = new PointF(detector.getFocusX(), detector.getFocusY());
		scaleCanvas(dScale, focus);
	}
	
	@Override
	public void onSurfaceScaleBegin(ScaleGestureDetector detector) {
		state = ReaderState.SCALING;
	}
	
	@Override
	public void onSurfaceScaleEnd(ScaleGestureDetector detector) {
		state = ReaderState.NOTHING;
	}
	
	private boolean activePointerChanged(MotionEvent event) {
		boolean activePointerIdChanged = false;
		int newActivePointerId = event.getPointerId(0);
		if(newActivePointerId != mActivePointerId) {
			activePointerIdChanged = true;
		}
		mActivePointerId = newActivePointerId;
		
		return activePointerIdChanged;
	}
}
