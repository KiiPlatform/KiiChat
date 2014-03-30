package com.kii.sample.chat.ui;

import java.util.List;

import com.kii.sample.chat.KiiChatApplication;
import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatFriend;
import com.kii.sample.chat.model.ChatUser;
import com.kii.sample.chat.ui.ConfirmAddFriendDialogFragment.OnAddFriendListener;
import com.kii.sample.chat.ui.adapter.UserListAdapter;
import com.kii.sample.chat.ui.loader.UserListLoader;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class AddFriendActivity extends ActionBarActivity implements LoaderCallbacks<List<ChatUser>>, OnItemClickListener, OnAddFriendListener {
	
	private static final String ARGS_KEYWORD = "keyword";
	private ListView listView;
	private TextView textEmpty;
	private ProgressBar progressBar;
	private UserListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend);
		this.listView = (ListView)findViewById(R.id.list_view);
		this.listView.setItemsCanFocus(false);
		this.adapter = new UserListAdapter(this);
		this.listView.setAdapter(this.adapter);
		this.listView.setOnItemClickListener(this);
		this.textEmpty = (TextView)findViewById(R.id.text_empty);
		this.progressBar = (ProgressBar)findViewById(R.id.progress_bar);
		this.progressBar.setVisibility(View.GONE);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_friend_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.menu_search_user);
		SearchView searchView = (SearchView)MenuItemCompat.getActionView(searchItem);
		searchView.setQueryHint(KiiChatApplication.getMessage(R.string.hint_search_user));
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				listView.setVisibility(View.GONE);
				textEmpty.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
				Bundle bundle = new Bundle();
				bundle.putString(ARGS_KEYWORD, query.trim());
				getSupportLoaderManager().restartLoader(0, bundle, AddFriendActivity.this);
				return false;
			}
			@Override
			public boolean onQueryTextChange(String query) {
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public Loader<List<ChatUser>> onCreateLoader(int id, Bundle bundle) {
		return new UserListLoader(this, bundle.getString(ARGS_KEYWORD));
	}
	@Override
	public void onLoadFinished(Loader<List<ChatUser>> loader, List<ChatUser> data) {
		this.progressBar.setVisibility(View.GONE);
		if (data == null || data.size() == 0) {
			this.listView.setVisibility(View.GONE);
			this.textEmpty.setVisibility(View.VISIBLE);
		} else {
			this.listView.setVisibility(View.VISIBLE);
			this.textEmpty.setVisibility(View.GONE);
			this.adapter.setData(data);
			this.listView.setAdapter(this.adapter);
		}
	}
	@Override
	public void onLoaderReset(Loader<List<ChatUser>> loader) {
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ChatUser user = (ChatUser)parent.getItemAtPosition(position);
		ConfirmAddFriendDialogFragment dialog = ConfirmAddFriendDialogFragment.newInstance(this, user.getKiiObject().toJSON(), position);
		dialog.show(getSupportFragmentManager(), "confirmFriendDialog");
	}
	@Override
	public void onFriendAdded(int position) {
		ChatUser chatUser = (ChatUser)this.listView.getItemAtPosition(position);
		new AddingFriendTask(chatUser).execute();
	}
	private class AddingFriendTask extends AsyncTask<Void, Void, Boolean> {
		private final ChatUser chatUser;
		
		private AddingFriendTask(ChatUser chatUser) {
			this.chatUser = chatUser;
		}
		
		@Override
		protected void onPreExecute() {
			SimpleProgressDialogFragment.show(getSupportFragmentManager(), "Adding Friend", "Processing...");
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				ChatFriend friend = new ChatFriend(this.chatUser);
				friend.getKiiObject().save();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		@Override
		protected void onPostExecute(Boolean result) {
			SimpleProgressDialogFragment.hide(getSupportFragmentManager());
			if (result) {
				setResult(RESULT_OK);
			} else {
				ToastUtils.showShort(AddFriendActivity.this, "Unable to add friend");
				setResult(RESULT_CANCELED);
			}
			finish();
		}
	}
}
