package com.sequenia.reader;

import com.sequenia.reader.surface.ReaderSurface;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
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
			final Context context = this;
			OpenFileDialog fileDialog = new OpenFileDialog(this)
			.setFilter(".*\\.epub")
			.setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
				@Override
				public void OnSelectedFile(String fileName) {
					new LibraryManager().addToLibrary(context, fileName, surface);
				}
			});
			fileDialog.show();
			return true;
			
		case R.id.action_show_library:
			Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
