package com.sequenia.reader;

import android.content.Context;
import android.graphics.Canvas;
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

	public ReaderSurface(Context _context) {
		super(_context);
		
		state = ReaderState.NOTHING;
		reader = new Reader();
	}
	
	@Override
	public void draw(Canvas canvas, long delta) {
		update(delta);
		
		canvas.translate(currentX, currentY);

		reader.draw(canvas, delta);
	}
	
	private void update(long delta) {
		
	}
	
	@Override
	public void onSurfaceTouch(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if(state != ReaderState.SCALING) {
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
	}
	
	@Override
	public void onSurfaceScaleBegin(ScaleGestureDetector detector) {
		state = ReaderState.SCALING;
	}
	
	@Override
	public void onSurfaceScaleEnd(ScaleGestureDetector detector) {
		state = ReaderState.NOTHING;
	}
}
