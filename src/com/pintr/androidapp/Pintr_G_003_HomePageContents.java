package com.pintr.androidapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class Pintr_G_003_HomePageContents  extends AsyncTask<String, Void, String> {

	final Pintr_G_002_DBQuery dbq;
	final Pintr_G_010_QuestionsDBMSManager rDB = new Pintr_G_010_QuestionsDBMSManager();
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	public Pintr_03_entry_page p03;
	
	HashMap<Integer, HashMap<String, String>> RecomQnAHM ;
	HashMap<Integer, HashMap<String, String>> RecentQnAHM ;
	HashMap<Integer, HashMap<String, String>> unreadMsgHM ;
	HashMap<Integer, HashMap<String, String>> eventInvitesHM ;

	Context context ;
	
	public Pintr_G_003_HomePageContents(Context contextIn){
		dbq = new Pintr_G_002_DBQuery(contextIn);
		context = contextIn;
	}
	
	public void loadDataIntoHashMaps(){
		
		ArrayList<String> Creds = dbq.UserCreds();
		final String email  = Creds.get(0).toString();
		final String password = Creds.get(1).toString();
		final String pintr_uid = Creds.get(2).toString();

		RecomQnAHM = parseQnAJsons("RECOMMENDED", "recommendedquestionlist", email, password);
		RecentQnAHM = parseQnAJsons("RECENT", "recentquestionlist", email, password);
		unreadMsgHM =parseUnreadMsgs();
		eventInvitesHM = parseEvents(pintr_uid);
	}
	
	
	
	public HashMap<Integer, HashMap<String, String>> parseQnAJsons( String type
																	, String function
																	, String email
																	, String password
																	){
		Log.d("EM/PW", email + "/" + password);
		String inputJSONString = rDB.JSONQuestionGetter( 5,email,password
														,type,"",0
														,0,"",0);
		Log.d("inputJSONString", inputJSONString);
		Log.d("PARSING QNA TYPE:", function);
		HashMap<Integer, HashMap<String, String>> allValuesMap = new HashMap<Integer, HashMap<String, String>> ();
		if(!inputJSONString.equals("EMPTYQUERY")){

			JSONArray inputJSONArray = jsh.JSON_Parser(inputJSONString ,function);

			int arrayLength = inputJSONArray.length();

			for(int i = 0; i < arrayLength; i++){
				HashMap<String, String> valuesReturned = new HashMap<String, String>();
				try {
					Log.d("QU_ID STORED", inputJSONArray.getJSONObject(i).getString("question_id"));
					valuesReturned.put("question_id", inputJSONArray.getJSONObject(i).getString("question_id"));
					valuesReturned.put("user_made_question_id", inputJSONArray.getJSONObject(i).getString("user_made_question_id"));
					valuesReturned.put("question_text", inputJSONArray.getJSONObject(i).getString("question_text"));
					valuesReturned.put("correspondent_id", inputJSONArray.getJSONObject(i).getString("correspondent_id"));
					valuesReturned.put("last_comms_date", inputJSONArray.getJSONObject(i).getString("last_comms_date"));
					valuesReturned.put("hashtag_id", inputJSONArray.getJSONObject(i).getString("hashtag_id"));
					valuesReturned.put("hashtag", inputJSONArray.getJSONObject(i).getString("hashtag"));
					} 
		        catch (JSONException e) {e.printStackTrace();}
		        
				if(type.equals("RECENT")){
			        try {
			        	valuesReturned.put("correspondent_read_question", inputJSONArray.getJSONObject(i).getString("correspondent_read_question"));
			        	valuesReturned.put("handler", inputJSONArray.getJSONObject(i).getString("handler"));
			        	valuesReturned.put("correspondent_read_question", inputJSONArray.getJSONObject(i).getString("correspondent_read_question"));
			        	Log.d("handler", inputJSONArray.getJSONObject(i).getString("handler"));
						} 
			        catch (JSONException e) {e.printStackTrace();}	          
				}
				
				allValuesMap.put(i, valuesReturned);
			}
	
		}
		return allValuesMap;
	}
	
	
	public HashMap<Integer, HashMap<String, String>> parseUnreadMsgs(){
		
		HashMap<Integer, HashMap<String, String>> allValuesMap = new HashMap<Integer, HashMap<String, String>> ();
		
		//GET UNREAD CONVO INFO FROM SQLITE
		ArrayList<Integer> unreadConvoIds = new ArrayList<Integer> ();
		unreadConvoIds  = dbq.unreadMsgConvoList();
		int unreadConvoCount = unreadConvoIds.size();
		Log.d("unreadConvoCount", "->" + unreadConvoCount);
		
		for (int i = 0; i < unreadConvoCount ; i++){

			String JSONString = dbq.readMessagesTable("READONECONVOSUMMARY", unreadConvoIds.get(i));
			JSONArray results = jsh.JSON_Parser(JSONString  
												, "msgs_results"
												);
			Log.d("CONVOSUMMARY", JSONString );			
			try {
				HashMap<String, String> valuesReturned = new HashMap<String, String>();
				valuesReturned.put("conversation_id",results.getJSONObject(i).getString("conversation_id"));
				valuesReturned.put("last_message",results.getJSONObject(i).getString("last_message") );
				valuesReturned.put("max_dttm_sent",results.getJSONObject(i).getString("max_dttm_sent") );
				valuesReturned.put("last_messager_handle",results.getJSONObject(i).getString("last_messager_handle") );
				allValuesMap.put(i, valuesReturned);
				} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return allValuesMap;
	}

		
	public HashMap<Integer, HashMap<String, String>> parseEvents(String pintr_uid ){

		HashMap<Integer, HashMap<String, String>> allValuesMap = new HashMap<Integer, HashMap<String, String>> ();
		
		String whereClauses =	  "  AND  l.invite_status = 'INVT' " 
								+ "  AND  r.participant_attending = ''  "
								;
		ArrayList<String> yourEventsArLi = dbq.yourEventsSummaryReader( "#", whereClauses, pintr_uid );
		Log.d("EVENTS CT", "->" + Integer.toString(yourEventsArLi.size()));
		
		//GET EVENT DATA ONE BY ONE
		for(int i =0; i < yourEventsArLi.size(); i++){
			//EVENT DATA READ:
			Log.d("EVENT ENTRY:",yourEventsArLi.get(i).toString());
			HashMap<String, String> valuesReturned = new HashMap<String, String>();
			String [] yourEventsCSV = yourEventsArLi.get(i).toString().split("#");
			valuesReturned.put("Event_id", yourEventsCSV[0]);
			valuesReturned.put("handler",  yourEventsCSV[2]);
			valuesReturned.put("Event_DtTm",  yourEventsCSV[3]);
			valuesReturned.put("event_type",  yourEventsCSV[4]);
			valuesReturned.put("type_variant",  yourEventsCSV[5 ]);
			valuesReturned.put("event_title",  yourEventsCSV[6]);
			valuesReturned.put("venue_name",  yourEventsCSV[7]);
			valuesReturned.put("venue_address_text",  yourEventsCSV[8]);
			valuesReturned.put("chg_evt_dttm",  yourEventsCSV[9]);
			valuesReturned.put("hashtags", yourEventsCSV[10]);
			valuesReturned.put("venue_yelp_id", yourEventsCSV[11]);
			valuesReturned.put("venue_yelp_url", yourEventsCSV[12]);
			valuesReturned.put("invite_status", yourEventsCSV[13]);
			valuesReturned.put("event_description", yourEventsCSV[14]);
			valuesReturned.put("participant_attending", yourEventsCSV[15]);
			valuesReturned.put("participant_event_status", yourEventsCSV[16]);
			allValuesMap.put(i, valuesReturned);
		}   	
			
		return allValuesMap;
	}
		
	
	

	@Override
	protected String doInBackground(String... params) {
		loadDataIntoHashMaps();
		return null;
	}

	
    @Override
    protected void onPostExecute(String msg) {

    	p03.processFinish(RecomQnAHM, RecentQnAHM, unreadMsgHM, eventInvitesHM);
    }

    
	public void createMessageSections(	HashMap<String, String> unreadMsg 
										,LinearLayout displayArea 
										,final String email
										,final String password
										){
		
		//GET DATA
		String messageRead = "false";
		final int ConversationID = Integer.parseInt(unreadMsg.get("conversation_id"));
		String participantList = unreadMsg.get("last_messager_handle");
		String DisplayMessage = unreadMsg.get("last_message");
		String last_comms_dateFmtd = unreadMsg.get("max_dttm_sent");
		
		//ADD HORIZONTAL LAYOUT
	    LinearLayout LL= new LinearLayout (context);
	    //ADD TITLE
	    TextView paritcipantsTV = new TextView(context);
	    TextView messageTV = new TextView(context);
	    TextView dateTV = new TextView(context);
	   
	    LL.setOrientation(LinearLayout.VERTICAL);
	    LinearLayout.LayoutParams lp1;
        lp1 = new LinearLayout.LayoutParams(
        		LayoutParams.MATCH_PARENT,
        		LayoutParams.WRAP_CONTENT);
	    LL.setPadding (10,10,10,10);
	    lp1.setMargins(0, 0, 0, 20);
	    
	    LL.setLayoutParams(lp1);
	    LL.setBackgroundResource(R.drawable.already_read_message_bar);
	    paritcipantsTV.setTextColor(context.getResources().getColor(R.color.CharcoalGreyBckGd));
    	messageTV.setTextColor(context.getResources().getColor(R.color.CharcoalGreyBckGd));
    	dateTV.setTextColor(context.getResources().getColor(R.color.CharcoalGreyBckGd));
	    if(messageRead.equals("false")){
	    	LL.setBackgroundResource(R.drawable.notyet_read_message_bar);
	    	messageTV.setTextColor(context.getResources().getColor(R.color.TextColor));
	    	dateTV.setTextColor(context.getResources().getColor(R.color.TextColor));
	    }
	    displayArea.addView(LL, lp1);
	    
	    LL.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				dbq.updateMessageRowAsRead(ConversationID);
				
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
				Date nowDt = Calendar.getInstance().getTime();
				String nowDtFmtd = simpleDateFormat.format(nowDt);
			
				final Pintr_MSG_001_DownloadMesssageHistory dlmh = new Pintr_MSG_001_DownloadMesssageHistory();
				dlmh.sendReadTimeToServer( context
											, ConversationID
								            ,  email
								            ,  password
								            , nowDtFmtd
											);
				gotoNextPage(	Pintr_20_MessageConversationTool.class
								, ConversationID
								);		
			}
	    });

	    RelativeLayout RL= new RelativeLayout (context);
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
	
	

	//GET EVENTS INTO DISPLAY
	public void displayEvents(	HashMap<String, String> eventInvites
								, LinearLayout eventItemsDisplayArea
								, int i 
								, ArrayList<String> EventTypesArray
								){
		Pintr_22_EventsOverview p22 = new Pintr_22_EventsOverview();

		String handler = eventInvites.get("handler");
		String Event_DtTm = eventInvites.get("Event_DtTm");
		String event_type = eventInvites.get("event_type");
		String event_title = eventInvites.get("event_title");
		String venue_name = eventInvites.get("venue_name");
		String venue_address_text = eventInvites.get("venue_address_text");
		String participant_attending  = eventInvites.get("participant_attending");
			
		//ADD THE GENERIC EVENT SUMMARY VIEW
		LinearLayout linearLayout
			= (LinearLayout) View.inflate(context,	R.layout.pintr_g_95_event_summary, null);
		eventItemsDisplayArea.addView(linearLayout);
		
		//CHANGE THE SUMMARY VIEW IDS AND ADD THE INFORMATION
		LinearLayout eventSummaryView = (LinearLayout)linearLayout.findViewById(R.id.eventSummaryView);
		int eventSummaryViewNuID = R.id.eventSummaryView * 100 +i ;
		eventSummaryView.setId(eventSummaryViewNuID);
		
		TextView eventTitle = (TextView )linearLayout.findViewById(R.id.eventTitle);
		int eventTitleNuID = R.id.eventTitle * 100 +i ;
		eventTitle.setId(eventTitleNuID);
		eventTitle.setText(event_title);
		
		TextView eventCreator = (TextView )linearLayout.findViewById(R.id.eventCreator);
		int eventCreatorNuID = R.id.eventCreator * 100 +i ;
		eventCreator.setId(eventCreatorNuID);
		eventCreator.setText(handler);
		
		TextView eventType = (TextView )linearLayout.findViewById(R.id.eventType);
		int eventTypeNuID = R.id.eventType * 100 +i ;
		eventType.setId(eventTypeNuID);
		eventType.setText(EventTypesArray.get(Integer.parseInt(event_type)));
		
		TextView eventDate = (TextView )linearLayout.findViewById(R.id.eventDate);
		int eventDateNuID = R.id.eventDate * 100 +i ;
		eventDate.setId(eventDateNuID);
		eventDate.setText(p22.dateLabelMaker(Event_DtTm));
		
		TextView eventLocation = (TextView )linearLayout.findViewById(R.id.eventLocation);
		int eventLocationNuID = R.id.eventLocation * 100 +i ;
		eventLocation.setId(eventLocationNuID);
		eventLocation.setText(venue_name + " - " + venue_address_text);

		ImageView eventAttend = (ImageView )linearLayout.findViewById(R.id.user_attend);
		int eventAttendNuID = R.id.user_attend * 100 +i ;
		eventAttend.setId(eventAttendNuID);
		if(participant_attending.equals("1"))
			eventAttend.setImageResource(R.drawable.green_tick_rs);
		else if(participant_attending.equals("0"))
			eventAttend.setImageResource(R.drawable.red_x);
		
		
		final HashMap<String,String> EventDetailsMapF = eventInvites;
		
		//ADD ONCLICK LISTENER TO VIEW FULL INDIVIDUAL EVENT
		eventSummaryView.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

					Intent MenuIntent = new Intent(context, Pintr_23_EventMaker.class);
				    MenuIntent.putExtra("Origin", "EXISTINGEVENT");
				    MenuIntent.putExtra("EventDetailsMapF", EventDetailsMapF);
				    context.startActivity(MenuIntent);
			}
		});	
	}

	
	public void writeQuestionLists( HashMap<String, String> QuestionInfo
								    ,LinearLayout masterLinLayout
								    ,final String function
								    ){

		Log.d("function", function);
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
		
		QuestionID_t = Integer.parseInt(QuestionInfo.get("question_id"));
		question_maker_id_t = Integer.parseInt(QuestionInfo.get("user_made_question_id"));
    	question_text_t = QuestionInfo.get("question_text");
		correspondent_id_t = Integer.parseInt(QuestionInfo.get("correspondent_id"));
		last_comms_date = QuestionInfo.get("last_comms_date");
		hashtag_id_t = Integer.parseInt(QuestionInfo.get("hashtag_id"));
		hashtag_t = QuestionInfo.get("hashtag");
		
		if(function.equals("RECENT")){			
        	handler = QuestionInfo.get("handler");
			correspondent_read_question = QuestionInfo.get("correspondent_read_question");
			Log.d("RECENT", "READ SPECIFIC ITEMS");
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
		
		if(function.equals("TRENDING")==false)
			try {
				 last_comms_dateLong = simpleDateFormat.parse(last_comms_date).getTime();
				 last_comms_dateFmtd = df.format(last_comms_dateLong);
			} catch (ParseException e) {e.printStackTrace();}
		

		final String hashtag = hashtag_t;
		
		if(function.equals("RECENT"))
			BubbleText =   question_text + " \n " + last_comms_dateFmtd   ;
		
		else if(function.equals("RECOMMENDED"))
			BubbleText = question_text  ;
		

        //ADD HORIZONTAL LAYOUT
	    LinearLayout RL= new LinearLayout (context);
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
	    TextView valueTV = new TextView(context);
	    TextView valueTVQuestion = new TextView(context);
	    TextView valueTVHashtag= new TextView(context);
	    valueTV.setTextColor(context.getResources().getColor(R.color.MessageBubbleTextColor));
	    valueTVQuestion.setTextColor(context.getResources().getColor(R.color.MessageBubbleTextColor));
	    valueTVHashtag.setTextColor(context.getResources().getColor(R.color.MessageBubbleTextColor));
        valueTV.setText(BubbleText);
        
        if(function.equals("RECENT"))
        	Log.d("handler", "" + handler);
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
				//THREADS CLICKTHROUGH
				gotoMsg(
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
		});	
		
	}
	public void gotoNextPage(
			Class<?> classIn
			, int ConversationID
			){

		//STATE THAT THE INTEREST LIST SHOULD START AT THE TOP
	    Intent AutoRegOnBoard = new Intent(context, classIn);
	    AutoRegOnBoard.putExtra("ConversationID", ConversationID);
	    context.startActivity(AutoRegOnBoard);	
	}

	public void gotoMsg(
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
	    Intent AutoRegOnBoard = new Intent(context, classIn);
	    AutoRegOnBoard.putExtra("QuestionType", QuestionType);
	    AutoRegOnBoard.putExtra("QuestionTitle", QuestionTitle);
	    AutoRegOnBoard.putExtra("JSONQuestionListName", JSONQuestionListName);
	    AutoRegOnBoard.putExtra("QuestionMakerID", QuestionMakerID);
	    AutoRegOnBoard.putExtra("AnswererID", AnswererID);
	    AutoRegOnBoard.putExtra("QuestionID", QuestionID);
	    AutoRegOnBoard.putExtra("hashtag_id", hashtag_id);
	    AutoRegOnBoard.putExtra("hashtag", hashtag);
	    context.startActivity(AutoRegOnBoard);	
	}
}
