package com.pintr.androidapp;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;


public class Pintr_26_LFP extends Activity implements OnItemSelectedListener  {

	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	Spinner eventTopicSpinner, whenSpinner, eventOrInviteSpinner, distanceToEventSpinner;
	EditText eventLocation, edittextDescription ;
    private static final String EVENTS_TYPES_TABLE = "P_EK_EVTP"; 
    String latitude, longitude;
    Context context = this;
	ArrayList<String> EventTypesArray = new ArrayList<String> ();
	String [] eventDetailLabels = {"Event_id","Event_creator_uid","handler"
			,"Event_DtTm","event_type","type_variant"
			,"event_title","venue_name","venue_address_text"
			,"chg_evt_dttm","hashtags","venue_yelp_id"
			,"venue_yelp_url","invite_status", "event_description"
			,"participant_event_status", "participant_attending"};

	Button postButton, searchButton , hereButton ;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_26_lfg);

		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
		PageTitle.setText("LFP");
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		//******  END MENU MANAGER ******
		
		eventTopicSpinner = (Spinner)findViewById(R.id.eventTopicSpinner);
		whenSpinner = (Spinner)findViewById(R.id.whenSpinner);
		eventOrInviteSpinner = (Spinner)findViewById(R.id.eventOrInviteSpinner);
		distanceToEventSpinner = (Spinner)findViewById(R.id.distanceToEventSpinner);
		eventLocation = (EditText)findViewById(R.id.eventLocation);
		edittextDescription = (EditText)findViewById(R.id.edittextDescription);
		
		final LinearLayout collapsibleAdvancedView = (LinearLayout)findViewById(R.id.collapsibleAdvancedView);
		final LinearLayout expandCollapsibleAdvancedView = (LinearLayout)findViewById(R.id.expandCollapsibleAdvancedView);

		postButton = (Button)findViewById(R.id.postButton);
		searchButton = (Button)findViewById(R.id.searchButton );
		hereButton = (Button)findViewById(R.id.hereButton);
		
		loadSpinners();
		
		
		expandCollapsibleAdvancedView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				Log.d("VISIBILTY", "-> " +  collapsibleAdvancedView.getVisibility());
				if(collapsibleAdvancedView.getVisibility()==View.GONE)
					collapsibleAdvancedView.setVisibility(View.VISIBLE);
				else 
					collapsibleAdvancedView.setVisibility(View.GONE);
			
			}
		});	

		postButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				if(eventOrInviteSpinner.getSelectedItemPosition()==0)
					onSubBtnPressed("LFI_POST");
				else if(eventOrInviteSpinner.getSelectedItemPosition()==1)
					gotoNextPage(Pintr_23_EventMaker.class);
			
			}
		});		
