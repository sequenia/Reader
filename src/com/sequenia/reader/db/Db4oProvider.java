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
	
	public void deletBook(DbBook book) {
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
	
	public void deleteDatabase() {
		try {
			db().close();
			File file = new File(getContext().getDir("data", 0), getDbName());
			file.delete();
		} catch (NullPointerException e) {
		}
	}
}
