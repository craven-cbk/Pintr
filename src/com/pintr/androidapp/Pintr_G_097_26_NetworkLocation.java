package com.pintr.androidapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class Pintr_G_097_26_NetworkLocation extends Activity  implements LocationListener {
	Context context;
	protected LocationManager locationManager;
	public Pintr_26_LFP activity;
	Boolean continueToActivity;
	String function, edittextDescription;
	
	Pintr_G_097_26_NetworkLocation(	Context x
									, Pintr_26_LFP a
									, Boolean continueToActivityIn
									, String functionIn
									, String edittextDescriptionIn
									){
		Log.d("G_097_26_NetworkLocation", "FIRING...");
		locationManager = (LocationManager) x.getSystemService(LOCATION_SERVICE);
		context = x;
        this.activity = a;
        function = functionIn;
        continueToActivity = continueToActivityIn;
        edittextDescription = edittextDescriptionIn;
	}
	
	@Override
	public void onLocationChanged(Location loc){
		Log.d("LISTENER", "onLocationChanged called");
		activity.listLocat(loc, context, continueToActivity, function, edittextDescription);
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