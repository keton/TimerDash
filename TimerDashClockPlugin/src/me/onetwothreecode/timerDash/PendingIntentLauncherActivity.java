package me.onetwothreecode.timerDash;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;

/*
 * DashClock doesn't allow us to execute PendingIntent
 * It also executes onClickIntent without extras so this activity was necessary
 * It is basically a relay to fire TimerDashExtension.ClockLauncReciever.onRecieve()
 * which in turn executes contentIntent from clock notification
 * 
 */

public class PendingIntentLauncherActivity extends Activity {

	final String TAG = "PendingIntentLauncherActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"activity Launched!");
		//notify TimerDashExtension.ClockLauncReciever
		Intent i = new Intent(NLService.INTENT_LAUNCH_CLOCK);
		
		sendBroadcast(i);
		finish();
	}

}
