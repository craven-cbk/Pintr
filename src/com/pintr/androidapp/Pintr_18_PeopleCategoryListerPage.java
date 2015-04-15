package com.pintr.androidapp;

import java.util.ArrayList;

import org.json.JSONArray;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;


public class Pintr_18_PeopleCategoryListerPage extends Activity {
	
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	final Pintr_G_018_PeopleHandler pph = new Pintr_G_018_PeopleHandler();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_017_ProfilesDBMaker dbm = new Pintr_G_017_ProfilesDBMaker(this);
	
	Boolean networkAvailable ;
	String email, password;
			
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_17_people_lister_page);

		//MENU MANAGER:
		menumanager();
		settingsMenumanager(this);

		ArrayList<String> Creds = dbq.UserCreds();
		email  = Creds.get(0).toString();
		password = Creds.get(1).toString();
		
		networkAvailable = nw.haveNetworkConnection(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		Bundle extras = getIntent().getExtras();
		String category  = extras.getString("category");
		String title = "";
		
		if(category.equals("friends")){
			title="Friends";
		}else if(category.equals("youlike")){
			title="People You Like";
		}else if(category.equals("likeyou")){
			title="People Who Like You";			
		}else if(category.equals("friendrequests")){
			title="Friend Requests";			
		}

		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
		PageTitle.setText(title);
		
		//GET PEOPLE LIST
		LinearLayout  masterLinLayout =  (LinearLayout )findViewById(R.id.profileListResultSection);
	    updateInterface(this
						,category
						,masterLinLayout 
						);
	}

	//UPDATE INTERFACEC
	public void updateInterface(final Context context
								,String statusString
								,LinearLayout masterLinLayout 
								){

		String JSONstringIn = "";
		JSONArray JSONArrayIn = new JSONArray();
		ArrayList<Integer> peopleUIDs = new ArrayList<Integer>();
		ArrayList<String> peopleHandlers = new ArrayList<String>();
		ArrayList<String> friendSQLtDataResults = new ArrayList<String>();
		
		if(networkAvailable){
			JSONstringIn  = pph.getPeopleList(this, statusString, email, password);
			JSONArrayIn = jsh.JSON_Parser( JSONstringIn
										, "fr_like_stat"
										);
			pph.storeJSONData(JSONArrayIn, statusString, this);
		}
		friendSQLtDataResults = dbm.peopleReader(statusString);
		
		for (int i=0; i < friendSQLtDataResults .size(); i++){
			String[] elements = friendSQLtDataResults.get(i).split(",");
			peopleUIDs.add(Integer.parseInt(elements[0]));
			peopleHandlers.add(elements[1]);
		}

	    displayResults( masterLinLayout, peopleUIDs, peopleHandlers, 0, this);
		
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
		    
		    RL.setBackgroundResource(R.drawable.qna_background);
	        
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