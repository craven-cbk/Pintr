package com.pintr.androidapp;

import java.util.ArrayList;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 

public  class Pintr_G_017_ProfilesDBMaker extends SQLiteOpenHelper {

	//DATABASE CONFIG ELEMENTS:
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Pintr_DB";
	private static final String PEOPLE_TABLE_NAME = "P_EK_PPL";
		
	//INITIALISE AND CREATE DATABASE
	public Pintr_G_017_ProfilesDBMaker(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //UPDATE TABLE
    public void peopleUpdate(int pintr_user_id
							,String handle
							, String status
							){
		ArrayList<String> itemNames = new ArrayList<String>();
		ArrayList<String> itemValues = new ArrayList<String>();

		itemNames.add("pintr_user_id");
		itemNames.add("handle");
		itemNames.add("status");
		itemValues.add(Integer.toString(pintr_user_id));
		itemValues.add(handle);
		itemValues.add(status);
		
		TableRowUpdater(PEOPLE_TABLE_NAME
						,itemNames 
						,itemValues
						,"pintr_user_id"
						,Integer.toString(pintr_user_id)
						,status
						);
	}

    //READ TABLE
	public ArrayList<String> peopleReader(String statusRequested){
		
		String FullQuery = "SELECT * FROM " 
							+ PEOPLE_TABLE_NAME  
							+ " WHERE status = '" 
							+ statusRequested 
							+ "';"
							;
	    ArrayList<String> Creds = new ArrayList<String>();

        SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(FullQuery, null);
		cursor.moveToFirst();
		int size = cursor.getCount();
		Log.d("Resultset Size:", Integer.toString(size));
		if(size>0){
			do {
				int pintr_user_id = cursor.getInt(0);
				String handle = cursor.getString(1);
				String status = cursor.getString(2);
				
				String row = Integer.toString(pintr_user_id)
						+ "," + handle
						+ "," + status
						;
				Log.d("ROW READ FROM PEOPLE TABLE:", row );
			    Creds.add(row);
			} while (cursor.moveToNext());
		}
		
		//TIDY UP - CLOSE CONNECTIONS
		cursor.close();
		db.close();	
	    return Creds;
	}

	//UPDATE TABLE
	public void TableRowUpdater(String tableName
				    		, ArrayList<String> itemNames
				    		, ArrayList<String> itemValues
				    		, String keyname
				    		, String keyval
				    		, String status_to_delete
				    		){
        SQLiteDatabase db = this.getWritableDatabase();
		ContentValues insert_row_values= new ContentValues();
		int tableSize = itemNames.size();
		
		for (int i = 0; i < tableSize; i++)
			insert_row_values.put(itemNames.get(i), itemValues.get(i));
		
		db.insert(tableName, null, insert_row_values);
		db.close();	
	}
	
	
	//EMPTY TABLE
	public void EmptyTable(String status){

        SQLiteDatabase db = this.getWritableDatabase();
		String KILL_TABLE = 
	    		"DELETE FROM "
	    		+ PEOPLE_TABLE_NAME
	    		+ " WHERE status = '" + status + "';"
	    		;
	    db.execSQL(KILL_TABLE);
		db.close();		
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
	    
}