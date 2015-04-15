package com.pintr.androidapp;

import java.util.ArrayList;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class Pintr_PL_01_PostLoginPreLoad 	extends 	Activity 
											implements 	Pintr_PL_03_LoginLoadUserListener 
											{
    Context context ;
    
	final Pintr_MSG_001_DownloadMesssageHistory sendFn = new Pintr_MSG_001_DownloadMesssageHistory();
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	final Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
	final Pintr_G_007_DtlsChgr dtls = new Pintr_G_007_DtlsChgr();
	final Pintr_G_018_PeopleHandler pph = new Pintr_G_018_PeopleHandler();
	Pintr_G_016_StatusHandler dbsh;
	Pintr_G_021_YourEventsReader yevr;
	Pintr_G_002_DBQuery dbq ;
	Pintr_G_001_DBMaker db;
	
    String EmailGiven, PasswordGiven, Lo_1, Lo_2, Lo_3, Lo_4;
    ProgressDialog progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_pl_01_post_login_pre_load);
		
		context = this;
		
		db = new Pintr_G_001_DBMaker(context);
		dbq = new Pintr_G_002_DBQuery(context);
		dbsh = new Pintr_G_016_StatusHandler(context);
		yevr = new Pintr_G_021_YourEventsReader(context);
		
		progress = new ProgressDialog(context);
        progress.setTitle("Loading");
		progress.setMessage("Wait while loading...");
		progress.show();
		
		Log.d("PRE-START", "PRESTART COMMENCED onCREATE");


		createNotificationPreferences();
		
		ArrayList<String> Creds = dbq.UserCreds();
		Lo_1  = Creds.get(0).toString();
		Lo_2  = Creds.get(1).toString();
		Lo_3  = Creds.get(2).toString();
		Lo_4  = Creds.get(3).toString();
				
		EmailGiven = Lo_1 ;
		PasswordGiven = Lo_2;
		
		
		 //GET DEVICE GCM REG ID
		Class<?> refCL = Pintr_05_ConfirmRegistration.class;
		SharedPreferences prefs = getSharedPreferences(refCL.getSimpleName(),Context.MODE_PRIVATE);
		Pintr_NT_02_LoadWebPageASYNC task 
				= new Pintr_NT_02_LoadWebPageASYNC(context
													, prefs
													, Lo_1
													, Lo_2
													, gcmuf.getDeviceId(context , getContentResolver())
													);
		task.execute();
 
		
        Pintr_PL_02_LoginLoadUserASYNC loadDtlsTask 
			= new Pintr_PL_02_LoginLoadUserASYNC (this
								                , Lo_1
								                , Lo_2
												, Lo_3
								                , Lo_4
												, progress 
							                	);
        loadDtlsTask.execute();

        loadDtlsTask.pl03 = this;
        
       
	}

	
	public void createNotificationPreferences(){
		savePreferences("QuestionsNotificationsOn", true);
		savePreferences("AnswersNotificationsOn", true);
		savePreferences("FriendRequestsNotificationsOn", true);
		savePreferences("MessagesNotificationsOn", true);
		savePreferences("InvitesNotificationsOn", true);
		savePreferences("EventUpdatesNotificationsOn", true);
	
	}



    private void savePreferences(String key, Boolean value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

	 
	 
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbq != null) {
        	dbq.close();
        }
        if (db != null) {
        	db.close();
        }
    }  
    
    public void finishedTasks(){

		progress.dismiss();
		startActivity(new Intent(this, Pintr_03_entry_page.class));
    }



	@Override
	public void processFinish(String output) {

		Log.d("STRING FROM LISTENER", output);
		finishedTasks();
	}

}
