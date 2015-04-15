package com.pintr.androidapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Pintr_G_015_GeneralTextBubbleMaker {
		
	public Pintr_G_015_GeneralTextBubbleMaker() {
		
	}

	public LinearLayout bubbleTextMaker(	LinearLayout masterLinLayout
											,Context context
											,String answered
											,String bubbleText
											) {
		    //ADD HORIZONTAL LAYOUT
		    LinearLayout RL= new LinearLayout (context);

		    LinearLayout.LayoutParams lp1;
	        lp1 = new LinearLayout.LayoutParams(
	        		LayoutParams.MATCH_PARENT,
	        		LayoutParams.WRAP_CONTENT);
		    lp1.setMargins(20, 0, 20, 30);
		    
		    RL.setLayoutParams(lp1);
		    masterLinLayout.addView(RL, lp1);
		    
		    RelativeLayout.LayoutParams lp;
		    RL.setBackgroundResource(R.drawable.qna_unanswered_background);
	        
		    //ADD TITLE
		    TextView valueTV = new TextView(context);
		    valueTV.setText(bubbleText);
	        valueTV.setTypeface(null, Typeface.BOLD);
		    valueTV.setTextColor(context.getResources().getColor(R.color.MessageBubbleTextColor));
	        if(answered.equals("1")){
			    RL.setBackgroundResource(R.drawable.qna_background);
	        }
	        valueTV.setPadding (10,10,10,10);
	        lp = new RelativeLayout.LayoutParams(
			    	LayoutParams.WRAP_CONTENT,
			    	LayoutParams.WRAP_CONTENT);
	        lp.addRule(RelativeLayout.CENTER_VERTICAL);
	        valueTV.setLayoutParams(lp);

	        RL.addView(valueTV);
	        
	        
	        return RL;
	}
}
