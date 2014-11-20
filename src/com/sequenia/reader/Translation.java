package com.sequenia.reader;

import android.graphics.PointF;

public class Translation {
	public enum TranslationType {
		ACCEL_TRANSLATION, ACCEL_SCALING, UNIFORM_TRANSLATION, UNIFORM_SCALING, UNIFORM_MOTION
	}
	
	TranslationType type;
	boolean stoped = false;
	
	public Translation() {
		
	}
	
	public Translation(TranslationType _type) {
		type = _type;
	}
}

class UniformTranslation extends Translation {
	public PointF v;
	public PointF d;
	
	public UniformTranslation() {
		super(TranslationType.UNIFORM_TRANSLATION);
		v = new PointF(0.0f, 0.0f);
		d = new PointF(0.0f, 0.0f);
	}
	
	public UniformTranslation(PointF _v) {
		super(TranslationType.UNIFORM_TRANSLATION);
		v = new PointF(_v.x, _v.y);
		d = new PointF(0.0f, 0.0f);
	}
	
	public PointF move(float t) {
		float dx = v.x * t;
		float dy = v.y * t;
		
		d.x += dx;
		d.y += dy;
		
		return new PointF(dx, dy);
	}
}

class UniformScaling extends Translation {
	public float v;
	public float s;
	
	public UniformScaling() {
		super(TranslationType.UNIFORM_SCALING);
		v = 0.0f;
		s = 0.0f;
	}
	
	public UniformScaling(float _v) {
		super(TranslationType.UNIFORM_SCALING);
		v = _v;
		s = 1.0f;
	}
	
	public float move(float t) {
		float ds = (float) Math.pow(v, t);
		
		s *= ds;
		
		return ds;
	}
}

class UniformMotion extends Translation {
	public float vx;
	public float vy;
	public float vs;
	
	public float sx;
	public float sy;
	public float ss;
	
	UniformMotionResult needs;
	UniformMotionResult pointToMove;
	
	public UniformMotion() {
		super(TranslationType.UNIFORM_MOTION);

		vx = 0.0f;
		vy = 0.0f;
		vs = 0.0f;
		
		sx = 0.0f;
		sy = 0.0f;
		ss = 1.0f;

		needs = null;
		pointToMove = null;
	}
	
	public UniformMotion(float _vx, float _vy, float _vs) {
		super(TranslationType.UNIFORM_MOTION);

		vx = _vx;
		vy = _vy;
		vs = _vs;
		
		sx = 0.0f;
		sy = 0.0f;
		ss = 1.0f;

		needs = null;
		pointToMove = null;
	}
	
	public UniformMotionResult move(float t) {
		float ds = (float) Math.pow(vs, t);
		ss *= ds;
		
		float dx = vx * t;
		float dy = vy * t;
		
		sx += dx;
		sy += dy;
		
		if(needs != null) {
			boolean translationEnded = false;
			boolean scalingEnded = false;
			
			if(needs.x < 0.0f && sx < needs.x) {
				translationEnded = true;
			}
			
			if(needs.x >= 0.0f && sx >= needs.x) {
				translationEnded = true;
			}
			
			if(needs.y < 0.0f && sy < needs.y) {
				translationEnded = true;
			}
			
			if(needs.y >= 0.0f && sy >= needs.y) {
				translationEnded = true;
			}
			
			if(needs.s < 1.0f && ss < needs.s) {
				scalingEnded = true;
			}
			
			if(needs.s >= 1.0f && ss >= needs.s) {
				scalingEnded = true;
			}
			
			if(translationEnded && scalingEnded) {
				stoped = true;
			}
		}
		
		return new UniformMotionResult(dx, dy, ds);
	}
	
	public static class UniformMotionResult {
		public float x;
		public float y;
		public float s;

		public UniformMotionResult() {
			x = 0.0f;
			y = 0.0f;
			s = 1.0f;
		}
		
		public UniformMotionResult(float _x, float _y, float _s) {
			x = _x;
			y = _y;
			s = _s;
		}
	}
}

class AccelTranslation extends Translation {
	public PointF v;
	public PointF a;
	public PointF d;
	
	public boolean directionChanged = false;
	
	public AccelTranslation() {
		super(TranslationType.ACCEL_TRANSLATION);
		v = new PointF(0.0f, 0.0f);
		a = new PointF(0.0f, 0.0f);
		d = new PointF(0.0f, 0.0f);
	}
	
	public AccelTranslation(PointF _v, PointF _a) {
		super(TranslationType.ACCEL_TRANSLATION);
		v = new PointF(_v.x, _v.y);
		a = new PointF(_a.x, _a.y);
		d = new PointF(0.0f, 0.0f);
	}
	
	public PointF move(float t) {
		float oldVx = v.x;
		float oldVy = v.y;
		
		float t_x_t = t * t;
		float t_x_t_div_2 = t_x_t / 2.0f;

		float dx = v.x * t + a.x * t_x_t_div_2;
		float dy = v.y * t + a.y * t_x_t_div_2;
		
		float dvx = a.x * t;
		float dvy = a.y * t;
		
		d.x += dx;
		d.y += dy;
		
		v.x += dvx;
		v.y += dvy;
		
		directionChanged = oldVx > 0 && v.x <=0 || oldVx < 0 && v.x >=0 || oldVy > 0 && v.y <=0 || oldVy < 0 && v.y >=0;
		
		return (new PointF(dx, dy));
	}
}

class AccelScaling extends Translation {
	public float v;
	public float a;
	public float s;
	
	public boolean directionChanged = false;
	
	public AccelScaling() {
		super(TranslationType.ACCEL_SCALING);
		v = 0.0f;
		a = 0.0f;
		s = 1.0f;
	}
	
	public AccelScaling(float _v, float _a) {
		super(TranslationType.ACCEL_SCALING);
		v = _v;
		a = _a;
		s = 1.0f;
	}
	
	public float move(float t) {
		float oldV = v;
		
		float ds = (float) (Math.pow(v, t) * Math.pow(a, (t * t) / 2.0f));
		float dvs = (float) Math.pow(a, t);
		
		s *= ds;
		v *= dvs;
		
		directionChanged = oldV < 1.0f && v >= 1.0f || oldV > 1.0f && v <= 1.0f;
		
		return ds;
	}
}