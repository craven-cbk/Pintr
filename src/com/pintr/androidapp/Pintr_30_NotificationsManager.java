package com.pintr.androidapp;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ToggleButton;


public class Pintr_30_NotificationsManager extends Activity  {

	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_014_ProfilerDBMSManager rDB = new Pintr_G_014_ProfilerDBMSManager();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_30_notification_preferences);

		//MENU MANAGER:
		if(nw.haveNetworkConnection(this))
			rDB.storeFriendRequestsCount(this);
		menumanager();
		settingsMenumanager(this);

		
		Switch QuestionsSwitch = (Switch)findViewById(R.id.QuestionsSwitch);
		Switch AnswersSwitch = (Switch)findViewById(R.id.AnswersSwitch);
		Switch FriendReqSwitch = (Switch)findViewById(R.id.FriendReqSwitch);
		Switch MessageSwitch = (Switch)findViewById(R.id.MessageSwitch);
		Switch InviteSwitch = (Switch)findViewById(R.id.InviteSwitch);
		Switch EventUpdateSwitch = (Switch)findViewById(R.id.EventUpdateSwitch);

		loadSavedPreferences(QuestionsSwitch, "QuestionsNotificationsOn");
		loadSavedPreferences(AnswersSwitch, "AnswersNotificationsOn");
		loadSavedPreferences(FriendReqSwitch, "FriendRequestsNotificationsOn");
		loadSavedPreferences(MessageSwitch, "MessagesNotificationsOn");
		loadSavedPreferences(InviteSwitch, "InvitesNotificationsOn");
		loadSavedPreferences(EventUpdateSwitch, "EventUpdatesNotificationsOn");
		
		

		QuestionsSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	savePreferences("QuestionsNotificationsOn", isChecked) ;
		    }
		});
		AnswersSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	savePreferences("AnswersNotificationsOn", isChecked) ;
		    }
		});
		FriendReqSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	savePreferences("FriendRequestsNotificationsOn", isChecked) ;
		    }
		});
		MessageSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	savePreferences("MessagesNotificationsOn", isChecked) ;
		    }
		});
		InviteSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	savePreferences("InvitesNotificationsOn", isChecked) ;
		    }
		});
		EventUpdateSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		    	savePreferences("EventUpdatesNotificationsOn", isChecked) ;
		    }
		});
	}
	
	
	 private void loadSavedPreferences(Switch shwitchName, String prefName) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean checkBoxValue = sharedPreferences.getBoolean(prefName, true);
        if (checkBoxValue) {
        	shwitchName.setChecked(true);
        } else {
        	shwitchName.setChecked(false);
        }
        Log.d(prefName, Boolean.toString(checkBoxValue));
    }

 
    private void savePreferences(String key, Boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
        Log.d("SAVED PREFERENCE" + key, Boolean.toString(value));
    }


	//**************************************************
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