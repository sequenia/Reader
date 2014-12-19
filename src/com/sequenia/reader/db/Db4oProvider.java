package com.sequenia.reader.db;

import java.io.File;
import java.util.List;

import android.content.Context;

public class Db4oProvider extends Db4oHelper {

	private static Db4oProvider provider = null;
	
	public Db4oProvider(Context ctx) {
		super(ctx);
		setDbName("reader.db4o");
	}
	
	/*@Override
	public EmbeddedConfiguration dbConfig() throws IOException {
		EmbeddedConfiguration configuration = super.dbConfig();
		
		configuration.common().objectClass(DbBook.class).objectField("name").indexed(true);
		configuration.common().add(new UniqueFieldValueConstraint(DbBook.class, "name"));
		
		return configuration;
	}*/

	public static Db4oProvider getInstance(Context ctx) {
		if (provider == null) {
			provider = new Db4oProvider(ctx);
		}
		return provider;
	}
	
	public void storeBook(DbBook book) throws Exception {
		if(book.name != null) {
			DbBook existing = findByName(book.name);
			if(existing == null) {
				db().store(book);
				db().commit();
			} else {
				System.out.println("Книга с таким именем уже существует");
				throw new Exception();
			}
		} else {
			System.out.println("Книга не добавлена. Имя не может быть пустым");
			throw new Exception();
		}
	}
	
	public void deleteBook(DbBook book) {
		
		if(book.parsedTextPath != null) {
			File dir = getContext().getFilesDir();
			File text = new File(dir, book.parsedTextPath);
			if(text.exists()) {
				text.delete();
			}
		}
		
		db().delete(book);
		db().commit();
	}
	
	public List<DbBook> findAllBooks() {
		return db().query(DbBook.class);
	}
	
	public List<DbBook> getBooks(DbBook book) {
		return db().queryByExample(book);
	}
	
	public DbBook findByName(String name) {
		DbBook example = new DbBook(name, null);
		List<DbBook> matched = db().queryByExample(example);
		
		if(!matched.isEmpty()) {
			return matched.get(0);
		} else {
			return null;
		}
	}
	
	public void deleteAllBooks() {
		List<DbBook> books = findAllBooks();
		for(DbBook b : books) {
			deleteBook(b);
		}
	}
	
	public void deleteDatabase() {
		try {
			db().close();
			File file = new File(getContext().getDir("data", 0), getDbName());
			file.delete();
			
			File dir = getContext().getFilesDir();
			String[] files = dir.list();
			for(int i = 0; i < files.length; i++) {
				File dirFile = new File(dir, files[i]);
				if(!dirFile.isDirectory()) {
					dirFile.delete();
				}
			}
			
		} catch (NullPointerException e) {
		}
	}
}
