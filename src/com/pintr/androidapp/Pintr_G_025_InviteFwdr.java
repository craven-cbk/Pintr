package com.pintr.androidapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import android.content.Context;
import android.util.Log;



public class Pintr_G_025_InviteFwdr {

	final Pintr_G_002_DBQuery dbq;
	String email, password , your_handler ;
	
	public Pintr_G_025_InviteFwdr(Context context){
		dbq = new Pintr_G_002_DBQuery(context);
		ArrayList<String> Creds = dbq.UserCreds();
		email = Creds.get(0).toString();
		password = Creds.get(1).toString();
		your_handler = Creds.get(3).toString();

	}

	public String regFwdInvite(String Event_id
								, HashMap<Integer, String> participantsMap
								){
		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress ="http://www.pickmemycar.com/pintr/EADFD/EVFDUDCHP070.php";
		String Event_participant_uid_list="";
		String Event_participant_status_list="";
		
		Iterator<Integer> myVeryOwnIterator = participantsMap.keySet().iterator();
		int i = 0;
		while(myVeryOwnIterator.hasNext()) {
			String key=Integer.toString(myVeryOwnIterator.next());
			String sep = "";
			if (i > 0) sep = ",";
			Event_participant_uid_list = Event_participant_uid_list 
											+ sep  
											+ key
											;
			Event_participant_status_list = Event_participant_status_list
											+ sep  
											+ "ATT"
											;
							
			i++;
		}
		
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("Event_id");
		inputVariables.add("Event_participant_status_list");
		inputVariables.add("Event_participant_uid_list");
		
		inputValues.add(email);
		inputValues.add(password);
		inputValues.add(Event_id);
		inputValues.add(Event_participant_status_list);
		inputValues.add(Event_participant_uid_list);
		
		return rDB.getDBQuery(	inputVariables
								,inputValues
								,queryAddress
								);
	}
	
	public void dispatchNotification(HashMap<Integer, String> participantsMap){
		//SEND MESSAGE NOTIFICATION TO PARTICIPANTS
		final Pintr_NT_01_GCMUtilityFunctions gcmuf = new Pintr_NT_01_GCMUtilityFunctions();
		Iterator<Integer> myVeryOwnIterator = participantsMap.keySet().iterator();
		while(myVeryOwnIterator.hasNext()) {
			Log.d("DISPATCH NOTIF", "SENDING FWD NOTE	");	
			String key=Integer.toString(myVeryOwnIterator.next());
			String ServerResponse = 
					gcmuf.dispatchMessageToServer(	key
													,email
													,password
													,your_handler  + " has sent you an invite " 
													,"INV"
													);
			Log.d("ServerResponse", ServerResponse);	
		}	
	}
	

}
