package com.designproject.dreamcoding.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.designproject.dreamcoding.R;
import com.designproject.dreamcoding.listener.PanAndZoomListener;
import com.designproject.dreamcoding.listener.PanAndZoomListener.Anchor;

public class PuzzleActivity extends DefaultActivity {

	private static int RESULT_LOAD_IMAGE = 1;
	Context mContext;

	//추가한 이미지에 아이디를 붙이기 위한 변수
	int imageId = -1;

	//이미지 이동을 위한 변수
	int startX[] = new int[10];
	int startY[] = new int[10];
	int imageX[] = new int[10];
	int imageY[] = new int[10];

	//이미지 회전을 위한 변수
	private ImageView[] mImageView = new ImageView[10];
	private Button mBtnRotate;
	private Bitmap[] mOrgImage = new Bitmap[10];
	private int currentAngle = 0;

	// 드래그 모드인지 핀치줌 모드인지 구분
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// 드래그시 좌표 저장
	int posX1=0, posX2=0, posY1=0, posY2=0;

	// 핀치시 두좌표간의 거리 저장
	float oldDist = 1f;
	float newDist = 1f;

	// 한번에 회전해야 되는 각도
	private static final int ROTATE_VALUE = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_puzzle);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 

		mContext = this;

		Button buttonLoadImage = (Button) findViewById(R.id.button_loadimage);
		buttonLoadImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent i = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});

		mBtnRotate = (Button)findViewById(R.id.btn_rotate);
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

			FrameLayout imageViewHolder = (FrameLayout) findViewById(R.id.image_view_holder);

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

			imageX[imageId] = (int) mImageView[imageId].getX();
			imageY[imageId] = (int) mImageView[imageId].getY();
			
			mImageView[imageId].setOnTouchListener(new PanAndZoomListener(imageViewHolder, mImageView[imageId], Anchor.CENTER));
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
		getMenuInflater().inflate(R.menu.puzzle, menu);
		return true;
	}

	//테스트 영역
	// Given an action int, returns a string description
	public static String actionToString(int action) {
		switch (action) {

		case MotionEvent.ACTION_DOWN: return "Down";
		case MotionEvent.ACTION_MOVE: return "Move";
		case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
		case MotionEvent.ACTION_UP: return "Up";
		case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
		case MotionEvent.ACTION_OUTSIDE: return "Outside";
		case MotionEvent.ACTION_CANCEL: return "Cancel";
		}
		return "";
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	public static void KLog(String tag, String text){
		boolean enable = false;

		if(enable == true){
			Log.d(tag, text);
		}
	}
}
