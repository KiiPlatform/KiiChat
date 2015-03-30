package com.kii.sample.chat.model;

/**
 * Represents a user who is ChatUser or ChatFriend.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public interface IUser {
	public String getUsername();
	public String getEmail();
	public String getUri();
}
