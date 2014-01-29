package me.onetwothreecode.timerDash;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NLService extends NotificationListenerService {

	private String TAG = this.getClass().getSimpleName();
	private NLServiceReceiver nlservicereciver;
	public static final String INTENT_NOTIFY_SERVICE="me.onetwothreecode.timerDash.NOTIFICATION_LISTENER_SERVICE";
	public static final String INTENT_NOTIFY_EXTENSION="me.onetwothreecode.timerDash.NOTIFICATION_EXTENSION_LISTENER";
	public static final String INTENT_LAUNCH_CLOCK="me.onetwothreecode.timerDash.LAUNCH_CLOCK_RECIEVER";
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		nlservicereciver = new NLServiceReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(NLService.INTENT_NOTIFY_SERVICE);
		registerReceiver(nlservicereciver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(nlservicereciver);
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {

		if (sbn.getPackageName().equalsIgnoreCase("com.android.deskclock")) {
			
			Notification notification = sbn.getNotification();
			String notifText = notification.extras
					.getString(Notification.EXTRA_TEXT);
			String notifTitle = notification.extras
					.getString(Notification.EXTRA_TITLE);

			int iconResId = notification.extras
					.getInt(Notification.EXTRA_SMALL_ICON);

			Log.d(TAG, "New clock notification: " + notifTitle + ": "
					+ notifText);
			Log.d(TAG, "New clock ticker: " + sbn.getNotification().tickerText);

			Resources resources;
			String iconName = new String();
			try {
				PackageManager manager = getPackageManager();
				resources = manager
						.getResourcesForApplication("com.android.deskclock");

				iconName = resources.getResourceEntryName(iconResId);
				Log.d(TAG, "Clock icon name: " + iconName);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Clock icon resource id " + iconResId + " not found");
			}

			if (TimerDashExtension.TimerNotificationType.fromString(iconName) != TimerDashExtension.TimerNotificationType.STOPWATCH) {
				Intent i = new Intent(
						NLService.INTENT_NOTIFY_EXTENSION);
				
				//i.putExtra("notification_event", "Clock notification :"+ notifTitle + ": " + notifText + "\n");
				i.putExtra("clockTitle", notifTitle);
				i.putExtra("clockStatus", notifText);
				i.putExtra("clockVisible", true);
				i.putExtra("clockIconName", iconName);
				i.putExtra("clockNotification", notification);
				
				
				sendBroadcast(i);
			}
		}

	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {

		if (sbn.getPackageName().equalsIgnoreCase("com.android.deskclock")) {
			

			Resources resources;
			String iconName = new String();
			int iconResId = sbn.getNotification().extras
					.getInt(Notification.EXTRA_SMALL_ICON);
			try {
				PackageManager manager = getPackageManager();
				resources = manager
						.getResourcesForApplication("com.android.deskclock");

				iconName = resources.getResourceEntryName(iconResId);
				Log.d(TAG, "Clock icon name: " + iconName);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Clock icon resource id " + iconResId + " not found");
			}

			if (TimerDashExtension.TimerNotificationType.fromString(iconName) != TimerDashExtension.TimerNotificationType.STOPWATCH) 
			{
				nlservicereciver.hideClockNotification();
				// handle multiple clock notifications
				for (StatusBarNotification sbn2 : NLService.this
						.getActiveNotifications()) {
					if (sbn2.getPackageName().equalsIgnoreCase(
							"com.android.deskclock")) {
						NLService.this.onNotificationPosted(sbn2);
					}

				}
			}
			
		}

	}

	class NLServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getStringExtra("command").equals("list")) {
				boolean hasClockNotifications=false;
				try {
					for (StatusBarNotification sbn : NLService.this
							.getActiveNotifications())

						if (sbn.getPackageName().equalsIgnoreCase(
								"com.android.deskclock")) {
							hasClockNotifications=true;
							NLService.this.onNotificationPosted(sbn);
						}
					
					if (!hasClockNotifications) this.hideClockNotification();
				} catch (Exception e) {
					showSecurityError();
				}
				
			}

		}

		public void showSecurityError() {
			Intent i = new Intent(NLService.INTENT_NOTIFY_EXTENSION);

			i.putExtra("clockTitle", NLService.this.getResources().getString(R.string.security_error_title));
			i.putExtra("clockStatus",
					NLService.this.getResources().getString(R.string.security_error_body));
			i.putExtra("clockVisible", true);
			i.putExtra("clockIconName", "ic_error_icon");
			sendBroadcast(i);
		}
		public void hideClockNotification()
		{
			Intent i = new Intent(
					NLService.INTENT_NOTIFY_EXTENSION);

			i.putExtra("clockTitle", "Paused");
			i.putExtra("clockStatus", "");
			i.putExtra("clockVisible", false);
			sendBroadcast(i);
		}
	}
	
}
