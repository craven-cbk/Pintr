package com.pintr.androidapp;

import android.os.AsyncTask;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


	
public class Pintr_NT_05_SendMessagesASYNC extends AsyncTask<String, Void, String> {

	static final String TAG = "GCMDemo";
	GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;
    String regid, EmailGiven, PasswordGiven, DeviceId ;
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    String SENDER_ID = "92970889157";
    int convoID, pintr_uid;
    String message, now, status, recipientList;
    Map<Integer, String> participantsMap = new HashMap<Integer, String>();
    Iterator<Integer> myVeryOwnIterator = participantsMap.keySet().iterator();
    Activity mActivity;
    TextView tv;
    RelativeLayout rl, spinnerArea;
    ProgressBar pbspnner;
    ImageView tick;
	Button sendFailBtn;
	String oldDate;
    
    final Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
    final Pintr_MSG_001_DownloadMesssageHistory sendFn = new Pintr_MSG_001_DownloadMesssageHistory();
    final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	
    public Pintr_NT_05_SendMessagesASYNC (Context contextIn
    									, String Email
    									, String Password
    									, int convoIDIn
    									, String messageIn
    									, String statusIn 
    									, String recipientListIn
    									, int uid
    									, Map participantsMapIn
    									, RelativeLayout rlIn
//    									, RelativeLayout spinnerAreaIn
    								    , ProgressBar pbspnnerIn
						                , ImageView tickIn
						    			, Button sendFailBtnIn
						    			, String oldDateIn
    
    									){
		context = contextIn;
		EmailGiven = Email;
		PasswordGiven = Password;
		convoID = convoIDIn;
		message = messageIn;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		Date nowDt = Calendar.getInstance().getTime();
		now = simpleDateFormat.format(nowDt);
		status = statusIn;
		recipientList = recipientListIn;
		participantsMap = participantsMapIn;
		pintr_uid = uid;
		rl = rlIn;
//		spinnerAreaIn = spinnerArea;
		pbspnner = pbspnnerIn;
		tick = tickIn;
		sendFailBtn = sendFailBtnIn;
		oldDate = oldDateIn;
		
	}
	
    
    
	@Override
	protected String doInBackground(String... params) {
        
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
		Date nowDt = Calendar.getInstance().getTime();
		String nowDtFmtd = simpleDateFormat.format(nowDt);
		String sendDt = "";

		Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
		Log.d("MSG TBL ATM - 1", dbq.readMessagesTable("READCONVO", convoID));
		ArrayList<String> Creds = dbq.UserCreds();
		
		if(oldDate.equals(""))
			dbq.messagesUpdate( convoID
								, Integer.toString(pintr_uid)
								,  message
								,  nowDtFmtd
								,  false
								,  Creds.get(3).toString()
								,  true
								,  "false"
								);
		Log.d("MSG TBL ATM - 2", dbq.readMessagesTable("READCONVO", convoID));
		
		Boolean networkAvailable = nw.haveNetworkConnection(context);
		Boolean serverTransmitOk = false;
		
		if(networkAvailable){

			String serverEcho 
				= sendFn.sendMessageToServer( context
											, convoID
							                , EmailGiven
							                , PasswordGiven
							                , message
							                , now
							                , status
							                , recipientList
							                );

			Log.d("serverEcho/convoID", "-> p" + serverEcho + "/" + convoID);
			serverTransmitOk = isNumeric(serverEcho) ;
			
			if(serverTransmitOk ){
				Log.d("serverTransmitOk ", "OK");
				if(!oldDate.equals(""))
					sendDt = oldDate;
				else
					sendDt = now;				
					
				if(convoID == 0 ){
					convoID = Integer.parseInt(serverEcho);
					dbq.updateMessageConvoidByDate(convoID, nowDtFmtd, pintr_uid, sendDt  );
					Log.d("MSG TBL ATM - 3", dbq.readMessagesTable("READCONVO", convoID));
					
					Log.d("NEW convoID ", "->" + convoID );
				}
				else{
					dbq.updateMessageRowByDateAsSent(convoID , sendDt, nowDtFmtd);
					Log.d("OLD UPDATE", "->" + convoID );
					Log.d("MSG TBL ATM - 4", dbq.readMessagesTable("READCONVO", convoID));
					
				}
				
				sendFn.sendReadTimeToServer( context
											, Integer.parseInt(serverEcho)
								            ,  EmailGiven
								            ,  PasswordGiven
								            , nowDtFmtd
											);
						
				//SEND MESSAGE NOTIFICATION TO PARTICIPANTS
				myVeryOwnIterator = participantsMap.keySet().iterator();
				while(myVeryOwnIterator.hasNext()) {
					Integer key=myVeryOwnIterator.next();
					if (key != pintr_uid){
						String ServerResponse = 
								gcmuf.dispatchMessageToServer(	Integer.toString(key)
																,EmailGiven
																,PasswordGiven
																,participantsMap.get(pintr_uid) + ": '" + message + "'"
																,"MSG"
																);
						Log.d("ServerResponse", ServerResponse);	
					}
				}
				Log.d("MSG TBL ATM - 5", dbq.readMessagesTable("READCONVO", convoID));
				
				return "success";	
				
			}else return "serverfail";
		} else	if(networkAvailable == false)	
			return "networkfail";
		else	
			return "unknownfail";
		}
	

	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
		  Integer.parseInt(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
	
	
	
    @Override
    protected void onPostExecute(String msg) {
    	Log.d(TAG, "MESSAGE SEND STATUS:  " + msg);
    	String displayMessage;
    	
    	if(msg.equals("success")){
    		rl.setBackgroundResource(R.drawable.callout_white_grey_borders);
    		pbspnner.setVisibility(View.GONE);
    		tick.setVisibility(View.VISIBLE);
    		displayMessage = "Message successfully sent";
    	
    	}else {
    		displayMessage = "Message send failed";
    		pbspnner.setVisibility(View.GONE);
    		sendFailBtn.setVisibility(View.VISIBLE);
    	}
    	
    	Toast.makeText(context,displayMessage, 
                Toast.LENGTH_SHORT).show();

    	Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
		Log.d("MSG TBL ATM - 6", dbq.readMessagesTable("READCONVO", convoID));
		
    }
    
    
}