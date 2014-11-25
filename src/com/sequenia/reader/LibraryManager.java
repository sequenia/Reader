package com.sequenia.reader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class LibraryManager {
	private ProgressDialog pd;
	
	public LibraryManager() {
		
	}
	
	public void addToLibrary(Context context, String filename, ReaderSettings settings) {
		pd = createProgressDialog(context);
		pd.show();

		AddToLibraryTask task = new AddToLibraryTask(filename, settings);
		task.execute();
	}
	
	private ProgressDialog createProgressDialog(Context context) {
		ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage("Пожалуйста, подождите");
		pd.setCancelable(false);
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setMax(100);
		pd.setIndeterminate(true);
		return pd;
	}
	
	class AddToLibraryTask extends AsyncTask<String, Integer, Void> {
		String filename;
		ReaderSettings settings;

		public AddToLibraryTask(String _filename, ReaderSettings _settings) {
			filename = _filename;
			settings = _settings;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
	
		@Override
		protected Void doInBackground(String... params) {
			Book book = BookParser.construct(filename).parse();
			ReaderBook readerBook = ReaderBookCreator.createReaderBook(book, settings, 0.0f, 0.0f);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			pd.dismiss();
			super.onPostExecute(result);
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			pd.incrementProgressBy(values[0] - pd.getProgress());
			super.onProgressUpdate(values);
		}
	}
}
