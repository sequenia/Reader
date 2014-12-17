package com.sequenia.reader;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sequenia.reader.Book.BookPage;
import com.sequenia.reader.Book.PageElem;
import com.sequenia.reader.Book.PageText;

/*
 * Парсер электронных книг.
 * Используется для приведения электронной книги к общему формату, описанному в классе Book.
 */
public class BookParser {
	private String filename;
	
	public static BookParser construct(String _filename) {
		String extension = _filename.substring(_filename.lastIndexOf("."));
		BookParser parser;
		
		while(true) {
			if(extension.equals(".epub")) {
				parser = new EpubParser(_filename);
				break;
			}
			
			parser = new BookParser(_filename);
			break;
		}

		return parser;
	}
	
	// Должен возвращать книгу в формате класса Book
	public Book parse() {
		return null;
	}
	
	public BookParser(String _filename) {
		filename = _filename;
	}
	
	public void setFilename(String _filename) {
		filename = _filename;
	}
	
	public String getFilename() {
		return filename;
	}
}

/*
 * Парсер книжек формата epub.
 * 
 * Книга в формате epub представляет собой набор html файлов,
 * упакованных в zip архив.
 * 
 * Путь к файлу, содержащему структуру книги (Корневой файл) находится в файле "META-INF/container.xml" (EpubInfo.containerFileName).
 * Ресурсы, используемые в книжке, перечислены в элементе Manifest корневого файла.
 * Порядок чтения ресурсов указан в элементе Spine корневого файла.
 */
class EpubParser extends BookParser {

	public EpubParser(String _filename) {
		super(_filename);
	}
	
	@Override
	public Book parse() {
		System.out.println("Парсинг книги EPUB....");

		String filename = getFilename();
		System.out.println("Название файла: " + filename);
		
		// В файле с именем containerFileName находится путь к файлу, в котором описана структура книги
		String containerFileName = EpubInfo.containerFileName;
		
		Book book = new Book();

		// EPUB является zip архивом. Считываем файлы из архива в файловое дерево
		System.out.println("Чтение из zip архива...");
		FilesTree files = zipToFiles(filename);
		System.out.println("Чтение из архива завершено");

		org.w3c.dom.Document containerXml = getDomDocumentFromBuffer(files.findNode(containerFileName).getData());
		String rootFileName = getRootFileName(containerXml);
		if(rootFileName == null) {
			System.out.println("ОШИБКА: нет имени корневого файла в " + containerFileName);
			return null;
		}
		System.out.println("Имя корневого файла: " + rootFileName);
		
		// rootFile - файл с описанием структуры книги
		System.out.println("Чтение данных из корневого файла...");
		org.w3c.dom.Document rootFile = getDomDocumentFromBuffer(files.findNode(rootFileName).getData());
		if(!parseMetadata(rootFile, book)) {
			System.out.println("ОШИБКА: Не удалость считать метаданные");
			return null;
		}
		System.out.println("Метаданные считаны");
		
		HashMap<String, ManifestItem> manifest = parseManifest(rootFile);
		if(manifest == null) {
			System.out.println("ОШИБКА: не удалось считать манифест");
			return null;
		}
		System.out.println("Манифест считан");
		
		ArrayList<SpineItem> spine = parseSpine(rootFile);
		if(spine == null) {
			System.out.println("ОШИБКА: не удалось считать позвоночник");
			return null;
		}
		System.out.println("Позвоночник считан");
		System.out.println("Чтение данных из корневого файла закончено");
		
		System.out.println("Чтение контента книги...");
		parseBookContent(book, files, manifest, spine, rootFileName);
		System.out.println("Чтение контента книги закончено");
		System.out.println("Парсинг EPUB завершен!");
		
		return book;
	}
	
	private boolean parseBookContent(Book book, FilesTree files, HashMap<String, ManifestItem> manifest, ArrayList<SpineItem> spine, String rootFileName) {
		String rootPath = "";
		
		String[] names = rootFileName.split("/");
		for(int i = 0; i < names.length - 1; i++) {
			rootPath += names[i] + "/";
		}
		
		for(int i = 0; i < spine.size(); i++) {
			ManifestItem manifestItem = manifest.get(spine.get(i).idref);
			
			if(manifestItem.type.equals("application/xhtml+xml")) {
				TreeNode<ByteArrayOutputStream> node = findNode(manifestItem, files, rootPath);
				org.w3c.dom.Document file = getDomDocumentFromBuffer(node.getData());
				parseSpineFile(file, book);
			}
		}
		
		return true;
	}

	private boolean parseSpineFile(org.w3c.dom.Document file, Book book) {
		if(file == null || book == null) {
			System.out.println("ОШИБКА: parseSpineFile - xml or book is null");
			return false;
		}
		
		NodeList nList = getElementsByTagName(file, EpubInfo.bodyTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseSpineFile - отсутствует body в документе.");
			return false; 
		}
		
		Element body = (Element)nList.item(0);
		String text = body.getTextContent();
		
		BookPage page = new BookPage();
		PageElem pageText = new PageText(text);
		page.elements.add(pageText);
		book.pages.add(page);
		
		return true;
	}

