package com.designproject.dreamcoding.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.designproject.dreamcoding.R;
import com.designproject.dreamcoding.R.drawable;
import com.designproject.dreamcoding.listener.PanAndZoomListener;
import com.designproject.dreamcoding.listener.PanAndZoomListener.Anchor;

public class PuzzleActivity extends DefaultActivity {

	private static int RESULT_LOAD_IMAGE = 1;
	Context mContext;
	Button buttonLoadImage;
	FrameLayout imageViewHolder;

	private ImageView mOrgImage;
	private ImageView mFixedImage;
	private Boolean isOrgView = false;
	
	private Button mResultButton;
	private TextView mSize;
	private TextView mLoc;
	
	private List<ImageView> imgPiece = new ArrayList<ImageView>();
	private int[][] imgPieceData = {  //조각의 x좌표, y좌표, 너비, 높이, 각도 

			//{150,150,100,150,30},
	};
	
	private int[] pieceList = {
			R.drawable.hair1,
			R.drawable.hair2,
			R.drawable.eye,
	};

	private MenuItem[] actionbarMenu = new MenuItem[6];

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

		setContentView(R.layout.activity_puzzle);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 

		mContext = this;
		//mActionBar.setDisplayHomeAsUpEnabled(true);

		imageViewHolder = (FrameLayout) findViewById(R.id.image_view_holder);
		mOrgImage = (ImageView) findViewById(R.id.original);
		mFixedImage = (ImageView) findViewById(R.id.fixedImage);
		mResultButton = (Button) findViewById(R.id.getResult);
		mLoc = (TextView) findViewById(R.id.locAccuracy);
		mSize = (TextView) findViewById(R.id.sizeAccuracy);
		
		mLoc.setText("위치 정확도 : 88%");
		mSize.setText("크기 정확도 : 3%");
		mResultButton.setText("정답 제출하기?");

		Drawable dr = getResources().getDrawable(R.drawable.original);
		mOrgImage.setImageDrawable(dr);
		
		dr = getResources().getDrawable(R.drawable.cloth);
		mFixedImage.setImageDrawable(dr);
		
		for(int i = 0; i < pieceList.length; i++){
			ImageView ttemp = new ImageView(this);
			Drawable temp = getResources().getDrawable(pieceList[i]);
			
			ttemp.setImageDrawable(temp);
			ttemp.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));		
			
			ttemp.setScaleType(ScaleType.MATRIX);
			ttemp.setAdjustViewBounds(true);
			ttemp.setOnTouchListener(new PanAndZoomListener(imageViewHolder, ttemp, Anchor.TOPLEFT));
			//imgPiece[0].setOnTouchListener(new testListener());
			imgPiece.add(ttemp);
			imageViewHolder.addView(imgPiece.get(i));
		}
		
//		for(int i = 0; i < imgPieceData.length; i ++){
//			
//			Bitmap temp = crop(R.drawable.mr_brown, imgPieceData[i][0], imgPieceData[i][1], imgPieceData[i][2], imgPieceData[i][3]);
//			ImageView ttemp = new ImageView(this);
//			ttemp.setImageBitmap(temp);
//			ttemp.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
//
			//TODO 매트릭스랑 뷰바운드 충돌일어남... 동시에 작동해주지 않음.
			/*
			 * TODO 
			 * 1.각 조각별 정확도 검증하여 실시간으로 보여주기
			 * 2.원본 이미지 숨김/보여주기 모드 설정
			 * 3.조각 회전 구현
			 * 
			 * 받아야 할 것 :
			 * 1. 작은 그림으로 받기
			 * 2. 각 조각별 정답 좌표
			 * 3. 폰트
			 * 4. 디자인
			 */
//			ttemp.setScaleType(ScaleType.MATRIX);
//			ttemp.setAdjustViewBounds(true);
//			ttemp.setOnTouchListener(new PanAndZoomListener(imageViewHolder, ttemp, Anchor.TOPLEFT));
//			//imgPiece[0].setOnTouchListener(new testListener());
//			imgPiece.add(ttemp);
//			imageViewHolder.addView(imgPiece.get(i));
//			
//		}
		
		mOrgImage.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				KLog("X, Y", event.getX() + ", " + event.getY());
				KLog("left, top",mOrgImage.getX() + ", " + mOrgImage.getY());
				KLog("width, height",mOrgImage.getWidth() + ", " + mOrgImage.getHeight());

				return false;
			}
		});
	}

	@SuppressLint("NewApi")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		//이미지 주소 받아서 추가
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			//기기의 가로세로 픽셀
			final int width = metrics.widthPixels;
			final int height = metrics.heightPixels;

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		actionbarMenu[0] = menu
				.add(0, 0, 0, "MENU")
				.setIcon(R.drawable.puzzle_03);
		actionbarMenu[0].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		//actionbarMenu[4] = menu.addSubMenu(groupId, itemId, order, title)

//		actionbarMenu[1] = menu
//				.add(0, 1, 1, "100%");
//		actionbarMenu[1].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); //크기비례비율
//
//		actionbarMenu[2] = menu
//				.add(0, 2, 2, "100%");
//		actionbarMenu[2].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); //좌표정확도
//
		actionbarMenu[3] = menu
				//TODO 원본보기 버튼 누르면 원본 보기, 떼면 다시 퍼즐로 돌아오기
				.add(0, 3, 3, "원본 보기");
				//.setIcon(R.drawable.puzzle_06);
		actionbarMenu[3].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS );

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case android.R.id.home:
			finish();
			break;
		case 0:
			Toast.makeText(mContext, "MENU!", Toast.LENGTH_SHORT).show();
			break;
		case 1:
			Toast.makeText(mContext, "크기 정확도 : " + actionbarMenu[1].getTitle(), Toast.LENGTH_SHORT).show();
			break;
		case 2:
			Toast.makeText(mContext, "좌표 정확도 : " + actionbarMenu[2].getTitle(), Toast.LENGTH_SHORT).show();
			break;			
		case 3:
			if(isOrgView == true){
				isOrgView = false;
				mOrgImage.setVisibility(View.INVISIBLE);
				
				for(int i = 0; i < pieceList.length; i++){
					imgPiece.get(i).setVisibility(View.VISIBLE);	
				}
				
				actionbarMenu[3].setTitle("원본 보기");
				
			} else {
				isOrgView = true;
				mOrgImage.setVisibility(View.VISIBLE);
				
				for(int i = 0; i < pieceList.length; i++){
					imgPiece.get(i).setVisibility(View.INVISIBLE);	
				}
				
				actionbarMenu[3].setTitle("퍼즐 보기");
			}
			break;
		default:
			return false;
		}

		return true;
	}

	public static void KLog(String tag, String text){
		boolean enable = true;

		if(enable == true){
			Log.d(tag, text);
		}
	}

	public Bitmap crop(int dr, int x, int y, int width, int height){
		Bitmap bmp=BitmapFactory.decodeResource(getResources(), dr);
		Bitmap result = Bitmap.createBitmap(bmp, x, y, width, height); //시작x, 시작y, 너비, 높이

		return result;
	}
}
