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

	public static Db4oProvider getInstance(Context ctx) {
		if (provider == null) {
			provider = new Db4oProvider(ctx);
		}
		return provider;
	}
	
	public void store(DbBook book) {
		db().store(book);
		db().commit();
	}
	
	public void delete(DbBook book) {
		db().delete(book);
		db().commit();
	}
	
	public List<DbBook> findAll() {
		return db().query(DbBook.class);
	}
	
	public void deleteAll() {
		try {
			db().close();
			File file = new File(getContext().getDir("data", 0), getDbName());
			file.delete();
		} catch (NullPointerException e) {
		}
	}
	
	public List<DbBook> getRecord(DbBook book) {
		return db().queryByExample(book);
	}
}
