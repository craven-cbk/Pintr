package com.pintr.androidapp;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;


public class Pintr_15_PeoplePage 	extends Activity 
									implements 	Pintr_PL_03_LoginLoadUserListener{

	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_014_ProfilerDBMSManager rDB = new Pintr_G_014_ProfilerDBMSManager();
	final Pintr_G_017_ProfilesDBMaker dbm = new Pintr_G_017_ProfilesDBMaker(this);
	
	String email, password;
	
	Boolean networkAvailable ;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_15_people_page);

		//ADD SPINNER WHILST LOADING DATA
		ProgressBar pbspnner  = new ProgressBar(this);;
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		LinearLayout.LayoutParams iRL 
			= new LinearLayout.LayoutParams(50, 50);
		spinnerRL.addView(pbspnner,iRL);

		ArrayList<String> Creds = dbq.UserCreds();
		email  = Creds.get(0).toString();
		password = Creds.get(1).toString();
		
		//MENU MANAGER:
		if(nw.haveNetworkConnection(this))
			rDB.storeFriendRequestsCount(this);
		menumanager();
		settingsMenumanager(this);

		//SET TITLE
		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
		PageTitle.setText("People");
		
		//ENABLE SEARCH BUTTON
		final Button searchBtn = (Button)findViewById(R.id.searchButton);
		final EditText searchText = (EditText )findViewById(R.id.searchText);

				
		searchBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	
				actionPerform();
			}
		});	


		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	actionPerform();
		            return true;
		        }
		        return false;
		    }
		});

	    networkAvailable = nw.haveNetworkConnection(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		} else if (networkAvailable) {
			Pintr_A_001_PageLoaderAsync loadDtlsTask 
				= new Pintr_A_001_PageLoaderAsync (this, email, password);
	        loadDtlsTask.execute();
	        loadDtlsTask.pl03 = this;
		}	
	}
	

	@Override
	public void processFinish(String output) {
		showPeople();
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		spinnerRL.removeAllViews();
    	
	}  
	
	public void showPeople(){
		
		LinearLayout masterLinLayout;
		Context context = this;

		//***** GET FRIENDS LIST
		masterLinLayout =  (LinearLayout )findViewById(R.id.peopleFriends);
		RelativeLayout  masterRelLayout =  (RelativeLayout )findViewById(R.id.peopleFriendsHdr);
		masterLinLayout.removeAllViews();
		updateInterface(context
						,"friends"
						,masterLinLayout 
						,masterRelLayout
						);
			
		//***** GET YOULIKE LIST
		masterLinLayout =  (LinearLayout )findViewById(R.id.peopleYouLike);
	    masterRelLayout =  (RelativeLayout )findViewById(R.id.peopleYouLikeHdr);
	    masterLinLayout.removeAllViews();
		updateInterface(context
						,"youlike"
						,masterLinLayout 
						,masterRelLayout
						);
		
	    

		//***** GET LIKEYOU  LIST
		masterLinLayout =  (LinearLayout )findViewById(R.id.peopleWhoLikeYou);
	    masterRelLayout =  (RelativeLayout )findViewById(R.id.peopleWhoLikeYouHdr);
	    masterLinLayout.removeAllViews();
		updateInterface(context
						,"likeyou"
						,masterLinLayout 
						,masterRelLayout
						);
		
		
		//***** GET FRIEND REQUESTS LIST
		masterLinLayout =  (LinearLayout )findViewById(R.id.peopleFriendRequests);
	    masterRelLayout =  (RelativeLayout )findViewById(R.id.peopleFriendRequestsHdr);
	    masterLinLayout.removeAllViews();
		updateInterface(context
						,"friendrequests"
						,masterLinLayout 
						,masterRelLayout
						);
	}
	
	
	
	//UPDATE INTERFACEC
	public void updateInterface(final Context context
								,final String statusString
								,LinearLayout masterLinLayout 
								,RelativeLayout masterRelLayout
								){


		ArrayList<String> friendSQLtDataResults = new ArrayList<String>();
		ArrayList<Integer> peopleUIDs = new ArrayList<Integer>();
		ArrayList<String> peopleHandlers = new ArrayList<String>();

		friendSQLtDataResults = dbm.peopleReader(statusString);
		
		for (int i=0; i < friendSQLtDataResults .size(); i++){
			String[] elements = friendSQLtDataResults.get(i).split(",");
			peopleUIDs.add(Integer.parseInt(elements[0]));
			peopleHandlers.add(elements[1]);
		}

	    displayResults( masterLinLayout, peopleUIDs, peopleHandlers,3,context);

		masterRelLayout.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	
				Intent AutoRegOnBoard = new Intent(context, Pintr_18_PeopleCategoryListerPage.class);
			    AutoRegOnBoard.putExtra("category", statusString);
			    startActivity(AutoRegOnBoard);	

			}
		});	
		
	}
	
	
	public void displayResults(LinearLayout masterLinLayout
									, ArrayList<Integer> userids
									, ArrayList<String> handlers
									,int limitIn
									,final Context contextIn
									){
		
		int jsonlength = userids.size();		
		int pintr_user_id = 0, limit = 0;
		String handler = "";
		
		Boolean networkAvailable  = nw.haveNetworkConnection(contextIn);
		
		Log.d("jsonlength", Integer.toString(jsonlength));
		
		if (limitIn == 0){
			limit = jsonlength;
		}
		else if (limitIn > jsonlength){
			limit = jsonlength;
		}
		else if (limitIn > 0){
			limit = limitIn;
		}
		else {
			limit = jsonlength ;
		}
		
	    //PARSE THE INPUT FILE
		for(int i = 0; i < limit; i++){
			
        	pintr_user_id = userids.get(i);
			handler = handlers.get(i);
	        
	        
		    //ADD HORIZONTAL LAYOUT
		    LinearLayout RL= new LinearLayout (contextIn);
		    RL.setOrientation(LinearLayout.VERTICAL);

		    LinearLayout.LayoutParams lp1;
	        lp1 = new LinearLayout.LayoutParams(
	        		LayoutParams.MATCH_PARENT,
	        		LayoutParams.WRAP_CONTENT);
		    lp1.setMargins(20, 0, 20, 30);
		    
		    RL.setLayoutParams(lp1);
		    masterLinLayout.addView(RL, lp1);
		    
		    RL.setBackgroundResource(R.drawable.profile_background);
	        
    		//ADD TITLE
		    LinearLayout.LayoutParams lp;  
		    TextView valueTV = new TextView(contextIn);
		    valueTV.setTextColor(contextIn.getResources().getColor(R.color.MessageBubbleTextColor));
	        valueTV.setText(handler);
	      
	        valueTV.setPadding (0,10,20,10);
	        lp = new LinearLayout.LayoutParams(
	        		LayoutParams.WRAP_CONTENT,
	        		LayoutParams.WRAP_CONTENT);
	        valueTV.setLayoutParams(lp);

	        RL.addView(valueTV, lp);

	        //MAKE NEW VIEW CLICKABLE
			if(networkAvailable){
		        final int pintr_user_id_final = pintr_user_id;
		        final String handler_final = handler;
		        
		        RL.setOnClickListener(new View.OnClickListener() {			
					@Override
					public void onClick(View v) {
						Log.d("profileview inputs",pintr_user_id_final + handler_final );
						Intent AutoRegOnBoard = new Intent(contextIn, Pintr_16_ProfileDisplayPage.class);
					    AutoRegOnBoard.putExtra("pintr_user_id", pintr_user_id_final);
					    AutoRegOnBoard.putExtra("handler", handler_final);
					    startActivity(AutoRegOnBoard);	
					}
				});	
			}
		}
	}
	
	
	public void actionPerform(){
		
		final EditText searchText = (EditText )findViewById(R.id.searchText);
		String searchTextString = searchText.getText().toString(); 
		
		if(searchTextString.equals("") == false){
		    LinearLayout masterLinLayout =  (LinearLayout )findViewById(R.id.profileSearchResultSection);
		    masterLinLayout.removeAllViews();

		    ArrayList<Integer> userids = new ArrayList<Integer> ();
		    ArrayList<String> handlers = new ArrayList<String> ();
		    
		    ArrayList<String> resultsReturned = searchForUser(searchTextString);
		    if(resultsReturned != null && resultsReturned.size() > 0){

			    userids.add(Integer.parseInt(resultsReturned.get(0)));
			    handlers.add(resultsReturned.get(1));

		    	displayResults(masterLinLayout
									,userids
									,handlers
									,0
									,this
									);	
		    } else{
		    	searchText.setText("");
				searchText.setHint("No matching users found");
		    	
		    }	
		}
		else {
			searchText.setHint("Please enter a search term");
		}
	}
	
	public ArrayList<String> searchForUser(String searchTextString){

		//GET THE RESULTS FROM THE DB
		
		ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();

		ArrayList<String> results = new ArrayList<String>();
		
		String result = rDB.profileSearchResultGetter(email, password, searchTextString);
		
		if(!result.equals("CONNECTIONTIMEOUT")){
			//INTERPRET TEH RESULTING JSON FILE
			JSONArray returnResultsJson = jsh.JSON_Parser(
					result
					, "handler_search_result"
					);
			
			try {
				results.add(returnResultsJson.getJSONObject(0).getString("pintr_user_id"));
				results.add(returnResultsJson.getJSONObject(0).getString("handler"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else results.add("Network unavailable");

		return results ;
	}
	
	
	//**************************************************
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
        if (dbm != null) {
        	dbm.close();
        }
    }



}
