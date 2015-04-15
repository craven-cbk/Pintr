package com.pintr.androidapp;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;


@SuppressLint("SetJavaScriptEnabled")
public class Pintr_24_YelpVenueFinder extends Activity implements OnItemSelectedListener {
	
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_022_VenueSearchFunctions vsf = new Pintr_G_022_VenueSearchFunctions();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();

    private static final String EVENTS_TYPES_TABLE = "P_EK_EVTP";  
	HashMap<String,String> EventSubTypesMap = new HashMap<String,String>();
	HashMap<String, String> EventDetailsMapF ;
	String[][] EventToSubEventKeysMap;
	ArrayList<String> EventTypesArray = new ArrayList<String>();
    ArrayList<String> evtSubTypeLabels = new ArrayList<String> ();
    HashMap<Integer, String> participants ;
    LinearLayout webviewLayout, DisplaySelection , displayCurrentVenue;
	EditText LocationTextEntry ;
	EditText TypeTextEntry ;
	LinearLayout DisplayResults ;
	int evtItems;
	Spinner eventTypeSp , eventSubtypeSp;
	String venue_yelp_id , venue_yelp_url, venue_name, venue_address_text;
	String curr_venue_yelp_id , curr_venue_yelp_url, curr_venue_name
			, curr_venue_address_text, event_type, type_variant, location
			, curr_venue_latitude, curr_venue_longitude;
	Context context = this;
	String OrganiserData, Origin;
	int count=0;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_24_yelp_venue_finder);
		getWindow().setSoftInputMode(
			    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
			);

		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		Button subBtn = (Button )findViewById(R.id.findLocnBtn);
		Button BackBtnWebView = (Button )findViewById(R.id.BackBtnWebView);
		Button AddBtnWebView = (Button )findViewById(R.id.AddBtnWebView);
		Button BackBtn = (Button )findViewById(R.id.BackBtn);
		Button AddBtn = (Button )findViewById(R.id.AddBtn);
		Button removeLocnBtn = (Button )findViewById(R.id.removeLocnBtn);
		Button useThisLocnBtn = (Button )findViewById(R.id.useThisLocnBtn);
		Button hereBtn = (Button )findViewById(R.id.hereBtn );
		Button nearBtn = (Button )findViewById(R.id.nearBtn);
		
		LocationTextEntry = (EditText)findViewById(R.id.LocationTextEntry);
		DisplayResults = (LinearLayout )findViewById(R.id.DisplayResults);
		webviewLayout = (LinearLayout)findViewById(R.id.webviewLayout);
		
		setEventSpinnerValues();
		
		
		final Bundle extras = getIntent().getExtras();
		participants  = (HashMap<Integer, String>) getIntent().getSerializableExtra("participants");
		EventDetailsMapF = (HashMap<String, String>) getIntent().getSerializableExtra("EventDetailsMapF");
		Origin = extras.getString ("Origin");
		OrganiserData = extras.getString ("OrganiserData");
		Log.d("CALL ORIGIN", Origin);
		

		Iterator<Integer> myVeryOwnIterator = participants.keySet().iterator();
		while(myVeryOwnIterator.hasNext()) {

			Integer key=myVeryOwnIterator.next();
			Log.d(Integer.toString(key), participants.get(key));
		}

		venue_yelp_id = EventDetailsMapF.get("venue_yelp_id");
		venue_yelp_url = EventDetailsMapF.get("venue_yelp_url");
		venue_name = EventDetailsMapF.get("venue_name");
		venue_address_text = EventDetailsMapF.get("venue_address_text");
		curr_venue_latitude = EventDetailsMapF.get("curr_venue_latitude");
		curr_venue_longitude = EventDetailsMapF.get("curr_venue_longitude");
		event_type = EventDetailsMapF.get("event_type");
		type_variant = EventDetailsMapF.get("type_variant");
		if(!event_type.equals("1") 
				|| !event_type.equals("2")
				|| !event_type.equals("4")  
				){
			event_type = "1";
			type_variant = "1";
		} else if(type_variant.equals("0"))
			type_variant = "1";
		
		int type_variantNum =  Integer.parseInt(type_variant);
		
	
		//SET EVENT TYPE SPINNERS WITH EXISTING DATA
		Log.d("EVENT TYPE/SUBTP", event_type + "/" + type_variant);
		eventTypeSp.setSelection(Integer.parseInt(event_type));
		loadSubEventSpinner(Integer.parseInt(event_type));
		eventSubtypeSp.setSelection(type_variantNum);
