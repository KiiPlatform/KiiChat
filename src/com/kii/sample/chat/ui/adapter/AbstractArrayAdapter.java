package com.kii.sample.chat.ui.adapter;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  Skeleton implementation of ArrayAdapter.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public abstract class AbstractArrayAdapter<T> extends ArrayAdapter<T> {
	static class ViewHolder {
		int position;
		TextView text;
		ImageView icon;
	}
	public AbstractArrayAdapter(Context context, int resourceId) {
		super(context, resourceId);
	}
	public AbstractArrayAdapter(Context context, int resourceId, List<T> objects) {
		super(context, resourceId, objects);
	}
	public void addAll(List<? extends T> dataList) {
		if (dataList != null && dataList.size() > 0) {
			for (T data : dataList) {
				add(data);
			}
		}
	}
	public void setData(List<? extends T> data) {
		this.clear();
		this.addAll(data);
	}
}
