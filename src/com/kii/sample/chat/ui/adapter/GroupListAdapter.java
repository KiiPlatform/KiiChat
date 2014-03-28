package com.kii.sample.chat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kii.cloud.storage.KiiGroup;
import com.kii.sample.chat.R;

/**
 * A adapter class to show the {@link KiiGroup}.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class GroupListAdapter extends AbstractArrayAdapter<KiiGroup> {
	
	private final LayoutInflater inflater;
	
	public GroupListAdapter(Context context) {
		super(context, R.layout.image_list_item);
		this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = this.inflater.inflate(R.layout.image_list_item, parent, false);
			holder = new ViewHolder();
			holder.text = (TextView)convertView.findViewById(R.id.row_text);
			holder.icon = (ImageView)convertView.findViewById(R.id.row_icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		KiiGroup group = this.getItem(position);
		holder.position = position;
		holder.text.setText(group.getGroupName());
		holder.icon.setImageResource(R.drawable.list_account);
		return convertView;
	}

}
