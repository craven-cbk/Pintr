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
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Pintr_04_forgotten_login_page extends Activity  {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pintr_04_forgotten_login);
		
		final Button ResendPasswordButton = (Button)findViewById(R.id.ResendPasswordButton);
		final TextView emailLabel = (TextView )findViewById(R.id.emailLabel);
		final EditText email = (EditText)findViewById(R.id.emailInput);
		
		ResendPasswordButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {			
				String emailGiven = email.getText().toString();
				sendEmailToServer(emailGiven);
				emailLabel.setText("Thanks.  If you are already registered, we'll send an email to your account.");
			}
		});
	}
	
	//SEND EMAIL ADDRESS TO MAILER SCRIPT
	public void sendEmailToServer(String emailInput){

		String answerdata = "";
		StringBuilder sb = new StringBuilder();
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 
		
		try {
			answerdata = 		URLEncoder.encode("emailInput", "UTF-8") 
								+ "=" + URLEncoder.encode(emailInput, "UTF-8")
								;
			} catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
		
		try {
			String link = "http://www.pickmemycar.com/pintr/Flogin_mailer.php";
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
		
	}

		
}