//	
		searchButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(eventOrInviteSpinner.getSelectedItemPosition()==0)
					onSubBtnPressed("PEOPLE_SEARCH");
				else if(eventOrInviteSpinner.getSelectedItemPosition()==1)
					onSubBtnPressed("EVENT_SEARCH");
				
			}
		});
		
		
		hereButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				getCurrLocn(false, "", "");
			}
		});	
	}

	public void onSubBtnPressed(String function){
		
		showLoadSpinner();
		
		String evtLocStr = eventLocation.getText().toString();
		String textEntry  = edittextDescription.getText().toString();
		
		
		//  A - HAVE A LOCATION ENTERED AS TEXT
		if(evtLocStr != null && !evtLocStr.equals("")){

			Log.d("SCENARIO","A - HAVE A LOCATION ENTERED AS TEXT");
			getCoordsFromLocn(evtLocStr);
			executeAsync(function, evtLocStr, textEntry);
		} 
		//  B - NO LOCATION, NO CO-ORDS AVAILABLE
		else if (latitude == null){

			Log.d("SCENARIO","B - NO LOCATION, NO CO-ORDS AVAILABLE- COORDS: "+ latitude + "/" + longitude);
			getCurrLocn(true, function, textEntry);
			
		}
		//  C - NO LOCATION,CO-ORDS AVAILABLE
		else {
			Log.d("SCENARIO","C - NO LOCATION, BUT CO-ORDS AVAILABLE - COORDS: " + latitude + "/" + longitude);
			executeAsync(function, evtLocStr, textEntry);
		}
		
	}

	
	public void executeAsync(String function, String evtLocStr, String textEntry){

		int eventTopicPosn = eventTopicSpinner.getSelectedItemPosition();
		int whenPosn = whenSpinner.getSelectedItemPosition();
		int distancePosn = distanceToEventSpinner.getSelectedItemPosition();
		
		int timerangeHours = 0;

		if (whenPosn == 0)		timerangeHours = 4;
		else if (whenPosn == 1)	timerangeHours = 24;
		else if (whenPosn == 2)	timerangeHours = 48;
		else if (whenPosn == 3)	timerangeHours = 168;
		else if (whenPosn == 4)	timerangeHours = 336;

		int distanceInMiles = 0;

		if (distancePosn == 0)		distanceInMiles = 1;
		else if (distancePosn == 1)	distanceInMiles = 2;
		else if (distancePosn == 2)	distanceInMiles = 3;
		else if (distancePosn == 3)	distanceInMiles = 5;
		else if (distancePosn == 4)	distanceInMiles = 10;
		else if (distancePosn == 5)	distanceInMiles = 30;
		
		
		Pintr_A_005_LFPAsync a05Task = new Pintr_A_005_LFPAsync(this
																, function
																, evtLocStr
																, eventTopicPosn
																, timerangeHours
																, distanceInMiles
																, latitude
																, longitude
																, textEntry
																);
		a05Task.execute();
		a05Task.p26 = this;
	
	
	}
	
	
	public void processFinish(String response, String process){
		hideLoadSpinner();

		if(!response.equals("EMPTYQUERY")){

			Log.d("LFP: SERVER RESPONSE", "-> " + response);
			
			String jsonHeader = "";
			if(process.equals("EVENT_SEARCH") || process.equals("PEOPLE_SEARCH")){
				
				if(process.equals("PEOPLE_SEARCH")) jsonHeader = "people_returned";
				else if(process.equals("EVENT_SEARCH")) jsonHeader = "events_LFP_list_returned";
				JSONArray results = jsh.JSON_Parser( response
						, jsonHeader
						);
				displayResults(results, process );
				
			} else if(process.equals("EVENT_SEARCH"))
				eventLocation.setText("Availability posted");
		
		}
	}

	
	public void displayResults(JSONArray results, String process ){
		
		LinearLayout displayArea = (LinearLayout)findViewById(R.id.displayArea);
		displayArea.removeAllViews();
		
		if(process.equals("PEOPLE_SEARCH")) 
			displayPeople(results, displayArea);
		
		else if(process.equals("EVENT_SEARCH"))
			displayEvents(results, displayArea);
	}
	
	
	public void displayPeople(JSONArray results, LinearLayout displayArea ){

		
		//PARSE THE JSON FILE

		if( results!=null )
			for(int i = 0; i < results.length(); i++){
				LinearLayout linearLayout
					= (LinearLayout) View.inflate(this,	R.layout.pintr_g_91_lfp_avail_post, null);
				displayArea.addView(linearLayout);
				    	
	        	String 	pintr_user_id="", post_DtTm="", handler=""
//	        			, radius_miles=""
	        			, event_type="", description_posted = "";
		        try {
		        	pintr_user_id = results.getJSONObject(i).getString("pintr_user_id");
		        	post_DtTm = results.getJSONObject(i).getString("post_DtTm");
//		        	radius_miles = results.getJSONObject(i).getString("radius_miles");
		        	handler = results.getJSONObject(i).getString("handler");
		        	event_type = results.getJSONObject(i).getString("event_type");
		        	description_posted = results.getJSONObject(i).getString("description_posted");
		        	
		        } catch (JSONException e) {e.printStackTrace();}
		        
	
				//CHANGE THE SUMMARY VIEW IDS AND ADD THE INFORMATION
				LinearLayout peopleView = (LinearLayout)findViewById(R.id.peopleView);
				int esvNuID = R.id.peopleView * 100 +i ;
				Log.d("esvNuID", "->"+esvNuID);
				peopleView.setId(esvNuID);
				
				TextView personHandle = (TextView )findViewById(R.id.personHandle);
				int personNuID = R.id.personHandle * 100 +i ;
				personHandle.setId(personNuID);
				personHandle.setText(handler);
				
				TextView seekingDescription = (TextView )findViewById(R.id.seekingDescription);
				int descNuID = R.id.eventCreator * 100 +i ;
				seekingDescription.setId(descNuID );
				seekingDescription.setText(description_posted);
				
				TextView eventType = (TextView )findViewById(R.id.eventType);
				int eventTypeNuID = R.id.eventType * 100 +i ;
				eventType.setId(eventTypeNuID);
				eventType.setText(EventTypesArray.get(Integer.parseInt(event_type)));
				
				TextView eventDate = (TextView )findViewById(R.id.eventDate);
				int eventDateNuID = R.id.eventDate * 100 +i ;
				eventDate.setId(eventDateNuID);
				
				DateFormat df = new SimpleDateFormat("dd LLLL yyyy, hh:mm a",Locale.getDefault());
				final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
				String post_DtTmFmtd="";
				try {
					post_DtTmFmtd = df.format(simpleDateFormat.parse(post_DtTm));
				} catch (ParseException e) {e.printStackTrace();}
				
				eventDate.setText(post_DtTmFmtd);
							
				//ADD ONCLICK LISTENER TO VIEW FULL INDIVIDUAL EVENT
				final int pintr_user_id_f = Integer.parseInt(pintr_user_id);
				peopleView.setOnClickListener(new View.OnClickListener() {			
					@Override
					public void onClick(View v) {
	
						Intent MenuIntent = new Intent(context, Pintr_16_ProfileDisplayPage.class);
					    MenuIntent.putExtra("pintr_user_id", pintr_user_id_f);
					    startActivity(MenuIntent);
					}
				});	
				Log.d("ADDED PROFILE "," - >" + i);
	        }
	
	}

	
	public void displayEvents(JSONArray results, LinearLayout displayArea ){

		//PARSE THE JSON FILE

		if( results!=null )
			for(int i = 0; i < results.length(); i++){
				LinearLayout linearLayout
					= (LinearLayout) View.inflate(this,	R.layout.pintr_g_95_event_summary, null);
				displayArea.addView(linearLayout);
				
				    	
	        	String 	Event_id="", Event_creator_uid="", handler=""
	        			, Event_DtTm="", event_type="", type_variant=""
	        			, event_title="", venue_name="", venue_address_text=""
	        			, hashtags="", venue_yelp_id="", venue_yelp_url=""
	        			, invite_status="", event_description=""
	        			;
		        try {
		        	Event_id = results.getJSONObject(i).getString("Event_id");
		        	Event_creator_uid = results.getJSONObject(i).getString("Event_creator_uid");
		        	handler = results.getJSONObject(i).getString("handler");
		        	Event_DtTm = results.getJSONObject(i).getString("Event_DtTm");
		        	event_type = results.getJSONObject(i).getString("event_type");
		        	type_variant = results.getJSONObject(i).getString("type_variant");
		        	event_title = results.getJSONObject(i).getString("event_title");
		        	venue_name = results.getJSONObject(i).getString("venue_name");
		        	venue_address_text = results.getJSONObject(i).getString("venue_address_text");
		        	hashtags = results.getJSONObject(i).getString("hashtags");
		        	venue_yelp_id = results.getJSONObject(i).getString("venue_yelp_id");
		        	venue_yelp_url = results.getJSONObject(i).getString("venue_yelp_url");
		        	invite_status = results.getJSONObject(i).getString("invite_status");
		        	event_description = results.getJSONObject(i).getString("event_description");
		        	
		        	
		        } catch (JSONException e) {e.printStackTrace();}
		        
	
				//CHANGE THE SUMMARY VIEW IDS AND ADD THE INFORMATION
				LinearLayout eventSummaryView = (LinearLayout)findViewById(R.id.eventSummaryView);
				int eventSummaryViewNuID = R.id.eventSummaryView * 100 +i ;
				eventSummaryView.setId(eventSummaryViewNuID);
				
				TextView eventTitle = (TextView )findViewById(R.id.eventTitle);
				int eventTitleNuID = R.id.eventTitle * 100 +i ;
				eventTitle.setId(eventTitleNuID);
				eventTitle.setText(event_title);
				
				TextView eventCreator = (TextView )findViewById(R.id.eventCreator);
				int eventCreatorNuID = R.id.eventCreator * 100 +i ;
				eventCreator.setId(eventCreatorNuID);
				eventCreator.setText(handler);
				
				TextView eventType = (TextView )findViewById(R.id.eventType);
				int eventTypeNuID = R.id.eventType * 100 +i ;
				eventType.setId(eventTypeNuID);
				eventType.setText(EventTypesArray.get(Integer.parseInt(event_type)));
				
				TextView eventDate = (TextView )findViewById(R.id.eventDate);
				int eventDateNuID = R.id.eventDate * 100 +i ;
				eventDate.setId(eventDateNuID);
				eventDate.setText(Event_DtTm);
				
				TextView eventLocation = (TextView )findViewById(R.id.eventLocation);
				int eventLocationNuID = R.id.eventLocation * 100 +i ;
				eventLocation.setId(eventLocationNuID);
				eventLocation.setText(venue_name + " - " + venue_address_text);
				
				TextView eventDescription = (TextView)findViewById(R.id.eventDescription);
				int eventDescriptionNuID = R.id.eventDescription * 100 +i ;
				eventDescription.setId(eventDescriptionNuID);
				eventDescription.setVisibility(View.VISIBLE);
				eventDescription.setText(" - " + event_description);
				
				
				ArrayList<String> Creds = dbq.UserCreds();
				String pintr_user_id  = Creds.get(2).toString();
				
				String participant_event_status="RFI";
				if(Event_creator_uid.equals(pintr_user_id) )
					participant_event_status = "ORG";
				
	        	String [] evtSummary = 	{Event_id, Event_creator_uid, handler
						        			, Event_DtTm, event_type, type_variant
						        			, event_title, venue_name, venue_address_text
						        			,"", hashtags, venue_yelp_id, venue_yelp_url
						        			, invite_status, event_description, participant_event_status , ""
			        					};
	        	
				
				HashMap<String,String> EventDetailsMap = new HashMap<String,String>();
				for(int k=0; k < evtSummary.length; k++)
					EventDetailsMap.put(eventDetailLabels[k], evtSummary[k]);
				
				final HashMap<String,String> EventDetailsMapF = EventDetailsMap;
				
				//ADD ONCLICK LISTENER TO VIEW FULL INDIVIDUAL EVENT
				eventSummaryView.setOnClickListener(new View.OnClickListener() {			
					@Override
					public void onClick(View v) {
	
						Intent MenuIntent = new Intent(context, Pintr_23_EventMaker.class);
					    MenuIntent.putExtra("Origin", "EXISTINGEVENTLFP");
					    MenuIntent.putExtra("EventDetailsMapF", EventDetailsMapF);
					    startActivity(MenuIntent);
					}
				});	
	        }
	
	}


	public void getCurrLocn(Boolean continueToActivity, String function, String textEntry){

		//GET LOCATION
		Log.d("LOCATION","FIRING CALLER...");
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		networkAvailable = nw.haveNetworkConnection(this);
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new Pintr_G_097_26_NetworkLocation (this, this, continueToActivity, function, textEntry);
		if(networkAvailable){
			mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
			Log.d("LOCATION","FIRING LISTENER...");
		}
	}

	public void listLocat(	Location loc
							, Context context
							, Boolean continueToActivity
							, String function
							, String edittextDescription
							){
		latitude = String.valueOf( loc.getLatitude());
		longitude = String.valueOf( loc.getLongitude());
		Log.d("LISTLOCAT ","->  " + latitude + "/" + longitude );

	    Geocoder geocoder = new Geocoder(context, Locale.getDefault());   
	    String result = null;
	    try {
	        List<Address> list = geocoder.getFromLocation(
	        		loc.getLatitude(), loc.getLongitude(), 1);
	        if (list != null && list.size() > 0) {
	            Address address = list.get(0);
            	result = 	address.getAddressLine(0) 
            				+ ", " + address.getAddressLine(1) 
            				+ ", " + address.getAddressLine(2)
            				;
	        }
	    } catch (IOException e) {    } 

	    if(result.equals(null)){
	    	result = "Location unobtainable";
	    } else  if (continueToActivity)
			executeAsync(function, result, edittextDescription);
	    eventLocation.setText(result);
	    
	   
	}

	public void getCoordsFromLocn(String curr_venue_address_text){
		Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
		List<Address> fromLocationName = null;
		try {
			fromLocationName = geocoder.getFromLocationName(curr_venue_address_text,   1);
		} catch (IOException e) {e.printStackTrace();}
		if (fromLocationName != null && fromLocationName.size() > 0) {
			 if(fromLocationName.size() > 0) {
				 latitude = String.valueOf(fromLocationName.get(0).getLatitude());
				 longitude = String.valueOf(fromLocationName.get(0).getLongitude());
			} else {
				 latitude = "0";
				 longitude = "0";
			}
		}
		Log.d("GEOLOC ADD/LAT/LONG", curr_venue_address_text + "/" + latitude  + "/" + longitude);
	}
	
	
	public void loadSpinners(){

		ArrayList<String> whenLabels = new ArrayList<String> ();
		ArrayList<String> radiusLabels = new ArrayList<String> ();
		ArrayList<String> evtInviteLabels = new ArrayList<String> ();
		whenLabels.add("Now (starts less than 4 hours' time)");
		whenLabels.add("Later (starts sometime today)");
		whenLabels.add("Tomorrow");
		whenLabels.add("This week");
		whenLabels.add("Next week");
		

		radiusLabels.add("up to 15 minutes' walk (1 mile)");
		radiusLabels.add("up to 15 minutes' bus (2 miles)");
		radiusLabels.add("up to 15 minutes' tube (3 miles)");
		radiusLabels.add("This part of town (5 miles)");
		radiusLabels.add("This city");
		radiusLabels.add("This region");

		evtInviteLabels.add("Looking for People");
		evtInviteLabels.add("Looking for Events");
		
        
		ArrayAdapter<String> whenLabelsAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, whenLabels);
		whenLabelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		whenSpinner.setAdapter(whenLabelsAdapter );
		
		ArrayAdapter<String> evtInviteLabelsAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, evtInviteLabels);
		evtInviteLabelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eventOrInviteSpinner.setAdapter(evtInviteLabelsAdapter);
		eventOrInviteSpinner.setOnItemSelectedListener(this);
		
		ArrayAdapter<String> radiusLabelsAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, radiusLabels);
		radiusLabelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		distanceToEventSpinner.setAdapter(radiusLabelsAdapter );
		distanceToEventSpinner.setSelection(2);

		//****  GET THE DATA FROM SQLITE INTO ARRAYLISTS
		ArrayList<String> evtTblData = dbq.wholeTableReader(EVENTS_TYPES_TABLE, ",");
		
		int evtItems = evtTblData.size();
		String lastEventTypeLabel="";
		
		for (int i = 0; i < evtItems; i++){
			
			String [] items = evtTblData.get(i).toString().split(",");
			
			if (lastEventTypeLabel.equals(items[2]) == false)
				EventTypesArray.add(items[2]);
		
			lastEventTypeLabel = items[2];
		}
		
		//LOAD EVENT SPINNER
		ArrayAdapter<String> eventTypeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, EventTypesArray);
		eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eventTopicSpinner.setAdapter(eventTypeAdapter);	
		eventTopicSpinner.setSelection(1);

	}

	

	
	@Override
	public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
		
		int spinPosn = eventOrInviteSpinner.getSelectedItemPosition();
		if (spinPosn == 0){
			postButton.setText("Post Availability"); 
			searchButton.setText("Find People");
			
		} else if (spinPosn == 1){
			postButton.setText("Make Event"); 
			searchButton.setText("Find Event");
		}
	}
	
	
	public void showLoadSpinner(){
		ProgressBar pbspnner  = new ProgressBar(this);;
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		LinearLayout.LayoutParams iRL 
			= new LinearLayout.LayoutParams(50, 50);
		spinnerRL.addView(pbspnner,iRL);

	}
	
	public void hideLoadSpinner(){
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		spinnerRL.removeAllViews();
    	
	}
	
	public void gotoNextPage(Class<?> classIn){

	    Intent AutoRegOnBoard = new Intent(this, classIn );
		
	    AutoRegOnBoard.putExtra("Origin", "NEWLFPEVENT");
	    AutoRegOnBoard.putExtra("evtType", eventTopicSpinner.getSelectedItemPosition());
	    
	    startActivity(AutoRegOnBoard);	
	}
	
	//**************************************************
	//GENERAL PAGE UTILITY FUNCTIONS
	//MENU MANAGER:
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
				(LinearLayout)findViewById(R.id.menuLayout)
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
	public void onNothingSelected(AdapterView<?> parent) {}
}
