package com.pintr.androidapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;


public class Pintr_19_MessagesOverview 	extends Activity
										implements 	Pintr_PL_03_LoginLoadUserListener { 


	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	final Pintr_MSG_001_DownloadMesssageHistory dlmh = new Pintr_MSG_001_DownloadMesssageHistory();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	Context context = this;
	String email, password;
	int yourUserId  ;
	
	Boolean networkAvailable ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_19_messages_overview);

		TextView PageTitle = (TextView )findViewById(R.id.textPageHeader);
		PageTitle.setText("Messages");
		
		ProgressBar pbspnner  = new ProgressBar(this);;
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		LinearLayout.LayoutParams iRL 
			= new LinearLayout.LayoutParams(50, 50);
		spinnerRL.addView(pbspnner,iRL);

		LinearLayout ComposeMessageView = (LinearLayout)findViewById(R.id.ComposeMessageView);
		
		ComposeMessageView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				gotoNextPage(
						Pintr_20_MessageConversationTool.class
						, 0
						);
			}
	    });
		
		
		ArrayList<String> Creds = dbq.UserCreds();
		email = Creds.get(0).toString();
		password = Creds.get(1).toString();
		yourUserId  = Integer.parseInt(Creds.get(2).toString());
		
		TextView textView1 = (TextView)findViewById(R.id.textView1);
		textView1.setText("Loading...");
		networkAvailable = nw.haveNetworkConnection(this);

		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		//UPDATE SQLITE DB WITH CONVOS
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		} else{
			final Pintr_A_002_MsgPageLoaderAsync loadDtlsTask 
				= new Pintr_A_002_MsgPageLoaderAsync (this);
	        loadDtlsTask.execute();
	        loadDtlsTask.p19 = this;
		}
		
		textView1.setText("SQLITE LOADED");
		textView1.setText("");
		
		
		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		//****  END MENU MANAGER  ******
		
	}


	@Override
	public void processFinish(String output) {
		
		String JSONString = dbq.readMessagesTable("CONVOSUMMARY", 0);
		JSONArray results = jsh.JSON_Parser(JSONString  
											, "msgs_results"
											);
		Log.d("CONVOSUMMARY", JSONString );
		//GET UNREAD CONVO INFO FROM SQLITE
		ArrayList<Integer> unreadConvoIds = new ArrayList<Integer> ();
		unreadConvoIds  = dbq.unreadMsgConvoList();
		
		//READ MESSAGE SUMMARY INTO PAGE
		int msgTopicsCount = results.length();
		int convoIDcurrent  = 0, convoIDlast = 0,  currentParticipant=0;
		ArrayList<Integer> conversationIDs = new ArrayList<Integer>(); 
		ArrayList<String> lastMessages = new ArrayList<String>(); 
		ArrayList<String> lastCommsDate = new ArrayList<String>(); 
		ArrayList<String> lastHandler = new ArrayList<String>(); 
		ArrayList<String> participants = new ArrayList<String>();
		ArrayList<String> allRead= new ArrayList<String>();
		String ParticipantList = "";
			
		
		//BUILD A LIST OF CONVERSATION OVERVIEWS
		for (int i = 0; i < msgTopicsCount ; i++ ){
			
			try {
				convoIDcurrent = results.getJSONObject(i).getInt("conversation_id");
				currentParticipant = results.getJSONObject(i).getInt("pintr_user_id");
				
				if (convoIDcurrent != convoIDlast){
					conversationIDs.add(convoIDcurrent );
					if(unreadConvoIds.contains(convoIDcurrent)){
						for (int unreadConvoId : unreadConvoIds){
						    Log.d("unreadConvoId: ", Integer.toString(unreadConvoId));
						}
						allRead.add("false");
						Log.d("MSGREAD TR/FL", "ADDED:  ***FALSE****");
					}
					else {
						allRead.add("true");
						Log.d("MSGREAD TR/FL", "ADDED:  ***TRUE****");
					}
					lastMessages.add(results.getJSONObject(i).getString("last_message") );
					lastCommsDate.add(results.getJSONObject(i).getString("max_dttm_sent") );
					lastHandler.add(results.getJSONObject(i).getString("last_messager_handle") );
					if(i > 0){
						participants.add(ParticipantList);
						ParticipantList = "";
					}
					
					if(currentParticipant != yourUserId){
						ParticipantList = results.getJSONObject(i).getString("participant");
					} else ParticipantList = "";
					Log.d("MSG SUMMARRY - ParticipantList:", ParticipantList);
				} 
				else if(currentParticipant != yourUserId){
					
					if(ParticipantList.length() > 0) ParticipantList = ParticipantList 
							+ ", ";
					ParticipantList = ParticipantList 
										+ results.getJSONObject(i).getString("participant")
										;
				}

				if ( i  == msgTopicsCount - 1){
					participants.add(ParticipantList);
					Log.d("MSG SUMMARRY - ParticipantList:", ParticipantList);
				}
				
			} catch (JSONException e) {e.printStackTrace();}
		
			convoIDlast = convoIDcurrent; 
		}
		
		
		for (int i=0; i < conversationIDs.size(); i++){
			
			int convoID = Integer.parseInt(conversationIDs.get(i).toString()); 
			String latestMessage = lastMessages.get(i).toString(); 
			String last_comms_date = lastCommsDate.get(i).toString() ; 
			String lastCommsHandler = lastHandler.get(i).toString() ; 
			String participantList = participants.get(i).toString() ;
			String messageRead = allRead.get(i).toString() ;

			String showMessage = latestMessage;
			if (latestMessage.length() > 10)
				showMessage = latestMessage.substring(0,10) + "...";
			
			else if (latestMessage.equals(null))
				showMessage = "";
			
				
			String DisplayMessage = lastCommsHandler + ":  " + showMessage;

			if (participantList.length() > 24)
				participantList = participantList.substring(0,24) + "...";	
			
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
			String last_comms_dateFmtd = "";
			Date last_comms_dateDate = null;
			Date nowDt = Calendar.getInstance().getTime();
			
			try {
				 last_comms_dateDate = simpleDateFormat.parse(last_comms_date);
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
			createSections(
					 participantList
					, DisplayMessage
					, last_comms_dateFmtd
					, convoID
					, messageRead 
					);		
		}
		
		//REMOVE LOAD SPINNER
		LinearLayout spinnerRL = (LinearLayout)findViewById(R.id.loadingSpinnerArea);
		spinnerRL.removeAllViews();
	}  
	
	
	public void createSections(
						String participantList
						,String DisplayMessage
						,String last_comms_dateFmtd
						,final int ConversationID
						,String messageRead
						){
		LinearLayout displayArea = (LinearLayout)findViewById(R.id.ConversationsView);
		
		//ADD HORIZONTAL LAYOUT
	    LinearLayout LL= new LinearLayout (this);
	    //ADD TITLE
	    TextView paritcipantsTV = new TextView(this);
	    TextView messageTV = new TextView(this);
	    TextView dateTV = new TextView(this);
	   
	    LL.setOrientation(LinearLayout.VERTICAL);
	    LinearLayout.LayoutParams lp1;
        lp1 = new LinearLayout.LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT);
	    LL.setPadding (10,10,10,10);
	    lp1.setMargins(0, 0, 0, 20);
	    
	    LL.setLayoutParams(lp1);
	    LL.setBackgroundResource(R.drawable.already_read_message_bar);
	    paritcipantsTV.setTextColor(getResources().getColor(R.color.CharcoalGreyBckGd));
    	messageTV.setTextColor(getResources().getColor(R.color.CharcoalGreyBckGd));
    	dateTV.setTextColor(getResources().getColor(R.color.CharcoalGreyBckGd));
	    if(messageRead.equals("false")){
	    	LL.setBackgroundResource(R.drawable.notyet_read_message_bar);
	    	messageTV.setTextColor(getResources().getColor(R.color.TextColor));
	    	dateTV.setTextColor(getResources().getColor(R.color.TextColor));
	    }
	    displayArea.addView(LL, lp1);
	    
	    LL.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				dbq.updateMessageRowAsRead(ConversationID);
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
				Date nowDt = Calendar.getInstance().getTime();
				String nowDtFmtd = simpleDateFormat.format(nowDt);
			
				dlmh.sendReadTimeToServer( context
											, ConversationID
								            ,  email
								            ,  password
								            , nowDtFmtd
											);
				gotoNextPage(
						Pintr_20_MessageConversationTool.class
						, ConversationID
						);		
			}
	    });

	    RelativeLayout RL= new RelativeLayout (this);
	    RelativeLayout.LayoutParams lp;  
	    lp = new RelativeLayout.LayoutParams(
		    	LayoutParams.MATCH_PARENT,
		    	LayoutParams.MATCH_PARENT);
	    
	    paritcipantsTV.setText(participantList);
	    messageTV.setText(DisplayMessage);
	    dateTV.setText(last_comms_dateFmtd);

	    paritcipantsTV.setTypeface(null, Typeface.BOLD);
	    paritcipantsTV.setPadding (0,0,0,0);
        LL.addView(paritcipantsTV);
	    LL.addView(RL, lp);

	    RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    lay.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
	    RL.addView(messageTV, lay);
        
	    RelativeLayout.LayoutParams lay2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    lay2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
	    RL.addView(dateTV, lay2);
	    
	    
	}
	
		
	public void gotoNextPage(
			Class<?> classIn
			, int ConversationID
			){

		//STATE THAT THE INTEREST LIST SHOULD START AT THE TOP
	    Intent AutoRegOnBoard = new Intent(this, classIn);
	    AutoRegOnBoard.putExtra("ConversationID", ConversationID);
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