package com.kii.sample.chat.util;

import java.util.concurrent.RejectedExecutionException;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

/**
 * A suite of utilities surrounding the use of the thread.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ThreadUtils {
	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException ignore) {
		}
	}
	@TargetApi(11)
	public static <T> AsyncTask<T, ?, ?> executeTaskOnExecutor(AsyncTask<T, ?, ?> task, T... arg) {
		for (int i = 0; i < 2; i++) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, arg);
				} else {
					task.execute(arg);
				}
				break;
			} catch (RejectedExecutionException ignore) {
				Logger.w("unable to start thread", ignore);
				sleep(1000);
			}
		}
		return task;
	}
}
