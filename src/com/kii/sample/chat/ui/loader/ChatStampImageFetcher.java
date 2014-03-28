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
 * ImageViewに表示するスタンプ画像をバックグラウンドで取得します。
 * StampImageLoaderがImageViewのTagにこのインスタンスを関連づけて無駄なダウロード処理を抑制します。
 * 画像ファイルの読み込みにも多少の時間がかかるため、メモリにもBitmapのキャッシュを保持します。
 * メモリ > ディスク > KiiCloud の順に画像ファイルを探します。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatStampImageFetcher {
	
	/**
	 * 3MBほどメモリキャッシュに利用します。
	 * もしOutOfMemoryErrorが発生するようでしたら、値を小さくしてください。
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
			// メモリにキャッシュされている場合は、BitmapをImageViewに設定して終了する。
			imageView.setImageDrawable(new BitmapDrawable(this.resources, bitmap));
			return;
		} else {
			// メモリにキャッシュされていない場合は、取得に時間がかかる可能性があるので'読み込み中'のアイコンを表示する
			imageView.setImageBitmap(this.loadingBitmap);
		}
		StampImageLoader loader = (StampImageLoader)imageView.getTag();
		if (loader != null) {
			if (!loader.getUri().equals(stamp.getUri())) {
				// 画面に表示されなくなったImageViewに紐づくタスクをキャンセルする
				loader.cancel(true);
			} else {
				// 既に同一IDのタスクが存在する場合は何もしない
				return;
			}
		}
		// 画像をロードするタスクを作成してImageViewに関連付けて実行する
		loader = new StampImageLoader(stamp, imageView);
		imageView.setTag(loader);
		ThreadUtils.executeTaskOnExecutor(loader);
	}
	/**
	 * KiiCloudからスタンプ画像本体をダウンロードします。
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
			// Pause中はスレッドを停止する
			synchronized (pauseWorkLock) {
				while (isPauseWork && !isCancelled()) {
					try {
						pauseWorkLock.wait();
					} catch (InterruptedException ignore) {
					}
				}
			}
			if (isCancelled()) {
				// タスクがキャンセルされていたら何もしない
				Logger.i("loader task is canceled.");
				return null;
			}
			// ディスクまたはKiiCloudから画像を取得する
			Bitmap bitmap = stamp.getImage();
			if (bitmap != null) {
				// 画像をメモリキャッシュとディスクキャッシュの両方に書き込む
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
