package com.kii.sample.chat.ui;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.ui.task.ChatUserInitializeTask.OnInitializeListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.callback.KiiSocialCallBack;
import com.kii.cloud.storage.social.KiiFacebookConnect;
import com.kii.cloud.storage.social.KiiSocialConnect.SocialNetwork;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.PreferencesManager;
import com.kii.sample.chat.ui.task.ChatUserInitializeTask;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;
import com.kii.sample.chat.util.Logger;

/**
 * Activity of sign in screen.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class SigninActivity extends FragmentActivity implements OnInitializeListener{
	
	private TextView textNewAccount;
	private Button btnFbSignin;
	private Button btnSignin;
	private CheckBox checkRemember;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		
		this.textNewAccount = (TextView)findViewById(R.id.text_new_account);
		this.checkRemember = (CheckBox)findViewById(R.id.check_remember);
		this.btnFbSignin = (Button)findViewById(R.id.button_facebook);
		this.btnSignin = (Button)findViewById(R.id.button_signin);
		
		this.btnFbSignin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KiiFacebookConnect connect = (KiiFacebookConnect) Kii.socialConnect(SocialNetwork.FACEBOOK);
				connect.initialize(ApplicationConst.FACEBOOK_APP_ID, null, null);
				Bundle options = new Bundle();
				String[] permission = new String[] { "email" };
				options.putStringArray(KiiFacebookConnect.FACEBOOK_PERMISSIONS, permission);
				connect.logIn(SigninActivity.this, options, new KiiSocialCallBack() {
					public void onLoginCompleted(SocialNetwork network, KiiUser user, Exception exception) {
						if (exception == null) {
							if (checkRemember.isChecked()) {
								Logger.i(user.getAccessToken());
								PreferencesManager.setStoredAccessToken(user.getAccessToken());
							}
							new PostSigninTask(user.getDisplayname(), user.getEmail()).execute();
						} else {
							Logger.e("failed to sign up", exception);
							ToastUtils.showShort(SigninActivity.this, "Unable to sign up");
						}
					}
				});
			}
		});
		this.btnSignin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SigninDialogFragment signinFragment = SigninDialogFragment.newInstance(SigninActivity.this, checkRemember.isChecked());
				signinFragment.show(getSupportFragmentManager(), SigninDialogFragment.TAG);
			}
		});
		this.textNewAccount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SignupDialogFragment signupFragment = SignupDialogFragment.newInstance(SigninActivity.this);
				signupFragment.show(getSupportFragmentManager(), "signup");
			}
		});
	}
	
	private class PostSigninTask extends ChatUserInitializeTask {
		
		private PostSigninTask(String username, String email) {
			super(username, email);
		}
		
		@Override
		protected void onPreExecute() {
			SimpleProgressDialogFragment.show(getSupportFragmentManager(), "Signin", "Processing...");
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			SimpleProgressDialogFragment.hide(getSupportFragmentManager());
			if (result) {
				moveToChatMain();
			} else {
				ToastUtils.showShort(SigninActivity.this, "Unable to sign in");
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Kii.socialConnect(SocialNetwork.FACEBOOK).respondAuthOnActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onInitializeCompleted() {
		moveToChatMain();
	}
	private void moveToChatMain() {
		ChatRoom.ensureSubscribedBucket(KiiUser.getCurrentUser());
		Intent intent = new Intent(SigninActivity.this, ChatMainActivity.class);
		startActivity(intent);
	}
}
