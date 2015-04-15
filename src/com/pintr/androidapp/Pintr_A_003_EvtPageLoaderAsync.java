package com.pintr.androidapp;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class Pintr_A_003_EvtPageLoaderAsync extends AsyncTask<String, Void, String> {
	
	public Pintr_22_EventsOverview p22 = null;
	final Pintr_G_002_DBQuery dbq;
	final Pintr_G_020_EventsTypeListReader etlr;
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_021_YourEventsReader yevr;
	Context context;
	
	public Pintr_A_003_EvtPageLoaderAsync(Context contextIn){
		context = contextIn;
		dbq = new Pintr_G_002_DBQuery(context);
		etlr = new Pintr_G_020_EventsTypeListReader(context);
		yevr = new Pintr_G_021_YourEventsReader(context);
	}
    
	@Override
	protected String doInBackground(String... params) {

		//**  DL THE LATEST EVT TYPE TBL
		String JSONdata = etlr.getEventList();
		Log.d("EVENTS LIST", "->" + JSONdata);
		if(!JSONdata.equals("CONNECTIONTIMEOUT"))
			etlr.storeJSONData(jsh.JSON_Parser(JSONdata , "event_types_list"));
		
		//GET YOUR USER DETAILS FROM THE ONBOARD DB
		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		
		yevr.downloadYourEvents(email, password);

		return null;
	}

	
    @Override
    protected void onPostExecute(String msg) {
    	p22.processFinish("Passed from listener!");
    }
    
}
