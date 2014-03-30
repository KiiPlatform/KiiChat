package com.kii.sample.chat.ui;

import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatUser;
import com.kii.sample.chat.util.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ConfirmAddFriendDialogFragment extends DialogFragment {
	
	public interface OnAddFriendListener {
		public void onFriendAdded(int position);
	}
	
	private static final String ARGS_POSITION = "position";
	private static final String ARGS_USER_JSON = "user_json";
	
	public static ConfirmAddFriendDialogFragment newInstance(OnAddFriendListener onAddFriendListener, JSONObject user, int position) {
		Bundle args = new Bundle();
		args.putString(ARGS_USER_JSON, user.toString());
		args.putInt(ARGS_POSITION, position);
		ConfirmAddFriendDialogFragment dialog = new ConfirmAddFriendDialogFragment(onAddFriendListener);
		dialog.setArguments(args);
		return dialog;
	}
	
	private final WeakReference<OnAddFriendListener> onAddFriendListener;
	private JSONObject user;
	private int position;
	private TextView textUsername;
	private TextView textEmail;
	
	private ConfirmAddFriendDialogFragment(OnAddFriendListener onAddFriendListener) {
		this.onAddFriendListener = new WeakReference<ConfirmAddFriendDialogFragment.OnAddFriendListener>(onAddFriendListener);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(ARGS_USER_JSON)) {
				try {
					this.position = savedInstanceState.getInt(ARGS_POSITION);
					this.user = new JSONObject(savedInstanceState.getString(ARGS_USER_JSON));
				} catch (JSONException ignore) {
					Logger.e("invalid argument", ignore);
				}
			}
		}
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_confirm_friend, null, false);
		this.textUsername = (TextView)view.findViewById(R.id.text_username);
		this.textUsername.setText(ChatUser.getUsername(this.user));
		this.textEmail = (TextView)view.findViewById(R.id.text_email);
		this.textEmail.setText(ChatUser.getEmail(this.user));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Add new friend");
		builder.setPositiveButton(R.string.button_add, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				OnAddFriendListener listener = onAddFriendListener.get();
				if (listener != null) {
					listener.onFriendAdded(position);
				}
				dismiss();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.setView(view);
		return builder.create();
	}

}
