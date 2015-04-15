package com.pintr.androidapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class Pintr_21_MessageRecipientPicker extends Activity {

	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_017_ProfilesDBMaker dbm = new Pintr_G_017_ProfilesDBMaker(this);
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	
	Map<Integer, String> friendsMap = new HashMap<Integer, String>();
	LinearLayout friendsPickerSpinner;
	HashMap<Integer, String> participants ;
	HashMap<Integer, String> existingParticipants ;
	HashMap<Integer, String> nuParticipants ;
	final Context context = this;
	HashMap<String, String> EventDetailsMapF  = new HashMap<String, String>();
	String Origin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_21_conversation_friend_adder);

		Button AddRecipientsButton = (Button) findViewById(R.id.AddRecipientsButton);
		friendsPickerSpinner = (LinearLayout) findViewById(R.id.friendsPickerSpinner);

		// ****** MENU MANAGER: ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout) findViewById(R.id.offlineZone);
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if (networkAvailable == false) {
			offlineZone.setVisibility(View.VISIBLE);
		}
		// ****** END MENU MANAGER ******

		//GET BUNDLES
		final Bundle extras = getIntent().getExtras();
		
		Origin = extras.getString ("Origin");
		final String OrganiserData = extras.getString ("OrganiserData");
		
		participants = (HashMap<Integer, String>) getIntent().getSerializableExtra("participants");
		if(Origin.equals("EVTFWD")){
			existingParticipants = (HashMap<Integer, String>) getIntent().getSerializableExtra("participants");
			nuParticipants = new HashMap<Integer, String>();
			AddRecipientsButton.setText("Forward Invite");
		}
		if (participants==null) participants = new HashMap<Integer, String>();

		final Class<?> messageClass = Pintr_20_MessageConversationTool.class;
		final Class<?> eventMaker = Pintr_23_EventMaker.class;
		final Class<?> eventViewer = Pintr_22_EventsOverview.class;
	 	
		//WRITE THE FRIENDS TO SCREEN
		DisplayFriends();
		
		
	   //ADD RECIPIENTS BUTTON
	    AddRecipientsButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

				Intent AutoRegOnBoard = new Intent();
				
				//STATE THAT THE INTEREST LIST SHOULD START AT THE TOP
				if(Origin.equals("MESSAGES")){

					AutoRegOnBoard = new Intent(context, messageClass);
					final String messageSoFar = extras.getString ("message");
				    AutoRegOnBoard.putExtra("ConversationID", 0);
				    AutoRegOnBoard.putExtra("message", messageSoFar);
				    AutoRegOnBoard.putExtra("Origin", "MESSAGE");
				}
				else if(Origin.equals("EVENTS")){

					AutoRegOnBoard = new Intent(context, eventMaker);
				    AutoRegOnBoard.putExtra("Origin", "ADDPPL");
				    AutoRegOnBoard.putExtra("OrganiserData", OrganiserData);
					EventDetailsMapF 
						= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");
					AutoRegOnBoard.putExtra("EventDetailsMapF", EventDetailsMapF);
				}
				if(Origin.equals("EVTFWD")){
					AutoRegOnBoard = new Intent(context, eventViewer);
				    AutoRegOnBoard.putExtra("Origin", "FWDIVT");
				    AutoRegOnBoard.putExtra("OrganiserData", OrganiserData);
					EventDetailsMapF 
						= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");
					AutoRegOnBoard.putExtra("EventDetailsMapF", EventDetailsMapF);
					AutoRegOnBoard.putExtra("nuParticipants", nuParticipants);
					Pintr_G_025_InviteFwdr ivfwd = new Pintr_G_025_InviteFwdr(context);
					String fwdRegRes = ivfwd.regFwdInvite(EventDetailsMapF.get("Event_id"), nuParticipants); 
					ivfwd.dispatchNotification(nuParticipants);
					Log.d("fwdRegRes ", fwdRegRes );
				}
				
			    AutoRegOnBoard.putExtra("participants", participants);
			    startActivity(AutoRegOnBoard);	
				
			}
	    });
	}

	public void DisplayFriends() {

		// LOAD FRIENDS AND THEIR PNTR INTO A HASH MAP
		ArrayList<String> friendSQLtDataResults = new ArrayList<String>();
		friendSQLtDataResults = dbm.peopleReader("friends");

		// *******************************************

		int arrayLength = friendSQLtDataResults.size();
		int[] arr_images = new int[arrayLength + 1];
		final int[] friendUIDs = new int[arrayLength + 1];
		String[] friendsHandles = new String[arrayLength + 1];

		friendsHandles[0] = "Select friends to add";
		friendUIDs[0] = 0;
		arr_images[0] = (R.drawable.grey_tick_rs);

		for (int i = 0; i < friendSQLtDataResults.size(); i++) {
			String[] elements = friendSQLtDataResults.get(i).split(",");
			friendsMap.put(Integer.parseInt(elements[0]), elements[1]);
			friendsHandles[i + 1] = elements[1];
			friendUIDs[i + 1] = Integer.parseInt(elements[0]);
			int uid = Integer.parseInt(elements[0]);
			String handle = elements[1];

			if(Origin.equals("EVTFWD") == false
					||existingParticipants.containsKey(uid) == false
				){
					
					// ADD HORIZONTAL LAYOUT
					RelativeLayout RL = new RelativeLayout(this);
					RelativeLayout.LayoutParams lp;
					RL.setPadding(10, 10, 10, 10);
					lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					friendsPickerSpinner.addView(RL, lp);

					// ADD TICK ICON
					final ImageView Tick = new ImageView(this);
					Tick.setImageResource(R.drawable.grey_tick_rs);

					lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					lp.addRule(RelativeLayout.CENTER_VERTICAL);
					final int NewId = uid;
					Tick.setId(NewId);
					Tick.setLayoutParams(lp);
					Tick.requestLayout();
					Tick.getLayoutParams().height = 50;
					Tick.getLayoutParams().width = 50;

					RL.addView(Tick);

					// ADD TITLE
					TextView valueTV = new TextView(this);
					valueTV.setText(handle);
					valueTV.setTextColor(getResources().getColor(R.color.CharcoalGreyBckGd));
					lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					valueTV.setBackgroundResource(R.drawable.profile_background);
					valueTV.setPadding(10, 10, 10, 10);
					lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					lp.addRule(RelativeLayout.CENTER_VERTICAL);
					valueTV.setLayoutParams(lp);

					RL.addView(valueTV);

					// MAKE NEW VIEW CLICKABLE
					RL.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							Log.d("PRE-INTEGER PASSED", "" + Integer.toString(NewId));
							selectConvoParticipant(NewId, Tick);
						}
					});

					// ADD DIVIDER LINE
					View GreyLine = new View(this);
					GreyLine.setPadding(5, 5, 5, 5);
					GreyLine.setLayoutParams(new LayoutParams(
							LayoutParams.WRAP_CONTENT, 1));
					GreyLine.setBackgroundColor(Color.parseColor("#C9C9C9"));

					friendsPickerSpinner.addView(GreyLine);
				
					String keyExistsStr ="" ;
					if(participants != null) {
						keyExistsStr  = participants.get(uid);
					
						if(keyExistsStr != null){
				    	Tick.setImageResource(R.drawable.green_tick_rs);
						} 
					}	
			}
		}
	}

	public void selectConvoParticipant(int uid, ImageView Tick ){
		
		String keyExistsStr = "";
		
		if(participants != null) {
				keyExistsStr  = participants.get(uid);
				Log.d("keyExists", "" + keyExistsStr);
		}
		
        if(keyExistsStr == null){
			String handle = friendsMap.get(uid);
	    	Log.d("Key pressed - added", "" +  handle);
			participants.put(uid, handle);
	    	Tick.setImageResource(R.drawable.green_tick_rs);
			if(Origin.equals("EVTFWD"))
				nuParticipants.put(uid, handle);
		} 
        
        else {
			Log.d("Key pressed - removed","" +   friendsMap.get(uid));
			participants.remove(uid);
			if(Origin.equals("EVTFWD"))
				nuParticipants.remove(uid);
	    	Tick.setImageResource(R.drawable.grey_tick_rs);
		}
	}
	
	
	
	
	
	
	
	

	// GENERAL PAGE UTILITY FUNCTIONS
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
		
		logoutButton.setOnClickListener(bsm.logoutClick);
		settingsButton.setOnClickListener(bsm.settingsClick);
		interestsButton.setOnClickListener(bsm.interestsClick);
		
	}

	// CUSTOM USER MENU MANAGER
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {

		final LinearLayout menuLayout = (LinearLayout) findViewById(R.id.settingsMenuLayout);

		switch (keycode) {
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
        if (dbm != null) {
        	dbm.close();
        }
    }  
}