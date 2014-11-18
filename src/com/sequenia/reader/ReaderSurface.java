package com.sequenia.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class ReaderSurface extends GestureSurface {
	public static enum ReaderState {
		NOTHING, TRANSLATION, SCALING, ACCEL_TRANSLATION
	}
	Translation translation;

	private ReaderState state;
	private Reader reader;
	
	float currentX = 0.0f;
	float currentY = 0.0f;
	float prevX = 0.0f;
	float prevY = 0.0f;
	float scaleFactor = 1.0f;
	private int mActivePointerId = 0;	

	ReaderSettings settings;

	public ReaderSurface(Context _context) {
		super(_context);
		
		settings = new ReaderSettings();
		state = ReaderState.NOTHING;
		reader = new Reader(settings);
		translation = null;
	}
	
	@Override
	public void draw(Canvas canvas, long delta) {
		update(delta);
		
		canvas.translate(currentX, currentY);
		canvas.scale(scaleFactor, scaleFactor);

		reader.draw(canvas, delta);
	}
	
	private void update(long delta) {
		float time = (float) delta / 1000.0f;
		
		switch (state) {
		case ACCEL_TRANSLATION:
			PointF distance = ((AccelTranslation) translation).move(time);
			
			currentX += distance.x;
			currentY += distance.y;
			break;

		default:
			break;
		}
	}
	
	private void moveCanvas(float dx, float dy) {
		currentX += dx;
		currentY += dy;
	}
	
	private void scaleCanvas(float dScale, PointF focus) {
		scaleFactor *= dScale;
		
		// Ищем вектор, указывающий из фокуса в позицию канваса
		PointF vecToPos = new PointF(currentX - focus.x, currentY - focus.y);
		
		float vecMultiplyer = dScale - 1;
		float dx = vecToPos.x * vecMultiplyer;
		float dy = vecToPos.y * vecMultiplyer;
		
		moveCanvas(dx, dy);
	}
	
	@Override
	public void onSurfaceTouch(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		boolean activePointerIdChanged = activePointerChanged(event);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if(state != ReaderState.SCALING && state != ReaderState.ACCEL_TRANSLATION && !activePointerIdChanged) {
				state = ReaderState.TRANSLATION;

				float dx = x - prevX;
				float dy = y - prevY;
				
				moveCanvas(dx, dy);
			}

			break;
			
		case MotionEvent.ACTION_UP:
			if(state != ReaderState.ACCEL_TRANSLATION) {
				state = ReaderState.NOTHING;
			}
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
	
	@Override
	public void onSurfaceFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float velocityValue = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
		float normalVelocityX = velocityX / velocityValue;
		float normalVelocityY = velocityY / velocityValue;
		
		PointF v = new PointF(velocityX, velocityY);
		PointF a = new PointF(settings.stopAccel * normalVelocityX, settings.stopAccel * normalVelocityY);
		
		translation = new AccelTranslation(v, a);
		state = ReaderState.ACCEL_TRANSLATION;
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
