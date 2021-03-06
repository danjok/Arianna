package fr.eurecom.mobserv.arianna;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

/**
 * 
 * @author uccio
 *
 */
public class TrialActivity extends Activity {

	// TODO I don't know why different TrialActivities
	// are not put on top of the stack keeping on
	// scanning the NFC tag. In fact, if the back button
	// is pressed, the dashboard is suddenly visualized
	
	//TODO Having the app closed and scanning a tag,
	// TrialActivity is started. Try to start dashboard
	// when the back button is pressed, instead of leaving
	// the app
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trial);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//retrieve the intent wich started the activity
		Intent intent = getIntent();
		if (intent != null) {
			// the uri of the POI is stored in the intent
			// constants for intent extra are defined in the dashboard
			String uri = intent.getStringExtra(DashboardActivity.EXTRA_URI);
			if (uri != null) {
				//Toast.makeText(this, uri, Toast.LENGTH_LONG).show();
				final TextView trialText = (TextView) findViewById(R.id.trialText);
				trialText.setText(uri);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_trial, menu);
		return true;
	}
	
	public void poidetail(View w) {
		Intent intent = new Intent(this, PointOfInterestDetail.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

}
