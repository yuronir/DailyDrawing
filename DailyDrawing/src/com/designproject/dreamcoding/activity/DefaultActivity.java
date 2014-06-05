package com.designproject.dreamcoding.activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class DefaultActivity extends SherlockFragmentActivity {
	
	protected ActionBar mActionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setTitle("Daily Drawing");
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		default:
			return false;
		}

		return true;
	}
}
