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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;


public  class Pintr_05_ConfirmRegistration extends Activity {


	//INITIALISE THE FORM COMPONENTS
	TextView DisplayLabel;
	Spinner Answer1Spinner , Answer2Spinner ;
	Button SubButton, RefreshImgsButton;
	ImageView Question1 ,  Question2;
	String emailGiven;
	
	final Pintr_G_016_StatusHandler qman = new Pintr_G_016_StatusHandler(this);
	final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
	final Pintr_MSG_001_DownloadMesssageHistory dlmh = new Pintr_MSG_001_DownloadMesssageHistory();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_05_registration_confirmation);
		
		//GET THE REGISTRATION INFORMATION ENTERED IN THE PREVIOUS FORM
		Bundle extras = getIntent().getExtras();
		final String DateOfBirth  = extras.getString("DateOfBirth");
		final String FirstName  = extras.getString("FirstName");
		final String LastName  = extras.getString("LastName");
		final String Email  = extras.getString("Email");
		final String PasswordStr  = extras.getString("PasswordStr");
		final String Gender = extras.getString("Gender");
		final String HandlerName = extras.getString("Handler");
		final String location = extras.getString("location");

		//INITIALISE THE FORM COMPONENTS
		DisplayLabel = (TextView)findViewById(R.id.displayLabel);
		Answer1Spinner = (Spinner)findViewById(R.id.idimg1Spinner);
		Answer2Spinner = (Spinner)findViewById(R.id.idimg2Spinner);
		SubButton = (Button)findViewById(R.id.SubRegConfirmationButton);
		RefreshImgsButton =  (Button)findViewById(R.id.RefreshImgsButton);
		Question1 = (ImageView)findViewById(R.id.idImageView1);
		Question2 = (ImageView)findViewById(R.id.idImageView2);
		emailGiven = "testeremail@thisisatest.com";

		//LOAD QUESTION IMAGES ON PAGE LOAD
		loadQuestionImages(emailGiven);
		
		//CREATE DROPDOWN SPINNER VALUES ARRAY	
		List<String> answerSpinnerList = new ArrayList<String>();
		answerSpinnerList.add("Umbrella");
		answerSpinnerList.add("Horse");
		answerSpinnerList.add("Teddybear");
		answerSpinnerList.add("Cupcake");
		answerSpinnerList.add("Tree");
		answerSpinnerList.add("Cat");
		answerSpinnerList.add("Bicycle");
		answerSpinnerList.add("House");
		answerSpinnerList.add("Armchair");
		answerSpinnerList.add("Door");
		
		//POPULATE ANSWER SPINNERS
		ArrayAdapter<String> AnswerRangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, answerSpinnerList);
		AnswerRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Answer1Spinner.setAdapter(AnswerRangeAdapter );	
		Answer2Spinner.setAdapter(AnswerRangeAdapter );	
		
		
		//SET REGISTRATION BUTTON FUNCTION
		SubButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	

				String returnCode = submitAnswers(emailGiven);

				//IF THE IMAGES WERE IDENTIFIED CORRECTLY, REGISTER THE USER
				if (returnCode.equals("true")){
		    		
					RegisterUserOnDatabase(
										Email 
										, FirstName
										, LastName
										, PasswordStr
										, DateOfBirth
										, Gender 
										, HandlerName
										, location
										);
					
				}
				else{
					DisplayLabel.setText("Sorry, those images don't match");
				}
			}
		});
		
		//REFRESH/CHANGE THE IMAGES IF THE USER ASKS FOR IT
		RefreshImgsButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {

				loadQuestionImages(emailGiven);
			}
		});	
	}
	
	
	
	public void loadQuestionImages(String emailInput){

		 //GET THE IMAGE LOCATIONS FOR THE IMAGES TO DISPLAY
		String imageURLs = RequestImgs(emailInput);
		String[] imgs = imageURLs.split(",");
		String img1 = imgs[0];
		String img2 = imgs[1]; 

		//DISPLAY THE IMAGES IN THE PAGE
		new Pintr_G_006_ImageLoader(Question1).execute(img1);
		new Pintr_G_006_ImageLoader(Question2).execute(img2);
	}
	
	
	public String submitAnswers(String emailInput){

		String Answer1 = Answer1Spinner.getSelectedItem().toString(); 
		String Answer2 = Answer2Spinner.getSelectedItem().toString();
		
		String link = null;
		String answerdata = null;
		StringBuilder sb = new StringBuilder();
		

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		try {
			answerdata = 	URLEncoder.encode("emailInput", "UTF-8") 
					+ "=" + URLEncoder.encode(emailInput, "UTF-8")
					+ "&" + URLEncoder.encode("answer1", "UTF-8") 
					+ "=" + URLEncoder.encode(Answer1, "UTF-8")
					+ "&" + URLEncoder.encode("answer2", "UTF-8") 
					+ "=" + URLEncoder.encode(Answer2, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		
		//SUBMIT THE QUESTION ANSWERS AND GET THE TRUE/FALSE RESPONSE
		try {
			link = "http://www.pickmemycar.com/pintr/CPT_response.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write(answerdata);
			SendToPhp.flush();  

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            // RETRIEVE SERVER RESPONSE
            while((line = reader.readLine()) != null)
	            {
	               sb.append(line);
	               break;
	            }
			}
		
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		String returnCode = sb.toString().trim();
		return returnCode;
	}
		
	
	public String RequestImgs(String emailInput){

		String link = null;
		String data = null;
		StringBuilder sb = new StringBuilder();
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		try {
			data = 	URLEncoder.encode("emailInput", "UTF-8") 
					+ "=" + URLEncoder.encode(emailInput, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/imagePicker.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write(data);
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
		
		return returnCode;
	}

	public String RegisterUserOnDatabase(String EmailAddressGiven
										,String firstNameGiven
										,String lastNameGiven
										,String PasswordGiven
										,String DoBGiven
										,String GenderGiven
										,String HandlerName
										,String location
										){
		
		String data = null;
		String link = null;
		StringBuilder sb = new StringBuilder();
		
		Log.d("HandlerName",HandlerName );
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(EmailAddressGiven, "UTF-8")
					+ "&" + URLEncoder.encode("firstNameGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(firstNameGiven, "UTF-8")
					+ "&" + URLEncoder.encode("lastNameGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(lastNameGiven, "UTF-8")
					+ "&" + URLEncoder.encode("PasswordGiven", "UTF-8") 
					+ "=" + URLEncoder.encode(PasswordGiven, "UTF-8")
					+ "&" + URLEncoder.encode("dob", "UTF-8") 
					+ "=" + URLEncoder.encode(DoBGiven, "UTF-8")
					+ "&" + URLEncoder.encode("gender", "UTF-8") 
					+ "=" + URLEncoder.encode(GenderGiven, "UTF-8")
					+ "&" + URLEncoder.encode("handler", "UTF-8") 
					+ "=" + URLEncoder.encode(HandlerName, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/PintrRegistration.php";
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
		
		String [] returnCodeArray = sb.toString().split("@");
		
		String returnCode = returnCodeArray[0];
		Log.d("RETURNODE REG ATTEMPT", " =>" + sb.toString());
		
		//RETURN CODES:
		//  50001 - REGISTRATION SUCCESSFUL
		//  50099 - REG FAILED - EMAIL EXISTS
		if (returnCode.equals("50001")){
			
			final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
			dbq.MakeUserDetailsTable( 
					firstNameGiven
					, lastNameGiven
					, DoBGiven
					, GenderGiven
					);
			
			dlmh.sendReadTimeToServer( this
					, 0
		            ,  EmailAddressGiven
		            ,  PasswordGiven
		            ,  "2000/01/01 00:00:00.000"
					);

			Class<?> refCL = Pintr_05_ConfirmRegistration.class;
			SharedPreferences prefs = getSharedPreferences(refCL.getSimpleName(),Context.MODE_PRIVATE);
			final Context context = this;
			
			Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
			
			//GET DEVICE APP GCM REG ID
			Pintr_NT_02_LoadWebPageASYNC task 
					= new Pintr_NT_02_LoadWebPageASYNC(context
														, prefs
														, EmailAddressGiven
														, PasswordGiven
														, gcmuf.getDeviceId(this , getContentResolver())
														);
    		task.execute();
			
			SimpleDateFormat simpleDateFormat 
					= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
			
			Calendar now = Calendar.getInstance();
			String timeNow  = simpleDateFormat.format(now.getTime());
			
			Pintr_G_016_StatusHandler dbsh = new Pintr_G_016_StatusHandler(this);
			dbsh.JSONStatusUpload(
					EmailAddressGiven
					,PasswordGiven
					,1
					,location
					,timeNow  
					,0
					,0
					);
			DisplayLabel.setText("Registration successful");
			SendRegEmail(EmailAddressGiven);
			
			registration_procedure(EmailAddressGiven, PasswordGiven, returnCodeArray[1], returnCodeArray[2]);
			
			return "";
		}
		else if (returnCode.equals("50099")){
			return "Email address already exists";
		}
		else {
			return "Undetermined error:  " + returnCode  ;
		}
	}

	
	public void registration_procedure(String Lo_1, String Lo_2, String Lo_3, String Lo_4){

		final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
  		dbq.RegisterUserOnBoard(Lo_1, Lo_2, Lo_3, Lo_4);
  		
	    Intent AutoRegOnBoard = new Intent(this, Pintr_10_InterestsOverview.class);
	    AutoRegOnBoard.putExtra("ORIGIN", "REGISTRATION");
	    startActivity(AutoRegOnBoard);		
	}
	
	
	public String SendRegEmail(String email_input){

		String data = null;
		String link = null;
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		String line = "";
		
		try {
			data = 	URLEncoder.encode("email", "UTF-8") 
					+ "=" + URLEncoder.encode(email_input, "UTF-8")
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/Reg_mailer.php";
			URL url = new URL(link);  
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter SendToPhp = new OutputStreamWriter(conn.getOutputStream());
			SendToPhp.write( data ); 
			SendToPhp.flush();  
			
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            line  = reader.readLine();
            
			}
		
		catch (MalformedURLException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
	
		return line;
	}
	
	
	
}
