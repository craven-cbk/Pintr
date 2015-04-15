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
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;


public class Pintr_G_014_ProfilerDBMSManager {
	
	public Pintr_G_014_ProfilerDBMSManager(){
		
	}
	

	// 1.  RETURN THE LIST OF USERS FOUND FROM USERNAME SEARCH
	public String profileSearchResultGetter(
			String email
			,String password
			,String searchterm_handle){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("searchterm_handle", "UTF-8") 
					+ "=" + URLEncoder.encode(searchterm_handle, "UTF-8")
					+ "&" + URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_PR_fdr.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Connection", "close");
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

	// 2.  RETURN THE LATEST X QUESTIONS ASKED BY THE USER
	public String lastAnsweredQuestions(
			String email
			,String password
			,int user_id
			,int limit
			,String topicId){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("profile_uid", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(user_id), "UTF-8")
					+ "&" + URLEncoder.encode("row_limit", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(limit), "UTF-8")
					+ "&" + URLEncoder.encode("topic_id", "UTF-8") 
					+ "=" + URLEncoder.encode(topicId, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_PR_QLAT.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
	

	// 3.  RETURN THE  X MOST POPULAR QUESTIONS ASKED BY THE USER
	public String mostpopAnsweredQuestions(
			String email
			,String password
			,int user_id
			,int limit
			,String topicId){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("profile_uid", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(user_id), "UTF-8")
					+ "&" + URLEncoder.encode("row_limit", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(limit), "UTF-8")
					+ "&" + URLEncoder.encode("topic_id", "UTF-8") 
					+ "=" + URLEncoder.encode(topicId, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_PR_QPOP.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
	


	// 4. RETURN THE USERS INTERESTS
	public String userInterests(
			String email
			,String password
			,int user_id){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("profile_uid", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(user_id), "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_PR_INT.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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


	// 5. RETURN THE USERS DOB
	public String userDOB(  String email
							,String password
							,int user_id){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("profile_uid", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(user_id), "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_PR_POP.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
	
	
	//6.  RETURN THE USERS RELATIONSHIP TO THE VIEWER
	public String userRelationship( String email
									,String password
									,int correspondent_id){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("correspondent_user_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(correspondent_id), "UTF-8")
					+ "&" + URLEncoder.encode("operation", "UTF-8") 
					+ "=" + URLEncoder.encode("GET", "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_FR_LK_UD.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
            Log.d("RETURN RELATIONSHIP:  ",JsonString );
            }
		catch (java.net.SocketTimeoutException e) {
			JsonString = "CONNECTIONTIMEOUT";
            Log.d(JsonString , JsonString );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		return JsonString;
	}	
	
	//7.  CHANGE THE USERS RELATIONSHIP TO THE VIEWER
	public void changeRelationship( String email
									,String password
									,int correspondent_id
									,Boolean you_like_user 
									,Boolean  user_likes_you 
									,Boolean  friends
									,Boolean  you_sent_user_friend_request
									,Boolean  user_sent_you_friend_request
									,String   request_datetime
									){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("correspondent_user_id", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(correspondent_id), "UTF-8")
					+ "&" + URLEncoder.encode("you_like_user", "UTF-8") 
					+ "=" + URLEncoder.encode(String.valueOf(you_like_user), "UTF-8")
					+ "&" + URLEncoder.encode("user_likes_you", "UTF-8") 
					+ "=" + URLEncoder.encode(String.valueOf(user_likes_you), "UTF-8")
					+ "&" + URLEncoder.encode("friends", "UTF-8") 
					+ "=" + URLEncoder.encode(String.valueOf(friends), "UTF-8")
					+ "&" + URLEncoder.encode("you_sent_user_friend_request", "UTF-8") 
					+ "=" + URLEncoder.encode(String.valueOf(you_sent_user_friend_request), "UTF-8")
					+ "&" + URLEncoder.encode("user_sent_you_friend_request", "UTF-8") 
					+ "=" + URLEncoder.encode(String.valueOf(user_sent_you_friend_request), "UTF-8")
					+ "&" + URLEncoder.encode("fr_dt_request", "UTF-8") 
					+ "=" + URLEncoder.encode(request_datetime, "UTF-8")
					+ "&" + URLEncoder.encode("operation", "UTF-8") 
					+ "=" + URLEncoder.encode("INSERT", "UTF-8")
					;
			Log.d("String.valueOf(user_sent_you_friend_request)", String.valueOf(user_sent_you_friend_request));
			Log.d("String.valueOf(you_like_user)", String.valueOf(you_like_user));
			Log.d("String.valueOf(user_likes_you)", String.valueOf(user_likes_you));
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_FR_LK_UD.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
            Log.d("CHANGE QUERIES:", sb.toString());
			
            }
		catch (java.net.SocketTimeoutException e) {
            Log.d( "CONNECTIONTIMEOUT" ,  "CONNECTIONTIMEOUT" );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
	}	
	
	
	
	//8.  GET FRIEND REQUESTS OUTSTANDING
	public int storeFriendRequestsCount( Context context){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
		ArrayList<String> Creds = dbq.UserCreds();
		String email  = Creds.get(0).toString();
		String password = Creds.get(1).toString();
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("operation", "UTF-8") 
					+ "=" + URLEncoder.encode("FRCT", "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}

		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_FR_LK_UD.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
            Log.d("G_14 # FRIEND REQUESTS:  ",JsonString );
            }
		catch (java.net.SocketTimeoutException e) {
			JsonString = "CONNECTIONTIMEOUT";
            Log.d(JsonString , JsonString );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		if(!JsonString.equals("CONNECTIONTIMEOUT")){
			dbq.storeGlobalVar(Integer.parseInt(JsonString));
			return Integer.parseInt(JsonString);
		} else return 0;
	}	
	
	
}
