package com.sequenia.reader.translations;

// Объединяет в себе равномерное движение и равномерное масштабирование
public class UniformMotion extends Translation {
	public float vx;
	public float vy;
	public float vs;
	
	public float sx;
	public float sy;
	public float ss;
	
	boolean translationEndedX = false;
	boolean translationEndedY = false;
	boolean scalingEnded = false;
	
	public UniformMotionResult needs;          // Путь, который нужно пройти в результате движения
	public Object pointToMove;                 // Точка, в которой нужно оказаться в рузельтате движения
	
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
		
		// Приращение домножается на пройденный масштаб.
		// Это необходимо для корректного перемещения холста во время масштабирования,
		// так как он меняет свою позицию относительно экрана при масштабировании
		float dxRes = dx * ss;
		float dyRes = dy * ss;
		
		sx += dx;
		sy += dy;
		
		if(needs != null) {
			if(needs.x < 0.0f && sx < needs.x) {
				translationEndedX = true;
				dxRes = 0.0f;
			}
			
			if(needs.x >= 0.0f && sx >= needs.x) {
				translationEndedX = true;
				dxRes = 0.0f;
			}
			
			if(needs.y < 0.0f && sy < needs.y) {
				translationEndedY = true;
				dyRes = 0.0f;
			}
			
			if(needs.y >= 0.0f && sy >= needs.y) {
				translationEndedY = true;
				dyRes = 0.0f;
			}
			
			if(needs.s < 1.0f && ss < needs.s) {
				scalingEnded = true;
				ds = 1.0f;
			}
			
			if(needs.s >= 1.0f && ss >= needs.s) {
				scalingEnded = true;
				ds = 1.0f;
			}
			
			if(translationEndedX && translationEndedY && scalingEnded) {
				stoped = true;
			}
		}
		
		return new UniformMotionResult(dxRes, dyRes, ds);
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