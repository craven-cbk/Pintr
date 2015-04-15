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
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Pintr_06_Deregister_page extends Activity  {

	Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_06_deregister);

		final Button DeleteAccountButton = (Button)findViewById(R.id.DeleteAccountButton);
		final TextView DeleteActlLabel = (TextView )findViewById(R.id.DeleteActlLabel);
		final EditText PasswordInput = (EditText)findViewById(R.id.PasswordInput);

		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		DeleteAccountButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {			
				String passwordGiven = PasswordInput.getText().toString();
				DeleteActlLabel.setText(CheckPassword(passwordGiven));
			}
		});
		
		//MENU MANAGER:
//		frReqsCt = rDB.userFriendRequests( 	email
//				,password
//				);
		menumanager();
		settingsMenumanager(this);
	}
	
	//CHECK PASSWORD ON INTERNAL DATABASE
	public String CheckPassword(String passwordGiven){

		String attemptOutcome = null;
		Context context = this;
	    final String DATABASE_NAME = "Pintr_DB";
		
		ArrayList<String> UserCreds = dbq.UserCreds();
		String Lo_1 = UserCreds.get(0);
		String Lo_2 = UserCreds.get(1);
		
		if(Lo_2.equals(passwordGiven)){
		
			attemptOutcome = "Passwords match - account will be deleted...";
			DeleteAccount(Lo_1, Lo_2);
			context.deleteDatabase(DATABASE_NAME);
			startActivity(new Intent(this, PintrMain.class));
		}
		else if(passwordGiven.equals("")){
		
			attemptOutcome = "Please enter a password to proceed";
		}
		else {
		
			attemptOutcome = "Password incorrect";
		}
		
		return attemptOutcome;
		
	}
	
	//ORDER PHP SCRIPT TO DELETE ACCOUNTS
	public void DeleteAccount(String Lo_1, String Lo_2){

		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder();
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(Lo_1, "UTF-8")
					+ "&" + URLEncoder.encode("PasswordGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(Lo_2, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/delete_user.php";
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
			}
		
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
				
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
	

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbq != null) {
        	dbq.close();
        }
    }  		
    
}