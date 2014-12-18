package com.sequenia.parsers;

import java.util.ArrayList;

/*
 * Класс представляет общий формат электронной книги.
 * К данному формату приводится книга любого типа для дальнейшей работы с ней.
 */
public class Book {
	// Типы элементов, которые могут присутствовать на странице книги
	public static enum ElemType {
		Text, Image
	}
	
	// Некоторые форматы (например, epub) могут включать в себя несколько заголовков, писателей и т.д,
	// поэтому для их хранения используются списки.
	public ArrayList<String> titles;
	public ArrayList<String> dates;
	public ArrayList<String> creators;
	public ArrayList<String> contributors;
	public ArrayList<String> publishers;
	public ArrayList<String> descriptions;
	
	// Здесь хранится текст по страницам в том порядке, в котором он содержится в электронной книге.
	// Далее текст будет дробиться на более мелкие страницы, которые помещаются на экране устройства.
	public ArrayList<BookPage> pages;
	
	public Book() {
		titles = new ArrayList<String>();
		contributors = new ArrayList<String>();
		dates = new ArrayList<String>();
		creators = new ArrayList<String>();
		publishers = new ArrayList<String>();
		descriptions = new ArrayList<String>();
		pages = new ArrayList<BookPage>();
	}
	
	public static class BookPage {
		public ArrayList<PageElem> elements;
		
		public BookPage() {
			elements = new ArrayList<PageElem>();
		}
	}
	
	public static class PageText extends PageElem {
		public String text;
		
		public PageText() {
			type = ElemType.Text;
		}
		
		public PageText(String _text) {
			type = ElemType.Text;
			text = _text;
		}
	}
	
	public static class PageElem {
		public ElemType type;
		
		public PageElem() {
			
		}
	}
}
