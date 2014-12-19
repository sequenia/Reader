package com.sequenia.reader;

import java.util.ArrayList;
import java.util.Random;

import com.sequenia.reader.db.Db4oProvider;
import com.sequenia.reader.db.DbBook;
import com.sequenia.reader.parsers.Book;
import com.sequenia.reader.parsers.BookContentParser;
import com.sequenia.reader.parsers.BookParser;
import com.sequenia.reader.reader_objects.ReaderBook;
import com.sequenia.reader.surface.ReaderSurface;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author chybakut2004
 *
 * Управляет библиотекой книг:
 * 1. Добавляет книги в библиотеку
 * 2. Показывает книги из библиотеки на экране
 *
 */
public class LibraryManager {
	private ProgressDialog pd;
	private Db4oProvider provider;
	private Context context;
	
	public LibraryManager(Context context) {
		this.context = context;
	}
	
	/**
	 * @param context
	 * @param filename
	 * @param surface
	 * 
	 * Добавляет книгу в библиотеку.
	 * Производит парсинг электронного формата книги, приводит его ко внутреннему формату,
	 * и сохраняет книгу в базе данных.
	 */
	public void addToLibrary(String filename, ReaderSurface surface) {
		pd = createProgressDialog(context);
		pd.show();
		
		provider = new Db4oProvider(context);

		AddToLibraryTask task = new AddToLibraryTask(filename, surface);
		task.execute();
	}
	
	/**
	 * @param context
	 * @param name
	 * @param surface
	 * 
	 * Показывает книгу из базы данных на экране
	 */
	public void showBook(String name, ReaderSurface surface) {
		ReaderSettings settings = surface.getSettings();
		Reader reader = surface.getReader();
		
		provider = new Db4oProvider(context);
		
		DbBook book = provider.findByName(name);
		
		if(book != null) {
			// Ищем координаты, в которых показать книгу
			float readerBookX = 0.0f;
			float readerBookY = 0.0f;

			ArrayList<ReaderBook> readerBooks = reader.getBooks();
			int booksCount = readerBooks.size();
			if(booksCount > 0) {
				ReaderBook lastBook = readerBooks.get(booksCount - 1);
				readerBookX = lastBook.getAbsoluteX() + lastBook.getWidth() + settings.getScreenWidth();
				readerBookY = lastBook.getAbsoluteY();
			}
			
			// Создаем рисуемую на экране книгу
			ReaderBook readerBook = ReaderBookCreator.createReaderBook(context, book, settings, readerBookX, readerBookY);
			if(readerBook != null) {
				reader.addBook(readerBook);
			}
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
	
	public class AddToLibraryTask extends AsyncTask<String, Integer, Void> {
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
			// Приводим книгу к общему внутреннему виду
			Book book = BookParser.construct(filename).parse();
			
			if(book == null) {
				System.out.println("Ошибка при парсинге книги");
				return null;
			}
			
			pd.setIndeterminate(false);
			
			// Добавляем эту книгу в базу данных
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
			
			try {
				provider.storeBook(dbBook);
				BookContentParser.createParsedTextFile(book, dbBook.parsedTextPath, settings, this, context);
			} catch (Exception e) {
				Log.e("ОШИБКА", e.toString());
			}
		}
	}
}
