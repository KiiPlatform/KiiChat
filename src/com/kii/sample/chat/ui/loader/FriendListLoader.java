package com.kii.sample.chat.ui.loader;

import java.util.List;

import android.content.Context;

import com.kii.sample.chat.model.ChatFriend;

/**
 * {@link ChatFriend}をchat_friendsバケットから取得するローダーです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class FriendListLoader extends AbstractAsyncTaskLoader<List<ChatFriend>> {
	
	public FriendListLoader(Context context) {
		super(context);
	}
	@Override
	public List<ChatFriend> loadInBackground() {
		return ChatFriend.list();
	}
}
