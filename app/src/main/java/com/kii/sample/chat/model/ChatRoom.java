package com.kii.sample.chat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.sample.chat.util.Logger;

/**
 * Represents the chat room.
 * Chat room is represented by group and bucket. It's not a KiiObject.
 * KiiGroup will create when user starts chat.
 * Then 'chat_room' bucket will create on this group scope.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatRoom {
	
	private static final String BUCKET_NAME = "chat_room";
	public static final String CAHT_GROUP_URI = "chat_group_uri";
	
	public static KiiBucket getBucket(KiiGroup kiiGroup) {
		return kiiGroup.bucket(BUCKET_NAME);
	}
	/**
	 * Makes sure that specified user already subscribed all chat_room bucket.
	 * Subscribes bucket, If user did not subscribe bucket.
	 * 
	 * @param kiiUser
	 */
	public static void ensureSubscribedBucket(final KiiUser kiiUser) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					List<KiiGroup> groups = kiiUser.memberOfGroups();
					for (KiiGroup group : groups) {
						KiiBucket chatBucket = ChatRoom.getBucket(group);
						boolean isSubscribed = kiiUser.pushSubscription().isSubscribed(chatBucket);
						if (!isSubscribed) {
							kiiUser.pushSubscription().subscribeBucket(chatBucket);
						}
					}
					return true;
				} catch (Exception e) {
					return false;
				}
			}
			@Override
			protected void onPostExecute(Boolean result) {
				if (!result) {
					Logger.e("Unable to subscribe group bucket");
				}
			}
		}.execute();
	}
	/**
	 * Gets the name of chat room.
	 * 
	 * @param user
	 * @param chatFriend
	 * @return
	 * @throws Exception
	 */
	public static String getChatRoomName(KiiUser user, ChatFriend chatFriend) throws Exception {
		ChatUser me = ChatUser.findByUri(user.toUri());
		List<String> members = new ArrayList<String>();
		members.add(me.getUsername());
		members.add(chatFriend.getUsername());
		Collections.sort(members);
		return TextUtils.join(",", members);
	}
	/**
	 * Generates a key which identifies the chat room.
	 * 
	 * @param kiiGroup
	 * @return
	 */
	public static String getUniqueKey(KiiGroup kiiGroup) {
		try {
			kiiGroup.refresh();
			List<KiiUser> members = kiiGroup.listMembers();
			return getUniqueKey(members);
		} catch (Exception e) {
			return null;
		}
	}
	public static String getUniqueKey(KiiUser user, ChatFriend friend) {
		List<KiiUser> members = new ArrayList<KiiUser>();
		members.add(user);
		members.add(KiiUser.createByUri(Uri.parse(friend.getUri())));
		return getUniqueKey(members);
	}
	public static String getUniqueKey(List<KiiUser> members) {
		List<String> memberUri = new ArrayList<String>();
		for (KiiUser member : members) {
			memberUri.add(member.toUri().toString());
		}
		Collections.sort(memberUri);
		return TextUtils.join("_", memberUri);
	}
	
	private final KiiBucket kiiBucket;
	
	public ChatRoom(KiiGroup kiiGroup) {
		this.kiiBucket = getBucket(kiiGroup);
	}
	
	/**
	 * Gets all messages in the chat room bucket
	 * 
	 * @return　Message list that is sorted in ascending order
	 */
	public List<ChatMessage> getMessageList() {
		return this.queryMessageList(ChatMessage.createQuery());
	}
	/**
	 * Gets messages in the chat room bucket that is created after the specified date.
	 * 
	 * @param modifiedSinceTime
	 * @return Message list that is sorted in ascending order
	 */
	public List<ChatMessage> getMessageList(long modifiedSinceTime) {
		return this.queryMessageList(ChatMessage.createQuery(modifiedSinceTime));
	}
	private List<ChatMessage> queryMessageList(KiiQuery query) {
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			List<KiiObject> results = this.kiiBucket.query(query).getResult();
			for (KiiObject o : results) {
				messages.add(new ChatMessage(o));
			}
		} catch (Exception e) {
			Logger.e("Unable to list messages", e);
		}
		return messages;
	}
}
