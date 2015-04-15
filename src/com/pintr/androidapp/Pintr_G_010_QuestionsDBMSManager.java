package com.pintr.androidapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.os.StrictMode;
import android.util.Log;


public class Pintr_G_010_QuestionsDBMSManager {
	
	public Pintr_G_010_QuestionsDBMSManager(){
		
	}
	
	
	// 1.  RETURN THE LIST OF TOPICS ON THE DATABASE FOR A GIVEN TOPIC LEVEL, AS A JSON STRING
	public String JSONQuestionGetter(
			int records_requested
			,String email
			,String password
			,String function
			,String topic_of_interest
			,int hashtag_id
			,int question_id
			,String maxDate
			,int InterestTopic
			){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = "";
		//String function = "RECENT";
		
		try {
			data = 	URLEncoder.encode("topic_of_interest", "UTF-8") 
					+ "=" + URLEncoder.encode(topic_of_interest, "UTF-8")
					+ "&" + URLEncoder.encode("function", "UTF-8") 
					+ "=" + URLEncoder.encode(function, "UTF-8")
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("records_requested", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(records_requested), "UTF-8")
					+ "&" + URLEncoder.encode("hashtag_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(hashtag_id), "UTF-8")
					+ "&" + URLEncoder.encode("question_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(question_id), "UTF-8")					
					+ "&" + URLEncoder.encode("maxDate", "UTF-8") 
					+ "=" + URLEncoder.encode(maxDate, "UTF-8")
					
					+ "&" + URLEncoder.encode("InterestTopic", "UTF-8")
					+ "=" + URLEncoder.encode(Integer.toString(InterestTopic), "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_QUA_manager.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Connection", "close");
 			conn.setConnectTimeout(3000);
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write( data ); 
			SendToPhp.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            // Read Server Response
            while((line = reader.readLine()) != null)
	            {
        		
            		sb.append(line);
            		break;
	            }
            JsonString = sb.toString();
		
    		}
		catch (java.net.SocketTimeoutException e) {
			JsonString = "CONNECTIONTIMEOUT";
            Log.d(JsonString , JsonString );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		return JsonString;
	}
	
}