//		eventSubtypeSp.setSelection(Integer.parseInt(type_variant));
		
		Log.d("VENUE DETAILS PASSED", 	venue_yelp_id + " - " + 
										venue_yelp_url + " - " +
										venue_name + " - " +
										venue_address_text + " - " 
					);
		DisplaySelection = (LinearLayout)findViewById(R.id.DisplaySelection);
		displayCurrentVenue = (LinearLayout)findViewById(R.id.DisplayVenueSelectionLL);
		
		if(venue_yelp_id != null
			&& venue_yelp_id.equals("")==false){
			
				displaySearchResultBox(	venue_name
										, venue_yelp_url
										, venue_yelp_id
										, venue_address_text
										, ""
										, displayCurrentVenue
										, curr_venue_latitude
										, curr_venue_longitude
										);
				DisplaySelection.setVisibility(View.VISIBLE);
		} 
		else if(venue_address_text != null 
				&& venue_address_text.equals("")==false){
			
			String mapsURL = "https://www.google.ro/maps/place/" + venue_address_text;
			displayCurrentVenue.removeAllViews();
			displaySearchResultBox(	"Custom location"
									, mapsURL
									, ""
									, venue_address_text
									, ""
									, displayCurrentVenue
									, curr_venue_latitude
									, curr_venue_longitude
									);
			DisplaySelection.setVisibility(View.VISIBLE);
		}
		
		//FOR BACK TO EVENTS:
		BackBtnWebView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				webviewLayout.setVisibility(View.GONE);
					
			}
		});
		
		AddBtnWebView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				addVenue(displayCurrentVenue, DisplaySelection);
			}
		});

		removeLocnBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				removeVenue(displayCurrentVenue, DisplaySelection);
			}
		});
		
		BackBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				returnToEvtMkr();
			}
		});
		
		AddBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				returnToEvtMkr();
			}
		});
		
		
		subBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				getDataForSearch();
			}
		});

		
		useThisLocnBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				useThisLocation();
			}
		});
		
		hereBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				getCurrLocn("HERE");
			}
		});
		
		nearBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				getCurrLocn("NEAR");
			}
		});
	}
	
	public void getDataForSearch(){

		DisplayResults.removeAllViews();

		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        
		String Location = LocationTextEntry.getText().toString();
		String eventSubtypeSpStr = eventSubtypeSp.getSelectedItem().toString();
		String lat="", lon="";
		
		if(eventSubtypeSp.getSelectedItemPosition() == 0)
			Toast.makeText(getApplicationContext()
					, "Please pick a venue type"
					, Toast.LENGTH_LONG).show();
		else{
			String YelpSearchResults = vsf.subSearch(Location , eventSubtypeSpStr , "10");
			Log.d("YelpSearchResults", YelpSearchResults);

			JSONObject object;
			JSONObject errorExists = null;
			try {
				object = new JSONObject(YelpSearchResults);
				errorExists =object.getJSONObject("error");
			} catch (JSONException e1) {e1.printStackTrace();}

			if(errorExists != null)
				Toast.makeText(getApplicationContext()
						, "Location not recognised"
						, Toast.LENGTH_LONG).show();
			
			else{
				JSONArray jsa = jsh.JSON_Parser(YelpSearchResults, "businesses");
				int jsonLength = jsa.length();
				String name=""
						, url="" 
						, display_address=""
						, categories ="" 
						, yelp_id=""
						;
				
				for (int i = 0; i < jsonLength; i++){
					try {
						name = jsa.getJSONObject(i).getString("name");
						yelp_id = jsa.getJSONObject(i).getString("id");
						url = jsa.getJSONObject(i).getString("url");
						categories = jsa.getJSONObject(i).getString("categories");
						String loc = jsa.getJSONObject(i).getString("location");
						
						Log.d("LOCATION STRING", " -> "  + loc );
						JSONArray locArr = new JSONArray("[" + loc + "]");
						JSONArray x = jsh.JSON_Parser(loc, "display_address");
						display_address = x.toString();
						String coords = locArr.getJSONObject(0).getString("coordinate");
						
						lat = new JSONArray("[" + coords + "]").getJSONObject(0).getString("latitude");
						lon = new JSONArray("[" + coords + "]").getJSONObject(0).getString("longitude"); 
						Log.d("URL", url);
						Log.d("LATTITUDE/LONGITUDE", " - " + lat + "/" + lon);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					displaySearchResultBox(name
											, url
											, yelp_id
											, display_address
											, categories
											, DisplayResults
											, lat
											, lon
											);
				}
			}	
		}
	}


	public void displaySearchResultBox(final String name
										, final String url
										, final String yelp_id
										, final String display_address
										, final String categories
										, LinearLayout placeToPut
										, final String lat
										, final String lon
										){
		//ADD HORIZONTAL LAYOUT
	    LinearLayout LL= new LinearLayout (this);
	    //ADD TITLE
	    TextView placeNameTV = new TextView(this);
	    TextView placeAddressTV = new TextView(this);
	    placeNameTV.setTextAppearance(getApplicationContext(), R.style.EvtSumTextBig);
	    placeAddressTV.setTextAppearance(getApplicationContext(), R.style.EvtSumTextSmall);
	    //TextView placeCategoriesTV = new TextView(this);

	    placeNameTV.setText(name);
		if(!name.equals(""))
			placeNameTV.setText(name);
		else
			placeNameTV.setVisibility(View.GONE);
	    placeAddressTV.setText(display_address.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\"", ""));
	   
	    LL.setOrientation(LinearLayout.VERTICAL);
	    LL.setPadding (10,10,10,10);
	    LL.setBackgroundResource(R.drawable.eventvenueview_background);
	    
	    LinearLayout.LayoutParams lp1;
        lp1 = new LinearLayout.LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT);
	    lp1.setMargins(0, 0, 0, 10);
	    LL.setLayoutParams(lp1);

	    placeToPut.addView(LL, lp1);
	    
	    LL.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				webviewLayout.setVisibility(View.VISIBLE);
				WebView webView = (WebView) findViewById(R.id.webView1);
				webView.setWebViewClient(new WebViewClient());
