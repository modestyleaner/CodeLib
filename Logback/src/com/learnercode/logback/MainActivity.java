package com.learnercode.logback;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		String name="Jack" ;
		String [] fruits = {"apple","bnana"};
		Logger log = LoggerFactory.getLogger(MainActivity.class);
		log.info("hello world ");
		//支持变量打印方式
		//log.info("my name is {}",name);
		//log.info("i like {}　{} ",fruits);
		log.debug("i am a debug level log");
		log.warn("i am a warn level log");
		
		Log.d("debug", "--------------debug");
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
