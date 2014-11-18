package com.sequenia.reader;

import android.graphics.PointF;

public class Translation {
	public enum TranslationType {
		ACCEL_TRANSLATION, ACCEL_SCALING
	}
	
	TranslationType type;
	
	public Translation() {
		
	}
	
	public Translation(TranslationType _type) {
		type = _type;
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