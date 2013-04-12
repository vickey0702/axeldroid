/*
 *   Copyright 2012 by dragon 
 *   bolg:	http://blog.csdn.net/xidomlove
 *   mail:	fufulove2012@gmail.com
 *   File:      Axel.java
 *   Date:      2012-12-17下午10:50:47
 */
package com.axeldroid;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.os.AsyncTask;

/**
 * @author ganyouf
 * 
 */
/**
 * @author ganyouf
 * 
 */
/**
 * @author ganyouf
 * 
 */
public class Axel {

	private static final java.util.Timer timer = new java.util.Timer(true);

	private static final Executor myExecutor = Executors.newCachedThreadPool();

	/**
	 * 已下载字节数
	 */
	protected long bytes_done;

	/**
	 * 文件总大小
	 */
	protected long file_size;

	/**
	 * 当前下载速度，
	 */
	protected int bytes_per_second;

	/**
	 * 离下载完成剩下的时间
	 */
	protected int left_seconds;

	/**
	 * 下载完成消耗的总时间
	 */
	protected int cost_seconds;

	private TimerTask task;

	/**
	 * 连接数
	 */
	public int connections = 2;

	/**
	 * 通知间隔时间(毫秒)
	 */
	public int progressDelay = 1000;

	/**
	 * 指向axel的指针
	 * 
	 */
	private long pMyAxel = 0;

	/**
	 * AxelFile 对象
	 */
	private AxelFile axelFile;

	/**
	 * @return the axelFile
	 */
	public AxelFile getAxelFile() {
		return axelFile;
	}

	static {
		System.loadLibrary("axeldroid");
	}

	/**
	 * 
	 */
	public Axel() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 新建一个下载任务
	 * 
	 * @param saveFileNameString
	 *            保存的文件名
	 * @param urls
	 *            地址（可以多个以便从不同服务器下载）
	 */
	public final void axel_new(String saveFileNameString, String[] urls) {
		this.axelFile = new AxelFile(saveFileNameString);
		this.axelFile.setUrlStrings(urls);
		writeURLs();
	}

	/**
	 * 新建一个下载任务
	 * 
	 * @param axelFile
	 *            该对象包含里文件的一些信息
	 */
	public final void axel_new(AxelFile axelFile) {
		this.axelFile = axelFile;
		writeURLs();
	}

	private void writeURLs() {

		try {
			// File file=new File(axelFile
			// .getAbsolutePath() + ".st.urls");
			// file.
			BufferedWriter bufferedWriter = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(axelFile
							.getAbsolutePath() + ".st.urls")));
			for (String url : axelFile.getUrlStrings()) {
				bufferedWriter.write(url);
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 开始下载
	 * 
	 * @throws Exception
	 */
	public final void axel_start() throws Exception {
		if (axelFile.getUrlStrings() == null) {
			throw new Exception("url is null,can not start download");
		}
		new DownloadFilesTask().executeOnExecutor(myExecutor, 0);
	}

	// private final void start() {
	//
	// }

	private native void newTask(int conns, String fnString, String[] urls);

	private native void refreshProgress(long pAxel);

	/**
	 * 重写该方法以获取进度通知
	 */
	protected void onProgress() {
		// System.out.println("progress:" + bytes_done);
	}

	/**重写该方法以获取即将完成的通知
	 * @param message
	 * 0:正常
	 * -1:url解析错误
	 * -2:打开本地文件错误
	 * -3:连接初始化错误（无法连接到服务器，代理服务器错误等）
	 * -4:文件名太长
	 * -5:文件已经存在，但是找不到上次下载保存的状态
	 * -100:其他错误
	 */
	protected void onFinish(int message) {
		task.cancel();
	}

	/**
	 * 即将开始下载
	 */
	protected void onStart() {

	}

	private native void axel_stop(long paxel);

	/**
	 * 停止下载并保存状态
	 */
	public final void stop() {
		axel_stop(pMyAxel);
	}

	private class DownloadFilesTask extends
			AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			// TODO Auto-generated method stub

			task = new TimerTask() {
				public void run() {
					if (pMyAxel != 0) {
						refreshProgress(pMyAxel);
						publishProgress(0);
					}
				}
			};
			timer.schedule(task, 0, progressDelay);
			newTask(connections, axelFile.getAbsolutePath(),
					axelFile.getUrlStrings());
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			onProgress();
		}

	}
}
