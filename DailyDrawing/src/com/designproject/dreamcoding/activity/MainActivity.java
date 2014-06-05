package com.designproject.dreamcoding.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.designproject.dreamcoding.R;

public class MainActivity extends DefaultActivity {
	
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		
		mContext =  this;
		mActionBar.setDisplayHomeAsUpEnabled(false);

		Button singleButton = (Button) findViewById(R.id.button_single);
		Button multiButton = (Button) findViewById(R.id.button_multi);
		
		singleButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(mContext, SingleActivity.class);
				mContext.startActivity(it);
			}
		});
		
		multiButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(mContext, MultiActivity.class);
				mContext.startActivity(it);				
			}
		});
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case android.R.id.home:
			break;
		case 0:

		default:
			return false;
		}

		return true;
	}
    
}
