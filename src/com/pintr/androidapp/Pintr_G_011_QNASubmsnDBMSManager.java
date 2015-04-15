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


public class Pintr_G_011_QNASubmsnDBMSManager {
	
	public Pintr_G_011_QNASubmsnDBMSManager(){
		
	}
	
	
	// 1.  RETURN THE LIST OF TOPICS ON THE DATABASE FOR A GIVEN TOPIC LEVEL, AS A JSON STRING
	public String JSONQuestionGetter(
			String function
			,String email
			,String password
			,int topic_id
			,int questionmaker_id
			,int user_answered_id
			,String questiontext 
			,String answertext
			,int question_id
			,int answer_id
			,int below_answerid
			,String answerdate
			,int hashtag_id
			,String hashtag
			){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 			URLEncoder.encode("function", "UTF-8") 
					+ "=" + URLEncoder.encode(function, "UTF-8")
					
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					
					+ "&" + URLEncoder.encode("topic_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(topic_id), "UTF-8")
					
					+ "&" + URLEncoder.encode("questionmaker_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(questionmaker_id), "UTF-8")
					
					+ "&" + URLEncoder.encode("user_answered_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(user_answered_id), "UTF-8")
					
					+ "&" + URLEncoder.encode("questiontext", "UTF-8") 
					+ "=" + URLEncoder.encode(questiontext, "UTF-8")
					
					+ "&" + URLEncoder.encode("answertext", "UTF-8") 
					+ "=" + URLEncoder.encode(answertext, "UTF-8")
					
					+ "&" + URLEncoder.encode("question_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(question_id), "UTF-8")
					
					+ "&" + URLEncoder.encode("answer_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(answer_id), "UTF-8")
					
					+ "&" + URLEncoder.encode("below_answerid", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(below_answerid), "UTF-8")
					
					+ "&" + URLEncoder.encode("answerdate", "UTF-8") 
					+ "=" + URLEncoder.encode(answerdate, "UTF-8")
					
					+ "&" + URLEncoder.encode("hashtag_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(hashtag_id), "UTF-8")
					
					+ "&" + URLEncoder.encode("hashtag", "UTF-8") 
					+ "=" + URLEncoder.encode(hashtag, "UTF-8")
					
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_QUA_subter.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
    		conn.setRequestProperty("Connection", "close");

 			conn.setConnectTimeout(5000);
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
