package com.pintr.androidapp;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;

import android.util.Log;

import org.json.JSONException;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;


public class Pintr_16_ProfileDisplayPage extends Activity {
	

	//GET THE USER PROFILE DATA FROM THE REMOTE DB
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
	final Pintr_G_014_ProfilerDBMSManager rDB = new Pintr_G_014_ProfilerDBMSManager();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	Pintr_G_016_StatusHandler dbsh = new Pintr_G_016_StatusHandler(this);
	
	Boolean friendStatusNowBool=false
			, likeStatusNowBool=false
			, you_sent_user_friend_request = false
			, user_sent_you_friend_request = false
			, user_likes_you = false
			;
	String finalHandle;
	boolean listener1 = false;
	ImageView likeImg, friendStatusImg ; 
	LinearLayout likeImgButton, friendsImgButton, messageImgButton, eventsInviteImgButton;
	TextView friendsDisplayLabel;
	String fr_datetime  = "null", email , password , uid , yourHandle;
	Context context = this;
	int pintr_user_id  ;
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_16_profile_display_page);
		
		//MENU MANAGER:
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		Bundle extras = getIntent().getExtras();
		pintr_user_id  = extras.getInt("pintr_user_id");
		
		likeImgButton = (LinearLayout)findViewById(R.id.likeImgButton); 
		friendsImgButton = (LinearLayout)findViewById(R.id.friendsImgButton);
		messageImgButton = (LinearLayout)findViewById(R.id.messageImgLOButton);
		eventsInviteImgButton = (LinearLayout)findViewById(R.id.eventsInviteImgButton);
		friendsDisplayLabel = (TextView )findViewById(R.id.friendsDisplayLabel); 
		likeImg = (ImageView)findViewById(R.id.likeImg);  
		friendStatusImg = (ImageView)findViewById(R.id.friendStatusImg);
		
		ArrayList<String> Creds = dbq.UserCreds();
		email  = Creds.get(0).toString();
		password = Creds.get(1).toString();
		uid = Creds.get(2).toString();
		yourHandle = Creds.get(3).toString();

		//GET OTHER USERS PROFILE HEADLINE INFO
		String dobJSONstr = rDB.userDOB(email, password, pintr_user_id);
		String dob = "";
		
		if(!dobJSONstr.equals("CONNECTIONTIMEOUT")){
			
			JSONArray dobHandleJSONArray 
					= jsh.JSON_Parser(	dobJSONstr
										, "handler_profile_data"
										);
			Log.d("DOB JSON", dobJSONstr);
			
		    try {
		    	dob = dobHandleJSONArray.getJSONObject(0).getString("user_dob");
		    	finalHandle = dobHandleJSONArray.getJSONObject(0).getString("handler");
	        	} 
	        catch (JSONException e) {e.printStackTrace();}
		    
			
			//GET RELATIONSHIP DATA
			String relationshipJSONstr = rDB.userRelationship(email
																,password
																,pintr_user_id);
			JSONArray relationshipJSONArray  = jsh.JSON_Parser(
														relationshipJSONstr 
														,"reltion_stat"
														);
			//PARSE RELATIONSHIP DATA INTO VARIABLES
			try {
				likeStatusNowBool = Boolean.valueOf(relationshipJSONArray.getJSONObject(0).getString("you_like_user"));
				user_likes_you = Boolean.valueOf(relationshipJSONArray.getJSONObject(0).getString("user_likes_you"));
				friendStatusNowBool = Boolean.valueOf(relationshipJSONArray.getJSONObject(0).getString("friends"));
				you_sent_user_friend_request = Boolean.valueOf(relationshipJSONArray.getJSONObject(0).getString("you_sent_user_friend_request"));
				user_sent_you_friend_request = Boolean.valueOf(relationshipJSONArray.getJSONObject(0).getString("user_sent_you_friend_request"));
				
			} catch (JSONException e1) {e1.printStackTrace();}
			
			
			//SET LIKE BUTTON
			if(likeStatusNowBool)
				likeImg.setImageResource(R.drawable.green_tick_rs);
	
			//SET FRIENDSHIP STATUS
			parseFriendshipStatus();
			
			
			//ENABLE BUTTONS
			//  LIKE BUTTON:
			likeImgButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					likeAction();
				}
			});
			
			//  FRIENDS BUTTON:
			friendsImgButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					friendshipAction();
				}
			});
			
			//  MESSAGES BUTTON:
		    messageImgButton.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					gotoMsgPage(
							Pintr_20_MessageConversationTool.class
							, pintr_user_id
							, finalHandle
							);
				}
			});
	
			//  EVENTS BUTTON:
			eventsInviteImgButton.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					Intent MenuIntent = new Intent(context, Pintr_22_EventsOverview.class);
				    MenuIntent.putExtra("Origin","FWDINVT" );
				    MenuIntent.putExtra("input_pintr_uid", Integer.toString(pintr_user_id));
				    MenuIntent.putExtra("input_handler", finalHandle);
				    startActivity(MenuIntent);
				}
			});
			
			
	
			//MAKE CATEGORY HEADERS CLICKABLE
			RelativeLayout mostPopQns = (RelativeLayout)findViewById(R.id.profileSectionMostPopularQuestionsHdr);
	        mostPopQns.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					gotoNextPage( 
							Pintr_17_ProfileQuestionsTopicLister.class
							,"MOSTPOP"
							,""
							,finalHandle
							,pintr_user_id
							,Integer.parseInt(uid)
							,0
							,0
							,""
							);
				}
			});	
	
	        RelativeLayout mostRecentQns = (RelativeLayout)findViewById(R.id.profileSectionRecentQuestionsHdr);
			mostRecentQns.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					gotoNextPage( 
							Pintr_17_ProfileQuestionsTopicLister.class
							,"MOSTREC"
							,""
							,finalHandle
							,pintr_user_id
							,Integer.parseInt(uid)
							,0
							,0
							,""
							);
				}
			});		
			
			
			//SET USER DATA FOR PROFILE	
		    setStatusSpinner(dob);
		    
		    
		
			//***********POPULATE PROFILE *********//
			final Pintr_G_015_GeneralTextBubbleMaker bm = new Pintr_G_015_GeneralTextBubbleMaker();
	
			String latestQuestionsJSONstr  = rDB.lastAnsweredQuestions(email, password, pintr_user_id, 3, "");
			String popularQuestionJSONstr = rDB.mostpopAnsweredQuestions(email, password, pintr_user_id, 3, "");
			String interestsJSONstr = rDB.userInterests(email, password, pintr_user_id);
			
			JSONArray popularQuestionsJSONArray = jsh.JSON_Parser(
					popularQuestionJSONstr
					, "handler_top_question_data"
					);
			JSONArray recentQuestionsJSONArray = jsh.JSON_Parser(
					latestQuestionsJSONstr
					, "handler_last_question_data"
					);
			JSONArray userInterestsJSONArray = jsh.JSON_Parser(
					interestsJSONstr
					, "handler_interests_data"
					);
	
			//2.  INTERESTS LIST:
			int interestsArLen = userInterestsJSONArray.length();   
			LinearLayout InterestsLL = (LinearLayout )findViewById(R.id.profileSectionInterests);
	
			for(int i = 0; i < interestsArLen; i++){
		        try {
		        	bm.bubbleTextMaker(InterestsLL
							,this
							,"1"
							,userInterestsJSONArray.getJSONObject(i).getString("topic")
							);
		        	} 
		        catch (JSONException e) {e.printStackTrace();}
			}
	
			
			//3.  MOST POPULAR QUESTIONS:
			int mostpopArLen = popularQuestionsJSONArray.length();   
			LinearLayout mostpopLL = (LinearLayout )findViewById(R.id.profileSectionMostPopularQuestions);
	
			for(int i = 0; i < mostpopArLen; i++){
		        try {
	
		        	final int question_id = Integer.parseInt(popularQuestionsJSONArray.getJSONObject(i).getString("question_id"));
		        	final int user_created_id = Integer.parseInt(popularQuestionsJSONArray.getJSONObject(i).getString("user_created_id"));
		        	final int user_answered_id = Integer.parseInt(popularQuestionsJSONArray.getJSONObject(i).getString("user_answered_id"));
		        	final String question_text = popularQuestionsJSONArray.getJSONObject(i).getString("question_text");
		        	final String viewer_involved = popularQuestionsJSONArray.getJSONObject(i).getString("viewer_involved");
		        	String hashtagIn = popularQuestionsJSONArray.getJSONObject(i).getString("hashtag");
					
		        	if (hashtagIn == null){
		        		hashtagIn="";
		        	}
		        	final String hashtag = hashtagIn;
		        	
		        	LinearLayout mostpopRL  = 
		        			bm.bubbleTextMaker(	mostpopLL
												,this
												,viewer_involved
												,popularQuestionsJSONArray.getJSONObject(i).getString("question_text")
												);
		        	
		        	
			        //MAKE NEW VIEW CLICKABLE
		        	mostpopRL.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
	
							gotoNextPage(
									Pintr_14_DiscussionTool.class
									, "GETQN"
									, ""
									, question_text
									, user_created_id
									, user_answered_id
									, question_id
									, 0
									, hashtag
									);
						}
					});	
	        	} 
		        catch (JSONException e) {e.printStackTrace();}
			}		
			
	
			//4.  MOST RECENT QUESTIONS:
			int mostRecArLen = recentQuestionsJSONArray.length();   
			LinearLayout mostRecLL = (LinearLayout )findViewById(R.id.profileSectionRecentQuestions);
	
			for(int i = 0; i < mostRecArLen; i++){
		        try {
	
		        	final int question_id = Integer.parseInt(recentQuestionsJSONArray.getJSONObject(i).getString("question_id"));
		        	final int user_created_id = Integer.parseInt(recentQuestionsJSONArray.getJSONObject(i).getString("user_created_id"));
		        	final int user_answered_id = Integer.parseInt(recentQuestionsJSONArray.getJSONObject(i).getString("user_answered_id"));
		        	final String question_text = recentQuestionsJSONArray.getJSONObject(i).getString("question_text");
		        	final String viewer_involved = recentQuestionsJSONArray.getJSONObject(i).getString("viewer_involved");
		        	String hashtagIn = recentQuestionsJSONArray.getJSONObject(i).getString("hashtag");
					
		        	if (hashtagIn == null){
		        		hashtagIn="";
		        	}
		        	final String hashtag = hashtagIn;
		        	
		        	
		        	LinearLayout mostpopRL  = 
		        			bm.bubbleTextMaker(	mostRecLL
												,this
												,viewer_involved
												,recentQuestionsJSONArray.getJSONObject(i).getString("question_text")
												);
		        	
			        //MAKE NEW VIEW CLICKABLE
		        	mostpopRL.setOnClickListener(new View.OnClickListener() {			
						@Override
						public void onClick(View v) {
	
							gotoNextPage(
									Pintr_14_DiscussionTool.class
									, "GETQN"
									, ""
									, question_text
									, user_created_id
									, user_answered_id
									, question_id
									, 0
									, hashtag
									);
						}
					});	
		        	} 
		        catch (JSONException e) {e.printStackTrace();}
			}
			
		}
		
	}

	public void setStatusSpinner(String dob){
		
		List<String> statusListArray =  dbsh.getStatusList();
		int arrLen = statusListArray.size();
		
		List<String> statusLabels = new ArrayList<String>();
		List<String> statusIds = new ArrayList<String>();

		String[] statusListArrayItems;
		
		for (int i=0; i < arrLen; i++){
			statusListArrayItems = statusListArray.get(i).split(",");
			statusIds.add(statusListArrayItems[0]);
			statusLabels.add(statusListArrayItems[1]);
		}
		
		
		//SET THE STATUS SPINNER
		String StatusJSON = dbsh.JSONOthersStatusDownload(email, password,Integer.toString(pintr_user_id));
		Log.d("StatusJSON", StatusJSON );
		JSONArray StatusJSONArray  = jsh.JSON_Parser(
											StatusJSON 
											,"current_status"
											);
		int social_status =0;
		String location = "";
		String date_status_set = "";
		
		try {
			 social_status = StatusJSONArray.getJSONObject(0).getInt("social_status");
			 location = StatusJSONArray.getJSONObject(0).getString("location");
			 date_status_set = StatusJSONArray.getJSONObject(0).getString("date_status_set");

		} catch (JSONException e) {	e.printStackTrace();}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		Date updateDateLast = null;
		try {updateDateLast = simpleDateFormat.parse(date_status_set);
		} catch (ParseException e) {e.printStackTrace();}
		Date currentTime = Calendar.getInstance().getTime();
		Long TimeSinceupdateSecs = (currentTime.getTime() - updateDateLast.getTime())/1000;
		
		//IF IT HAS BEEN OVER 4 HOURS SINCE THE LAST STATUS SET, CHANGE TO "NO STATUS"
		if(TimeSinceupdateSecs > 14400 && social_status > 1){
			social_status = 1;
		}
		
		String statusEntry  = "";
		if(friendStatusNowBool){
			statusEntry = " - " + statusLabels.get(social_status-1);
		}
		//INSERT THE NEW INFO INTO THE VIEW
	    TextView handleDisplay = (TextView)findViewById(R.id.handleDisplay);
		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
	    
		Date DOBdate = null;
		try {DOBdate = simpleDateFormat.parse(dob);
		} catch (ParseException e) {e.printStackTrace();}
		currentTime = Calendar.getInstance().getTime();
		Long AgeInSecs = (currentTime.getTime() - DOBdate.getTime())/1000;
		Long AgeInYears = AgeInSecs / (365 * 24 * 3600);
		int roundAgeInYears = Math.round(AgeInYears) ;
		
		handleDisplay.setText(Integer.toString(roundAgeInYears) + " yo, " 
								+ location
								+ statusEntry
								);
		PageTitle.setText(finalHandle);
	}
	
	
	public void parseFriendshipStatus(){

		if(friendStatusNowBool ){
			friendsImgButton.setBackgroundResource(R.color.lightBlue);
			friendStatusImg.setImageResource(R.drawable.person_green_solid);
			friendsDisplayLabel.setText("Friends");
			
		} else if(friendStatusNowBool ==false && user_sent_you_friend_request){
			friendsImgButton.setBackgroundResource(R.color.cthruMessageYellow);
			friendStatusImg.setImageResource(R.drawable.person);
			friendsDisplayLabel.setText("Accept");
			
		} else if(friendStatusNowBool ==false && you_sent_user_friend_request ){
			friendsImgButton.setBackgroundResource(R.color.cthruMessageYellow);
			friendStatusImg.setImageResource(R.drawable.person);
			friendsDisplayLabel.setText("Invited");
			

		} else if(friendStatusNowBool ==false ){
			friendsDisplayLabel.setText("Connect");
			friendsImgButton.setBackgroundResource(R.color.lightBlue);
			friendStatusImg.setImageResource(R.drawable.person);
		}
	}
	
	public void likeAction(){

		Log.d("likeStatusNow PREPRESS", Boolean.toString(likeStatusNowBool ));
		if(likeStatusNowBool ==true)
			likeStatusNowBool  = false;
		else likeStatusNowBool = true;
		Log.d("friendStatusNow", Boolean.toString(friendStatusNowBool));
		Log.d("likeStatusNow POSTPRESS", Boolean.toString(likeStatusNowBool ));

		if(likeStatusNowBool)
			likeImg.setImageResource(R.drawable.green_tick_rs);
		else 
			likeImg.setImageResource(R.drawable.grey_tick_rs);
		
		rDB.changeRelationship(  email
								, password
								, pintr_user_id
								, likeStatusNowBool 
								, user_likes_you 
								, friendStatusNowBool
								, you_sent_user_friend_request 
								, user_sent_you_friend_request
								,"null"
								);
	}
	
	
	public void friendshipAction(){

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
				
		//FRIENDS - DEFRIEND
		if( friendStatusNowBool ){
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	//"YES" BUTTON CLICKED
						friendStatusNowBool=false;
						user_sent_you_friend_request = false;
						you_sent_user_friend_request = false;
						
						friendsDisplayLabel.setText("Connect");
						friendStatusImg.setImageResource(R.drawable.person);
						friendsImgButton.setBackgroundResource(R.color.lightBlue);	
						updateRelationship();
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //"NO" BUTTON CLICKED
			            break;
			        }
			    }
			};

			builder.setMessage("Do you want to de-friend this person?")
							.setPositiveButton("Yes", dialogClickListener)
							.setNegativeButton("No", dialogClickListener)
							.show()
							;
			
		}

		//NOT FRIENDS - ACCEPT INVITE
		else if( user_sent_you_friend_request ){

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	//"YES" BUTTON CLICKED
						friendStatusNowBool=true;
						user_sent_you_friend_request = false;
						you_sent_user_friend_request = false;

						friendsImgButton.setBackgroundResource(R.color.lightBlue);
						friendStatusImg.setImageResource(R.drawable.person_green_solid);
						friendsDisplayLabel.setText("Friends");
						
						rDB.storeFriendRequestsCount(context);	
						updateRelationship();
						gcmuf.dispatchMessageToServer(	Integer.toString(pintr_user_id),email,password,yourHandle,"FRA");
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //"NO" BUTTON CLICKED
						friendStatusNowBool=false;
						user_sent_you_friend_request = false;
						you_sent_user_friend_request = false;
						
						friendsDisplayLabel.setText("Connect");
						friendStatusImg.setImageResource(R.drawable.person);
						friendsImgButton.setBackgroundResource(R.color.lightBlue);	
						updateRelationship();
			            break;
			        }
			    }
			};

			builder.setMessage("Do you want to add this person to your friends?")
							.setPositiveButton("Yes", dialogClickListener)
							.setNegativeButton("No", dialogClickListener).show()
							;
					
		}
		//NOT FRIENDS, NO FRIENDSHIP INVITES - YOU INVITE THEM
		else if( !you_sent_user_friend_request ){

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
			        switch (which){
			        case DialogInterface.BUTTON_POSITIVE:
			        	//"YES" BUTTON CLICKED
						friendStatusNowBool=false;
						user_sent_you_friend_request = false;
						you_sent_user_friend_request=true;
						
						friendsImgButton.setBackgroundResource(R.color.cthruMessageYellow);
						friendStatusImg.setImageResource(R.drawable.person);
						friendsDisplayLabel.setText("Invited");
						
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
						Calendar now = Calendar.getInstance();
						fr_datetime  = dateFormat.format(now.getTime());
						
						updateRelationship();
						gcmuf.dispatchMessageToServer(	Integer.toString(pintr_user_id),email,password,yourHandle,"FRQ");
			            break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            //"NO" BUTTON CLICKED
			            break;
			        }
			    }
			};

			builder.setMessage("Do you want to send this person a friend request?")
							.setPositiveButton("Yes", dialogClickListener)
							.setNegativeButton("No", dialogClickListener).show()
							;
		}
	}
	
	public void updateRelationship(){
		rDB.changeRelationship(  email
								, password
								, pintr_user_id
								, likeStatusNowBool 
								, user_likes_you 
								, friendStatusNowBool
								, you_sent_user_friend_request 
								, user_sent_you_friend_request
								, fr_datetime
								);	
	}
	
	//POPULATE  DATE SPINNERS
	public void setSpinnerValues(
					Spinner SpinnerIn
					,String [] spinnerItems
					){
		
		//POPULATE SPINNER LIST
		List<String> SpinnerList = new ArrayList<String>();
		int spinnerLength = spinnerItems.length;
		for(int j=0; j < spinnerLength ; j++){

			SpinnerList.add(spinnerItems[j]);	
		}
			
		//ADD ADAPTER TO SPINEER
		ArrayAdapter<String> RangeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, SpinnerList);
		RangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerIn.setAdapter(RangeAdapter );	
		
			
	}
	
	public void gotoMsgPage(
			Class<?> classIn
			, int uid
			, String handle
			){
		HashMap<Integer, String> participants = new HashMap<Integer, String>();
		participants.put(uid, handle);
		//STATE THAT THE INTEREST LIST SHOULD START AT THE TOP
	    Intent AutoRegOnBoard = new Intent(this, classIn);
	    AutoRegOnBoard.putExtra("participants", participants);
		
	    startActivity(AutoRegOnBoard);	
	}
	
	public void gotoNextPage(
			Class<?> classIn
			, String QuestionType
			, String JSONQuestionListName
			, String QuestionTitle
			, int QuestionMakerID
			, int AnswererID
			, int QuestionID
			, int hashtag_id
			, String hashtag
			){

		//STATE THAT THE INTEREST LIST SHOULD START AT THE TOP
	    Intent AutoRegOnBoard = new Intent(this, classIn);
	    AutoRegOnBoard.putExtra("QuestionType", QuestionType);
	    AutoRegOnBoard.putExtra("QuestionTitle", QuestionTitle);
	    AutoRegOnBoard.putExtra("JSONQuestionListName", JSONQuestionListName);
	    AutoRegOnBoard.putExtra("QuestionMakerID", QuestionMakerID);
	    AutoRegOnBoard.putExtra("AnswererID", AnswererID);
	    AutoRegOnBoard.putExtra("QuestionID", QuestionID);
	    AutoRegOnBoard.putExtra("hashtag_id", hashtag_id);
	    AutoRegOnBoard.putExtra("hashtag", hashtag);
	    startActivity(AutoRegOnBoard);	
	}

	
	//**************************************************

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
        if (dbsh != null) {
        	dbsh.close();
        }
    }  
}