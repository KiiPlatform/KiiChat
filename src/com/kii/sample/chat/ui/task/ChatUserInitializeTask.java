package com.kii.sample.chat.ui.task;

import android.os.AsyncTask;

import com.kii.cloud.storage.KiiACL;
import com.kii.cloud.storage.KiiACL.TopicAction;
import com.kii.cloud.storage.KiiACLEntry;
import com.kii.cloud.storage.KiiAnyAuthenticatedUser;
import com.kii.cloud.storage.KiiPushSubscription;
import com.kii.cloud.storage.KiiTopic;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.exception.ACLOperationException;
import com.kii.cloud.storage.exception.app.ConflictException;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.model.ChatUser;
import com.kii.sample.chat.util.GCMUtils;
import com.kii.sample.chat.util.Logger;

/**
 * バックグラウンドでユーザーサインアップ・ログイン後の初期化処理を実行します。
 * 
 * @author ryuji.ochi@kii.com
 */
public abstract class ChatUserInitializeTask extends AsyncTask<Void, Void, Boolean> {
	
	private static final String INITIALIZED_USER_KEY = "initialized";
	
	public interface OnInitializeListener {
		public void onInitializeCompleted();
	}
	
	private final String username;
	private final String email;
	
	public ChatUserInitializeTask(String username, String email) {
		this.username = username;
		this.email = email;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		if (KiiUser.getCurrentUser().getBoolean(INITIALIZED_USER_KEY, false)) {
			// 初期化処理が終了している場合、GCMのRegistrationIDを更新する
			try {
				installGCMRegistrationID();
				return true;
			} catch (Exception ex) {
				Logger.e("Unable to setup the GCM.", ex);
				return false;
			}
		} else {
			// 初期化処理を行う
			try {
				initializeChatUser();
				return true;
			} catch (Exception e) {
				Logger.e("Failed to initialize", e);
				return false;
			}
		}
	}
	
	private void installGCMRegistrationID() throws Exception {
		String registrationId = GCMUtils.register();
		KiiUser.pushInstallation().install(registrationId);
	}
	
	private void initializeChatUser() throws Exception {
		// ユーザーサインアップ後の処理は以下の5つの処理で構成される
		// 途中で失敗したかどうかを判断するためにユーザーのプロパティに "initialized" を追加している。
		//
		// 1.AppScope Bucketへのチャットユーザー登録
		// 2.Pushのinstall
		// 3.User Topicの作成
		// 4.User TopicへのACLの設定
		// 5.Topicの購読
		KiiUser kiiUser = KiiUser.getCurrentUser();
		ChatUser user = ChatUser.findByUri(kiiUser.toUri());
		if (user == null) {
			user = new ChatUser(kiiUser.toUri().toString(), username, email);
			user.getKiiObject().save();
		}
		// GCMの設定
		installGCMRegistrationID();
		// サーバからプッシュ通知を受信する為に、自分専用のトピックを作成する
		// このトピックは他の全てのユーザに書き込み権限を与え
		// 他のユーザが自分をチャットメンバー追加したことを通知する為に使用する
		KiiTopic topic = KiiUser.topic(ApplicationConst.TOPIC_INVITE_NOTIFICATION);
		try {
			topic.save();
		} catch (ConflictException e) {
			// このExceptionをキャッチした場合は、Topicはすでに作成されている
		}
		KiiACL acl = topic.acl();
		acl.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser.create(), TopicAction.SEND_MESSAGE_TO_TOPIC, true));
		try {
			acl.save();
		} catch (ACLOperationException e) {
			Throwable t = e.getCause();
			if (t instanceof ConflictException){
				// このExceptionをキャッチした場合は、ACLはすでに作成されている
			} else {
				// それ以外の場合のExceptionの場合は再度スローする
				throw e;
			}
		}
		KiiPushSubscription subscription = kiiUser.pushSubscription();
		try {
			subscription.subscribe(topic);
		} catch (ConflictException e) {
			// このExceptionをキャッチした場合は、Topicはすでに購読されている
		}
		// 初期化が完了していることを示すフラグをユーザーのカスタムフィールドに追加し、更新する
		kiiUser.set(INITIALIZED_USER_KEY, true);
		kiiUser.update();
	}
}