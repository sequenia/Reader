package com.sequenia.reader.db;

import java.util.ArrayList;

public class DbBook {
	public String name;
	public String parsedTextPath;
	public ArrayList<String> titles;
	public ArrayList<String> dates;
	public ArrayList<String> creators;
	public ArrayList<String> contributors;
	public ArrayList<String> publishers;
	public ArrayList<String> descriptions;
	
	public DbBook() {
		name = null;
		parsedTextPath = null;

		titles = new ArrayList<String>();
		dates = new ArrayList<String>();
		creators = new ArrayList<String>();
		contributors = new ArrayList<String>();
		publishers = new ArrayList<String>();
		descriptions = new ArrayList<String>();
	}
	
	public DbBook(String name, String parsedTextPath) {
		this.name = name;
		this.parsedTextPath = parsedTextPath;

		titles = new ArrayList<String>();
		dates = new ArrayList<String>();
		creators = new ArrayList<String>();
		contributors = new ArrayList<String>();
		publishers = new ArrayList<String>();
		descriptions = new ArrayList<String>();
	}
	
	//Object will return in this format.
	public String toString() {
		return name + "/" + parsedTextPath;
	}
}
