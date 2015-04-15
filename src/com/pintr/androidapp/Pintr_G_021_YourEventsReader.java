package com.pintr.androidapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Pintr_G_021_YourEventsReader   {
	
	Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	
	// DATABASE CONFIG ELEMENTS:
    private static final String YOUR_EVENTS_TABLE = "P_EK_EVTSUM";
    private static final String YOUR_EVENTS_PARTICIPANT_TABLE = "P_EK_EVTPAR"; 
    private static final String GLOBAL_VAR_NAME = "P_EK_GVAR";  
    
    Context context;

	//INITIALISE AND CREATE DATABASE
	public Pintr_G_021_YourEventsReader(Context contextIn) {
	    context = contextIn;
	}

	public String setAttendance(String userEmail
								, String userPassword
								, String Event_id
								, String participant_attending
								, String origin
								, String delfn
								, String uid
								){
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress ="http://www.pickmemycar.com/pintr/EADFD/EVFDUDPAT.php";
		String participant_event_att_status = "";
		
		if(participant_attending.equals("1"))
			participant_event_att_status = "TRUE";
		else 
			if(participant_attending.equals("0"))
				participant_event_att_status = "FALSE";
		
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("Event_id");
		inputVariables.add("participant_event_att_status");
		inputVariables.add("origin");
		inputVariables.add("delfn");
		inputVariables.add("uid");
		
		inputValues.add(userEmail);
		inputValues.add(userPassword);
		inputValues.add(Event_id);
		inputValues.add(participant_event_att_status);
		inputValues.add(origin);
		inputValues.add(delfn);
		inputValues.add(uid);
		
		return rDB.getDBQuery(	inputVariables
								,inputValues
								,queryAddress
								);
	}
	

	public void downloadYourEvents(String email , String password){

		String JSONstringIn = "";
		JSONArray JSONArrayIn = new JSONArray();
		
		JSONstringIn  = getEventList(email ,password);
		
		if(!JSONstringIn.equals("EMPTYQUERY")
				&& !JSONstringIn.equals("CONNECTIONTIMEOUT")){
			JSONArrayIn = jsh.JSON_Parser( JSONstringIn
										, "events_list_returned"
										);
			Log.d("G_21 EVT JSON", JSONstringIn);
			storeJSONData(JSONArrayIn);	
		}
		
		//SET DATE LAST DOWNLOADED
		TimeZone tz = TimeZone.getTimeZone("GMT");
		Calendar now = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		dateFormat.setTimeZone(tz);
		String nowDtTm = dateFormat.format(now.getTime());
		Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
    	dbq.globalValueUpdater(GLOBAL_VAR_NAME, "date_last_dl_events_data", nowDtTm);
	}
	
	
	public ArrayList<String> readEventsList(){
		
		return new ArrayList<String>();
	}

	
	public String getEventList(String userEmail, String userPassword){
		
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
    	String [] gvals = dbq.wholeTableReader(GLOBAL_VAR_NAME, ",").get(0).split(",");
    	String lastUpdateDate = "";
    	
    	if(gvals[1].equals(null) || gvals[1].equals("null")) 
    		lastUpdateDate = "2000-01-01 00:00:00";
    	else
       		lastUpdateDate = gvals[1];

    	Log.d("LAST EVENT READ DTTM:", lastUpdateDate);
    	
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress ="http://www.pickmemycar.com/pintr/EADFD/EVFDLIEV.php";

		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("search_type");
		inputVariables.add("last_dl_dttm");
		
		inputValues.add(userEmail);
		inputValues.add(userPassword);
		inputValues.add("ALLMYEVENTS");
		inputValues.add(lastUpdateDate);
		
		return rDB.getDBQuery(	inputVariables
								,inputValues
								,queryAddress
								);
	}
	

	public void storeJSONData(JSONArray JSONArrayIn){
		
		int jsonlength = JSONArrayIn.length();		
		
		//PARSE THE JSON FILE
		for(int i = 0; i < jsonlength; i++){
	        try {
	    		ArrayList<String> itemNames = new ArrayList<String> ();
	    		ArrayList<String> itemValues = new ArrayList<String> ();
	    		ArrayList<String> keyNames = new ArrayList<String> ();
	    		ArrayList<String> keyValues = new ArrayList<String> ();
	    		
	        	String 	Event_id = JSONArrayIn.getJSONObject(i).getString("Event_id");
	        	String 	Event_creator_uid = JSONArrayIn.getJSONObject(i).getString("Event_creator_uid");
	        	String 	handler = JSONArrayIn.getJSONObject(i).getString("handler");
	        	//*******************************
	        	String 	Event_DtTm = JSONArrayIn.getJSONObject(i).getString("Event_DtTm");
	        	//*******************************
	        	String 	event_type = JSONArrayIn.getJSONObject(i).getString("event_type");
	        	String 	type_variant = JSONArrayIn.getJSONObject(i).getString("type_variant");
	        	String 	event_title = JSONArrayIn.getJSONObject(i).getString("event_title");
	        	String 	venue_name = JSONArrayIn.getJSONObject(i).getString("venue_name");
	        	String 	venue_address_text = JSONArrayIn.getJSONObject(i).getString("venue_address_text");
	        	String 	chg_evt_dttm = JSONArrayIn.getJSONObject(i).getString("chg_evt_dttm");
	        	String 	hashtags = JSONArrayIn.getJSONObject(i).getString("hashtags");
	        	String 	participants = JSONArrayIn.getJSONObject(i).getString("participants");
	        	String 	venue_yelp_id = JSONArrayIn.getJSONObject(i).getString("venue_yelp_id");
	        	String 	venue_yelp_url = JSONArrayIn.getJSONObject(i).getString("venue_yelp_url");
	        	String 	invite_status = JSONArrayIn.getJSONObject(i).getString("invite_status");
	        	String 	event_description = JSONArrayIn.getJSONObject(i).getString("event_description");
	        		

	    	    Date Event_DtTmDate = null;
	    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
	    		final Calendar cal = Calendar.getInstance();
			    Calendar calAlarmTime  = Calendar.getInstance();
	    	    try {
	    			Event_DtTmDate = simpleDateFormat.parse(Event_DtTm);
	    		    cal.setTime(Event_DtTmDate);
	    		    calAlarmTime.setTime(Event_DtTmDate);
	    		    calAlarmTime.add(Calendar.HOUR, -1);
	    		    String event =  handler + " - " + event_title;
		        	if(calAlarmTime.getTimeInMillis() > Calendar.getInstance().getTimeInMillis())
		        		setEventAlarm(cal, event);
	    		} catch (ParseException e) {e.printStackTrace();}
	        	
	    	    
	        	itemNames.add("Event_id");
	        	itemNames.add("Event_creator_uid");
	        	itemNames.add("handler");
	        	itemNames.add("Event_DtTm");
	        	itemNames.add("event_type");
	        	itemNames.add("type_variant");
	        	itemNames.add("event_title");
	        	itemNames.add("venue_name");
	        	itemNames.add("venue_address_text");
	        	itemNames.add("chg_evt_dttm");
	        	itemNames.add("hashtags");
	        	itemNames.add("venue_yelp_id");
	        	itemNames.add("venue_yelp_url");
	        	itemNames.add("invite_status");
	        	itemNames.add("event_description");
	        	
	        	itemValues.add(Event_id);
	        	itemValues.add(Event_creator_uid);
	        	itemValues.add(handler);
	        	itemValues.add(Event_DtTm);
	        	itemValues.add(event_type);
	        	itemValues.add(type_variant);
	        	itemValues.add(event_title);
	        	itemValues.add(venue_name);
	        	itemValues.add(venue_address_text);
	        	itemValues.add(chg_evt_dttm);
	        	itemValues.add(hashtags);
	        	itemValues.add(venue_yelp_id);
	        	itemValues.add(venue_yelp_url);
	        	itemValues.add(invite_status);
	        	itemValues.add(event_description);
	        	

	        	keyNames.add("Event_id");
	        	keyValues.add(Event_id);
	        	
	        	Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
	        	dbq.TableRowDeleteInsert(YOUR_EVENTS_TABLE
							    		, itemNames
							    		, itemValues
							    		, keyNames
							    		, keyValues
							    		);
	        	
	        	JSONArray partJSArray = jsh.JSON_Parser( "{\"participants\":" + participants + "}"
														, "participants"
														);

	    		for(int j = 0; j < partJSArray.length(); j++){
	    			ArrayList<String> partNameItemNames = new ArrayList<String> ();
		    		ArrayList<String> partNameItemValues = new ArrayList<String> ();
		    		ArrayList<String> subKeyNames = new ArrayList<String> ();
		    		ArrayList<String> subKeyValues = new ArrayList<String> ();
		    		
		    		String 	participant_user_id = partJSArray.getJSONObject(j).getString("user_id");
		        	String 	participant_event_status = partJSArray.getJSONObject(j).getString("participant_event_status");
		        	String 	participant_attending = partJSArray.getJSONObject(j).getString("participant_attending");
		        	String 	handlerPart = partJSArray.getJSONObject(j).getString("handler");
		        	
		        	partNameItemNames.add("Event_id");
		        	partNameItemNames.add("participant_user_id");
		        	partNameItemNames.add("participant_event_status");
		        	partNameItemNames.add("participant_attending");
		        	partNameItemNames.add("handler");
		        
		        	partNameItemValues.add(Event_id);
		        	partNameItemValues.add(participant_user_id);
		        	partNameItemValues.add(participant_event_status);
		        	partNameItemValues.add(participant_attending);
		        	partNameItemValues.add(handlerPart);
		        	
		        	Log.d("EVT PART DATA TO DELSTOR",Event_id + "->" 
				        	+ participant_user_id + "/" 
		        			+ participant_event_status + "/" 
		        			+ participant_attending + "/" 
		        			+ handlerPart + "."
		        			);
		        	
		        	subKeyNames.add("Event_id");
		        	subKeyNames.add("participant_user_id");

		        	subKeyValues.add(Event_id);
		        	subKeyValues.add(participant_user_id);
		        	
		        	dbq.TableRowDeleteInsert(YOUR_EVENTS_PARTICIPANT_TABLE
						        			, partNameItemNames
								    		, partNameItemValues
								    		, subKeyNames
								    		, subKeyValues
								    		);
	    		}
			} 
	        
	        
	        catch (JSONException e) {e.printStackTrace();}
		}
	}
	
	public void setEventAlarm(Calendar cal, String event){
		
		//RING THE ALARM 1 HOUR BEFORE THE EVENT
		cal.add(Calendar.HOUR, -1);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
		Log.d("EVT ALARM", "SET FOR:   " +  simpleDateFormat.format(cal.getTime()));
		    
		Intent intent = new Intent(context, Pintr_AM_01_EventNotifier.class);
		intent.putExtra("event", event);


        PendingIntent sender 
        	= PendingIntent.getBroadcast(context
					        			, 0
					        			, intent
					        			,PendingIntent.FLAG_UPDATE_CURRENT
					        			);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);
	}
	
}
