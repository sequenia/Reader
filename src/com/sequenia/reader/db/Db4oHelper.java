package com.sequenia.reader.db;

import java.io.IOException;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;

import android.content.Context;
import android.util.Log;

public class Db4oHelper {
	private Context context;
	private static ObjectContainer oc = null;
	private String dbName;

	/**
	 * @param context
	 */
	public Db4oHelper(Context ctx) {
		context = ctx;
	}
	
	/** 
	  * Create, open and close the database 
	  */  
	public ObjectContainer db() {
		try {
			if(oc == null || oc.ext().isClosed()) {
				oc = Db4oEmbedded.openFile(dbConfig(), db4oDBFullPath(context));
			}
			
			return oc;
		} catch(Exception ie) {
			Log.e(Db4oHelper.class.getName(), ie.toString());  
			return null;
		}
	}
	
	/** 
	  * Configure the behavior of the database 
	  */
	public EmbeddedConfiguration dbConfig() throws IOException {
		EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
		configuration.common().activationDepth(3);
		return configuration;
	}
	
	private String db4oDBFullPath(Context ctx) {
		return ctx.getDir("data", 0) + "/" + dbName;
	}
	
	public void close() {
		if(oc != null) {
			oc.close();
		}
	}
	
	public Context getContext() {
		return context;
	}
	
	public String getDbName() {
		return dbName;
	}
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
}
