package com.pintr.androidapp;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;


public class Pintr_17_ProfileQuestionsTopicLister extends Activity   {

	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_015_GeneralTextBubbleMaker bm = new Pintr_G_015_GeneralTextBubbleMaker();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_014_ProfilerDBMSManager rDB = new Pintr_G_014_ProfilerDBMSManager();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_13_questions_topic_lister);
		//MENU MANAGER:
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		LinearLayout ButtonDisplayArea = (LinearLayout)findViewById(R.id.ButtonDisplayArea);
		ButtonDisplayArea.setVisibility(View.GONE);
		
		
		Bundle extras = getIntent().getExtras();
		String QuestionType  = extras.getString("QuestionType");
		int pintr_user_id  = extras.getInt("QuestionMakerID");
		String handle  = extras.getString("QuestionTitle");

		ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();

		String JSONstrExtractor = "";
		String dataTag = "";
		String title = "";
		
		if(QuestionType.equals("MOSTPOP")){
			JSONstrExtractor  = rDB.mostpopAnsweredQuestions(email, password, pintr_user_id, 100, "");
			dataTag = "handler_top_question_data";
			title=  handle + " - Most popular questions" ;
		}
		else if(QuestionType.equals("MOSTREC")){
			JSONstrExtractor  = rDB.lastAnsweredQuestions(email, password, pintr_user_id, 100, "");
			dataTag = "handler_last_question_data";
			title=  handle + " - Most recent questions" ;
			}

		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
	    PageTitle.setText(title);
		
	    if(!JSONstrExtractor.equals("CONNECTIONTIMEOUT")){
	    	
			JSONArray QuestionsJSONArray = jsh.JSON_Parser(
					JSONstrExtractor
					, dataTag
					);
			
			int ArLen = QuestionsJSONArray .length();   
			LinearLayout displayLL = (LinearLayout )findViewById(R.id.QuestionActivitiesDisplayView);

			for(int i = 0; i < ArLen; i++){
		        try {
		        	final int question_id = Integer.parseInt(QuestionsJSONArray.getJSONObject(i).getString("question_id"));
		        	final int user_created_id = Integer.parseInt(QuestionsJSONArray.getJSONObject(i).getString("user_created_id"));
		        	final int user_answered_id = Integer.parseInt(QuestionsJSONArray.getJSONObject(i).getString("user_answered_id"));
		        	final String question_text = QuestionsJSONArray.getJSONObject(i).getString("question_text");
		        	final String viewer_involved = QuestionsJSONArray.getJSONObject(i).getString("viewer_involved");
		        	String hashtagIn = QuestionsJSONArray.getJSONObject(i).getString("hashtag");
					
		        	if (hashtagIn == null)
		        		hashtagIn="";
		        	final String hashtag = hashtagIn;
		        	
		        	LinearLayout mostpopRL  = 
		        			bm.bubbleTextMaker(	displayLL
												,this
												,viewer_involved
												,QuestionsJSONArray.getJSONObject(i).getString("question_text")
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
	

	//MENU MANAGER:

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

