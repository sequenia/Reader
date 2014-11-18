package com.sequenia.reader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.Window;

public class ReaderSurface extends GestureSurface {
	public static enum ReaderState {
		NOTHING, TRANSLATION, SCALING, ACCEL_TRANSLATION, ACCEL_SCALING
	}
	Translation translation;

	private ReaderState state;
	private Reader reader;
	
	float currentX = 0.0f;
	float currentY = 0.0f;
	float prevX = 0.0f;
	float prevY = 0.0f;
	float scaleFactor = 1.0f;
	float scaleVelocity = 1.0f;
	private int mActivePointerId = 0;

	ReaderSettings settings;

	public ReaderSurface(Context _context) {
		super(_context);
		
		settings = new ReaderSettings();
		state = ReaderState.NOTHING;
		translation = null;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		PointF screenSize = getScreenSize(getSurfaceContext());
		settings.screenWidth = screenSize.x;
		settings.screenHeight = screenSize.y;
		
		reader = new Reader(settings);

		super.surfaceCreated(arg0);
	}
	
	@Override
	public void draw(Canvas canvas, long delta) {
		update(delta);
		
		canvas.translate(currentX, currentY);
		canvas.scale(scaleFactor, scaleFactor);

		reader.draw(canvas, delta, scaleFactor);
	}
	
	private void update(long delta) {
		settings.pageBorderPaint.setStrokeWidth(settings.pageBorderSize / scaleFactor);
		float time = (float) delta / 1000.0f;
		
		switch (state) {
		case ACCEL_TRANSLATION:
			AccelTranslation accelTranslation = (AccelTranslation) translation;
			PointF distance = accelTranslation.move(time);
			
			if(accelTranslation.directionChanged) {
				state = ReaderState.NOTHING;
				translation = null;
			} else {
				currentX += distance.x;
				currentY += distance.y;
			}
			break;

		case ACCEL_SCALING:
			AccelScaling accelScaling = (AccelScaling) translation;
			float scale = accelScaling.move(time);
			
			if(accelScaling.directionChanged) {
				state = ReaderState.NOTHING;
				translation = null;
			} else {
				float focusX = settings.screenWidth / 2.0f;
				float focusY = settings.screenHeight / 2.0f;
				scaleCanvas(scale, new PointF(focusX, focusY));
			}
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
			if((state == ReaderState.NOTHING || state == ReaderState.TRANSLATION) && !activePointerIdChanged) {
				state = ReaderState.TRANSLATION;

				float dx = x - prevX;
				float dy = y - prevY;
				
				moveCanvas(dx, dy);
			}

			break;
			
		case MotionEvent.ACTION_DOWN:
			state = ReaderState.NOTHING;
			break;

		case MotionEvent.ACTION_UP:
			if(state == ReaderState.TRANSLATION) {
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
	public void onSurfaceScaleBegin(ScaleGestureDetector detector) {
		state = ReaderState.SCALING;
	}
	
	@Override
	public void onSurfaceScale(ScaleGestureDetector detector) {
		float dScale = detector.getScaleFactor();
		PointF focus = new PointF(detector.getFocusX(), detector.getFocusY());
		scaleVelocity = (float) Math.pow(dScale, 1.0f / ((float)detector.getTimeDelta() / 1000.0f));
		
		scaleCanvas(dScale, focus);
	}
	
	@Override
	public void onSurfaceScaleEnd(ScaleGestureDetector detector) {
		if(scaleVelocity < settings.minScaleVelocity) {
			scaleVelocity = settings.minScaleVelocity;
		}
		
		if(scaleVelocity > settings.maxScaleVelocity) {
			scaleVelocity = settings.maxScaleVelocity;
		}
		
		if(scaleVelocity < 1.0 / settings.accelScaleSensivity || scaleVelocity > 1.0 * settings.accelScaleSensivity) {
			float accel = settings.scaleAccel;
			if(scaleVelocity < 1.0f) {
				accel = 1.0f / accel;
			}
			
			translation = new AccelScaling(scaleVelocity, accel);
			state = ReaderState.ACCEL_SCALING;
		} else {
			state = ReaderState.NOTHING;
		}
	}
	
	@Override
	public void onSurfaceFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(state != ReaderState.ACCEL_SCALING && state != ReaderState.SCALING) {
			float velocityValue = (float) Math.sqrt(velocityX * velocityX + velocityY * velocityY);
			float normalVelocityX = velocityX / velocityValue;
			float normalVelocityY = velocityY / velocityValue;
			
			PointF v = new PointF(velocityX, velocityY);
			PointF a = new PointF(settings.stopAccel * normalVelocityX, settings.stopAccel * normalVelocityY);
			
			translation = new AccelTranslation(v, a);
			state = ReaderState.ACCEL_TRANSLATION;
		}
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
	
	public PointF getScreenSize(Context context) {
		Activity activity = (Activity) context;
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		Rect rect = new Rect(); 
		Window win = activity.getWindow();
		win.getDecorView().getWindowVisibleDisplayFrame(rect); 
		int statusBarHeight = rect.top; 
		int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		int titleBarHeight = contentViewTop - statusBarHeight; 

		float screenHeight = ((float)dm.heightPixels - (titleBarHeight + statusBarHeight));
		float screenWidth = (float)dm.widthPixels;
		
		return new PointF(screenWidth, screenHeight);
	}
}