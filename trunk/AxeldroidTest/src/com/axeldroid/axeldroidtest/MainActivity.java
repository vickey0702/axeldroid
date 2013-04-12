package com.axeldroid.axeldroidtest;

import com.axeldroid.Axel;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	//Axel axel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Axel axel = new Axel() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.dragon.net.Axel#onProgress()
			 */
			@Override
			protected void onProgress() {
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
			protected void onFinish(int message) {
				// TODO Auto-generated method stub
				super.onFinish(message);

				Log.v("axel", "onfinished,size:" + bytes_done
						+ ",cost_seconds:" + cost_seconds);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see com.dragon.net.Axel#onStart()
			 */
			@Override
			protected void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
			}

		};
		axel.axel_new("/mnt/sdcard/aa.jpg",
				new String[] { "http://avatar.csdn.net/A/1/4/1_stefzeus.jpg" });
		// axel.axel_new(
		// "/mnt/sdcard/CZPAD/a.exe",
		// new String[] {
		// "http://hezuo.down.xunlei.com/xunlei_hezuo_2/thunder(12564).exe" });
		axel.connections = 1;

		try {
			axel.axel_start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
