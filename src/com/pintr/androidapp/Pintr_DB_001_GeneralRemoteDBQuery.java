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
import java.util.List;
import android.os.StrictMode;
import android.util.Log;

public class Pintr_DB_001_GeneralRemoteDBQuery  {

	public Pintr_DB_001_GeneralRemoteDBQuery(){
	}

	public String getDBQuery(List<String> inputVariables
							,List<String> inputValues
							,String queryAddress
							){
		Log.d("ADDRESS FOR QUERY:","@" + queryAddress);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = "";
		StringBuilder sb = new StringBuilder() ;
		String JsonString = "";
		
		int arrayLength = inputVariables.size();
		try {
			for(int i=0; i < arrayLength; i++){
				if(i>0){
					data = data + "&";
				}
				Log.d("DB PARSED ENTRIES FOR QUERY:","@" + Integer.toString(i) + "   " + inputVariables.get(i).toString());
				Log.d("DB PARSED VALUES FOR QUERY:","@" + Integer.toString(i) + "   " + inputValues.get(i).toString());
				data = data 
						+ URLEncoder.encode(inputVariables.get(i).toString(), "UTF-8") 
						+ "=" 
						+ URLEncoder.encode(inputValues.get(i).toString(), "UTF-8")
						;
	
			}
		} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		try {
			URL url = new URL(queryAddress);  
			URLConnection conn = url.openConnection();
 			conn.setRequestProperty("Connection", "close");
 			conn.setConnectTimeout(10000);
    		
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write( data ); 
			SendToPhp.flush();
			Log.d("WROTE TO DB", "---");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            // Read Server Response
            while((line = reader.readLine()) != null)
	            {
        		
            		sb.append(line);
            		break;
	            }
            JsonString = sb.toString();
            Log.d("RETRUNFROMDB", JsonString );
    		}
		catch (java.net.SocketTimeoutException e) {
            JsonString = "CONNECTIONTIMEOUT";
            Log.d(JsonString , JsonString );
    	}catch (MalformedURLException e) {
    		e.printStackTrace();
    	} catch (IOException e) {e.printStackTrace();}
		
		return JsonString;
		
	}
}
