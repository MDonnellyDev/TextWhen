package com.mddev.l8text;

import java.util.List;

import com.mddev.l8text.R;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TextListViewer extends Activity {
	private static final int						MENU_ITEM						= Menu.FIRST;
	private Cursor											dbCursor;
	private TextDbAdapter								db;
	private List<OutgoingText>					textList;
	private ArrayAdapter<OutgoingText>	adapter;
	private ListView										scheduledView;
	private TextView										statusView;
	private int													CONTEXTMENU_DELETE	= 0;
	private int													CONTEXTMENU_EDIT		= 1;

	@Override
	public void onResume() {
		super.onResume();

		if (db == null) {
			db = new TextDbAdapter(this);
			db.open();
		}
		textList = db.getAllEntriesList();
		for (OutgoingText text : textList) {
			text.setKey(db.getEntryRow(text));
		}

		adapter = new ArrayAdapter<OutgoingText>(this,
				android.R.layout.simple_list_item_1, textList);

		scheduledView.setAdapter(adapter);
		statusView.setText(getString(R.string.instruction_text) + "\n"
				+ textList.size() + " texts in pending queue.");

		adapter.notifyDataSetChanged();
		return;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.textview);
		scheduledView = (ListView) findViewById(R.id.scheduledView);
		statusView = (TextView) findViewById(R.id.statusView);

		this.registerForContextMenu(scheduledView);
		scheduledView
				.setOnCreateContextMenuListener(new OnCreateContextMenuListener()

				{
					public void onCreateContextMenu(ContextMenu menu, View v,
							ContextMenuInfo menuInfo) {
						menu.add(0, CONTEXTMENU_DELETE, 0, R.string.context_delete);
						menu.add(0, CONTEXTMENU_EDIT, 0, R.string.context_edit);
						return;
					}
				});

		scheduledView.setAdapter(adapter);
		scheduledView.setClickable(true);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getItemId() == CONTEXTMENU_DELETE) {
			OutgoingText text = (OutgoingText) scheduledView.getAdapter().getItem(
					menuInfo.position);
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
			OutgoingText text = (OutgoingText) scheduledView.getAdapter().getItem(
					menuInfo.position);

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

		ActionBar actionBar = this.getActionBar();

		Tab listTab = actionBar.newTab();
		
		TabListener tabListener = new ActionBar.TabListener() {
			
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO unselect?
			}
			
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				// TODO if(listTab) refreshFragment;				
			}
			
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}
		};
		
		listTab.setTabListener(tabListener);

		listTab.setIcon(android.R.drawable.ic_menu_agenda);

		Tab createTab = actionBar.newTab();
		createTab.setTabListener(tabListener);

		createTab.setIcon(android.R.drawable.ic_input_add);

		actionBar.addTab(listTab);
		actionBar.addTab(createTab);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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
}
