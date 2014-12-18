package com.sequenia.reader.surface;

import com.sequenia.reader.Reader;
import com.sequenia.reader.ReaderSettings;
import com.sequenia.reader.reader_objects.ReaderBook;
import com.sequenia.reader.reader_objects.ReaderPage;
import com.sequenia.reader.translations.AccelScaling;
import com.sequenia.reader.translations.AccelTranslation;
import com.sequenia.reader.translations.Translation;
import com.sequenia.reader.translations.UniformMotion;
import com.sequenia.reader.translations.UniformTranslation;
import com.sequenia.reader.translations.UniformMotion.UniformMotionResult;

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

/*
 * Холст читалки.
 * Рисует книги читалки.
 * Выполняет определенные действия в ответ на жесты пользователя:
 * - Увеличивает и уменьшает масштаб
 * - Перелистывает страницы,
 * - и т.д.
 */
public class ReaderSurface extends GestureSurface {
	/*
	 * Возможные состояния читалки.
	 * В зависимости от текущего состояния при обновлении производятся различные действия, например:
	 * MOVING_TO_PAGE - сейчас камера двигается к странице. Нужно продвинуть камеру на некоторое расстояние
	 * ACCEL_SCALING - сейчас происходит масштабирование с ускорением. Нужно поменять масштаб
	 */
	public static enum ReaderState {
		NOTHING, TRANSLATION, SCALING, ACCEL_TRANSLATION, ACCEL_SCALING,
		MOVING_TO_PAGE, WAITING_FOR_TO_READ_CORRECTION
	}
	
	// Возможные режимы читалки.
	public static enum ReaderMode {
		OVERVIEW, READING
	}

	Translation translation;   // Занимается расчетом пройденного расстояния и масштаба при перемещении
	ReaderMode mode;           // Текущий режим

	private ReaderState state; // Текущее состояние
	private Reader reader;     // Читалка
	
	float currentX = 0.0f;
	float currentY = 0.0f;
	float prevX = 0.0f;
	float prevY = 0.0f;
	float scaleFactor = 1.0f;
	float scaleVelocity = 1.0f;
	private int mActivePointerId = 0;
	
	private ReaderSettings settings;

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
		
		if(!isCreated()) {
			reader = new Reader(settings);
		}

