package com.kii.sample.chat.ui;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.chat.R;
import com.kii.sample.chat.ui.task.ChatUserInitializeTask;
import com.kii.sample.chat.ui.task.ChatUserInitializeTask.OnInitializeListener;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;
import com.kii.sample.chat.util.Logger;

/**
 * Fragment of sign up screen.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class SignupDialogFragment extends DialogFragment implements OnClickListener {
	
	public static SignupDialogFragment newInstance(OnInitializeListener onSignupListener) {
		SignupDialogFragment dialog = new SignupDialogFragment();
		dialog.setOnSignupListener(onSignupListener);
		return dialog;
	}
	
	private WeakReference<OnInitializeListener> onSignupListener;
	private EditText editName;
	private EditText editEmail;
	private EditText editPassword;
	
	private void setOnSignupListener(OnInitializeListener onSignupListener) {
		this.onSignupListener = new WeakReference<OnInitializeListener>(onSignupListener);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {  
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_signup, null, false);
		
		this.editName = (EditText)view.findViewById(R.id.edit_name);
		this.editEmail = (EditText)view.findViewById(R.id.edit_email);
		this.editPassword = (EditText)view.findViewById(R.id.edit_password);
		this.editPassword.setTransformationMethod(new PasswordTransformationMethod());
		this.editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Create new account");
		builder.setPositiveButton(R.string.button_signup, null);
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.setView(view);
		AlertDialog dialog = builder.show();
		Button buttonOK = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		buttonOK.setOnClickListener(this);
		return dialog;
	}
	
	@Override
	public void onClick(View v) {
		final String username = editName.getText().toString();
		final String email = editEmail.getText().toString();
		final String password = editPassword.getText().toString();
		if (TextUtils.isEmpty(username)) {
			ToastUtils.showShort(getActivity(), "Please input username");
			return;
		}
		if (TextUtils.isEmpty(email)) {
			ToastUtils.showShort(getActivity(), "Please input email");
			return;
		}
		if (TextUtils.isEmpty(password)) {
			ToastUtils.showShort(getActivity(), "Please input password");
			return;
		}
		new SignupTask(username, email, password).execute();
	}

	/**
	 * Do sign up on background thread.
	 */
	private class SignupTask extends ChatUserInitializeTask {
		
		private final String username;
		private final String email;
		private final String password;
		
		private SignupTask(String username, String email, String password) {
			super(username, email);
			this.username = username;
			this.email = email;
			this.password = password;
		}
		@Override
		protected void onPreExecute() {
			SimpleProgressDialogFragment.show(getFragmentManager(), "Signup", "Processing...");
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				KiiUser.Builder builder = KiiUser.builderWithEmail(email);
				KiiUser kiiUser = builder.build();
				kiiUser.setDisplayname(username);
				kiiUser.register(password);
				Logger.i("registered user uri=" + kiiUser.toUri().toString());
				return super.doInBackground(params);
			} catch (Exception e) {
				Logger.e("failed to sign up", e);
				return false;
			}
		}
		@Override
		protected void onPostExecute(Boolean result) {
			SimpleProgressDialogFragment.hide(getFragmentManager());
			if (result) {
				// Call callback if process is success
				OnInitializeListener listener = onSignupListener.get();
				if (listener != null) {
					listener.onInitializeCompleted();
				}
			} else {
				ToastUtils.showShort(getActivity(), "Unable to sign up");
			}
			dismiss();
		}
	}
}
