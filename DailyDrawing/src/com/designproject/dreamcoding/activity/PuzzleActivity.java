package com.designproject.dreamcoding.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.designproject.dreamcoding.R;
import com.designproject.dreamcoding.listener.PanAndZoomListener;
import com.designproject.dreamcoding.listener.PanAndZoomListener.Anchor;
import com.designproject.dreamcoding.listener.testListener;

public class PuzzleActivity extends DefaultActivity {

	private static int RESULT_LOAD_IMAGE = 1;
	Context mContext;
	ImageView original;
	ImageView piece;
	Button buttonLoadImage;
	FrameLayout imageViewHolder;

	//추가한 이미지에 아이디를 붙이기 위한 변수
	int imageId = -1;

	//이미지 회전을 위한 변수
	private ImageView[] mImageView = new ImageView[10];
	private Button mBtnRotate;
	private Bitmap[] mOrgImage = new Bitmap[10];
	private int currentAngle = 0;
	
	private MenuItem[] actionbarMenu = new MenuItem[4];

	// 한번에 회전해야 되는 각도
	private static final int ROTATE_VALUE = 10;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		
		setContentView(R.layout.activity_puzzle);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 

		mContext = this;
		mActionBar.setDisplayHomeAsUpEnabled(true);

		buttonLoadImage = (Button) findViewById(R.id.button_loadimage);		imageViewHolder = (FrameLayout) findViewById(R.id.image_view_holder);
		original = (ImageView) findViewById(R.id.original);
		mBtnRotate = (Button)findViewById(R.id.btn_rotate);
		
		Drawable dr = getResources().getDrawable(R.drawable.mr_brown);
		original.setImageDrawable(dr);
		
		//조각을 담을 이미지뷰 동적 추가
		mImageView[++imageId] = new ImageView(this);
		mImageView[imageId].setId(imageId);
		mOrgImage[imageId] = crop(R.drawable.mr_brown); //자르고 토막내기
		mImageView[imageId].setImageBitmap(mOrgImage[imageId]);
		mImageView[imageId].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		//TODO 매트릭스랑 뷰바운드 충돌일어남... 동시에 작동해주지 않음.
		mImageView[imageId].setScaleType(ScaleType.MATRIX);
		mImageView[imageId].setAdjustViewBounds(true);
		imageViewHolder.addView(mImageView[imageId]);

		mImageView[imageId].setOnTouchListener(new PanAndZoomListener(imageViewHolder, mImageView[imageId], Anchor.TOPLEFT));
		//mImageView[imageId].setOnTouchListener(new testListener());

		//회전 및 그림 불러오기 기능 숨겨두긔잼
		buttonLoadImage.setVisibility(View.INVISIBLE);
		mBtnRotate.setVisibility(View.INVISIBLE);
		
		buttonLoadImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});

		mBtnRotate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int width = mImageView[imageId].getWidth();
				int height = mImageView[imageId].getHeight();
				currentAngle = ROTATE_VALUE + (currentAngle % 360);
				Bitmap resize = getImageProcess(mOrgImage[imageId], currentAngle, width, height);
				mImageView[imageId].setImageBitmap(resize);
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

			/**TODO
			 * 각각의 이미지뷰에 올바른 이동 모션 주기(현재 : 갱신된 새로운 mImageView로 포커싱이 됨, 그 프레임 밖으로 이미지가 나가지 못함, 이미지가 끝에 다다르면 축소되면서 이동함)
			 */

			//이미지뷰 동적 추가
			mImageView[++imageId] = new ImageView(this);
			mImageView[imageId].setId(imageId);
			mOrgImage[imageId] = BitmapFactory.decodeFile(picturePath);
			mImageView[imageId].setImageBitmap(mOrgImage[imageId]);
			mImageView[imageId].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));

			imageViewHolder.addView(mImageView[imageId]);

			mImageView[imageId].setOnTouchListener(new PanAndZoomListener(imageViewHolder, mImageView[imageId], Anchor.TOPLEFT));
		}
	}
	
	/**
	 * 이미지 관련 처리를 수행한다.
	 * 회전을 시키며, 이미지를 화면에 맞게 줄인다.
	 * @author master
	 * 2011. 3. 31.
	 */
	private Bitmap getImageProcess(Bitmap bmp, int nRotate, int viewW, int viewH){

		Matrix matrix = new Matrix();

		// 이미지의 해상도를 줄인다.
		/*float scaleWidth = ((float) newWidth) / width;
    float scaleHeight = ((float) newHeight) / height;
    matrix.postScale(scaleWidth, scaleHeight);*/

		matrix.postRotate(nRotate); // 회전
		// 이미지를 회전시킨다
		Bitmap rotateBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

		// View 사이즈에 맞게 이미지를 조절한다.
		Bitmap resize = Bitmap.createScaledBitmap(rotateBitmap, viewW, viewH, true);

		//return resize;
		return resize;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		actionbarMenu[0] = menu
				.add(0, 0, 0, "MENU")
				.setIcon(R.drawable.puzzle_03);
		actionbarMenu[0].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		actionbarMenu[1] = menu
				.add(0, 1, 1, "100%")
				.setIcon(R.drawable.puzzle_04);
		actionbarMenu[1].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		actionbarMenu[2] = menu
				.add(0, 2, 2, "00 : 00")
				.setIcon(R.drawable.puzzle_05);
		actionbarMenu[2].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		actionbarMenu[3] = menu
				.add(0, 3, 3, "?")
				.setIcon(R.drawable.puzzle_06);
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
			Toast.makeText(mContext, "타이머입니다!", Toast.LENGTH_SHORT).show();
			break;			
		default:
			return false;
		}
		
		return true;
	}

	public static void KLog(String tag, String text){
		boolean enable = false;

		if(enable == true){
			Log.d(tag, text);
		}
	}
	
	public Bitmap crop(int dr){
		Bitmap bmp=BitmapFactory.decodeResource(getResources(), dr);
		Bitmap result = Bitmap.createBitmap(bmp, 100, 100, 150, 200); //시작x, 시작y, 너비, 높이

		return result;
	}
}
