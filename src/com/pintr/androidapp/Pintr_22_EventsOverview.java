package com.pintr.androidapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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


public class Pintr_22_EventsOverview 	extends 	Activity 
										implements 	OnItemSelectedListener
													, Pintr_PL_03_LoginLoadUserListener { 

	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	
    private static final String EVENTS_TYPES_TABLE = "P_EK_EVTP";
	Spinner eventTypeSp, eventSubtypeSp, timeRangeSp;
	Button newEventBtn;
	HashMap<String,String> EventSubTypesMap = new HashMap<String,String>();
	ArrayList<String> EventTypesArray = new ArrayList<String>();
	String[][] EventToSubEventKeysMap;
	int evtItems ;
	final String YOUR_EVENTS_TABLE = "P_EK_EVTSUM";
    final String YOUR_EVENTS_PARTICIPANT_TABLE = "P_EK_EVTPAR";
	List<String> DateSpinnerList = new ArrayList<String>();
	String allTimeOnTimeSpinnerLabel = "All Time";
	String yourHandle;
	String pintr_uid ;
	String Origin="", input_pintr_uid, input_handler;
	final Context context = this;
	String [] eventDetailLabels = {"Event_id","Event_creator_uid","handler"
									,"Event_DtTm","event_type","type_variant"
									,"event_title","venue_name","venue_address_text"
									,"chg_evt_dttm","hashtags","venue_yelp_id"
									,"venue_yelp_url","invite_status", "event_description"
									, "participant_attending","participant_event_status"};

		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_22_events_overview);

		ProgressBar pbspnner  = new ProgressBar(this);;
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		LinearLayout.LayoutParams iRL 
			= new LinearLayout.LayoutParams(50, 50);
		spinnerRL.addView(pbspnner,iRL);

		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
		PageTitle.setText("Events");
		
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}else{
			Pintr_A_003_EvtPageLoaderAsync loadDtlsTask 
				= new Pintr_A_003_EvtPageLoaderAsync (this);
	        loadDtlsTask.execute();
	        loadDtlsTask.p22 = this;
		}
		
		ArrayList<String> Creds = dbq.UserCreds();
		pintr_uid = Creds.get(2).toString();
		
		
		//******  END MENU MANAGER ******
		
		//GET ANY PASSTHROUGH DATA IF IT EXISTS
		final Bundle extras = getIntent().getExtras();
		if(extras != null){
			Origin = extras.getString ("Origin");
			input_pintr_uid = extras.getString ("input_pintr_uid");
			input_handler = extras.getString ("input_handler");
		}
		
		//SET NEW EVENT BUTTON FUNCTION
		newEventBtn = (Button)findViewById(R.id.makeNewEventButton);
		newEventBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

			    Intent AutoRegOnBoard = new Intent(context, Pintr_23_EventMaker.class);	
			    AutoRegOnBoard.putExtra("Origin", "NEWEVENT");		    
			    startActivity(AutoRegOnBoard);		
			}
		});
	
	}


	@Override
	public void processFinish(String output) {
		
		setEventSpinnerValues();
		
		displayEvents();
		
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		spinnerRL.removeAllViews();
	}  

	public ArrayList<String> readYourEvents(){
		
		String whereClauses = "";
		 
		int eventTypeSelected = eventTypeSp.getSelectedItemPosition();
		int eventSubtypeSelected = eventSubtypeSp.getSelectedItemPosition();
		int timeRangeSelected = timeRangeSp.getSelectedItemPosition();
		
		//PROFILE-BASED INVITE FWDING?
		if(Origin.equals("FWDINVT")){

			whereClauses = whereClauses 
								+ "  AND  (l.invite_status = 'INVT' OR "
								+ "        l.Event_creator_uid = " + pintr_uid 
								+ "       )"
								;
		}
		
		//EVENT TYPE-SUBTYPE SPINNER VALUE EXTRACTION
		if(eventTypeSelected > 0){
			whereClauses = whereClauses + "  AND  l.event_type = '" + eventTypeSelected + "'  " ;
			
			if(eventSubtypeSelected > 0)
				whereClauses = whereClauses + "  AND  l.type_variant = '" + Integer.toString(eventSubtypeSelected) + "'  " ;
		}
		
		//DATE SPINNER EXTRACTION
		if(DateSpinnerList.get(timeRangeSelected).equals(allTimeOnTimeSpinnerLabel) == false){
			
			String timeRangeWhereClStr = "";
			String timeRangeSelectedStr = DateSpinnerList.get(timeRangeSelected);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
			Calendar now = Calendar.getInstance();
			Calendar c = Calendar.getInstance();
			String nextDtTmStr  = "";

			//INCREMENT THE DATES BY THE SELECTED RANGE
			if(timeRangeSelectedStr.equals("Future Events")){
				String nowDtTmStr  = dateFormat.format(now.getTime());
				timeRangeWhereClStr = " AND Event_DtTm >= '" + nowDtTmStr + "'  ";
			}
			else{
				if(timeRangeSelectedStr.equals("Today")){
					c.add(Calendar.DATE, 1);				
				} else if(timeRangeSelectedStr.equals("Tomorrow")){
					now.add(Calendar.DATE, 1);				
					c.add(Calendar.DATE, 2);				
				} else if(timeRangeSelectedStr.equals("This Week")){
					c.add(Calendar.DATE, 7);				
				} else if(timeRangeSelectedStr.equals("Next Week")){
					now.add(Calendar.DATE, 7);				
					c.add(Calendar.DATE, 14);				
				} else if(timeRangeSelectedStr.equals("This Month")){			
					c.add(Calendar.DATE, 31);	
				}
				nextDtTmStr   = dateFormat.format(c.getTime());
				String nowDtTmStr  = dateFormat.format(now.getTime());
				timeRangeWhereClStr = " AND l.Event_DtTm BETWEEN '"
											+ nowDtTmStr
											+ "' AND '"
											+ nextDtTmStr
											+ "'  "
											;
			}
			
			whereClauses = whereClauses  + timeRangeWhereClStr ;
			Log.d("EVENT LISTRER WHERECLZ","-> " + whereClauses );
		}
		
		return  dbq.yourEventsSummaryReader( "#", whereClauses, pintr_uid);
	}
	
	//GET EVENTS INTO DISPLAY
	public void displayEvents(){

		ArrayList<String> yourEventsArLi = readYourEvents();
		LinearLayout eventItemsDisplayArea = (LinearLayout)findViewById(R.id.eventItemsDisplayArea);
		eventItemsDisplayArea.removeAllViews();

		//GET EVENT DATA ONE BY ONE
		for(int i =0; i < yourEventsArLi.size(); i++){
			//EVENT DATA READ:
			Log.d("EVENT ENTRY:",yourEventsArLi.get(i).toString());
			String [] yourEventsCSV = yourEventsArLi.get(i).toString().split("#");
			final String event_id = yourEventsCSV[0];
			String handler = yourEventsCSV[2];
			String Event_DtTm = yourEventsCSV[3];
			String event_type = yourEventsCSV[4];
			String event_title = yourEventsCSV[6];
			String venue_name = yourEventsCSV[7];
			String venue_address_text = yourEventsCSV[8];
			String chg_evt_dttm = yourEventsCSV[9];
			String hashtags = yourEventsCSV[10];
			String venue_yelp_id = yourEventsCSV[11];
			String venue_yelp_url = yourEventsCSV[12];
			String invite_status = yourEventsCSV[13];
			String event_description = yourEventsCSV[14];
			String participant_attending  = yourEventsCSV[15];
			String participant_event_status= yourEventsCSV[16];

			Log.d("participant_event_status", "->" + participant_event_status);
			//ADD THE GENERIC EVENT SUMMARY VIEW
			LinearLayout linearLayout
				= (LinearLayout) View.inflate(this,	R.layout.pintr_g_95_event_summary, null);
			eventItemsDisplayArea.addView(linearLayout);
			
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
			eventDate.setText(dateLabelMaker(Event_DtTm));
			
			TextView eventLocation = (TextView )findViewById(R.id.eventLocation);
			int eventLocationNuID = R.id.eventLocation * 100 +i ;
			eventLocation.setId(eventLocationNuID);
			eventLocation.setText(venue_name + " - " + venue_address_text);

			ImageView eventAttend = (ImageView )findViewById(R.id.user_attend);
			int eventAttendNuID = R.id.user_attend * 100 +i ;
			eventAttend.setId(eventAttendNuID);
			if(participant_attending.equals("1"))
				eventAttend.setImageResource(R.drawable.green_tick_rs);
			else if(participant_attending.equals("0"))
				eventAttend.setImageResource(R.drawable.red_x);
			
			HashMap<String,String> EventDetailsMap = new HashMap<String,String>();
			for(int k=0; k < yourEventsCSV.length; k++)
				EventDetailsMap.put(eventDetailLabels[k], yourEventsCSV[k]);
			
			final HashMap<String,String> EventDetailsMapF = EventDetailsMap;
			
			//ADD ONCLICK LISTENER TO VIEW FULL INDIVIDUAL EVENT
			eventSummaryView.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {

					if(Origin.equals("FWDINVT")){

						Pintr_G_025_InviteFwdr ivfwd = new Pintr_G_025_InviteFwdr(context);
						HashMap<Integer, String> nuParticipants = new HashMap<Integer, String> ();
						nuParticipants.put(Integer.parseInt(input_pintr_uid ),input_handler);
						String fwdRegRes = ivfwd.regFwdInvite(event_id, nuParticipants); 
						Log.d("INVITE FWDED?", fwdRegRes);
						ivfwd.dispatchNotification(nuParticipants);
						
						Intent MenuIntent = new Intent(context, Pintr_16_ProfileDisplayPage.class);
					    MenuIntent.putExtra("pintr_user_id", Integer.parseInt(input_pintr_uid ));
					    startActivity(MenuIntent);
						
					}else{
						Intent MenuIntent = new Intent(context, Pintr_23_EventMaker.class);
					    MenuIntent.putExtra("Origin", "EXISTINGEVENT");
					    MenuIntent.putExtra("EventDetailsMapF", EventDetailsMapF);
					    startActivity(MenuIntent);
					}
				}
			});	

		}
	}
	
	
    //POPULATE EVENT SPINNERS
	public void setEventSpinnerValues(){
		
		eventTypeSp = (Spinner)findViewById(R.id.eventTopicSpinner);
		eventSubtypeSp = (Spinner)findViewById(R.id.eventSubtopicSpinner);
		timeRangeSp = (Spinner)findViewById(R.id.eventDateTimeRangeSpinner);
		
		
		//****  GET THE DATA FROM SQLITE INTO ARRAYLISTS
		ArrayList<String> evtTblData = dbq.wholeTableReader(EVENTS_TYPES_TABLE, ",");
		
		evtItems = evtTblData.size();
		EventToSubEventKeysMap = new String[2][evtItems] ;
		String lastEventTypeLabel="";
		
		for (int i = 0; i < evtItems; i++){
			
			String [] items = evtTblData.get(i).toString().split(",");
			
			if (lastEventTypeLabel.equals(items[2]) == false)
				EventTypesArray.add(items[2]);
			
			EventSubTypesMap.put(items[0] + "_" + items[1],items[3]);
			
			EventToSubEventKeysMap[0][i] = items[0] ;
			EventToSubEventKeysMap[1][i] =  items[0] + "_" + items[1];
			
			lastEventTypeLabel = items[2];
		}
		
		//LOAD EVENT SPINNER
		ArrayAdapter<String> eventTypeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, EventTypesArray);
		eventTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eventTypeSp.setAdapter(eventTypeAdapter);	

		//ADD ITEMSELECT LISTENER
		eventTypeSp.setOnItemSelectedListener(this);
		eventSubtypeSp.setOnItemSelectedListener(this);
		timeRangeSp.setOnItemSelectedListener(this);
		
		//LOAD TIMEPERIOD SPINNER
		datelistSpinnerPop();
		ArrayAdapter<String> DateRangeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, DateSpinnerList);
		DateRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		timeRangeSp.setAdapter(DateRangeAdapter);	
					
	}
	
	public void datelistSpinnerPop(){
		//DEFINE TIMEPERIOD SPINNER VALUES
		DateSpinnerList.add("Future Events");
		DateSpinnerList.add("Today");
		DateSpinnerList.add("Tomorrow");
		DateSpinnerList.add("This Week");
		DateSpinnerList.add("Next Week");
		DateSpinnerList.add("This Month");
		DateSpinnerList.add(allTimeOnTimeSpinnerLabel);
	}
	
	
	public String dateLabelMaker(String Event_DtTm){
		SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
		SimpleDateFormat displayFormat1 = new SimpleDateFormat("h.mm aa",Locale.getDefault());
		SimpleDateFormat displayFormat2 = new SimpleDateFormat("E, MMM d, yyyy",Locale.getDefault());
		
		Date convertedDate = new Date();
        try {
			convertedDate = datetimeFormat.parse(Event_DtTm);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        String convertedDateStr = dayFormat.format(convertedDate);
		Calendar now = Calendar.getInstance();
		String nowDayStr  = dayFormat.format(now.getTime());
		now.add(Calendar.DAY_OF_YEAR, 1);
		String tomorrowDayStr  = dayFormat.format(now.getTime());
		String eventDatelabel;
		
		if(convertedDateStr.equals(nowDayStr)){
			eventDatelabel = "Today, " + displayFormat1.format(convertedDate.getTime());
		} else if(convertedDateStr.equals(tomorrowDayStr)){

			eventDatelabel = "Tomorrow, " + displayFormat1.format(convertedDate.getTime());
		} else
			eventDatelabel = displayFormat2.format(convertedDate.getTime());

		return eventDatelabel;
	}
	
	public void loadSubEventSpinner(int position){

    	//LOAD THE APPROPRIATE SUBTYPES OF EVENTS INTO THE SUBTYPE SPINNER
        ArrayList<String> evtSubTypeIds = new ArrayList<String> ();
        ArrayList<String> evtSubTypeLabels = new ArrayList<String> ();

		for(int i=0; i < evtItems; i++)			
			if(Integer.parseInt(EventToSubEventKeysMap[0][i]) == position)
				evtSubTypeIds.add(EventToSubEventKeysMap[1][i]);
		
		for(int i=0; i < evtSubTypeIds.size(); i++)
			evtSubTypeLabels.add(EventSubTypesMap.get(evtSubTypeIds.get(i)) );
		
		ArrayAdapter<String> eventSubtypeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, evtSubTypeLabels);
		eventSubtypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eventSubtypeSp.setAdapter(eventSubtypeAdapter );
	}

	@Override
	public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
    	
		 switch(spinner.getId()) {
		 
	        case R.id.eventTopicSpinner:
	        	loadSubEventSpinner(position);
				//RETURN EVENTS BASED ON SELECTION
				displayEvents();
				break;
				
	        case R.id.eventSubtopicSpinner:
	        	//RETURN EVENTS BASED ON SELECTION
				displayEvents();
				break;

	        case R.id.eventDateTimeRangeSpinner:
	        	//RETURN EVENTS BASED ON SELECTION
				displayEvents();
				break;
		 }
		
    }
	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
    }
	

	//**************************************************
	//GENERAL PAGE UTILITY FUNCTIONS
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
