package com.kii.sample.chat.ui;

import java.lang.ref.WeakReference;

import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatFriend;

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
public class ConfirmStartChatDialogFragment extends DialogFragment {
	
	public interface OnStartChatListener {
		public void onChatStarted(int position);
	}
	
	private static final String ARGS_POSITION = "position";
	private static final String ARGS_USERNAME = "username";
	private static final String ARGS_EMAIL = "email";
	
	public static ConfirmStartChatDialogFragment newInstance(OnStartChatListener onStartChatListener, ChatFriend friend, int position) {
		Bundle args = new Bundle();
		args.putInt(ARGS_POSITION, position);
		args.putString(ARGS_USERNAME, friend.getUsername());
		args.putString(ARGS_EMAIL, friend.getEmail());
		ConfirmStartChatDialogFragment dialog = new ConfirmStartChatDialogFragment(onStartChatListener);
		dialog.setArguments(args);
		return dialog;
	}
	
	private final WeakReference<OnStartChatListener> onStartChatListener;
	private int position;
	private String username;
	private String email;
	private TextView textUsername;
	private TextView textEmail;
	
	private ConfirmStartChatDialogFragment(OnStartChatListener onStartChatListener) {
		this.onStartChatListener = new WeakReference<ConfirmStartChatDialogFragment.OnStartChatListener>(onStartChatListener);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		if (savedInstanceState != null) {
			this.position = savedInstanceState.getInt(ARGS_POSITION);
			this.username = savedInstanceState.getString(ARGS_USERNAME);
			this.email = savedInstanceState.getString(ARGS_EMAIL);
		}
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_confirm_friend, null, false);
		this.textUsername = (TextView)view.findViewById(R.id.text_username);
		this.textUsername.setText(this.username);
		this.textEmail = (TextView)view.findViewById(R.id.text_email);
		this.textEmail.setText(this.email);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Start new chat");
		builder.setPositiveButton(R.string.button_chat, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				OnStartChatListener listener = onStartChatListener.get();
				if (listener != null) {
					listener.onChatStarted(position);
				}
				dismiss();
			}
		});
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.setView(view);
		return builder.create();
	}
}
