package com.kii.sample.chat.ui;

import java.util.List;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiPushMessage;
import com.kii.cloud.storage.KiiTopic;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.KiiPushMessage.Data;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.PreferencesManager;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatFriend;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.ui.SelectFriendDialogFragment.OnSelectFriendListener;
import com.kii.sample.chat.ui.adapter.GroupListAdapter;
import com.kii.sample.chat.ui.loader.ChatListLoader;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;
import com.kii.sample.chat.util.Logger;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatListFragment extends ListFragment implements LoaderCallbacks<List<KiiGroup>>, OnItemClickListener, OnSelectFriendListener {
	
	public static ChatListFragment newInstance() {
		return new ChatListFragment();
	}
	
	private GroupListAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);
		this.adapter = new GroupListAdapter(getActivity());
		this.setListAdapter(this.adapter);
		this.setListShown(false);
		this.getListView().setOnItemClickListener(this);
		this.getLoaderManager().initLoader(0, state, this);
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.chat_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
			case R.id.menu_new_chat:
				SelectFriendDialogFragment selectFriendDialog = SelectFriendDialogFragment.newInstance(this);
				selectFriendDialog.show(getFragmentManager(), "selectFriend");
				return true;
			case R.id.menu_reload:
				this.getLoaderManager().restartLoader(0, null, this);
				return true;
			case R.id.menu_signout:
				PreferencesManager.setStoredAccessToken("");
				KiiUser.logOut();
				intent = new Intent(getActivity(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public Loader<List<KiiGroup>> onCreateLoader(int id, Bundle bundle) {
		return new ChatListLoader(getActivity());
	}
	@Override
	public void onLoadFinished(Loader<List<KiiGroup>> loader, List<KiiGroup> data) {
		this.adapter.setData(data);
		if (isResumed()) {
			this.setListShown(true);
		} else {
			this.setListShownNoAnimation(true);
		}
	}
	@Override
	public void onLoaderReset(Loader<List<KiiGroup>> loader) {
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		KiiGroup kiiGroup = (KiiGroup)parent.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), ChatActivity.class);
		intent.putExtra(ChatActivity.INTENT_GROUP_URI, kiiGroup.toUri().toString());
		startActivity(intent);
	}
	@Override
	public void onSelectFriend(ChatFriend friend) {
		new NewChatTask(friend).execute();
	}
	
	private class NewChatTask extends AsyncTask<Void, Void, KiiGroup> {
		private final ChatFriend chatFriend;
		private NewChatTask(ChatFriend chatFriend) {
			this.chatFriend = chatFriend;
		}
		@Override
		protected void onPreExecute() {
			SimpleProgressDialogFragment.show(getFragmentManager(), "Start Chat", "Processing...");
		}
		@Override
		protected KiiGroup doInBackground(Void... params) {
			try {
				String chatRoomName = ChatRoom.getChatRoomName(KiiUser.getCurrentUser(), this.chatFriend);
				String uniqueKey = ChatRoom.getUniqueKey(KiiUser.getCurrentUser(), this.chatFriend);
				for (int i = 0; i < getListView().getCount(); i++) {
					KiiGroup kiiGroup = (KiiGroup)getListView().getItemAtPosition(i);
					if (TextUtils.equals(uniqueKey, ChatRoom.getUniqueKey(kiiGroup))) {
						return kiiGroup;
					}
				}
				// Creating a group
				KiiGroup kiiGroup = Kii.group(chatRoomName);
				KiiUser target = KiiUser.createByUri(Uri.parse(this.chatFriend.getUri()));
				target.refresh();
				kiiGroup.addUser(target);
				kiiGroup.save();
				// Subscribes a chat bucket in order to receive push notification when message is saved in bucket.
				KiiBucket chatBucket = ChatRoom.getBucket(kiiGroup);
				KiiUser.getCurrentUser().pushSubscription().subscribeBucket(chatBucket);
				// Sends notification to the collocutor.
				KiiTopic topic = target.topicOfThisUser(ApplicationConst.TOPIC_INVITE_NOTIFICATION);
				Data data = new Data();
				data.put(ChatRoom.CAHT_GROUP_URI, kiiGroup.toUri().toString());
				KiiPushMessage message = KiiPushMessage.buildWith(data).build();
				topic.sendMessage(message);
				Logger.i("sent notification to " + target.toUri().toString());
				return kiiGroup;
			} catch (Exception e) {
				Logger.e("failed to start chat", e);
				return null;
			}
		}
		protected void onPostExecute(KiiGroup kiiGroup) {
			SimpleProgressDialogFragment.hide(getFragmentManager());
			if (kiiGroup == null) {
				ToastUtils.showShort(getActivity(), "Unable to start chat");
				return;
			}
			Intent intent = new Intent(getActivity(), ChatActivity.class);
			intent.putExtra(ChatActivity.INTENT_GROUP_URI, kiiGroup.toUri().toString());
			startActivity(intent);
		}
	}
}
