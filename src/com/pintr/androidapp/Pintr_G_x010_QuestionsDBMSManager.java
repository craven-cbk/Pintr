



package com.pintr.androidapp;

import java.util.ArrayList;
import java.util.List;


public class Pintr_G_x010_QuestionsDBMSManager {
	
	public Pintr_G_x010_QuestionsDBMSManager() {
	}
	
	
	// 1.  RETURN THE LIST OF TOPICS ON THE DATABASE FOR A GIVEN TOPIC LEVEL, AS A JSON STRING
	public String JSONQuestionGetter(int records_requested
									,String email
									,String password
									,String function
									,String topic_of_interest
									,int hashtag_id
									,int question_id
									,String maxDate
									,int InterestTopic
									){		

		Pintr_DB_001_GeneralRemoteDBQuery rDB = new Pintr_DB_001_GeneralRemoteDBQuery();
		
		List<String> inputVariables = new ArrayList<String>();
		List<String> inputValues = new ArrayList<String>();
		String queryAddress = "http://www.pickmemycar.com/pintr/AVICI/AVICI_QUA_manager.php";
		
		inputVariables.add("topic_of_interest");
		inputVariables.add("function");
		inputVariables.add("email");
		inputVariables.add("password");
		inputVariables.add("records_requested");
		inputVariables.add("hashtag_id");
		inputVariables.add("question_id");
		inputVariables.add("maxDate");
		
		inputValues.add(topic_of_interest);
		inputValues.add(function);
		inputValues.add(email);
		inputValues.add(password);
		inputValues.add(Integer.toString(records_requested));
		inputValues.add(Integer.toString(hashtag_id));
		inputValues.add(Integer.toString(question_id));
		inputValues.add(maxDate);
		
		return rDB.getDBQuery(	inputVariables
								,inputValues
								,queryAddress
								);
	}
}