	private TreeNode<ByteArrayOutputStream> findNode(ManifestItem manifestItem, FilesTree files, String rootPath) {
		String filename = manifestItem.href;
		TreeNode<ByteArrayOutputStream> node = files.findNode(filename);
		if(node == null) {
			node = files.findNode(rootPath + filename);
		}
		
		return node;
	}
	
	private ArrayList<SpineItem> parseSpine(org.w3c.dom.Document xml) {
		if(xml == null) {
			System.out.println("ОШИБКА: parseSpine - xml is null");
			return null;
		}
		
		NodeList nList = getElementsByTagName(xml, EpubInfo.spineTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseSpine - отсутствует spine.");
			return null; 
		}
		
		Element spine = (Element)nList.item(0);
		ArrayList<SpineItem> spineItems = new ArrayList<SpineItem>();
		
		nList = getElementsByTagName(spine, EpubInfo.itemrefTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			Element elem = (Element) nNode;
			String idref = elem.getAttribute(EpubInfo.idrefAttrName);
			SpineItem item = new SpineItem(idref);
			spineItems.add(item);
		}
		
		return spineItems;
	}
	
	private boolean parseMetadata(org.w3c.dom.Document xml, Book book) {
		if(book == null || xml == null) {
			System.out.println("ОШИБКА: parseMetadata - xml or book is null");
			return false;
		}

		NodeList nList = getElementsByTagName(xml, EpubInfo.metadataTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseMetadata - отсутствуют метаданные.");
			return false; 
		}
		
		Element metadata = (Element)nList.item(0);
				
		nList =  getElementsByTagName(metadata, EpubInfo.titleTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.titles.add(nNode.getTextContent());
		}
		
		nList = getElementsByTagName(metadata, EpubInfo.dateTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.dates.add(nNode.getTextContent());
		}
		
		nList = getElementsByTagName(metadata, EpubInfo.creatorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.creators.add(nNode.getTextContent());
		}
		
		nList = getElementsByTagName(metadata, EpubInfo.contributorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.contributors.add(nNode.getTextContent());
		}
		
		nList = getElementsByTagName(metadata, EpubInfo.publisherTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.publishers.add(nNode.getTextContent());
		}
		
		nList = getElementsByTagName(metadata, EpubInfo.descriptionTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.descriptions.add(nNode.getTextContent());
		}
		
		return true;
	}
	
	private HashMap<String, ManifestItem> parseManifest(org.w3c.dom.Document xml) {
		if(xml == null) {
			System.out.println("ОШИБКА: parseManifest - xml is null");
			return null;
		}
		
		NodeList nList = getElementsByTagName(xml, EpubInfo.manifestTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseMetadata - отсутствует манифест.");
			return null; 
		}
		
		Element manifest = (Element)nList.item(0);
		HashMap<String, ManifestItem> manifestItems = new HashMap<String, ManifestItem>();
		
		nList = getElementsByTagName(manifest, EpubInfo.itemTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			Element elem = (Element) nNode;
			String id = elem.getAttribute(EpubInfo.idAttrName);
			String href = elem.getAttribute(EpubInfo.hrefAttrName);
			String type = elem.getAttribute(EpubInfo.typeAttrName);
			ManifestItem item = new ManifestItem(id, href, type);
			manifestItems.put(id, item);
		}
		
		return manifestItems;
	}
	
	private String getRootFileName(org.w3c.dom.Document xml) {
		if(xml == null) {
			System.out.println("ОШИБКА: getRootFileName - xml is null");
			return null;
		}

		NodeList nList = getElementsByTagName(xml, EpubInfo.rootFileTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: no rootfile tag in container xml");
			return null;
		}
		
		Node nNode = nList.item(0);
		Element eElement = (Element) nNode;
		String rootFileName = eElement.getAttribute(EpubInfo.fullPathAttrName);
		
		return rootFileName;
	}
	
	private FilesTree zipToFiles(String filename) {
		InputStream is;
		FilesTree tree = null;
		try {
			is = new FileInputStream(filename);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			tree = new FilesTree(zis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return tree;
	}
	
	private org.w3c.dom.Document getDomDocumentFromBuffer(ByteArrayOutputStream buffer) {
		if(buffer == null) {
			System.out.println("ОШИБКА: getDomDocumentFromBuffer - buffer is null");
			return null; 
		}

		org.w3c.dom.Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			try {
				doc = dBuilder.parse(new ByteArrayInputStream(buffer.toByteArray()));
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		return doc;
	}
	
	public class ManifestItem {
		public String id;
		public String href;
		public String type;
		
		public ManifestItem(String _id, String _href, String _type) {
			id = _id;
			href = _href;
			type = _type;
		}
	}
	
	public class SpineItem {
		public String idref;
		
		public SpineItem(String _idref) {
			idref = _idref;
		}
	}
	
	private NodeList getElementsByTagName(org.w3c.dom.Document xml, String tagname) {
		NodeList nList = xml.getElementsByTagName(tagname);
		if(nList.getLength() == 0) {
			nList = xml.getElementsByTagNameNS("*", tagname);
		}
		return nList;
	}
	
	private NodeList getElementsByTagName(Element element, String tagname) {
		NodeList nList = element.getElementsByTagName(tagname);
		if(nList.getLength() == 0) {
			nList = element.getElementsByTagNameNS("*", tagname);
		}
		return nList;
	}
}