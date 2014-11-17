package com.sequenia.reader;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.ScaleGestureDetector;

public class GestureSurface extends Surface {
	private ScaleGestureDetector mScaleDetector;
	private GestureDetectorCompat mDetector; 
	
	public GestureSurface(Context _context) {
		super(_context);
	}

}
