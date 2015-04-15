package com.pintr.androidapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class Pintr_20_MessageConversationTool extends Activity   {

	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	
	int pintr_uid = 0, globalParticipantsCount = 0;
	LinearLayout spinnerRL;
	ProgressBar pbspnner;
	ImageView tick;
	int firstKey = 0, convoIDGlobal;
	String email, password, participantCSV="", Status;
	Map<Integer, String> participantsMap = new HashMap<Integer, String>();
	Map<Integer, Integer> participantsMapPositionMap = new HashMap<Integer, Integer>();
	Map<Integer, String> friendsMap = new HashMap<Integer, String>();
	Iterator<Integer> myVeryOwnIterator = participantsMap.keySet().iterator();
	RelativeLayout messagesRecipientsDisplay ;
	LinearLayout DiscussionDisplayView, friendsPickerSpinner,friendsPickerSection ;
	EditText MessageEntryField ;
	Button sendFailBtn;
	final Pintr_MSG_001_DownloadMesssageHistory sendFn = new Pintr_MSG_001_DownloadMesssageHistory();
    final Context context=this;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_20_conversation_tool);
		
		Button SubmitButton = (Button)findViewById(R.id.SubmitButton);
		Button AddRecipientsButton = (Button)findViewById(R.id.AddRecipientsButton);
		messagesRecipientsDisplay = (RelativeLayout)findViewById(R.id.messagesRecipientsDisplay);
		DiscussionDisplayView = (LinearLayout)findViewById(R.id.DiscussionDisplayView);
		friendsPickerSection = (LinearLayout)findViewById(R.id.friendsPickerSection);
		MessageEntryField = (EditText)findViewById(R.id.MessageEntryField);
		friendsPickerSpinner = (LinearLayout)findViewById(R.id.friendsPickerSpinner);

		friendsPickerSpinner.setVisibility(View.GONE);
		
		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		//******  END MENU MANAGER ******

		final ScrollView scrollview = ((ScrollView) findViewById(R.id.messagesDisplaySection));
	    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		//GET THE USER DETAILS FROM THE ONBOARD DB
		ArrayList<String> Creds = dbq.UserCreds();
		email = Creds.get(0).toString();
		password = Creds.get(1).toString();
		pintr_uid = Integer.parseInt(Creds.get(2).toString());
		
		
		Bundle extras = getIntent().getExtras();
		final int convoID = extras.getInt("ConversationID");
		Log.d("COINVOID@START", "->" + convoID + ";");
		convoIDGlobal = convoID;
		Log.d("CONVOID PASSED", "->" + Integer.toString(convoIDGlobal));
		final String messageSoFar = extras.getString ("message");
		final HashMap<Integer, String> participants 
				= (HashMap<Integer, String>) getIntent().getSerializableExtra("participants");
		MessageEntryField.setText(messageSoFar);
		
		Log.d("ConversationID", Integer.toString(convoID));
		
		//EXISTING CONVERSATION:
	    if(convoID != 0){
	    	readExistingConversation( convoID);
	    }
	    
	   //NEW CONVERSATION:
	    else if(convoID == 0){

			int lastUid = 0, lastUidButOne = 0, i = 0;
			if(participants != null){

				StringBuilder sb = new StringBuilder();
				participantsMap = participants;
				globalParticipantsCount  = participantsMap.size();
				myVeryOwnIterator = participantsMap.keySet().iterator();
				
		    	while(myVeryOwnIterator.hasNext()) {
					Integer key=myVeryOwnIterator.next();
					
					if(key != pintr_uid){
						displayParticipants(key
									, lastUid
									, lastUidButOne
									, participantsMap.get(key)
									, participantsMap.size()
									);
						lastUid = key;
						lastUidButOne = lastUid ;
						
						if(i > 0)sb.append(",");
						else firstKey = key;
						
						sb.append(Integer.toString(key));
						i++;	
					}
				}	
		    	participantCSV = sb.toString(); 
			}
	    }

	    final Class<?> pickFriendsClass = Pintr_21_MessageRecipientPicker.class;
	    
	   //ADD RECIPIENTS BUTTON
	    AddRecipientsButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

				String message = MessageEntryField.getText().toString();
				Intent AutoRegOnBoard = new Intent(context, pickFriendsClass);
			    AutoRegOnBoard.putExtra("ConversationID", convoID);
			    AutoRegOnBoard.putExtra("participants", participants);
			    AutoRegOnBoard.putExtra("message", message);
			    AutoRegOnBoard.putExtra("Origin", "MESSAGES");
			    startActivity(AutoRegOnBoard);	
			}
	    });	
	    
	    
	    //SEND MESSAGE BUTTON
	    SubmitButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	
				userMessageDispatch(convoID);
			}
	    });
	    
	    //SET KEYBOARD "ENTER" KEY TO SEND MSG
	    final EditText MessageEntryField = (EditText) findViewById(R.id.MessageEntryField);
	    MessageEntryField.setOnEditorActionListener(new TextView.OnEditorActionListener() { 
            @Override 
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { 
                    boolean handled = false; 
                    if (actionId == EditorInfo.IME_ACTION_GO) { 
                    	userMessageDispatch(convoID);
                        handled = true; 
                    } 
                    return handled; 
            } 
	    }); 
	    
	    //GOTO LAST MESSAGE IN LIST
	    scrollview.post(new Runnable() {
	        @Override
	        public void run() {
	            scrollview.fullScroll(View.FOCUS_DOWN);
	        }
	    });

	}
	
	public void userMessageDispatch(int convoID){

		//CHECK FOR EXISTING CONVERSATION WITH THE RECIPIENT
		int returnConvoid = 0;
		Log.d("CHECK4EXIST_CONV", "Curr convoid = " + convoID);
		if(  convoID == 0 ){
			if(globalParticipantsCount == 1){
				returnConvoid = dbq.checkForExistingConvo(firstKey, pintr_uid);	
				Log.d("LOOK@OB-DB", "Found existing convo in DB = " + returnConvoid);
			}
		}
		
		if(convoID != 0){
			
			Log.d("CONVO", "FROM INSIDE EXISTING CONVO");
			Status = "false";
			convoIDGlobal = convoID;
			Log.d("CONVOID/STATUS FOR MSG", "->" + Integer.toString(convoIDGlobal) + "/" + Status);
			sendMessage(convoIDGlobal);
			
		} else if(returnConvoid  != 0 ){
			
			Log.d("CONVO", "EXISTING CONVO");
			readExistingConversation( returnConvoid);
			Status = "false";
			convoIDGlobal = returnConvoid;
			Log.d("CONVOID/STATUS FOR MSG", "->" + Integer.toString(convoIDGlobal) + "/" + Status);
			sendMessage(convoIDGlobal);
			
		} else{

			Log.d("CONVO", "NEW CONVO");
			Status = "true";
			convoIDGlobal = convoID;
			Log.d("CONVOID/STATUS FOR MSG", "->" + Integer.toString(convoIDGlobal) + "/" + Status);
			sendMessage(convoIDGlobal);
		}
	}

	
	public RelativeLayout displayMessages( String participantList
										,final String DisplayMessage
										,String last_comms_dateFmtd
										,int  participantUID
										,Boolean newMsgJustSent
										,Boolean sentMessage
										){
		final LinearLayout displayArea = (LinearLayout)findViewById(R.id.DiscussionDisplayView);
		
		//ADD HORIZONTAL LAYOUT
		final LinearLayout LL= new LinearLayout (this);
		LL.setOrientation(LinearLayout.VERTICAL);		

		LinearLayout.LayoutParams lp1;
		lp1 = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
//		RelativeLayout.LayoutParams sRL;
		lp1.setMargins(0, 10, 100, 10);
		

		final RelativeLayout RL= new RelativeLayout (this);
		spinnerRL= new LinearLayout (this);
		RelativeLayout.LayoutParams lp;  
		lp = new RelativeLayout.LayoutParams(
			LayoutParams.MATCH_PARENT,
			LayoutParams.MATCH_PARENT);
		RL.setBackgroundResource(R.drawable.callout_yellow_grey_borders2);
		
		if(pintr_uid == participantUID){
			participantList="You";
			lp1.setMargins(100, 10, 0, 10);
			RL.setBackgroundResource(R.drawable.callout_white_grey_borders);
		}

		LL.setLayoutParams(lp1);
		displayArea.addView(LL, lp1);
		
		//ADD TITLE
		TextView paritcipantsTV = new TextView(this);
		TextView messageTV = new TextView(this);
		TextView dateTV = new TextView(this);

		paritcipantsTV.setText(participantList);
		messageTV.setText(DisplayMessage);
		
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		Date last_comms_dateDate = null;
		final Date nowDt = Calendar.getInstance().getTime();
		String last_comms_dateTransmit = last_comms_dateFmtd;
		
		try {
			 last_comms_dateDate = simpleDateFormat.parse(last_comms_dateFmtd);
		} catch (ParseException e) {e.printStackTrace();}

		Long timeDiff =  (nowDt.getTime() - last_comms_dateDate.getTime()) / 1000;
		
		if(timeDiff < 86400){
			DateFormat df = new SimpleDateFormat("hh:mm a",Locale.getDefault());
			last_comms_dateFmtd = df.format(last_comms_dateDate);
			
		} else if(timeDiff > 86400*365){
			DateFormat df = new SimpleDateFormat("dd LLLL yyyy ",Locale.getDefault());
			last_comms_dateFmtd = df.format(last_comms_dateDate);
							
		} else {
			DateFormat df = new SimpleDateFormat("dd LLLL",Locale.getDefault());
			last_comms_dateFmtd = df.format(last_comms_dateDate);
							
		}
		dateTV.setText(last_comms_dateFmtd);
		
		
		paritcipantsTV.setTypeface(null, Typeface.BOLD);
		LL.addView(paritcipantsTV);
		LL.addView(RL, lp);

		messageTV.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor) );
		dateTV.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor) );
		

		if(newMsgJustSent || sentMessage==false){
			newMessageBox(	LL
							, displayArea
							, RL
							, DisplayMessage
							, sentMessage
							, last_comms_dateTransmit
							);
			
		}
		
		RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lay.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		lay.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		lay.setMargins(0, 0, 0, 0);
		messageTV.setPadding (0,0,0,20);
		RL.addView(messageTV, lay);
		
		RelativeLayout.LayoutParams lay2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lay2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lay2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		RL.addView(dateTV, lay2);

		final ScrollView scrollview = ((ScrollView) findViewById(R.id.messagesDisplaySection));
		scrollview.fullScroll(View.FOCUS_DOWN);
		InputMethodManager imm = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(MessageEntryField.getWindowToken(), 0);
		return RL;
	}
	
	public void newMessageBox(	final LinearLayout LL
								, final LinearLayout displayArea
								, RelativeLayout RL
								, final String DisplayMessage
								, Boolean sentMessage
								, final String last_comms_dateFmtd
								){
		RelativeLayout.LayoutParams sRL 
				= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT
												, LayoutParams.WRAP_CONTENT);
		sRL.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		sRL.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		
		RL.setBackgroundResource(R.drawable.callout_white_pink_borders);
		spinnerRL.setOrientation(LinearLayout.HORIZONTAL);
		RL.addView(spinnerRL, sRL );
		
		LinearLayout.LayoutParams iRL 
			= new LinearLayout.LayoutParams(50
											, 50);
		
		pbspnner = new ProgressBar(this);
		tick = new ImageView(this);
		tick.setImageResource(R.drawable.green_tick_rs);
		sendFailBtn = new Button(this); 
		sendFailBtn.setBackgroundResource(R.drawable.resend_message_btn);
		spinnerRL.addView(pbspnner,iRL);
		spinnerRL.addView(tick,iRL );
		LinearLayout.LayoutParams fRL 
		= new LinearLayout.LayoutParams(80
										, 80);
		spinnerRL.addView(sendFailBtn, fRL);
		
		sendFailBtn.setVisibility(View.GONE);
		tick.setVisibility(View.GONE);
		
		if(sentMessage==false){
			sendFailBtn.setVisibility(View.VISIBLE);
			pbspnner.setVisibility(View.GONE);
				
		}
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		Date nowDt = Calendar.getInstance().getTime();
		final String nowTransmit = simpleDateFormat.format(nowDt);
		
		sendFailBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				
				displayArea.removeView(LL);
				
				RelativeLayout newRL = displayMessages("You"
														,DisplayMessage
														,nowTransmit
														,pintr_uid
														,true
														,true
														);
				tick.setVisibility(View.VISIBLE);

				int returnConvoid = 0;
				if(  convoIDGlobal  == 0 ){
					if(globalParticipantsCount  == 1){
						returnConvoid = dbq.checkForExistingConvo(firstKey, pintr_uid);	
					}
				}
				
				if(returnConvoid  != 0)Status = "true";
				else Status = "false";
				
				
				startAsyncTransmit( DisplayMessage
					                , convoIDGlobal
					                , newRL
						            , Status
						            , last_comms_dateFmtd
						            );
				tick.setVisibility(View.GONE);
						
			}
		});
	}
	
	
	
	public void displayParticipants(int uid
									, int lastUid
									, int lastUidButOne
									, String participantHandle
									, int partCount 
									){
		
		RelativeLayout.LayoutParams rlP;
		rlP = new RelativeLayout.LayoutParams(
							        LayoutParams.WRAP_CONTENT,
						        	LayoutParams.WRAP_CONTENT
						        	); 
		if(lastUid == 0) rlP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		else rlP.addRule(RelativeLayout.BELOW, lastUid);

		LinearLayout LL= new LinearLayout (this);
		LL.setOrientation(LinearLayout.VERTICAL);	
		LL.setId(uid);
		
		LL.setPadding (10,10,10,10);
		LL.setBackgroundResource(R.drawable.qna_background);
		
		LinearLayout.LayoutParams lp1;
		lp1 = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		lp1.setMargins(10,10,10,10);
		rlP.setMargins(10,10,10,10);

	          
		LL.setLayoutParams(lp1);
		messagesRecipientsDisplay.addView(LL, rlP);
		
		TextView paritcipantTV = new TextView(this);
		paritcipantTV.setText(participantHandle);
		paritcipantTV.setTypeface(null, Typeface.BOLD);
		paritcipantTV.setPadding (0,0,0,0);

		paritcipantTV.setTextColor(getResources().getColor(R.color.MessageBubbleTextColor) );
		
		LL.addView(paritcipantTV);
		Log.d("ADDED VIEW","participantHandle");
	}
	
	
	public void sendMessage(int convoID){
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		Date nowDt = Calendar.getInstance().getTime();
		String now = simpleDateFormat.format(nowDt);
		String message = MessageEntryField.getText().toString();

		//SEND MESSAGE TO SERVER
		if(participantsMap.size() >= 1){

			//DISPLAY MESSAGE 
			RelativeLayout neuMsgLo 
					= displayMessages( "You"
										,message
										,now
										,pintr_uid
										,true
										,true
										);
			startAsyncTransmit( message
				                , convoID
				                , neuMsgLo
					            , Status
					            , ""
					            );
			MessageEntryField.setText("");
		    
		} else 
			Toast.makeText(context
							,"Please pick a correspondant", 
	                		Toast.LENGTH_SHORT).show();	
	}
	
	
	public void readExistingConversation(int convoID){

		friendsPickerSection.setVisibility(View.GONE);
		String JSONString = dbq.readMessagesTable("READCONVO", convoID);
		JSONArray results = jsh.JSON_Parser( JSONString 
											, "msgs_results"
											);
		int msgsCount = results.length();
		
		int lastKey = 0, lastKeyButOne = 0, i=0;
		Log.d("EXISTING CONVO MAKER", JSONString);
		
		//DISPLAY MESSAGES
		for(i = 0; i < msgsCount; i++){
			try {
				String realReply = results.getJSONObject(i).getString("message_read");
				if(realReply.equals("false")){
					Boolean message_sent = Boolean.valueOf(results.getJSONObject(i).getString("message_sent"));
					displayMessages( results.getJSONObject(i).getString("last_messager_handle")
									,results.getJSONObject(i).getString("last_message")
									,results.getJSONObject(i).getString("max_dttm_sent")
									,results.getJSONObject(i).getInt("pintr_user_id")
									,false
									,message_sent
									);
				}
				participantsMap.put(results.getJSONObject(i).getInt("pintr_user_id")
									,results.getJSONObject(i).getString("last_messager_handle")
									);
				
			} catch (JSONException e){ e.printStackTrace();} 
		}
		globalParticipantsCount  = participantsMap.size();
		
		//DISPLAY PARTICIPANTS
		myVeryOwnIterator = participantsMap.keySet().iterator();
		i=0;
		while(myVeryOwnIterator.hasNext()) {
			
			Integer key=myVeryOwnIterator.next();
			
			if(key != pintr_uid){
				i++;
				displayParticipants(key, lastKey, lastKeyButOne, participantsMap.get(key), i);
			    lastKeyButOne = lastKey;
			    lastKey = key;
			    Log.d("NEXT CONVO PARTICIPANT", participantsMap.get(key));
			    if (i > 1) participantCSV = participantCSV + ",";
				participantCSV = participantCSV + Integer.toString(key);
				
			}
		}
	}
	
	
	public void startAsyncTransmit(	String message
									,int convoID
						            ,RelativeLayout  neuMsgLo
						            ,String status
						            ,String oldDate
						            ){

		Log.d("participantCSV", " = > " + participantCSV);
		Log.d("participantsMap count", " -> " + participantsMap.size());
	
		Pintr_NT_05_SendMessagesASYNC task 
				= new Pintr_NT_05_SendMessagesASYNC (context
									                , email
									                , password
													, convoID
									                , message
									                , status
									                , participantCSV
									                , pintr_uid
									                , participantsMap
									                , neuMsgLo 
									                , pbspnner
									                , tick
									    			, sendFailBtn
									    			, oldDate
									                );
		task.execute();
	}
	
	
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
		
//		final ToggleButton menuButton = (ToggleButton)findViewById(R.id.menuToggle);
//		menuButton.setOnCheckedChangeListener(bsm.menuCheckList);
//		
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