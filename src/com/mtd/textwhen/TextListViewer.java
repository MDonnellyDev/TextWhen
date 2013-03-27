package com.mtd.textwhen;

import java.util.ArrayList;
import java.util.List;

import com.mtd.textwhen.R;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TextListViewer extends Activity {
	private static final int MENU_ITEM = Menu.FIRST;
	private Cursor dbCursor;
	private TextDbAdapter db;
	private List<OutgoingText> textList;
	private ArrayAdapter<OutgoingText> adapter;
	private ListView scheduledView;
	private TextView statusView;
	private int CONTEXTMENU_DELETE = 0;
	private int CONTEXTMENU_EDIT = 1;

	@Override
	public void onResume() {
		super.onResume();

		if (db == null) {
			db = new TextDbAdapter(this);
			db.open();
		}
		this.textList = db.getAllEntriesList();
		for (OutgoingText text : this.textList) {
			text.setKey(db.getEntryRow(text));
		}

		this.adapter = new ArrayAdapter<OutgoingText>(this,
				R.layout.outgoingtext, R.id.text_info, textList);

		this.scheduledView.setAdapter(this.adapter);
		this.statusView.setText(this.textList.size() + " pending text"
				+ (this.textList.size() == 1 ? "" : "s"));

		this.adapter.notifyDataSetChanged();
		return;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.textview);
		this.scheduledView = (ListView) findViewById(R.id.scheduledView);
		this.statusView = (TextView) findViewById(R.id.statusView);

		this.registerForContextMenu(this.scheduledView);
		this.scheduledView
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener()

				{
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.add(0, CONTEXTMENU_DELETE, 0,
								R.string.context_delete);
						menu.add(0, CONTEXTMENU_EDIT, 0, R.string.context_edit);
						return;
					}
				});

		this.scheduledView.setAdapter(this.adapter);
		this.scheduledView.setClickable(true);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == CONTEXTMENU_DELETE) {
			OutgoingText text = (OutgoingText) scheduledView.getAdapter()
					.getItem(menuInfo.position);
			adapter.remove(text);

			Intent intent = new Intent(this, AlarmService.class);
			intent.putExtra("recipient", text.getRecipient());
			intent.putExtra("subject", text.getSubject());
			intent.putExtra("body", text.getMessageContent());
			intent.putExtra("date", text.getScheduledDateAsLong());
			intent.putExtra("modified", text.getModifiedDateAsLong());
			intent.putExtra("key", text.getKey());
			intent.putExtra("setAlarm", false);

			startService(intent);

			db.removeEntry(db.getEntryRow(text));

		} else if (item.getItemId() == CONTEXTMENU_EDIT) {
			OutgoingText text = (OutgoingText) scheduledView.getAdapter()
					.getItem(menuInfo.position);

			Intent intent = new Intent(this, TextEditor.class);
			intent.putExtra("update", true);
			intent.putExtra("setAlarm", true);
			intent.putExtra("recipient", text.getRecipient());
			intent.putExtra("subject", text.getSubject());
			intent.putExtra("body", text.getMessageContent());
			intent.putExtra("date", text.getScheduledDateAsLong());
			intent.putExtra("modified", text.getModifiedDateAsLong());
			intent.putExtra("key", text.getKey());

			startActivity(intent);
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		return;
	}

	@Override
	public void finish() {
		super.finish();
		db.close();
	}

	@Override
	public void onPause() {
		super.onPause();
		db.close();
		db = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		int groupId = 0;
		int menuItemId = MENU_ITEM;
		MenuItem menuItem = menu.add(groupId, MENU_ITEM, 0,
				this.getString(R.string.btn_addNew));
		menuItem.setIntent(new Intent(this, TextEditor.class));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent intent = item.getIntent();
		intent.putExtra("update", false);
		intent.putExtra("setAlarm", true);
		startActivity(intent);

		return true;
	}

	public void onDestroy() {
		super.onDestroy();
	}

	public void cancelTextButton(View v) {
		ListView list = (ListView) v.getParent().getParent();
		int itemPosition = list.getPositionForView((RelativeLayout) v
				.getParent());
		cancelByPosition(itemPosition);

	}

	public void cancelByPosition(int itemPosition) {
		OutgoingText text = (OutgoingText) scheduledView.getAdapter().getItem(
				itemPosition);
		adapter.remove(text);

		Intent intent = new Intent(this, AlarmService.class);
		intent.putExtra("recipient", text.getRecipient());
		intent.putExtra("subject", text.getSubject());
		intent.putExtra("body", text.getMessageContent());
		intent.putExtra("date", text.getScheduledDateAsLong());
		intent.putExtra("modified", text.getModifiedDateAsLong());
		intent.putExtra("key", text.getKey());
		intent.putExtra("setAlarm", false);

		startService(intent);

		db.removeEntry(db.getEntryRow(text));
	}

	public void editTextButton(View v) {
		ListView list = (ListView) v.getParent().getParent();
		int itemPosition = list.getPositionForView((RelativeLayout) v
				.getParent());
		editByPosition(itemPosition);

	}

	private void editByPosition(int itemPosition) {
		OutgoingText text = (OutgoingText) scheduledView.getAdapter().getItem(
				itemPosition);

		Intent intent = new Intent(this, TextEditor.class);
		intent.putExtra("update", true);
		intent.putExtra("setAlarm", true);
		intent.putExtra("recipient", text.getRecipient());
		intent.putExtra("subject", text.getSubject());
		intent.putExtra("body", text.getMessageContent());
		intent.putExtra("date", text.getScheduledDateAsLong());
		intent.putExtra("modified", text.getModifiedDateAsLong());
		intent.putExtra("key", text.getKey());

		startActivity(intent);
	}

	public void createTextButton(View v) {
		Intent intent = new Intent(this, TextEditor.class);
		intent.putExtra("update", false);
		intent.putExtra("setAlarm", true);
		startActivity(intent);

	}

}
