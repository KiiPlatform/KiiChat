package com.kii.sample.chat.model;

/**
 * ChatUserとChatFriendを共通のAdapterで処理する為の、インターフェースです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public interface IUser {
	public String getUsername();
	public String getEmail();
	public String getUri();
}
