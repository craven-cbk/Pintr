package com.pintr.androidapp;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;


public class Pintr_10_InterestsOverview extends Activity {

	final Pintr_G_008_InterestsrDBMSManager rDB = new Pintr_G_008_InterestsrDBMSManager();
	final Pintr_G_001_DBMaker db = new Pintr_G_001_DBMaker(this);
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	
    private static final String USER_INTERESTS_TABLE = "P_EK_INT";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_10_interests_overview);

		LinearLayout InterestSubBtnView = (LinearLayout)findViewById(R.id.InterestSubBtnView);
		LinearLayout HeaderView = (LinearLayout)findViewById(R.id.HeaderView );
		
		//IS THIS FROM REGISTRATION?
		String ORIGIN_in ="";
		Bundle extras = getIntent().getExtras();
		if(extras  != null && extras.getString("ORIGIN") != null){
			
			ORIGIN_in = extras.getString("ORIGIN");
			Log.d("ORIGIN", "->" + ORIGIN_in);
			InterestSubBtnView.setVisibility(View.VISIBLE);
			HeaderView.setVisibility(View.GONE);
		}
		final String ORIGIN = ORIGIN_in ;
		
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		if(networkAvailable){

			//GET THE USER DETAILS FROM THE ONBOARD DB
			ArrayList<String> Creds = dbq.UserCreds();
			final String email  = Creds.get(0).toString();
			final String password = Creds.get(1).toString();
			
			//GET INTERESTS FROM REMOTE DB
			String InterestsJSON = rDB.UserInterestsExtract(email,password);
	
			//WRITE USER INTERESTS TO ONBOARD DB
			Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
			 if(!InterestsJSON.equals("CONNECTIONTIMEOUT")){
				JSONArray returnTopicJson = jsh.JSON_Parser(InterestsJSON
												, "usersTopics"
												);
				int numberOfTopics = returnTopicJson.length();
			    Log.d("InterestsJSON", InterestsJSON);		
				
				String topic = "";
				int topic_id = 0;
				String priority = null;
				
				//EMPTY ONBOARD TABLE
				db.EmptyTable(USER_INTERESTS_TABLE);
				
				for(int i = 0; i < numberOfTopics; i++){
				    try {
						topic = returnTopicJson.getJSONObject(i).getString("topic");
						topic_id = returnTopicJson.getJSONObject(i).getInt("topic_id");
						priority = returnTopicJson.getJSONObject(i).getString("priority");
						
						//ADD TO SQLITE DB
						db.UserInterestsUpdate( topic_id
												, topic
												,  priority
												);
						Log.d("ADDED TO INTERESTS TABLE:",  topic);
						} 
			        catch (JSONException e) {e.printStackTrace();}
				}
			}
		}
		
		//READ USER INTERESTS FROM ONBOARD DB AND WRITE INTERESTS TO UI

		ArrayList<String> UserInterests  = new ArrayList<String>();
		
		UserInterests = db.UserInterests();
		
		for(int i = 0; i < UserInterests.size(); i++){
			String[] contents = UserInterests.get(i).split(",");
			writeSections(Integer.parseInt(contents[0]),  contents[1]);
		}
		
		
		View InterestsList = findViewById(R.id.InterestTopicsView);
		
		//MAKE INTERESTS AREA CLICKABLE
		if(networkAvailable){
			InterestsList.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
	
					gotoNextPage( Pintr_11_AllTopicsOfInterest.class,  ORIGIN );
				}
			});	
		}			
		InterestSubBtnView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

				gotoNextPage(Pintr_14_DiscussionTool.class, ORIGIN );
			}
		});	

	}
	
	//SECTION WRITER
	public void writeSections(int topic_id, String topic){

		//ADD VIEWS:
	    LinearLayout masterLinLayout =  (LinearLayout )findViewById(R.id.YourInterestsDisplayView);

	    //ADD HORIZONTAL LAYOUT
	    RelativeLayout RL= new RelativeLayout(this);
	    masterLinLayout.addView(RL);
	    RelativeLayout.LayoutParams lp;

	    //ADD INTEREST TICK ICON
        ImageView Tick = new ImageView(this);
    	Tick.setImageResource(R.drawable.green_tick_rs);
    	Tick.setTag("green");
	    lp = new RelativeLayout.LayoutParams(
		    	LayoutParams.WRAP_CONTENT,
		    	LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        final int NewId = topic_id;
        Tick.setId(NewId);
        Tick.setPadding (10,10,10,10);
        Tick.setLayoutParams(lp);
        Tick.requestLayout();
        Tick.getLayoutParams().height = 100;
        Tick.getLayoutParams().width = 100;

        RL.addView(Tick);
        
		//ADD TITLE
	    TextView valueTV = new TextView(this);
        valueTV.setText(topic );
        valueTV.setPadding (150,10,10,10);
        lp = new RelativeLayout.LayoutParams(
		    	LayoutParams.WRAP_CONTENT,
		    	LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        valueTV.setLayoutParams(lp);

        RL.addView(valueTV);

	   	//MAKE NEW VIEW CLICKABLE
        final int topic_id_numeric = topic_id;
        ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();
		  
		boolean networkAvailable = nw.haveNetworkConnection(this) ;
		
		if(networkAvailable){
			//MAKE THE INTERESTED BOOLEAN AND ITS TICK MARK TOGGLE-ABLE
		
	        Tick.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {	
					
					ImageView tickMarkToChange = (ImageView)findViewById(NewId);
					String imageTag = tickMarkToChange.getTag().toString();
					if(imageTag == "grey"){
						tickMarkToChange.setImageResource(R.drawable.green_tick_rs);
						tickMarkToChange.setTag("green");
						rDB.UserDeclareTopicInterest(
								topic_id_numeric
								,email
								,password
								);
					}
					else if(imageTag == "green"){
						tickMarkToChange.setImageResource(R.drawable.grey_tick_rs);
						tickMarkToChange.setTag("grey");
						rDB.UserRemoveTopicInterest(
								topic_id_numeric
								,email
								,password
								);
					}
				}
			});
		} 
		
        //ADD DIVIDER LINE	    	        
	    View GreyLine= new View(this);
        GreyLine.setPadding (5,30,30,5);
        GreyLine.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,3));
        GreyLine.setBackgroundColor(Color.parseColor("#C9C9C9"));
        
        masterLinLayout.addView(GreyLine);
	}
	

	public void gotoNextPage(Class<?> classIn, String ORIGIN){

	    Intent AutoRegOnBoard = new Intent(this, classIn );

	    AutoRegOnBoard.putExtra("ROOT_TOPIC_ID", 0);
	    AutoRegOnBoard.putExtra("ROOT_TOPIC_NAME", "");
	    
		if(ORIGIN.equals("REGISTRATION")){
		    AutoRegOnBoard.putExtra("QuestionType", "MAKENEWQUESTION");
		    AutoRegOnBoard.putExtra("ORIGIN", "REGISTRATION");
		}
	    startActivity(AutoRegOnBoard);	
	}
		

//	CALL THE MENU WHEN THE MENU BUTTON IS PRESSED
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.pintr_menu_items, menu);
		return true;
	}

	

//	REACT TO ITEMS PRESSED IN THE MENU DROP-DOWN
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    Intent MenuIntent = new Intent(this, Pintr_G_005_MenuManager.class);
	    int MenuItemId = item.getItemId();
	    
	    if (MenuItemId==android.R.id.home) {
	    	startActivity(new Intent(this, Pintr_03_entry_page.class));
	    }    
	    else{
 
	    	MenuIntent.putExtra("menuitem", MenuItemId);
		    startActivity(MenuIntent);
	    }
		return true;
	}


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
        if (db != null) {
        	db.close();
        }
    }  
}