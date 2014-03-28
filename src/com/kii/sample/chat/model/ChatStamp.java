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
 * チャットのメッセージで使用できるスタンプを表します。
 * ユーザは画像ファイルをアプロードしてスタンプとして使用できます。
 * アプリケーションスコープのデータとしてKiiCloudに保存されるので、他のユーザによってアップロードされたスタンプは誰でも利用することが可能です。
 * メッセージとしてのスタンプは通常のチャットメッセージと同じようにchat_roomバケットに保存されます。
 * その際、'$STAMP:{画像のKiiObjectのURI}'という形式のテキストとして保存します。
 * KiiChatアプリケーションは '$STAMP:' から始まるメッセージを受信した場合、それがスタンプであると判断し画像を表示します。
 * もしユーザが'$STAMP:'から始まるテキストメッセージを送信しようとすると、うまくテキストを送信することはできません。
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
	 * ユーザにアップロードされた全てのスタンプを新着順に取得します。
	 * スタンプ本体の画像イメージは取得されません。
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<ChatStamp> listOrderByNewly() {
		List<ChatStamp> stamps = new ArrayList<ChatStamp>();
		try {
			// 作成日時でソートして、クエリ結果の順序を保証する
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
	 * 新着順でソートする為のComparatorを取得します。
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
	 * 人気順でソートする為のComparatorを取得します。
	 * Analyticsの結果を元に人気順を判定するため、KiiCloudとの通信が発生します。メインスレッドでは実行しないでください。
	 * 
	 * @return
	 */
	public static Comparator<ChatStamp> getPopularityComparator() {
		try {
			// 直近1ヶ月のスタンプの利用データを取得
			Calendar cal = Calendar.getInstance();
			SimpleDate end = new SimpleDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
			cal.add(Calendar.MONTH, -1);
			SimpleDate start = new SimpleDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
			DateRange dateRange = new DateRange(start, end);
			ResultQuery query = ResultQuery
					.builderWithGroupingKey(EVENT_KEY_STAMP_URI)	// スタンプのURI毎にグルーピング
					.withDateRange(dateRange)						// 直近1ヶ月のデータのみ取得
					.build();
			GroupedResult result = KiiAnalytics.getResult(ApplicationConst.AGGREGATION_RULE_ID, query);
			List<GroupedSnapShot> snapshots = result.getSnapShots();
			// Analyticsの結果をMapに格納する、key=スタンプのURI, value=スタンプが使用された回数
			final Map<String, Long> stampUsageMap = new HashMap<String, Long>();
			for (int i = 0; i < snapshots.size(); i++) {
				try {
					// Analyticsの結果は日別(pointInterval)に分割されて配列として取得されるので、合計値を計算する
					long usage = 0;
					for (int j = 0; j < snapshots.get(i).getData().length(); j++) {
						usage += snapshots.get(i).getData().getLong(j);
					}
					stampUsageMap.put(snapshots.get(i).getName(), usage);
				} catch (JSONException e) {
					stampUsageMap.put(snapshots.get(i).getName(), 0L);
				}
			}
			// スタンプの使用回数でソートする
			return new Comparator<ChatStamp>() {
				@Override
				public int compare(ChatStamp lhs, ChatStamp rhs) {
					// 比較対象のスタンプの使用回数を比較する
					long lhsUsage = stampUsageMap.get(lhs.getUri()) == null ? 0L : stampUsageMap.get(lhs.getUri());
					long rhsUsage = stampUsageMap.get(rhs.getUri()) == null ? 0L : stampUsageMap.get(rhs.getUri());
					// 使用回数が多い順（降順）にソートしたいので、通常のComparatorの定義とは逆の戻り値を返す
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
			// Analytics結果の取得に失敗した場合は、デフォルトの新着順のComparatorを返す
			Logger.w("failed to get analytics result", ignore);
			return getNewlyComparator();
		}
	}
	/**
	 * スタンプの利用状況を表すイベントデータを送信します。
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
	 * ローカルファイルからインスタンスを生成します。
	 * このコンストラクタは新規スタンプの追加時に作成されます。
	 * 
	 * @param imageFile
	 */
	public ChatStamp(File imageFile) {
		super(getBucket().object());
		this.imageFile = imageFile;
	}
	/**
	 * スタンプを表すKiiObjectからインスタンスを生成します。
	 * 
	 * @param kiiObject
	 */
	public ChatStamp(KiiObject kiiObject) {
		super(kiiObject);
		this.uri = kiiObject.toUri().toString();
	}
	/**
	 * ChatMessageからインスタンスを生成します。
	 * 渡されるChatMessageはスタンプはスタンプを表すChatMessageである必要があります。(isStamp()がtrueのもの)
	 * 
	 * @param message
	 */
	public ChatStamp(ChatMessage message) {
		super(KiiObject.createByUri(Uri.parse(message.getStampUri())));
		this.uri = message.getStampUri();
	}
	/**
	 * スタンプをKiiCloudに保存し、画像をアップロードします。
	 * 
	 * @throws Exception
	 */
	public void save() throws Exception {
		this.kiiObject.save();
		if (this.imageFile != null) {
			this.uri = this.kiiObject.toUri().toString();
			KiiUploader uploader = this.kiiObject.uploader(KiiChatApplication.getContext(), this.imageFile);
			uploader.transfer(null);
			// アップロードしたファイルを、KiiObjectのURIに応じた名前にリネームする
			File cacheFile = StampCacheUtils.getCacheFile(this.kiiObject.toUri().toString());
			this.imageFile.renameTo(cacheFile);
		}
	}
	/**
	 * このスタンプを表すKiiObjectのURIを取得します。
	 * 
	 * @return
	 */
	public String getUri() {
		return this.uri;
	}
	/**
	 * スタンプの画像を取得します。
	 * ディスクに画像がキャッシュされている場合は、KiiCloudにアクセスすることなく画像を返します。
	 * 画像がキャッシュにない場合、KiiCloudとの通信が発生するので、メインスレッドでは実行しないでください。
	 * 
	 * @return
	 */
	public Bitmap getImage() {
		try {
			byte[] image = null;
			if (this.imageFile != null) {
				// ファイルを指定してChatStampのインスタンスが生成された場合 (新規スタンプの追加時)
				image = readImageFromLocal(this.imageFile);
			} else if (this.uri != null) {
				// イメージがキャッシュされていれば、キャッシュから読み込む
				File cacheFile = StampCacheUtils.getCacheFile(this.uri);
				if (cacheFile.exists()) {
					image = readImageFromLocal(cacheFile);
				} else {
					// キャッシュに存在しない場合は、KiiCloudからダウンロードする
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
	 * ローカルのファイルを読み込みます。
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
