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

import android.content.Context;
import android.os.StrictMode;


public  class Pintr_G_007_DtlsChgr {

    public Pintr_G_007_DtlsChgr() {
    }


	public String GetUserDetails(String EmailAddressGiven,String PasswordGiven){

		StringBuilder sb = new StringBuilder();
		//List<String> outputList = null;

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 

		String data = null;
		String link = null;
		String output = null;
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(EmailAddressGiven, "UTF-8")
					+ "&" + URLEncoder.encode("PasswordGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(PasswordGiven, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/GetUserDtls.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
		
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		output = sb.toString();
		return output;
	}
	

	public String ChgUserDetailsOnDatabase(
			String EmailAddressGiven
			,String PasswordGiven
			,String firstNameGiven
			,String lastNameGiven
			,String DoBGiven
			,String GenderGiven
			,Context context
		){
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder();


		//THIS IS WHAT WAS MISSING:  BECAUSE THE TASK IS EECUTING ASYNCHRONOUSLY, 
		// THE TASK FINISHES BEFORE THE OUTPUT HAS A CHANCE TO BE RETRIEVED... (?)
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(EmailAddressGiven, "UTF-8")
					+ "&" + URLEncoder.encode("PasswordGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(PasswordGiven, "UTF-8")
					+ "&" + URLEncoder.encode("firstNameGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(firstNameGiven, "UTF-8")
					+ "&" + URLEncoder.encode("lastNameGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(lastNameGiven, "UTF-8")
					+ "&" + URLEncoder.encode("dob", "UTF-8") 
					+ "=" + URLEncoder.encode(DoBGiven, "UTF-8")
					+ "&" + URLEncoder.encode("gender", "UTF-8") 
					+ "=" + URLEncoder.encode(GenderGiven, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/ChgUserDtls.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
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
		
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		String returnCode = sb.toString().trim();
		
		//RETURN CODES:
		//  50001 - REGISTRATION SUCCESSFUL
		//  50099 - REG FAILED - EMAIL EXISTS
		if (returnCode.equals("60001")){
			
			final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(context);
			dbq.MakeUserDetailsTable( 
					firstNameGiven
					, lastNameGiven
					, DoBGiven
					, GenderGiven
					);
			
			return "Details changed successfully";
		}
		else if (returnCode.equals("60999")){
			return "There was an error changing your details";
		}
		else {
			return "Undetermined error  " ;
		}
	}
	
	
	//CHANGE USER PASSWORD IF USER DECIDES TO DO SO
	public String chpw(String email
						, String uid
						, String pw
						, String newpw
						, Context context){
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 

		String data = null;
		String link = null;
		String returncode="";

		Pintr_G_001_DBMaker dbk = new Pintr_G_001_DBMaker(context);
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email, "UTF-8")
					+ "&" + URLEncoder.encode("pw", "UTF-8") 
					+ "=" + URLEncoder.encode(pw, "UTF-8")
					+ "&" + URLEncoder.encode("newpw", "UTF-8")  
					+ "=" + URLEncoder.encode(newpw, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/CHPD.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write( data ); 
			SendToPhp.flush();  

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            returncode = line;
			
		}
		
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}

		//UPDATE THE SQLITE DB
		dbk.UserCredsUpdate( 
				email
				, newpw
				, uid
				);

		if(returncode.equals("SuccessPROCESS END")){
			return  "Password changed successfully";
		}
		else{
			return  "Password not changed ";
		}
	}
	
}