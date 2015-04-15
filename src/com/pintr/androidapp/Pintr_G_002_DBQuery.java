package com.pintr.androidapp;


import java.util.ArrayList;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 

public  class Pintr_G_002_DBQuery  extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Pintr_DB";
    private static final String USER_TABLE_NAME = "P_EK_UIDPW";
    private static final String DETAILS_TABLE_NAME = "P_EK_DTLS";
    private static final String PEOPLE_TABLE_NAME = "P_EK_PPL";
    private static final String MSGS_TABLE_NAME = "P_EK_MSGS";
    private static final String MSGS_LREAD_NAME = "P_EK_MSLR";
    private static final String GLOBAL_VAR_NAME = "P_EK_GVAR";
    private static final String GLOBAL_MSG_DL_DT = "P_EK_GMSDLDT";  
    private static final String YOUR_EVENTS_TABLE = "P_EK_EVTSUM"; 
    private static final String YOUR_EVENTS_PARTICIPANT_TABLE = "P_EK_EVTPAR";    

    final Pintr_G_001_DBMaker g01 ;
    
    //INITIALISE AND CREATE DATABASE
    public Pintr_G_002_DBQuery(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        g01 = new Pintr_G_001_DBMaker(context);
    }

	//READ ITEMS FROM USER DETAILS  DATABASE
	public ArrayList<String> UserCreds(){

        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery = "SELECT * FROM " + USER_TABLE_NAME ;
		
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
	    ArrayList<String> Creds = new ArrayList<String>();
		
		if(cursor != null && cursor.getCount() > 0) {
			String email = cursor.getString(0);
			String password = cursor.getString(1);
			String uid = cursor.getString(2);
			String handle = cursor.getString(3);
		    Creds.add(email);
		    Creds.add(password);
		    Creds.add(uid);
		    Creds.add(handle);
		}
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	

	    return Creds;
	}
	

	//ADD ITEMS TO USER DETAILS  DATABASE
	public void RegisterUserOnBoard(String Lo_1, String Lo_2, String Lo_3, String Lo_4){


        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
		insert_row_values.put("Lo_1", Lo_1);	
		insert_row_values.put("Lo_2", Lo_2);	
		insert_row_values.put("Lo_3", Lo_3);	
		insert_row_values.put("Lo_4", Lo_4);	
		Log.d("UID REGD ONBOARD:", Lo_3);	
		Log.d("UID HANDLE ONBOARD:", Lo_4);
		db.insert(USER_TABLE_NAME, null, insert_row_values);
		
		String FullQuery = "SELECT * FROM " + USER_TABLE_NAME ;
		
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		
		String email = cursor.getString(0);
		String password = cursor.getString(1);
		String uid = cursor.getString(2);
		
		Log.d("FOUND ON ONBOARD DB:",email + password + uid );

		cursor.close();
		db.close();	
	}

	//INSERT ITEMS INTO PEOPLE TABLE
	public void PeopleAdder(
						int pintr_user_id 
						,int correspondent_user_id 
						,boolean fr_sent 
						,boolean fr_true 
						,boolean like_user ){
		

        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
		
		insert_row_values.put("pintr_user_id", pintr_user_id);	
		insert_row_values.put("correspondent_user_id", correspondent_user_id);	
		insert_row_values.put("fr_sent", fr_sent);	
		insert_row_values.put("fr_true", fr_true);		
		insert_row_values.put("like_user", like_user);	
		
		db.insert(PEOPLE_TABLE_NAME, null, insert_row_values);
		db.close();	
	}
    
	
