package com.pintr.androidapp;


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
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;


public class Pintr_07_ChangeDetails extends Activity  implements OnItemSelectedListener   {

	Spinner dob_YearSpinner, dob_MonthSpinner, dob_DaySpinner, GenderSpinner;
	Button SubmitButton;
	EditText firstname, lastname ;
	TextView ErrorMessaging;
	int user_daydob = 0;
	Pintr_G_007_DtlsChgr dtls = new Pintr_G_007_DtlsChgr();
	Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_07_change_details);


		//******  MENU MANAGER:  ******
		menumanager();
		settingsMenumanager(this);
		
		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);
		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		
		//****  END MENU MANAGER  ******
		
		
		ArrayList<String> UserCreds = dbq.UserCreds();
		final String EmailAddressGiven = UserCreds.get(0);
		final String PasswordGiven = UserCreds.get(1);
		
		//DEFINE & POPULATE DOB DATE SPINNERS
		dob_YearSpinner = (Spinner)findViewById(R.id.dob_Year);
		dob_MonthSpinner = (Spinner)findViewById(R.id.dob_Month);
		dob_DaySpinner = (Spinner)findViewById(R.id.dob_Day);
		GenderSpinner = (Spinner)findViewById(R.id.GenderSpinner);

		firstname = (EditText)findViewById(R.id.firstnameInput);
		lastname = (EditText)findViewById(R.id.lastnameInput);
		ErrorMessaging = (TextView)findViewById(R.id.ErrorMessgagingLabel);
		
		
		SubmitButton = (Button)findViewById(R.id.SubmitButton);
		
		
		//GET USER DETAILS FROM DB
		String [] DetailsArray = dtls.GetUserDetails(EmailAddressGiven, PasswordGiven).split(",");

		int dob_day = Integer.parseInt(DetailsArray[2].substring(8,10)) - 1;
		int dob_month = Integer.parseInt(DetailsArray[2].substring(5,7)) - 1;
		int dob_year = Integer.parseInt(DetailsArray[2].substring(0,4));

		int year = Calendar.getInstance().get(Calendar.YEAR);
		int dob_yearIndex = year - dob_year;

		user_daydob = dob_day;

		//POPULATE SPINNERS
		setDateSpinnerValues(dob_YearSpinner ,dob_MonthSpinner, dob_DaySpinner, GenderSpinner);

		//SET MONTHSPINNER LISTENER, TO SET THE DAY SPINNER VALUES
		dob_MonthSpinner.setOnItemSelectedListener(this);
		
		dob_YearSpinner.setSelection(dob_yearIndex);
		dob_MonthSpinner.setSelection(dob_month);
		dob_DaySpinner.setSelection(dob_day);	
		

		dob_YearSpinner.setSelection(dob_yearIndex);

		if(DetailsArray[3].equals("M")){
			GenderSpinner.setSelection(0);			
		}
		else if(DetailsArray[3].equals("F")){
			GenderSpinner.setSelection(1);			
		}
		
		//DISPLAY DETAILS IN FORM
		firstname.setText(DetailsArray[0]);
		lastname.setText(DetailsArray[1]);		
		
		//SET REGISTRATION BUTTON FUNCTION
		SubmitButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				String OutcomeString = HarvestUserDetails(EmailAddressGiven, PasswordGiven);
				ErrorMessaging.setText(OutcomeString);
				
				if(OutcomeString.equals("Details changed successfully")){
					ChangePage();
				}
			}
		});	
	
	}
	
		
	//WHEN REGISTER BUTTON IS PRESSED, DO THE FOLLOWING:
	public String HarvestUserDetails(String EmailAddressGiven
									, String PasswordGiven){


		Pintr_G_007_DtlsChgr dtls = new Pintr_G_007_DtlsChgr();
		
		//MAKE DOB STRING
		String dob_Year_str = dob_YearSpinner.getSelectedItem().toString(); 
		String dob_Month_str = dob_MonthSpinner.getSelectedItem().toString(); 
		String dob_Day_str =  dob_DaySpinner.getSelectedItem().toString(); 
		String DateOfBirth = 	dob_Year_str
								+ "-"
								+ String.format("%02d",Integer.parseInt(dob_Month_str))
								+ "-"
								+ String.format("%02d",Integer.parseInt(dob_Day_str))
								+ " "
								+ " 00:00:00.000"
								;
		
		// GET USER NAME, GENDER, PASSWORD & EMAIL
		String FirstName = firstname.getText().toString();
		String LastName = lastname.getText().toString();
		String Gender = GenderSpinner.getSelectedItem().toString();
		
		// GET USER AGE
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss",Locale.getDefault());
		Date DOBdate = null;
		try {DOBdate = simpleDateFormat.parse(DateOfBirth);
		} catch (ParseException e) {e.printStackTrace();}
		
		Date currentTime = Calendar.getInstance().getTime();
		Long AgeInSecs = (currentTime.getTime() - DOBdate.getTime())/1000;
		Long AgeInYears = AgeInSecs / (365 * 24 * 3600);
		
		// 1.  CHECK NAME FOR VALIDITY
		if(	FirstName.length() < 1
			||	LastName.length() < 1
			){
			return "Please input a valid name and surname";
			
		}
		//2.  IF AGE IS LESS THAN 16 YEARS OLD, REJECT
		else if(AgeInYears < 16
				){
			return "At " + AgeInYears + " years old, you are too young to join.";			
		}
		
		// 3. IF EVERYTHING IS FINE, CHANGE THE REGISTERED DETAILS
		else{

			return dtls.ChgUserDetailsOnDatabase(EmailAddressGiven
										,PasswordGiven
										,FirstName
										,LastName
										,DateOfBirth
										,Gender
										,this
										);
		}
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
		for(int i=1; i < 32; i++){
			daySpinnerList.add(Integer.toString(i));		
		}
		ArrayAdapter<String> DayrangeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, daySpinnerList);
		DayrangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		DaySpinner.setAdapter(DayrangeAdapter );
					
	}
	
	@Override
	public void onItemSelected(AdapterView<?> spinner, View container, int position, long id) {
    	int monthNumSelected = position + 1;
    	int NumberOfDaysInMonthSelected = 31;
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
		dob_DaySpinner.setAdapter(dayrangeAdapter );
		
		if(user_daydob != 0){
			dob_DaySpinner.setSelection(user_daydob);				
		}
	
    }

	public void ChangePage(){

		startActivity(new Intent(this, Pintr_03_entry_page.class));
	}

    @Override
	public void onNothingSelected(AdapterView<?> arg0) {
    }

