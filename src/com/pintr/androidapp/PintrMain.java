package com.pintr.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PintrMain extends Activity {
	
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery (this);
	final Pintr_G_001_DBMaker db = new Pintr_G_001_DBMaker(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Boolean dbExists = db.DBTableExistsCheck("P_EK_UIDPW");
		Log.d("DOES DB EXIST?", Boolean.toString(dbExists) );
		
		if(dbExists){
			//IF THERE IS A DATABASE WITH LOGIN CREDS, GET USER DETAILS AND GOTO HOME PAGE
			Intent AutoRegOnBoard = new Intent(this, Pintr_PL_01_PostLoginPreLoad.class);
			startActivity(AutoRegOnBoard);
		}
		else{
			//IF THERES NO DATABASE WITH LOGIN CREDS, GOTO LOGIN PAGE
			startActivity(new Intent(this, Pintr_01_login_page.class));			
		}
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
}
