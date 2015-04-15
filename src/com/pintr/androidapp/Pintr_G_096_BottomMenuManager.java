package com.pintr.androidapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Pintr_G_096_BottomMenuManager extends Activity  {


	LinearLayout menuLayout
				,menuContentsLogout
				,menuContentsSettings
				,menuContentsInterests
				;
	Context context;
	final Intent interestsIntent , settingsIntent, logoutIntent ;
	int colorOn, colorOff;
	String devId;
	Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
    
	public Pintr_G_096_BottomMenuManager(
						LinearLayout menuLayoutIn
						, LinearLayout menuContentsLogoutIn
						, LinearLayout menuContentsSettingsIn
						, LinearLayout menuContentsInterestsIn
						, Context contextIn
						, int colorIn
						, int colorOut
						){
		
		
		menuLayout = menuLayoutIn;
		menuContentsLogout = menuContentsLogoutIn;
		menuContentsSettings = menuContentsSettingsIn;
		menuContentsInterests = menuContentsInterestsIn;
		context = contextIn;
		ContentResolver cr = context.getContentResolver();
		devId = gcmuf.getDeviceId(context , cr);
		Log.d("DeviceID", devId);
		
		interestsIntent = new Intent(context, Pintr_10_InterestsOverview.class);
		settingsIntent = new Intent(context, Pintr_08_Settings.class);
		logoutIntent = new Intent(context, PintrMain.class);

		colorOn = colorIn ;
		colorOff = colorOut;
	}

	OnCheckedChangeListener menuCheckList = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (isChecked) 
				menuLayout.setVisibility(View.VISIBLE);
			else 
				menuLayout.setVisibility(View.GONE);
		}
	};
	
	OnClickListener logoutClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			
			final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
			ArrayList<String> Creds = dbq.UserCreds();
			String Lo_1 = Creds.get(0).toString();
			String Lo_2 = Creds.get(1).toString();

			String dbReturn = gcmuf.removeFromRDB( Lo_1
												, Lo_2
												, devId
												);
		    Log.d("LOGOUT RESULT FROM SERVER:", dbReturn);
			killDB();
			pressMenuButton(menuContentsLogout, logoutIntent);
		}
	};
	
	OnClickListener settingsClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			pressMenuButton(menuContentsSettings, settingsIntent);
		}
	};
	
	OnClickListener interestsClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			pressMenuButton(menuContentsInterests, interestsIntent);
		}
	};
	

	public void pressMenuButton(final LinearLayout btn, final Intent i ){
		btn.setBackgroundColor(colorOn);
		Handler handler = new Handler();;  
	    handler.postDelayed(new Runnable() { 
	         @Override
			public void run() { 
		     	btn.setBackgroundColor(colorOff);
	    		((Activity) context).startActivity(i);
	         } 
	    }, 200); 
		
	}

	public void killDB(){

	    String DATABASE_NAME = "Pintr_DB";
		context.deleteDatabase(DATABASE_NAME);

	}
	
}
