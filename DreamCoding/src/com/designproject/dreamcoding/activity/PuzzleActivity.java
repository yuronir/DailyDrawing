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

	//�߰��� �̹����� ���̵� ���̱� ���� ����
	int imageId = -1;

	//�̹��� �̵��� ���� ����
	int startX[] = new int[10];
	int startY[] = new int[10];
	int imageX[] = new int[10];
	int imageY[] = new int[10];

	//�̹��� ȸ���� ���� ����
	private ImageView[] mImageView = new ImageView[10];
	private Button mBtnRotate;
	private Bitmap[] mOrgImage = new Bitmap[10];
	private int currentAngle = 0;

	// �巡�� ������� ��ġ�� ������� ����
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// �巡�׽� ��ǥ ����
	int posX1=0, posX2=0, posY1=0, posY2=0;

	// ��ġ�� ����ǥ���� �Ÿ� ����
	float oldDist = 1f;
	float newDist = 1f;

	// �ѹ��� ȸ���ؾ� �Ǵ� ����
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

		//�̹��� �ּ� �޾Ƽ� �߰�
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			//����� ���μ��� �ȼ�
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
			 * ������ �̹����信 �ùٸ� �̵� ��� �ֱ�(���� : ���ŵ� ���ο� mImageView�� ��Ŀ���� ��, �� ������ ������ �̹����� ������ ����, �̹����� ���� �ٴٸ��� ��ҵǸ鼭 �̵���)
			 */

			//�̹����� ���� �߰�
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
	 * �̹��� ���� ó���� �����Ѵ�.
	 * ȸ���� ��Ű��, �̹����� ȭ�鿡 �°� ���δ�.
	 * @author master
	 * 2011. 3. 31.
	 */
	private Bitmap getImageProcess(Bitmap bmp, int nRotate, int viewW, int viewH){

		Matrix matrix = new Matrix();

		// �̹����� �ػ󵵸� ���δ�.
		/*float scaleWidth = ((float) newWidth) / width;
    float scaleHeight = ((float) newHeight) / height;
    matrix.postScale(scaleWidth, scaleHeight);*/

		matrix.postRotate(nRotate); // ȸ��
		// �̹����� ȸ����Ų��
		Bitmap rotateBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

		// View ����� �°� �̹����� �����Ѵ�.
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

	//�׽�Ʈ ����
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