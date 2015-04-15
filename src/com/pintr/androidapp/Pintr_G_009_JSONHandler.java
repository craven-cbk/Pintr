package com.pintr.androidapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Pintr_G_009_JSONHandler  {

	protected Pintr_G_009_JSONHandler() {
	
	}

	public JSONArray JSON_Parser(
			String JSONinput
			, String datasetName
			){
		JSONObject jsonObj;
		JSONArray datasetArray = null;
		
		try {
			jsonObj = new JSONObject(JSONinput);
			datasetArray = jsonObj.getJSONArray(datasetName);
		} catch (JSONException e) {
			e.printStackTrace();
		}

			return datasetArray;
	}
}
