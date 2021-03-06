package fr.eurecom.mobserv.arianna;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import fr.eurecom.mobserv.arianna.model.Event;
import fr.eurecom.mobserv.arianna.model.Model;
import fr.eurecom.mobserv.arianna.model.PointOfInterest;

// TODO Use a Loader in order to query the db in a different thread
//	than the main UI's one.
public class PointOfInterestList extends BaseDrawerActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar ab = getActionBar();
        ab.setTitle(R.string.title_poi_list);
        ab.setDisplayShowTitleEnabled(true);
		getOverflowMenu();
		ListView listView = (ListView) findViewById(R.id.point_of_interest_list_view);
		Event event = (Event) Model.getByURI(Event.class, "E0",
				getApplicationContext());
		List<PointOfInterest> pois = new ArrayList<PointOfInterest>(event
				.getPois().values());
		Collections.sort(pois);
		ArrayAdapter<PointOfInterest> adapter = new PointOfInterestArrayAdapter(
				this, pois);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				PointOfInterest item = (PointOfInterest) parent.getAdapter()
						.getItem(position);
				Intent intentDetail = new Intent(getApplicationContext(),
						PointOfInterestDetail.class);
				intentDetail.putExtra(DashboardActivity.EXTRA_URI,
						item.getUri());
				intentDetail.putExtra(DashboardActivity.EXTRA_LAUNCHER,
						DashboardActivity.LAUNCHER_POI_LIST);
				intentDetail.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intentDetail);
				//Toast.makeText(getApplicationContext(),
				//		item.getTitle() + " selected", Toast.LENGTH_LONG)
				//		.show();

			}
		});
	}

	/**
	 * method to retrieve elements of action bar to put them in overflow icon
	 */
	public void getOverflowMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_point_of_interest_list, menu);
		return true;
	}

	@Override
	protected int getContentViewResource() {
		return R.layout.activity_point_of_interest_list;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search:
			//Toast.makeText(this, "Tapped search", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_map:
			Intent intent = new Intent(this, MapActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			break;
		case R.id.menu_settings: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_setting);
			builder.setMessage(R.string.msg_setting)
					.setCancelable(false)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// do things
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			break;
		}
		case R.id.menu_help: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.title_help);
			builder.setMessage(R.string.msg_help)
					.setCancelable(false)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// do things
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

}