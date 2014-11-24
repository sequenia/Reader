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
		NOTHING, TRANSLATION, SCALING, ACCEL_TRANSLATION, ACCEL_SCALING, TO_READ_CORRECTION, WAITING_FOR_TO_READ_CORRECTION
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
		PointF screenSize = getScreenSize(getContext());
		settings.setScreenWidth(screenSize.x);
		settings.setScreenHeight(screenSize.y);
		
		reader = new Reader(settings);

		super.surfaceCreated(arg0);
	}
	
	@Override
	public void draw(Canvas canvas, long delta) {
		float time = (float) delta / 1000.0f;

		update(time);
		updateCanvasMatrix(canvas);
		reader.update(canvas);
		postUpdate(time);

		reader.draw(canvas, delta, scaleFactor);
	}
	
	private void updateCanvasMatrix(Canvas canvas) {
		canvas.translate(currentX, currentY);
		canvas.scale(scaleFactor, scaleFactor);
	}
	
	private void update(float time) {
		updatePaints();
		
		switch(mode) {
		case OVERVIEW:
			overviewUpdate(time);
			break;

		case READING:
			readingUpdate(time);
			break;

		default:
			break;
		}
	}
	
	private void postUpdate(float time) {
		switch(state) {
		case WAITING_FOR_TO_READ_CORRECTION:
			moveToNearestPage();
			break;

		default:
			break;
		}
	}
	
	private void overviewUpdate(float time) {
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
					state = ReaderState.WAITING_FOR_TO_READ_CORRECTION;
				}
			} else {
				if(scaleFactor >= settings.toReadModeSensivity &&
				  (scaleFactor < 1.0f && scale > 1.0f || scaleFactor >= 1.0f && scale <= 1.0f)) {
					state = ReaderState.WAITING_FOR_TO_READ_CORRECTION;
				} else {
					scaleCanvas(scale, new PointF(settings.halfScreenWidth, settings.halfScreenHeight));
				}
			}
			break;
			
		case TO_READ_CORRECTION:
			UniformMotion uniformMotion = (UniformMotion) translation;
			UniformMotionResult result = uniformMotion.move(time);
			
			ReaderPage page = (ReaderPage)uniformMotion.pointToMove;
			
			if(uniformMotion.stoped) {
				currentX = - page.getAbsoluteX();
				currentY = - page.getAbsoluteY();
				scaleFactor = 1.0f;
				
				ReaderBook book = (ReaderBook) page.getParent();
				book.setCurrentPage(page);
				
				stopTranslation();
				mode = ReaderMode.READING;
			} else {
				moveCanvas(result.x, result.y);
				scaleCanvas(result.s, new PointF(settings.halfScreenWidth, settings.halfScreenHeight));
			}
			break;
			
		default:
			break;
		}
	}
	
	private void readingUpdate(float time) {
		
	}
	
	private void updatePaints() {
		settings.pageBorderPaint.setStrokeWidth(settings.pageBorderSize / scaleFactor);
		settings.currentPageBorderPaint.setStrokeWidth(settings.currentPageBorderSize / scaleFactor);
		settings.bookBorderPaint.setStrokeWidth(settings.bookBorderSize / scaleFactor);
		settings.bookPagesBorderPaint.setStrokeWidth(settings.bookPagesBorderSize / scaleFactor);
	}
	
	@Override
	public void onSurfaceTouch(MotionEvent event) {
		float x = event.getX();
		float y = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			onActionMove(x, y, event);
			break;
			
		case MotionEvent.ACTION_DOWN:
			onActionDown(x, y, event);
			break;

		case MotionEvent.ACTION_UP:
			onActionUp(x, y, event);
			break;

		default:
			break;
		}
		
		prevX = x;
		prevY = y;
	}
	
	private void onActionMove(float x, float y, MotionEvent event) {
		boolean activePointerIdChanged = activePointerChanged(event);

		if((state == ReaderState.NOTHING || state == ReaderState.TRANSLATION) && !activePointerIdChanged) {
			state = ReaderState.TRANSLATION;

			float dx = 0.0f;
			float dy = 0.0f;
			
			switch (mode) {
			case OVERVIEW:
				dx = x - prevX;
				dy = y - prevY;
				break;

			case READING:
				dx = x - prevX;
				break;
				
			default:
				break;
			}
			
			moveCanvas(dx, dy);
		}
	}
	
	private void onActionDown(float x, float y, MotionEvent event) {
		if(state != ReaderState.TO_READ_CORRECTION) {
			state = ReaderState.NOTHING;
		}
	}
	
	private void onActionUp(float x, float y, MotionEvent event) {
		if(state == ReaderState.TRANSLATION) {
			switch (mode) {
			case OVERVIEW:
				if(scaleFactor >= settings.toReadModeSensivity) {
					state = ReaderState.WAITING_FOR_TO_READ_CORRECTION;
				} else {
					state = ReaderState.NOTHING;
				}
				break;

			case READING:
				state = ReaderState.NOTHING;
				break;
				
			default:
				break;
			}
		}
	}
	
	@Override
	public void onSurfaceScaleBegin(ScaleGestureDetector detector) {
		state = ReaderState.SCALING;
		mode = ReaderMode.OVERVIEW;
	}
	
	@Override
	public void onSurfaceScale(ScaleGestureDetector detector) {
		mode = ReaderMode.OVERVIEW;
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
				state = ReaderState.WAITING_FOR_TO_READ_CORRECTION;
			} else {
				state = ReaderState.NOTHING;
			}
		}
	}
	
	@Override
	public void onSurfaceFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		switch (mode) {
		case OVERVIEW:
			overviewFling(e1, e2, velocityX, velocityY);
			break;

		case READING:
			readingFling(e1, e2, velocityX, velocityY);
			break;
			
		default:
			break;
		}
	}
	
	private void overviewFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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
	
	private void readingFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		
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
	
	private void moveToNearestPage() {
		PointF screenCenter = new PointF(settings.halfScreenWidth, settings.halfScreenHeight);
		PointF screenCenterOnCanvas = screenToCanvasCoord(screenCenter, settings.getScreenWidth(), settings.getScreenHeight());
		ReaderPage nearestPage = reader.getNearestPage(screenCenterOnCanvas.x, screenCenterOnCanvas.y);
		moveToPage(nearestPage);
	}
	
	private void moveToPage(ReaderPage page) {
		if(page == null) {
			state = ReaderState.NOTHING;
			return; 
		}

		float pageX = page.getAbsoluteX();
		float pageY = page.getAbsoluteY();
		float pageWidth = page.getWidth();
		float pageHeight = page.getHeight();
		
		// Центр страницы на канвасе
		float pageCenterX = pageX + pageWidth / 2.0f;
		float pageCenterY = pageY + pageHeight / 2.0f;
		
		// Центр страницы относительно экрана
		PointF sreenPageCenter = canvasToScreenCoord(new PointF(pageCenterX, pageCenterY), settings.getScreenWidth(), settings.getScreenHeight());
		
		// Расстояние, которое должен пройти канвас относительно экрана
		float distanceX = settings.halfScreenWidth - sreenPageCenter.x;
		float distanceY = settings.halfScreenHeight - sreenPageCenter.y;
		float distanceScale = 1.0f / scaleFactor;
		
		float xV = distanceX / settings.toReadTime;
		float yV = distanceY / settings.toReadTime;
		float scaleV = (float) Math.pow(distanceScale, 1.0f / settings.toReadTime);
		
		translation = new UniformMotion(xV, yV, scaleV);
		((UniformMotion) translation).needs = new UniformMotionResult(distanceX, distanceY, distanceScale);
		((UniformMotion) translation).pointToMove = page;
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