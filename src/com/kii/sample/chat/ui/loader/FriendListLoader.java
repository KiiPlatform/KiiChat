package com.kii.sample.chat.ui.loader;

import java.util.List;

import android.content.Context;

import com.kii.sample.chat.model.ChatFriend;

/**
 * A Loader to get {@link ChatFriend} from chat_friends bucket.
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
