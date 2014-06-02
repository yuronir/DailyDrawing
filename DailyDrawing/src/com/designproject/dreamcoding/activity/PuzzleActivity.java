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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.designproject.dreamcoding.R;
import com.designproject.dreamcoding.listener.PanAndZoomListener;
import com.designproject.dreamcoding.listener.PanAndZoomListener.Anchor;

public class PuzzleActivity extends DefaultActivity {

	private static int RESULT_LOAD_IMAGE = 1;
	Context mContext;
	ImageView original;
	ImageView piece;
	Button buttonLoadImage;
	FrameLayout imageViewHolder;

	//�߰��� �̹����� ���̵� ���̱� ���� ����
	int imageId = -1;

	//�̹��� ȸ���� ���� ����
	private ImageView[] mImageView = new ImageView[10];
	private Button mBtnRotate;
	private Bitmap[] mOrgImage = new Bitmap[10];
	private int currentAngle = 0;
	
	private MenuItem[] actionbarMenu = new MenuItem[3];

	// �ѹ��� ȸ���ؾ� �Ǵ� ����
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
		
		//������ ���� �̹����� ���� �߰�
		mImageView[++imageId] = new ImageView(this);
		mImageView[imageId].setId(imageId);
		mOrgImage[imageId] = crop(R.drawable.mr_brown); //�ڸ��� �丷����
		mImageView[imageId].setImageBitmap(mOrgImage[imageId]);
		mImageView[imageId].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));

		imageViewHolder.addView(mImageView[imageId]);

		mImageView[imageId].setOnTouchListener(new PanAndZoomListener(imageViewHolder, mImageView[imageId], Anchor.TOPLEFT));

		//ȸ�� �� �׸� �ҷ����� ��� ���ܵα���
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

		//�̹��� �ּ� �޾Ƽ� �߰�
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			DisplayMetrics metrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(metrics);

			//����� ���μ��� �ȼ�
			final int width = metrics.widthPixels;
			final int height = metrics.heightPixels;

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

			mImageView[imageId].setOnTouchListener(new PanAndZoomListener(imageViewHolder, mImageView[imageId], Anchor.TOPLEFT));
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
		actionbarMenu[0] = menu
				.add(0, 0, 0, "MENU")
				;
		actionbarMenu[0].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		actionbarMenu[1] = menu
				.add(0, 1, 1, "00 : 00");
		actionbarMenu[1].setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
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
			Toast.makeText(mContext, "Ÿ�̸��Դϴ�!", Toast.LENGTH_SHORT).show();
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
		Bitmap result = Bitmap.createBitmap(bmp, 100, 100, 150, 200); //����x, ����y, �ʺ�, ����

		return result;
	}
}