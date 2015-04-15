package com.pintr.androidapp;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class Pintr_08_Settings extends Activity  {

	Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
    Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_08_settings);

		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable ;
		final Context context = this;
		
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		
		menumanager();
		settingsMenumanager(this);
		networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		RelativeLayout ChangeDetailsView = (RelativeLayout )findViewById(R.id.ChangeDetailsView);
		RelativeLayout  DeregisterView = (RelativeLayout )findViewById(R.id.DeregisterView);
		RelativeLayout  LogoutView = (RelativeLayout )findViewById(R.id.LogoutView);
		RelativeLayout  ChangePassword = (RelativeLayout )findViewById(R.id.ChangePasswordView);
		RelativeLayout  NotificationsView = (RelativeLayout )findViewById(R.id.NotificationsView);

		ImageView idImageViewCross1 = (ImageView )findViewById(R.id.idImageViewCross1);
		ImageView idImageViewCross2 = (ImageView )findViewById(R.id.idImageViewCross2);
		ImageView idImageViewCross3 = (ImageView )findViewById(R.id.idImageViewCross3);
		ImageView idImageViewCross4 = (ImageView )findViewById(R.id.idImageViewCross4);

		//CHECK FOR NETWORK AND ASSIGN SECTION CLICK LISTENERS
		if(networkAvailable){
			ChangeDetailsView.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					
					gotoNextPage(Pintr_07_ChangeDetails.class);
				}
			});	
	
			DeregisterView.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
	
					gotoNextPage(Pintr_06_Deregister_page.class);			
				}
			});	
	
			ChangePassword.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
	
					gotoNextPage(Pintr_09_Credchg_page.class);	
				}
			});	

			NotificationsView.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
	
					gotoNextPage(Pintr_30_NotificationsManager.class);	
				}
			});	
		}
		
		//IF THERES NO NETWORK, DISABLE THEM
		else {
			showCrossIfOffline(idImageViewCross1, this);
			showCrossIfOffline(idImageViewCross2, this);
			showCrossIfOffline(idImageViewCross3, this);
			showCrossIfOffline(idImageViewCross4, this);
		}
		
		LogoutView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

			    
				ArrayList<String> UserCreds = dbq.UserCreds();
				String Lo_1 = UserCreds.get(0);
				String Lo_2 = UserCreds.get(1);
				
			    String dbReturn = gcmuf.removeFromRDB( Lo_1
														, Lo_2
														, gcmuf.getDeviceId(context , getContentResolver())
														);
			    Log.d("dbReturn ", dbReturn );
				killDB();
				gotoNextPage(PintrMain.class);	
			}
		});	

		
	}
	
	public void showCrossIfOffline(ImageView imgView, final Context context){
		
		imgView.setImageResource(R.drawable.red_x);
	        
		imgView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				String msgStr = "Please enable your internet connection to change";				
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
		});	
	}
	
	public void gotoNextPage(Class<?> classIn){

		startActivity(new Intent(this, classIn));
	}
	
	public void killDB(){

	    String DATABASE_NAME = "Pintr_DB";
		this.deleteDatabase(DATABASE_NAME);

	}
		


//	CALL THE MENU WHEN THE MENU BUTTON IS PRESSED
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.pintr_menu_items, menu);
		return true;
	}

	

//	REACT TO ITEMS PRESSED IN THE MENU DROP-DOWN
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    Intent MenuIntent = new Intent(this, Pintr_G_005_MenuManager.class);
	    int MenuItemId = item.getItemId();
	    
	    if (MenuItemId==android.R.id.home) {
	    	startActivity(new Intent(this, Pintr_03_entry_page.class));
	    }    
	    else{
 
	    	MenuIntent.putExtra("menuitem", MenuItemId);
		    startActivity(MenuIntent);
	    }
		return true;
	}
	
	public void menumanager(){		
		//MENU MANAGER:
		final ToggleButton menuButton = (ToggleButton)findViewById(R.id.menuToggle);
		final LinearLayout menuLayout = (LinearLayout)findViewById(R.id.menuLayout);
		final LinearLayout homeButton = (LinearLayout)findViewById(R.id.homeButton);
		final LinearLayout converseButton = (LinearLayout)findViewById(R.id.converseButton);
		final LinearLayout friendsButton = (LinearLayout)findViewById(R.id.friendsButton);
		final LinearLayout messagesButton = (LinearLayout)findViewById(R.id.messagesButton);
		final LinearLayout eventsButton = (LinearLayout)findViewById(R.id.eventsButton);
		final LinearLayout lfpButton = (LinearLayout)findViewById(R.id.lfpButton);
		final View entryPageMain = (View)findViewById(R.id.entryPageMain);

		Resources res = getResources();
		int colorIn =  res.getColor(R.color.MenuButtonBckgdPress);
		int colorOut =  res.getColor(R.color.CharcoalGreyBckGd);

		int unreadMessages = dbq.unreadMsgCt();
		int FriendRequestsCount = dbq.getFriendRequestsCount();
		
		Pintr_G_099_HeaderMenuManager pmm = new Pintr_G_099_HeaderMenuManager(this
											,menuLayout  
											,homeButton  
											,converseButton  
											,friendsButton  
											,messagesButton  
											,eventsButton 
											,colorIn 
											,colorOut
											,menuButton
											,entryPageMain
											,unreadMessages
											,FriendRequestsCount 
											,lfpButton
											);
		
		homeButton.setOnClickListener(pmm.homeClick);
		converseButton.setOnClickListener(pmm.converseClick);
		friendsButton.setOnClickListener(pmm.friendsClick);
		messagesButton.setOnClickListener(pmm.messagesClick);
		eventsButton.setOnClickListener(pmm.interestsClick);
		lfpButton.setOnClickListener(pmm.lfpClick);
		
	}

	
	public void settingsMenumanager(Context context){

		Resources res = getResources();
		int colorIn =  res.getColor(R.color.MenuButtonBckgdPress);
		int colorOut =  res.getColor(R.color.CharcoalGreyBckGd);

		LinearLayout logoutButton = (LinearLayout)findViewById(R.id.logoutButton);
		LinearLayout settingsButton = (LinearLayout)findViewById(R.id.settingsButton);
		LinearLayout interestsButton = (LinearLayout)findViewById(R.id.interestsButton);
		
		Pintr_G_096_BottomMenuManager bsm = new Pintr_G_096_BottomMenuManager(
				(LinearLayout)findViewById(R.id.settingsMenuLayout)
				,logoutButton
				,settingsButton
				,interestsButton
				,context
				,colorIn
				,colorOut
				);
		
		final ToggleButton menuButton = (ToggleButton)findViewById(R.id.menuToggle);
		menuButton.setOnCheckedChangeListener(bsm.menuCheckList);
		
		logoutButton.setOnClickListener(bsm.logoutClick);
		settingsButton.setOnClickListener(bsm.settingsClick);
		interestsButton.setOnClickListener(bsm.interestsClick);
		
	}

//	CUSTOM USER MENU MANAGER
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		
		final LinearLayout menuLayout = (LinearLayout)findViewById(R.id.settingsMenuLayout);
		
	    switch(keycode) {
        case KeyEvent.KEYCODE_MENU:
        	if (menuLayout.getVisibility() == View.VISIBLE) {
    			menuLayout.setVisibility(View.INVISIBLE);
    		} else {
    			menuLayout.setVisibility(View.VISIBLE);
    		}
    		return true;
	    }

	    return super.onKeyDown(keycode, e);
	}
	
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbq != null) {
        	dbq.close();
        }
    }  
}