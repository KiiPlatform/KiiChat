package com.kii.sample.chat.ui;

import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.List;

import com.kii.sample.chat.R;
import com.kii.sample.chat.model.ChatStamp;
import com.kii.sample.chat.ui.adapter.AbstractArrayAdapter;
import com.kii.sample.chat.ui.loader.ChatStampImageFetcher;
import com.kii.sample.chat.ui.loader.ChatStampListLoader;
import com.kii.sample.chat.ui.util.SimpleProgressDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Prompts the user to select a stamp.
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class SelectStampDialogFragment extends DialogFragment implements LoaderCallbacks<List<ChatStamp>>, OnItemClickListener {
	
	public interface OnSelectStampListener {
		public void onSelectStamp(ChatStamp stamp);
	}
	
	public static SelectStampDialogFragment newInstance(OnSelectStampListener onSelectStampListener) {
		SelectStampDialogFragment dialog = new SelectStampDialogFragment();
		dialog.setOnSelectStampListener(onSelectStampListener);
		return dialog;
	}
	
	private WeakReference<OnSelectStampListener> onSelectStampListener;
	private TextView textEmpty;
	private GridView gridView;
	private ImageButton btnAddStamp;
	private ImageButton btnSortStamp;
	private ChatStampListAdpter adapter;
	private ChatStampImageFetcher imageFetcher;
	private int selectedPopupMenuItem = R.id.menu_sort_by_newly;

	public void setOnSelectStampListener(OnSelectStampListener onSelectStampListener) {
		this.onSelectStampListener = new WeakReference<SelectStampDialogFragment.OnSelectStampListener>(onSelectStampListener);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.imageFetcher = new ChatStampImageFetcher(getActivity());
		this.imageFetcher.setLoadingImage(R.drawable.spinner);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {  
		super.onActivityCreated(savedInstanceState);
		this.adapter = new ChatStampListAdpter(getActivity());
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_select_stamp_dialog, null, false);
		this.textEmpty = (TextView)view.findViewById(R.id.text_empty);
		this.gridView = (GridView)view.findViewById(R.id.grid_stamp);
		this.gridView.setOnItemClickListener(this);
		this.gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					imageFetcher.setPauseWork(true);
				} else {
					imageFetcher.setPauseWork(false);
				}
			}
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		this.btnAddStamp = (ImageButton)view.findViewById(R.id.button_add_stamp);
		this.btnAddStamp.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				getActivity().startActivityForResult(intent, ChatActivity.REQUEST_GET_IMAGE_FROM_GALLERY);
				dismiss();
			}
		});
		this.btnSortStamp = (ImageButton)view.findViewById(R.id.button_sort_stamp);
		this.btnSortStamp.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popup = new PopupMenu(getActivity(), btnSortStamp);
				popup.getMenuInflater().inflate(R.menu.sort_popup_menu, popup.getMenu());
				if (selectedPopupMenuItem == R.id.menu_sort_by_popularity) {
					popup.getMenu().getItem(1).setChecked(true);
				} else {
					popup.getMenu().getItem(0).setChecked(true);
				}
				popup.show();
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(final MenuItem item) {
						selectedPopupMenuItem = item.getItemId();
						new AsyncTask<Void, Void, Comparator<ChatStamp>>() {
							@Override
							protected void onPreExecute() {
								String title = (item.getItemId() == R.id.menu_sort_by_popularity) ? "Sort by popularity" : "Sort by newly";
								SimpleProgressDialogFragment.show(getFragmentManager(), title, "Sorting...");
							}
							@Override
							protected Comparator<ChatStamp> doInBackground(Void... params) {
								if (item.getItemId() == R.id.menu_sort_by_popularity) {
									return ChatStamp.getPopularityComparator();
								}
								return ChatStamp.getNewlyComparator();
							}
							@Override
							protected void onPostExecute(Comparator<ChatStamp> comparator) {
								SimpleProgressDialogFragment.hide(getFragmentManager());
								adapter.sort(comparator);
								adapter.notifyDataSetChanged();
							}
						}.execute();
						return true;
					}
				});
			}
		});
		this.getLoaderManager().initLoader(0, savedInstanceState, this);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setNegativeButton(R.string.button_cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		builder.setView(view);
		return builder.create();
	}
	@Override
	public Loader<List<ChatStamp>> onCreateLoader(int id, Bundle bundle) {
		return new ChatStampListLoader(getActivity());
	}
	@Override
	public void onLoadFinished(Loader<List<ChatStamp>> loader, List<ChatStamp> data) {
		if (data == null || data.size() == 0) {
			this.gridView.setVisibility(View.GONE);
			this.textEmpty.setVisibility(View.VISIBLE);
		} else {
			this.gridView.setVisibility(View.VISIBLE);
			this.textEmpty.setVisibility(View.GONE);
			this.adapter.setData(data);
			this.gridView.setAdapter(this.adapter);
		}
	}
	@Override
	public void onLoaderReset(Loader<List<ChatStamp>> loader) {
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ChatStamp stamp = (ChatStamp)parent.getItemAtPosition(position);
		OnSelectStampListener listener = onSelectStampListener.get();
		if (listener != null && stamp != null) {
			listener.onSelectStamp(stamp);
		}
		dismiss();
	}
	
	private static class ViewHolder {
		ImageView stampImage;
	}
	/**
	 * A adapter class to show the {@link ChatStamp}.
	 */
	private class ChatStampListAdpter extends AbstractArrayAdapter<ChatStamp> {
		
		private final LayoutInflater inflater;
		
		public ChatStampListAdpter(Context context) {
			super(context, R.layout.stamp_image_item);
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = this.inflater.inflate(R.layout.stamp_image_item, parent, false);
				holder = new ViewHolder();
				holder.stampImage = (ImageView)convertView.findViewById(R.id.row_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			ChatStamp stamp = this.getItem(position);
			imageFetcher.fetchStamp(stamp, holder.stampImage);
			return convertView;
		}
	}
}
