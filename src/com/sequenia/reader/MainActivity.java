package com.sequenia.reader;

import android.support.v7.app.ActionBarActivity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
	ReaderSurface surface;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		surface = new ReaderSurface(this);
		
		setContentView(surface);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		surface.runDrawThread();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		surface.stopDrawThread();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
		case R.id.action_to_current_page:
			surface.moveToCurrentPage();
			return true;

		case R.id.action_add_to_library:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
