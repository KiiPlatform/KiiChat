package com.kii.sample.chat;

import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.storage.Kii;
import com.kii.sample.chat.util.Logger;

import android.app.Application;
import android.content.Context;

/**
 * Custom implementation of Application.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class KiiChatApplication extends Application {
	
	private static Context context;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		// Initialize SDK when application is started.
		Logger.i("■■■ initialize KII SDK ■■■");
		Kii.initialize(getContext(), ApplicationConst.APP_ID, ApplicationConst.APP_KEY, ApplicationConst.APP_SITE);
	}
	public static Context getContext(){
		return context;
	}
	public static String getMessage(int msgId) {
		return context.getResources().getString(msgId);
	}
	public static String getFormattedMessage(int msgId, Object... args) {
		String message = context.getResources().getString(msgId);
		return String.format(message, args);
	}
}
