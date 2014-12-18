package com.sequenia.reader;

import android.app.Activity;
import android.os.Bundle;

public class LibraryActivity extends Activity {

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		setContentView(R.layout.activity_library);
	}
}
