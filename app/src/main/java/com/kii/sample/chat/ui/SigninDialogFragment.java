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
import com.kii.cloud.storage.callback.KiiUserCallBack;
import com.kii.sample.chat.PreferencesManager;
import com.kii.sample.chat.R;
import com.kii.sample.chat.ui.task.ChatUserInitializeTask;
import com.kii.sample.chat.ui.task.ChatUserInitializeTask.OnInitializeListener;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;
import com.kii.sample.chat.ui.util.ToastUtils;
import com.kii.sample.chat.util.Logger;

/**
 * Fragment of sign in screen.
 * 
 * @author ryuji.ochi@kii.com
 */
public class SigninDialogFragment extends DialogFragment implements OnClickListener {
	
	public static final String TAG = "SigninDialogFragment";
	
	public static SigninDialogFragment newInstance(OnInitializeListener onSignupListener) {
		return newInstance(onSignupListener, false);
	}
	
	public static SigninDialogFragment newInstance(OnInitializeListener onSignupListener, Boolean remember) {
		SigninDialogFragment dialog = new SigninDialogFragment();
		dialog.setOnSignupListener(onSignupListener);
		dialog.checkRemember = remember;
		return dialog;
	}
	
	private WeakReference<OnInitializeListener> onSignupListener;
	private EditText editEmail;
	private EditText editPassword;
	private Boolean checkRemember = false;
	
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
		View view = inflater.inflate(R.layout.fragment_signin, null, false);
		
		this.editEmail = (EditText) view.findViewById(R.id.edit_email);
		this.editPassword = (EditText) view.findViewById(R.id.edit_password);
		this.editPassword.setTransformationMethod(new PasswordTransformationMethod());
		this.editPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Signin with your email account");
		builder.setPositiveButton(R.string.button_signin, null);
		builder.setNegativeButton(R.string.button_cancel, null);
		builder.setView(view);
		AlertDialog dialog = builder.show();
		Button buttonOK = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		buttonOK.setOnClickListener(this);
		return dialog;
	}
	
	@Override
	public void onClick(View v) {
		final String email = editEmail.getText().toString();
		final String password = editPassword.getText().toString();
		if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
			ToastUtils.showShort(getActivity(), "Please input email address and password");
			return;
		}
		
		SimpleProgressDialogFragment.show(getFragmentManager(), "Signin", "Processing...");
		KiiUser.logIn(new KiiUserCallBack() {
			@Override
			public void onLoginCompleted(int token, KiiUser user, Exception e) {
				if (e != null) {
					Logger.e("Unable to login.", e);
					ToastUtils.showShort(getActivity(), "Unable to login");
					SimpleProgressDialogFragment.hide(getFragmentManager());
					return;
				}
				if (checkRemember) {
					Logger.i(user.getAccessToken());
					PreferencesManager.setStoredAccessToken(user.getAccessToken());
				}
				new PostSigninTask(user.getDisplayname(), user.getEmail()).execute();
			}
		}, email, password);
	}
	
	/**
	 * Does post-initialization process of the user on background thread.
	 */
	private class PostSigninTask extends ChatUserInitializeTask {
		
		private PostSigninTask(String username, String email) {
			super(username, email);
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
				ToastUtils.showShort(getActivity(), "Unable to sign in");
			}
			dismiss();
		}
	}
}
