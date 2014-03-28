package com.kii.sample.chat.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.text.TextUtils;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.query.KiiClause;
import com.kii.cloud.storage.query.KiiQuery;

/**
 * KiiChatを利用するユーザを表します。
 * アプリケーションスコープのデータとしてサインアップ時にKiiCloudに保存され、他のユーザから検索することが可能です。
 * 実世界のチャットアプリケーションでは全てのユーザを制限なく検索できるとプライバシー的に問題になるので、何かしらの制限を加える必要があります。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatUser extends KiiObjectWrapper implements IUser {
	
	private static final String BUCKET_NAME = "chat_users";
	private static final String FIELD_USERNAME = "username";
	private static final String FIELD_EMAIL = "email";
	private static final String FIELD_URI = "uri";  // KiiUser.toUri()が返した値
	
	public static KiiBucket getBucket() {
		return Kii.bucket(BUCKET_NAME);
	}
	/**
	 * 指定したキーワードでチャットユーザを検索します。
	 * キーワードは前方一致で、ユーザ名とメールアドレスに対して検索を行います。
	 * キーワードが'*'の場合、全てのユーザを返します。
	 * 検索結果が存在しない場合は空のListを返します。
	 * 
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	public static List<ChatUser> searchByKeyword(String keyword) throws Exception {
		KiiQuery query = null;
		if (TextUtils.equals("*", keyword)) {
			query = new KiiQuery();
		} else {
			query = new KiiQuery(
				KiiClause.or(
					KiiClause.startsWith(FIELD_USERNAME, keyword),
					KiiClause.startsWith(FIELD_EMAIL, keyword)
				)
			);
		}
		List<ChatUser> users = new ArrayList<ChatUser>();
		List<KiiObject> objects = getBucket().query(query).getResult();
		for (KiiObject object : objects) {
			users.add(new ChatUser(object));
		}
		return users;
	}
	/**
	 * 指定したURIでチャットユーザを検索します。
	 * チャットユーザが存在しない場合、nullを返します。
	 * 
	 * @param uri
	 * @return
	 * @throws Exception
	 */
	public static ChatUser findByUri(Uri uri) throws Exception {
		KiiQuery query = new KiiQuery(KiiClause.equals(FIELD_URI, uri.toString()));
		List<KiiObject> objects = getBucket().query(query).getResult();
		if (objects.size() == 0) {
			return null;
		} else if (objects.size() == 1) {
			return new ChatUser(objects.get(0));
		} else {
			throw new RuntimeException("too many rows ChatUser uri=" + uri.toString());
		}
	}
	/**
	 * 与えられたJSONObjectからユーザ名を取得します。
	 * 
	 * @param json
	 * @return
	 */
	public static String getUsername(JSONObject json) {
		try {
			return json.getString(FIELD_USERNAME);
		} catch (JSONException e) {
			return "";
		}
	}
	/**
	 * 与えられたJSONObjectからメールアドレスを取得します。
	 * 
	 * @param json
	 * @return
	 */
	public static String getEmail(JSONObject json) {
		try {
			return json.getString(FIELD_EMAIL);
		} catch (JSONException e) {
			return "";
		}
	}
	/**
	 * 与えられたJSONObjectからURIを取得します。
	 * 
	 * @param json
	 * @return
	 */
	public static String getUri(JSONObject json) {
		try {
			return json.getString(FIELD_URI);
		} catch (JSONException e) {
			return "";
		}
	}
	
	public ChatUser() {
		super(getBucket().object());
	}
	public ChatUser(KiiObject user) {
		super(user);
	}
	public ChatUser(String uri, String username, String email) {
		this();
		this.setUri(uri);
		this.setUsername(username);
		this.setEmail(email);
	}
	@Override
	public String getUsername() {
		return getString(FIELD_USERNAME);
	}
	public void setUsername(String username) {
		set(FIELD_USERNAME, username);
	}
	@Override
	public String getEmail() {
		return getString(FIELD_EMAIL);
	}
	public void setEmail(String email) {
		set(FIELD_EMAIL, email);
	}
	@Override
	public String getUri() {
		return getString(FIELD_URI);
	}
	public void setUri(String uri) {
		set(FIELD_URI, uri);
	}
}
