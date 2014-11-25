package com.sequenia.reader;

import android.graphics.PointF;

/*
 * Класс используется для расчета перемещений разных типов.
 */
public class Translation {
	public enum TranslationType {
		ACCEL_TRANSLATION, ACCEL_SCALING, UNIFORM_TRANSLATION, UNIFORM_SCALING, UNIFORM_MOTION
	}
	
	TranslationType type;
	boolean stoped = false; // Закончилось ли перемещение
	
	public Translation() {
		
	}
	
	public Translation(TranslationType _type) {
		type = _type;
	}
}

// Используется для расчета равномерного движения
class UniformTranslation extends Translation {
	public PointF v; // Скорость
	public PointF d; // Пройденный путь
	
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

// Используется для расчета равномерного масштабирования
class UniformScaling extends Translation {
	public float v; // Скорость масштабирования
	public float s; // Общее масштабирование
	
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
	
	/*
	 *  Здесь пройденный "путь" (Масштаб) умножается на приращение.
	 *  Приращение не прибавляется, как в случае равномерного движения,
	 *  так как скорость масштабирования показывает, ВО сколько раз увеличился масштаб
	 *  за единицу времени, а не НА сколько.
	 *  
	 *  В связи с этим изменяется и формула расчета приращения.
	 *  Умножение скорости на время заменяется на возведение в степень.
	 *  
	 *  То есть любая формула будет иметь вид, аналогичный прямолинейному движению,
	 *  только сложение заменено на умножение, а умножение на возведение в степень.
	 */ 
	public float move(float t) {
		float ds = (float) Math.pow(v, t);
		
		s *= ds;
		
		return ds;
	}
}

// Объединяет в себе равномерное движение и равномерное масштабирование
class UniformMotion extends Translation {
	public float vx;
	public float vy;
	public float vs;
	
	public float sx;
	public float sy;
	public float ss;
	
	boolean translationEndedX = false;
	boolean translationEndedY = false;
	boolean scalingEnded = false;
	
	UniformMotionResult needs;          // Путь, который нужно пройти в результате движения
	Object pointToMove;                 // Точка, в которой нужно оказаться в рузельтате движения
	
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