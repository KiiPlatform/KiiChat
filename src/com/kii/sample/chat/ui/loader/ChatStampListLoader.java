package com.kii.sample.chat.ui.loader;

import java.util.List;

import android.content.Context;

import com.kii.sample.chat.model.ChatStamp;

/**
 * {@link ChatStamp}をchat_stampsバケットから取得するローダーです。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatStampListLoader extends AbstractAsyncTaskLoader<List<ChatStamp>> {

	public ChatStampListLoader(Context context) {
		super(context);
	}
	@Override
	public List<ChatStamp> loadInBackground() {
		return ChatStamp.listOrderByNewly();
	}

}
