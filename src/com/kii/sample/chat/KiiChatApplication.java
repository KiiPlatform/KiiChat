package com.kii.sample.chat;

import com.kii.cloud.analytics.KiiAnalytics;
import com.kii.cloud.storage.Kii;
import com.kii.sample.chat.util.Logger;

import android.app.Application;
import android.content.Context;

/**
 * Applicationのカスタム実装です。
 * ApplicationContextに関する共通処理を行います。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class KiiChatApplication extends Application {
	
	private static Context context;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		// アプリケーション起動時にKiiSDKを初期化します。
		Logger.i("■■■ initialize KII SDK ■■■");
		Kii.initialize(ApplicationConst.APP_ID, ApplicationConst.APP_KEY, Kii.Site.JP);
		Logger.i("■■■ initialize KII Analytics SDK ■■■");
		KiiAnalytics.initialize(context, ApplicationConst.APP_ID, ApplicationConst.APP_KEY, KiiAnalytics.Site.JP);
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
