package com.sequenia.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sequenia.reader.db.Db4oProvider;
import com.sequenia.reader.db.DbBook;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class LibraryActivity extends ListActivity {
	
	Db4oProvider provider;
	ArrayList<HashMap<String, Object>> booksList;
	ListView listView;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setContentView(R.layout.activity_library);
		
		provider = new Db4oProvider(this);

		showBooks(provider.findAllBooks());
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
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				String bookName = ((TextView) view.findViewById(R.id.tv_name)).getText().toString();
				Intent returnIntent = new Intent();
				returnIntent.putExtra("bookName", bookName);
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
	}
	
	public void deleteDatabase(View v) {
		provider.deleteDatabase();
		showBooks(provider.findAllBooks());
	}
	
	public void deleteBooks(View v) {
		provider.deleteAllBooks();
		showBooks(provider.findAllBooks());
	}
}
