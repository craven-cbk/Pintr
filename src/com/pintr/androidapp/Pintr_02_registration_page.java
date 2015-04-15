package com.pintr.androidapp;


import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;


public class Pintr_02_registration_page extends Activity  implements OnItemSelectedListener   {

	Spinner dob_Year, dob_Month, dob_Day, GenderSpinner;
	Button RegisterButton;
	EditText  email, Password, rptPassword, handleName, locationEntry ;
	TextView ErrorMessaging;
	Pintr_G_013_WordDenialList DNL = new Pintr_G_013_WordDenialList();
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_02_registration);
	
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);

		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}

		//GRAB LOCATION
		networkAvailable = nw.haveNetworkConnection(this);
		LocationManager mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		LocationListener mlocListener = new Pintr_G_097_02_NetworkLocation (this, this);
		if(networkAvailable){
			mlocManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, mlocListener);
		}
		
		//DEFINE & POPULATE DOB DATE SPINNERS
		dob_Year = (Spinner)findViewById(R.id.dob_Year);
		dob_Month = (Spinner)findViewById(R.id.dob_Month);
		dob_Day = (Spinner)findViewById(R.id.dob_Day);
		GenderSpinner = (Spinner)findViewById(R.id.GenderSpinner);

		handleName = (EditText)findViewById(R.id.handlerInput);
		email = (EditText)findViewById(R.id.emailInput);
		Password = (EditText)findViewById(R.id.passwordInput);
		rptPassword = (EditText)findViewById(R.id.repeatpasswordInput);
		ErrorMessaging = (TextView)findViewById(R.id.ErrorMessgagingLabel);
		
		
		RegisterButton = (Button)findViewById(R.id.RegisterButton);
		
		setDateSpinnerValues(dob_Year ,dob_Month, dob_Day, GenderSpinner);
       
		
		//SET MONTHSPINNER LISTENER, TO SET THE DAY SPINNER VALUES
		dob_Month.setOnItemSelectedListener(this);
		
		//SET REGISTRATION BUTTON FUNCTION
		RegisterButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {	
				RegisterUser();
			}
		});	
	
	}
		
	public void listLocat(Location loc, Context context){
		loc.getLatitude();
		loc.getLongitude();

	    Geocoder geocoder = new Geocoder(context, Locale.getDefault());   
	    String result = null;
	    try {
	        List<Address> list = geocoder.getFromLocation(
	        		loc.getLatitude(), loc.getLongitude(), 1);
	        if (list != null && list.size() > 0) {
	            Address address = list.get(0);
	            result = address.getAddressLine(1) + ", " + address.getAddressLine(2);
	        }
	        else{
	        }
	    } catch (IOException e) {    } 

	    if(result.equals(null)){
	    	result = "Location unobtainable";
	    }
	    locationEntry = (EditText)findViewById(R.id.locationInput);
	    locationEntry.setText(result);
	}
	
	//WHEN REGISTER BUTTON IS PRESSED, DO THE FOLLOWING:
	public void RegisterUser(){
 
		//MAKE DOB STRING
		String dob_Year_str = dob_Year.getSelectedItem().toString(); 
		String dob_Month_str = dob_Month.getSelectedItem().toString(); 
		String dob_Day_str =  dob_Day.getSelectedItem().toString(); 
		String DateOfBirth = 	dob_Year_str
								+ "-"
								+ String.format("%02d",Integer.parseInt(dob_Month_str))
								+ "-"
								+ String.format("%02d",Integer.parseInt(dob_Day_str))
								+ " "
								+ " 00:00:00.000"
								;
		
		// GET USER NAME, GENDER, PASSWORD & EMAIL
		String FirstName = "";
		String LastName = "";
		String Email = email.getText().toString();
		String PasswordStr = Password.getText().toString();
		String RepeatPasswordStr = rptPassword.getText().toString();
		String Gender = GenderSpinner.getSelectedItem().toString();
		String HandlerName = handleName.getText().toString();
		String location = locationEntry.getText().toString();
		
		
		
		// GET USER AGE
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss",Locale.getDefault());
		Date DOBdate = null;
		try {DOBdate = simpleDateFormat.parse(DateOfBirth);
		} catch (ParseException e) {e.printStackTrace();}
		
		Date currentTime = Calendar.getInstance().getTime();
		Long AgeInSecs = (currentTime.getTime() - DOBdate.getTime())/1000;
		Long AgeInYears = AgeInSecs / (365 * 24 * 3600);


		// 2. CHECK NAMES FOR VALIDITY
		if (	Email.indexOf("@") < 1
					||Email.indexOf(".") < 0
					||Email.length() == 0
				){
				ErrorMessaging.setText("Please input a valid email address");
		}
		
		//3. CHECK PASSWORD FOR VALIDITY
		else if(PasswordStr.length() == 0){
			ErrorMessaging.setText("Please add a password for your account");			
		}
		else if(PasswordStr.length() < 6){
			ErrorMessaging.setText("Please make a password that is at least 6 characters long");			
		}
		else if(PasswordStr.equals(RepeatPasswordStr) == false){
			ErrorMessaging.setText("Your passwords do not match");			
		}
		else if(PasswordStr.matches(".*\\d+.*") == false
				){
			ErrorMessaging.setText("Please make a password containing at least one number");			
		}
		
		//4.  IF AGE IS LESS THAN 16 YEARS OLD, REJECT
		else if(AgeInYears < 16
				){
			ErrorMessaging.setText("At " + AgeInYears + " years old, you are too young to join.");			
		}
		
		//5.  CHECK HANDLER FOR GENDER REFERENCES
		else if(denialChecker(HandlerName).equals("") == false){
			String displayWord = denialChecker(HandlerName);
			ErrorMessaging.setText("I'm sorry, your handler contains the gender-specific term '" + displayWord + "'");			
		}

		
		// 6. IF EVERYTHING IS FINE, CALL THE BOTCHECKER TO REGISTER THE USER
		else{

		    Intent AutoRegOnBoard = new Intent(this, Pintr_05_ConfirmRegistration.class);
		    AutoRegOnBoard.putExtra("DateOfBirth", DateOfBirth);
		    AutoRegOnBoard.putExtra("FirstName", FirstName);
		    AutoRegOnBoard.putExtra("LastName", LastName);
		    AutoRegOnBoard.putExtra("Email", Email);
		    AutoRegOnBoard.putExtra("PasswordStr", PasswordStr);
		    AutoRegOnBoard.putExtra("Gender", Gender);
		    AutoRegOnBoard.putExtra("Handler", HandlerName);
		    AutoRegOnBoard.putExtra("location", location);
		    startActivity(AutoRegOnBoard);	

		}
	}
	
	//CHECK HANDLE FOR FORBIDDEN WORDS
	public String denialChecker(String HandlerName) {
		String[] list = DNL.denialList();
		int length = list.length;
		String deniedWord = "";
		
		outerloop:
		for (int i = 0; i < length; i++){
			Boolean permissble 
				= HandlerName.toLowerCase(Locale.US).contains(list[i].toLowerCase(Locale.US));
			if (permissble == true ){
				deniedWord = list[i];
				break outerloop;
			}
		}
		return deniedWord;
	}
	
	
	
	//POPULATE  DATE SPINNERS
	public void setDateSpinnerValues(
					Spinner YearSpinner
					, Spinner MonthSpinner
					, Spinner  DaySpinner
					, Spinner  GenderSpinner
					){
		
		//GENDER SPINNER
		List<String> genderSpinnerList = new ArrayList<String>();
		genderSpinnerList.add("Male");
		genderSpinnerList.add("Female");
		ArrayAdapter<String> GenderRangeAdapter 
			= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, genderSpinnerList);
		GenderRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		GenderSpinner.setAdapter(GenderRangeAdapter);	
		
		//Year spinner
		int year = Calendar.getInstance().get(Calendar.YEAR);
		List<String> yearSpinnerList = new ArrayList<String>();
		for(int i = year; i >= 1900; i--){
			yearSpinnerList.add(Integer.toString(i));			
		}
		ArrayAdapter<String> YearrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, yearSpinnerList);
		YearrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		YearSpinner.setAdapter(YearrangeAdapter );	
		
		//Month spinner
		List<String> monthSpinnerList = new ArrayList<String>();
		for(int i=1; i < 13; i++){
			monthSpinnerList.add(Integer.toString(i));			
		}
		ArrayAdapter<String> MonthrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, monthSpinnerList);
		MonthrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		MonthSpinner.setAdapter(MonthrangeAdapter );	
	
	
		//Day spinner
		List<String> daySpinnerList = new ArrayList<String>();
		for(int i=1; i < 31; i++){
			daySpinnerList.add(Integer.toString(i));		
		}
		ArrayAdapter<String> DayrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, daySpinnerList);
		DayrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		DaySpinner.setAdapter(DayrangeAdapter );
					
	}
	
	@Override
	public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
    	int monthNumSelected = position + 1;
    	int NumberOfDaysInMonthSelected = 30;
    	if(monthNumSelected == 1  
	    		||	monthNumSelected == 3 
	    		||	monthNumSelected == 5 
	    		||	monthNumSelected == 7 
	    		||	monthNumSelected == 8 
	    		||	monthNumSelected == 10 
	    		||	monthNumSelected == 12){
    		NumberOfDaysInMonthSelected = 31;
    	}
    	else if(monthNumSelected == 2){
    		NumberOfDaysInMonthSelected = 28;
    	}
    	else {
    		NumberOfDaysInMonthSelected = 30;		    		
    	}

		List<String> daylist = new ArrayList<String>();
		for (int i = 1; i <= NumberOfDaysInMonthSelected; i++){
			daylist.add(Integer.toString(i));			
		}
    	
		ArrayAdapter<String> dayrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, daylist);
		dayrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dob_Day.setAdapter(dayrangeAdapter );
		
		//IF THE MONTH SELECTED IS THIS MONTH THEN SET THE DAY SPINNER TO TODAY'S DATE
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdfMM = new SimpleDateFormat("MM",Locale.getDefault());

		int currmonth = Integer.parseInt(sdfMM.format(cal.getTime()));
		
		if(monthNumSelected == currmonth){

			int currday = cal.get(Calendar.DAY_OF_MONTH) - 1;
			dob_Day.setSelection(currday);
		}

    }


    @Override
	public void onNothingSelected(AdapterView<?> arg0) {
    }
	
}
