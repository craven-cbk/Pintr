package com.pintr.androidapp;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;

public class Pintr_G_018_PeopleHandler {
	
	Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	
	public Pintr_G_018_PeopleHandler(){
		
	}

	public void downloadPeopleData(	String statusString
									, String email
									, String password
									, Context context
									){

		String JSONstringIn = "";
		JSONArray JSONArrayIn = new JSONArray();
		
		JSONstringIn  = getPeopleList(context, statusString,email,password);
		if(!JSONstringIn.equals("CONNECTIONTIMEOUT")){
			JSONArrayIn = jsh.JSON_Parser( JSONstringIn
										, "fr_like_stat"
										);
			storeJSONData(JSONArrayIn, statusString, context);
		}
	}
	

	public void storeJSONData(JSONArray JSONArrayIn, String status, Context context){
		
		int jsonlength = JSONArrayIn.length();		
		int pintr_user_id = 0;
		String handler = "";
		
		//PARSE THE JSON FILE
		Pintr_G_017_ProfilesDBMaker dbm = new Pintr_G_017_ProfilesDBMaker(context);
		dbm.EmptyTable(status);
		for(int i = 0; i < jsonlength; i++){
	        try {
	        	pintr_user_id = JSONArrayIn.getJSONObject(i).getInt("pintr_user_id");
				handler = JSONArrayIn.getJSONObject(i).getString("handler");
				dbm.peopleUpdate(pintr_user_id, handler, status);
//				Log.d("Results to SQLITE:",pintr_user_id + " - " + handler + " - " + status);
				} 
	        catch (JSONException e) {e.printStackTrace();}
		}
	}
	
	public String getPeopleList(Context context, String status_requested, String email, String password){
		
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress ="http://www.pickmemycar.com/pintr/AVICI/AVICI_FR_DN.php";

		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("status_requested");

		inputValues.add(email);
		inputValues.add(password);
		inputValues.add(status_requested);

		return rDB.getDBQuery(inputVariables
								,inputValues
								,queryAddress);
	}
}