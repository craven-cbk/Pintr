package com.pintr.androidapp;

import android.os.AsyncTask;
import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.util.Log;


	
public class Pintr_NT_02_LoadWebPageASYNC extends AsyncTask<String, Void, String> {

	static final String TAG = "GCMDemo";
	GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid, EmailGiven, PasswordGiven, DeviceId ;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 3000;
    String SENDER_ID = "92970889157";

    Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
    
    public Pintr_NT_02_LoadWebPageASYNC (Context contextIn
    									, SharedPreferences prefsIn
    									, String Email
    									, String Password
    									, String DeviceIdIn
    									){
		context = contextIn;
		prefs = prefsIn;
		EmailGiven = Email;
		PasswordGiven = Password;
		DeviceId = DeviceIdIn;
	}
	
    
	@Override
	protected String doInBackground(String... params) {
        String msg = "";
       
        //EXPONENTIAL BACKOFF FOR DEVICE REGISTRATION - TRY 3 PINGS, STOP IF TRPLE NULL, BREAK IF GOOD. 
        int backoff = PLAY_SERVICES_RESOLUTION_REQUEST;
        for (int i = 1; i <= 3; i++) {
            try {
	            if (gcm == null) {
	                gcm = GoogleCloudMessaging.getInstance(context);
	            }
	            regid = gcm.register(SENDER_ID);
	            msg = regid;

	            if (regid != null){
	            	gcmuf.storeRegistrationId(
		            			context
		            			, regid
		            			, prefs
		            			);

	            	Log.d(" REGISTER DEVICE WITH FOLLOWING PARMS:  ", EmailGiven 
	            			+ "->" + PasswordGiven 
	            			+ "->" + regid 
	            			+ "->" + DeviceId );
	            	String registration = 
	            			gcmuf.sendRegToRDB(
		            			EmailGiven
		            			, PasswordGiven
		            			, regid
		            			, DeviceId 
		            			);
	            	Log.d("DEVICE REGISTRATION - SQL RESULTS:  ", registration );
	            	break;
	            }
	            
	        } catch (IOException ex) {
	        	
	        	msg = "Error :" + ex.getMessage();
	        	try {
	                Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
	                Thread.sleep(backoff);
	            } catch (InterruptedException e1) {
	                Log.d(TAG, "Thread interrupted: abort remaining retries!");
	                Thread.currentThread().interrupt();
	            }
	            backoff *= 2;
	        }
              
        }
        return msg;
	}

//    @Override
//    protected void onPostExecute(String msg) {
//    	Log.d(TAG, "REGISTERED WITH GCM SERVER  ");
//    }
    
    
}