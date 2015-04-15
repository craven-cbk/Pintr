package com.pintr.androidapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;


public class Pintr_14_DiscussionTool extends Activity  implements OnItemSelectedListener 
																, Pintr_PL_03_LoginLoadUserListener {


	Spinner InterestPickerSpinner;
	int topic_id;
	int[] InterestIDs;
	String ORIGIN ;
	
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_008_InterestsrDBMSManager rInt = new Pintr_G_008_InterestsrDBMSManager();
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_011_QNASubmsnDBMSManager rDB = new Pintr_G_011_QNASubmsnDBMSManager();
	final Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_14_question_answer_page);
		
		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		//GET THE USER DETAILS FROM THE ONBOARD DB
		ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();
		final int yourUID = Integer.parseInt(Creds.get(2).toString());

		//THIS SHOULD HAVE A VALUE OF "MAKENEWQUESTION" WHEN MAKING NEW QUESTION - IE FROM CLASS 12
		
		Bundle extras = getIntent().getExtras();
		ORIGIN = extras.getString("ORIGIN");
		final String QuestionType  = extras.getString("QuestionType");
		final String QuestionTitle = extras.getString("QuestionTitle");
		final int QuestionID  = extras.getInt("QuestionID");
		final int QuestionMakerID  = extras.getInt("QuestionMakerID");
		final int AnswererID  = extras.getInt("AnswererID");
		final int hashtag_id  = extras.getInt("hashtag_id");
		final String hashtag    = extras.getString("hashtag");

		Button SubBtn = (Button)findViewById(R.id.SubmitButton);
		Button ViewUserButton = (Button)findViewById(R.id.ViewUserButton );
		Button BackToQuestionsButton = (Button)findViewById(R.id.BackToQuestionsButton);
		Button nuAnsButton = (Button)findViewById(R.id.nuAnsButton);
		ViewUserButton.setVisibility(View.INVISIBLE);
		
		TextView QuestionDisplay = (TextView)findViewById(R.id.textPageHeader);
		EditText QuestionEntryField = (EditText)findViewById(R.id.QuestionEntryField);
		EditText AnswerEntryField = (EditText)findViewById(R.id.AnswerEntryField);
		EditText HashtagEntryField = (EditText)findViewById(R.id.HashtagEntryField);
		LinearLayout AnswerDisplayArea = (LinearLayout)findViewById(R.id.AnswerDisplayArea);
		LinearLayout ButtonDisplayArea = (LinearLayout)findViewById(R.id.ButtonDisplayArea);
		View InterestsView = findViewById(R.id.InterestsView);

		//DEFINE THE ITEMS THAT WILL APPEAR IN THE "INTERESTS" DROP-DOWN SPINNER
		String InterestsJSONStr = rInt.UserInterestsExtract(email, password);
		
		//IF THE CONNECTION TO INTERESTS DIDN'T TIME OUT, DO THE WHOLE QUESTION THING
		if(!InterestsJSONStr.equals("CONNECTIONTIMEOUT")){
			
			JSONArray InterestsJSONArray = jsh.JSON_Parser(	InterestsJSONStr
															, "usersTopics"
															);
			int arrayLength = InterestsJSONArray.length();    	
			int[] arr_images = new int[arrayLength +1 ];
			InterestIDs  = new int[arrayLength + 1];
			String[] Interests = new String[arrayLength + 1];
			String[] InterestStringArray = new String[arrayLength + 1];

	    	Interests[0] = "Select the question topic";
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
								Pintr_14_DiscussionTool.this
								, spinnerFormatXML  
								, InterestStringArray
								, Interests
								, Interests
								, inflater
								, arr_images
								);
			InterestPickerSpinner.setAdapter(InterestsDDList);
			InterestPickerSpinner.setOnItemSelectedListener(this);
			InterestPickerSpinner.setSelection(0);

			// IF THIS IS A REGISTRATION QUESTION, GET A 
			//RANDOM QUESTION RELATED TO THE USERS INTERESTS
			Log.d("ORIGIN", "->" + ORIGIN);
			if(ORIGIN != null){

				QuestionDisplay.setText("Guess the question");
				
				Log.d("ORIGIN", "->" + ORIGIN);
				LinearLayout HeaderView = (LinearLayout)findViewById(R.id.HeaderView);;
				LinearLayout nuAnsButtonArea = (LinearLayout)findViewById(R.id.nuAnsButtonArea);
				HeaderView.setVisibility(View.GONE);
				nuAnsButtonArea.setVisibility(View.VISIBLE);
				QuestionEntryField.setHint("What's the question to this answer?");
				AnswerEntryField.setHint("And what would your answer be?");
			}

			
			// 1. - SUBMIT A NEW QUESTION
			if(QuestionType.equals("MAKENEWQUESTION")){

				QuestionDisplay.setText("Ask a new question");
				
				QuestionEntryField.setVisibility(View.INVISIBLE);
				AnswerEntryField.setVisibility(View.INVISIBLE);
				AnswerDisplayArea.setVisibility(View.INVISIBLE);
				ButtonDisplayArea.setVisibility(View.INVISIBLE);
				HashtagEntryField.setVisibility(View.INVISIBLE);
				
				QuestionDisplay.setText("Ask  A New Question");
			}
			
			// 2. **OR** GET CONVERSATION/Q&A THREAD
			else if(QuestionType.equals("GETQN")){

				QuestionDisplay.setText("What do you think?");

				QuestionDisplay.setText(QuestionTitle);
				HashtagEntryField.setText(hashtag);
				if(hashtag.equals("")){
					HashtagEntryField.setVisibility(View.GONE);
				}
				QuestionEntryField.setVisibility(View.GONE);
				InterestsView.setVisibility(View.GONE);
				HashtagEntryField.setEnabled(false);

				ViewUserButton.setVisibility(View.VISIBLE);
				
				String ThreadContents = rDB.JSONQuestionGetter(
												"GETQN"
												,email
												,password
												,1
												,QuestionMakerID
												,AnswererID
												,"",""
												,QuestionID
												,0,0,"",0,""
												);
				Log.d("ThreadContents",ThreadContents);
				
				if(!ThreadContents.equals("CONNECTIONTIMEOUT")){
					JSONArray ThreadContentsJSONArray = jsh.JSON_Parser(ThreadContents
																		, "discussiontree"
																		);
					LinearLayout ConvoLayout = (LinearLayout)findViewById(R.id.DiscussionDisplayView);
	
					writeQuestionLists(
							ThreadContentsJSONArray
						    ,ConvoLayout 
						    );
				}
			}
			
			//DETERMINE THE CONVERSATION COUNTERPARTY
			int userToViewTemp = 0;
			if(yourUID == QuestionMakerID){
				userToViewTemp = AnswererID;
			}
			else if(yourUID == AnswererID){
				userToViewTemp = QuestionMakerID;
			}
			else {
				userToViewTemp = QuestionMakerID;					
			}
			
			final int userToView = userToViewTemp ;
			
			//ASSIGN THE FUNCTIONS FOR THE SUBMIT BUTTON 
			SubBtn.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {

					if(QuestionType.equals("MAKENEWQUESTION")){

						int interestSelectionPosition = InterestPickerSpinner.getSelectedItemPosition();
						int interestSelection = InterestIDs[interestSelectionPosition];
						SubmitNewQuestion(interestSelection, ORIGIN );	
					}
					
					else if(QuestionType.equals("GETQN")){
						
						SubmitNewAnswer(QuestionMakerID
										, QuestionID
										, AnswererID
										, userToView
										, hashtag_id
										, hashtag
										, topic_id
										);	
					}
				}
			});	
			
			nuAnsButton.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					getRecommendedQuestion();
				}
			});	
			
			final Intent gotoQuestionsPage = new Intent(this, Pintr_12_QuestionsManager.class);
			
			BackToQuestionsButton.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
				    startActivity(gotoQuestionsPage );
				}
			});	


			ViewUserButton.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					gotoNextPage(Pintr_16_ProfileDisplayPage.class
								, userToView
								);
				}
			});	
		//IF THERE IS NO CONNECTION, HIDE EVERYTHING
		} else{

			HashtagEntryField.setVisibility(View.GONE);
			QuestionEntryField.setVisibility(View.GONE);
			InterestsView.setVisibility(View.GONE);
			HashtagEntryField.setEnabled(false);
			QuestionDisplay.setText(QuestionTitle);
		}
		 		
	}
	

	@Override
	public void processFinish(String asyncVal) {
		
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		spinnerRL.removeAllViews();
		populateRecommendedAnswer(asyncVal);
	}  

	public void getRecommendedQuestion(){

		ProgressBar pbspnner  = new ProgressBar(this);;
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		LinearLayout.LayoutParams iRL 
			= new LinearLayout.LayoutParams(50, 50);
		spinnerRL.addView(pbspnner,iRL);

		int interestSelectionPosition = InterestPickerSpinner.getSelectedItemPosition();
		int interestSelection = InterestIDs[interestSelectionPosition];
		Pintr_A_004_QuestionRecommendLoaderAsync Task 
			= new Pintr_A_004_QuestionRecommendLoaderAsync (this
															, Integer.toString(interestSelection)
															);
		Task.execute();
		Task.p14 = this;
	}
	
	
	
	public void populateRecommendedAnswer(String asyncVal){

		Log.d("ASYNTASC PASSD JSON", asyncVal);
		
		if(!asyncVal.equals("CONNECTIONTIMEOUT")
				&& !asyncVal.equals("EMPTYQUERY")
			){
			LinearLayout ConvoLayout = (LinearLayout)findViewById(R.id.DiscussionDisplayView);
			ConvoLayout.removeAllViews();
			displayAnswer( ConvoLayout 
							,asyncVal
							,"Some user"
							,"Some time");
		}
	}

	public void SubmitNewAnswer(
					int QuestionMakerID
					, int QuestionID
					, int AnswererID
					, int CounterpartyId
					, int hashtag_id
					, String hashtag
					, int topic_id_in
					){		
		//GET THE USER DETAILS FROM THE ONBOARD DB
		ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();
		
		EditText NewAnswerInput = (EditText)findViewById(R.id.AnswerEntryField);
		String NewAnswer = NewAnswerInput.getText().toString();

		//GET DATETIME CLASS KICKS OFF
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());
		Calendar now = Calendar.getInstance();
		String TimeOfAnswerSubmission  = dateFormat.format(now.getTime());

		rDB.JSONQuestionGetter( "SUBANS"
								,email
								,password
								,topic_id_in
								,QuestionMakerID
								,AnswererID
								,""
								,NewAnswer
								,QuestionID
								,0
								,0
								,TimeOfAnswerSubmission
								,hashtag_id
								,hashtag
								);
		
		//SEND NOTIFICATION TO QNA COUNTERPARTY
		String ServerResponse = 
				gcmuf.dispatchMessageToServer(	Integer.toString(CounterpartyId)
												,email
												,password
												,NewAnswer
												,"ANS"
												);

		Log.d("NOTIFICATION SERVER RESPONSE", ServerResponse );

		startActivity(new Intent(this, Pintr_12_QuestionsManager.class));	
	}

	public void SubmitNewQuestion(	int interestSelection
									, String ORIGIN
									){

		//GET THE USER DETAILS FROM THE ONBOARD DB
		ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();
		
		EditText QuestionText = (EditText)findViewById(R.id.QuestionEntryField);
		EditText AnswerText = (EditText)findViewById(R.id.AnswerEntryField);
		EditText Hashtag = (EditText)findViewById(R.id.HashtagEntryField);
		TextView ErrorMessageLabel = (TextView)findViewById(R.id.ErrorMessageLabel);

		String QuestionGiven = QuestionText.getText().toString();
		String AnswerGiven = AnswerText.getText().toString();
		String HashtagText= Hashtag.getText().toString().replaceAll("#", "");
		
		//GET DATETIME CLASS KICKS OFF
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());
		Calendar now = Calendar.getInstance();
		String TimeOfQuestionSubmission  = dateFormat.format(now.getTime());

		if(QuestionGiven.length() < 20){
			ErrorMessageLabel.setText("Please enter a proper question");
		}
		else if(AnswerGiven.length() < 10){
			ErrorMessageLabel.setText("Please enter a proper answer");
		}
		else{
			rDB.JSONQuestionGetter(
					"SUBQN"
					,email
					,password
					,interestSelection
					,0
					,0
					,QuestionGiven 
					,AnswerGiven
					,0
					,0
					,0
					,TimeOfQuestionSubmission
					,0
					,HashtagText
					);
			if(ORIGIN != null){
				Log.d("ORIGIN", ORIGIN);
				startActivity(new Intent(this, Pintr_PL_01_PostLoginPreLoad.class));
			}else
				startActivity(new Intent(this, Pintr_12_QuestionsManager.class));	
		
		}
	}
	

	public void writeQuestionLists(
						JSONArray inputJSONArray
					    ,LinearLayout masterLinLayout
					    ){

		String answer_text_t = "";
		String date_answer_given_t = "";
		String handler = "";
		String correspondent="";
		
		int arrayLength = inputJSONArray.length();
		
	    //PARSE THE JSON FILE
		for(int i = 0; i < arrayLength; i++){
	        try {
	        	answer_text_t = inputJSONArray.getJSONObject(i).getString("answer_text");
	        	date_answer_given_t = inputJSONArray.getJSONObject(i).getString("date_answer_given");
				handler = inputJSONArray.getJSONObject(i).getString("handler");
				topic_id = inputJSONArray.getJSONObject(i).getInt("topic_id");
				} 
	        catch (JSONException e) {e.printStackTrace();}
	        
	        if(handler.equals("You") == false){
	        	correspondent = handler;
	        }
	        
	        final String date_answer_given = date_answer_given_t;
			
	        
	        //FORMAT THE DATETIME OF THE COMMS
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
			DateFormat df = new SimpleDateFormat("dd LLLL yyyy @ hh:mm a",Locale.getDefault());
			String date_answer_givenFmtd = "";
			long date_answer_givenLong = 0; 
			
			try {
				date_answer_givenLong = simpleDateFormat.parse(date_answer_given).getTime();
				date_answer_givenFmtd = df.format(date_answer_givenLong);
			} catch (ParseException e) {e.printStackTrace();}
			
			displayAnswer( masterLinLayout
							,answer_text_t
							,date_answer_givenFmtd
							,handler);
	        
	    }
		

		Button ViewUserButton = (Button)findViewById(R.id.ViewUserButton );
		ViewUserButton.setText(correspondent + "'s Profile");
	}
	

