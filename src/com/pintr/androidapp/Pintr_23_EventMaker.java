package com.pintr.androidapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.text.DateFormatSymbols;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;


public class Pintr_23_EventMaker extends Activity implements OnItemSelectedListener {

	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_021_YourEventsReader yevr = new Pintr_G_021_YourEventsReader(this);

    private static final String EVENTS_TYPES_TABLE = "P_EK_EVTP";  
	
	Spinner event_Day, event_Month, event_Year, event_Hour, event_Minute
			, eventTypeSp, eventSubtypeSp, eventInviteStatusSp;
	String[][] EventToSubEventKeysMap;
	HashMap<String,String> participantsMap = new HashMap<String,String>();
	HashMap<String,String> EventSubTypesMap = new HashMap<String,String>();
	HashMap<String,String> EventDetailsMapF, nuParticipants;
	ArrayList<String> EventTypesArray = new ArrayList<String>();
	ArrayList <String> participantArray = new ArrayList <String>();
    ArrayList<String> evtSubTypeLabels = new ArrayList<String> ();
    List<String> InviteStatusList = new ArrayList<String>();
    Button attendEventButton, skipEventButton, venueAddButton
    		, inviteesAddButton, makeEventButton, inviteesFwdButton
		    , requestInviteButton ;
    ImageView user_attend_symbol ;
    int count=0 , countMts =0;
    HashMap<Integer, String> participantUIDs = new HashMap<Integer, String>();
	int evtItems;
	String eventTypeSpStr, eventTitleInput, eventDate, Origin,  OrganiserData = "" 
				, participant_attending="", Event_ID, invite_status = "OPEN", participant_event_status
				,eventDescInput , eventHashtagInput
				, curr_venue_latitude, curr_venue_longitude
				;
	Context context = this;
	EditText eventTitle, eventDesc, eventHashtag ;
	String [] eventDetailLabels = {"Event_id","Event_creator_uid","handler"
									,"Event_DtTm","event_type","type_variant"
									,"event_title","venue_name","venue_address_text"
									,"chg_evt_dttm","hashtags","venue_yelp_id"
									,"venue_yelp_url","invite_status", "event_description"
									,"participant_event_status", "participant_attending"};

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_23_event_maker);
		getWindow().setSoftInputMode(
			    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
			);
		
		attendEventButton = (Button )findViewById(R.id.attendEventButton);
		skipEventButton = (Button )findViewById(R.id.skipEventButton);
		inviteesFwdButton = (Button)findViewById(R.id.inviteesFwdButton );
		venueAddButton = (Button )findViewById(R.id.venueAddButton);
		makeEventButton = (Button )findViewById(R.id.makeEventButton);
		inviteesAddButton = (Button )findViewById(R.id.inviteesAddButton);
		requestInviteButton = (Button )findViewById(R.id.requestInviteButton );
		user_attend_symbol = (ImageView)findViewById(R.id.user_attend_symbol);
		
		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}

		LinearLayout eventAttendingHeaderArea = (LinearLayout)findViewById(R.id.eventAttendingHeaderArea);

		eventTitle = (EditText)findViewById(R.id.eventTitle);
		eventDesc = (EditText)findViewById(R.id.eventDescription);
		eventHashtag  = (EditText)findViewById(R.id.eventHashtag);
		event_Day = (Spinner)findViewById(R.id.event_Day);
		event_Month = (Spinner)findViewById(R.id.event_Month);
		event_Year = (Spinner)findViewById(R.id.event_Year);
		event_Hour = (Spinner)findViewById(R.id.event_hour);
		event_Minute = (Spinner)findViewById(R.id.event_minute);

		
		//POPULATE EVENT DETAILS ALREDY SET
		setDateSpinnerValues( event_Year, event_Month, event_Day,  event_Hour, event_Minute);
		setEventSpinnerValues();
		
		//EXTRACT ANY PASSTHRU DATA
		Bundle extras = getIntent().getExtras();	
	    Origin = extras.getString ("Origin");
		Log.d("CALL ORIGIN", Origin);
		
		if(Origin.equals("EXISTINGEVENT")){
	
			EventDetailsMapF 
				= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");
			Log.d("EVENTID:", "-> " + EventDetailsMapF.get("Event_id"));
			participantArray = dbq.yourEventParticipantsReader(EventDetailsMapF.get("Event_id"));
			if( EventDetailsMapF != null){
				populateEventWithData(EventDetailsMapF);

				TextView inviteRequestedLabel = (TextView)findViewById(R.id.inviteRequestedLabel);
				if(EventDetailsMapF.get("invite_status").equals("LFP"))
					inviteRequestedLabel.setVisibility(View.VISIBLE);
			}
			venueAddButton.setText("Change Venue");
			inviteesAddButton.setVisibility(View.GONE);
			makeEventButton.setText("Change Event");
		    LinearLayout invitesRequestedDisplayArea = (LinearLayout)findViewById(R.id.invitesRequestedDisplayArea);
		    invitesRequestedDisplayArea.setVisibility(View.VISIBLE);
		   
		} else if(Origin.equals("EXISTINGEVENTLFP")){
				
				EventDetailsMapF 
					= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");
				Log.d("EVENTID:", "-> " + EventDetailsMapF.get("Event_id"));
				participantArray = dbq.yourEventParticipantsReader(EventDetailsMapF.get("Event_id"));
				if( EventDetailsMapF != null)
					populateEventWithData(EventDetailsMapF);
				venueAddButton.setText("Change Venue");
				inviteesAddButton.setVisibility(View.GONE);
				makeEventButton.setText("Change Event"); 
			    requestInviteButton.setVisibility(View.VISIBLE);
				LinearLayout eventInviteesArea = (LinearLayout)findViewById(R.id.eventInviteesArea);
				RelativeLayout eventInviteesHeaderArea = (RelativeLayout )findViewById(R.id.eventInviteesHeaderArea);
				
				Log.d("YOURPARTSTAT", " -> " + EventDetailsMapF.get("participant_event_status"));
				if(EventDetailsMapF.get("participant_event_status").equals("ORG")){
					requestInviteButton.setVisibility(View.GONE);
					eventInviteesHeaderArea.setVisibility(View.VISIBLE);
					eventInviteesArea.setVisibility(View.VISIBLE);
					
				} else {
					eventInviteesHeaderArea.setVisibility(View.GONE);
					eventInviteesArea.setVisibility(View.GONE);
				}

			
			} else if(Origin.equals("FWDIVT")){

			EventDetailsMapF 
				= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");

			nuParticipants
				= (HashMap<String, String>) getIntent().getSerializableExtra("nuParticipants");
			participantArray = dbq.yourEventParticipantsReader(EventDetailsMapF.get("Event_id"));
			if( EventDetailsMapF != null)
				populateEventWithData(EventDetailsMapF);
			venueAddButton.setText("Change Venue");
			inviteesAddButton.setVisibility(View.VISIBLE);
		
		} else if(Origin.equals("ADDPPL")){

			OrganiserData = extras.getString ("OrganiserData");
			final HashMap<Integer, String> participants 
				= (HashMap<Integer, String>) getIntent().getSerializableExtra("participants");
			EventDetailsMapF 
				= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");

			populateEventWithData(EventDetailsMapF);
			Iterator<Integer> myVeryOwnIterator = participants.keySet().iterator();
			while(myVeryOwnIterator.hasNext()) {

				Integer key=myVeryOwnIterator.next();
				Log.d(Integer.toString(key), participants.get(key));
				participantArray.add(Integer.toString(key) + ",ATT,," + participants.get(key));
			}
			if( EventDetailsMapF != null)
				populateEventWithData(EventDetailsMapF);
			inviteesFwdButton.setVisibility(View.GONE);
			eventAttendingHeaderArea.setVisibility(View.GONE);
			if(EventDetailsMapF.containsKey("venue_address_text")){
				if(EventDetailsMapF.get("venue_address_text").equals("")==false 
						&& EventDetailsMapF.get("venue_address_text").equals(null) == false)
					venueAddButton.setText("Change Venue");
			}
		
		} else if(Origin.equals("NEWEVENT")){

			ArrayList<String> Creds = dbq.UserCreds();
			OrganiserData  = Creds.get(2).toString() + ",,ORG," + Creds.get(3).toString() ;
			Log.d("ORG", OrganiserData );
			displayOrganizer();
			setDtTmSpinnersToNow(Calendar.getInstance());
			inviteesFwdButton.setVisibility(View.GONE);
			eventAttendingHeaderArea.setVisibility(View.GONE);
			EventDetailsMapF = new HashMap<String,String>();
			loadSubEventSpinner(0);
			
		} else if(Origin.equals("NEWLFPEVENT")){
 
			ArrayList<String> Creds = dbq.UserCreds();
			OrganiserData  = Creds.get(2).toString() + ",,ORG," + Creds.get(3).toString() ;
			Log.d("ORG", OrganiserData );
			displayOrganizer();
			setDtTmSpinnersToNow(Calendar.getInstance());
			inviteesFwdButton.setVisibility(View.GONE);
			eventAttendingHeaderArea.setVisibility(View.GONE);
			EventDetailsMapF = new HashMap<String,String>();
			eventInviteStatusSp.setSelection(2);
		    eventTypeSp.setSelection(extras.getInt("evtType"));

		    loadSubEventSpinner(0);
			
		} else if(Origin.equals("VENUEFINDER_NU")){

			OrganiserData = extras.getString ("OrganiserData");
			final HashMap<Integer, String> participants 
				= (HashMap<Integer, String>) getIntent().getSerializableExtra("participants");
			Iterator<Integer> myVeryOwnIterator = participants.keySet().iterator();
			while(myVeryOwnIterator.hasNext()) {
				Integer key=myVeryOwnIterator.next();
				Log.d(Integer.toString(key), participants.get(key));
				participantArray.add(Integer.toString(key) + ",ATT,," + participants.get(key));
			}
			EventDetailsMapF 
				= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");
			
			populateEventWithData(EventDetailsMapF);
			
			if(EventDetailsMapF.containsKey("venue_address_text")){
				if(EventDetailsMapF.get("venue_address_text").equals("")==false 
					&& EventDetailsMapF.get("venue_address_text").equals(null) == false)
				venueAddButton.setText("Change Venue");
			}
			inviteesFwdButton.setVisibility(View.GONE);
			eventAttendingHeaderArea.setVisibility(View.GONE);
			int variantIndex = Integer.parseInt(EventDetailsMapF.get("type_variant"));
			eventSubtypeSp.setSelection(variantIndex);
			
		} else if(Origin.equals("VENUEFINDER_EX")){
			
			OrganiserData = extras.getString ("OrganiserData");
			final HashMap<Integer, String> participants 
				= (HashMap<Integer, String>) getIntent().getSerializableExtra("participants");
			EventDetailsMapF 
				= (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");
			Iterator<Integer> myVeryOwnIterator = participants.keySet().iterator();
			while(myVeryOwnIterator.hasNext()) {
				Integer key=myVeryOwnIterator.next();
				Log.d(Integer.toString(key), participants.get(key));
				participantArray.add(Integer.toString(key) + ",ATT,," + participants.get(key));
			}
			if( EventDetailsMapF != null)
				populateEventWithData(EventDetailsMapF);
			venueAddButton.setText("Change Venue");
			inviteesAddButton.setVisibility(View.GONE);
			makeEventButton.setText("Change Event");
		
		}


		venueAddButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				switchInterface(Pintr_24_YelpVenueFinder.class,  Origin);
			}
		});
		
		makeEventButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d("Origin", Origin);
				if(Origin.equals("NEWEVENT")
						 || Origin.equals("VENUEFINDER_NU")
						 || Origin.equals("NEWLFPEVENT")
						 || Origin.equals("ADDPPL"))
					makeEventActions("NEW");
				else if(Origin.equals("EXISTINGEVENT") || Origin.equals("VENUEFINDER_EX"))
					makeEventActions("CHG");
			}
		});

		inviteesAddButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				switchInterface(Pintr_21_MessageRecipientPicker.class,  "EVENTS");
			}
		});


		attendEventButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				participant_attending = "1";
				user_attend_symbol.setImageResource(R.drawable.green_tick_rs);
				attendEventButton.setVisibility(View.GONE);
				skipEventButton.setVisibility(View.VISIBLE);
				//TODO 
				Log.d("ATT - LOGGED ATT STAT", " -> " + EventDetailsMapF.get("participant_event_status"));
				postParticipation(EventDetailsMapF.get("participant_event_status"));
				skipEventButton.setText("Cancel");
			}
		});

		skipEventButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

				participant_attending = "0";
				Log.d("SKIP - LOGGED ATT STAT", " -> " + EventDetailsMapF.get("participant_event_status"));
				postParticipation(EventDetailsMapF.get("participant_event_status"));
				user_attend_symbol.setImageResource(R.drawable.red_x);
				skipEventButton.setVisibility(View.GONE);
				attendEventButton.setVisibility(View.VISIBLE);
				attendEventButton.setText("Go after all");
			}
		});	
		
		inviteesFwdButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				switchInterface(Pintr_21_MessageRecipientPicker.class,  "EVTFWD");
			}
		});	
		

		requestInviteButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				participant_attending = "1";
				postParticipation("RFI");
			}
		});

	}
	
	
	
	//POPULATE EVENT VIEW WITH EVENT DATA 
	public void populateEventWithData(HashMap<String,String> EventDetailsIn){
		 
		//SET INVITEES
		if (!Origin.equals("EXISTINGEVENTLFP") || EventDetailsIn.get("participant_event_status").equals("ORG")){
			displayParticipants();

			//SET PARTICIPANT UID ARRAY
			for(int i =0 ; i < participantArray.size(); i++){
				String [] participantStr = participantArray.get(i).split(",");
				participantUIDs.put(Integer.parseInt(participantStr[0]), participantStr[3]);
			}
			
			//GET PARTICIPANT STATUS
			if(!EventDetailsIn.get("participant_event_status").equals("ORG")){
				event_Year.setEnabled(false);
				event_Day.setEnabled(false);
				event_Month.setEnabled(false);
				event_Hour.setEnabled(false);
				event_Minute.setEnabled(false);
				eventTypeSp.setEnabled(false);
				eventSubtypeSp.setEnabled(false);
				eventTitle.setKeyListener(null);
				eventInviteStatusSp.setEnabled(false);
				venueAddButton.setVisibility(View.GONE);
				makeEventButton.setVisibility(View.GONE);
			}
		}
			
		
		//GET EVENT ID
		Event_ID = EventDetailsIn.get("Event_id");
		invite_status = EventDetailsIn.get("invite_status");
		
		if(invite_status.equals("INVT")) eventInviteStatusSp.setSelection(0);
		else if(invite_status.equals("OPEN")) eventInviteStatusSp.setSelection(1);
		else if(invite_status.equals("LFP")) eventInviteStatusSp.setSelection(2);
		if(invite_status.equals("INVT")
				&& !EventDetailsIn.get("participant_event_status").equals("ORG")){
			inviteesFwdButton.setVisibility(View.GONE);
		}

		//SET TITLE
		eventTitleInput = EventDetailsIn.get("event_title");
		eventTitle.setText(eventTitleInput);
		eventDescInput = EventDetailsIn.get("event_description");
		eventDesc.setText(eventDescInput);
		eventHashtagInput = EventDetailsIn.get("hashtags");
		eventHashtag.setText(eventHashtagInput);

		
		
		//SET EVENT DATETIME
		String Event_DtTm = EventDetailsIn.get("Event_DtTm");
		Log.d("INPUT Event_DtTm", Event_DtTm);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
		Date Event_DtTmDate = null;
		final Calendar cal = Calendar.getInstance();
	    try {
			Event_DtTmDate = simpleDateFormat.parse(Event_DtTm);
		    cal.setTime(Event_DtTmDate);
		} catch (ParseException e) {e.printStackTrace();}
	    setDtTmSpinnersToNow(cal);

	    
		int eventIndex = Integer.parseInt(EventDetailsIn.get("event_type"));
		int variantIndex = Integer.parseInt(EventDetailsIn.get("type_variant"));
		//int  inviteIndex = EventTypesArray.indexOf(EventDetailsIn.get("event_type"));
		
		Log.d("TYPE/SUBTYPE", EventDetailsIn.get("event_type") + "/" + EventDetailsIn.get("event_type"));
		
		//SET EVENT TYPE
		eventTypeSp.setSelection(eventIndex );
		loadSubEventSpinner(eventIndex );
		eventSubtypeSp.setSelection(variantIndex);
		

		//GET PARTICIPATION ATTENDANCE STATUS
		participant_attending = EventDetailsIn.get("participant_attending");
		Log.d("participant_attending", " - > " + participant_attending);
		if(participant_attending.equals("1")){
			
			user_attend_symbol.setImageResource(R.drawable.green_tick_rs);
			attendEventButton.setVisibility(View.GONE);
			skipEventButton.setText("Cancel");
		} else if(participant_attending.equals("0")){
			
			user_attend_symbol.setImageResource(R.drawable.red_x);
			skipEventButton.setVisibility(View.GONE);
			attendEventButton.setText("Go after all");
		}
		
		//DISPLAY THE VENUE 
		LinearLayout venueDisplayArea = (LinearLayout)findViewById(R.id.venueDisplayArea);
		Log.d("venue_name", "venue_name -> " + EventDetailsIn.get("venue_name"));
		Log.d("venue_address_text", "venue_address_text -> '" + EventDetailsIn.get("venue_address_text") + "'");
		Log.d("venue_yelp_url", "venue_yelp_url -> '" + EventDetailsIn.get("venue_yelp_url")+ "'");
		
		if(EventDetailsIn.containsKey("venue_address_text")){
			if(EventDetailsIn.get("venue_address_text").trim().equals(null)==false
					&& EventDetailsIn.get("venue_address_text").trim().equals("") ==false	)	{
				Log.d("All GOOD", "ALL GOOD");
				displayEvents(	EventDetailsIn.get("venue_name")
								,EventDetailsIn.get("venue_address_text")
								, EventDetailsIn.get("venue_yelp_url")
								);
			} else venueDisplayArea.setVisibility(View.GONE);
			
		} else venueDisplayArea.setVisibility(View.GONE);
	}
	
	public void setDtTmSpinnersToNow(Calendar cal){
		int EventDtTmYear = cal.get(Calendar.YEAR);
		int EventDtTmMonth = cal.get(Calendar.MONTH);
		int EventDtTmDay = cal.get(Calendar.DAY_OF_MONTH);
		int EventDtTmHour = cal.get(Calendar.HOUR_OF_DAY);
		int EventDtTmMin = cal.get(Calendar.MINUTE);

		int EventDtTmMinIndex = 0;
		for(int i =0; i < 12; i++)
			if(EventDtTmMin >= i*5 && EventDtTmMin < (i+1)*5)
				EventDtTmMinIndex = i;
		

		event_Month.setSelection(EventDtTmMonth);
		event_Year.setSelection(2015 - EventDtTmYear);
		event_Hour.setSelection(EventDtTmHour);
		event_Minute.setSelection(EventDtTmMinIndex);
		setDayFromMonth(EventDtTmMonth);
		event_Day.setSelection(EventDtTmDay-1);
		}
	
	
	//GET EVENT DATA ENTERED BY USER
	public void getEventData(){

		String event_DayStr = event_Day.getSelectedItem().toString();
		String event_MonthStr = Integer.toString(event_Month.getSelectedItemPosition() + 1);
		String event_YearStr = event_Year.getSelectedItem().toString();
		String event_HourStr = event_Hour.getSelectedItem().toString();
		String event_MinuteStr = event_Minute.getSelectedItem().toString();
		
		String eventDtTm = ""
							+ "" + event_YearStr 
							+ "-" + String.format("%02d", Integer.parseInt(event_MonthStr)) 
							+ "-" + String.format("%02d", Integer.parseInt(event_DayStr))
							+ " " + String.format("%02d", Integer.parseInt(event_HourStr))
							+ ":" + event_MinuteStr 
							+ ":00"
							;

		eventTypeSpStr = eventTypeSp.getSelectedItem().toString();
		String eventSubtypeSpStr = eventSubtypeSp.getSelectedItem().toString();
		String eventInviteStatusSpStr = eventInviteStatusSp.getSelectedItem().toString();
		Log.d("VALUE @ eventTypeSpStr", eventTypeSpStr);
		Log.d("VALUE @ eventSubtypeSpStr ", eventSubtypeSpStr );
		Log.d("VALUE @ eventInviteStatusSpStr ", eventInviteStatusSpStr );

		if(eventInviteStatusSpStr.equals("Invite")) invite_status= "INVT";
		else if(eventInviteStatusSpStr.equals("Open")) invite_status= "OPEN";
		else if(eventInviteStatusSpStr.equals("LFP")) invite_status= "LFP";

		eventTitleInput = eventTitle.getText().toString();
		eventDescInput = eventDesc.getText().toString(); 
		eventHashtagInput = eventHashtag.getText().toString();
		if(Origin.equals("NEWEVENT")  ||  Origin.equals("NEWLFPEVENT"))
			participant_event_status="ORG";
		else participant_event_status = EventDetailsMapF.get("participant_event_status");

		EventDetailsMapF.put("event_title", eventTitleInput);
		EventDetailsMapF.put("Event_DtTm", eventDtTm);
		EventDetailsMapF.put("event_type", Integer.toString(EventTypesArray.indexOf(eventTypeSpStr)));
		EventDetailsMapF.put("type_variant", Integer.toString(evtSubTypeLabels.indexOf(eventSubtypeSpStr)));
		EventDetailsMapF.put("event_description", eventDescInput);
		EventDetailsMapF.put("hashtags", eventHashtagInput);
		EventDetailsMapF.put("participant_event_status", participant_event_status);
		EventDetailsMapF.put("participant_attending", participant_attending);
		EventDetailsMapF.put("invite_status", invite_status);
	}
	
	//GET EVENTS INTO DISPLAY
	public void displayEvents(String venueName, String venueLocation, final String venueURL){
		
		LinearLayout venueDisplayArea = (LinearLayout)findViewById(R.id.venueDisplayArea);
		venueDisplayArea.removeAllViews();

		//ADD THE GENERIC EVENT SUMMARY VIEW
		LinearLayout linearLayout
			= (LinearLayout) View.inflate(this,	R.layout.pintr_g_94_venue_summary, null);
		venueDisplayArea.addView(linearLayout);
		
		TextView venueTitleVw = (TextView )findViewById(R.id.venueTitle);
		if(!venueName.equals(""))
			venueTitleVw.setText(venueName);
		else
			venueTitleVw.setVisibility(View.GONE);
		TextView venueLocationVw = (TextView )findViewById(R.id.venueLocation);
		venueLocationVw .setText(venueLocation.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", "").replaceAll(",", ", "));
		
		venueDisplayArea.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(venueURL));
				startActivity(browserIntent);
			}
		});	
	}
		

	
	//GET PARTICIPANTS INTO DISPLAY
	public void displayParticipants(){
		
		LinearLayout participantDisplayArea = (LinearLayout)findViewById(R.id.eventInviteesDisplayArea);
		LinearLayout invitesRequestedDisplayArea = (LinearLayout)findViewById(R.id.invitesRequestedDisplayArea);
		
		
		for(int i =0 ; i < participantArray.size(); i++){
			LinearLayout linearLayout 
				= (LinearLayout) View.inflate(this,	R.layout.pintr_g_93_participant_view, null);
			
			String [] participantStr = participantArray.get(i).split(",");
			Log.d("PARTICIPANTS LOGGED:", "-> " + participantArray.get(i));
			Log.d("WRITE PARTIC",participantStr[3]);
			
			if(participantStr[1].equals("ORG"))
				OrganiserData = participantArray.get(i);
			else {
				if(participantStr[1].equals("ATT"))
					participantDisplayArea.addView(linearLayout);
				
				else if(participantStr[1].equals("RFI"))
					invitesRequestedDisplayArea.addView(linearLayout);
					
				LinearLayout participantSummaryView = (LinearLayout)findViewById(R.id.participantSummaryView);
				int participantSummaryViewNuID = R.id.participantSummaryView * 100 +i ;
				participantSummaryView.setId(participantSummaryViewNuID);
				
				TextView user_idVw = (TextView )findViewById(R.id.user_id);
				int user_idVwNuID = R.id.user_id * 100 +i ;
				user_idVw.setId(user_idVwNuID);
				user_idVw.setText(participantStr[3]);
				
				if(participantStr[1].equals("ATT"))
				{
					ImageView eventAttend = (ImageView )findViewById(R.id.user_attend);
					int eventAttendNuId = R.id.user_attend * 100 +i ;
					eventAttend.setId(eventAttendNuId);
					eventAttend.setVisibility(View.VISIBLE);
					
					if(participantStr[2].equals("1"))
							eventAttend.setImageResource(R.drawable.green_tick_rs);
					else if(participantStr[2].equals("0"))
							eventAttend.setImageResource(R.drawable.red_x);
				}
				
				else if(participantStr[1].equals("RFI")){

					final String uid = participantStr[0];
					Button requestInviteRespondButton = (Button)findViewById(R.id.requestInviteRespondButton);
					int rqstAttendNuId = R.id.requestInviteRespondButton * 100 +i ;
					requestInviteRespondButton.setId(rqstAttendNuId);
					requestInviteRespondButton.setVisibility(View.VISIBLE);
					requestInviteRespondButton.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
							inviteRequestedResponse(uid );
						}
					});	
				}
				
				

				final String uid = participantStr[0];
				linearLayout.setOnClickListener(new View.OnClickListener() {			
					@Override
					public void onClick(View v) {
		
						Intent MenuIntent = new Intent(context, Pintr_16_ProfileDisplayPage.class);
					    MenuIntent.putExtra("pintr_user_id", uid);
//						    startActivity(MenuIntent);	
					}
				});		
			}
		}
		displayOrganizer();
	}

	

	public void inviteRequestedResponse(final String  uid){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		
		//FRIENDS - DEFRIEND
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	postAcceptDenyAttend("ATT", "0", uid);
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //"NO" BUTTON CLICKED
			        	postAcceptDenyAttend("RFI", "1", uid);
			            break;
			        }
			    }
			};

		builder.setMessage("Do you want to add this person to the event?")
						.setPositiveButton("Yes", dialogClickListener)
						.setNegativeButton("No", dialogClickListener)
						.show()
						;
	}
	
	public void postParticipation(String origin){

		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		
		String response = yevr.setAttendance(email, password, Event_ID, participant_attending, origin, "", "" );
		Log.d("PARTICIPATION RESPONSE",response );
	}
	

	public void postAcceptDenyAttend(String origin, String delfn, String uid ){

		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		
		String response = yevr.setAttendance(email, password, Event_ID, participant_attending, origin, delfn , uid );
		Log.d("PARTICIPATION RESPONSE",response );
	}
	
	public void switchInterface(Class<?> classIn, String originIn){
		getEventData();
		
		Intent ActivityIntent = new Intent(context, classIn);
		ActivityIntent.putExtra("EventDetailsMapF", EventDetailsMapF);
		ActivityIntent.putExtra("participants", participantUIDs);
		ActivityIntent.putExtra("Origin", originIn);
		ActivityIntent.putExtra("OrganiserData", OrganiserData);
	    startActivity(ActivityIntent);	
	}
	

	public void makeEventActions(String ChgType){
		
		getEventData();

		Log.d("EVT LATITUDE", "->" + EventDetailsMapF.get("curr_venue_latitude"));
		Log.d("EVT LONGITUDE","->" + EventDetailsMapF.get("curr_venue_longitude"));
		TextView errorsDisplay = (TextView)findViewById(R.id.errorsDisplay);
		if(eventTypeSpStr.equals("no prefs"))
			errorsDisplay.setText("Please select an event type");
		else if(eventTitleInput.equals(""))
			errorsDisplay.setText("Please enter a title for the event");
		else if(EventDetailsMapF.containsKey("venue_address_text") == false)
			errorsDisplay.setText("You haven't added a location for the event");
		else if(EventDetailsMapF.get("venue_address_text").equals(null)
				|| EventDetailsMapF.get("venue_address_text").equals("") )
			errorsDisplay.setText("Please select a location for the event");	
		else {
			ArrayList<String> itemNames = new ArrayList<String>();
			ArrayList<String> itemValues = new ArrayList<String>();
			
			ArrayList<String> Creds = dbq.UserCreds();
			String email = Creds.get(0).toString();
			String password = Creds.get(1).toString();
			String pintr_uid = Creds.get(2).toString();
			String your_handler = Creds.get(3).toString();

			itemNames.add("email");
			itemNames.add("password");

			itemValues.add(email);
			itemValues.add(password );
			
			Iterator<String> myVeryOwnIterator = EventDetailsMapF.keySet().iterator();
			while(myVeryOwnIterator.hasNext()) {
				String key=myVeryOwnIterator.next();
				Log.d(key, EventDetailsMapF.get(key));
				itemNames.add(key);
				if(key.equals("venue_address_text")){
					String x = EventDetailsMapF.get(key);
					itemValues.add(x.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", ""));
				} else					
					itemValues.add(EventDetailsMapF.get(key));
				Log.d("ADDED KEY/VAL", key + "/" + EventDetailsMapF.get(key));
			} 
			
			itemNames.add("Event_participant_uid_list");
			itemNames.add("Event_participant_status_list");
			
			String participantStr =  pintr_uid;
			String participantStatusStr = "ORG" ;
			
			if(participantArray.size() >0 )
				for(int i =0 ; i < participantArray.size(); i++){
					String sep =  ",";
					String orgStat = "";
					String [] participantArr = participantArray.get(i).split(",");
					
					participantStr = participantStr + sep + participantArr[0] ;
					if(participantArr[0].equals(EventDetailsMapF.get("Event_creator_uid")))
						orgStat = "ORG";
					else 
						orgStat = "ATT";
					participantStatusStr = participantStatusStr + sep + orgStat;
					participantsMap.put(participantArr[0], participantArr[3]);
				}
			itemValues.add(participantStr);
			itemValues.add(participantStatusStr);
			
			String response = makeEvent(itemNames, itemValues, ChgType);
			
			if(response.equals("SUCCESS")){
				
				//SEND MESSAGE NOTIFICATION TO PARTICIPANTS
				final Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
				myVeryOwnIterator = participantsMap.keySet().iterator();
				while(myVeryOwnIterator.hasNext()) {
					String key=myVeryOwnIterator.next();
					if (key != pintr_uid){
						String ServerResponse = 
								gcmuf.dispatchMessageToServer(	key
																,email
																,password
																,your_handler  + " has sent you an invite " 
																,"INV"
																);
						Log.d("ServerResponse", ServerResponse);	
					}
				}
				Intent ActivityIntent = new Intent(this, Pintr_22_EventsOverview.class);
				startActivity(ActivityIntent);	
			}
			else 
				errorsDisplay.setText("Event did not dispatch, check your connection and try again");
		}
	}
	
	public String makeEvent(List<String> inputVariables,List<String> inputValues, String ChgType ){
		
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		String queryAddress ="";
		if(ChgType.equals("NEW"))
			queryAddress ="http://www.pickmemycar.com/pintr/EADFD/EVFDMK077.php";
		else if(ChgType.equals("CHG"))
			queryAddress ="http://www.pickmemycar.com/pintr/EADFD/EVFDUD070.php";
		Log.d("DISPATCH EVENT TO:", "/" + queryAddress );
		
		return rDB.getDBQuery(inputVariables
								,inputValues
								,queryAddress);
		
	}
	
	public void displayOrganizer(){
		TextView organizersDisplay = (TextView)findViewById(R.id.organizersDisplay);
		Log.d("OrganiserData", "OD -> " + OrganiserData);
		organizersDisplay.setText(OrganiserData.split(",")[3]);
	}

	//POPULATE  DATE SPINNERS
	public void setDateSpinnerValues(
					Spinner YearSpinner
					, Spinner MonthSpinner
					, Spinner  DaySpinner
					, Spinner  HourSpinner
					, Spinner  MinuteSpinner
					){
		
		//Year spinner
		int year = Calendar.getInstance().get(Calendar.YEAR);
		List<String> yearSpinnerList = new ArrayList<String>();
		for(int i = year; i >= 2014; i--){
			yearSpinnerList.add(Integer.toString(i));			
		}
		ArrayAdapter<String> YearrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, yearSpinnerList);
		YearrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		YearSpinner.setAdapter(YearrangeAdapter );	
		
		//Month spinner
		List<String> monthSpinnerList = new ArrayList<String>();
		for(int i=1; i < 13; i++){
			monthSpinnerList.add(formatMonth(i));			
		}
		ArrayAdapter<String> MonthrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, monthSpinnerList);
		MonthrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		MonthSpinner.setAdapter(MonthrangeAdapter );	
	
	
		//Day spinner
		List<String> daySpinnerList = new ArrayList<String>();
		for(int i=1; i < 31; i++){
			daySpinnerList.add(Integer.toString(i));		
		}
		ArrayAdapter<String> DayrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, daySpinnerList);
		DayrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		DaySpinner.setAdapter(DayrangeAdapter );
		

		event_Month.setOnItemSelectedListener(this);
		Log.d("MONTH INVOKED AT","setOnItemSelectedListener");
		
		//HOUR SPINNER
		List<String> hourSpinnerList = new ArrayList<String>();
		for(int i=0; i < 24; i++){
			hourSpinnerList.add(Integer.toString(i));			
		}
		ArrayAdapter<String> hourRangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, hourSpinnerList);
		hourRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		HourSpinner.setAdapter(hourRangeAdapter );
		
		
		//MINUTE SPINNER
		List<String> minutesList = new ArrayList<String>();
		for(int i =0; i < 12; i++){
			int mins = i*5;
			minutesList.add(String.format("%02d", mins));
		}
		ArrayAdapter<String> MinutesRangeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, minutesList);
		MinutesRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		MinuteSpinner.setAdapter(MinutesRangeAdapter);	
		
					
	}
	

	public void setEventSpinnerValues(){
		
		eventTypeSp = (Spinner)findViewById(R.id.eventTopicSpinner);
		eventSubtypeSp = (Spinner)findViewById(R.id.eventSubtopicSpinner);
		eventInviteStatusSp = (Spinner)findViewById(R.id.eventInviteStatusSpinner);
		

		InviteStatusList.add("Invite");
		InviteStatusList.add("Open");
		InviteStatusList.add("LFP");
		ArrayAdapter<String> GenderRangeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, InviteStatusList);
		GenderRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eventInviteStatusSp.setAdapter(GenderRangeAdapter);	
		
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
		
	}
	

	public void setDayFromMonth(int position){

    	int monthNumSelected = position + 1;
    	int NumberOfDaysInMonthSelected = 30;
    	if(monthNumSelected == 1  
	    		||	monthNumSelected == 3 
	    		||	monthNumSelected == 5 
	    		||	monthNumSelected == 7 
	    		||	monthNumSelected == 8 
	    		||	monthNumSelected == 10 
	    		||	monthNumSelected == 12){
    		NumberOfDaysInMonthSelected = 31;
    	}
    	else if(monthNumSelected == 2){
    		NumberOfDaysInMonthSelected = 28;
    	}
    	else {
    		NumberOfDaysInMonthSelected = 30;		    		
    	}

		List<String> daylist = new ArrayList<String>();
		for (int i = 1; i <= NumberOfDaysInMonthSelected; i++){
			daylist.add(Integer.toString(i));			
		}
    	
		ArrayAdapter<String> dayrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, daylist);
		dayrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		event_Day.setAdapter(dayrangeAdapter );
		
		//IF THE MONTH SELECTED IS THIS MONTH THEN SET THE DAY SPINNER TO TODAY'S DATE
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdfMM = new SimpleDateFormat("MM",Locale.getDefault());

		int currmonth = Integer.parseInt(sdfMM.format(cal.getTime()));
		
		if(monthNumSelected == currmonth){

			int currday = cal.get(Calendar.DAY_OF_MONTH) - 1;
			event_Day.setSelection(currday);
		}
	}
	
	
	@Override
	public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
	    
		 switch(spinner.getId()) {
		 
	        case R.id.event_Month:
	        	if(countMts >= 1){
	        	    	setDayFromMonth(position);
	        	}
	        	countMts++;
				break;
					
	        case R.id.eventTopicSpinner:
	        	if(count >= 1){
	        		loadSubEventSpinner(position);
	        	}
	        	count++;
		        break;
		 }
    }
	

	public void loadSubEventSpinner(int position){

    	//LOAD THE APPROPRIATE SUBTYPES OF EVENTS INTO THE SUBTYPE SPINNER
        ArrayList<String> evtSubTypeIds = new ArrayList<String> ();
        evtSubTypeLabels.removeAll(evtSubTypeLabels);

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
		logoutButton.setOnClickListener(bsm.logoutClick);
		settingsButton.setOnClickListener(bsm.settingsClick);
		interestsButton.setOnClickListener(bsm.interestsClick);
		
	}
	

	public String formatMonth(int month) {
	    DateFormatSymbols symbols = new DateFormatSymbols();
	    String[] monthNames = symbols.getMonths();
	    return monthNames[month - 1];
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
	public void onNothingSelected(AdapterView<?> parent) {	}
	
}
