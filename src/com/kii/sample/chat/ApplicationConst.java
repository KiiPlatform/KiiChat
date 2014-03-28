package com.kii.sample.chat;

/**
 * KiiChatアプリケーション共通で使用する定数を管理します。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public final class ApplicationConst {
	
	// TODO:APP_IDとAPP_KEYはKiiCloudでアプリケーションを作成した際に発行された値に書き換えてください。
	public static final String APP_ID = "f39c2d34";
	public static final String APP_KEY = "2e98ef0bb78a58da92f9ac0709dc99ed";
	// TODO:SENDER_IDはGCMの設定を有効にした際に発行された値に書き換えてください。
	public static final String SENDER_ID = "1012419078893";
	// TODO:FACEBOOK_APP_IDはFacebookDevelopersでアプリケーションを作成した際に発行された値に書き換えてください。
	public static final String FACEBOOK_APP_ID = "699038683441923";
	// TODO:AGGREGATION_RULE_IDはデベロッパーポータルでAggregation Ruleを作成した時に発行されたIDに書き換えてください。
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
	 * チャットが開始されたことを表すアクションです。
	 */
	public static final String ACTION_CHAT_STARTED = "com.kii.sample.chat.ACTION_CHAT_STARTED";
	/**
	 * メッセージを受信したことを表すアクションです。
	 */
	public static final String ACTION_MESSAGE_RECEIVED = "com.kii.sample.chat.ACTION_MESSAGE_RECEIVED";
	/**
	 * BroadcastReceiverが受信したPush通知をActivityに送る時に使用するキーです。
	 */
	public static final String EXTRA_MESSAGE = "com.kii.sample.chat.EXTRA_MESSAGE";
	
	private ApplicationConst() {
	}
}
