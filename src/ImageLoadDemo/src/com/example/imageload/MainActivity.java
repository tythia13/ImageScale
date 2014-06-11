package com.example.imageload;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static String IMAGE_SELECT_ITEM = "com.example.imageload.activity.select.item.position";
	public static String CREATE_TIME = "com.example.imageload.activity.create.time";
	public static String CREATOR_NAME = "com.example.imageload.activity.creator.name";
	// public static String CREATOR_NAMES =
	// "com.example.imageload.activity.create.names";
	public static String IMAGE_NAMES = "com.example.imageload.activity.image.names";
	public static String IMAGE_URLS = "com.example.imageload.activity.image.urls";
	public static String CREATOR_IMAGE_URL = "com.example.imageload.activity.image.CREATOR_IMAGE_URL";

	public static final int FROM_SPACE = 0x001;
	public static final int FROM_ALBUM = 0x002;

	private RelativeLayout mRelativeLayoutTitlebar;
	private CommonGallery mGalleryAlbum;
	private ImageButton mButtonBack;
	private TextView mTextViewImageCount, mTextViewTime, mTextViewUserName;
	private LinearLayout mLinearLayoutBottombar;
	private ProgressBar mProgressBar;

	private int mClickCount = 0;

	private int mSelectedItem = 0;
	private int mLoadingItem = -1;
	private int mCurrntItem = 0;
	private String mCreatorName = null;
	private String mCreateTime = null;
	private List<String> mCreatorNames = null;
	private List<String> mImagePath = null;
	private List<String> mImageUrl = null;

	private int mAlbumSize;
	private int[] mIsDown;

	private final int HANDLE_MESSAGE_WHAT_TITLE_BAR = 0x001;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		initView();
		initData();
	}

	private void initView() {
		mRelativeLayoutTitlebar = (RelativeLayout) findViewById(R.id.image_gallery_activity_title_bar);
		mRelativeLayoutTitlebar.setVisibility(View.GONE);
		mLinearLayoutBottombar = (LinearLayout) findViewById(R.id.image_gallery_activity_linearlayout_info);
		mLinearLayoutBottombar.setVisibility(View.GONE);
		mGalleryAlbum = (CommonGallery) findViewById(R.id.iamge_gallery_activity_gallery);
		mButtonBack = (ImageButton) findViewById(R.id.image_gallery_activity_title_bar_left_button);
		mTextViewImageCount = (TextView) findViewById(R.id.image_gallery_activity_title_bar_title);
		mTextViewTime = (TextView) findViewById(R.id.image_gallery_activity_textview_time);
		mTextViewUserName = (TextView) findViewById(R.id.image_gallery_activity_textview_name);
		mProgressBar = (ProgressBar) findViewById(R.id.image_gallery_activity_progressbar);
		mProgressBar.setProgress(0);

		mButtonBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mRelativeLayoutTitlebar.setVisibility(View.VISIBLE);
		mLinearLayoutBottombar.setVisibility(View.VISIBLE);
		missTitleBar();
	}

	private void initData() {
		Intent intent = getIntent();
		mSelectedItem = intent.getIntExtra(IMAGE_SELECT_ITEM, -1);
		String imageNames = getIntent().getStringExtra(IMAGE_NAMES);
		String imageUrls = getIntent().getStringExtra(IMAGE_URLS);
		mCreatorName = getIntent().getStringExtra(CREATOR_NAME);
		mCreateTime = getIntent().getStringExtra(CREATE_TIME);
		if (mCreatorName != null) {
			mTextViewUserName.setText("xxx upload");
		}
		if (mCreateTime != null) {
			mTextViewTime.setText(mCreateTime);
		} 
		mCurrntItem = mSelectedItem;
		List<String> imagePathList = new ArrayList<String>();
		imagePathList.add("1");
		imagePathList.add("2");
		imagePathList.add("3");
		List<String> imageUrlList = new ArrayList<String>();
		imageUrlList.add("url1");
		imageUrlList.add("url2");
		imageUrlList.add("url3");
		mAlbumSize = imageUrlList.size();
		mIsDown = new int[mAlbumSize];
		for (int i = 0; i < mAlbumSize; i++) {
			mIsDown[i] = 0;
		}

		ImageAdapter adapter = new ImageAdapter(this, imagePathList,
				imageUrlList, mIsDown, mGalleryAlbum);
		mGalleryAlbum.setAdapter(adapter);
		if (mAlbumSize == 1)
			mGalleryAlbum.setSlipState(false);
		setPageCount(mSelectedItem, mAlbumSize);
		mGalleryAlbum.setSelection(mSelectedItem - 1);

		mGalleryAlbum
				.setOnItemSelectedListener(new Gallery.OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> groupView,
							View selectedView, int position, long arg3) {
						mCurrntItem = position;
						setPageCount(position + 1, mAlbumSize);
						if (position == mLoadingItem) {
							mProgressBar.setVisibility(View.VISIBLE);
						} else {
							mProgressBar.setVisibility(View.INVISIBLE);
						}
						didFinishMovement(selectedView);
					}

					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

	}

	private void setPageCount(int currentPage, int pageCount) {
		String current = String.valueOf(currentPage);
		String count = String.valueOf(pageCount);
		SpannableStringBuilder multiWord = new SpannableStringBuilder(current
				+ "/" + count);
		mTextViewImageCount.setText(multiWord);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (mRelativeLayoutTitlebar.isShown() == false) {
			titlebarStartShowAnimation();
			bottombarStartShowAnimation();
		}
		// when downloading image, it needn't hide the bar
		if (mLoadingItem < 0) {
			missTitleBar();
		}
		return super.dispatchTouchEvent(ev);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLE_MESSAGE_WHAT_TITLE_BAR:
				mClickCount--;
				// when downloading image, it needn't hide the bar
				if (mClickCount == 0 && mLoadingItem < 0) {
					titleBarStartMissAnimation();
					bottombarStartMissAnimation();
				}
				break;
			}
		}
	};

	private void titlebarStartShowAnimation() {
		TranslateAnimation animation_translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
				0);
		animation_translate.setRepeatCount(0);
		animation_translate.setDuration(200);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(animation_translate);
		animationSet.setFillAfter(false);
		mRelativeLayoutTitlebar.startAnimation(animationSet);
		animation_translate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mRelativeLayoutTitlebar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});
	}

	private void titleBarStartMissAnimation() {
		TranslateAnimation animation_translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
				-1.0f);
		animation_translate.setRepeatCount(0);
		animation_translate.setDuration(200);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(animation_translate);
		animationSet.setFillAfter(false);
		mRelativeLayoutTitlebar.startAnimation(animationSet);
		animation_translate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mRelativeLayoutTitlebar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
	}

	private void bottombarStartShowAnimation() {
		TranslateAnimation animation_translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0);
		animation_translate.setRepeatCount(0);
		animation_translate.setDuration(200);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(animation_translate);
		animationSet.setFillAfter(false);
		mLinearLayoutBottombar.startAnimation(animationSet);
		animation_translate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mLinearLayoutBottombar.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
	}

	private void bottombarStartMissAnimation() {
		TranslateAnimation animation_translate = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1.0f);
		animation_translate.setRepeatCount(0);
		animation_translate.setDuration(200);
		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(animation_translate);
		animationSet.setFillAfter(false);
		mLinearLayoutBottombar.startAnimation(animationSet);
		animation_translate.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mLinearLayoutBottombar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});
	}

	private void missTitleBar() {
		mClickCount++;
		handler.sendEmptyMessageDelayed(HANDLE_MESSAGE_WHAT_TITLE_BAR, 5000);
	}

	private void didFinishMovement(View view) {
		CommonGalleryItemView imageView = (CommonGalleryItemView) view
				.findViewById(R.id.tvContent);

		if (imageView != null) {
			imageView.setImageDrawableDuringScroll(true, null);
		}
	}

	private boolean isEmpty(String str) {
		return str == null && str == "";
	}
}
