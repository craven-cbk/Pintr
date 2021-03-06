package com.pintr.androidapp;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;


public class Pintr_09_Credchg_page extends Activity    {

	Button SubButton;
	EditText Password, rptPassword, newPassword ;
	TextView ErrorMessaging;
	Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_09_credchg);

		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
				
		Password = (EditText)findViewById(R.id.passwordInput);
		newPassword = (EditText)findViewById(R.id.newpasswordInput);
		rptPassword = (EditText)findViewById(R.id.repeatpasswordInput);
		ErrorMessaging = (TextView)findViewById(R.id.ErrorMessgagingLabel);
		
		SubButton = (Button)findViewById(R.id.SubButton);
		
		//SET REGISTRATION BUTTON FUNCTION
		SubButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	
				String PasswordStr = Password.getText().toString();
				String newPasswordStr = newPassword.getText().toString();
				String rptPasswordStr = rptPassword.getText().toString();
				
				ChgPw(PasswordStr, newPasswordStr, rptPasswordStr);
			}
		});	
	
	}
		
	//WHEN REGISTER BUTTON IS PRESSED, DO THE FOLLOWING:
	public void ChgPw(	String Password
						,String newPassword
						,String rptPassword){
		
		//GET PW STORED ON DEVICE
		ArrayList<String> UserCreds = dbq.UserCreds();
		
		String Email  = UserCreds.get(0);
		String currPw = UserCreds.get(1);
		String uid = UserCreds.get(2);
		
		Boolean correctPW = Password.equals(currPw);
		
		// CHECK PASSWORD FOR VALIDITY
		if(newPassword.length() == 0){
			ErrorMessaging.setText("Please add a password for your account");			
		}
		else if(Password.length() < 6){
			ErrorMessaging.setText("Please enter your old password");			
		}
		else if(newPassword.length() < 6){
			ErrorMessaging.setText("Please make a new password that is at least 6 characters long");			
		}
		else if(newPassword.equals(rptPassword) == false){
			ErrorMessaging.setText("Your passwords do not match");			
		}
		else if(newPassword.matches(".*\\d+.*") == false
				){
			ErrorMessaging.setText("Please make a password containing at least one number");			
		}
		
		//CHECK PASSWORD AGAINST LOCAL DB
		else if(correctPW == false){
			ErrorMessaging.setText("I'm sorry, your old password was entered incorrectly");
			
		}
		// IF EVERYTHING IS FINE, SUBMIT THE NEW PW FOR REGISTRATION
		else{
			Pintr_G_007_DtlsChgr dtls = new Pintr_G_007_DtlsChgr();
			String returnMsg = dtls.chpw(	Email
										,uid
										,Password
										,newPassword
										,this
										);	
			ErrorMessaging.setText(returnMsg);
		}
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