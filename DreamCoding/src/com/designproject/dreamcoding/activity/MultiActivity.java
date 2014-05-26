package com.designproject.dreamcoding.activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;

import com.designproject.dreamcoding.R;

public class MultiActivity extends DefaultActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.multi, menu);
		return true;
	}

}
