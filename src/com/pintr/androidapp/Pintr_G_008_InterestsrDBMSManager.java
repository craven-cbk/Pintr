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
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;


public class Pintr_G_008_InterestsrDBMSManager {
	
	public Pintr_G_008_InterestsrDBMSManager(){
		
	}
	
	
	// 1.  RETURN THE LIST OF TOPICS ON THE DATABASE FOR A GIVEN TOPIC LEVEL, AS A JSON STRING
	public String TopicListGetter(
			int TopicParentLevel
			,String email
			,String password){
		
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress = "http://www.pickmemycar.com/pintr/AVICI/AVICI_topic_manager.php";
		
		String function = "TOPICLIST";
		
		inputVariables.add("topic_root_id");
		inputVariables.add("function");
		inputVariables.add("email");
		inputVariables.add("password");
		
		inputValues.add(Integer.toString(TopicParentLevel));
		inputValues.add(function);
		inputValues.add(email);
		inputValues.add(password);
		
		return rDB.getDBQuery(	inputVariables
								,inputValues
								,queryAddress
								);
	}
	
	//2.  DECLARE TOPIC INTEREST BY USER
	public String UserDeclareTopicInterest(
				int topicOfInterest
				,String email
				,String password
				){
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String response = null;
		String function = "ADDINTEREST";
		
		try {
			data = 	URLEncoder.encode("topic_interest", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(topicOfInterest), "UTF-8")
					+ "&" + URLEncoder.encode("function", "UTF-8") 
					+ "=" + URLEncoder.encode(function, "UTF-8")
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_topic_manager.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
 			conn.setConnectTimeout(5000);
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
            response = sb.toString();
			
    		}
		catch (java.net.SocketTimeoutException e) {
			response = "CONNECTIONTIMEOUT";
            Log.d(response , response );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		return response;
	}
	

	//3.  REMOVE USER INTEREST IN TOPIC
	public String UserRemoveTopicInterest(
				int topicOfInterest
				,String email
				,String password
				){
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String response = null;
		String function = "REMOVEINTEREST";
		
		try {
			data = 	URLEncoder.encode("topic_interest", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(topicOfInterest), "UTF-8")
					+ "&" + URLEncoder.encode("function", "UTF-8") 
					+ "=" + URLEncoder.encode(function, "UTF-8")
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_topic_manager.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
 			conn.setConnectTimeout(5000);
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
            response = sb.toString();
			
    		}
		catch (java.net.SocketTimeoutException e) {
			response = "CONNECTIONTIMEOUT";
            Log.d(response , response );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		return response;
	}
	
	//4.  RETURN A GIVEN USERS TOPICS OF INTEREST
	public String UserInterestsExtract(
			String email
			,String password
			){

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		String function = "RETURNUSERINTERESTS";
		
		try {
			data =  URLEncoder.encode("function", "UTF-8") 
					+ "=" + URLEncoder.encode(function, "UTF-8")
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_topic_manager.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
 			conn.setConnectTimeout(5000);
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
	
	

	public String getRecommendedQuestion(String topic_id_list, String email, String password){
		
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress ="http://www.pickmemycar.com/pintr/AVICI/AVICI_QUSUG.php";

		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("topic_id_list");

		inputValues.add(email);
		inputValues.add(password);
		inputValues.add(topic_id_list);

		return rDB.getDBQuery(inputVariables
								,inputValues
								,queryAddress);
	}
							
}
