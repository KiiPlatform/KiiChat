package com.kii.sample.chat.util;

import android.util.Log;

public class Logger {
	
	public static final String LOG_TAG = "KiiChat";
	
	public static void d(String msg) {
		Log.d(LOG_TAG, msg);
	}
	public static void d(String msg, Throwable tr) {
		Log.d(LOG_TAG, msg, tr);
	}
	public static void v(String msg) {
		Log.v(LOG_TAG, msg);
	}
	public static void v(String msg, Throwable tr) {
		Log.v(LOG_TAG, msg, tr);
	}
	public static void i(String msg) {
		Log.i(LOG_TAG, msg);
	}
	public static void i(String msg, Throwable tr) {
		Log.i(LOG_TAG, msg, tr);
	}
	public static void w(String msg) {
		Log.w(LOG_TAG, msg);
	}
	public static void w(String msg, Throwable tr) {
		Log.w(LOG_TAG, msg, tr);
	}
	public static void e(String msg) {
		Log.e(LOG_TAG, msg);
	}
	public static void e(String msg, Throwable tr) {
		Log.e(LOG_TAG, msg, tr);
	}
}
