package com.kii.sample.chat.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.analytics.KiiAnalyticsException;
import com.kii.cloud.analytics.KiiEvent;
import com.kii.cloud.analytics.aggregationresult.DateRange;
import com.kii.cloud.analytics.aggregationresult.GroupedResult;
import com.kii.cloud.analytics.aggregationresult.GroupedSnapShot;
import com.kii.cloud.analytics.aggregationresult.ResultQuery;
import com.kii.cloud.analytics.aggregationresult.SimpleDate;
import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.resumabletransfer.KiiDownloader;
import com.kii.cloud.storage.resumabletransfer.KiiUploader;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.KiiChatApplication;
import com.kii.sample.chat.util.Logger;
import com.kii.sample.chat.util.StampCacheUtils;

/**
 * Represents the stamp.
 * User can use all stamps uploaded by anyone.
 * Message is saved as '''$STAMP:{URI of image object}', If user sends stamp.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatStamp extends KiiObjectWrapper {
	
	private static final String BUCKET_NAME = "chat_stamps";
	private static final String EVENT_TYPE = "stamp_usage";
	private static final String EVENT_KEY_STAMP_URI = "stamp_uri";
	
	public static KiiBucket getBucket() {
		return Kii.bucket(BUCKET_NAME);
	}
	
	/**
	 * Gets all stamps from KiiCloud order by newly listed.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<ChatStamp> listOrderByNewly() {
		List<ChatStamp> stamps = new ArrayList<ChatStamp>();
		try {
			KiiQuery query = new KiiQuery();
			query.sortByDesc(FIELD_CREATED);
			List<KiiObject> objects = getBucket().query(query).getResult();
			for (KiiObject object : objects) {
				stamps.add(new ChatStamp(object));
			}
			return stamps;
		} catch (Exception e) {
			Logger.e("Unable to list stamps", e);
			return stamps;
		}
	}
	/**
	 * Gets a Comparator for sorting by newest.
	 * 
	 * @return
	 */
	public static Comparator<ChatStamp> getNewlyComparator() {
		return new Comparator<ChatStamp>() {
			@Override
			public int compare(ChatStamp lhs, ChatStamp rhs) {
				if (lhs.getCreatedTime() > rhs.getCreatedTime()) {
					return -1;
				} else if (lhs.getCreatedTime() > rhs.getCreatedTime()) {
					return 1;
				} else {
					return 0;
				}
			}
		};
	}
	/**
	 * Gets a Comparator for sorting by popularity using result of flex analytics.
	 * 
	 * @return
	 */
	public static Comparator<ChatStamp> getPopularityComparator() {
		try {
			// Gets a usage of stamp over the last a month.
			Calendar cal = Calendar.getInstance();
			SimpleDate end = new SimpleDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
			cal.add(Calendar.MONTH, -1);
			SimpleDate start = new SimpleDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
			DateRange dateRange = new DateRange(start, end);
			ResultQuery query = ResultQuery
					.builderWithGroupingKey(EVENT_KEY_STAMP_URI)	// Group by URI of stamp
					.withDateRange(dateRange)						// Over the last a month
					.build();
			GroupedResult result = KiiAnalytics.getResult(ApplicationConst.AGGREGATION_RULE_ID, query);
			List<GroupedSnapShot> snapshots = result.getSnapShots();
			// Converts result of analytics to the map. key=URI of stamp, value=Number of uses
			final Map<String, Long> stampUsageMap = new HashMap<String, Long>();
			for (int i = 0; i < snapshots.size(); i++) {
				try {
					long usage = 0;
					for (int j = 0; j < snapshots.get(i).getData().length(); j++) {
						usage += snapshots.get(i).getData().getLong(j);
					}
					stampUsageMap.put(snapshots.get(i).getName(), usage);
				} catch (JSONException e) {
					stampUsageMap.put(snapshots.get(i).getName(), 0L);
				}
			}
			// Sorts by number of uses
			return new Comparator<ChatStamp>() {
				@Override
				public int compare(ChatStamp lhs, ChatStamp rhs) {
					long lhsUsage = stampUsageMap.get(lhs.getUri()) == null ? 0L : stampUsageMap.get(lhs.getUri());
					long rhsUsage = stampUsageMap.get(rhs.getUri()) == null ? 0L : stampUsageMap.get(rhs.getUri());
					if (lhsUsage > rhsUsage) {
						return -1;
					} else if (lhsUsage < rhsUsage) {
						return 1;
					} else {
						return 0;
					}
				}
			};
		} catch (KiiAnalyticsException ignore) {
			// Returns NewlyComparator if fails to get the result of analytics.
			Logger.w("failed to get analytics result", ignore);
			return getNewlyComparator();
		}
	}
	/**
	 * Sends the usage of stamp to the KiiCloud.
	 * 
	 * @param message
	 */
	public static void sendUsageEvent(ChatMessage message) {
		if (message.isStamp()) {
			try {
				KiiEvent event = KiiAnalytics.event(EVENT_TYPE);
				event.set(EVENT_KEY_STAMP_URI, message.getStampUri());
				event.push();
			} catch (IOException ignore) {
				Logger.w("failed to send event");
			}
		}
	}
	
	private File imageFile;
	private String uri;
	
	/**
	 * Initializes a new instance from image file.
	 * 
	 * @param imageFile
	 */
	public ChatStamp(File imageFile) {
		super(getBucket().object());
		this.imageFile = imageFile;
	}
	/**
	 * Initializes a new instance from KiiObject instance.
	 * 
	 * @param kiiObject
	 */
	public ChatStamp(KiiObject kiiObject) {
		super(kiiObject);
		this.uri = kiiObject.toUri().toString();
	}
	/**
	 * Initializes a new instance from ChatMessage instance.
	 * 
	 * @param message Must be a ChatMessage representing the stamp.
	 */
	public ChatStamp(ChatMessage message) {
		super(KiiObject.createByUri(Uri.parse(message.getStampUri())));
		this.uri = message.getStampUri();
	}
	/**
	 * Saves a KiiObject and uploads image to the KiiCloud.
	 * 
	 * @throws Exception
	 */
	public void save() throws Exception {
		this.kiiObject.save();
		if (this.imageFile != null) {
			this.uri = this.kiiObject.toUri().toString();
			KiiUploader uploader = this.kiiObject.uploader(KiiChatApplication.getContext(), this.imageFile);
			uploader.transfer(null);
			// Renames uploaded image in order to cache the image.
			File cacheFile = StampCacheUtils.getCacheFile(this.kiiObject.toUri().toString());
			this.imageFile.renameTo(cacheFile);
		}
	}
	/**
	 * Gets URI of this stamp.
	 * 
	 * @return
	 */
	public String getUri() {
		return this.uri;
	}
	/**
	 * Retrieves image from local cache or KiiCloud.
	 * 
	 * @return
	 */
	public Bitmap getImage() {
		try {
			byte[] image = null;
			if (this.imageFile != null) {
				// When adding a new stamp from local file.
				image = readImageFromLocal(this.imageFile);
			} else if (this.uri != null) {
				// Reads a image from local cache.
				File cacheFile = StampCacheUtils.getCacheFile(this.uri);
				if (cacheFile.exists()) {
					image = readImageFromLocal(cacheFile);
				} else {
					// Downloads a image from KiiCloud.
					Logger.i("downloads stamp image from KiiCloud");
					KiiDownloader downloader = this.kiiObject.downloader(KiiChatApplication.getContext(), cacheFile);
					downloader.transfer(null);
					image = readImageFromLocal(cacheFile);
				}
			}
			if (image != null) {
				return BitmapFactory.decodeByteArray(image, 0, image.length);
			}
			Logger.w("failed to download stamp");
			return null;
		} catch (Exception e) {
			Logger.e("failed to download stamp", e);
			return null;
		}
	}
	/**
	 * Read image file from local storage.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private byte[] readImageFromLocal(File file) throws IOException {
		FileInputStream fs = new FileInputStream(file);
		try {
			return IOUtils.toByteArray(fs);
		} finally {
			IOUtils.closeQuietly(fs);
		}
	}
}
