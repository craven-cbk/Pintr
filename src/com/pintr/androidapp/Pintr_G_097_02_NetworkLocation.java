package com.pintr.androidapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class Pintr_G_097_02_NetworkLocation extends Activity  implements LocationListener {
	Context context;
	protected LocationManager locationManager;
	public Pintr_02_registration_page activity;
	
	Pintr_G_097_02_NetworkLocation(Context x, Pintr_02_registration_page a){
		locationManager = (LocationManager) x.getSystemService(LOCATION_SERVICE);
		context = x;
        this.activity = a;
	}
	
	@Override
	public void onLocationChanged(Location loc){
		Log.d("LISTENER", "onLocationChanged called");
		activity.listLocat(loc, context);
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