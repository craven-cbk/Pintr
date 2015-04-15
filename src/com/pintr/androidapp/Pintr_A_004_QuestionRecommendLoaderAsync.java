package com.pintr.androidapp;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


public class Pintr_A_004_QuestionRecommendLoaderAsync extends AsyncTask<String, Void, String> {
	
	public Pintr_14_DiscussionTool p14 = null;
	final Pintr_G_002_DBQuery dbq;
	final Pintr_G_008_InterestsrDBMSManager intm = new Pintr_G_008_InterestsrDBMSManager();
	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
	Context context;
	String topic_id_list= "";
	
	public Pintr_A_004_QuestionRecommendLoaderAsync(Context contextIn, String topic_id_listIn){
		context = contextIn;
		topic_id_list = topic_id_listIn;
		dbq = new Pintr_G_002_DBQuery(context);
	}
    
	@Override
	protected String doInBackground(String... params) {

		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		String response = "";

		String JSONdata = intm.getRecommendedQuestion(topic_id_list, email, password);
		if(!JSONdata.equals("CONNECTIONTIMEOUT")
			&& !JSONdata.equals("EMPTYQUERY")
			){
			JSONArray jsonArray = jsh.JSON_Parser(JSONdata
										, "question_recommend"
										);
			try {
				response  = jsonArray.getJSONObject(0).getString("answer_text");
			} catch (JSONException e) {e.printStackTrace();}
		}
		else 
			response  = JSONdata;
		Log.d("RECQN JSON", JSONdata);
		
		return response ;
	}

	
    @Override
    protected void onPostExecute(String msg) {
    	p14.processFinish(msg);
    }
    
}
