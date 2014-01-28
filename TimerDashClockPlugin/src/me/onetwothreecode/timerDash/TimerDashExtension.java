package me.onetwothreecode.timerDash;

import android.app.Notification;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class TimerDashExtension extends DashClockExtension {
	private static final String TAG = "TimerDashExtension";

	//BroadcastReciever for handling of notifications
	private BroadcastReceiver messageReciever;
	//BroadcastReciever that handles pendingIntent launches
	private BroadcastReceiver clockReciever;
	
	// findClockImplementation() variables
	// is true when we know what clock implementation user has
	boolean foundClockImpl = false;
	// points to alarm clock implementation
	Intent alarmClockIntent;
	//reference to current timer notification
	Notification clockNotification=null;
	
	public enum TimerNotificationType {
		ALARM("stat_notify_alarm"), TIMER("stat_notify_timer"), STOPWATCH(
				"ic_tab_stopwatch_activated"), ERROR("ic_error_icon"), UNKNOWN(
				"");

		private String text;

		TimerNotificationType(String text) {
			this.text = text;
		}

		public String getText() {
			return this.text;
		}

		public static TimerNotificationType fromString(String text) {
			if (text != null) {
				for (TimerNotificationType b : TimerNotificationType.values()) {
					if (text.equalsIgnoreCase(b.text)) {
						return b;
					}
				}
			}
			return UNKNOWN;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "Service created");
		// start notification listener service
		this.startService(new Intent(getApplicationContext(), NLService.class));

		// determine what clock implemetation user has
		findClockImplementation();
		
		//register broadcast recievers
		messageReciever = new NotificationReceiver();

		registerReceiver(messageReciever, new IntentFilter(
				NLService.INTENT_NOTIFY_EXTENSION));
		clockReciever=new ClockLaunchReceiver();
		registerReceiver(clockReciever, new IntentFilter(
				NLService.INTENT_LAUNCH_CLOCK));
		
	}

	@Override
	protected void onInitialize(boolean isReconnect) {
		this.onUpdateData(UPDATE_REASON_INITIAL);
	}

	@Override
	protected void onUpdateData(int reason) {

		Log.d(TAG, "Sending update request");
		Intent i = new Intent(NLService.INTENT_NOTIFY_SERVICE);
		i.putExtra("command", "list");
		sendBroadcast(i);

	}

	@Override
	public void onDestroy() {
		// Unregister since the activity is about to be closed.
		unregisterReceiver(messageReciever);
		unregisterReceiver(clockReciever);
		Intent i = new Intent();
		i.setClass(getApplicationContext(), NLService.class);
		this.stopService(i);
		super.onDestroy();
	}

	class ClockLaunchReceiver extends BroadcastReceiver {
		
		//executed when user clicks on extension
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.d(TAG,"ClockLaunchReceiver onReceive()");
			if (clockNotification!=null)
				try {
					clockNotification.contentIntent.send();
				} catch (CanceledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(TAG,"PendingIntent launch error: "+e.getMessage());
				}
		}}
	
	class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			ExtensionData update = new ExtensionData()
					.visible(intent.getBooleanExtra("clockVisible", false))

					.status(intent.getStringExtra("clockTitle"))
					.expandedBody(intent.getStringExtra("clockStatus"));

			String iconResName = intent.getStringExtra("clockIconName");
			
			
			//check if new clock notification was posted, store reference
			if (intent.hasExtra("clockNotification")) 
			{
			Log.d(TAG, "clockNotification detected");
			clockNotification=intent.getParcelableExtra("clockNotification");
			}			
			else clockNotification=null;
			
			if (iconResName != null) {
				
				switch (TimerDashExtension.TimerNotificationType
						.fromString(iconResName))

				{
				case ALARM:
					update.icon(R.drawable.ic_alarm);
					if (clockNotification!=null) update.clickIntent(new Intent(getApplicationContext(),PendingIntentLauncherActivity.class));
					else if (foundClockImpl)
						update.clickIntent(alarmClockIntent);
					Log.d(TAG, "Alarm notification");
					break;
				case TIMER:
					update.icon(R.drawable.ic_tab_timer_activated);
					if (clockNotification!=null) update.clickIntent(new Intent(getApplicationContext(),PendingIntentLauncherActivity.class));
					else if (foundClockImpl)
						update.clickIntent(alarmClockIntent);
					Log.d(TAG, "timer notification");
					break;
				case STOPWATCH: // this notification doesn't return any data
					update.icon(R.drawable.ic_tab_stopwatch_activated);
					update.visible(false);
					Log.d(TAG, "stopwatch notification");
					break;
				case ERROR: // user didn't setup the notification service in
							// system settings
					update.icon(R.drawable.ic_dialog_alert_holo_dark)
							.clickIntent(
									new Intent(TimerDashExtension.this
											.getApplicationContext(),
											MainActivity.class));

					Log.d(TAG, "timer notification");
					break;
				default:
					update.icon(R.drawable.ic_tab_timer_activated);
					Log.d(TAG, "default notification");
					break;
				}
			}

			publishUpdate(update);
		}
	}

	// find what alarm clock user has
	// shameless rip from
	// http://stackoverflow.com/questions/3590955/intent-to-launch-the-clock-application-on-android
	// all credit to Gilles
	void findClockImplementation() {
		PackageManager packageManager = TimerDashExtension.this
				.getPackageManager();
		alarmClockIntent = new Intent(Intent.ACTION_MAIN)
				.addCategory(Intent.CATEGORY_LAUNCHER);

		// Verify clock implementation
		String clockImpls[][] = {
				{ "HTC Alarm Clock", "com.htc.android.worldclock",
						"com.htc.android.worldclock.WorldClockTabControl" },
				{ "Standar Alarm Clock", "com.android.deskclock",
						"com.android.deskclock.AlarmClock" },
				{ "Froyo Nexus Alarm Clock", "com.google.android.deskclock",
						"com.android.deskclock.DeskClock" },
				{ "Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",
						"com.motorola.blur.alarmclock.AlarmClock" },
				{ "Galaxy Nexus Alarm Clock", "com.google.android.deskclock",
						"com.android.deskclock.AlarmClock" },
				{ "Samsung Galaxy Clock", "com.sec.android.app.clockpackage",
						"com.sec.android.app.clockpackage.ClockPackage" } };

		for (int i = 0; i < clockImpls.length; i++) {
			String vendor = clockImpls[i][0];
			String packageName = clockImpls[i][1];
			String className = clockImpls[i][2];
			try {
				ComponentName cn = new ComponentName(packageName, className);
				@SuppressWarnings("unused")
				ActivityInfo aInfo = packageManager.getActivityInfo(cn,
						PackageManager.GET_META_DATA);
				alarmClockIntent.setComponent(cn);
				Log.d(TAG, "Found " + vendor + " --> " + packageName + "/"
						+ className);
				foundClockImpl = true;
			} catch (NameNotFoundException e) {
				Log.d(TAG, vendor + " does not exists");
			}
		}

	}
}
