package com.pintr.androidapp;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class Pintr_A_005_LFPAsync extends AsyncTask<String, Void, String> {
	
	public Pintr_26_LFP p26 = null;
	final Pintr_G_002_DBQuery dbq;
	final Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	Context context;
	String function, evtLocStr, latitude, longitude, response, edittextDescription;
	int eventTopicPosn, timerangeHours, distanceInMiles;
	
	public Pintr_A_005_LFPAsync(Context contextIn
								, String functionIn
								, String evtLocStrIn
								, int eventTopicPosnIn
								, int timerangeHoursIn
								, int distanceInMilesIn
								, String latitudeIn
								, String longitudeIn
								, String edittextDescriptionIn
								){
		context = contextIn;
		dbq = new Pintr_G_002_DBQuery(context);
		function = functionIn;
		evtLocStr = evtLocStrIn;
		eventTopicPosn = eventTopicPosnIn;
		timerangeHours = timerangeHoursIn;
		distanceInMiles = distanceInMilesIn;
		latitude = latitudeIn;
		longitude = longitudeIn;
		edittextDescription = edittextDescriptionIn;
	}
    
	@Override
	protected String doInBackground(String... params) {

		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		
		Log.d("SUBTD LFP VALS:", " - > " + eventTopicPosn
								 +  "/"  + latitude
								 +  "/"  + longitude
								 +  "/"  + timerangeHours
								 +  "/"  + ""
							 	 +  "/"  + distanceInMiles
								 );
		if (function.equals("LFI_POST"))
			response = postLookingForInvite(email
											, password
											, eventTopicPosn
											, latitude
											, longitude
											, timerangeHours
											, ""
											, distanceInMiles
											, edittextDescription
											);

		else if (function.equals("PEOPLE_SEARCH"))
			response = searchForPeople(email
										, password
										,  eventTopicPosn
										,  latitude
										,  longitude
										,  timerangeHours
										,  ""
										);
			
		
		else if (function.equals("EVENT_SEARCH"))
			response = 
				searchForEvent(email
								, password
								,  eventTopicPosn
								,  latitude
								,  longitude
								,  timerangeHours
								,  ""
								, distanceInMiles
								);
		return response ;
	}

	
    @Override
    protected void onPostExecute(String msg) {
    	Log.d("ASYNCTASK","FINISHED PROCESSING");
    	p26.processFinish(response, function);
    }
    
    

	public String searchForEvent(String email
								, String password
								, int event_type
								, String latitude
								, String longitude
								, int timerange_hours
								, String search_terms
								, int search_radius
								){
		String queryAddress ="http://www.pickmemycar.com/pintr/LFP/LFP_03_LFP_evtSch.php";

			
		List<String> extraInputVariables = new ArrayList<String> ();
		List<String> extraInputValues = new ArrayList<String> () ;

		extraInputVariables.add("event_type");
		extraInputVariables.add("latitude");
		extraInputVariables.add("longitude");
		extraInputVariables.add("timerange_hours");
		extraInputVariables.add("search_terms");
		extraInputVariables.add("search_radius");
		extraInputVariables.add("now");
			
		final Date nowDt = Calendar.getInstance().getTime();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		String now = simpleDateFormat.format(nowDt);
	

		extraInputValues.add(Integer.toString(event_type));
		extraInputValues.add(latitude);
		extraInputValues.add(longitude);
		extraInputValues.add(Integer.toString(timerange_hours));
		extraInputValues.add(search_terms);
		extraInputValues.add(Integer.toString(search_radius));
		extraInputValues.add(now);

		String JSONresult = generalRemoteQuery(	email
												, password
												, queryAddress
												, extraInputVariables
												, extraInputValues
												);
		Log.d("LFP QUERY RESULT", "-> " + JSONresult);
		return JSONresult;
		
	}
    

	public String postLookingForInvite(	String email
										, String password
										, int event_type
										, String latitude
										, String longitude
										, int timerange_hours
										, String search_terms
										, int search_radius
										, String description_posted
										){
		String queryAddress ="http://www.pickmemycar.com/pintr/LFP/LFP_01_LFI_post.php";
		
		final Date nowDt = Calendar.getInstance().getTime();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		String now = simpleDateFormat.format(nowDt);
	
		List<String> extraInputVariables = new ArrayList<String> ();
		List<String> extraInputValues = new ArrayList<String> () ;
		
		extraInputVariables.add("event_type");
		extraInputVariables.add("latitude");
		extraInputVariables.add("longitude");
		extraInputVariables.add("post_DtTm");
		extraInputVariables.add("description_posted");
		extraInputVariables.add("radius_posted");
		
		extraInputValues.add(Integer.toString(event_type));
		extraInputValues.add(latitude);
		extraInputValues.add(longitude);
		extraInputValues.add(now);
		extraInputValues.add(description_posted);
		extraInputValues.add(Integer.toString(search_radius));
		
		String JSONresult = generalRemoteQuery(	email
												, password
												, queryAddress
												, extraInputVariables
												, extraInputValues
												);
		Log.d("LFP QUERY RESULT", "-> " + JSONresult);
		return JSONresult;
	}
	
	public String searchForPeople(	String email
									, String password
									, int event_type
									, String latitude
									, String longitude
									, int timerange_hours
									, String search_terms
									){
		String queryAddress ="http://www.pickmemycar.com/pintr/LFP/LFP_02_LFI_find.php";
		
		List<String> extraInputVariables = new ArrayList<String> ();
		List<String> extraInputValues = new ArrayList<String> () ;
		
		extraInputVariables.add("event_type");
		extraInputVariables.add("latitude");
		extraInputVariables.add("longitude");
		extraInputVariables.add("timerange_hours");
		extraInputVariables.add("search_terms");
		extraInputVariables.add("now");
			
		final Date nowDt = Calendar.getInstance().getTime();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		String now = simpleDateFormat.format(nowDt);
		
		extraInputValues.add(Integer.toString(event_type));
		extraInputValues.add(latitude);
		extraInputValues.add(longitude);
		extraInputValues.add(Integer.toString(timerange_hours));
		extraInputValues.add(search_terms);
		extraInputValues.add(now);

		
		String JSONresult = generalRemoteQuery(	email
												, password
												, queryAddress
												, extraInputVariables
												, extraInputValues
												);
		Log.d("LFP QUERY RESULT", "-> " + JSONresult);
		return JSONresult;
	}
	
	
	public String generalRemoteQuery(String email
									, String password
									, String queryAddress
									, List<String> extraInputVariables
									, List<String> extraInputValues
									){

		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		
		inputVariables.add("email");
		inputVariables.add("password");
		
		inputValues.add(email);
		inputValues.add(password);

		inputVariables.addAll(extraInputVariables);
		inputValues.addAll(extraInputValues);
		
		return rDB.getDBQuery(	inputVariables
								,inputValues
								,queryAddress
								);
	}

    
}
