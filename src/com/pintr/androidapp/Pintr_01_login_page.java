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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Pintr_01_login_page extends Activity  {
	
	TextView errorMessagingField;
	LayoutParams errorMessagingFieldParams;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_01_login);

		LinearLayout offlineZone = (LinearLayout)findViewById(R.id.offlineZone);

		final Pintr_NW_01_NetworkConnectionChecker nw = new Pintr_NW_01_NetworkConnectionChecker();
		Boolean networkAvailable = nw.haveNetworkConnection(this);
		if(networkAvailable==false){
			offlineZone.setVisibility(View.VISIBLE);
		}
		errorMessagingField = (TextView)findViewById(R.id.ErrorDisplay);
		errorMessagingField.setVisibility(View.INVISIBLE);	
		errorMessagingFieldParams = errorMessagingField.getLayoutParams();

		Button Login = (Button)findViewById(R.id.LoginButton);
		Button Register = (Button)findViewById(R.id.RegisterButton);
		Button ForgottenLogin = (Button)findViewById(R.id.ForgottenLoginButton);

		Login.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {		
				loginAction();
			}
		});	

		Register.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {			
				registerAction();
			}
		});

		ForgottenLogin.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				flogin();
			}
		});
		
		
		EditText passwordEntered = (EditText)findViewById(R.id.passwordInput);
		passwordEntered.setOnEditorActionListener(new TextView.OnEditorActionListener() { 
		    @Override 
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { 		
				loginAction();
	            return true; 
		    } 
		}); 
		
	}
	
	public void flogin(){

		startActivity(new Intent(this, Pintr_04_forgotten_login_page.class));
	}
	
	public void loginAction(){

		EditText emailEntered = (EditText)findViewById(R.id.emailInput);
		EditText passwordEntered = (EditText)findViewById(R.id.passwordInput);
		String email, password;
		
		email = emailEntered.getText().toString().trim();
		password = passwordEntered.getText().toString().trim();
		
		if (email.indexOf("@") < 1
			||email.indexOf(".") < 0
			||email.length() == 0
			)
		{
			errorMessagingField.setVisibility(View.VISIBLE);
			errorMessagingField.setText("Please enter a valid email address");
		}
		
		else if(password.length()  < 4){
			errorMessagingField.setVisibility(View.VISIBLE);
			errorMessagingField.setText("Password is too short");
		}
		
		else{
			String result = LoginUserActivity(email , password);
			errorMessagingField.setVisibility(View.VISIBLE);
			errorMessagingField.setText(result);
		}
	}

	public void registerAction(){
		startActivity(new Intent(this, Pintr_02_registration_page.class));
		
	}

	public void floginAction(){
		startActivity(new Intent(this, Pintr_02_registration_page.class));
		
	}

	public String LoginUserActivity(
			String EmailAddressGiven
			,String PasswordGiven
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
					;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		//CONNECT TO THE TARGET PHP PAGE URL ON THE SERVER AND WRITE REGISTRATION DETAILS
		try {
			link = "http://www.pickmemycar.com/pintr/PintrLogin.php";
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

		Log.d("RETURNCODE", sb.toString());
		String [] returnCodeArray = sb.toString().trim().split(",");
		
		String returnCode = returnCodeArray[0];
		
		//RETURN CODES:
		//  50001 - REGISTRATION SUCCESSFUL
		//  50099 - REG FAILED - EMAIL EXISTS
		
		String ReturnMessage = "";
		if (returnCode.equals("10001")){

			ReturnMessage = "Login Successful";
			Log.d("UID RETURNED", returnCodeArray[1]);
			Log.d("HANDLE RETURNED", returnCodeArray[2]);
			login_procedure(EmailAddressGiven, PasswordGiven,returnCodeArray[1], returnCodeArray[2]);
		}
		else if (returnCode.equals("10099")){
			ReturnMessage =  "Email or password does not exist";
		}
		else if (returnCode.equals("10092")){
			ReturnMessage = "Registration error";
		}
		else if (returnCode.equals("10024")){
			ReturnMessage = "Database error...";
		}
		else {
			ReturnMessage = "Undetermined error:  " + returnCode  ;
		}
		
		return ReturnMessage ;
	}
	
	public void login_procedure(String  Lo_1, String Lo_2, String Lo_3, String Lo_4){
		errorMessagingField.setVisibility(View.VISIBLE);
		errorMessagingField.setText("Login Successful");
	
		//ADD USER DETAILS TO SQLITE DB
		Pintr_G_001_DBMaker db = new Pintr_G_001_DBMaker(this);
		final Pintr_G_002_DBQuery dbq = new Pintr_G_002_DBQuery(this);
  		dbq.RegisterUserOnBoard(Lo_1, Lo_2, Lo_3, Lo_4);
		
	    Intent AutoRegOnBoard = new Intent(this, Pintr_PL_01_PostLoginPreLoad.class);
	    startActivity(AutoRegOnBoard);		
	    
	}
}
