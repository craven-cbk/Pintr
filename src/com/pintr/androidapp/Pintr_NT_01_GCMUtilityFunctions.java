package com.pintr.androidapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.UUID;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Pintr_NT_01_GCMUtilityFunctions  {

	static final String TAG = "GCMDemo";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    
	public Pintr_NT_01_GCMUtilityFunctions(){
	}    


	public String getDeviceId(Context context, ContentResolver cr){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	    
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(cr, android.provider.Settings.Secure.ANDROID_ID);

	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    String deviceId = deviceUuid.toString();
	    
	    Log.d("DEVICE ID FOUND:", deviceId);
	    
	    return deviceId ;
	}

	public String getRegistrationId(Context context, SharedPreferences prefs) {
		
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");

		Log.d(TAG, "******  REG ID ONBOARD:  " + registrationId);
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    Log.d(TAG, "registered version = " +  registeredVersion );
	    
	    int currentVersion = 0;
	    	    try {
	    	        PackageInfo packageInfo = context.getPackageManager()
	    	                .getPackageInfo(context.getPackageName(), 0);
	    	        currentVersion = packageInfo.versionCode;
	    	    } catch (NameNotFoundException e) {
	    	        throw new RuntimeException("Could not get package name: " + e);
	    	    };

	    Log.d(TAG, "current version = " +  currentVersion );
	    	    
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	public void storeRegistrationId(Context context
			, String regId
			, SharedPreferences prefs
			) {
	    
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();

        Log.d(TAG , "JUST STORED REGISTRATION ID :  " + regId + " FOR APP VERSION " + appVersion);
	}
    
	public String sendRegToRDB(String Email
								, String pw
								, String regid
								, String DeviceId
								){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String serverResponse = null;
		
		try {
			data = 	URLEncoder.encode("regin", "UTF-8") 
					+ "=" + URLEncoder.encode(regid, "UTF-8")
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(Email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(pw, "UTF-8")
					+ "&" + URLEncoder.encode("DeviceId", "UTF-8") 
					+ "=" + URLEncoder.encode(DeviceId, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/PintrRegDVID.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write( data ); 
			SendToPhp.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;

            while((line = reader.readLine()) != null)
	            {
            		sb.append(line);
            		break;
	            }
            serverResponse = sb.toString();
			}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		return serverResponse;		
	}
	

	public String removeFromRDB(String Email
								, String pw
								, String DeviceId
								){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String serverResponse = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(Email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(pw, "UTF-8")
					+ "&" + URLEncoder.encode("DeviceId", "UTF-8") 
					+ "=" + URLEncoder.encode(DeviceId, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/PintrDeregDVID.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write( data ); 
			SendToPhp.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;

            while((line = reader.readLine()) != null)
	            {
            		sb.append(line);
            		break;
	            }
            serverResponse = sb.toString();
			}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		return serverResponse;		
	}
	
	public static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	

	public String dispatchMessageToServer(
			String regid
			,String email
			,String pw
			,String msg
			,String msg_tp
			){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("regin", "UTF-8") 
					+ "=" + URLEncoder.encode(regid, "UTF-8")
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("pw", "UTF-8") 
					+ "=" + URLEncoder.encode(pw, "UTF-8")
					+ "&" + URLEncoder.encode("msg", "UTF-8") 
					+ "=" + URLEncoder.encode(msg, "UTF-8")
					+ "&" + URLEncoder.encode("msg_tp", "UTF-8") 
					+ "=" + URLEncoder.encode(msg_tp, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/NOTA/NOTA_TRAMAT.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write( data ); 
			SendToPhp.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            // Read Server Response
            while((line = reader.readLine()) != null)
	            {
            		sb.append(line);
            		break;
	            }
            JsonString = sb.toString();
			}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		return JsonString;
	}
	
}