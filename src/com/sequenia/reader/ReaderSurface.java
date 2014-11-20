package com.sequenia.reader;

import com.sequenia.reader.UniformMotion.UniformMotionResult;

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
		NOTHING, TRANSLATION, SCALING, ACCEL_TRANSLATION, ACCEL_SCALING, TO_READ_CORRECTION
	}
	public static enum ReaderMode {
		OVERVIEW, READING
	}
	Translation translation;
	ReaderMode mode;

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
		mode = ReaderMode.READING;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		PointF screenSize = getScreenSize(getSurfaceContext());
		settings.setScreenWidth(screenSize.x);
		settings.setScreenHeight(screenSize.y);
		
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
		float time = (float) delta / 1000.0f;

		updatePaints();
		
		switch (state) {
		case ACCEL_TRANSLATION:
			AccelTranslation accelTranslation = (AccelTranslation) translation;
			PointF distance = accelTranslation.move(time);
			
			if(accelTranslation.directionChanged || accelTranslation.stoped) {
				stopTranslation();
			} else {
				moveCanvas(distance.x, distance.y);
			}
			break;

		case ACCEL_SCALING:
			AccelScaling accelScaling = (AccelScaling) translation;
			float scale = accelScaling.move(time);
			
			if(accelScaling.directionChanged || accelScaling.stoped) {
				stopTranslation();
				if(scaleFactor >= settings.toReadModeSensivity) {
					moveToPage();
				}
			} else {
				if(scaleFactor >= settings.toReadModeSensivity &&
				  (scaleFactor < 1.0f && scale > 1.0f || scaleFactor >= 1.0f && scale <= 1.0f)) {
					moveToPage();
				} else {
					scaleCanvas(scale, new PointF(settings.halfScreenWidth, settings.halfScreenHeight));
				}
			}
			break;
			
		case TO_READ_CORRECTION:
			UniformMotion uniformMotion = (UniformMotion) translation;
			UniformMotionResult result = uniformMotion.move(time);
			
			if(uniformMotion.stoped) {
				currentX = uniformMotion.pointToMove.x;
				currentY = uniformMotion.pointToMove.y;
				scaleFactor = uniformMotion.pointToMove.s;
				stopTranslation();
			} else {
				moveCanvas(result.x, result.y);
				scaleCanvas(result.s, new PointF(settings.halfScreenWidth, settings.halfScreenHeight));
			}
			break;
			
		default:
			break;
		}
	}
	
	private void updatePaints() {
		settings.pageBorderPaint.setStrokeWidth(settings.pageBorderSize / scaleFactor);
		settings.currentPageBorderPaint.setStrokeWidth(settings.currentPageBorderSize / scaleFactor);
		settings.bookBorderPaint.setStrokeWidth(settings.bookBorderSize / scaleFactor);
		settings.bookPagesBorderPaint.setStrokeWidth(settings.bookPagesBorderSize / scaleFactor);
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
			if(state != ReaderState.TO_READ_CORRECTION) {
				state = ReaderState.NOTHING;
			}
			break;

		case MotionEvent.ACTION_UP:
			if(state == ReaderState.TRANSLATION) {
				if(scaleFactor >= settings.toReadModeSensivity) {
					moveToPage();
				} else {
					state = ReaderState.NOTHING;
				}
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
			if(scaleFactor >= settings.toReadModeSensivity) {
				moveToPage();
			} else {
				state = ReaderState.NOTHING;
			}
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
	
	private void moveToPage() {
		float pageX = 2.0f * settings.getScreenWidth();
		float pageY = 2.0f * settings.getScreenHeight();
		float pageWidth = settings.getScreenWidth();
		float pageHeight = settings.getScreenHeight();

		// Координаты канваса относительно экрана после движения
		float canvasNeededX = - pageX;
		float canvasNeededY = - pageY;
		float canvasNeededScale = 1.0f;
		
		// Центр страницы на канвасе
		float pageCenterX = pageX + pageWidth / 2.0f;
		float pageCenterY = pageY + pageHeight / 2.0f;
		
		// Центр страницы относительно экрана
		PointF sreenPageCenter = canvasToScreenCoord(new PointF(pageCenterX, pageCenterY), settings.getScreenWidth(), settings.getScreenHeight());
		
		// Расстояние, которое должен пройти канвас относительно экранаs
		float distanceX = settings.halfScreenWidth - sreenPageCenter.x;
		float distanceY = settings.halfScreenHeight - sreenPageCenter.y;
		float distanceScale = canvasNeededScale / scaleFactor;
		
		float xV = distanceX / settings.toReadTime;
		float yV = distanceY / settings.toReadTime;
		float scaleV = (float) Math.pow(distanceScale, 1.0f / settings.toReadTime);
		
		translation = new UniformMotion(xV, yV, scaleV);
		((UniformMotion) translation).needs = new UniformMotionResult(distanceX, distanceY, distanceScale);
		((UniformMotion) translation).pointToMove = new UniformMotionResult(canvasNeededX, canvasNeededY, canvasNeededScale);
		state = ReaderState.TO_READ_CORRECTION;
	}
	
	private PointF screenToCanvasCoord(PointF screenCoord, float screenWidth, float screenHeight) {
		float screenX = screenCoord.x;
		float screenY = screenCoord.y;
		
		float canvasX = (screenX - currentX) / scaleFactor;
		float canvasY = (screenY - currentY) / scaleFactor;
		
		return new PointF(canvasX, canvasY);
	}
	
	private PointF canvasToScreenCoord(PointF canvasCoord, float screenWidth, float screenHeight) {
		float canvasX = canvasCoord.x;
		float canvasY = canvasCoord.y;
		
		float screenX = canvasX * scaleFactor + currentX;
		float screenY = canvasY * scaleFactor + currentY;
		
		return new PointF(screenX, screenY);
	}
	
	private void stopTranslation() {
		state = ReaderState.NOTHING;
		translation = null;
	}
}