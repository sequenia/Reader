package com.sequenia.reader;

/*
 * Используется для задания интервала.
 * Умеет определять, находится ли точка в заданном интервале.
 */
public class Interval {
	public float left = 0.0f;
	public float right = 0.0f;
	public boolean includeLeft = false;
	public boolean includeRight = false;
	
	public Interval(float _left, float _right) {
		left = _left;
		right = _right;
	}
	
	public Interval(float _left, float _right, boolean _includeLeft, boolean _includeRight) {
		left = _left;
		right = _right;
		includeLeft = _includeLeft;
		includeRight = _includeRight;
	}
	
	public boolean isIn(float number) {
		return (includeLeft  && number >= left  || !includeLeft  && number > left &&
				includeRight && number <= right || !includeRight && number < right);
	}
}
