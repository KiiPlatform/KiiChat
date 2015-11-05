package com.kii.sample.chat;

import com.kii.cloud.storage.Kii;

/**
 * Define application constant.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public final class ApplicationConst {
	
	// TODO:Needs to overwrite APP_ID and APP_KEY. Refer to http://documentation.kii.com/en/starts/android/
	public static final String APP_ID = "f39c2d34";
	public static final String APP_KEY = "2e98ef0bb78a58da92f9ac0709dc99ed";
	public static final Kii.Site APP_SITE = Kii.Site.JP;
	// TODO:Needs to overwrite SENDER_ID. Refer to http://developer.android.com/google/gcm/gcm.html#senderid
	public static final String SENDER_ID = "1012419078893";
	// TODO:Needs to overwrite AGGREGATION_RULE_ID. Refer to http://documentation.kii.com/en/guides/android/managing-analytics/flex-analytics/analyze-event-data/
	public static final String AGGREGATION_RULE_ID = "87";
	
	/**
	 * 全てのチャットユーザが自分用に保持しているTOPICの名前です。
	 * このトピックはユーザが個別に持つメールボックスに似ています。
	 * 他のユーザがこのトピックにメッセージを送信すると、ユーザにプッシュ通知が送信されます。
	 * 具体的には、チャットを開始した時に、チャットが開始されたことを相手に伝える為に使用します。
	 * この通知を受けた相手は、チャット用に作成されたグループスコープのchat_roomバケットを購読して監視するようにします。
	 * 
	 * @see http://documentation.kii.com/ja/guides/android/managing-push-notification/push-to-user/
	 */
	public static final String TOPIC_INVITE_NOTIFICATION = "invite_notify";
	/**
	 * 
	 * Represents the action indicating that chat is started.
	 */
	public static final String ACTION_CHAT_STARTED = "com.kii.sample.chat.ACTION_CHAT_STARTED";
	/**
	 * Represents the action indicating that push message is received.
	 */
	public static final String ACTION_MESSAGE_RECEIVED = "com.kii.sample.chat.ACTION_MESSAGE_RECEIVED";
	/**
	 * The name of the extra data.
	 */
	public static final String EXTRA_MESSAGE = "com.kii.sample.chat.EXTRA_MESSAGE";
	
	private ApplicationConst() {
	}
}
