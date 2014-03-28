package com.kii.sample.chat.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.kii.sample.chat.KiiChatApplication;
import com.kii.sample.chat.model.ChatStamp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

/**
 * A suite of utilities surrounding the use of the image of stamp.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class StampCacheUtils {
	/**
	 * Reduce an image, and store in the cache directory.
	 * 
	 * @param source
	 * @param maxSize
	 * @return
	 */
	public static File copyToCache(Uri source, int maxSize) throws IOException {
		InputStream is = KiiChatApplication.getContext().getContentResolver().openInputStream(source);
		FileOutputStream os = null;
		try {
			BitmapFactory.Options imageOptions = new BitmapFactory.Options();
			imageOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(is, null, imageOptions);
			IOUtils.closeQuietly(is);
			
			float imageScaleWidth = (float)imageOptions.outWidth / maxSize;
			float imageScaleHeight = (float)imageOptions.outHeight / maxSize;
			is = KiiChatApplication.getContext().getContentResolver().openInputStream(source);
			
			Bitmap bitmap = null;
			if (imageScaleWidth > 2 && imageScaleHeight > 2) {
				imageOptions = new BitmapFactory.Options();
				imageOptions.inPreferredConfig = Config.ARGB_8888; // support transparent PNG.
				int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleHeight : imageScaleWidth));
				for (int i = 2; i < imageScale; i *= 2) {
					imageOptions.inSampleSize = i;
				}
				bitmap = BitmapFactory.decodeStream(is, null, imageOptions);
			} else {
				bitmap = BitmapFactory.decodeStream(is);
			}
			File out = getCacheFile(escapeUri(source.toString()));
			os = new FileOutputStream(out);
			bitmap.compress(CompressFormat.PNG, 75, os);
			return out;
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);
		}
	}
	/**
	 * Retrieve the image cache from file.
	 * 
	 * @param uri ChatStampのURI
	 * @return
	 */
	public static File getCacheFile(String uri) {
		final File cacheDir = new File(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !isExternalStorageRemovable() ?
				getExternalCacheDir(KiiChatApplication.getContext()).getPath() : KiiChatApplication.getContext().getCacheDir().getPath());
		if (cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return new File(cacheDir + File.separator + escapeUri(uri));
	}
	/**
	 * Save the cache on background thread.
	 * 
	 * @param stamp
	 * @param bitmap
	 */
	public static void saveCache(final ChatStamp stamp, final Bitmap bitmap) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				File f = getCacheFile(stamp.getUri());
				FileOutputStream os = null;
				try {
					os = new FileOutputStream(f);
					bitmap.compress(CompressFormat.PNG, 75, os);
				} catch (IOException ignore) {
					Logger.w("failed to save cache", ignore);
				} finally {
					IOUtils.closeQuietly(os);
				}
			}
		});
	}
	/**
	 * To escape characters that can not be used in the file name from the URI.
	 * 
	 * @param uri
	 * @return
	 */
	private static String escapeUri(String uri) {
		return uri.replace("://", "_").replace("/", "_");
	}
	private static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}
	private static File getExternalCacheDir(Context context) {
		if (hasExternalCacheDir()) {
			return context.getExternalCacheDir();
		}
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}
	private static boolean hasExternalCacheDir() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}
}
