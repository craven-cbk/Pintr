package com.pintr.androidapp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Context;
import android.util.Log;



public class Pintr_MSG_001_DownloadMesssageHistory extends Activity{

	Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
	Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	
	
	public 	void writeDataFromServerToSQLite(Context context, String login_status){
		Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);

		ArrayList<String> Creds = dbq.UserCreds();
		String msg_ids = "";
		String convo_ids = "";

		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress = "http://www.pickmemycar.com/pintr/COM/get.php";
		String date_last_dl = dbq.msgsLastUpdatedDateRead();
		Log.d("READING MESSAGES ","-> date_last_dl:  " +  date_last_dl );

		if(date_last_dl.equals("null")
				|| date_last_dl.equals(null)
				|| date_last_dl.equals("")
				){
			date_last_dl = "2000/01/01 00:00:00.000";
		}
		
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("date_last_dl");

		inputValues.add(Creds.get(0).toString());
		inputValues.add(Creds.get(1).toString());
		inputValues.add(date_last_dl);
		
		int current_pintr_user_id = Integer.parseInt(Creds.get(2).toString());

		String returnedJSONString
					= rDB.getDBQuery(inputVariables
									,inputValues
									,queryAddress
									);
		Log.d("MSGS FROM DB", returnedJSONString);
		
		
		if(returnedJSONString.equals("CONNECTIONTIMEOUT"))
            Log.d("MSG_001 - writeDataFromServerToSQLite", "CONNECTIONTIMEOUT");
		
		else if(returnedJSONString.equals("EMPTYQUERY") == false){

			JSONArray convoJSON = jsh.JSON_Parser( returnedJSONString
											, "conversationsallcontents"
											);

			int jsonlength = convoJSON.length();
			for(int i = 0; i < jsonlength; i++){
		        try {
		        	int conversation_id = convoJSON.getJSONObject(i).getInt("conversation_id");
		        	String pintr_user_id = convoJSON.getJSONObject(i).getString("pintr_user_id");
		        	String message = convoJSON.getJSONObject(i).getString("message");
		        	String dttm_sent = convoJSON.getJSONObject(i).getString("dttm_sent");
		        	String replied_flag = convoJSON.getJSONObject(i).getString("recipient_notyetreplied_flag");
		        	String handler = convoJSON.getJSONObject(i).getString("handler");
		        	String msg_id = convoJSON.getJSONObject(i).getString("msg_id");
		        	msg_ids = msg_ids + msg_id;
		        	convo_ids  = convo_ids + conversation_id ;
		        	if(i < jsonlength-1){
		        		msg_ids = msg_ids + ",";
		        		convo_ids = convo_ids + ",";
		        	}
		        	Log.d("msg_ids ", msg_ids );
		        	Log.d("convo_ids ", convo_ids );

		        	if(replied_flag.equals("1")) replied_flag="true";
		        	else if(replied_flag.equals("0")) replied_flag="false";
		        	
		        	Boolean already_read = true;
		        	
		        	if (Integer.parseInt(pintr_user_id) != current_pintr_user_id){
		        		already_read = false;
		        	}
					
					dbq.messagesUpdate( conversation_id
										, pintr_user_id
										,  message
										,  dttm_sent
										,  Boolean.valueOf(replied_flag)
										,  handler
										,  already_read
										,  "true"
										);
					} 
		        catch (JSONException e) {e.printStackTrace();}
			}
			String transmitReponse = transmitDownloadOk(msg_ids
														, convo_ids
														, Creds.get(0).toString()
														, Creds.get(1).toString()
														, login_status
														);
			Log.d("transmitReponse",transmitReponse );
			
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
			Date nowDt = Calendar.getInstance().getTime();
			String now = simpleDateFormat.format(nowDt);
			Log.d("DATE - msgsLastUpdatedDateSave", now);
			dbq.msgsLastUpdatedDateSave( now );
		}
	}
	
	public String transmitDownloadOk(String msg_ids
									, String convo_ids
									, String email
									, String password
									, String login_status
									){
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress = "http://www.pickmemycar.com/pintr/COM/regdl.php";

		
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("msg_ids");
		inputVariables.add("convo_ids");
		inputVariables.add("login_status");
		
		inputValues.add(email);
		inputValues.add(password);
		inputValues.add(msg_ids);
		inputValues.add(convo_ids);
		inputValues.add(login_status);
		
		
		String serverEcho
					= rDB.getDBQuery(inputVariables
									,inputValues
									,queryAddress
									);
		return serverEcho;
	}

	
	public String sendMessageToServer(Context context
									, int convoID
						            , String email
						            , String password
						            , String message
						            , String dateTime
						            , String new_action_flag
						            , String recipients
									) {
		
			
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress = "http://www.pickmemycar.com/pintr/COM/send.php";

		if(dateTime.equals("null")){
			dateTime = "2000/01/01 00:00:00.000";
		}
		
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("conversation_id");
		inputVariables.add("message");
		inputVariables.add("dttm_sent");
		inputVariables.add("new_action_flag");
		inputVariables.add("recipients");

		inputValues.add(email);
		inputValues.add(password);
		inputValues.add(Integer.toString(convoID));
		inputValues.add(message);
		inputValues.add(dateTime);
		inputValues.add(new_action_flag);
		inputValues.add(recipients);
		
		
		String serverEcho
					= rDB.getDBQuery(inputVariables
									,inputValues
									,queryAddress
									);
		Log.d("MSGS FROM DB", serverEcho);
		return serverEcho;
	}

	
