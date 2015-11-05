package com.kii.sample.chat.ui;

import java.io.File;
import java.util.List;

import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatMessage;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.model.ChatStamp;
import com.kii.sample.chat.ui.SelectStampDialogFragment.OnSelectStampListener;
import com.kii.sample.chat.ui.adapter.AbstractArrayAdapter;
import com.kii.sample.chat.ui.loader.ChatStampImageFetcher;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;
import com.kii.sample.chat.util.Logger;
import com.kii.sample.chat.util.StampCacheUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatActivity extends ActionBarActivity implements OnSelectStampListener {
	
	public static final String INTENT_GROUP_URI = "group_uri";
	public static int REQUEST_GET_IMAGE_FROM_GALLERY = 1;
	
	private Vibrator vibrator;
	private ListView listView;
	private MessageListAdapter adapter;
	private ChatStampImageFetcher imageFetcher;
	private EditText editMessage;
	private ImageButton btnSelectEmoticon;
	private ImageButton btnSend;
	private KiiGroup kiiGroup;
	private Long lastGotTime;
	
	private final BroadcastReceiver handleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateMessage(false);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		this.vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		this.adapter = new MessageListAdapter(this, KiiUser.getCurrentUser());
		this.imageFetcher = new ChatStampImageFetcher(this);
		this.imageFetcher.setLoadingImage(R.drawable.spinner);
		this.listView = (ListView)findViewById(R.id.list_view);
		this.listView.setAdapter(this.adapter);
		this.editMessage = (EditText)findViewById(R.id.edit_message);
		this.editMessage.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				if (TextUtils.isEmpty(editMessage.getText().toString())) {
					btnSend.setEnabled(false);
				} else {
					btnSend.setEnabled(true);
				}
			}
		});
		this.btnSelectEmoticon = (ImageButton)findViewById(R.id.button_select_stamp);
		this.btnSelectEmoticon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectStampDialogFragment dialog = SelectStampDialogFragment.newInstance(ChatActivity.this);
				dialog.show(getSupportFragmentManager(), "selectStampDialogFragment");
			}
		});
		
		this.btnSend = (ImageButton)findViewById(R.id.button_send);
		this.btnSend.setEnabled(false);
		this.btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Saves inputted message on background thread.
				btnSend.setEnabled(false);
				final ChatMessage message = new ChatMessage(kiiGroup);
				message.setMessage(editMessage.getText().toString());
				message.setSenderUri(KiiUser.getCurrentUser().toUri().toString());
				new SendMessageTask(message).execute();
			}
		});
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_GET_IMAGE_FROM_GALLERY && data != null) {
			new StampUploader(data.getData()).execute();
		}
	}
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(this.handleMessageReceiver, new IntentFilter(ApplicationConst.ACTION_MESSAGE_RECEIVED));
		String uri = getIntent().getStringExtra(INTENT_GROUP_URI);
		this.kiiGroup = KiiGroup.createByUri(Uri.parse(uri));
		updateMessage(true);
	}
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(this.handleMessageReceiver);
	}
	private void updateMessage(boolean showProgress) {
		new GetMessageTask(showProgress).execute();
	}
	@Override
	public void onSelectStamp(ChatStamp stamp) {
		new SendMessageTask(ChatMessage.createStampChatMessage(this.kiiGroup, stamp)).execute();
	}
	private class SendMessageTask extends AsyncTask<Void, Void, Boolean> {
		private final ChatMessage message;
		private SendMessageTask(ChatMessage message) {
			this.message = message;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				this.message.getKiiObject().save();
				ChatStamp.sendUsageEvent(this.message);
				return true;
			} catch (Exception e) {
				Logger.e("failed to send messsage", e);
				return false;
			}
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				editMessage.setText("");
			} else {
				ToastUtils.showShort(ChatActivity.this, "Unable to send messsage");
			}
		}
	}
	private static class ViewHolder {
		TextView message;
		ImageView stamp;
	}
	/**
	 * A adapter class to show the {@link ChatMessage}.
	 */
	private class MessageListAdapter extends AbstractArrayAdapter<ChatMessage> {
		
		private static final int ROW_SELF_MESSAGE = 0;
		private static final int ROW_FRIEND_MESSAGE = 1;
		private static final int ROW_SELF_STAMP = 2;
		private static final int ROW_FRIEND_STAMP = 3;
		
		private final LayoutInflater inflater;
		private final String userUri;
		
		public MessageListAdapter(Context context, KiiUser kiiUser) {
			super(context, R.layout.chat_message_me);
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.userUri = kiiUser.toUri().toString();
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			ChatMessage chatMessage = this.getItem(position);
			if (convertView == null) {
				switch (getRowType(chatMessage)) {
					case ROW_SELF_MESSAGE:
						convertView = this.inflater.inflate(R.layout.chat_message_me, parent, false);
						break;
					case ROW_SELF_STAMP:
						convertView = this.inflater.inflate(R.layout.chat_stamp_me, parent, false);
						break;
					case ROW_FRIEND_MESSAGE:
						convertView = this.inflater.inflate(R.layout.chat_message_friend, parent, false);
						break;
					case ROW_FRIEND_STAMP:
						convertView = this.inflater.inflate(R.layout.chat_stamp_friend, parent, false);
						break;
				}
				holder = new ViewHolder();
				if (chatMessage.isStamp()) {
					holder.message = null;
					holder.stamp = (ImageView)convertView.findViewById(R.id.row_stamp);
				} else {
					holder.message = (TextView)convertView.findViewById(R.id.row_message);
					holder.stamp = null;
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			if (chatMessage.isStamp()) {
				ChatStamp stamp = new ChatStamp(chatMessage);
				imageFetcher.fetchStamp(stamp, holder.stamp);
			} else {
				String message = chatMessage.getMessage() == null ? "" : chatMessage.getMessage();
				holder.message.setText(message);
			}
			return convertView;
		}
		@Override
		public int getViewTypeCount() {
			return 4;
		}
		@Override
		public int getItemViewType(int position) {
			return getRowType(getItem(position));
		}
		private int getRowType(ChatMessage chatMessage) {
			if (TextUtils.equals(this.userUri, chatMessage.getSenderUri())) {
				if (chatMessage.isStamp()) {
					return ROW_SELF_STAMP;
				} else {
					return ROW_SELF_MESSAGE;
				}
			} else {
				if (chatMessage.isStamp()) {
					return ROW_FRIEND_STAMP;
				} else {
					return ROW_FRIEND_MESSAGE;
				}
			}
		}
	}
	private class GetMessageTask extends AsyncTask<Void, Void, List<ChatMessage>> {
		private final boolean showProgress;
		private GetMessageTask(boolean showProgress) {
			this.showProgress = showProgress;
		}
		@Override
		protected void onPreExecute() {
			if (this.showProgress) {
				SimpleProgressDialogFragment.show(getSupportFragmentManager(), "Chat", "Loading...");
			}
		}
		@Override
		protected List<ChatMessage> doInBackground(Void... params) {
			try {
				ChatRoom chatRoom = new ChatRoom(kiiGroup);
				List<ChatMessage> messages = null;
				if (lastGotTime == null) {
					messages = chatRoom.getMessageList();
				} else {
					messages = chatRoom.getMessageList(lastGotTime);
				}
				if (messages.size() > 0) {
					lastGotTime = messages.get(messages.size() - 1).getKiiObject().getCreatedTime();
				}
				return messages;
			} catch (Exception e) {
				Logger.e("failed to get message", e);
				return null;
			}
		}
		@Override
		protected void onPostExecute(List<ChatMessage> messages) {
			if (messages != null) {
				adapter.addAll(messages);
				adapter.notifyDataSetChanged();
			} else {
				ToastUtils.showShort(ChatActivity.this, "Unable to get message");
			}
			if (this.showProgress) {
				SimpleProgressDialogFragment.hide(getSupportFragmentManager());
			} else {
				vibrator.vibrate(500);
			}
			listView.setSelection(listView.getCount());
		}
	}
	private class StampUploader extends AsyncTask<Void, Void, ChatStamp> {
		private final Uri imageUri;
		private StampUploader(Uri imageUri) {
			this.imageUri = imageUri;
		}
		@Override
		protected void onPreExecute() {
			SimpleProgressDialogFragment.show(getSupportFragmentManager(), "Add Stamp", "Uploading...");
		}
		@Override
		protected ChatStamp doInBackground(Void... params) {
			try {
				File imageFile = StampCacheUtils.copyToCache(this.imageUri, 128);
				ChatStamp stamp = new ChatStamp(imageFile);
				stamp.save();
				return stamp;
			} catch (Exception e) {
				Logger.e("failed to upload image", e);
				return null;
			}
		}
		@Override
		protected void onPostExecute(ChatStamp stamp) {
			SimpleProgressDialogFragment.hide(getSupportFragmentManager());
			if (stamp != null) {
				onSelectStamp(stamp);
			} else {
				ToastUtils.showShort(ChatActivity.this, "Unable to upload stamp");
			}
		}
	}
}
