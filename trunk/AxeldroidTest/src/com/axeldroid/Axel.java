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
public class Axel {

	private static final java.util.Timer timer = new java.util.Timer(true);

	private static final Executor myExecutor = Executors.newCachedThreadPool();

	// 已下载字节数
	protected long bytes_done;

	// 文件总大小
	protected long file_size;

	// 下载速度，
	protected int bytes_per_second;

	// 剩下的时间
	protected int left_seconds;

	// 下载完成消耗的时间
	protected int cost_seconds;

	private TimerTask task;

	// 连接数
	public int connections = 2;

	// 通知间隔时间(毫秒)
	public int progressDelay = 1000;

	// 指向axel的指针
	private long pMyAxel = 0;

	// AxelFile 对象
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

	public final void axel_new(String saveFileNameString, String[] urls) {
		this.axelFile = new AxelFile(saveFileNameString);
		this.axelFile.setUrlStrings(urls);
	}

	public final void axel_new(AxelFile axelFile) {
		this.axelFile = axelFile;

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

	// 进度通知，
	protected void onProgress() {
		// System.out.println("progress:" + bytes_done);
	}

	// 即将完成
	protected void onFinish() {
		task.cancel();
	}

	// 即将开始下载
	protected void onStart() {

	}

	private native void axel_stop(long paxel);

	// 停止下载并保存状态
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