//		        webView.setWebChromeClient(new WebChromeClient());
				webView.getSettings().setDomStorageEnabled(true);
				webView.getSettings().setJavaScriptEnabled(true);
				Log.d("URL CALLED", url);
				Log.d("ADDRESS TO DISPLAY", display_address);
				webView.loadUrl(url);
				
				curr_venue_yelp_id = yelp_id;
				curr_venue_yelp_url = url;
				curr_venue_name = name;
				curr_venue_address_text = display_address;
				curr_venue_latitude = lat;
				curr_venue_longitude = lon;
				Log.d("curr_venue_address_text", curr_venue_address_text);
			}
	    });

	    RelativeLayout RL= new RelativeLayout (this);
	    RelativeLayout.LayoutParams lp;  
	    lp = new RelativeLayout.LayoutParams(
		    	LayoutParams.MATCH_PARENT,
		    	LayoutParams.MATCH_PARENT);
	    

	    placeNameTV.setTypeface(null, Typeface.BOLD);
	    placeNameTV.setPadding (0,0,0,0);
        LL.addView(placeNameTV);
	    LL.addView(RL, lp);

	    RelativeLayout.LayoutParams lay2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    lay2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
	    RL.addView(placeAddressTV, lay2);	
	}
	
	public void useThisLocation(){
		String LocationString = LocationTextEntry.getText().toString();

		String latStr = "";
		String lonStr = "";
		curr_venue_yelp_id = "";
		curr_venue_yelp_url = "https://www.google.ro/maps/search/" + LocationString;
		curr_venue_name = "";
		curr_venue_address_text = LocationString;
		DisplaySelection.setVisibility(View.VISIBLE);
		displayCurrentVenue.removeAllViews();
		
		Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
		Log.d("curr_venue_address_text", "-> " + curr_venue_address_text);
		List<Address> fromLocationName = null;
		try {
			fromLocationName = geocoder.getFromLocationName(curr_venue_address_text,   1);
		} catch (IOException e) {e.printStackTrace();}
		if (fromLocationName != null && fromLocationName.size() > 0) {
			 if(fromLocationName.size() > 0) {
				 latStr = String.valueOf(fromLocationName.get(0).getLatitude());
				 lonStr = String.valueOf(fromLocationName.get(0).getLongitude());
				 Log.d("curr_venue LAT/LON", "-> " + lonStr + "/" + latStr);
				 curr_venue_latitude = latStr;
				 curr_venue_longitude = lonStr ;
				}
		}
		 
		displaySearchResultBox(	curr_venue_name
								, curr_venue_yelp_url
								, ""
								, curr_venue_address_text
								, ""
								, displayCurrentVenue
								, latStr
								, lonStr
								);
		updateVenueHashMap(curr_venue_name
							, curr_venue_yelp_url
							, ""
							, curr_venue_address_text
							, curr_venue_latitude
							, curr_venue_longitude
							);
	}

	
	public void addVenue(LinearLayout displayCurrentVenue, LinearLayout DisplaySelection){

		updateVenueHashMap(curr_venue_name
							, curr_venue_yelp_url
							, curr_venue_yelp_id
							, curr_venue_address_text
							, curr_venue_latitude
							, curr_venue_longitude
							);

		Log.d("ADDED VENUE", curr_venue_name + " -> " + curr_venue_address_text);
		displayCurrentVenue.removeAllViews();
		displaySearchResultBox(	curr_venue_name
								, curr_venue_yelp_url
								, ""
								, curr_venue_address_text
								, ""
								, displayCurrentVenue
								, curr_venue_latitude
								, curr_venue_longitude
								);
		DisplaySelection.setVisibility(View.VISIBLE);
		webviewLayout.setVisibility(View.GONE);
		
	}
	

	public void removeVenue(LinearLayout displayCurrentVenue, LinearLayout DisplaySelection){
		updateVenueHashMap("", "", "", "", "", "");
		Log.d("REMOVED VENUE", curr_venue_name + " -> `" + curr_venue_address_text);
		displayCurrentVenue.removeAllViews();
		DisplaySelection.setVisibility(View.GONE);
	}
	
	public void updateVenueHashMap(String curr_venue_name
									, String curr_venue_yelp_url
									, String curr_venue_yelp_id
									, String venue_address_text
									, String curr_venue_latitude
									, String curr_venue_longitude
									){

		EventDetailsMapF.put("venue_yelp_id",curr_venue_yelp_id);
		EventDetailsMapF.put("venue_yelp_url", curr_venue_yelp_url);
		EventDetailsMapF.put("venue_name", curr_venue_name);
		EventDetailsMapF.put("venue_address_text", venue_address_text);
		EventDetailsMapF.put("curr_venue_latitude", curr_venue_latitude);
		EventDetailsMapF.put("curr_venue_longitude", curr_venue_longitude);
		
	}

