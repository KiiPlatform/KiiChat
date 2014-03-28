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
 * チャットルームを表します。
 * チャットルームはKiiObjectとして保存されるのではなく、KiiGroupとchat_roomという名前で作成されるバケットで表現されます。
 * ユーザがチャットを開始すると、自分とチャット友達が属するKiiGroupが作成されます。
 * さらにそのグループスコープのバケットとしてchat_roomが作成され、そこにメッセージを保存します。
 * チャットメンバーはこのchat_roomバケットを監視しているため、誰かがchat_roomバケットにメッセージを保存すると、チャットメンバーに通知されます。
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
	 * ログイン中のユーザが所属するグループのchat_roomバケットを全て購読していることを確認します。
	 * 購読していないバケットがある場合は、購読します。
	 * chat_roomバケットの購読処理は通常、他のユーザがチャットを開始したタイミングでPush通知を介して行われますが
	 * Push通知を受けた時点でユーザがログインしていないと、購読処理が行われないため、ログイン時にこのメソッドを呼ぶ必要があります。
	 * 毎回、購読状況をサーバに確認するのは非効率なので、実際は購読状況をローカルにキャッシュするのが望ましいです。
	 * 
	 * @param kiiUser
	 */
	public static void ensureSubscribedBucket(final KiiUser kiiUser) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					// ユーザが所属しているグループの一覧を取得
					List<KiiGroup> groups = kiiUser.memberOfGroups();
					for (KiiGroup group : groups) {
						// 全てのグループでchat_roomバケットを購読済みであることを確認
						KiiBucket chatBucket = ChatRoom.getBucket(group);
						boolean isSubscribed = kiiUser.pushSubscription().isSubscribed(chatBucket);
						if (!isSubscribed) {
							// 購読されていない場合は、購読する
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
	 * チャットルームの名前を取得します。
	 * 名前はチャットメンバーの名前をカンマ区切りで連結した文字列です。
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
	 * チャットルームを一意に識別するキーを生成します。
	 * キーはチャットメンバー全員のURIを"_"で連結した文字列です。
	 * このキーを比較することで、既に存在するチャットかどうかを判定できます。
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
	 * チャットルーム内の全てのメッセージを取得します。
	 * 
	 * @return　昇順にソートされたメッセージリスト
	 */
	public List<ChatMessage> getMessageList() {
		return this.queryMessageList(ChatMessage.createQuery());
	}
	/**
	 * 指定した日時以降に作成されたチャットルーム内のメッセージを取得します。
	 * 
	 * @param modifiedSinceTime
	 * @return 昇順にソートされたメッセージリスト
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
