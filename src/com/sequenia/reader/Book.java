package com.sequenia.reader;

import java.util.ArrayList;

/*
 * Класс используется для передачи данных в конструктор книжки.
 * К этому виду приводится книжка любого формата перед конечным парсингом.
 * 
 * Некоторые форматы (например, epub) могут включать в себя несколько заголовков, писателей и т.д,
 * поэтому для их хранения используются списки.
 */
public class Book {
	public enum ElemType {
		Text, Image
	}
	
	public ArrayList<String> titles;
	public ArrayList<String> dates;
	public ArrayList<String> creators;
	public ArrayList<String> contributors;
	public ArrayList<String> publishers;
	public ArrayList<String> descriptions;
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
	
	public static class TextElem extends PageElem {
		String text;
		
		public TextElem() {
			type = ElemType.Text;
		}
		
		public TextElem(String _text) {
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
