package com.sequenia.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Surface extends SurfaceView implements SurfaceHolder.Callback {
	private Context context;
	private DrawThread drawThread;

	private boolean created = false;

	public Surface(Context _context) {
		super(_context);

		getHolder().addCallback(this); // Получаем объект SurfaceHolder, который предоставляет canvas для отрисовки
		
		context = _context;
		drawThread = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		created = true;
		runDrawThread();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		stopDrawThread();
	}
	
	public void draw(Canvas canvas, long delta) {
		canvas.drawColor(Color.rgb((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255)));
	}
	
	// Вызвать в onPause у Activity
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
	
	// Вызвать в onResume у Activity
	public void runDrawThread() {
		if(drawThread == null && created == true) {
			drawThread = new DrawThread(getHolder());
	        drawThread.setRunning(true);
	        drawThread.start();
		}
	}

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
	            long delta = now - prevTime;
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
