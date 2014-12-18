package com.sequenia.reader;

import java.util.ArrayList;
import java.util.Random;

import com.sequenia.reader.db.Db4oProvider;
import com.sequenia.reader.db.DbBook;
import com.sequenia.reader.parsers.Book;
import com.sequenia.reader.parsers.BookParser;
import com.sequenia.reader.reader_objects.ReaderBook;
import com.sequenia.reader.surface.ReaderSurface;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class LibraryManager {
	private ProgressDialog pd;
	private Db4oProvider provider;
	private Context context;
	
	public LibraryManager() {
		
	}
	
	public void addToLibrary(Context context, String filename, ReaderSurface surface) {
		this.context = context;

		pd = createProgressDialog(context);
		pd.show();
		
		provider = new Db4oProvider(context);

		AddToLibraryTask task = new AddToLibraryTask(filename, surface);
		task.execute();
	}
	
	public void showBook(Context context, String name, ReaderSurface surface) {
		ReaderSettings settings = surface.getSettings();
		Reader reader = surface.getReader();
		
		DbBook queryBook = new DbBook();
		queryBook.name = name;
		ArrayList<DbBook> books = (ArrayList<DbBook>) provider.getRecord(queryBook);
		
		if(books.size() > 0) {
			DbBook book = books.get(0);
			
			float readerBookX = 0.0f;
			float readerBookY = 0.0f;

			ArrayList<ReaderBook> readerBooks = reader.getBooks();
			int booksCount = readerBooks.size();
			if(booksCount > 0) {
				ReaderBook lastBook = readerBooks.get(booksCount - 1);
				readerBookX = lastBook.getAbsoluteX() + lastBook.getWidth() + settings.getScreenWidth();
				readerBookY = lastBook.getAbsoluteY();
			}
			
			ReaderBook readerBook = ReaderBookCreator.createReaderBook(book, settings, readerBookX, readerBookY);
			reader.addBook(readerBook);
		} else {
			System.out.println("Книга не найдена!");
		}
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
	
	private String genRandomString() {
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	
	private ArrayList<String> cloneArrayList(ArrayList<String> list) {
		ArrayList<String> newList = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++) {
			newList.add(new String(list.get(i)));
		}
		return newList;
	}
	
	class AddToLibraryTask extends AsyncTask<String, Integer, Void> {
		String filename;
		ReaderSettings settings;
		Reader reader;

		public AddToLibraryTask(String _filename, ReaderSurface _surface) {
			filename = _filename;
			settings = _surface.getSettings();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
	
		@Override
		protected Void doInBackground(String... params) {
			// Приведение книги к общему виду
			Book book = BookParser.construct(filename).parse();
			
			if(book == null) {
				System.out.println("Ошибка при парсинге книги");
				return null;
			}
			
			pd.setIndeterminate(false);
			
			addBookToDb(book, filename);
			
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
		
		public void publish(int value) {
			publishProgress(value);
		}
		
		public void addBookToDb(Book book, String filename) {
			DbBook dbBook = new DbBook();
			
			if(book.titles.size() > 0) {
				dbBook.name = book.titles.get(0);
			} else {
				dbBook.name = filename;
			}
			
			dbBook.parsedTextPath = genRandomString();
			dbBook.titles = cloneArrayList(book.titles);
			dbBook.dates = cloneArrayList(book.dates);
			dbBook.creators = cloneArrayList(book.creators);
			dbBook.contributors = cloneArrayList(book.contributors);
			dbBook.publishers = cloneArrayList(book.publishers);
			dbBook.descriptions = cloneArrayList(book.descriptions);
			
			ReaderBookCreator.createParsedTextFile(book, dbBook.parsedTextPath, settings, this, context);
			
			provider.store(dbBook);
		}
	}
}
