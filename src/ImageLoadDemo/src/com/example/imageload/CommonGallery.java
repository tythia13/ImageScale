package com.example.imageload;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Gallery;

public class CommonGallery extends Gallery {
	public static final int TOUCH_STATUS_NOTOUCH = 0;
	public static final int TOUCH_STATUS_TOUCHING = 1;
	public static final int TOUCH_STATUS_TOUCHFINISHED = 2;

	private boolean mSlipState = true;
	private GestureDetector gestureDetector;
	private CommonGalleryItemView imageView;

	private boolean mIsTouching = false;

	private int mTouchStatus;
	private static int screenWidth, screenHeight;

	/**
	 * @return the mSlipState
	 */
	public boolean isSlipState() {
		return mSlipState;
	}

	/**
	 * @param mSlipState
	 *            the mSlipState to set
	 */
	public void setSlipState(boolean mSlipState) {
		this.mSlipState = mSlipState;
	}

	public CommonGallery(Context context) {
		this(context, null);
	}

	public CommonGallery(Context context, AttributeSet attrs) {
		super(context, attrs); // , android.R.attr.galleryStyle);
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		screenHeight = wm.getDefaultDisplay().getHeight();
		mTouchStatus = TOUCH_STATUS_NOTOUCH;
		setCallbackDuringFling(false);
		gestureDetector = new GestureDetector(new FBGallerySimpleGesture());
		this.setOnTouchListener(new OnTouchListener() {
			float baseValue;
			Point center;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				View view = CommonGallery.this.getSelectedView();
				if (view.getId() == R.id.image_gallery_item) {
					imageView = (CommonGalleryItemView) view
							.findViewById(R.id.tvContent);
					mTouchStatus = TOUCH_STATUS_TOUCHING;
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mIsTouching = true;
						baseValue = 0;
						if (event.getPointerCount() == 2) {
							center = new Point((int) (event.getX(0) + event
									.getX(1)) / 2, (int) (event.getY(0) + event
									.getY(1)) / 2);
							imageView.willZoom();
							return true;
						}

					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
						// Log.e("[[[[[onTouch Zoom]]]]]", "Count:" +
						// event.getPointerCount() + "isZooming:" +
						// imageView.isZooming());
						if (event.getPointerCount() == 2) { // the pinch gesture
															// recognizer
							float x = event.getX(0) - event.getX(1);
							float y = event.getY(0) - event.getY(1);
							float value = (float) Math.sqrt(x * x + y * y);// 计算两点的距离
							if (baseValue == 0) {
								baseValue = value;
								imageView.willZoom();
								// Log.e("------ willZoom ----",
								// "------ willZoom ----");

							} else {
								float scale = value / baseValue;// 当前两点间的距离除以手指落下时两点间的距离就是需要缩放的比例。
								// scale the image
								imageView.zoom(center, scale);
							}
							// intercept TouchEvent
							return true;
						}
						if (imageView.isZooming()) {
							return true;
						}
					}

				}
				return false;
			}

		});
	}

	// public CommonImageGallery(Context context, AttributeSet attrs, int
	// defStyle)
	// {
	// super(context, attrs, defStyle);
	//
	// }

	public int getTouchStatus() {
		return mTouchStatus;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		View view = CommonGallery.this.getSelectedView();
		// Log.e("------On Scroll----", "Point Count:" + e1.getPointerCount() +
		// "  :" + e2.getPointerCount());
		if (view.getId() == R.id.image_gallery_item
				&& e1.getPointerCount() != 2 && e2.getPointerCount() != 2) {
			imageView = (CommonGalleryItemView) view
					.findViewById(R.id.tvContent);
			float v[] = new float[9];
			Matrix m = imageView.getImageMatrix();
			m.getValues(v);
			// 图片实时的上下左右坐标
			float left, right;
			// 图片的实时宽，高
			float width = 0, height = 0;
			width = imageView.getCurrentWidth();
			height = imageView.getCurrentHeight();

			if ((int) width <= screenWidth && (int) height <= screenHeight) // 如果图片当前大小<屏幕大小，直接处理滑屏事件
			{
				return super.onScroll(e1, e2, distanceX, distanceY);
			} else {
				left = v[Matrix.MTRANS_X];
				right = left + width;
				Rect r = new Rect();
				imageView.getGlobalVisibleRect(r);

				if (distanceX > 0)// 向左滑动
				{
					if (r.left > 0) {// 判断当前ImageView是否显示完全
						return super.onScroll(e1, e2, distanceX, distanceY);
					} else if (right < screenWidth) {
						return super.onScroll(e1, e2, distanceX, distanceY);
					} else {
						imageView.translate(-distanceX, -distanceY);
					}
				} else if (distanceX < 0) { // 向右滑动

					if (r.right < screenWidth) {
						return super.onScroll(e1, e2, distanceX, distanceY);
					} else if (left > 0) {
						return super.onScroll(e1, e2, distanceX, distanceY);
					} else {
						imageView.translate(-distanceX, -distanceY);
					}
				}
			}

		} else if (e1.getPointerCount() != 2 && e2.getPointerCount() != 2) {
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		return true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (!mIsTouching) {
			super.onLayout(changed, left, top, right, bottom);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			mIsTouching = false;
			mTouchStatus = TOUCH_STATUS_TOUCHFINISHED;
			// 判断上下边界是否越界
			View view = CommonGallery.this.getSelectedView();
			if (view.getId() == R.id.image_gallery_item) {

				imageView = (CommonGalleryItemView) view
						.findViewById(R.id.tvContent);
				float width = imageView.getCurrentWidth();
				float height = imageView.getCurrentHeight();

				if (imageView.isZooming()) {
					imageView.finishZoom();
					break;
				}

				// adjust the image location after scroll
				if ((int) width <= screenWidth && (int) height <= screenHeight) // 如果图片当前大小<屏幕大小，判断边界
				{
					break;
				} else if ((int) width > screenWidth
						&& (int) height < screenHeight) {
					// if the width of image bigger then the screen width and
					// height of the image smaller the height of screen.
					imageView.center(false, true);
				} else if ((int) width < screenWidth
						&& (int) height > screenHeight) {
					// if the height of image bigger then the screen height and
					// width of the image smaller the width of screen.
					imageView.center(true, false);
				} else {
					imageView.adjustScroll();
				}

			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() == 2) {
				return true;
			} else if (imageView.isZooming()) {
				return true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			mIsTouching = false;
			Log.e("Touch Cancel", "IsTouching = false");
		}
		return super.onTouchEvent(event);

	}

	private class FBGallerySimpleGesture extends SimpleOnGestureListener {
		// double click the image
		public boolean onDoubleTap(MotionEvent e) {
			View view = CommonGallery.this.getSelectedView();
			if (view.getId() == R.id.image_gallery_item) {
				// Log.e("-----onDoubleTap-----", "-----onDoubleTap-----");
				imageView = (CommonGalleryItemView) view
						.findViewById(R.id.tvContent);
				if (imageView.getZoomStatus() == CommonGalleryItemView.ZOOMSTATUS_DOUBLE_CLICK) {
					imageView.zoomToOriginal();
				} else {
					Point center = new Point((int) e.getX(), (int) e.getY());
					imageView.doubleClick(center);
				}
			}
			return true;
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		View view = CommonGallery.this.getSelectedView();
		if (e1.getPointerCount() == 2 || e2.getPointerCount() == 2) {
			return false;
		} else if (view.getId() == R.id.image_gallery_item) {
			// if the image is scaled ignore this fling return false.
			imageView = (CommonGalleryItemView) view
					.findViewById(R.id.tvContent);
			if (imageView.getZoomStatus() != CommonGalleryItemView.ZOOMSTATUS_NO_ZOOM) {
				return false;
			}
		}

		int kEvent;
		if (isScrollingLeft(e1, e2)) {
			// Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			// Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		if (mSlipState)
			onKeyDown(kEvent, null);
		return true;
	}

	// @Override
	// public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
	// float velocityY)
	// {
	// int kEvent;
	// if (isScrollingLeft(e1, e2))
	// {
	// // Check if scrolling left
	// kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
	// }
	// else
	// {
	// // Otherwise scrolling right
	// kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
	// }
	// if (mSlipState) onKeyDown(kEvent, null);
	// return true;
	//
	// }
	//

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}
}
