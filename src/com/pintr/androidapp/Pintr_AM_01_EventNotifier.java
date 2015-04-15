package com.pintr.androidapp;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class Pintr_AM_01_EventNotifier  extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Intent resultIntent = new Intent(context,Pintr_03_entry_page.class);

	    String event = intent.getExtras().getString("event");

	    resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

	    PendingIntent resultPendingIntent 
	    	= PendingIntent.getActivity(context
	    								,0
	    								, resultIntent
	    								, PendingIntent.FLAG_UPDATE_CURRENT
	    								);

	    Notification.Builder mBuilder = new Notification.Builder(context)
										    .setSmallIcon(R.drawable.pintr_logo_glass_small2)
										    .setContentTitle("Event Reminder")
										    .setContentText(event + "  in 1 hour")
										    ;
	    mBuilder.setAutoCancel(true);   
	    mBuilder.setContentIntent(resultPendingIntent);

	    int mNotificationId = 001;
	    NotificationManager mNotifyMgr = (NotificationManager) context
	            .getSystemService(Context.NOTIFICATION_SERVICE);
	    
	    // Builds the notification and issues it.
	    mNotifyMgr.notify(mNotificationId, mBuilder.build());
	}

}