		super.surfaceCreated(arg0);
	}
	
	/*
	 * Рисование подразделяется на несколько этапов:
	 *   Первый проход обновления
	 *   Задание холсту текущих настроек
	 *   Обновление читалки
	 *   Второй проход обновления
	 *   Рисование
	 *   
	 *   Первый проход обновления необходим для рассчета текущих показателей холста: позиция и масштаб.
	 *   
	 *   Обновление читалки происходит после задания холсту текущих показателей,
	 *   так как его главной задачей является поиск книг и страниц, попадающих в область экрана.
	 *   
	 *   Во втором проходе выполняются действия, для которых необходимо знать,
	 *   какие книги и страницы находятся в области экрана.
	 */
	@Override
	public void draw(Canvas canvas, long delta) {
		float time = (float) delta / 1000.0f;

		update(time);
		updateCanvasMatrix(canvas);
		reader.update(canvas, mode, scaleFactor);
		postUpdate(time);

		reader.draw(canvas, delta, scaleFactor);
	}
	
	// Обновляет матрицу холста (Перемещение и масштабирование холста в зависимости от текущих настроек)
	private void updateCanvasMatrix(Canvas canvas) {
		canvas.translate(currentX, currentY);
		canvas.scale(scaleFactor, scaleFactor);
	}
	
	/*
	 * Обновление текущих показателей в зависимости от состояния и режима.
	 * Здесь приемущественно обновляются положение канваса и его масштаб.
	 */
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
			
		case MOVING_TO_PAGE:
			movingToPage(time);
			break;
			
		default:
			break;
		}
	}
	
	private void readingUpdate(float time) {
		switch (state) {
		case MOVING_TO_PAGE:
			movingToPage(time);
			break;
			
		default:
			break;
		}
	}
	
	private void movingToPage(float time) {
		UniformMotion uniformMotion = (UniformMotion) translation;
		UniformMotionResult result = uniformMotion.move(time);
		
		ReaderPage page = (ReaderPage)uniformMotion.pointToMove;
		
		if(uniformMotion.stoped) {
			currentX = - page.getAbsoluteX();
			currentY = - page.getAbsoluteY();
			scaleFactor = 1.0f;
			
			ReaderBook book = (ReaderBook) page.getParent();
			reader.setCurrentBook(book);
			book.setCurrentPage(page);
			
			stopTranslation();
			translation = new UniformTranslation();
			mode = ReaderMode.READING;
		} else {
			moveCanvas(result.x, result.y);
			scaleCanvas(result.s, new PointF(settings.halfScreenWidth, settings.halfScreenHeight));
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
				if(translation != null) {
					((UniformTranslation) translation).d.x += dx;
				}
				break;
				
			default:
				break;
			}
			
			moveCanvas(dx, dy);
		}
	}
	
	private void onActionDown(float x, float y, MotionEvent event) {
		if(state != ReaderState.MOVING_TO_PAGE) {
			if(mode == ReaderMode.READING) {
				translation = new UniformTranslation();
			}
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
				if(translation != null) {
					UniformTranslation uniformTranslation = (UniformTranslation) translation;
					float distance = uniformTranslation.d.x;
					float slideDistance = settings.pageSlideSensivity * settings.getScreenWidth();
					
					ReaderBook currentBook = reader.getCurrentBook();
					ReaderPage currentPage = currentBook.getCurrentPage();
					ReaderPage pageToMove = null;
					
					if(distance < - slideDistance || distance > slideDistance) {
						if(distance > 0) {
							pageToMove = currentPage.getPrevious();
						} else {
							pageToMove = currentPage.getNext();
						}
					}
					
					if(pageToMove == null) {
						pageToMove = currentPage;
					}
					
					slideToPage(currentPage, pageToMove);
				} else {
					state = ReaderState.NOTHING;
				}
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
	
	private void slideToPage(ReaderPage from, ReaderPage to) {
		if(from == to) {
			moveToPage(to, settings.pageSlideTime, false);
		} else {
			ReaderPage fromFake = from.getFake();
			ReaderPage toFake = to.getFake();
			
			if(fromFake != null && toFake != null) {
				moveToPage(to, settings.pageSlideTime, true);
			} else {
				moveToPage(to, settings.pageSlideTime, false);
			}
		}
	}
	
	private void moveToNearestPage() {
		PointF screenCenter = new PointF(settings.halfScreenWidth, settings.halfScreenHeight);
		PointF screenCenterOnCanvas = screenToCanvasCoord(screenCenter, settings.getScreenWidth(), settings.getScreenHeight());
		ReaderPage nearestPage = reader.getNearestPage(screenCenterOnCanvas.x, screenCenterOnCanvas.y);
		moveToPage(nearestPage, settings.toReadTime, false);
	}
	
	public void moveToCurrentPage() {
		state = ReaderState.NOTHING;
		mode = ReaderMode.READING;
		translation = new UniformTranslation();
		ReaderPage page = reader.getCurrentBook().getCurrentPage();
		currentX = - page.getAbsoluteX();
		currentY = - page.getAbsoluteY();
		scaleFactor = 1.0f;
	}
	
	private void moveToPage(ReaderPage page, float time, boolean toFake) {
		if(page == null) {
			state = ReaderState.NOTHING;
			return; 
		}

		float pageX;
		float pageY;
		float pageWidth = page.getWidth();
		float pageHeight = page.getHeight();
		
		ReaderPage fake = page.getFake();
		if(toFake && fake != null) {
			pageX = fake.getAbsoluteX();
			pageY = fake.getAbsoluteY();
		} else {
			pageX = page.getAbsoluteX();
			pageY = page.getAbsoluteY();
		}
		
		// Центр страницы на канвасе
		float pageCenterX = pageX + pageWidth / 2.0f;
		float pageCenterY = pageY + pageHeight / 2.0f;
		
		// Центр страницы относительно экрана
		PointF sreenPageCenter = canvasToScreenCoord(new PointF(pageCenterX, pageCenterY), settings.getScreenWidth(), settings.getScreenHeight());
		
		// Расстояние, которое должен пройти канвас относительно экрана
		float distanceX = settings.halfScreenWidth - sreenPageCenter.x;
		float distanceY = settings.halfScreenHeight - sreenPageCenter.y;
		float distanceScale = 1.0f / scaleFactor;
		
		float xV = distanceX / time;
		float yV = distanceY / time;
		float scaleV = (float) Math.pow(distanceScale, 1.0f / time);
		
		translation = new UniformMotion(xV, yV, scaleV);
		((UniformMotion) translation).needs = new UniformMotionResult(distanceX, distanceY, distanceScale);
		((UniformMotion) translation).pointToMove = page;
		state = ReaderState.MOVING_TO_PAGE;
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
	
	public Reader getReader() {
		return reader;
	}
	
	public ReaderSettings getSettings() {
		return settings;
	}
}