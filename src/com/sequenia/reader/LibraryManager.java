package com.sequenia.reader;

import java.util.ArrayList;

import com.sequenia.parsers.Book;
import com.sequenia.parsers.BookParser;
import com.sequenia.reader_objects.ReaderBook;
import com.sequenia.surface.ReaderSurface;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/*
 * Используется для добавления книги в библиотеку (Вывода ее на экран).
 * 
 * Добавление книги в библиотеку состоит из следующих действий
 * 1: Приведение электронной книги к общему формату
 * 2: Преобразование книги из общего формата к формату вывода на экран
 * 
 * Добавление в библиотеку - долгий процесс.
 * Он проходит в отдельном потоке и сопровождается выводом на экран полосы прогресса.
 */
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
			// Приведение книги к общему виду
			Book book = BookParser.construct(filename).parse();
			
			if(book == null) {
				System.out.println("Ошибка при парсинге книги");
				return null;
			}

			// Поиск позиции для новой книги
			float readerBookX = 0.0f;
			float readerBookY = 0.0f;

			ArrayList<ReaderBook> books = reader.getBooks();
			int booksCount = books.size();
			if(booksCount > 0) {
				ReaderBook lastBook = books.get(booksCount - 1);
				readerBookX = lastBook.getAbsoluteX() + lastBook.getWidth() + settings.getScreenWidth();
				readerBookY = lastBook.getAbsoluteY();
			}
			
			pd.setIndeterminate(false);
			
			// Создание книги, отображаемой на экране
			ReaderBook readerBook = ReaderBookCreator.createReaderBook(book, settings, readerBookX, readerBookY, this);
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
		
		public void publish(int value) {
			publishProgress(value);
		}
	}
}
