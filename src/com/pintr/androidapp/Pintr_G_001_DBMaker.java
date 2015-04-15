package com.pintr.androidapp;

import java.util.ArrayList;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
 

public  class Pintr_G_001_DBMaker extends SQLiteOpenHelper {

	// DATABASE CONFIG ELEMENTS:
	    private static final int DATABASE_VERSION = 1;
	    private static final String DATABASE_NAME = "Pintr_DB";
	    private static final String USER_TABLE_NAME = "P_EK_UIDPW";
	    private static final String PEOPLE_TABLE_NAME = "P_EK_PPL";
	    private static final String MSGS_TABLE_NAME = "P_EK_MSGS";
	    private static final String MSGS_LREAD_NAME = "P_EK_MSLR";
	    private static final String USER_INTERESTS_TABLE = "P_EK_INT";
	    private static final String GLOBAL_VAR_NAME = "P_EK_GVAR";  
	    private static final String GLOBAL_MSG_DL_DT = "P_EK_GMSDLDT";  
	    private static final String EVENTS_TYPES_TABLE = "P_EK_EVTP";
	    private static final String YOUR_EVENTS_TABLE = "P_EK_EVTSUM"; 
	    private static final String YOUR_EVENTS_PARTICIPANT_TABLE = "P_EK_EVTPAR";  
	    private static final String STATUS_LIST_TABLE_NAME = "P_IT_STATUS_LIST";
		 
	 

	    //INITIALISE AND CREATE DATABASE
	    public Pintr_G_001_DBMaker(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
	    }
	    
	    
	    public void killDB(Context context){
			context.deleteDatabase(DATABASE_NAME);
	    }

	    
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {			
		}
	    
		
		
	    @Override
		public void onCreate(SQLiteDatabase db) {
	    	//CREATE USER CREDS TABLE
		    String CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ USER_TABLE_NAME 
	        		+ " ( " 
	                + "Lo_1 TEXT "
	                + ",Lo_2 TEXT "
	                + ",Lo_3 TEXT "
	                + ",Lo_4 TEXT "
	                + ")";
	        db.execSQL(CREATE_TABLE);

	        CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ USER_INTERESTS_TABLE 
	        		+ " ( " 
	                + "topic_id NUMERIC "
	                + ",topic TEXT "
	                + ",priority TEXT "
	                + ")";
	        db.execSQL(CREATE_TABLE);

			CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ PEOPLE_TABLE_NAME 
	        		+ " ( " 
	                + "pintr_user_id TEXT"
					+ ",handle TEXT"
					+ ",status TEXT"
	                + ")";
	        db.execSQL(CREATE_TABLE);
	        

			CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ MSGS_TABLE_NAME 
	        		+ " ( " 
	                + "		conversation_id NUMERIC"
	                + "		,pintr_user_id TEXT"
	                + "		,message TEXT"
	                + "		,dttm_sent TEXT"
	                + "		,recipient_notyetreplied_flag BOOLEAN"
					+ "		,handle TEXT"
					+ "		,message_read BOOLEAN"
					+ "		,message_sent BOOLEAN"
	                + ")";
	        db.execSQL(CREATE_TABLE);


			CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ MSGS_LREAD_NAME
	        		+ " ( " 
	                + "		conversation_id NUMERIC"
	                + "		,dttm_read TEXT"
	                + ")";
	        db.execSQL(CREATE_TABLE);
	        

			CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ GLOBAL_MSG_DL_DT
	        		+ " ( " 
	                + "		date_last_dl_msg_data TEXT"
	                + ")";
	        db.execSQL(CREATE_TABLE);
	        
			CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ GLOBAL_VAR_NAME
	        		+ " ( " 
	                + "		outstanding_friend_requests NUMERIC"
	                + "		,date_last_dl_events_data TEXT"
	                + ")";
	        db.execSQL(CREATE_TABLE);

			CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ EVENTS_TYPES_TABLE
	        		+ " ( " 
	        	    + " Event_type_id NUMERIC "
	        	    + " , Event_subtype_id NUMERIC "
	        	    + " , Event_type char(20) "
	        	    + " , Event_subtype char(20) "
	                + ")";
	        db.execSQL(CREATE_TABLE);

	        CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ YOUR_EVENTS_TABLE 
	        		+ " ( " 
	                + "Event_id NUMERIC "
	                + ",Event_creator_uid NUMERIC "
	                + ",handler TEXT "
	                + ",Event_DtTm TEXT "
	                + ",event_type TEXT "
	                + ",type_variant TEXT "
	                + ",event_title TEXT "
	                + ",venue_name TEXT "
	                + ",venue_address_text TEXT "
	                + ",chg_evt_dttm TEXT "
	                + ",hashtags TEXT "
	                + ",venue_yelp_id TEXT "
	                + ",venue_yelp_url TEXT "
	                + ",invite_status TEXT"
	                + ",event_description TEXT"
	                + ")";
	        db.execSQL(CREATE_TABLE);

	        CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ YOUR_EVENTS_PARTICIPANT_TABLE 
	        		+ " ( " 
	                + "Event_id NUMERIC "
	                + ",participant_user_id NUMERIC "
	                + ",participant_event_status TEXT "
	                + ",participant_attending BOOLEAN "
	                + ",handler TEXT "
	                + ")";
	        db.execSQL(CREATE_TABLE);
	        CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ STATUS_LIST_TABLE_NAME 
	        		+ " ( " 
	                + "social_status NUMERIC "
	                + ",social_status_description TEXT "
	                + ")";
	        db.execSQL(CREATE_TABLE);
	    }

	    
	    
