package com.pintr.androidapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class Pintr_G_097_24_NetworkLocation extends Activity  implements LocationListener {
	Context context;
	protected LocationManager locationManager;
	public Pintr_24_YelpVenueFinder activity;
	String typeIn ;
	
	Pintr_G_097_24_NetworkLocation(Context x, Pintr_24_YelpVenueFinder a, String type){
		Log.d("G_097_03_NetworkLocation", "FIRING...");
		locationManager = (LocationManager) x.getSystemService(LOCATION_SERVICE);
		context = x;
        this.activity = a;
        typeIn = type;
		Log.d("LOCATION","PRIMING LISTENER..." + type);
	}
	
	@Override
	public void onLocationChanged(Location loc){
		Log.d("LISTENER", "onLocationChanged called");
		activity.listLocat(loc, context, typeIn);
		locationManager.removeUpdates(this);
    }

	@Override
	public void onProviderDisabled(String provider){
		Toast.makeText( getApplicationContext(),"Gps Disabled",	Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onProviderEnabled(String provider){
		Toast.makeText( getApplicationContext(),"Gps Enabled",Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)	{}
}