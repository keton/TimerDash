package me.onetwothreecode.timerDash;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;

public class PendingIntentLauncherActivity extends Activity {

	final String TAG = "PendingIntentLauncherActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"activity Launched!");
		Intent i = new Intent(NLService.INTENT_LAUNCH_CLOCK);
		
		sendBroadcast(i);
		finish();
	}

}
