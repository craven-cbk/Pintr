package com.pintr.androidapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;


public class Pintr_13_QuestionsTopicLister extends Activity implements OnItemSelectedListener  {


	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_010_QuestionsDBMSManager rDB = new Pintr_G_010_QuestionsDBMSManager();
	final Pintr_G_008_InterestsrDBMSManager rInt = new Pintr_G_008_InterestsrDBMSManager();
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	
	String maxDateParsed = "";
	Spinner InterestPickerSpinner;
	String email = "";
	String password = "";
	String QuestionType = "";
	int hashtag_id = 0;
	int QuestionID = 0;
	String JSONQuestionListName = "";
	int[] InterestIDs  ;
	int InterestIdCurrent = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_13_questions_topic_lister);
		
		final LinearLayout RecentConvosLayout = (LinearLayout)findViewById(R.id.QuestionActivitiesDisplayView);

		//MENU MANAGER:
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		TextView Title  = (TextView)findViewById(R.id.textPageHeader);
		Title.setText(R.string.pintr_13_questions_topic_lister_page_title);
		
		Bundle extras = getIntent().getExtras();
		QuestionType  = extras.getString("QuestionType");
		JSONQuestionListName  = extras.getString("JSONQuestionListName");
		QuestionID  = extras.getInt("QuestionID");
		final String QuestionTitleInput = extras.getString("QuestionTitle");
		hashtag_id  = extras.getInt("hashtag_id");

		final Button TenMoreQuestionsButton = (Button)findViewById(R.id.TenMoreQuestionsButton);
			
		//GET THE USER DETAILS FROM THE ONBOARD DB
		ArrayList<String> Creds = dbq.UserCreds();
		email  = Creds.get(0).toString();
		password = Creds.get(1).toString();
		
		//GET DATETIME CLASS KICKS OFF
		//String kickoffTimeStr = dateFormat.format(kickOffTime.getTime());
		
		//RENAME THE SECTION TITLE
		InterestPickerSpinner = (Spinner)findViewById(R.id.InterestPickerSpinner);
		View InterestsView = findViewById(R.id.InterestsView);

		
		//HIDE THE INTEREST SPINNER AREA IF THE TOPIC IS WRONG
		if(QuestionType.equals("RECOMMENDED") == false
			&& QuestionType.equals("TRENDING") == false
					){
			InterestsView.setVisibility(View.GONE);
			//LOAD THE FIRST SET OF QUESTIONS
			loadQuestions(
						email
						,password
						,QuestionType
						,hashtag_id
						,QuestionID
						,JSONQuestionListName
						,RecentConvosLayout
						,0
						);
		}
		else{
			String InterestsJSONStr = rInt.UserInterestsExtract(email, password);
			
			if(!InterestsJSONStr.equals("CONNECTIONTIMEOUT")){
				
		    	JSONArray InterestsJSONArray = jsh.JSON_Parser( InterestsJSONStr
																, "usersTopics"
																);
				int arrayLength = InterestsJSONArray.length();    	
				int[] arr_images = new int[arrayLength +1 ];
				InterestIDs  = new int[arrayLength + 1];
				String[] Interests = new String[arrayLength + 1];
				String[] InterestStringArray = new String[arrayLength + 1];

		    	Interests[0] = "Filter by interests";
		    	InterestIDs[0] = 0;
		    	arr_images[0] = (R.drawable.grey_tick_rs) ;
				
			    //PARSE THE JSON FILE
				for(int i = 0; i < arrayLength; i++){
			        try {
			        	Interests[i+1] = InterestsJSONArray.getJSONObject(i).getString("topic");
			        	InterestIDs[i+1] = Integer.parseInt(InterestsJSONArray.getJSONObject(i).getString("topic_id"));
			        	arr_images[i+1] = (R.drawable.green_tick_rs);
						} 
			        catch (JSONException e) {e.printStackTrace();}
				}
				
				InterestPickerSpinner = (Spinner)findViewById(R.id.InterestPickerSpinner);
				
				LayoutInflater inflater = getLayoutInflater();
				int spinnerFormatXML = R.layout.pintr_gx_001_interes_spinner_view_format;
				ArrayAdapter<String> InterestsDDList 
							= new Pintr_G_012_InteractiveSpinnerAdapter(
									Pintr_13_QuestionsTopicLister.this
									, spinnerFormatXML  
									, InterestStringArray
									, Interests
									, Interests
									, inflater
									, arr_images
									);
				InterestPickerSpinner.setAdapter(InterestsDDList);
				InterestPickerSpinner.setOnItemSelectedListener(this);
				//InterestPickerSpinner.setSelection(0);
			}
		}
		
		//SET THE SECTION TITLE 
		String QuestionTitle = "";
		if(QuestionType.equals("RECENT")){
			QuestionTitle = "Latest Conversation Updates";
		} else if(QuestionType.equals("RECOMMENDED")){
			QuestionTitle = "Recommended Questions";
		}else if(QuestionType.equals("YOURANSWERS")){
			QuestionTitle = "Answers to your questions";
		}else if(QuestionType.equals("YOURQUESTIONS")){
			QuestionTitle = "Questions You've Asked";
		}else if(QuestionType.equals("TRENDING")){
			QuestionTitle = "Trending hashtags";
		}else if(QuestionType.equals("GETTRNDQNS")){
			QuestionTitle = "#" + QuestionTitleInput;
		}else if(QuestionType.equals("FRIENDQUESTIONS")){
			QuestionTitle = "Your Friends' Questions";
		}
		Title.setText(QuestionTitle );
		
		//SET THE SPINNER TO INVISIBLE UNLESS ITS A TRENDING OR RECOMMEND PAGE
		if(QuestionType.equals("TRENDING")){
			TenMoreQuestionsButton.setVisibility(View.INVISIBLE);
		}
		
				
				
		//ACTIVATE THE TEN MORE QUESTIONS BUTTON
		TenMoreQuestionsButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				String NextConvosJSON = rDB.JSONQuestionGetter(
						10
						,email
						,password
						,QuestionType
						,""
						,hashtag_id
						,QuestionID
						,maxDateParsed
						,InterestIdCurrent
						);

				Log.d("NextConvosJSON",NextConvosJSON);

				Log.d("maxDateParsed",maxDateParsed);

				
				if(NextConvosJSON.equals("EMPTYQUERY") == false){
					
					JSONArray NextConvosJSONArray = jsh.JSON_Parser(
							NextConvosJSON 
							, JSONQuestionListName
							);
					
					writeQuestionLists(
						NextConvosJSONArray
					    ,RecentConvosLayout 
					    ,QuestionType
					    );
				}
				else if(NextConvosJSON.equals("EMPTYQUERY")){
					TenMoreQuestionsButton.setVisibility(View.INVISIBLE);
				}
			}
		});	
		
	}
	
	
	
	public void loadQuestions(
			String email
			,String password
			,String QuestionType
			,int hashtag_id
			,int QuestionID
			,String JSONQuestionListName
			,LinearLayout RecentConvosLayout
			,int interestSelection
			){
		//GET THE RECORDS FOR THE QUESTION TYPE ON THIS PAGE
		//  AND TURN THEM INTO A JSON ARRAY TO BE HANDLED
		int numberOfQuestionsToReturn = 10;
		
		String ConvosJSON = rDB.JSONQuestionGetter(
										numberOfQuestionsToReturn 
										,email
										,password
										,QuestionType
										,""
										,hashtag_id
										,QuestionID
										,""
										,interestSelection
										);
		
		if(!ConvosJSON.equals("EMPTYQUERY")
				&& !ConvosJSON.equals("CONNECTIONTIMEOUT")
				){
			JSONArray ConvosJSONArray = jsh.JSON_Parser(
											ConvosJSON
											, JSONQuestionListName
											);
	
			writeQuestionLists(
					ConvosJSONArray
				    ,RecentConvosLayout 
				    ,QuestionType
				    );
		}
	}
	
	
	public void writeQuestionLists(
			JSONArray inputJSONArray
		    ,LinearLayout masterLinLayout 
		    ,final String function
		    ){

		String question_text_t = "";
		int question_maker_id_t = 0;
		int QuestionID_t = 0;
		int correspondent_id_t = 0;
		String last_comms_date = "";
		String BubbleText = "";
		String handler = "";
		int hashtag_id_t = 0;
		String hashtag_t = "";
		String correspondent_read_question= "";
		
		
		int arrayLength = inputJSONArray.length();

	    //PARSE THE JSON FILE
		for(int i = 0; i < arrayLength; i++){
			hashtag_t = "";
			
			if(function.equals("RECENT")){
		        try {
		        	QuestionID_t = inputJSONArray.getJSONObject(i).getInt("question_id");
					question_maker_id_t = inputJSONArray.getJSONObject(i).getInt("user_made_question_id");
		        	question_text_t = inputJSONArray.getJSONObject(i).getString("question_text");
					correspondent_id_t = inputJSONArray.getJSONObject(i).getInt("correspondent_id");
					last_comms_date = inputJSONArray.getJSONObject(i).getString("last_comms_date");
					handler = inputJSONArray.getJSONObject(i).getString("handler");
					hashtag_id_t = inputJSONArray.getJSONObject(i).getInt("hashtag_id");
					hashtag_t = inputJSONArray.getJSONObject(i).getString("hashtag");
					correspondent_read_question = inputJSONArray.getJSONObject(i).getString("correspondent_read_question");
					} 
		        catch (JSONException e) {e.printStackTrace();}
		          
			}
			else if(function.equals("RECOMMENDED")){
			        try {
			        	QuestionID_t = inputJSONArray.getJSONObject(i).getInt("question_id");
						question_maker_id_t = inputJSONArray.getJSONObject(i).getInt("user_made_question_id");
			        	question_text_t = inputJSONArray.getJSONObject(i).getString("question_text");
						correspondent_id_t = inputJSONArray.getJSONObject(i).getInt("correspondent_id");
						last_comms_date = inputJSONArray.getJSONObject(i).getString("last_comms_date");
						hashtag_id_t = inputJSONArray.getJSONObject(i).getInt("hashtag_id");
						hashtag_t = inputJSONArray.getJSONObject(i).getString("hashtag");
						} 
			        catch (JSONException e) {e.printStackTrace();}
			          
			}
			else if(function.equals("YOURQUESTIONS")){
		        try {
		        	QuestionID_t = inputJSONArray.getJSONObject(i).getInt("question_id");
					question_text_t = inputJSONArray.getJSONObject(i).getString("question_text");
					last_comms_date = inputJSONArray.getJSONObject(i).getString("date_question_asked");
					} 
		        catch (JSONException e) {e.printStackTrace();}
			}

			else if(function.equals("TRENDING")){
		        try {
		        	QuestionID_t = inputJSONArray.getJSONObject(i).getInt("hashtag_id");
		        	question_text_t = inputJSONArray.getJSONObject(i).getString("hashtag");
		        	last_comms_date = inputJSONArray.getJSONObject(i).getString("last_comms_date");
					} 
		        catch (JSONException e) {e.printStackTrace();}
				}
			else if(function.equals("GETTRNDQNS")){
		        try {
		        	QuestionID_t = inputJSONArray.getJSONObject(i).getInt("question_id");
					question_maker_id_t = inputJSONArray.getJSONObject(i).getInt("user_made_question_id");
		        	question_text_t = inputJSONArray.getJSONObject(i).getString("question_text");
					correspondent_id_t = inputJSONArray.getJSONObject(i).getInt("correspondent_id");
					last_comms_date = inputJSONArray.getJSONObject(i).getString("last_comms_date");
					hashtag_id_t = inputJSONArray.getJSONObject(i).getInt("hashtag_id");
					hashtag_t = inputJSONArray.getJSONObject(i).getString("hashtag");
					} 
		        catch (JSONException e) {e.printStackTrace();}
				}
			else if(function.equals("YOURANSWERS")){
		        try {
		        	QuestionID_t = inputJSONArray.getJSONObject(i).getInt("question_id");
					question_maker_id_t = inputJSONArray.getJSONObject(i).getInt("user_made_question_id");
		        	question_text_t = inputJSONArray.getJSONObject(i).getString("question_text");
					correspondent_id_t = inputJSONArray.getJSONObject(i).getInt("correspondent_id");
					last_comms_date = inputJSONArray.getJSONObject(i).getString("last_comms_date");
					handler = inputJSONArray.getJSONObject(i).getString("handler");
					hashtag_id_t = inputJSONArray.getJSONObject(i).getInt("hashtag_id");
					hashtag_t = inputJSONArray.getJSONObject(i).getString("hashtag");
					} 
		        catch (JSONException e) {e.printStackTrace();}
			}
			else if(function.equals("FRIENDQUESTIONS")){
		        try {
		        	QuestionID_t = inputJSONArray.getJSONObject(i).getInt("question_id");
					question_maker_id_t = inputJSONArray.getJSONObject(i).getInt("user_made_question_id");
		        	question_text_t = inputJSONArray.getJSONObject(i).getString("question_text");
					correspondent_id_t = inputJSONArray.getJSONObject(i).getInt("correspondent_id");
					last_comms_date = inputJSONArray.getJSONObject(i).getString("last_comms_date");
					handler = inputJSONArray.getJSONObject(i).getString("handler");
					hashtag_id_t = inputJSONArray.getJSONObject(i).getInt("hashtag_id");
					hashtag_t = inputJSONArray.getJSONObject(i).getString("hashtag");
					} 
		        catch (JSONException e) {e.printStackTrace();}
			}
	        Log.d(function, question_text_t);

	        Log.d("QuestionID_t", Integer.toString(QuestionID_t));
	        Log.d("question_maker_id_t", Integer.toString(question_maker_id_t));
			Log.d("question_text_t", question_text_t);
	        Log.d("correspondent_id_t", Integer.toString(correspondent_id_t));
			Log.d("last_comms_date", last_comms_date);
			Log.d("handler", handler);
			Log.d("hashtag_id_t", Integer.toString(hashtag_id_t));
			Log.d("hashtag_t", hashtag_t);

			maxDateParsed = last_comms_date;
	        
	        final int correspondent_id = correspondent_id_t;
	        final String question_text = question_text_t;
	        final int question_maker_id = question_maker_id_t;
	        final int QuestionID = QuestionID_t;
			final int hashtag_id = hashtag_id_t;
	        
	        //FORMAT THE DATETIME OF THE COMMS
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
			DateFormat df = new SimpleDateFormat("dd LLLL yyyy @ hh:mm a",Locale.getDefault());
			String last_comms_dateFmtd = "";
			long last_comms_dateLong = 0; 
			
			try {
				 last_comms_dateLong = simpleDateFormat.parse(last_comms_date).getTime();
				 last_comms_dateFmtd = df.format(last_comms_dateLong);
			} catch (ParseException e) {e.printStackTrace();}
		
			if(hashtag_t.equals("") == false){
				hashtag_t = "\n - #" + hashtag_t ;
			}
			final String hashtag = hashtag_t;
			Log.d("Hashtag is:", " - " + hashtag);
			
			if(function.equals("RECENT")){
				BubbleText =  "With " + handler + "\n " + question_text + " \n " + last_comms_dateFmtd   + hashtag;
			}
			else if(function.equals("RECOMMENDED")){
				BubbleText = " \n " + question_text  +   hashtag  + " \n ";
			}
			else if(function.equals("GETTRNDQNS")){
				BubbleText =  question_text + " \n " + last_comms_dateFmtd  ;
			}
			else if(function.equals("YOURQUESTIONS")){
				BubbleText =  question_text + " \n " + last_comms_dateFmtd  ;
			}
			else if(function.equals("TRENDING")){
				BubbleText =  "#" + question_text  ;
			}
			else if(function.equals("YOURANSWERS")){
				BubbleText =  "With " + handler + "\n " + question_text + " \n " + last_comms_dateFmtd;
			}
			else if(function.equals("FRIENDQUESTIONS")){
				BubbleText =  "Asked by " + handler + "\n " + question_text + " \n " + last_comms_dateFmtd;
			}
			

		    //ADD HORIZONTAL LAYOUT
			LinearLayout RL= new LinearLayout(this);
			LinearLayout.LayoutParams lp1;
	        lp1 = new LinearLayout.LayoutParams(
	        		LayoutParams.MATCH_PARENT,
	        		LayoutParams.WRAP_CONTENT);
		    lp1.setMargins(20, 0, 20, 30);
		    
		    RL.setLayoutParams(lp1);
		    masterLinLayout.addView(RL, lp1);

		    RelativeLayout.LayoutParams lp;
		    RL.setBackgroundResource(R.drawable.qna_background);
	        
    		//ADD TITLE
		    TextView valueTV = new TextView(this);
		    valueTV.setText(BubbleText);
	        if(correspondent_read_question.equals("0")){
		        valueTV.setTypeface(null, Typeface.BOLD);
			    RL.setBackgroundResource(R.drawable.qna_unanswered_background);
	        }
	        valueTV.setPadding (50,10,10,10);
		    valueTV.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor));
	        lp = new RelativeLayout.LayoutParams(
			    	LayoutParams.WRAP_CONTENT,
			    	LayoutParams.WRAP_CONTENT);
	        lp.addRule(RelativeLayout.CENTER_VERTICAL);
	        valueTV.setLayoutParams(lp);

	        RL.addView(valueTV);

	        //MAKE NEW VIEW CLICKABLE
	        RL.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {	

					if(function.equals("RECOMMENDED")
							||function.equals("RECENT")
							||function.equals("GETTRNDQNS")
							||function.equals("YOURANSWERS")
							||function.equals("FRIENDQUESTIONS")
						){

						//THREADS CLICKTHROUGH
						gotoNextPage(
								Pintr_14_DiscussionTool.class
								, "GETQN"
								, ""
								, question_text
								, question_maker_id
								, correspondent_id
								, QuestionID
								, hashtag_id
								, hashtag
								);
					
					}
					else if(function.equals("TRENDING")){
						gotoNextPage(
								Pintr_13_QuestionsTopicLister.class
								, "GETTRNDQNS"
								, "trendingquestions"
								, question_text
								, 0
								, 0
								, 0
								, QuestionID
								, ""
								);
					}
					else if(function.equals("YOURQUESTIONS")){

						Log.d( "CLICK", "Clicked on YourQuestions bubble");
						//THREADS CLICKTHROUGH
						gotoNextPage(
								Pintr_13_QuestionsTopicLister.class
								, "YOURANSWERS"
								, "answerstoyourquestions"
								, question_text
								, question_maker_id
								, correspondent_id
								, QuestionID
								, 0
								, ""
								);
					
					}
				}
			});	
	    	        
		}
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
	    AutoRegOnBoard.putExtra("JSONQuestionListName", JSONQuestionListName);
	    AutoRegOnBoard.putExtra("QuestionMakerID", QuestionMakerID);
	    AutoRegOnBoard.putExtra("AnswererID", AnswererID);
	    AutoRegOnBoard.putExtra("QuestionID", QuestionID);
	    AutoRegOnBoard.putExtra("QuestionTitle", QuestionTitle);
	    AutoRegOnBoard.putExtra("hashtag_id", hashtag_id);
	    AutoRegOnBoard.putExtra("hashtag", hashtag);
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


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {

		//EMPTY GLOBAL VARIABLES
		maxDateParsed = "";
		InterestIdCurrent = 0;
		final Button TenMoreQuestionsButton = (Button)findViewById(R.id.TenMoreQuestionsButton);
		TenMoreQuestionsButton.setVisibility(View.VISIBLE);
		
		LinearLayout RecentConvosLayout = (LinearLayout)findViewById(R.id.QuestionActivitiesDisplayView);
		RecentConvosLayout.removeAllViews();
		int InterestPickedPosition = InterestPickerSpinner.getSelectedItemPosition();
		InterestIdCurrent = InterestIDs[InterestPickedPosition];
		
		loadQuestions(
				email
				,password
				,QuestionType
				,hashtag_id
				,QuestionID
				,JSONQuestionListName
				,RecentConvosLayout
				,InterestIdCurrent
				);

	}


	@Override
	public void onNothingSelected(AdapterView<?> parent) {
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
    }  
}