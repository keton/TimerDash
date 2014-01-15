package me.onetwothreecode.timerDash;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;


public class MainActivity extends Activity {

	Button securitySettingsButton;
	final String TAG="MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		securitySettingsButton = (Button) findViewById(R.id.permissions_button);
		securitySettingsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(
						"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
				//this way onActivityResult is called
				startActivityForResult(intent,0);
			}
		});

		
	}
	//hide security error notification
	//if user didn't set up service correctly error will reappear
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivityResult");
		Intent i = new Intent(NLService.INTENT_NOTIFY_EXTENSION);

		i.putExtra("clockTitle", "");
		i.putExtra("clockStatus",
				"");
		i.putExtra("clockVisible", false);
		sendBroadcast(i);
		
	}

}
