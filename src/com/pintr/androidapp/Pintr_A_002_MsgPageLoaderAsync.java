package com.pintr.androidapp;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class Pintr_A_002_MsgPageLoaderAsync extends AsyncTask<String, Void, String> {
	
	public Pintr_19_MessagesOverview p19 = null;
	final Pintr_MSG_001_DownloadMesssageHistory dlmh = new Pintr_MSG_001_DownloadMesssageHistory();
	Context context;
	
	public Pintr_A_002_MsgPageLoaderAsync(Context contextIn){
		context = contextIn;
	}
    
	@Override
	protected String doInBackground(String... params) {

		Log.d("ORIGIN CLASS", "Pintr_A_002_MsgPageLoaderAsync");
		dlmh.msgLastReadSrvToSqli(context);
		dlmh.writeDataFromServerToSQLite(context, "0");

		return null;
	}

	
    @Override
    protected void onPostExecute(String msg) {
    	p19.processFinish("Passed from listener!");
    }
    
}
