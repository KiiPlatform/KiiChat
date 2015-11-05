package com.kii.sample.chat.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatFriend;
import com.kii.sample.chat.ui.adapter.UserListAdapter;
import com.kii.sample.chat.ui.loader.FriendListLoader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Prompts the user to select a friend.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class SelectFriendDialogFragment extends DialogFragment implements LoaderCallbacks<List<ChatFriend>>, OnItemClickListener {
	
	public interface OnSelectFriendListener {
		public void onSelectFriend(ChatFriend friend);
	}
	
	public static SelectFriendDialogFragment newInstance(OnSelectFriendListener onSelectFriendListener) {
		SelectFriendDialogFragment dialog = new SelectFriendDialogFragment();
		dialog.setOnSelectFriendListener(onSelectFriendListener);
		return dialog;
	}
	
	private WeakReference<OnSelectFriendListener> onSelectFriendListener;
	private TextView textEmpty;
	private ListView listView;
	private UserListAdapter adapter;
	
	private void setOnSelectFriendListener(OnSelectFriendListener onSelectFriendListener) {
		this.onSelectFriendListener = new WeakReference<SelectFriendDialogFragment.OnSelectFriendListener>(onSelectFriendListener);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {  
		super.onActivityCreated(savedInstanceState);
		this.adapter = new UserListAdapter(getActivity());
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_select_list_dialog, null, false);
		this.textEmpty = (TextView)view.findViewById(R.id.text_empty);
		this.listView = (ListView)view.findViewById(R.id.list_view);
		this.listView.setOnItemClickListener(this);
		this.getLoaderManager().initLoader(0, savedInstanceState, this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setNegativeButton(R.string.button_cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		builder.setView(view);
		return builder.create();
	}
	@Override
	public Loader<List<ChatFriend>> onCreateLoader(int id, Bundle bundle) {
		return new FriendListLoader(getActivity());
	}
	@Override
	public void onLoadFinished(Loader<List<ChatFriend>> loader, List<ChatFriend> data) {
		if (data == null || data.size() == 0) {
			this.listView.setVisibility(View.GONE);
			this.textEmpty.setVisibility(View.VISIBLE);
		} else {
			this.adapter.setData(data);
			this.listView.setAdapter(this.adapter);
		}
	}
	@Override
	public void onLoaderReset(Loader<List<ChatFriend>> loader) {
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ChatFriend friend = (ChatFriend)parent.getItemAtPosition(position);
		OnSelectFriendListener listener = this.onSelectFriendListener.get();
		if (listener != null) {
			listener.onSelectFriend(friend);
		}
		dismiss();
	}
}
