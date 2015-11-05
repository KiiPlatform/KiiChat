package com.kii.sample.chat.ui.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Skeleton implementation of AsyncTaskLoader.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public abstract class AbstractAsyncTaskLoader<T> extends AsyncTaskLoader<T> {
	
	protected T result;
	
	public AbstractAsyncTaskLoader(Context context) {
		super(context);
	}
	@Override
	public void deliverResult(T result) {
		if (isReset()) {
			if (result != null) {
				this.result = null;
			}
			return;
		}
		this.result = result;
		if (isStarted()) {
			super.deliverResult(result);
		}
	}
	@Override
	protected void onStartLoading() {
		if (this.result != null) {
			deliverResult(this.result);
		}
		if (takeContentChanged() || this.result == null) {
			forceLoad();
		}
	}
	@Override
	protected void onStopLoading() {
		super.onStopLoading();
		cancelLoad();
	}
	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
	}
}
