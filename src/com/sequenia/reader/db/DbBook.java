package com.sequenia.reader.db;

public class DbBook {
	public String name;
	public String parsedTextPath;
	
	public DbBook(String name, String parsedTextPath) {
		this.name = name;
		this.parsedTextPath = parsedTextPath;
	}
	
	public DbBook(long id, String name, String parsedTextPath) {
		this.name = name;
		this.parsedTextPath = parsedTextPath;
	}
	
	//Object will return in this format.
	public String toString() {
		return name + "/" + parsedTextPath;
	}
}
