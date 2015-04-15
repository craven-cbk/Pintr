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



public class Pintr_G_022_VenueSearchFunctions {

	

	public String subSearch(String location, String term, String limit){
		String data = null;
		String link = null;
		String JsonString = "";
		StringBuilder sb = new StringBuilder();
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		try {
			data = 	URLEncoder.encode("location", "UTF-8") 
					+ "=" + URLEncoder.encode(location, "UTF-8")
					+ "&" + URLEncoder.encode("term", "UTF-8") 
					+ "=" + URLEncoder.encode(term, "UTF-8")
					+ "&" + URLEncoder.encode("limit", "UTF-8") 
					+ "=" + URLEncoder.encode(limit, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://pickmemycar.com/pintr/TestBed/YelpSearch.php";
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
            JsonString = sb.toString() ;
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