//******************************************
//****  MESSAGES TABLE  ********************



	//SAVE LAST UPDATE DATE
	public void msgsLastUpdatedDateSave( String date){
        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
			
		String KILL_TABLE = 
			"DELETE FROM " +  GLOBAL_MSG_DL_DT + ";"
			;
		db.execSQL(KILL_TABLE);
		
		insert_row_values.put("date_last_dl_msg_data", date);	
		db.insert(GLOBAL_MSG_DL_DT, null, insert_row_values);
		db.close();	
	}

	//READ LAST UPDATE DATE
	public String msgsLastUpdatedDateRead(){
        SQLiteDatabase db = this.getWritableDatabase();
        String FullQuery = " SELECT * FROM " + GLOBAL_MSG_DL_DT;
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();		
		String date = cursor.getString(0);
		Log.d("lastMSGDLDate", "->" + date);
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		return date;
	}
	
	
    //GET LAST UPDATE DATE
	public String lastMessagesUpdateDate(){
		
        SQLiteDatabase db = this.getWritableDatabase();
        String FullQuery = "SELECT MIN(dttm_sent) FROM "
			        		+ "	(SELECT MAX(dttm_sent) as dttm_sent " 
			        		+ "	FROM " +  MSGS_TABLE_NAME 
			        		+ "	GROUP BY conversation_id)" 
			        		;
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();		
		String lastUpdateDate = "" + cursor.getString(0);
		Log.d("lastUpdateDate", "" + lastUpdateDate);
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
	    return lastUpdateDate;
	}
	
    //UPDATE TABLE
    public void messagesUpdate(int conversation_id
								,String pintr_user_id
    							, String message
    							, String dttm_sent
    							, boolean recipient_notyetreplied_flag
    							, String handle
    							, Boolean message_read 
    							, String message_sent 
    							){

        ArrayList<String> itemNames = new ArrayList<String>();
		ArrayList<String> itemValues = new ArrayList<String>();

		itemNames.add("conversation_id");
		itemNames.add("pintr_user_id");
		itemNames.add("message");
		itemNames.add("dttm_sent");
		itemNames.add("recipient_notyetreplied_flag");
		itemNames.add("handle");
		itemNames.add("message_read");
		itemNames.add("message_sent");
		
		itemValues.add(Integer.toString(conversation_id));
		itemValues.add(pintr_user_id);
		itemValues.add(message);
		itemValues.add(dttm_sent);
		itemValues.add(String.valueOf(recipient_notyetreplied_flag));
		itemValues.add(handle);
		itemValues.add(String.valueOf(message_read));
		itemValues.add(message_sent);
		
		TableRowInserter(MSGS_TABLE_NAME,itemNames ,itemValues);
	}
    
    
    //READ TABLE
	public String readMessagesTable(String readType, int convoid){

        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery = "";
		
		if(readType.equals("READCONVO")){

			FullQuery = 	" SELECT DISTINCT "
						+	"		conversation_id"
						+ 	"		, message "
						+ 	"		, handle "
						+	"		, dttm_sent"
						+	"		, recipient_notyetreplied_flag"
						+	"		, pintr_user_id"
						+	"		, message_sent "
						+ 	" FROM " + MSGS_TABLE_NAME 
						+ 	" WHERE conversation_id = "
						+	convoid
						+ 	" 	;"
						;
			
		}else if(readType.equals("READONECONVOSUMMARY")){

			FullQuery = 	" SELECT DISTINCT "
						+	"		conversation_id"
						+ 	"		, message "
						+ 	"		, handle "
						+	"		, dttm_sent"
						+	"		, recipient_notyetreplied_flag"
						+	"		, pintr_user_id"
						+	"		, message_sent "
						+ 	" FROM " + MSGS_TABLE_NAME 
						+ 	" WHERE conversation_id = "
						+	convoid
						+ 	" ORDER BY dttm_sent DESC LIMIT 1	;"
						;
			
		}else if(readType.equals("CONVOSUMMARY")){

			FullQuery = 	" SELECT DISTINCT "
						+	"		l.conversation_id "
						+ 	"		, l.last_message "
						+ 	"		, l.last_messager_handle "
						+	"		, l.max_dttm_sent "
						+	"		, l.message_read_all "
						+ 	"		, r.participant"
						+ 	"		, r.pintr_user_id "
						+	" FROM "
						+	" 	(SELECT "
						+	"		conversation_id"
						+   "		, COALESCE( CASE WHEN message <> '' THEN message ELSE NULL END, NULL) "
						+ 	"			as last_message "
						+   "		, COALESCE( CASE WHEN message <> '' THEN handle ELSE NULL END, NULL) "
						+ 	"			as last_messager_handle "
						+	"		, dttm_sent as max_dttm_sent "
						+	"		, min(message_read) as message_read_all "
						+ 	" 	FROM " 
						+			 MSGS_TABLE_NAME 
						+	"	GROUP BY "
						+	"		conversation_id "
						+ 	"	HAVING"
						+ 	"		dttm_sent = max(dttm_sent)"
						+	"	) l "
						+	" LEFT JOIN "
						+	" 	(SELECT DISTINCT " 
						+ 	"		handle as participant, conversation_id, pintr_user_id  " 
						+ 	"	FROM "
						+			MSGS_TABLE_NAME 
						+ 	" 	) r "
						+	" ON 1=1 "
						+	"	AND	l.conversation_id  =  r.conversation_id  "
						+	" ORDER BY "
						+	" 	 l.max_dttm_sent DESC, l.conversation_id "
						;	
		}
		Log.d("querystring to SQLITE", FullQuery);
		
		
		String results = "{\"msgs_results\":[";
	    
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		int i = 0;
		if(cursor.getCount() > 0)
			do {
				if ( i > 0){
					results = results + ",";
				}
				String conversation_id = cursor.getString(0);
				String last_message = cursor.getString(1);
				String last_messager_handle = cursor.getString(2);
				String max_dttm_sent = cursor.getString(3);
				String message_read = "";
				String participant= "";
				String pintr_user_id = "";
				String message_sent = "";
				if(readType.equals("CONVOSUMMARY")){
	
					message_read = cursor.getString(4);
					participant = cursor.getString(5);
					pintr_user_id = cursor.getString(6);
				}else {
					message_read= cursor.getString(4);
					pintr_user_id = cursor.getString(5);
					message_sent =  cursor.getString(6);
				}
			
			String row = "{"
					+	"\"conversation_id\":\"" 		+ 	conversation_id + "\"," 
					+ 	"\"max_dttm_sent\":\""			+ 	max_dttm_sent+ "\"," 
					+ 	"\"last_messager_handle\":\""	+ 	last_messager_handle+ "\"," 
					+ 	"\"last_message\":\""			+ 	last_message + "\"," 
					+ 	"\"pintr_user_id\":\""			+ 	pintr_user_id + "\"," 
					+ 	"\"participant\":\""			+ 	participant+ "\"," 
					+ 	"\"message_read\":\""			+ 	message_read+ "\","
					+ 	"\"message_sent\":\""			+ 	message_sent+ "\""  
					+ 	"}"
					;
			results = results + row;
			i++;
		} while (cursor.moveToNext());

		results = results + "]}";
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	

	    return results;
	}
	

	//SET MESSAGES AS READ WHEN OPENED
	public void updateMessageRowAsRead(int ConvoID){

        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("message_read","true"); 
		db.update(MSGS_TABLE_NAME, cv, "conversation_id "+"=" + ConvoID, null);
		//TIDY UP - CLOSE CONNECTIONS
		db.close();	
		
		Log.d("CONVO SET AS READ", Integer.toString(ConvoID));
	}



	//SET MESSAGE CONVOID WITH SERVER-ASSIGNED VALUE
	public void updateMessageConvoidByDate(int ConvoID, String oldDate, int UID, String date){

		SQLiteDatabase db = this.getWritableDatabase();	
		//TIDY UP - CLOSE CONNECTIONS
		String FullQuery =    " UPDATE " + MSGS_TABLE_NAME
							+ " SET conversation_id = " + ConvoID + "  "
							+ "  	, message_sent = 'true'  "
							+ "  	, dttm_sent = '" + date + "'  "
							+ "  WHERE 1=1 "
							+ "  		AND pintr_user_id = " + UID + " "
							+ "  		AND conversation_id = 0  "
							+ "  		AND dttm_sent = '"	+ oldDate + "'"
							+ ";"
							;
		Log.d("UPDATEQUERY DISPLAY", FullQuery);
		db.execSQL(FullQuery);
		Log.d("CONVO ID UPDATED", Integer.toString(ConvoID) + " @ " + oldDate+ " @ " + UID);
		

		FullQuery =   " SELECT * FROM " + MSGS_TABLE_NAME
					+ " WHERE  	conversation_id = " + ConvoID
				;
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		do{
			Log.d("UPDATED CONVOID, RESULTS", " - >  " 
									+ cursor.getString(0) + ", "
									+ cursor.getString(1) + ", "
									+ cursor.getString(2) + ", "
									+ cursor.getString(3) + ", "
									+ cursor.getString(4) + ", "
									+ cursor.getString(5) + ", "
									+ cursor.getString(6) + ", "
									+ cursor.getString(7)
									);
		} while (cursor.moveToNext());

		db.close();	
		
	}

	
	
	//SET MESSAGES AS READ WHEN OPENED
	public void updateMessageRowByDateAsSent(int ConvoID, String oldDate, String date){

        SQLiteDatabase db = this.getWritableDatabase();
		Log.d("CONVO MSG UPDATED", Integer.toString(ConvoID) + " @ " + date);
		
		String FullQuery =   " UPDATE " + MSGS_TABLE_NAME
							+ "  SET message_sent = 'true',  dttm_sent = '" + date + "'  "
							+ "  WHERE  conversation_id = " + ConvoID + " "
							+ "  AND dttm_sent = '"	+ oldDate + "'"
							;
		Log.d("UPDATEQUERY DISPLAY", FullQuery);
		db.execSQL(FullQuery);
		
		FullQuery =   " SELECT * FROM " + MSGS_TABLE_NAME
					+ " WHERE  	conversation_id = " + ConvoID
					+ "		AND dttm_sent = '"	+ date + "'"
				;
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		Log.d("UPDATED SEND DATETIME, RESUTS", " - >  " 
								+ cursor.getString(0) + ", "
								+ cursor.getString(1) + ", "
								+ cursor.getString(2) + ", "
								+ cursor.getString(3) + ", "
								+ cursor.getString(4) + ", "
								+ cursor.getString(5) + ", "
								+ cursor.getString(6) + ", "
								+ cursor.getString(7)
								);
		db.close();	
	}

	
	
	//SET MESSAGES AS READ WHEN OPENED
	public void updateMessageRowAsSent(int ConvoID){

        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("message_sent","true"); 
		db.update(MSGS_TABLE_NAME, cv, "conversation_id "+"=" + ConvoID, null);
		//TIDY UP - CLOSE CONNECTIONS
		db.close();	
		
		Log.d("CONVO SET AS READ", Integer.toString(ConvoID));
	}
    


    //GET UNREAD MESSAGE COUNT
	public int unreadMsgCt(){

        ArrayList<Integer> unreadConvoIds = new ArrayList<Integer> ();
		unreadConvoIds  = unreadMsgConvoList();
		
		int retval = 0;
		if(unreadConvoIds.size() > 0){
			retval = unreadConvoIds.size();
		}
		
	    return retval;
	}
	
	public int checkForExistingConvo(int recipient, int yourUID){
		

        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery = 		" SELECT DISTINCT conversation_id  "
							+ 	" FROM " + MSGS_TABLE_NAME 
							+ 	" WHERE 	pintr_user_id <> " + yourUID + "   "
							+   "			AND conversation_id IN "
							+	" 				(SELECT DISTINCT conversation_id "
							+ 	" 	 			FROM " + MSGS_TABLE_NAME 
							+ 	" 	 			WHERE pintr_user_id = " + recipient
							+ 	"				)"
							+ 	" GROUP BY 	conversation_id "
							+ 	" HAVING 	count(pintr_user_id) = 1"
							+ 	" ORDER BY 	count(pintr_user_id) , max(dttm_sent) DESC "
							+ 	" ;"
							;
		Log.d("QUERY checkForExistingConvo", FullQuery);
		
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();		
		int convoid =0;
		if(cursor != null && cursor.getCount() > 0) {
			convoid = cursor.getInt(0);
		}
		Log.d("convoid", "" + Integer.toString(convoid));

		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
			
		return convoid;
	}
	
