package com.pintr.androidapp;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;


	
public class Pintr_PL_02_LoginLoadUserASYNC extends AsyncTask<String, Void, String> {

	Context context;
    String regid, EmailGiven, PasswordGiven, uidGiven, HandlerGiven ;
    ProgressDialog progress ;
    
    public Pintr_PL_03_LoginLoadUserListener pl03 = null;
    
    final Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
    final Pintr_MSG_001_DownloadMesssageHistory sendFn = new Pintr_MSG_001_DownloadMesssageHistory();
    final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
    final Pintr_G_018_PeopleHandler pph = new Pintr_G_018_PeopleHandler();
	final Pintr_G_016_StatusHandler dbsh ;
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	Pintr_G_020_EventsTypeListReader etlr;
    Pintr_G_021_YourEventsReader yevr ;
	

    
    public Pintr_PL_02_LoginLoadUserASYNC (Context contextIn
    									, String Email
    									, String Password
    									, String Lo_3
    									, String Lo_4
    									, ProgressDialog progressIn
    									){
		context = contextIn;
		EmailGiven = Email;
		PasswordGiven = Password;
		uidGiven = Lo_3;
		HandlerGiven = Lo_4;
		dbsh = new Pintr_G_016_StatusHandler(context);
		yevr = new Pintr_G_021_YourEventsReader(context);
		progress = progressIn;
		etlr = new Pintr_G_020_EventsTypeListReader(context);
		
	}
	
    
    
	@Override
	protected String doInBackground(String... params) {
      
        //INITIALISE THE ON-BOARD SQLITE DB:
		final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
		Pintr_G_001_DBMaker db = new Pintr_G_001_DBMaker(context);
		db.DBTableExistsCheck("P_EK_UIDPW");
  		
  		//ADD USER DETAILS TO SQLITE DB
//  		dbq.RegisterUserOnBoard(EmailGiven, PasswordGiven, uidGiven, HandlerGiven);
//  		Log.d("",EmailGiven+ PasswordGiven+ uidGiven);


  		Pintr_G_007_DtlsChgr dtls = new Pintr_G_007_DtlsChgr();
  		
  		String [] DetailsArray = dtls.GetUserDetails(EmailGiven, PasswordGiven).split(",");
  		
  		dbq.MakeUserDetailsTable( 
  				DetailsArray[0]
  				, DetailsArray[1]
  				, DetailsArray[2]
  				, DetailsArray[3]
  				);
  		
  		Boolean networkAvailable  = nw.haveNetworkConnection(context);
		
        if(networkAvailable){
    		dbsh.populateStatusList(EmailGiven, PasswordGiven);
			//SAVE USER STATUS
			dbsh.populateUserStatus(EmailGiven, PasswordGiven);
			
			//GET PEOPLE DATA
			pph.downloadPeopleData("friends",EmailGiven, PasswordGiven, context);
			pph.downloadPeopleData("youlike",EmailGiven, PasswordGiven, context);
			pph.downloadPeopleData("likeyou",EmailGiven, PasswordGiven, context);
			pph.downloadPeopleData("friendrequests",EmailGiven, PasswordGiven, context);
			
			//GET MESSAGES
			Log.d("ORIGIN CLASS", "Pintr_PL_02_LoginLoadUserASYNC");
			dbq.msgsLastUpdatedDateSave( "2000/01/01 00:00:00.000" );
			sendFn.writeDataFromServerToSQLite(context, "1");
			sendFn.msgLastReadSrvToSqli(context);
			
			//GET EVENTS
			yevr.downloadYourEvents(EmailGiven, PasswordGiven);
        }

		//**  DL THE LATEST EVT TYPE TBL
        String JSONdata = etlr.getEventList();
		Log.d("EVENTS LIST", "->" + JSONdata);
		if(!JSONdata.equals("CONNECTIONTIMEOUT"))
			etlr.storeJSONData(jsh.JSON_Parser(JSONdata , "event_types_list"));
		
		//GET YOUR USER DETAILS FROM THE ONBOARD DB
		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		
		yevr.downloadYourEvents(email, password);

		Log.d("PRE-START", "PRESTART EXECUTED onSTART");
		return "";
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

    	Log.d("Pintr_PL_02", "EXECUTE COMPLETE");
    	pl03.processFinish("Passed from listener!");
    }
    
    
}