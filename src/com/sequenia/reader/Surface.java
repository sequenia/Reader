package com.sequenia.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
 * Класс предоставляет View, содержащий Canvas для рисования.
 * Рисование происходит в отдельном потоке, чтобы не грузить UI Thread.
 */
public class Surface extends SurfaceView implements SurfaceHolder.Callback {
	private DrawThread drawThread;

	private boolean created = false;

	/*
	 * Конструктор.
	 * В нем можно инициализировать только те объекты,
	 * которые не зависят от canvas,
	 * так как canvas еще не создан.
	 */
	public Surface(Context _context) {
		super(_context);

		getHolder().addCallback(this); // Данный метод возвращает canvas для рисования.
		// Спомощью addCalback мы синхронизируем его работу с текущим классом.
		
		drawThread = null;
	}

	// Вызывается при изменении области для рисования
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {}

	/*
	 * Вызывается при создании области для рисования.
	 * В момент вызова этого метода View и Canvas уже созданы.
	 * Здесь следует иницилизировать объекты для рисования.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		created = true;
		runDrawThread();
	}

	// Вызывается при уничтожении области рисования
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		stopDrawThread();
	}
	
	/*
	 * Здесь рисуем все, что хотим.
	 */
	public void draw(Canvas canvas, long delta) {
		canvas.drawColor(Color.rgb((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
	}
	
	/*
	 * Останавливает рисовку путем удаления потока рисования
	 * ВНИМАНИЕ! Для корректной работы его необходимо вызывать в onPause у Activity
	 */
	public void stopDrawThread() {
		if(drawThread != null) {
			boolean retry = true;
			// завершаем работу потока
			drawThread.setRunning(false);
			while (retry) {
				try {
					drawThread.join();
					retry = false;
				} catch (InterruptedException e) {
					// если не получилось, то будем пытаться еще и еще
				}
			}

			drawThread = null;
		}
	}
	
	/*
	 * Запуск поток рисования
	 * ВНИМАНИЕ - Для корректной работы этот метод следует вызывать в onResume у Activity
	 */
	public void runDrawThread() {
		if(drawThread == null && created == true) {
			drawThread = new DrawThread(getHolder());
	        drawThread.setRunning(true);
	        drawThread.start();
		}
	}

	/*
	 * Поток для рисования
	 */
	public class DrawThread extends Thread {
		private boolean runFlag = false;
		private SurfaceHolder surfaceHolder;
		private long prevTime;
		
		public DrawThread(SurfaceHolder _surfaceHolder) {
			this.surfaceHolder = _surfaceHolder;
			prevTime = System.currentTimeMillis();
		}
		
		public void setRunning(boolean _runFlag) {
			runFlag = _runFlag;
		}
		
		@Override
		public void run() {
			Canvas canvas;
			while (runFlag) {
				long now = System.currentTimeMillis();
				long delta = now - prevTime; // Время, прошедшее за предыдущий шаг отрисовки
				prevTime = now;
				canvas = null;
				try {
					// получаем объект Canvas и выполняем отрисовку
					canvas = surfaceHolder.lockCanvas(null);
					synchronized (surfaceHolder) {
						draw(canvas, delta);
					}
				} 
				finally {
					if (canvas != null) {
						// отрисовка выполнена. выводим результат на экран
						surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}
	}
}
