package com.kii.sample.chat.model;

import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;

/**
 * Represents the message
 * This data is saved to KiiCloud as group scope data.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatMessage extends KiiObjectWrapper {
	
	private static final String FIELD_GROUP_URI = "group_uri";
	private static final String FIELD_MESSAGE = "message";
	private static final String FIELD_SENDER_URI = "sender_uri";
	private static final String PREFIX_STAMP = "$STAMP:";
	
	/**
	 * Creates new chat message from specified stamp.
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
	 * Creates KiiQuery to search messages.
	 * 
	 * @param 
	 * @return
	 */
	public static KiiQuery createQuery(Long modifiedSinceTime) {
		KiiQuery query = null;
		if (modifiedSinceTime != null) {
			query = new KiiQuery(KiiClause.greaterThan(FIELD_CREATED, modifiedSinceTime));
		} else {
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
	 * Gets URI of group of chat room.
	 */
	public String getGroupUri() {
		return getString(FIELD_GROUP_URI);
	}
	public void setGroupUri(String uri) {
		set(FIELD_GROUP_URI, uri);
	}
	/**
	 * Gets the message as string.
	 */
	public String getMessage() {
		return getString(FIELD_MESSAGE);
	}
	public void setMessage(String message) {
		set(FIELD_MESSAGE, message);
	}
	/**
	 * Gets the sender URI as string.
	 */
	public String getSenderUri() {
		return getString(FIELD_SENDER_URI);
	}
	public void setSenderUri(String uri) {
		set(FIELD_SENDER_URI, uri);
	}
	/**
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