	    //CHECK WHETHER DB ALREADY EXISTS
	    public Boolean DBTableExistsCheck(String TableName){

	    	SQLiteDatabase db = this.getWritableDatabase();
			String FullQuery = "SELECT count(*) as rows FROM " + TableName;
			
			//READ QUERY RESULTSET
			Cursor cursor = db.rawQuery(FullQuery, null);
			cursor.moveToFirst();
			int rowcount = Integer.parseInt(cursor.getString(0)); 
	  		
	  		cursor.close();
			db.close();	
	  		
			if (rowcount == 0) 
				return false;
			else 
				return true;
			
	    }
  //*********************************************
  //****    USER CREDS TABLE    *****************
  //*********************************************
	    
	    //REGISTER USER DETAILS TO USER CREDS TABLE
	    public void UserCredsReg(String email, String password, String uid){

			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues insert_row_values= new ContentValues();
			insert_row_values.put("lo_1", email);	
			insert_row_values.put("lo_2", password);	
			insert_row_values.put("lo_3", uid);	
			db.insert(USER_TABLE_NAME, null, insert_row_values);
			db.close();	
		}

	    public void UserCredsUpdate(String email
	    							,String password
	    							, String uid){

			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues insert_row_values= new ContentValues();
			insert_row_values.put("lo_1", email);	
			insert_row_values.put("lo_2", password);
			insert_row_values.put("lo_3", uid);	
			db.update(USER_TABLE_NAME, insert_row_values, "Lo_1 = '" + email + "'", null);
			db.close();	
		}
	    
	    
  //****************************************************
  //****    FRIENDS AND LIKES TABLE    *****************
  //****************************************************

	    //DROP USER FRIENDS AND LIKES TABLE
	    public void dropPeopleTable(){

			SQLiteDatabase db = this.getWritableDatabase();
	    	
			String KILL_TABLE = 
	        		"DROP TABLE IF EXISTS "
	        		+ PEOPLE_TABLE_NAME
	        		+ ";"
	        		;
	        db.execSQL(KILL_TABLE);	
			db.close();	
	    }
	    
	    //CREATE USER FRIENDS AND LIKES TABLE
	    public void MakeUserPeopleTable(){

			SQLiteDatabase db = this.getWritableDatabase();
	    	
		    String CREATE_TABLE = 
	        		"CREATE TABLE "
	        		+ PEOPLE_TABLE_NAME 
	        		+ " ( " 
	                + "pintr_user_id numeric"
					+ ",correspondent_user_id numeric"
					+ ",fr_sent boolean"
					+ ",fr_true boolean"
					+ ",like_user boolean "
	                + ")";
	        db.execSQL(CREATE_TABLE);
			db.close();	
	    }


//********************************************
//****    INTERESTS TABLE    *****************
//********************************************
	    
	    //UPDATE TABLE
	    public void UserInterestsUpdate(int topic_id
										,String topic
		    							, String priority
		    							){
			ArrayList<String> itemNames = new ArrayList<String>();
			ArrayList<String> itemValues = new ArrayList<String>();

			itemNames.add("topic_id");
			itemNames.add("topic");
			itemNames.add("priority");
			itemValues.add(Integer.toString(topic_id));
			itemValues.add(topic);
			itemValues.add(priority);
			
			TableRowInserter(USER_INTERESTS_TABLE,itemNames ,itemValues);
		}
	    
	    

	    //READ TABLE
		public ArrayList<String> UserInterests(){
			
			String FullQuery = "SELECT * FROM " + USER_INTERESTS_TABLE ;
			SQLiteDatabase db = this.getWritableDatabase();
		    ArrayList<String> Creds = new ArrayList<String>();
			
			Cursor cursor = db.rawQuery(FullQuery, null);
			cursor.moveToFirst();
			if(cursor != null && cursor.getCount() > 0) do {
				int topic_id = cursor.getInt(0);
				String topic = cursor.getString(1);
				String priority = cursor.getString(2);
				
				String row = Integer.toString(topic_id)
						+ "," + topic
						+ "," + priority
						;
			    Creds.add(row);
			} while (cursor.moveToNext());
			
			//TIDY UP - CLOSE CONNECTIONS
			cursor.close();
			db.close();	
		    return Creds;

		}
	    
//*********************************************************************

    //UPDATE TABLE
    public void TableRowInserter(String tableName
				    		, ArrayList<String> itemNames
				    		, ArrayList<String> itemValues
				    		){

		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
		
		int tableSize = itemNames.size();
		
		for (int i = 0; i < tableSize; i++){

			insert_row_values.put(itemNames.get(i), itemValues.get(i));
		}
		db.insert(tableName, null, insert_row_values);
		db.close();	
	}
    

    //EMPTY TABLE
    public void EmptyTable(String tableName){

		SQLiteDatabase db = this.getWritableDatabase();
    	String KILL_TABLE = 
        		"DELETE FROM "
        		+ tableName
        		+ ";"
        		;
        db.execSQL(KILL_TABLE);
		db.close();	
	}
	    
}
