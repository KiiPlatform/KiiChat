package com.kii.sample.chat.ui.loader;

import java.lang.ref.WeakReference;

import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatStamp;
import com.kii.sample.chat.util.Logger;
import com.kii.sample.chat.util.StampCacheUtils;
import com.kii.sample.chat.util.ThreadUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

/**
 * Fetches images from a KiiCloud.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatStampImageFetcher {
	
	/**
	 * Reduces this value if occurs OutOfMemoryError on your device.
	 */
	private static final int MEM_CACHE_SIZE = 1024 * 1024 * 3; 
	private final Object pauseWorkLock = new Object();
	private boolean isPauseWork = false;
	private final Resources resources;
	private Bitmap loadingBitmap;
	private LruCache<String, Bitmap> cache;
	
	public ChatStampImageFetcher(Context context) {
		this.resources = context.getResources();
		this.cache = new LruCache<String, Bitmap>(MEM_CACHE_SIZE);
	}
	public void setPauseWork(boolean isPause) {
		synchronized (this.pauseWorkLock) {
			this.isPauseWork = isPause;
			if (!this.isPauseWork) {
				this.pauseWorkLock.notifyAll();
			}
		}
	}
	public void setLoadingBitmap(Bitmap loadingBitmap) {
		this.loadingBitmap = loadingBitmap;
	}
	public void setLoadingImage(int resId) {
		this.loadingBitmap = BitmapFactory.decodeResource(this.resources, resId);
	}
	public void fetchStamp(ChatStamp stamp, ImageView imageView) {
		Bitmap bitmap = cache.get(stamp.getUri());
		if (bitmap != null) {
			// When cache is found
			imageView.setImageDrawable(new BitmapDrawable(this.resources, bitmap));
			return;
		} else {
			imageView.setImageBitmap(this.loadingBitmap);
		}
		StampImageLoader loader = (StampImageLoader)imageView.getTag();
		if (loader != null) {
			if (!loader.getUri().equals(stamp.getUri())) {
				loader.cancel(true);
			} else {
				return;
			}
		}
		loader = new StampImageLoader(stamp, imageView);
		imageView.setTag(loader);
		ThreadUtils.executeTaskOnExecutor(loader);
	}
	/**
	 * Downloads stamp image from KiiCloud.
	 */
	private class StampImageLoader extends AsyncTask<Void, Void, Bitmap> {
		
		private final ChatStamp stamp;
		private final WeakReference<ImageView> imageViewReference;
		
		private StampImageLoader(ChatStamp stamp, ImageView imageView) {
			this.stamp = stamp;
			this.imageViewReference = new WeakReference<ImageView>(imageView);
		}
		@Override
		protected Bitmap doInBackground(Void... params) {
			synchronized (pauseWorkLock) {
				while (isPauseWork && !isCancelled()) {
					try {
						pauseWorkLock.wait();
					} catch (InterruptedException ignore) {
					}
				}
			}
			if (isCancelled()) {
				Logger.i("loader task is canceled.");
				return null;
			}
			// Retrieves image from disc cache or KiiCloud.
			Bitmap bitmap = stamp.getImage();
			if (bitmap != null) {
				StampCacheUtils.saveCache(stamp, bitmap);
				cache.put(this.stamp.getUri(), bitmap);
			}
			return bitmap;
		}
		protected void onPostExecute(Bitmap bitmap) {
			ImageView imageView = this.imageViewReference.get();
			if (imageView != null && imageView.getTag() == this && !isCancelled()) {
				if (bitmap != null) {
						imageView.setImageDrawable(new BitmapDrawable(resources, bitmap));
				} else {
					imageView.setImageResource(R.drawable.error);
				}
			}
		}
		@Override
		protected void onCancelled(Bitmap bitmap) {
			super.onCancelled(bitmap);
			synchronized (pauseWorkLock) {
				pauseWorkLock.notifyAll();
			}
		}
		private String getUri() {
			return this.stamp.getUri();
		}
	}
}
