package com.kii.sample.chat;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * A helper class to handle SharedPreferences.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class PreferencesManager {
	interface Key {
		static final String ACCESS_TOKEN = "token";
		static final String SENDER_ID = "sender_id";
	}
	
	public static void setStoredAccessToken(String token) {
		setString(Key.ACCESS_TOKEN, token);
	}
	public static String getStoredAccessToken() {
		return getSharedPreferences().getString(Key.ACCESS_TOKEN, null);
	}
	private static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(KiiChatApplication.getContext());
	}
	private static void setString(String key, String value) {
		Editor editor = getSharedPreferences().edit();
		editor.putString(key, value);
		editor.commit();
	}
}
