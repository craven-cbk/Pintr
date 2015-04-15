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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;


public class Pintr_12_QuestionsManager extends Activity
										implements 	Pintr_PL_03_LoginLoadUserListener {


	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_010_QuestionsDBMSManager rDB = new Pintr_G_010_QuestionsDBMSManager();
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_12_questions_manager);
		

		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}

		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
		PageTitle.setText("Discussions");
		//****  END MENU MANAGER  ******
		
		//GET THE USER DETAILS FROM THE ONBOARD DB
		
		//GET THE RECORDS FOR EACH OF THE FOUR DISPLAY TYPES ON THIS PAGE
		//  AND TURN THEM INTO JSON ARRAYS TO BE HANDLED
		loadConvos();
		
		//ACTIVATE "ADD QUESTION" BUTTON
		Button NewQuestionBtn = (Button)findViewById(R.id.AddQuestionButton);
		NewQuestionBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				gotoNextPage( Pintr_14_DiscussionTool.class, "MAKENEWQUESTION","","",0,0,0,0,"" );
			}
		});	
	}
	

	@Override
	public void processFinish(String output) {
		// TODO Auto-generated method stub
		
	}  
	
	
	public void loadConvos(){
		String messageIfEmpty;
		LinearLayout Layout ;
		
		ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();

		
		LinearLayout RecentQns = (LinearLayout)findViewById(R.id.RecentQuestionsTitleView);
		LinearLayout RecommendedQns = (LinearLayout)findViewById(R.id.RecommendedQuestionsTitleView);
		LinearLayout FriendsQns = (LinearLayout)findViewById(R.id.FriendsQuestionsTitleView);
		LinearLayout YourQns = (LinearLayout)findViewById(R.id.YourQuestionsTitleView);
		LinearLayout TrendingView = (LinearLayout)findViewById(R.id.TrendingTitleView);
		
			
		//  1.  RECENT CONVERSATIONS (ALL)
		messageIfEmpty = "You haven't had any recent conversations";
		Layout = (LinearLayout)findViewById(R.id.RecentQuestionActivitiesDisplayView);
		createSections( email
						,password
						,"RECENT"
						,"recentquestionlist"
						,messageIfEmpty
						,Layout 
						,RecentQns
						);

		//  2.  RECOMMENDED QUESTIONS
		messageIfEmpty = "We don't have any more recommendations for you :(";
		Layout = (LinearLayout)findViewById(R.id.RecommendedQuestionsDisplayView);
		createSections( email
						,password
						,"RECOMMENDED"
						,"recommendedquestionlist"
						,messageIfEmpty
						,Layout 
						,RecommendedQns
						);
		
		//  2a.  FRIENDS QUESTIONS
		messageIfEmpty = "We don't have any more recommendations for you :(";
		Layout = (LinearLayout)findViewById(R.id.FriendsQuestionsDisplayView);
		createSections( email
						,password
						,"FRIENDQUESTIONS"
						,"friendsquestions"
						,messageIfEmpty
						,Layout 
						,FriendsQns
						);

		//   3. TRENDING
		messageIfEmpty = "Nothing's trending at the moment";
		Layout = (LinearLayout)findViewById(R.id.TrendingDisplayView);
		createSections( email
						,password
						,"TRENDING"
						,"trending"
						,messageIfEmpty
						,Layout
						,TrendingView  
						);

		//   4.  YOUR QUESTIONS 
		messageIfEmpty = "You haven't asked any questions recently";
		Layout = (LinearLayout)findViewById(R.id.YourQuestionsDisplayView);
		createSections( email
						,password
						,"YOURQUESTIONS"
						,"yourquestions"
						,messageIfEmpty
						,Layout
						,YourQns 
						);
	}
	
		
	public void createSections( String email
								,String password
								,final String QuestionsType
								,final String JSONDataName
								,final String messageIfEmpty
								,LinearLayout Layout 
								,LinearLayout HeaderView
								){
		String ConvosJSON = rDB.JSONQuestionGetter(
										3
										,email
										,password
										,QuestionsType
										,""
										,0
										,0
										,""
										,0
										);

		Log.d("JSON:", ConvosJSON);
		if( !ConvosJSON.equals("EMPTYQUERY") 
				&& !ConvosJSON.equals("CONNECTIONTIMEOUT") ){
		
			JSONArray newJSONArray = jsh.JSON_Parser(
											ConvosJSON
											,JSONDataName
											);
			
			writeQuestionLists(
					newJSONArray
				    ,Layout
					,QuestionsType
				    );
			
			//MAKE CATEGORY HEADERS CLICKABLE
			HeaderView.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					gotoNextPage( 
							Pintr_13_QuestionsTopicLister.class
							,QuestionsType
							, JSONDataName
							,""
							,0
							,0
							,0
							,0
							,""
							);
				}
			});	

		}
		else{
			//IF THERE ARE NO TOPICS HERE THEN JUST HAVE A POPUP TO SAY SO 
			Layout.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {
					emptySectionPopup(messageIfEmpty);
				}
			});	
		}
	}
	
		

	
	public void writeQuestionLists(
			JSONArray inputJSONArray
		    ,LinearLayout masterLinLayout
		    ,final String function
		    ){

		String question_text_t = "";
		int correspondent_id_t = 0;
		int question_maker_id_t = 0;
		String last_comms_date = "";
		int QuestionID_t = 0;
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
					hashtag_id_t = inputJSONArray.getJSONObject(i).getInt("hashtag_id");
					hashtag_t = inputJSONArray.getJSONObject(i).getString("hashtag");
		        	last_comms_date = "";
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
			
			if(function.equals("TRENDING")==false){
				try {
					 last_comms_dateLong = simpleDateFormat.parse(last_comms_date).getTime();
					 last_comms_dateFmtd = df.format(last_comms_dateLong);
				} catch (ParseException e) {e.printStackTrace();}
			}

			final String hashtag = hashtag_t;
			
			if(function.equals("RECENT")){
				BubbleText =   question_text + " \n " + last_comms_dateFmtd   ;
			}
			else if(function.equals("RECOMMENDED")){
				BubbleText = question_text  ;
			}
			else if(function.equals("YOURQUESTIONS")){
				BubbleText =  question_text + " \n " + last_comms_dateFmtd  ;
			}
			else if(function.equals("TRENDING")){
				BubbleText =  "#" + question_text  ;
			}
			else if(function.equals("FRIENDQUESTIONS")){
				BubbleText =  "Asked by " + handler + "\n " + question_text + " \n " + last_comms_dateFmtd;
			}

	        //ADD HORIZONTAL LAYOUT
		    LinearLayout RL= new LinearLayout (this);
		    RL.setOrientation(LinearLayout.VERTICAL);

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
		    TextView valueTVQuestion = new TextView(this);
		    TextView valueTVHashtag= new TextView(this);
		    valueTV.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor));
		    valueTVQuestion.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor));
		    valueTVHashtag.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor));
	        valueTV.setText(BubbleText);
	        
	        if(handler.equals("")==false){
	        	valueTVQuestion.setText("With " + handler);
		        valueTVQuestion.setTypeface(null, Typeface.BOLD);
		        valueTVQuestion.setPadding (0,0,0,10);
		        RL.addView(valueTVQuestion);
	        }
	        if(correspondent_read_question.equals("0")){
		        valueTV.setTypeface(null, Typeface.BOLD);
			    RL.setBackgroundResource(R.drawable.qna_unanswered_background);
	        }
	        valueTV.setPadding (0,10,20,10);
	        lp = new RelativeLayout.LayoutParams(
			    	LayoutParams.WRAP_CONTENT,
			    	LayoutParams.WRAP_CONTENT);
	        lp.addRule(RelativeLayout.CENTER_VERTICAL);
	        valueTV.setLayoutParams(lp);

	        RL.addView(valueTV);

	        if(hashtag.equals("")==false && function.equals("TRENDING")==false){
	        	valueTVHashtag.setText("- #" + hashtag);
		        valueTVHashtag.setPadding (0,0,0,20);
		        RL.addView(valueTVHashtag);
	        }
	        //MAKE NEW VIEW CLICKABLE
	        RL.setOnClickListener(new View.OnClickListener() {			
				@Override
				public void onClick(View v) {	

					if(function.equals("RECOMMENDED")
							||function.equals("RECENT")
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

						//THREADS CLICKTHROUGH
						gotoNextPage(
								Pintr_13_QuestionsTopicLister.class
								, "GETTRNDQNS"
								, "trendingquestions"
								, hashtag
								, 0
								, 0
								, 0
								, hashtag_id
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
	    AutoRegOnBoard.putExtra("QuestionTitle", QuestionTitle);
	    AutoRegOnBoard.putExtra("JSONQuestionListName", JSONQuestionListName);
	    AutoRegOnBoard.putExtra("QuestionMakerID", QuestionMakerID);
	    AutoRegOnBoard.putExtra("AnswererID", AnswererID);
	    AutoRegOnBoard.putExtra("QuestionID", QuestionID);
	    AutoRegOnBoard.putExtra("hashtag_id", hashtag_id);
	    AutoRegOnBoard.putExtra("hashtag", hashtag);
	    startActivity(AutoRegOnBoard);	
	}
	
	//POPUP IF A SECTION IS EMPTY
	public void emptySectionPopup(String MsgString){

			Context context = this;
		
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

			alertDialogBuilder.setTitle("No questions here...");

			// set dialog message
			alertDialogBuilder
			.setMessage(MsgString)
			.setCancelable(true)
			.setNegativeButton("Ok",new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();	
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
    }

}