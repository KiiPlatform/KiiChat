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
 * Represents a user who use this application.
 * This data is saved to KiiCloud as app scope data when user is signed up.
 * This data allow you to search for user.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatUser extends KiiObjectWrapper implements IUser {
	
	private static final String BUCKET_NAME = "chat_users";
	private static final String FIELD_USERNAME = "username";
	private static final String FIELD_EMAIL = "email";
	private static final String FIELD_URI = "uri";
	
	public static KiiBucket getBucket() {
		return Kii.bucket(BUCKET_NAME);
	}
	/**
	 * Prefix search for email and username by keyword.
	 * You can use '*' as wildcard.
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
	 * Finds a user by URI.
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
	 * Retrieves a username from specified JSON.
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
	 * Retrieves a email address from specified JSON.
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
	 * Retrieves a URI from specified JSON.
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
