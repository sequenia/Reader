package com.sequenia.reader;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class LibraryManager {
	private ProgressDialog pd;
	
	public LibraryManager() {
		
	}
	
	public void addToLibrary(Context context, String filename, ReaderSurface surface) {
		pd = createProgressDialog(context);
		pd.show();

		AddToLibraryTask task = new AddToLibraryTask(filename, surface);
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
		Reader reader;

		public AddToLibraryTask(String _filename, ReaderSurface _surface) {
			filename = _filename;
			settings = _surface.getSettings();
			reader = _surface.getReader();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
	
		@Override
		protected Void doInBackground(String... params) {
			Book book = BookParser.construct(filename).parse();
			
			float readerBookX = 0.0f;
			float readerBookY = 0.0f;

			ArrayList<ReaderBook> books = reader.getBooks();
			int booksCount = books.size();
			if(booksCount > 0) {
				ReaderBook lastBook = books.get(booksCount - 1);
				readerBookX = lastBook.getAbsoluteX() + lastBook.getWidth() + settings.getScreenWidth();
				readerBookY = lastBook.getAbsoluteY();
			}
			
			ReaderBook readerBook = ReaderBookCreator.createReaderBook(book, settings, readerBookX, readerBookY);
			reader.addBook(readerBook);
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
