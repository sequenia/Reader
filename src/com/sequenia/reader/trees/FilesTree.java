package com.sequenia.reader.trees;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * Класс используется для построения дерева файлов из ZIP архива.
 * Дерево необходимо для доступа к файлам как по абсолютому пути, так и по относительному.
 */
public class FilesTree {
	private FilesNode<ByteArrayOutputStream> root;
	
	public FilesTree(ZipInputStream zis) {
		root = new FilesNode<ByteArrayOutputStream>();
		root.setParent(null);
		root.setDirectory(true);
		
		ZipEntry ze;

		try {
			while((ze = zis.getNextEntry()) != null) {
				String filename = ze.getName();       // Абсолютный путь к текущему файлу с его именем
				String[] names = filename.split("/"); // Абсолютный путь, разбитый по директориям и файлам
				int length = names.length;
				String shortName = names[length - 1]; // Имя файла без пути

				String path = "";                     // Директория, в которой находится файл
				for(int i = 0; i < length -1; i++) {
					path += names[i] + "/";
				}
				
				// Ищем узел (директорию), в который поместить файл
				FilesNode<ByteArrayOutputStream> parent = (FilesNode<ByteArrayOutputStream>) findNode(path);
				// Если такого узла нет, создаем его
				if(parent == null) {
					parent = root;
					for(int i = 0; i < names.length - 1; i++) {
						FilesNode<ByteArrayOutputStream> dirNode = (FilesNode<ByteArrayOutputStream>) parent.getChild(names[i]);
						if(dirNode == null) {
							dirNode = new FilesNode<ByteArrayOutputStream>();
						}
						dirNode.setParent(parent);
						dirNode.setDirectory(true);
						parent.addChild(names[i], dirNode);
						parent = dirNode;
					}
				}
				
				// Создаем узел для текущего файла и помещаем его в дерево
				FilesNode<ByteArrayOutputStream> newNode = new FilesNode<ByteArrayOutputStream>();
				newNode.setParent(parent);
				newNode.setDirectory(ze.isDirectory());
				parent.addChild(shortName, newNode);
				
				if (ze.isDirectory()) {
					continue;
				}
				
				// Если это не директория, запихиваем данные в дерево
				ByteArrayOutputStream file = getByteBuffer(zis);
				if(file != null) {
					newNode.setData(file);
				}
				
				zis.closeEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Ищет узел по абсолютному пути
	 */
	public TreeNode<ByteArrayOutputStream> findNode(String name) {
		if(name.equals("")) {
			return root;
		}

		TreeNode<ByteArrayOutputStream> rootNode = root;
		String[] names = name.split("/");
		
		for(int i = 0; i < names.length && rootNode != null; i++) {
			if(names[i].equals("")) {
				continue;
			}

			if(names[i].equals(".")) {
				rootNode = rootNode.getParent();
			} else {
				rootNode = rootNode.getChild(names[i]);
			}
		}
		
		return rootNode;
	}
	
	private ByteArrayOutputStream getByteBuffer(ZipInputStream zis) {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		try {
			int n;
			while ((n = zis.read(buffer)) != -1) {
				byteBuffer.write(buffer, 0, n);
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
			return null;
		}
		
		return byteBuffer;
	}
	
	public class FilesNode<T> extends TreeNode<T> {
		private boolean directory;
		
		public void setDirectory(boolean _directory) {
			directory = _directory;
		}
		
		public boolean getDirectory() {
			return directory;
		}
	}
}