//*****SPINNERS 


	public void setEventSpinnerValues(){
		eventTypeSp = (Spinner)findViewById(R.id.eventTopicSpinner);
		eventSubtypeSp = (Spinner)findViewById(R.id.eventSubtopicSpinner);
		
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

	@Override
	public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {

    	if(count >= 1)
    		 loadSubEventSpinner(position);
        count++;
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
	
	
	
	public void returnToEvtMkr(){

		final Class<?> eventMaker = Pintr_23_EventMaker.class;
		Intent AutoRegOnBoard = new Intent(context, eventMaker);	
		AutoRegOnBoard = new Intent(context, eventMaker);	
		if (Origin.equals("EXISTINGEVENT")|| Origin.equals("VENUEFINDER_EX") )
			AutoRegOnBoard.putExtra("Origin", "VENUEFINDER_EX");	
		else if (Origin.equals("NEWEVENT")
				|| Origin.equals("NEWLFPEVENT")
				|| Origin.equals("ADDPPL")
				|| Origin.equals("VENUEFINDER_NU")
				)
			AutoRegOnBoard.putExtra("Origin", "VENUEFINDER_NU");
	    AutoRegOnBoard.putExtra("OrganiserData", OrganiserData);
		AutoRegOnBoard.putExtra("EventDetailsMapF", EventDetailsMapF);
	    AutoRegOnBoard.putExtra("participants", participants);
	    startActivity(AutoRegOnBoard);	
	}
	
	public void getCurrLocn(String type){

		//GET LOCATION
		Log.d("LOCATION","FIRING CALLER...");
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		networkAvailable = nw.haveNetworkConnection(this);
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new Pintr_G_097_24_NetworkLocation (this, this, type);
		if(networkAvailable){
			mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
			Log.d("LOCATION","FIRING LISTENER...");
		}
	}

	public void listLocat(Location loc, Context context, String type){
		loc.getLatitude();
		loc.getLongitude();

	    Geocoder geocoder = new Geocoder(context, Locale.getDefault());   
	    String result = null;
	    try {
	        List<Address> list = geocoder.getFromLocation(
	        		loc.getLatitude(), loc.getLongitude(), 1);
	        if (list != null && list.size() > 0) {
	            Address address = list.get(0);
	            if(type.equals("NEAR"))
	            	result = address.getAddressLine(1) + ", " + address.getAddressLine(2);
	            else if(type.equals("HERE"))
	            	result = 	address.getAddressLine(0) 
	            				+ ", " + address.getAddressLine(1) 
	            				+ ", " + address.getAddressLine(2)
	            				;
	        }
	        else{
	        }
	    } catch (IOException e) {    } 

	    if(result.equals(null)){
	    	result = "Location unobtainable";
	    }
	    LocationTextEntry.setText(result);
	    if(type.equals("HERE")){
        	useThisLocation();
			returnToEvtMkr();
		}else if(type.equals("NEAR")){
			getDataForSearch();
		}
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
	public void onNothingSelected(AdapterView<?> parent) {}



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbq != null) {
        	dbq.close();
        }
    }  
}