//***************************
//***  MSG LREAD DT FOR CONVOID
	
	
public void msgLastReadSrvToSqli(Context context) {
		
		Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
			
		ArrayList<String> Creds = dbq.UserCreds();
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String date_last_dl = dbq.lastMsgReadUpdateDate();
		String queryAddress = "http://www.pickmemycar.com/pintr/COM/GetReadTime.php";
		Log.d("ASKING SERVER FOR MSG INFO", "STARTING...");
		
		if(date_last_dl.equals("null")){
			date_last_dl = "2000/01/01 00:00:00.000";
		}
		
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("date_last_dl");
		inputValues.add(Creds.get(0).toString());
		inputValues.add(Creds.get(1).toString());
		inputValues.add(date_last_dl);

		Log.d("ASKING SERVER FOR MSG INFO", "CALLING QUERY...");
		String returnedJSONString
					= rDB.getDBQuery(inputVariables
									,inputValues
									,queryAddress
									);
		Log.d("MLARDS FROM DB", "DATA FROM " + date_last_dl + "  -> " + returnedJSONString);
		
		if(returnedJSONString.equals("CONNECTIONTIMEOUT"))
            Log.d("MSG_001 - msgLastReadSrvToSqli", "CONNECTIONTIMEOUT");
		
		else if(returnedJSONString.equals("EMPTYQUERY") == false){

			JSONArray convoJSON = jsh.JSON_Parser( returnedJSONString
												, "lastreads"
												);

			int jsonlength = convoJSON.length();
			for(int i = 0; i < jsonlength; i++){
		        try {
		        	int conversation_id = convoJSON.getJSONObject(i).getInt("conversation_id");
		        	String dttm_sent = convoJSON.getJSONObject(i).getString("dttm_read");
		        	
					dbq.msgLastReadTblUpdate( conversation_id
											 ,  dttm_sent
											 );
					Log.d("Results to SQLITE:","Wrote to SQLITE:  " + Integer.toString(conversation_id) + "  " +  dttm_sent);
				} catch (JSONException e) {e.printStackTrace();}
			}
		}
	}


	public String sendReadTimeToServer(Context context
										, int convoID
							            , String email
							            , String password
							            , String dateTime
										) {
		
			
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress = "http://www.pickmemycar.com/pintr/COM/PostReadTime.php";
	
		if(dateTime.equals("null")){
			dateTime = "2000/01/01 00:00:00.000";
		}
		
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("conversation_id");
		inputVariables.add("dttm_read");
	
		inputValues.add(email);
		inputValues.add(password);
		inputValues.add(Integer.toString(convoID));
		inputValues.add(dateTime);
		
		
		String serverEcho
					= rDB.getDBQuery(inputVariables
									,inputValues
									,queryAddress
									);
		Log.d("WRITE READTIME TO DB:", serverEcho);
		return serverEcho;
	}

	
	
}
