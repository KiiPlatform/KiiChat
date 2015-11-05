package com.kii.sample.chat.ui;

import java.util.HashMap;
import java.util.Map;

import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.R;

import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

/**
 * Main screen of chat application.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatMainActivity extends ActionBarActivity implements OnTabChangeListener {
	
	public enum Tab {
		USER,
		CHAT;
	}
	private TabHost tabHost;
	private TabInfo currentTabInfo;
	private Map<String, TabInfo> tabInfoMap = new HashMap<String, ChatMainActivity.TabInfo>();
	private final BroadcastReceiver handleNewChatReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.setupTabs();
	}
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(this.handleNewChatReceiver, new IntentFilter(ApplicationConst.ACTION_CHAT_STARTED));
	}
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(this.handleNewChatReceiver);
	}
	@Override
	public void onBackPressed() {
		moveTaskToBack(true);
	}
	private void setupTabs() {
		this.tabHost = (TabHost)findViewById(android.R.id.tabhost);
		this.tabHost.setup();
		this.tabHost.addTab(newTab(Tab.USER, R.string.tab_text_friends, R.drawable.tab_user));
		this.tabHost.addTab(newTab(Tab.CHAT, R.string.tab_text_chat, R.drawable.tab_chat));
		
		this.clearTab();
		if (!this.tabInfoMap.containsKey(Tab.USER.name())) {
			this.tabInfoMap.put(Tab.USER.name(), new TabInfo());
		}
		if (!this.tabInfoMap.containsKey(Tab.CHAT.name())) {
			this.tabInfoMap.put(Tab.CHAT.name(), new TabInfo());
		}
		this.tabHost.getTabWidget().setStripEnabled(true);
		this.tabHost.setOnTabChangedListener(this);
		
		TabInfo newTab = this.tabInfoMap.get(Tab.USER.name());
		this.currentTabInfo = newTab;
		newTab.fragment = FriendListFragment.newInstance();
		getSupportFragmentManager()
			.beginTransaction()
			.add(R.id.tab_real_content, newTab.fragment, Tab.USER.name())
			.commit();
	}
	private TabSpec newTab(Tab tab, int labelId, int iconId) {
		View indicator = LayoutInflater.from(this).inflate(R.layout.tab, null);
		((TextView)indicator.findViewById(R.id.tab_item_text)).setText(labelId);
		((TextView)indicator.findViewById(R.id.tab_item_text)).setCompoundDrawablesWithIntrinsicBounds(0, iconId, 0, 0);

		TabSpec tabSpec = this.tabHost.newTabSpec(tab.name());
		tabSpec.setIndicator(indicator);
		tabSpec.setContent(new DummyTabFactory(this));
		return tabSpec;
	}
	private void clearTab() {
		for (Tab tab : Tab.values()) {
			try {
				Fragment f = getSupportFragmentManager().findFragmentByTag(tab.name());
				if (f != null) {
					getSupportFragmentManager()
						.beginTransaction()
						.remove(f)
						.commit();
				}
			} catch (Exception ignore) {
			}
		}
		try {
			getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		} catch (Exception ignore) {
		}
	}
	private static class TabInfo {
		private Fragment fragment;
		private TabInfo() {
		}
	}
	private static class DummyTabFactory implements TabContentFactory {
		private final Context context;
		private DummyTabFactory(Context context) {
			this.context = context;
		}
		@Override
		public View createTabContent(String tag) {
			return new View(this.context);
		}
	}
	@Override
	public void onTabChanged(String tabId) {
		TabInfo newTab = this.tabInfoMap.get(tabId);
		if (this.currentTabInfo != newTab) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			if (this.currentTabInfo != null) {
				ft.detach(this.currentTabInfo.fragment);
			}
			if (newTab.fragment == null) {
				if (Tab.USER == Tab.valueOf(tabId)) {
					newTab.fragment = FriendListFragment.newInstance();
				} else if (Tab.CHAT == Tab.valueOf(tabId)) {
					newTab.fragment = ChatListFragment.newInstance();
				}
				ft.add(R.id.tab_real_content, newTab.fragment, tabId);
			} else {
				ft.attach(newTab.fragment);
			}
			this.currentTabInfo = newTab;
			ft.commit();
		}
	}
}
