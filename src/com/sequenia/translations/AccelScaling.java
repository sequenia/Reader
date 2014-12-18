package com.sequenia.translations;

public class AccelScaling extends Translation {
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