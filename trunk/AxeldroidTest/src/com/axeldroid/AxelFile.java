/*
 *   Copyright 2012 by dragon 
 *   bolg:	http://blog.csdn.net/xidomlove
 *   mail:	fufulove2012@gmail.com
 *   File:      AxelFile.java
 *   Date:      2012-12-20下午1:24:21
 */
package com.axeldroid;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ganyouf
 * 
 */
public class AxelFile extends File {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5690581242749557944L;

	private long bytes_done;
	private int connections = 2;
	private long[] connections_bytes_done;

	private int fileInfo;

	private String[] urlStrings;

	/**
	 * @return the urlStrings
	 */
	public String[] getUrlStrings() {
		return urlStrings;
	}

	/**
	 * @param urlStrings
	 *            the urlStrings to set
	 */
	public void setUrlStrings(String[] urlStrings) {
		this.urlStrings = urlStrings;
	}

	/**
	 * @return the bytes_done
	 * 获取当前已经下载的大小（字节数）
	 */
	public long getBytes_done() {
		return bytes_done;
	}

	/**
	 * @return the connections
	 * 获取该文件下载使用的连接数
	 */
	public int getConnections() {
		return connections;
	}

	/**
	 * @return the connections_bytes_done
	 * 获取各个连接分别已经下载完的大小
	 */
	public long[] getConnections_bytes_done() {
		return connections_bytes_done;
	}

	/**
	 * @return the fileInfo
	 * fileINfo的值：
	 * 0：文件不存在，为新建文件
	 * 1：文件存在并且已经下载完了
	 * 2：文件存在，但是没有下载完，可以继续下载
	 */
	public int getFileInfo() {
		return fileInfo;
	}

	/**
	 * @param path
	 */
	public AxelFile(String path) {
		super(path);
		// TODO Auto-generated constructor stub
		init();
	}

	/**
	 * @param uri
	 */
	public AxelFile(URI uri) {
		super(uri);
		// TODO Auto-generated constructor stub
		init();
	}

	/**
	 * @param dir
	 * @param name
	 */
	public AxelFile(File dir, String name) {
		super(dir, name);
		// TODO Auto-generated constructor stub
		init();
	}

	/**
	 * @param dirPath
	 * @param name
	 */
	public AxelFile(String dirPath, String name) {
		super(dirPath, name);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		if (!exists()) {
			fileInfo = 0;
			return;
		}

		File file = new File(this.getName() + ".st");
		if (!file.exists()) {
			fileInfo = 1;
			return;
		}

		fileInfo = 2;
		try {
			DataInputStream disDataInputStream = new DataInputStream(
					new FileInputStream(file));

			connections = disDataInputStream.readInt();

			bytes_done = disDataInputStream.readLong();

			connections_bytes_done = new long[connections];
			for (int i = 0; i < connections; i++) {
				connections_bytes_done[i] = disDataInputStream.readLong();
			}
			disDataInputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readURLs();
	}

	private void readURLs() {
		try {
			List<String> urlsList = new ArrayList<String>();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(this.getName()
							+ ".st.urls")));
			String url = null;
			while ((url = bufferedReader.readLine()) != null) {
				urlsList.add(url);
			}
			urlStrings = (String[]) urlsList.toArray();
			bufferedReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
