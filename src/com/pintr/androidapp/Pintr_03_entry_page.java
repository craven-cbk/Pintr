package com.pintr.androidapp;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;


public class Pintr_03_entry_page 	extends 	Activity
									implements 	OnItemSelectedListener {

	int social_status;
	double latitude=0, longitude=0;
	String  location
			, date_status_set
			, email 
			, password
			, handle
			;
    private static final String EVENTS_TYPES_TABLE = "P_EK_EVTP";
	
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);;
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_014_ProfilerDBMSManager rDB = new Pintr_G_014_ProfilerDBMSManager();
	final Pintr_G_016_StatusHandler dbsh = new Pintr_G_016_StatusHandler(this);;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_03_entry_page);
			
		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
		PageTitle.setText(R.string.pintr_03_entry_page_title);
		
		ArrayList<String> Creds = dbq.UserCreds();
		email  = Creds.get(0).toString();
		password = Creds.get(1).toString();
		handle = Creds.get(3).toString();
		
		TextView HandlerName = (TextView )findViewById(R.id.HandlerName);
		HandlerName.setText(handle );
		
		
		List<String> userStatusItems = dbsh.getStatus();
		social_status = Integer.parseInt(userStatusItems.get(0).toString());
		location = userStatusItems.get(1).toString();
		
		TextView textGeoloc = (TextView )findViewById(R.id.textGeoloc);
		textGeoloc.setText(location);
		
		//INITIALIZE THE MENUS
		if(nw.haveNetworkConnection(this))
			rDB.storeFriendRequestsCount(this);
		menumanager();
		settingsMenumanager(this);

		
		//GET STATUS LIST ARRAY
		List<String> statusListArray =  dbsh.getStatusList();
		int arrLen = statusListArray.size();
		
		List<String> statusLabels = new ArrayList<String>();
		List<String> statusIds = new ArrayList<String>();
		
		for (int i=0; i < arrLen; i++){
			String[] statusListArrayItems = statusListArray.get(i).split(",");
			statusIds.add(statusListArrayItems[0]);
			statusLabels.add(statusListArrayItems[1]);
		}
		
		//SET UP STATUS SPINNER
		Spinner statusSpinner = (Spinner)findViewById(R.id.statusSpinner);
		ArrayAdapter<String> statusAdapter 
					= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, statusLabels);
		statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		statusSpinner.setAdapter(statusAdapter );	
		

        Boolean networkAvailable  = nw.haveNetworkConnection(this);
        
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		if(networkAvailable==false){
		offlineZone.setVisibility(View.VISIBLE);
		Log.d("NETWORK DOWN", "Network unavailable");
		}
		
		//ADD SPINNER WHILST LOADING DATA
		ProgressBar pbspnner  = new ProgressBar(this);;
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		LinearLayout.LayoutParams iRL 
			= new LinearLayout.LayoutParams(50, 50);
		spinnerRL.addView(pbspnner,iRL);

		//GET LOCATION
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new Pintr_G_097_03_NetworkLocation (this, this);
		if(networkAvailable){
			mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
		}
		
		statusSpinner.setOnItemSelectedListener(this);
		
		//GET THE HOME PAGE CONTENTS ASYNCHRONOUSLY
		Pintr_G_003_HomePageContents Task 
			= new Pintr_G_003_HomePageContents (this);
	    Task.execute();
	    Task.p03 = this;
		
	}


	
	public void listLocat(Location loc, Context context){
		
		final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);;
		
		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		
		latitude = loc.getLatitude();
		longitude = loc.getLongitude();
		
		//GET THE USER DETAILS FROM THE ONBOARD DB
		
		List<String> userStatusItems = dbsh.getStatus();
		int social_status = Integer.parseInt(userStatusItems.get(0).toString());
		String location = userStatusItems.get(1).toString();
		String date_status_set = userStatusItems.get(2).toString();
		if(date_status_set.equals("")){
			date_status_set = "2000-01-01 00:00:00";
		}
		
	    Geocoder geocoder = new Geocoder(context, Locale.getDefault());   
	    String result = "";
	    try {
	        List<Address> list = geocoder.getFromLocation(
	        		loc.getLatitude(), loc.getLongitude(), 1);
	        if (list != null && list.size() > 0) {
	            Address address = list.get(0);
	            result = address.getAddressLine(1) + ", " + address.getAddressLine(2);
	        }
	    } catch (IOException e) {} 

	    if(result.equals(""))
	    	result = "Location unobtainable";
	    else{
	    	Log.d("LOCATION FOUND", result);
	    	location = result;
	    }
	    
	    TextView textGeoloc = (TextView )findViewById(R.id.textGeoloc);
		textGeoloc.setText(location);
		
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		spinnerRL.removeAllViews();
    	
	    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Date updateDateLast = null;
		try {updateDateLast = simpleDateFormat.parse(date_status_set);
		} catch (ParseException e) {e.printStackTrace();}
		
		Date currentTime = Calendar.getInstance().getTime();
		Long TimeSinceupdateSecs = (currentTime.getTime() - updateDateLast.getTime())/1000;
        Boolean networkAvailable  = nw.haveNetworkConnection(context);
      
		Calendar now = Calendar.getInstance();
	    String timeNow  = simpleDateFormat.format(now.getTime());
		
		//IF IT HAS BEEN OVER 4 HOURS SINCE THE LAST STATUS SET, CHANGE TO "NO STATUS"
		if(TimeSinceupdateSecs > 14400 && social_status > 1 && networkAvailable){
			social_status = 1;
			dbsh.JSONStatusUpload( email
									,password
									,social_status
									,location
									,timeNow 
									,latitude
									,longitude
									);
		}
		else if(networkAvailable)
			dbsh.JSONStatusUpload( email
									,password
									,social_status
									,location
									,timeNow  
									,latitude
									,longitude
									);
	}
	

	
	//STATUS SPINNER CLICK LISTENER
	@Override
	public void onItemSelected(AdapterView<?> parent
								, View view
								, int position
								, long id
								) {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		Calendar now = Calendar.getInstance();
	    String timeNow  = simpleDateFormat.format(now.getTime());
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if (networkAvailable){
			dbsh.JSONStatusUpload(
						email
						,password
						,position + 1
						,location
						,timeNow
						,latitude
						,longitude
						);
		}
	}
	
	
	
	public void processFinish(	HashMap<Integer, HashMap<String, String>> RecomQnAHM 
								,HashMap<Integer, HashMap<String, String>> RecentQnAHM 
								,HashMap<Integer, HashMap<String, String>> unreadMsgHM 
								,HashMap<Integer, HashMap<String, String>> eventInvitesHM 
							){
		Pintr_G_003_HomePageContents g003 = new Pintr_G_003_HomePageContents (this);
	    LinearLayout entryPageDisplayArea = (LinearLayout)findViewById(R.id.entryPageDisplayArea);

	    for (int i = 0; i < 5; i++){
			Log.d("CYCLE", "->" + i);
			if(RecomQnAHM.size() > i){
				Log.d("RECOMMENDED", "->" + i);
				
				HashMap<String, String> RecomQn =  RecomQnAHM.get(i);
				g003.writeQuestionLists( RecomQn
									    ,LayoutAdd(entryPageDisplayArea, "Recommended Question")
									    ,"RECOMMENDED"
									    );
			}
			if(RecentQnAHM.size() > i){
				Log.d("RECENT", "->" + i);
				HashMap<String, String> RecentQn =  RecentQnAHM.get(i);
				g003.writeQuestionLists( RecentQn
					    				,LayoutAdd(entryPageDisplayArea, "Recent Question")
									    ,"RECENT"
									    );	
			}
			if(unreadMsgHM.size() > i){
				Log.d("createMessageSections", "->" + i);
				HashMap<String, String> unreadMsg =  unreadMsgHM.get(i);
				g003.createMessageSections(	unreadMsg 
											,LayoutAdd(entryPageDisplayArea, "Messages")
											,email
											,password
											);
			}
			if(eventInvitesHM.size() > i){
				Log.d("displayEvents", "->" + i);
				HashMap<String, String> eventInvites =  eventInvitesHM.get(i);	
				g003.displayEvents(eventInvites
				    				,LayoutAdd(entryPageDisplayArea, "Upcomng Events")
									, i 
									, EventTypesArray()
									);
			}
		}
	}
	
	
	public LinearLayout LayoutAdd(LinearLayout entryPageDisplayArea, String labelText){
		//ADD HORIZONTAL LAYOUT
	    LinearLayout RL= new LinearLayout (this);
	    RL.setOrientation(LinearLayout.VERTICAL);

	    LinearLayout.LayoutParams lp1;
        lp1 = new LinearLayout.LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT);
	    
	    RL.setLayoutParams(lp1);
	    RL.setPadding (10,10,10,10);
	    lp1.setMargins(0, 0, 0, 5);
	    
	    entryPageDisplayArea.addView(RL, lp1);
	    TextView valueTV = new TextView(this);
	    valueTV.setText(labelText);
	    RL.addView(valueTV);
	    return RL;
	}
	
	public ArrayList<String>  EventTypesArray(){
		
		//****  GET THE DATA FROM SQLITE INTO ARRAYLISTS
		ArrayList<String> evtTblData = dbq.wholeTableReader(EVENTS_TYPES_TABLE, ",");
		String lastEventTypeLabel="";
		ArrayList<String> EventTypesArray = new ArrayList<String>();
		
		Log.d("evtTblData:", "->" + evtTblData.size());
		if(!evtTblData.get(0).toString().equals("null,null"))
			for (int i = 0; i <  evtTblData.size(); i++){
				String [] items = evtTblData.get(i).toString().split(",");
				Log.d("items 0/1", "" + items[0] + items[1] );
				if (lastEventTypeLabel.equals(items[2]) == false)
					EventTypesArray.add(items[2]);
				lastEventTypeLabel = items[2];
			}
		return EventTypesArray;
	}
	
	
	
	
	
	//******************************************
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

	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {}	

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
