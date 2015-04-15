package com.pintr.androidapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Pintr_G_099_HeaderMenuManager extends Activity  {

	LinearLayout menuLayout  
				,homeButton  
				,converseButton  
				,friendsButton  
				,messagesButton  
				,eventsButton  
				,lfpButton  
				;
	Context context;
	int colorOn, colorOff, unreadMessages;
	ToggleButton MenuButton;

	Intent homeIntent, settingsIntent, converseIntent, eventsIntent, profilesIntent, messagesIntent, lfpIntent;

	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	Boolean networkAvailable ;
	
	
	public Pintr_G_099_HeaderMenuManager(Context c
			, LinearLayout l
			, LinearLayout la1
			, LinearLayout la2
			, LinearLayout la3
			, LinearLayout la4
			, LinearLayout la5
			, int colorIn
			, int colorOut
			, ToggleButton tgBtn
			, View view
			, int unreadMessagesIn
			, int frReqsCt
			, LinearLayout la6
			){
		context = c;
		
		menuLayout = l;
		homeButton = la1;
		converseButton  = la2;
		friendsButton   = la3;
		messagesButton   = la4;
		eventsButton   = la5;
		lfpButton = la6;
		
		colorOn = colorIn ;
		colorOff = colorOut;

		homeIntent = new Intent(c, Pintr_03_entry_page.class);
		messagesIntent = new Intent(c, Pintr_19_MessagesOverview.class);
		converseIntent = new Intent(c, Pintr_12_QuestionsManager.class);
		eventsIntent = new Intent(c, Pintr_22_EventsOverview.class);
		profilesIntent = new Intent(c, Pintr_15_PeoplePage.class);
		lfpIntent = new Intent(c, Pintr_26_LFP.class);
		
		MenuButton = tgBtn;

		//CHECK FOR NETWORK
		networkAvailable = nw.haveNetworkConnection(context);
		
		//CHECK FOR UNREAD MESSAGES
		Log.d("Unred msg ct:", Integer.toString (unreadMessages ));
		Log.d("Friend Request ct:", Integer.toString (frReqsCt ));
		if(unreadMessagesIn > 0){

			TextView countTV = new TextView(c);
			countTV.setText(Integer.toString (unreadMessages ));
			countTV.setTypeface(null, Typeface.BOLD);
			LinearLayout.LayoutParams lp1;
	        lp1 = new LinearLayout.LayoutParams(
	        		LayoutParams.WRAP_CONTENT,
	        		LayoutParams.WRAP_CONTENT);
	        lp1.setMargins(10, 0, 0, 0);
			messagesButton.addView(countTV, lp1);
			messagesButton.setBackgroundResource(R.color.cthruMessageYellow);
			
		}
		if(frReqsCt > 0){

			TextView countTV = new TextView(c);
			countTV.setText(Integer.toString (frReqsCt ));
			countTV.setTypeface(null, Typeface.BOLD);
			LinearLayout.LayoutParams lp1;
	        lp1 = new LinearLayout.LayoutParams(
	        		LayoutParams.WRAP_CONTENT,
	        		LayoutParams.WRAP_CONTENT);
	        lp1.setMargins(10, 0, 0, 0);
	        friendsButton.addView(countTV, lp1);
	        friendsButton.setBackgroundResource(R.color.cthruMessageYellow);
			
		}

	}
	
	
	OnClickListener homeClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			pressMenuButton(homeButton, homeIntent);
		}
	};
	

	OnClickListener converseClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			if(networkAvailable){
				pressMenuButton(converseButton, converseIntent);
			}else{
				String msgStr = "Please enable your internet connection to view conversations";				
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		 
				alertDialogBuilder.setTitle("Network unavailable");
	 
				// set dialog message
				alertDialogBuilder
					.setMessage(msgStr)
					.setCancelable(true)
					.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,int id) {
							dialog.cancel();
						}
					});
					
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				
			}
		}
	};
	

	OnClickListener interestsClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			pressMenuButton(eventsButton, eventsIntent);
		}
	};
	
	

	OnClickListener friendsClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			pressMenuButton(friendsButton, profilesIntent);	
		}
	};
	

	OnClickListener messagesClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			pressMenuButton(messagesButton, messagesIntent);	
		}
	};

	OnClickListener lfpClick = new View.OnClickListener() {			
		@Override
		public void onClick(View v) {	
			pressMenuButton(lfpButton, lfpIntent);	
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
}
