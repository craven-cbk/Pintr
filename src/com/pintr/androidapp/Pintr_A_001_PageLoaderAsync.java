package com.pintr.androidapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class Pintr_A_001_PageLoaderAsync extends AsyncTask<String, Void, String> {
	
	public Pintr_PL_03_LoginLoadUserListener pl03 = null;
	final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
	final Pintr_G_018_PeopleHandler pph = new Pintr_G_018_PeopleHandler();
	Context context;
	String location,email, password;
	
	public Pintr_A_001_PageLoaderAsync(Context contextIn, String emailIn, String passwordIn){
		context = contextIn;
		email = emailIn;
		password = passwordIn;
	}
    
	@Override
	protected String doInBackground(String... params) {

		pph.downloadPeopleData("friends", email, password, context);
		pph.downloadPeopleData("youlike", email, password, context);
		pph.downloadPeopleData("likeyou", email, password, context);
		pph.downloadPeopleData("friendrequests", email, password, context);

		return null;
	}

	
    @Override
    protected void onPostExecute(String msg) {

    	Log.d("Pintr_G_003", "EXECUTE COMPLETE");
    	pl03.processFinish("Passed from listener!");
    }
    
}
