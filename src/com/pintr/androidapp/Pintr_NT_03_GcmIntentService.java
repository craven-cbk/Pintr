package com.pintr.androidapp;

import org.json.JSONArray;
import org.json.JSONException;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


public class Pintr_NT_03_GcmIntentService extends IntentService {
	  
	static final String TAG = "GCMDemo";
	
	
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public Pintr_NT_03_GcmIntentService() {
        super("Pintr_NT_03_GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        
		Log.d(TAG, "******INTENT STARTED");

        if (!extras.isEmpty()) {  
           if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } 
           else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
           } 
           else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                sendNotification(extras.getString ("message"));
                Log.d(TAG, "Received: " + extras.toString());
            }
        }
        Pintr_NT_04_ReceiveGCMMessages.completeWakefulIntent(intent);
        Log.d(TAG, "*****FINISHED INTENT***** " );
    }

    
   
    private void sendNotification(String msg) {

        Log.i(TAG, "INITIALISING NOTIFICATION MANAGER..." );
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
        final Pintr_MSG_001_DownloadMesssageHistory dlmh = new Pintr_MSG_001_DownloadMesssageHistory();
//        final Pintr_G_021_YourEventsReader yevr = new Pintr_G_021_YourEventsReader(this);
    	JSONArray notificationJSON = jsh.JSON_Parser(msg, "notification");
		String notebody = "";
		String msgType = "";
		Intent mainpg = new Intent();
		
        try {
        	msgType = notificationJSON.getJSONObject(0).getString("type");
        	notebody  = notificationJSON.getJSONObject(0).getString("notebody");
		} catch (JSONException e) {
			e.printStackTrace();
		}

        Boolean fireNotification = false;
        if(msgType.equals("ANS") && loadSavedPreferences("AnswersNotificationsOn")){
            mainpg = new Intent(this, Pintr_03_entry_page.class);
            fireNotification = true;
        }	
        else if(msgType.equals("MSG")  && loadSavedPreferences("MessagesNotificationsOn")){
    		dlmh.writeDataFromServerToSQLite(this, "0");
    		dlmh.msgLastReadSrvToSqli(this);
    		mainpg = new Intent(this, Pintr_19_MessagesOverview.class);	
            fireNotification = true;
        }
        else if(msgType.equals("FRQ")&& loadSavedPreferences("FriendRequestsNotificationsOn")){
            mainpg = new Intent(this, Pintr_15_PeoplePage.class);	
            fireNotification = true;
        }	
        else if(msgType.equals("FRA")&& loadSavedPreferences("FriendRequestsNotificationsOn")){
            mainpg = new Intent(this, Pintr_15_PeoplePage.class);
            fireNotification = true;
        }		
         else if(msgType.equals("INV")&& loadSavedPreferences("InvitesNotificationsOn")){
             mainpg = new Intent(this, Pintr_22_EventsOverview.class);    
             fireNotification = true;
         }		  
         
	    if(fireNotification){
	        PendingIntent contentIntent 
		    		= PendingIntent.getActivity(this, 0, mainpg, PendingIntent.FLAG_CANCEL_CURRENT);
		
		    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		    Log.i(TAG, "ASSIGNED INTENTS..." );
		    NotificationCompat.Builder mBuilder =
		            new NotificationCompat.Builder(this)
					        .setContentTitle("PiNTR")
					        .setOnlyAlertOnce(true)
					        .setAutoCancel(true)
					        .setStyle(new NotificationCompat.BigTextStyle()
					        .bigText(notebody))
					        .setContentText(notebody)
					        .setSmallIcon(R.drawable.pintr_logo_glass_small)
					        .setNumber(1)
					        .setTicker("New in PiNTR")
					        .setVibrate(new long[] { 1000, 1000 })
					        .setLights(Color.RED, 1000, 1000)
					        .setSound(alarmSound)
					        ;
		
		    mBuilder.setContentIntent(contentIntent);
		    Log.i(TAG, "SET CONTENTS AND INTENT..." );
		    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
		
		    Log.i(TAG, "RUNNING NOTIFICATION MANAGER" ); 
	    }
    }
    

	 private boolean loadSavedPreferences(String prefName) {
       SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
       boolean checkBoxValue = sharedPreferences.getBoolean(prefName, true);
       Log.d(prefName, Boolean.toString(checkBoxValue));
       return checkBoxValue;
   }
}