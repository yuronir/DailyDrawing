package com.designproject.dreamcoding.activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class DefaultActivity extends SherlockFragmentActivity {
	
	protected ActionBar mActionBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mActionBar = getSupportActionBar();
		mActionBar.setTitle("Daily Drawing");
	}
}
