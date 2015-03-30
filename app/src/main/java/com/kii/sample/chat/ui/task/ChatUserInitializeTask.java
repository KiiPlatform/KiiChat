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
 * Does initialization process of the user on background thread.
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
			try {
				installGCMRegistrationID();
				return true;
			} catch (Exception ex) {
				Logger.e("Unable to setup the GCM.", ex);
				return false;
			}
		} else {
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
		// Initialization process consists of the following five steps.
		// Sets property 'initialized' to user, if initialization all complete.
		// 
		// 1.Registering user profile to app scope bucket.
		// 2.Installing user Device.
		// 3.Creating user topic.
		// 4.Setting ACL to the topic.
		// 5.Subscribing the topic.
		KiiUser kiiUser = KiiUser.getCurrentUser();
		ChatUser user = ChatUser.findByUri(kiiUser.toUri());
		if (user == null) {
			user = new ChatUser(kiiUser.toUri().toString(), username, email);
			user.getKiiObject().save();
		}
		installGCMRegistrationID();
		// This topic is used to receive notification that other user starts chat with this user.
		KiiTopic topic = KiiUser.topic(ApplicationConst.TOPIC_INVITE_NOTIFICATION);
		try {
			topic.save();
		} catch (ConflictException e) {
			// Topic already exist.
		}
		KiiACL acl = topic.acl();
		acl.putACLEntry(new KiiACLEntry(KiiAnyAuthenticatedUser.create(), TopicAction.SEND_MESSAGE_TO_TOPIC, true));
		try {
			acl.save();
		} catch (ACLOperationException e) {
			Throwable t = e.getCause();
			if (t instanceof ConflictException){
				// ACL already exist.
			} else {
				throw e;
			}
		}
		KiiPushSubscription subscription = kiiUser.pushSubscription();
		try {
			subscription.subscribe(topic);
		} catch (ConflictException e) {
			// Topic is already subscribed.
		}
		kiiUser.set(INITIALIZED_USER_KEY, true);
		kiiUser.update();
	}
}