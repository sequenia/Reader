package com.sequenia.reader.translations;


/*
 * Класс используется для расчета перемещений разных типов.
 */
public class Translation {
	public enum TranslationType {
		ACCEL_TRANSLATION, ACCEL_SCALING, UNIFORM_TRANSLATION, UNIFORM_SCALING, UNIFORM_MOTION
	}
	
	TranslationType type;
	public boolean stoped = false; // Закончилось ли перемещение
	
	public Translation() {
		
	}
	
	public Translation(TranslationType _type) {
		type = _type;
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