package com.pintr.androidapp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class Pintr_common_support_functions extends Activity  {

	
	//POPULATE  DATE SPINNERS
		public void setDateSpinnerValues(Spinner YearSpinner, Spinner MonthSpinner, Spinner  DaySpinner){
			
			//Year spinner
			int year = Calendar.getInstance().get(Calendar.YEAR);
			List<String> yearSpinnerList = new ArrayList<String>();
			for(int i = year; i >= 2000; i--){
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

	
	
	
}
