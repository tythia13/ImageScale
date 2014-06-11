package com.example.imageload;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ImageAdapter extends ArrayAdapter<String> {
	private int itemCount;
	private Context mContext;
	private int[] mIsLoad;
	private List<String> mImageUrl;
	private HashMap<String, SoftReference<Drawable>> mImageCache;
	private int mDownloadItem = -1;
	private List<Thread> mRunningThread;
	private List<Thread> mWaitingThread;
	private HashMap<String, String> mLoadingImage;

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public int getCount() {
		return itemCount;
	}

	public ImageAdapter(Context context, List<String> imagePath,
			List<String> imageUrl, int[] isLoad, CommonGallery gallery) {
		super(context, 0, imagePath);
		mImageCache = new HashMap<String, SoftReference<Drawable>>();
		mImageUrl = imageUrl;
		mContext = context;
		itemCount = imagePath.size();
		mIsLoad = isLoad;
		mLoadingImage = new HashMap<String, String>();
		mRunningThread = new ArrayList<Thread>();
		mWaitingThread = new ArrayList<Thread>();
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();
		CommonGalleryItemView iview = null;

		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			convertView = inflater.inflate(
					R.layout.layout_image_gallery_view_item, null);
		}
		iview = (CommonGalleryItemView) convertView
				.findViewById(R.id.tvContent);
		iview.setTag(mImageUrl.get(position));
		iview.setImageResource(R.drawable.img_dashboard_default);

		return convertView;
	}

	public Drawable getImage(final String imageUrl) {
		Drawable drawable = null;
		if (mImageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = mImageCache.get(imageUrl);
			if (softReference != null) {
				drawable = softReference.get();
			}
			if (drawable != null) {
				return drawable;
			}
		}
		return null;
	}

};
