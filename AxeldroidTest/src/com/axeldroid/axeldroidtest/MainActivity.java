package com.axeldroid.axeldroidtest;

import com.axeldroid.Axel;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		new DownloadFilesTask().execute("");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class DownloadFilesTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			Axel axel = new Axel() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.dragon.net.Axel#onProgress()
				 */
				@Override
				public void onProgress() {
					// TODO Auto-generated method stub
					super.onProgress();
					Log.v("axel", "progress(byte):" + bytes_done
							+ ",speed(byte/s):" + bytes_per_second
							+ ",time left(s):" + left_seconds);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.dragon.net.Axel#onFinish()
				 */
				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					super.onFinish();

					Log.v("axel", "onfinished,size:" + bytes_done
							+ ",cost_seconds:" + cost_seconds);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see com.dragon.net.Axel#onStart()
				 */
				@Override
				public void onStart() {
					// TODO Auto-generated method stub
					super.onStart();
				}

			};
			axel.axel_new("/mnt/sdcard/adgpq.mov",
					new String[] { "http://192.168.48.122/gyf2.mov" });
			axel.connections = 4;

			try {
				axel.axel_start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// http://192.168.48.26/CZHDP.war"http://hezuo.down.xunlei.com/xunlei_hezuo_2/thunder(12564).exe"a
			return null;
		}

	}
}
