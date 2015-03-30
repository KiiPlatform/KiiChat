package com.kii.sample.chat.model;

import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.exception.IllegalKiiBaseObjectFormatException;

/**
 * Common base class for Domain classes.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public abstract class KiiObjectWrapper {
	
	// Following fields are reserved fields.
	// You can use these fields as a query condition or sort condition.
	public static final String FIELD_ID = "_id";
	public static final String FIELD_CREATED = "_created";
	public static final String FIELD_MODIFIED = "_modified";
	
	protected final KiiObject kiiObject;
	
	public KiiObjectWrapper(KiiObject object) {
		this.kiiObject = object;
	}
	
	protected void set(String key, String value) {
		this.kiiObject.set(key, value);
	}
	protected void remove(String key) {
		this.kiiObject.remove(key);
	}
	protected String getString(String key) {
		try {
			return this.kiiObject.getString(key);
		} catch (IllegalKiiBaseObjectFormatException e) {
			return null;
		}
	}
	protected Boolean getBoolean(String key) {
		try {
			return this.kiiObject.getBoolean(key);
		} catch (IllegalKiiBaseObjectFormatException e) {
			return null;
		}
	}
	public KiiObject getKiiObject() {
		return this.kiiObject;
	}
	public long getCreatedTime() {
		return this.kiiObject.getCreatedTime();
	}
	public long getModifedTime() {
		return this.kiiObject.getModifedTime();
	}
	@Override
	public String toString() {
		return this.kiiObject.toString();
	}
}
