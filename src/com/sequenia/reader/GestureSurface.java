package com.sequenia.reader;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/*
 * Класс холста с реализованными методами детекторов жестов:
 *   Детектор масштабирования ScaleGestureDetector
 *   Детектор жестов          GestureDetectorCompat
 *   
 * Данный класс необходимо использовать тогда,
 * когда вы хотите обработать взаимодействие пользователя с системой
 */
public class GestureSurface extends Surface {
	private ScaleGestureDetector mScaleDetector;
	private GestureDetectorCompat mDetector;
	
	public GestureSurface(Context context) {
		super(context);
		
		mDetector = new GestureDetectorCompat(context, new GestureListener());
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
	}
	
	public void onSurfaceFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		System.out.println("Fling");
	}
	
	public void onSurfaceScale(ScaleGestureDetector detector) {
		System.out.println("Scale");
	}
	
	public void onSurfaceScaleBegin(ScaleGestureDetector detector) {
		System.out.println("Scale Begin");
	}
	
	public void onSurfaceScaleEnd(ScaleGestureDetector detector) {
		System.out.println("Scale End");
	}
	
	public void onSurfaceTouch(MotionEvent event) {
		System.out.println("Touch");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);
		mDetector.onTouchEvent(event);
		
		onSurfaceTouch(event);
		
		return true;
	}

	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			onSurfaceFling(e1, e2, velocityX, velocityY);
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	}
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			onSurfaceScale(detector);
			return true; //super.onScale(detector);
		}
		
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			onSurfaceScaleBegin(detector);
			return super.onScaleBegin(detector);
		}
		
		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {
			onSurfaceScaleEnd(detector);
			super.onScaleEnd(detector);
		}
	}
	
	public GestureDetectorCompat getGestureDetector() {
		return mDetector;
	}

	public ScaleGestureDetector getScaleDetector() {
		return mScaleDetector;
	}
}
