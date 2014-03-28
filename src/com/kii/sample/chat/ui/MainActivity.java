package com.kii.sample.chat.ui;

import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.sample.chat.PreferencesManager;
import com.kii.sample.chat.model.ChatRoom;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

/**
 * アプリケーション起動時に呼ばれるアクティビティです。
 * このアクティビティはUIを持ちません。
 * 必要に応じて自動的にサインイン処理を行います。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class MainActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// SharedPreferencesにAccessTokenが保存されているかチェックする
		String token = PreferencesManager.getStoredAccessToken();
		if (!TextUtils.isEmpty(token)) {
			// 保存したTokenでログインを実行する
			SimpleProgressDialogFragment.show(getSupportFragmentManager(), "Login", "Processing...");
			KiiUser.loginWithToken(new KiiUserCallBack() {
				@Override
				public void onLoginCompleted(int token, KiiUser user, Exception e) {
					if (e == null) {
						// サインイン成功時はチャット画面に遷移
						ChatRoom.ensureSubscribedBucket(user);
						Intent intent = new Intent(MainActivity.this, ChatMainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					} else {
						// サインイン失敗時はサインイン画面に遷移
						PreferencesManager.setStoredAccessToken("");
						Intent intent = new Intent(MainActivity.this, SigninActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					SimpleProgressDialogFragment.hide(getSupportFragmentManager());
				}
			},token);
		} else {
			// Tokenが保存されていない場合はサインイン画面に遷移
			Intent intent = new Intent(MainActivity.this, SigninActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
	}
}
