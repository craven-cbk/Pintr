package com.pintr.androidapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Pintr_G_005_MenuManager  extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		int MenuItemId = extras.getInt("menuitem");

		if (MenuItemId == R.id.settings) {
			startActivity(new Intent(this, Pintr_08_Settings.class));
		}
		else if (MenuItemId == R.id.interests) {
			startActivity(new Intent(this, Pintr_10_InterestsOverview.class));
		}
		else if (MenuItemId == R.id.Questions) {
			startActivity(new Intent(this, Pintr_12_QuestionsManager.class));
		}

		
		
		finish();
	}
}
