package com.designproject.dreamcoding.listener;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class PanAndZoomListener implements OnTouchListener {

	public static class Anchor {

		public static final int CENTER = 0;
		public static final int TOPLEFT = 1;
	}
	private static final String TAG = "PanAndZoomListener";
	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	static final int ROTATION = 3;
	int mode = NONE;
	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	PointF imageCenter = new PointF();
	float oldDist = 1f;
	PanZoomCalculator panZoomCalculator;

	//이미지 이동을 위한 변수
	int startX;
	int startY;
	int imageX;
	int imageY;

	public PanAndZoomListener(FrameLayout containter, View view, int anchor) {
		panZoomCalculator = new PanZoomCalculator(containter, view, anchor);
	}

	@SuppressLint("NewApi")
	public boolean onTouch(View v, MotionEvent event) {

		//싱글터치시
		if(event.getPointerCount()  == 1){
			if(mode != DRAG)
			{
				Log.d(TAG, "mode=DRAG");
				mode = DRAG;
			}
			
			switch ( event.getAction()) {

			case MotionEvent.ACTION_DOWN:
				startX = (int) event.getRawX();
				startY = (int) event.getRawY();
				break;

			case MotionEvent.ACTION_MOVE:

				int x = (int) event.getRawX();
				int y = (int) event.getRawY();

				FrameLayout.LayoutParams mParams = (FrameLayout.LayoutParams) panZoomCalculator.child.getLayoutParams();

				mParams.leftMargin = Math.max(imageX - (startX -  x), 0);
				mParams.topMargin = Math.max(imageY - (startY -  y), 0);
				//Math.min(Math.max(0, imageCenter.x - lp.width/2), window.getWidth() - lp.width);
				panZoomCalculator.child.setLayoutParams(mParams);

				break;

			case MotionEvent.ACTION_UP:
				imageX = (int) panZoomCalculator.child.getX();
				imageY = (int) panZoomCalculator.child.getY();
				break;

			default:
				break;
			}
		} else if (event.getPointerCount() == 2) { // 두 군데 - 크기조절
			if(mode != ZOOM){
				panZoomCalculator.getImageCenter(); //줌을 할 시에 이미지 중심을 유지하기 위해
				oldDist = spacing(event);
				Log.d(TAG, "oldDist=" + oldDist);
				if (oldDist > 10f) {
					midPoint(mid, event);
					mode = ZOOM;
					Log.d(TAG, "mode=ZOOM");
				}
			}
			
			float newDist = spacing(event);
			Log.d(TAG, "newDist=" + newDist);
			if (newDist > 10f) {
				float scale = newDist / oldDist;
				//float scale = 0.5f + newDist / 2*oldDist; //감도를 절반으로
				oldDist = newDist;
				panZoomCalculator.doZoom(scale, mid);
			}

		} else if (event.getPointerCount() == 3) { // 세 군데 - 회전
			if(mode != ROTATION){
				Log.d(TAG, "mode=ROTATION");
			}
		} else{
			if(mode != NONE){
				Log.d(TAG, "mode=NONE");
				mode = NONE;
			}
		}

		// Handle touch events here...
		//		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		//		case MotionEvent.ACTION_DOWN:
		//			start.set(event.getX(), event.getY());
		//			Log.d(TAG, "mode=DRAG");
		//			mode = DRAG;
		//			break;
		//		case MotionEvent.ACTION_POINTER_DOWN:
		//			oldDist = spacing(event);
		//			Log.d(TAG, "oldDist=" + oldDist);
		//			if (oldDist > 10f) {
		//				midPoint(mid, event);
		//				mode = ZOOM;
		//				Log.d(TAG, "mode=ZOOM");
		//			}
		//			break;
		//		case MotionEvent.ACTION_UP:
		//		case MotionEvent.ACTION_POINTER_UP:
		//			mode = NONE;
		//			Log.d(TAG, "mode=NONE");
		//			break;
		//		case MotionEvent.ACTION_MOVE:
		//			if (mode == DRAG) {
		//				panZoomCalculator.doPan(event.getX() - start.x, event.getY() - start.y);
		//				start.set(event.getX(), event.getY());
		//			} else if (mode == ZOOM) {
		//				float newDist = spacing(event);
		//				Log.d(TAG, "newDist=" + newDist);
		//				if (newDist > 10f) {
		//					float scale = newDist / oldDist;
		//					oldDist = newDist;
		//					panZoomCalculator.doZoom(scale, mid);
		//				}
		//			}
		//			break;
		//		}
		return true; // indicate event was handled
	}

	// Determine the space between the first two fingers 
	private float spacing(MotionEvent event) {

		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	// Calculate the mid point of the first two fingers
	private void midPoint(PointF point, MotionEvent event) {
		// ...
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	class PanZoomCalculator {

		/// The current pan position
		PointF currentPan;
		/// The current zoom position
		float currentZoom;
		
		//
		final float minimumZoom = 0.5f;
		final float maximumZoom = 1.5f;
		
		/// The windows dimensions that we are zooming/panning in
		View window;
		View child;
		Matrix matrix;
		// Pan jitter is a workaround to get the video view to update it's layout properly when zoom is changed
		int panJitter = 0;
		int anchor;

		@SuppressLint("NewApi")
		PanZoomCalculator(View container, View child, int anchor) {
			// Initialize class fields
			currentPan = new PointF(0, 0);
			currentZoom = 1f;
			this.window = container;
			this.child = child;
			matrix = new Matrix();
			this.anchor = anchor;
			onPanZoomChanged();
			this.child.addOnLayoutChangeListener(new OnLayoutChangeListener() {
				// This catches when the image bitmap changes, for some reason it doesn't recurse

				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					onPanZoomChanged();
				}
			});
		}

		public void doZoom(float scale, PointF zoomCenter) {

			float oldZoom = currentZoom;

			// multiply in the zoom change
			currentZoom *= scale;

			// this limits the zoom
			currentZoom = Math.max(minimumZoom, currentZoom);
			currentZoom = Math.min(maximumZoom, currentZoom);

			// Adjust the pan accordingly
			// Need to make it such that the point under the zoomCenter remains under the zoom center after the zoom

			// calculate in fractions of the image so:

			float width = window.getWidth();
			float height = window.getHeight();
			float oldScaledWidth = width * oldZoom;
			float oldScaledHeight = height * oldZoom;
			float newScaledWidth = width * currentZoom;
			float newScaledHeight = height * currentZoom;

			if (anchor == Anchor.CENTER) {

				float reqXPos = ((oldScaledWidth - width) * 0.5f + zoomCenter.x - currentPan.x) / oldScaledWidth;
				float reqYPos = ((oldScaledHeight - height) * 0.5f + zoomCenter.y - currentPan.y) / oldScaledHeight;
				float actualXPos = ((newScaledWidth - width) * 0.5f + zoomCenter.x - currentPan.x) / newScaledWidth;
				float actualYPos = ((newScaledHeight - height) * 0.5f + zoomCenter.y - currentPan.y) / newScaledHeight;

				currentPan.x += (actualXPos - reqXPos) * newScaledWidth;
				currentPan.y += (actualYPos - reqYPos) * newScaledHeight;
			} else {
				// assuming top left
				float reqXPos = (zoomCenter.x - currentPan.x) / oldScaledWidth;
				float reqYPos = (zoomCenter.y - currentPan.y) / oldScaledHeight;
				float actualXPos = (zoomCenter.x - currentPan.x) / newScaledWidth;
				float actualYPos = (zoomCenter.y - currentPan.y) / newScaledHeight;
				currentPan.x += (actualXPos - reqXPos) * newScaledWidth;
				currentPan.y += (actualYPos - reqYPos) * newScaledHeight;
			}

			onPanZoomChanged();
		}

		public void doPan(float panX, float panY) {
			currentPan.x += panX;
			currentPan.y += panY;
			onPanZoomChanged();
		}

		/// Call this to reset the Pan/Zoom state machine
		public void reset() {
			// Reset zoom and pan
			currentZoom = 1f;
			currentPan = new PointF(0f, 0f);
			onPanZoomChanged();
		}

		public void onPanZoomChanged() {

			// Things to try: use a scroll view and set the pan from the scrollview
			// when panning, and set the pan of the scroll view when zooming

			float winWidth = window.getWidth();
			float winHeight = window.getHeight();

			if (currentZoom <= 1f) {
				currentPan.x = 0;
				currentPan.y = 0;
			} else if (anchor == Anchor.CENTER) {

				float maxPanX = (currentZoom - 1f) * window.getWidth() * 0.5f;
				float maxPanY = (currentZoom - 1f) * window.getHeight() * 0.5f;
				currentPan.x = Math.max(-maxPanX, Math.min(maxPanX, currentPan.x));
				currentPan.y = Math.max(-maxPanY, Math.min(maxPanY, currentPan.y));
			} else {
				// assume top left

				float maxPanX = (currentZoom - 1f) * window.getWidth();
				float maxPanY = (currentZoom - 1f) * window.getHeight();
				currentPan.x = Math.max(-maxPanX, Math.min(0, currentPan.x));
				currentPan.y = Math.max(-maxPanY, Math.min(0, currentPan.y));
			}

			if (child instanceof ImageView && ((ImageView) child).getScaleType()== ImageView.ScaleType.MATRIX) {
				ImageView view = (ImageView) child;
				Drawable drawable = view.getDrawable();
				if (drawable != null) {
					Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
					if (bm != null) {
						// Limit Pan

						float bmWidth = bm.getWidth();
						float bmHeight = bm.getHeight();

						float fitToWindow = Math.min(winWidth / bmWidth, winHeight / bmHeight);
						float xOffset = (winWidth - bmWidth * fitToWindow) * 0.5f * currentZoom;
						float yOffset = (winHeight - bmHeight * fitToWindow) * 0.5f * currentZoom;

						matrix.reset();
						matrix.postScale(currentZoom * fitToWindow, currentZoom * fitToWindow);
						matrix.postTranslate(currentPan.x + xOffset, currentPan.y + yOffset);
						((ImageView) child).setImageMatrix(matrix);
					}
				}
			} else {
				MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

				//리사이징 시 센터로 보내는 부분 주석처리
//				lp.leftMargin = (int) currentPan.x + panJitter;
//				lp.topMargin = (int) currentPan.y;
				
				//화면 밖으로 나가지 않게
				lp.width = (int) (window.getWidth() * currentZoom);
				lp.height = (int) (window.getHeight() * currentZoom);
				lp.leftMargin = (int) Math.min(Math.max(0, imageCenter.x - lp.width/2), window.getWidth() - lp.width);
				lp.topMargin = (int) Math.min(Math.max(0, imageCenter.y - lp.height/2), window.getHeight() - lp.height);
				panJitter ^= 1;

				child.setLayoutParams(lp);
			}
		}
		
		@SuppressLint("NewApi")
		public void getImageCenter(){
			float x  = child.getX() + child.getWidth()/2;
			float y = child.getY() + child.getHeight()/2;
			imageCenter.set(x, y);
		}
	}
}