//	REACT TO ITEMS PRESSED IN THE MENU DROP-DOWN
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    Intent MenuIntent = new Intent(this, Pintr_G_005_MenuManager.class);
	    int MenuItemId = item.getItemId();
	    
	    if (MenuItemId==android.R.id.home) {
	    	startActivity(new Intent(this, Pintr_03_entry_page.class));
	    }    
	    else{
 
	    	MenuIntent.putExtra("menuitem", MenuItemId);
		    startActivity(MenuIntent);
	    }
		return true;
	}
	
	public void menumanager(){		
		//MENU MANAGER:
		final ToggleButton menuButton = (ToggleButton)findViewById(R.id.menuToggle);
		final LinearLayout menuLayout = (LinearLayout)findViewById(R.id.menuLayout);
		final LinearLayout homeButton = (LinearLayout)findViewById(R.id.homeButton);
		final LinearLayout converseButton = (LinearLayout)findViewById(R.id.converseButton);
		final LinearLayout friendsButton = (LinearLayout)findViewById(R.id.friendsButton);
		final LinearLayout messagesButton = (LinearLayout)findViewById(R.id.messagesButton);
		final LinearLayout eventsButton = (LinearLayout)findViewById(R.id.eventsButton);
		final LinearLayout lfpButton = (LinearLayout)findViewById(R.id.lfpButton);
		final View entryPageMain = (View)findViewById(R.id.entryPageMain);

		Resources res = getResources();
		int colorIn =  res.getColor(R.color.MenuButtonBckgdPress);
		int colorOut =  res.getColor(R.color.CharcoalGreyBckGd);

		int unreadMessages = dbq.unreadMsgCt();
		int FriendRequestsCount = dbq.getFriendRequestsCount();
		
		Pintr_G_099_HeaderMenuManager pmm = new Pintr_G_099_HeaderMenuManager(this
											,menuLayout  
											,homeButton  
											,converseButton  
											,friendsButton  
											,messagesButton  
											,eventsButton 
											,colorIn 
											,colorOut
											,menuButton
											,entryPageMain
											,unreadMessages
											,FriendRequestsCount 
											,lfpButton
											);
		
		homeButton.setOnClickListener(pmm.homeClick);
		converseButton.setOnClickListener(pmm.converseClick);
		friendsButton.setOnClickListener(pmm.friendsClick);
		messagesButton.setOnClickListener(pmm.messagesClick);
		eventsButton.setOnClickListener(pmm.interestsClick);
		lfpButton.setOnClickListener(pmm.lfpClick);
		
	}

	
	public void settingsMenumanager(Context context){

		Resources res = getResources();
		int colorIn =  res.getColor(R.color.MenuButtonBckgdPress);
		int colorOut =  res.getColor(R.color.CharcoalGreyBckGd);

		LinearLayout logoutButton = (LinearLayout)findViewById(R.id.logoutButton);
		LinearLayout settingsButton = (LinearLayout)findViewById(R.id.settingsButton);
		LinearLayout interestsButton = (LinearLayout)findViewById(R.id.interestsButton);
		
		Pintr_G_096_BottomMenuManager bsm = new Pintr_G_096_BottomMenuManager(
				(LinearLayout)findViewById(R.id.settingsMenuLayout)
				,logoutButton
				,settingsButton
				,interestsButton
				,context
				,colorIn
				,colorOut
				);
		
		final ToggleButton menuButton = (ToggleButton)findViewById(R.id.menuToggle);
		menuButton.setOnCheckedChangeListener(bsm.menuCheckList);
		
		logoutButton.setOnClickListener(bsm.logoutClick);
		settingsButton.setOnClickListener(bsm.settingsClick);
		interestsButton.setOnClickListener(bsm.interestsClick);
		
	}

//	CUSTOM USER MENU MANAGER
	@Override
	public boolean onKeyDown(int keycode, KeyEvent e) {
		
		final LinearLayout menuLayout = (LinearLayout)findViewById(R.id.settingsMenuLayout);
		
	    switch(keycode) {
        case KeyEvent.KEYCODE_MENU:
        	if (menuLayout.getVisibility() == View.VISIBLE) {
    			menuLayout.setVisibility(View.INVISIBLE);
    		} else {
    			menuLayout.setVisibility(View.VISIBLE);
    		}
    		return true;
    }

    return super.onKeyDown(keycode, e);
	}



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbq != null) {
        	dbq.close();
        }
    }  
}