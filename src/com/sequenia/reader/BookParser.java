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
import com.sequenia.reader.Book.TextElem;

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

class EpubParser extends BookParser {

	public EpubParser(String _filename) {
		super(_filename);
	}
	
	@Override
	public Book parse() {
		String filename = getFilename();
		String containerFileName = EpubInfo.containerFileName;
		
		Book book = new Book();
		FilesTree files = zipToFiles(filename);
		
		org.w3c.dom.Document containerXml = getDomDocumentFromBuffer(files.findNode(containerFileName).getData());
		String rootFileName = getRootFileName(containerXml);
		
		org.w3c.dom.Document rootFile = getDomDocumentFromBuffer(files.findNode(rootFileName).getData());
		if(!parseMetadata(rootFile, book)) { return null; }
		
		HashMap<String, ManifestItem> manifest = parseManifest(rootFile);
		if(manifest == null) { return null; }
		
		ArrayList<SpineItem> spine = parseSpine(rootFile);
		if(spine == null) { return null; }
		
		parseBookContent(book, files, manifest, spine, rootFileName);
		
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
		
		NodeList nList = file.getElementsByTagName(EpubInfo.bodyTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseSpineFile - отсутствует body в документе.");
			return false; 
		}
		
		Element body = (Element)nList.item(0);
		String text = body.getTextContent();
		
		BookPage page = new BookPage();
		PageElem pageText = new TextElem(text);
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
		
		NodeList nList = xml.getElementsByTagName(EpubInfo.spineTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseSpine - отсутствует spine.");
			return null; 
		}
		
		Element spine = (Element)nList.item(0);
		ArrayList<SpineItem> spineItems = new ArrayList<SpineItem>();
		
		nList = spine.getElementsByTagName(EpubInfo.itemrefTagName);
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

		NodeList nList = xml.getElementsByTagName(EpubInfo.metadataTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseMetadata - отсутствуют метаданные.");
			return false; 
		}
		
		Element metadata = (Element)nList.item(0);
				
		nList =  metadata.getElementsByTagName(EpubInfo.titleTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.titles.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.dateTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.dates.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.creatorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.creators.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.contributorTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.contributors.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.publisherTagName);
		for(int i = 0; i < nList.getLength(); i++) {
			Node nNode = nList.item(i);
			book.publishers.add(nNode.getTextContent());
		}
		
		nList = metadata.getElementsByTagName(EpubInfo.descriptionTagName);
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
		
		NodeList nList = xml.getElementsByTagName(EpubInfo.manifestTagName);
		if(nList.getLength() == 0) {
			System.out.println("ОШИБКА: parseMetadata - отсутствует манифест.");
			return null; 
		}
		
		Element manifest = (Element)nList.item(0);
		HashMap<String, ManifestItem> manifestItems = new HashMap<String, ManifestItem>();
		
		nList = manifest.getElementsByTagName(EpubInfo.itemTagName);
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

		NodeList nList = xml.getElementsByTagName(EpubInfo.rootFileTagName);
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
	
}