//**********************************************
//***** MESSAGE READ TIME FUNCTIONS


    //GET LAST UPDATE DATE
	public String lastMsgReadUpdateDate(){

        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery = "SELECT MIN(dttm_read) FROM " + MSGS_LREAD_NAME ;
		
		
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();		
		String lastUpdateDate = "" + cursor.getString(0);
		Log.d("lastReadDate", "" + lastUpdateDate);
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
	    return lastUpdateDate;
	}
	
	
    //GET UNREAD MESSAGE COUNT
	public ArrayList<Integer> unreadMsgConvoList(){

        String pintr_uid = UserCreds().get(2).toString();
		
        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery =    " SELECT DISTINCT 	"
							+ " 		r.conversation_id"
							+ " 		, l.dttm_read "
							+ " 		, u.pintr_user_id "
							+ " FROM " 
							+ "		(SELECT DISTINCT "
							+ "			conversation_id "
							+ "			, MAX(dttm_sent) as ldtsent "
							+ "		 FROM " + MSGS_TABLE_NAME 
							+ "		 GROUP BY "
							+ "			conversation_id "
							+ "		) r "
							+ " LEFT JOIN "
							+ 		MSGS_TABLE_NAME + " u "
							+ " ON 1=1 "
							+ "  	AND r.conversation_id   = u.conversation_id  "
							+ "  	AND r.ldtsent   = u.dttm_sent  "
							+ " LEFT JOIN "
							+ 		MSGS_LREAD_NAME + " l "
							+ " ON "
							+ "		l.conversation_id = r.conversation_id	"
							+ " WHERE 1=1 "
							+ "		AND (l.dttm_read < r.ldtsent OR l.dttm_read IS NULL) "
							+ "		AND u.pintr_user_id <> '" + pintr_uid + "'  "
							+ " ORDER BY l.conversation_id "
							+ " ;"
							;

		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		ArrayList<Integer> unreadConvoIds = new ArrayList<Integer> ();
			
		
		if(cursor != null && cursor.getCount() > 0) do {
			unreadConvoIds.add(cursor.getInt(0));
			Log.d("UNREAD CONVO:", "" + Integer.toString(cursor.getInt(0)) + " at " + cursor.getString(1));
			
		} while (cursor.moveToNext());
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
	    return unreadConvoIds;
	}
	

	public ArrayList<Integer> NeverReadMsgConvoList(){

        SQLiteDatabase db = this.getWritableDatabase();
		String pintr_uid = UserCreds().get(2).toString();
		
		String FullQuery =	  " SELECT DISTINCT r.conversation_id , l.dttm_read "
							+ " FROM " 
							+ "		(SELECT DISTINCT "
							+ "			conversation_id"
							+ "			, MAX(dttm_sent) as ldtsent "
							+ "		 FROM " + MSGS_TABLE_NAME + ""
							+ "		 WHERE  pintr_user_id <> '" + pintr_uid + "' "
							+ "		 GROUP BY "
							+ "			conversation_id "
							+ "		) r "
							+ " LEFT JOIN "
							+ 		MSGS_LREAD_NAME + " l "
							+ " ON "
							+ "		l.conversation_id = r.conversation_id	"
							+ " WHERE l.dttm_read < r.ldtsent"
							+ " ORDER BY l.conversation_id "
							+ " ;"
							;

		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		ArrayList<Integer> unreadConvoIds = new ArrayList<Integer> ();
			
		
		if(cursor != null && cursor.getCount() > 0) do {
			unreadConvoIds.add(cursor.getInt(0));
			Log.d("UNREAD CONVO:", "" + Integer.toString(cursor.getInt(0)) + " at " + cursor.getString(1));
			
		} while (cursor.moveToNext());
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
	    return unreadConvoIds;
	}
	
	
	
	public ArrayList<Integer> AllConvoIds(){

        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery =	  " SELECT DISTINCT conversation_id "
							+ " FROM " 
							+ 		MSGS_LREAD_NAME 
							+ " ;"
							;

		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		ArrayList<Integer> allConvoIds = new ArrayList<Integer> ();
			
		
		if(cursor != null && cursor.getCount() > 0) do {
			allConvoIds.add(cursor.getInt(0));
			Log.d("CONVO IN DB:", "" + Integer.toString(cursor.getInt(0)));
			
		} while (cursor.moveToNext());
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
	    return allConvoIds;
	}
	
	
	 //UPDATE TABLE
    public void msgLastReadTblUpdate(int conversation_id
    							 , String dttm_read
    							 ){

        ArrayList<Integer> unreadConvoIds = new ArrayList<Integer> ();
		unreadConvoIds  = AllConvoIds();
		
		ArrayList<String> itemNames = new ArrayList<String>();
		ArrayList<String> itemValues = new ArrayList<String>();

		itemNames.add("conversation_id");
		itemNames.add("dttm_read");
		
		itemValues.add(Integer.toString(conversation_id));
		itemValues.add(dttm_read);


		if(unreadConvoIds.contains(conversation_id)){
			TableRowUpdater(MSGS_LREAD_NAME
					,itemNames 
					,itemValues
					,"conversation_id"
					,Integer.toString(conversation_id)
					);
		} else{
			TableRowInserter(MSGS_LREAD_NAME
					,itemNames 
					,itemValues
					);
		}
	}
    
    
