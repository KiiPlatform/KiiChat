package com.kii.sample.chat;

import java.util.List;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushMessageBundleHelper.MessageType;
import com.kii.cloud.storage.PushToAppMessage;
import com.kii.cloud.storage.ReceivedMessage;
import com.kii.cloud.storage.exception.GroupOperationException;
import com.kii.sample.chat.model.ChatMessage;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.util.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * GCMPushReceiver listens for incoming GCM messages.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class GCMPushReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		Logger.i("received push message");
		if (KiiUser.getCurrentUser() == null) {
			// Do nothing if user isn't logged in.
			return;
		}
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
			final Bundle extras = intent.getExtras();
			final ReceivedMessage message = PushMessageBundleHelper.parse(extras);
			MessageType type = message.pushMessageType();
			switch (type) {
				case DIRECT_PUSH:
					Logger.i("received DIRECT_PUSH");
					return;
				case PUSH_TO_APP:
					Logger.i("received PUSH_TO_APP");
					try {
						// If a new message is posted to the your chat room.
						Logger.i("received PUSH_TO_USER");
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									KiiObject obj = ((PushToAppMessage)message).getKiiObject();
									obj.refresh();
									ChatMessage chatMessage = new ChatMessage(obj);
									KiiGroup kiiGroup = KiiGroup.createByUri(Uri.parse(chatMessage.getGroupUri()));
									// Ignores message if it's not addressed to logged in user.
									if (isMember(kiiGroup)) {
										sendBroadcast(context, ApplicationConst.ACTION_MESSAGE_RECEIVED, chatMessage.getKiiObject().toJSON().toString());
									}
								} catch (Exception e) {
									Logger.e("Unable to subscribe group bucket", e);
								}
							}
						}).start();
					} catch (Exception e) {
						Logger.e("Unable to get the KiiObject", e);
					}
					break;
				case PUSH_TO_USER:
					Logger.i("received PUSH_TO_USER");
					// If someone starts a new chat with logged in user.
					// Subscribes to chat bucket for the push message.
					// When subscribe, it will be able to receive the push message if event happen in the bucket.
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String groupUri =  extras.getString(ChatRoom.CAHT_GROUP_URI);
								KiiGroup kiiGroup = KiiGroup.createByUri(Uri.parse(groupUri));
								// Ignores message if it's not addressed to logged in user.
								if (isMember(kiiGroup)) {
									KiiBucket chatBucket = ChatRoom.getBucket(kiiGroup);
									KiiUser.getCurrentUser().pushSubscription().subscribeBucket(chatBucket);
									sendBroadcast(context, ApplicationConst.ACTION_CHAT_STARTED, groupUri);
								}
							} catch (Exception e) {
								Logger.e("Unable to subscribe group bucket", e);
							}
						}
					}).start();
					break;
			}
		}
	}
	/**
	 * Checks whether logged in user who belongs to the specified group.
	 * 
	 * @param kiiGroup
	 * @return
	 * @throws GroupOperationException
	 */
	private boolean isMember(KiiGroup kiiGroup) throws GroupOperationException {
		if (KiiUser.getCurrentUser() != null) {
			kiiGroup.refresh();
			List<KiiUser> members = kiiGroup.listMembers();
			for (KiiUser member : members) {
				if (KiiUser.getCurrentUser().toUri().equals(member.toUri())) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Propagates received message to Activity using the broadcast intent.
	 * 
	 * @param context
	 * @param action
	 * @param message
	 */
	private void sendBroadcast(Context context, String action, String message) {
		Intent intent = new Intent(action);
		intent.putExtra(ApplicationConst.EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
}
