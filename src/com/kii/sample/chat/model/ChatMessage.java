package com.kii.sample.chat.model;

import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;

/**
 * チャットのメッセージを表します。
 * グループスコープのデータとしてKiiCloudに保存され、チャットに参加しているメンバー（KiiGroupに属しているメンバー）のみが参照することができます。
 * KiiObjectは保存時に_createdというをオブジェクトの作成日時を自動的にJSONフィールドに埋め込みます。
 * この_createdを利用してメッセージの順番を管理します。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatMessage extends KiiObjectWrapper {
	
	private static final String FIELD_GROUP_URI = "group_uri";
	private static final String FIELD_MESSAGE = "message";
	private static final String FIELD_SENDER_URI = "sender_uri";
	private static final String PREFIX_STAMP = "$STAMP:";
	
	/**
	 * 指定されたスタンプを表すChatMessageオブジェクトを作成します。
	 * 
	 * @param kiiGroup
	 * @param stamp
	 * @return
	 */
	public static ChatMessage createStampChatMessage(KiiGroup kiiGroup, ChatStamp stamp) {
		ChatMessage message = new ChatMessage(kiiGroup);
		message.setMessage(PREFIX_STAMP + stamp.getUri());
		message.setSenderUri(KiiUser.getCurrentUser().toUri().toString());
		return message;
	}
	
	public static KiiQuery createQuery() {
		return createQuery(null);
	}
	/**
	 * ChatMessageを検索するためのKiiQueryを生成します。
	 * 
	 * @param 
	 * @return
	 */
	public static KiiQuery createQuery(Long modifiedSinceTime) {
		KiiQuery query = null;
		if (modifiedSinceTime != null) {
			// 最新のメッセージのみを取得するクエリ
			query = new KiiQuery(KiiClause.greaterThan(FIELD_CREATED, modifiedSinceTime));
		} else {
			// 全てのメッセージを取得するクエリ
			query = new KiiQuery();
		}
		query.sortByAsc(FIELD_CREATED);
		return query;
	}
	
	public ChatMessage(KiiGroup kiiGroup) {
		super(ChatRoom.getBucket(kiiGroup).object());
		this.setGroupUri(kiiGroup.toUri().toString());
	}
	public ChatMessage(KiiObject message) {
		super(message);
	}
	/**
	 * このチャットルームのグループのURIを取得します。
	 */
	public String getGroupUri() {
		return getString(FIELD_GROUP_URI);
	}
	public void setGroupUri(String uri) {
		set(FIELD_GROUP_URI, uri);
	}
	/**
	 * メッセージの本文を取得します。
	 */
	public String getMessage() {
		return getString(FIELD_MESSAGE);
	}
	public void setMessage(String message) {
		set(FIELD_MESSAGE, message);
	}
	/**
	 * メッセージの送信者のURIを取得します。
	 */
	public String getSenderUri() {
		return getString(FIELD_SENDER_URI);
	}
	public void setSenderUri(String uri) {
		set(FIELD_SENDER_URI, uri);
	}
	/**
	 * このインスタンスがスタンプを表すChatMessageかどうかを返します。
	 * 
	 * @return
	 */
	public boolean isStamp() {
		if (this.getMessage().startsWith(PREFIX_STAMP)) {
			return true;
		}
		return false;
	}
	/**
	 * このインスタンスがスタンプの場合、スタンプのKiiObjectのURIを返します。
	 * 
	 * @return
	 */
	public String getStampUri() {
		if (!this.isStamp()) {
			return null;
		}
		return this.getMessage().replace(PREFIX_STAMP, "");
	}
}