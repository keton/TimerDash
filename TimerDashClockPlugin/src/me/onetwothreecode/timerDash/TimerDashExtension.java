
package me.onetwothreecode.timerDash;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;


public class TimerDashExtension extends DashClockExtension {
    private static final String TAG = "TimerDashExtension";

    public static final String PREF_NAME = "pref_name";
    private BroadcastReceiver messageReciever;
    
    public enum TimerNotificationType {
    	  ALARM("stat_notify_alarm"),
    	  TIMER("stat_notify_timer"),
    	  STOPWATCH("ic_tab_stopwatch_activated"),
    	  UNKNOWN("");

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
    public void onCreate()
    {
    	super.onCreate();
    	Log.d(TAG,"Service created");
    	Intent i=new Intent();
    	i.setClass(getApplicationContext(), NLService.class);
    	this.startService(i);
    	
    	messageReciever=new NotificationReceiver();
    	
    	registerReceiver(messageReciever,
    		      new IntentFilter(NLService.INTENT_NOTIFY_EXTENSION));
    	//setUpdateWhenScreenOn(true); 
    }
    @Override
    protected void onInitialize(boolean isReconnect)
    {
    	this.onUpdateData(UPDATE_REASON_INITIAL);	
    }
    
    @Override
    protected void onUpdateData(int reason) {
       
        
        Log.d(TAG,"Sending update request");
        Intent i = new Intent(NLService.INTENT_NOTIFY_SERVICE);
        i.putExtra("command","list");
        sendBroadcast(i);
		
		
    }
    
    @Override
	public void onDestroy() {
      // Unregister since the activity is about to be closed.
      unregisterReceiver(messageReciever);
      Intent i=new Intent();
  	i.setClass(getApplicationContext(), NLService.class);
  	this.stopService(i);
      super.onDestroy();
    }
    
    class NotificationReceiver extends BroadcastReceiver{

    	
    	
        @Override
        public void onReceive(Context context, Intent intent) 
        {
           	
        	ExtensionData update=new ExtensionData()
            .visible(intent.getBooleanExtra("clockVisible",false))
            
            .status(intent.getStringExtra("clockTitle"))
          //  .expandedTitle(intent.getStringExtra("clockTitle") + " "+intent.getStringExtra("clockStatus") )
            .expandedBody(intent.getStringExtra("clockStatus"));
        	
        	String iconResName=intent.getStringExtra("clockIconName");
        	
        	if (iconResName!=null) switch(TimerDashExtension.TimerNotificationType.fromString(iconResName))
        	{
        	case ALARM:
        		update.icon(R.drawable.ic_alarm);
        		Log.d(TAG,"Alarm notification");
        		break;
        	case TIMER:
        		update.icon(R.drawable.ic_tab_timer_activated);
        		Log.d(TAG,"timer notification");
        		break;
        	case STOPWATCH: //this notification doesn't return any data
        		update.icon(R.drawable.ic_tab_stopwatch_activated);
        		update.visible(false);
        		Log.d(TAG,"stopwatch notification");
        		break;
        		
        	default:
        		update.icon(R.drawable.ic_tab_timer_activated);
        		Log.d(TAG,"default notification");
        		break;
        	}
        		
        	
            publishUpdate(update);
        }
    }
}