//	REACT TO ITEMS PRESSED IN THE MENU DROP-DOWN
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    int MenuItemId = item.getItemId();
	    
	    if (MenuItemId==android.R.id.home) {
	    	startActivity(new Intent(this, Pintr_03_entry_page.class));
	    }    
		return true;
	}

	@Override
	public void onItemSelected(	AdapterView<?> parent
								, View view
								, int position
								,long id
								){

		int InterestPickedPosition = InterestPickerSpinner.getSelectedItemPosition();

		EditText QuestionEntryField = (EditText)findViewById(R.id.QuestionEntryField);
		EditText AnswerEntryField = (EditText)findViewById(R.id.AnswerEntryField);
		LinearLayout AnswerDisplayArea = (LinearLayout)findViewById(R.id.AnswerDisplayArea);
		LinearLayout ButtonDisplayArea = (LinearLayout)findViewById(R.id.ButtonDisplayArea);
		TextView ErrorMessageLabel = (TextView)findViewById(R.id.ErrorMessageLabel);
		EditText HashtagEntryField = (EditText)findViewById(R.id.HashtagEntryField);
		
		if(InterestPickedPosition > 0){

			QuestionEntryField.setVisibility(View.VISIBLE);
			AnswerEntryField.setVisibility(View.VISIBLE);
			AnswerDisplayArea.setVisibility(View.VISIBLE);
			ButtonDisplayArea.setVisibility(View.VISIBLE);
			HashtagEntryField.setVisibility(View.VISIBLE);
			ErrorMessageLabel.setText("");
			
			// IF ITS A REGISTRATION QUESTION, GET A NEW QUESTION 
			// EVERY TIME THE INTEREST SPINNER ITEM IS CHANGED
			if(ORIGIN != null)
				getRecommendedQuestion();
		}	
		else {
			QuestionEntryField.setVisibility(View.INVISIBLE);
			AnswerEntryField.setVisibility(View.INVISIBLE);
			AnswerDisplayArea.setVisibility(View.INVISIBLE);
			ButtonDisplayArea.setVisibility(View.INVISIBLE);
			HashtagEntryField.setVisibility(View.INVISIBLE);

			ErrorMessageLabel.setText("Please select a topic to proceed");
			
		}
	}
	
	public void displayAnswer(	LinearLayout masterLinLayout
								,String answer_text_t
								,String date_answer_givenFmtd
								,String handler
			){

        //ADD HORIZONTAL LAYOUT
	    LinearLayout RL= new LinearLayout(this);

	    LinearLayout.LayoutParams lp1;
        lp1 = new LinearLayout.LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT);
	    lp1.setMargins(20, 0, 20, 30);
	    
	    RL.setLayoutParams(lp1);
	    masterLinLayout.addView(RL, lp1);

	    LinearLayout.LayoutParams lp;  
	    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)RL.getLayoutParams();
	    RL.setOrientation(LinearLayout.VERTICAL);
	    
        if(handler.equals("You") == false){
		    RL.setBackgroundResource(R.drawable.callout_yellow_grey_borders2);	
		    params.setMargins(0, 0, 100, 0); 
		    RL.setLayoutParams(params);
		    
        }
        else{
		    RL.setBackgroundResource(R.drawable.callout_white_grey_borders);	
		    params.setMargins(100, 0, 0, 0); 
		    RL.setLayoutParams(params);
        }
        
		//ADD TITLE
	    TextView valueTV = new TextView(this);
	    TextView valueTVQuestion = new TextView(this);
	    TextView valueTVDate= new TextView(this);

	    valueTV.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor));
	    valueTVQuestion.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor));
	    valueTVDate.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor));
	    

    	valueTVQuestion.setText(handler);
        valueTVQuestion.setTypeface(null, Typeface.BOLD);
        valueTVQuestion.setPadding (10,10,10,5);
        RL.addView(valueTVQuestion);

        valueTV.setText(answer_text_t );
        valueTV.setPadding (10,5,10,5);
        lp = new LinearLayout.LayoutParams(
		    	LayoutParams.WRAP_CONTENT,
		    	LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.LEFT;
        valueTV.setLayoutParams(lp);
        RL.addView(valueTV);

        valueTVDate.setText("- " + date_answer_givenFmtd);
    	valueTVDate.setPadding (10,5,10,10);
    	valueTVDate.setTextSize(12);

        lp = new LinearLayout.LayoutParams(
		    	LayoutParams.WRAP_CONTENT,
		    	LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.RIGHT;
        valueTVDate.setLayoutParams(lp);
        
        RL.addView(valueTVDate);
	}
	
	
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}

	public void gotoNextPage(
			Class<?> classIn
			, int pintr_user_id
			){

		//STATE THAT THE INTEREST LIST SHOULD START AT THE TOP
	    Intent AutoRegOnBoard = new Intent(this, classIn);
	    AutoRegOnBoard.putExtra("pintr_user_id", pintr_user_id);
	    startActivity(AutoRegOnBoard);	
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

