package com.sequenia.reader.parsers;


/*
 * Парсер электронных книг.
 * Используется для приведения электронной книги к общему формату, описанному в классе Book.
 */
public class BookParser {
	private String filename;
	
	public static BookParser construct(String _filename) {
		String extension = _filename.substring(_filename.lastIndexOf("."));
		BookParser parser;
		
		while(true) {
			if(extension.equals(".epub")) {
				parser = new EpubParser(_filename);
				break;
			}
			
			parser = new BookParser(_filename);
			break;
		}

		return parser;
	}
	
	// Должен возвращать книгу в формате класса Book
	public Book parse() {
		return null;
	}
	
	public BookParser(String _filename) {
		filename = _filename;
	}
	
	public void setFilename(String _filename) {
		filename = _filename;
	}
	
	public String getFilename() {
		return filename;
	}
}