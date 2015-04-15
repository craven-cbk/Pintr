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

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.StrictMode;
import android.util.Log;


public class Pintr_G_016_StatusHandler extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Pintr_DB";
    private static final String STATUS_LIST_TABLE_NAME = "P_IT_STATUS_LIST";
    private static final String USER_STATUS_TABLE = "P_EK_UST"; 
    
	public Pintr_G_016_StatusHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
    
	//******************************************
	//*****************STATUS HANDLER***********


	//UPDATE THE STATUS LIST TABLE ON SQLITE
    public void populateStatusList(
						String email
						,String password){
    	
    	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
		
    	MakeStatusListTable();
    	String JSONReturn = JSONGeneralStatusListGetter(email, password);
    	JSONArray StatusList = jsh.JSON_Parser(
						    			JSONReturn
										, "all_status_list"
										);

		int ArLen = StatusList.length();   
		
		for(int i = 0; i < ArLen; i++){
			int social_status = 0;
			String social_status_description = "";
			
			try {
				social_status = StatusList.getJSONObject(i).getInt("social_status");
				social_status_description = StatusList.getJSONObject(i).getString("social_status_description");

				UpdatetStatusListTable(
			    		social_status 
			    		,social_status_description
			    		);
				} catch (JSONException e) {e.printStackTrace();}
			
		}
    }
    

	//EXTRACT THE STATUS LIST TABLE FROM SQLITE
   	public List<String> getStatusList(){

        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery = "SELECT * FROM " + STATUS_LIST_TABLE_NAME ;
		
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
	    List<String> Creds = new ArrayList<String>();
	    
	    if(cursor!=null && cursor.getCount()>0) 
	    	do {
	    		String social_status = cursor.getString(0);
	    		String social_status_description = cursor.getString(1);
	    	    Creds.add(social_status + "," + social_status_description);
	        } while (cursor.moveToNext());
        
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();
		return Creds;

	}	

    public void MakeStatusListTable(){

        SQLiteDatabase db = this.getWritableDatabase();
    	String KILL_TABLE = 
        		"DROP TABLE IF EXISTS "
        		+ STATUS_LIST_TABLE_NAME
        		+ ";"
        		;
        db.execSQL(KILL_TABLE);	
    	
        String CREATE_TABLE = 
        		"CREATE TABLE "
        		+ STATUS_LIST_TABLE_NAME 
        		+ " ( " 
                + "social_status NUMERIC "
                + ",social_status_description TEXT "
                + ")";
        db.execSQL(CREATE_TABLE);
        db.close();
		
    }
	
	
    //UPDATE STATUS LIST TABLE
    public void UpdatetStatusListTable(
    		int social_status 
    		,String social_status_description
    		){

        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
		insert_row_values.put("social_status", social_status);	
		insert_row_values.put("social_status_description", social_status_description);
		db.insert(STATUS_LIST_TABLE_NAME, null, insert_row_values);
		db.close();	
    }
    

	//DOWNLOAD STATUS LIST FROM SERVER
	public String JSONGeneralStatusListGetter(
			String email
			,String password
			){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		String JsonString = null;
		
		try {
			data =  URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		try {
			link = "http://www.pickmemycar.com/pintr/getStatusList.php";
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
	

	//UPLOAD STATUS TO SERVER
	public void JSONStatusUpload(
			String email
			,String password
			,int social_status
			,String location
			,String date_status_set
			,double latitude
			,double longitude
			){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder() ;
		
		try {
			data =  URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("social_status", "UTF-8") 
					+ "=" + URLEncoder.encode(Integer.toString(social_status), "UTF-8")
					+ "&" + URLEncoder.encode("location", "UTF-8") 
					+ "=" + URLEncoder.encode(location, "UTF-8")
					+ "&" + URLEncoder.encode("date_status_set", "UTF-8") 
					+ "=" + URLEncoder.encode(date_status_set, "UTF-8")
					+ "&" + URLEncoder.encode("action_requested", "UTF-8") 
					+ "=" + URLEncoder.encode("UPLOAD", "UTF-8")
					+ "&" + URLEncoder.encode("latitude", "UTF-8") 
					+ "=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
					+ "&" + URLEncoder.encode("longitude", "UTF-8") 
					+ "=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
//			Log.d("LOAD STRINGS:  ",email + ", " + password + ", " + location + ", " +date_status_set + " ->" + Integer.toString(social_status));
		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_ST_UT.php";
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
    		}
		catch (java.net.SocketTimeoutException e) {
            Log.d("CONNECTIONTIMEOUT" , "CONNECTIONTIMEOUT" );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		populateUserStatus(email, password);
	}
	

	//DOWNLOAD STATUS FROM SERVER
	public String JSONStatusDownload(
			String email
			,String password
			){

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		String currentStatusJson = null;
		StringBuilder sb = new StringBuilder() ;
		
		try {
			data =  URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("action_requested", "UTF-8") 
					+ "=" + URLEncoder.encode("GETCURSTAT", "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_ST_UT.php";
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
    		currentStatusJson = sb.toString();
    		}
		catch (java.net.SocketTimeoutException e) {
			currentStatusJson = "CONNECTIONTIMEOUT";
            Log.d(currentStatusJson , currentStatusJson );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		return currentStatusJson;
	}


    public void MakeUserStatusTable(){

        SQLiteDatabase db = this.getWritableDatabase();
    	String KILL_TABLE = 
        		"DROP TABLE IF EXISTS "
        		+ USER_STATUS_TABLE
        		+ ";"
        		;
        db.execSQL(KILL_TABLE);	
    	
        String CREATE_TABLE = 
        		"CREATE TABLE "
        		+ USER_STATUS_TABLE 
        		+ " ( " 
                + "social_status NUMERIC "
                + ",location TEXT "
                + ",date_status_set TEXT "
                + ")";
        db.execSQL(CREATE_TABLE);
		db.close();	
    }

    
    //STORE THE STATUS ON THE SQLITE TABLE
    public void populateUserStatus(
						String email
						,String password){
    	
    	final Pintr_G_009_JSONHandler jsh = new Pintr_G_009_JSONHandler();
		
    	MakeUserStatusTable();
    	String JSONReturn = JSONStatusDownload(email, password);
    	JSONArray StatusList = jsh.JSON_Parser(
						    			JSONReturn
										, "current_status"
										);

		int ArLen = StatusList.length();   
		
		for(int i = 0; i < ArLen; i++){
			int social_status = 0;
			String location = "";
			String date_status_set = "", social_status_ret ="";
			
			try {
				social_status_ret = StatusList.getJSONObject(i).getString("social_status");
				if(social_status_ret.equals("") == false){
					social_status = Integer.parseInt(social_status_ret);
				}
				location = StatusList.getJSONObject(i).getString("location");
				date_status_set = StatusList.getJSONObject(i).getString("date_status_set");

				UpdatetStatusTable(
			    		social_status 
			    		,location
			    		,date_status_set
			    		);
				} catch (JSONException e) {e.printStackTrace();}
			
		}
    }

    //UPDATE STATUS LIST TABLE
    public void UpdatetStatusTable(
    		int social_status 
    		,String location
    		,String date_status_set
    		){

        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
		insert_row_values.put("social_status", social_status);	
		insert_row_values.put("location", location);
		insert_row_values.put("date_status_set", date_status_set);
		db.insert(USER_STATUS_TABLE, null, insert_row_values);
		db.close();	
    }
    

	//EXTRACT THE STATUS FROM SQLITE
   	public List<String> getStatus(){
		
		String FullQuery = "SELECT * FROM " + USER_STATUS_TABLE ;

        SQLiteDatabase db = this.getWritableDatabase();
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
	    List<String> Creds = new ArrayList<String>();
    
	    String social_status = "1", location  = "", date_status_set="";
	    if(cursor != null && cursor.getCount()>0){
			social_status = cursor.getString(0);
			location = cursor.getString(1);
			date_status_set = cursor.getString(2);
		}
	    
	    Creds.add(social_status);
		Creds.add(location);
		Creds.add(date_status_set);
        
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
	    return Creds;
	}	
    
	//DOWNLOAD OTHER USERS STATUS FROM SERVER
	public String JSONOthersStatusDownload(
			String email
			,String password
			,String uid
			){

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String data = null;
		String link = null;
		String currentStatusJson = null;
		StringBuilder sb = new StringBuilder() ;
		
		try {
			data =  URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("password", "UTF-8") 
					+ "=" + URLEncoder.encode(password, "UTF-8")
					+ "&" + URLEncoder.encode("pintr_user_id", "UTF-8") 
					+ "=" + URLEncoder.encode(uid, "UTF-8")
					+ "&" + URLEncoder.encode("action_requested", "UTF-8") 
					+ "=" + URLEncoder.encode("GETOTHERSCURSTAT", "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		try {
			link = "http://www.pickmemycar.com/pintr/AVICI/AVICI_ST_UT.php";
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
    		currentStatusJson = sb.toString();
    		}
		catch (java.net.SocketTimeoutException e) {
			currentStatusJson = "CONNECTIONTIMEOUT";
            Log.d(currentStatusJson , currentStatusJson );
    	}
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		return currentStatusJson;
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
		
		
}
