package com.sequenia.reader.translations;

import android.graphics.PointF;

//Используется для расчета равномерного движения
public class UniformTranslation extends Translation {
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
