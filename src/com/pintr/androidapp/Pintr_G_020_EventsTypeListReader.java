package com.pintr.androidapp;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;


public class Pintr_G_020_EventsTypeListReader {
	
	
	// DATABASE CONFIG ELEMENTS:
    private static final String EVENTS_TYPES_TABLE = "P_EK_EVTP";  
    final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_001_DBMaker g01 ;
    Context context;
    

	//INITIALISE AND CREATE DATABASE
	public Pintr_G_020_EventsTypeListReader(Context contextIn) {
	    context = contextIn;
        g01 = new Pintr_G_001_DBMaker(context);
	}
	
	
	public void storeJSONData(JSONArray JSONArrayIn){
		
		int jsonlength = JSONArrayIn.length();		
		
		//PARSE THE JSON FILE
		g01.EmptyTable(EVENTS_TYPES_TABLE);
		for(int i = 0; i < jsonlength; i++){
	        try {
	        	String 	EventTypeId = JSONArrayIn.getJSONObject(i).getString("Event_type_id");
	        	String 	EventSubTypeId = JSONArrayIn.getJSONObject(i).getString("Event_subtype_id");
	        	String 	EventTypeName = JSONArrayIn.getJSONObject(i).getString("Event_type");
	        	String 	EventSubTypeName = JSONArrayIn.getJSONObject(i).getString("Event_subtype");
				evtListUpdate(EventTypeId, EventSubTypeId, EventTypeName, EventSubTypeName);
				} 
	        catch (JSONException e) {e.printStackTrace();}
		}
	}
	
	public String getEventList(){
		
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress ="http://www.pickmemycar.com/pintr/EADFD/EVFDTSTLST.php";

		inputVariables.add("");
		inputValues.add("");
		
		return rDB.getDBQuery(inputVariables
								,inputValues
								,queryAddress);
		
	}
	
	
	//UPDATE TABLE
    public void evtListUpdate(	String EventTypeId
					    		, String EventSubTypeId
					    		, String EventTypeName
					    		, String EventSubTypeName
								){
    	ArrayList<String> itemNames = new ArrayList<String>();
		ArrayList<String> itemValues = new ArrayList<String>();

		itemNames.add("Event_type_id");
		itemNames.add("Event_subtype_id");
		itemNames.add("Event_type");
		itemNames.add("Event_subtype");
		
		itemValues.add(EventTypeId);
		itemValues.add(EventSubTypeId);
		itemValues.add(EventTypeName);
		itemValues.add(EventSubTypeName);

		Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
		dbq.TableRowInserter(EVENTS_TYPES_TABLE
							,itemNames 
							,itemValues
							);
	}
}
