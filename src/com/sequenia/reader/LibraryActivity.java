package com.sequenia.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sequenia.reader.db.Db4oProvider;
import com.sequenia.reader.db.DbBook;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class LibraryActivity extends ListActivity {
	
	Db4oProvider provider;
	ArrayList<HashMap<String, Object>> booksList;
	ListView listView;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		provider = new Db4oProvider(this);
		showBooks(provider.findAll());
	}
	
	private void showBooks(List<DbBook> books) {
		booksList = new ArrayList<HashMap<String, Object>>();
		for(DbBook b : books) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("name", b.name);
			booksList.add(map);
		}
		
		ListAdapter adapter = new SimpleAdapter(this, booksList, R.layout.list_data,
				new String[] { "name" },
				new int[] { R.id.tv_name });
		
		listView = getListView();
		listView.setAdapter(adapter);
	}
}