//***********************************
//****  GLOBAL VARIABLES   ****
	
    public void storeGlobalVar(int frReqCt){

        ArrayList<String> itemNames = new ArrayList<String>();
		ArrayList<String> itemValues = new ArrayList<String>();
		
		itemNames.add("outstanding_friend_requests");
		itemValues.add(Integer.toString(frReqCt));
		
		g01.EmptyTable(GLOBAL_VAR_NAME);
		
		TableRowInserter(GLOBAL_VAR_NAME
						,itemNames 
						,itemValues
						);	
    }
    
    public int getFriendRequestsCount(){
        SQLiteDatabase db = this.getWritableDatabase();
		String FullQuery =	  " SELECT * "
							+ " FROM " 
							+ 		GLOBAL_VAR_NAME 
							+ " ;"
							;

		//READ QUERY RESULTSET
		int frRqstCt = 0;
		Cursor cursor = db.rawQuery(FullQuery, null);
		if(cursor != null && cursor.getCount() > 0){
			cursor.moveToFirst();
			frRqstCt = (cursor.getInt(0));
			
		}
			
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
	    return frRqstCt ;
    }
    
    
    
//********************************
//******  UTILITY FUNCTIONS
    

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
   
    
  //CREATE USER DETAILS TABLE
    public void MakeUserDetailsTable(
    		String fname
    		,String lname
    		,String dob
    		,String gender
    		){
        SQLiteDatabase db = this.getWritableDatabase();
    	String KILL_TABLE = 
        		"DROP TABLE IF EXISTS "
        		+ DETAILS_TABLE_NAME
        		+ ";"
        		;
        db.execSQL(KILL_TABLE);	
    	
        String CREATE_TABLE = 
        		"CREATE TABLE "
        		+ DETAILS_TABLE_NAME 
        		+ " ( " 
                + "fname TEXT "
                + ",lname TEXT "
                + ",dob TEXT "
                + ",gender TEXT "
                + ")";
        db.execSQL(CREATE_TABLE);
        
		ContentValues insert_row_values= new ContentValues();
		insert_row_values.put("fname", fname);	
		insert_row_values.put("lname", lname);	
		insert_row_values.put("dob", dob);	
		insert_row_values.put("gender", gender);	
		db.insert(DETAILS_TABLE_NAME, null, insert_row_values);

		db.close();	
    }


    public void TableRowDeleteInsert(String tableName
						    		, ArrayList<String> itemNames
						    		, ArrayList<String> itemValues
						    		, ArrayList<String> keyNames
						    		, ArrayList<String> keyValues
						    		){
		
		deleteRowsHard(	tableName
			    		,  keyNames
			    		,  keyValues
			    		);
		
		TableRowInserter(tableName
			    		, itemNames
			    		, itemValues
			    		);
	}

  //SET MESSAGES AS READ WHEN OPENED
  	public void globalValueUpdater(String table, String field, String value){

        SQLiteDatabase db = this.getWritableDatabase();
  		ContentValues cv = new ContentValues();
  		cv.put(field, value); 
  		db.update(table, cv, null, null);
  		//TIDY UP - CLOSE CONNECTIONS
  		
  		Log.d("TABLE GLOBAL UPDATE",field + " -> " + value);
//  		wholeTableReader(table, ",");
  	}
      

    public void TableRowUpdater(String tableName
					    		, ArrayList<String> itemNames
					    		, ArrayList<String> itemValues
					    		, String  keyNames
					    		, String  keyValues
					    		){

        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
		
		int tableSize = itemNames.size();
		
		for (int i = 0; i < tableSize; i++){

			Log.d("SQLITE -> MSG READS UPDATED", itemNames.get(i) + " -> " + itemValues.get(i));
			
			insert_row_values.put(itemNames.get(i), itemValues.get(i));
		}
		db.replace(tableName, keyNames + "=" + keyValues, insert_row_values);
	}

    public ArrayList<String> wholeTableReader(String tableName
    										, final String separatorChar
    										){

        SQLiteDatabase db = this.getWritableDatabase();
    	ArrayList<String> allData = new ArrayList<String>();
    	
    	
		String FullQuery =	" SELECT DISTINCT *  FROM " 
							+ tableName 
							+ " ;"
							;
		
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		int columnCount = cursor.getColumnCount();
		
		
		cursor.moveToFirst();
		if(cursor != null && cursor.getCount() > 0)
			do {
			
				String valuesStore = "";
				for (int i = 0; i < columnCount; i++){
					if (i>0) valuesStore = valuesStore  + separatorChar;
					valuesStore = valuesStore + cursor.getString(i) ;
				}
				allData.add(valuesStore);
			} while (cursor.moveToNext());
		else allData.add("null,null");
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
	    return allData;
	}
    
    public ArrayList<String> queryTableReader(	String tableName
												, String separatorChar
												, String whereClauses
												){
        SQLiteDatabase db = this.getWritableDatabase();
    	ArrayList<String> allData = new ArrayList<String>();
		
		String FullQuery =	" SELECT DISTINCT *  FROM " 
							+ tableName 
							+ "  WHERE 1=1 "
							+ whereClauses
							+ " ;"
							;
							
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		int columnCount = cursor.getColumnCount();
		
		
		cursor.moveToFirst();
		
		if(cursor != null && cursor.getCount() > 0) do {
		
			String valuesStore = "";
			for (int i = 0; i < columnCount; i++){
				if (i>0) valuesStore = valuesStore  + separatorChar;
				valuesStore = valuesStore + cursor.getString(i) ;
			}
			allData.add(valuesStore);
			Log.d("FULL TBL READ", valuesStore);
		
		} while (cursor.moveToNext());
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
		return allData;
	}
    
    

    public ArrayList<String> yourEventsSummaryReader(String separatorChar
													, String whereClauses
													, String pintr_uid
													){
    	
        SQLiteDatabase db = this.getWritableDatabase();
    	ArrayList<String> allData = new ArrayList<String>();
		
		String FullQuery =	  "  SELECT DISTINCT l.* , r.participant_attending, r.participant_event_status "
							+ "  FROM " 
							+ 		YOUR_EVENTS_TABLE + " l "
							+ "  LEFT JOIN  "
							+ 		YOUR_EVENTS_PARTICIPANT_TABLE + " r "
							+ "  ON "
							+ "		l.Event_id = r.Event_id AND "
							+ "		r.participant_user_id = " + pintr_uid
							+ "  WHERE 1=1 "
							+ whereClauses
							+ " ;"
							;
							
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		int columnCount = cursor.getColumnCount();
		
		Log.d("QUERY SUBMITTED", "->" + FullQuery);
		
		cursor.moveToFirst();
		
		if(cursor != null && cursor.getCount() > 0) do {

			Log.d("ROWS READ", "->" + cursor.getCount());
			Log.d("COLUMNS FOUND", "->" + columnCount);		String valuesStore = "";
			
			for (int i = 0; i < columnCount; i++){
				if (i > 0 )
					valuesStore = valuesStore  + separatorChar;
				if(cursor.getString(i).equals("") 
						|| cursor.getString(i).equals(null))
					valuesStore = valuesStore + " " ;
				else
					valuesStore = valuesStore + cursor.getString(i);
				Log.d("valuesStore " + i, valuesStore );
			}
			allData.add(valuesStore);
			Log.d("FULL TBL READ", valuesStore);
		
		} while (cursor.moveToNext());
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
		return allData;
	}
    
    
    public ArrayList <String> yourEventParticipantsReader( String event_id){

        SQLiteDatabase db = this.getWritableDatabase();    	
    	ArrayList <String> participantMap = new ArrayList <String>();
		
		String FullQuery =	  "  SELECT DISTINCT * "
							+ "  FROM " 
							+ 		YOUR_EVENTS_PARTICIPANT_TABLE 
							+ "  WHERE Event_id =  "
							+ event_id
							+ " ;"
							;
							
		//READ QUERY RESULTSET
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		
		if(cursor != null && cursor.getCount() > 0) do {

			String loadVals = cursor.getString(1)
								+ "," + cursor.getString(2)
								+ "," + cursor.getString(3)
								+ "," + cursor.getString(4)
								;
			participantMap.add(loadVals);
			Log.d("loadVals", loadVals);
		
		} while (cursor.moveToNext());
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
		
		return participantMap;
	}

    //DELETE ROWS
    public void deleteRowsHard(String tableName
					    		, ArrayList<String> whereName
					    		, ArrayList<String> whereVals
					    		){

        SQLiteDatabase db = this.getWritableDatabase();
    	String whereString = "";
    	if (whereName != null && whereName.size() > 0)
    		for(int i = 0; i < whereName.size(); i++)
    	    	if (	whereName.get(i).toString()  != null 
    	    			&& whereVals.get(i).toString()  != null
    	    			&& whereName.get(i).toString()  != "" 
    	    			&& whereVals.get(i).toString()  != ""
    	    		)
	    			whereString = whereString 
					    			+ "  AND " 
					    			+ whereName.get(i).toString() 
					    			+ " = " 
					    			+ whereVals.get(i).toString() 
					    			;		
    	String KILL_TABLE = 
        		"DELETE FROM "
        		+ tableName
        		+ "  WHERE 1=1 "
        		+ whereString
        		+ ";"
        		;
        db.execSQL(KILL_TABLE);	
		Log.d("DELETED", KILL_TABLE);
	}
    

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
    
}
