package com.pintr.androidapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;


final class Pintr_G_012_InteractiveSpinnerAdapter extends ArrayAdapter<String>{


	String y;
	String[] subs ;
	String[] strings ;
	LayoutInflater inflaterIn ;
	int arr_imagesIn[];
	int spinnerFormatXML ;
	
    public Pintr_G_012_InteractiveSpinnerAdapter(
		    		Context context
		    		, int textViewResourceId
		    		, String[] objects
		    		, String[] subsIn
		    		, String[] stringsIn 
		    		, LayoutInflater inflater
		    		, int arr_images[]
		    		){
        super(context, textViewResourceId, objects);
        inflaterIn = inflater;
        subs = subsIn;
        strings = stringsIn;
        arr_imagesIn = arr_images;
        spinnerFormatXML  = textViewResourceId;
    }

    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent) {

        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        
        View row=inflaterIn.inflate(spinnerFormatXML, parent, false);
        TextView label=(TextView)row.findViewById(R.id.company);
        label.setText(strings[position]);

        TextView sub=(TextView)row.findViewById(R.id.sub);
        sub.setText(subs[position]);

        ImageView icon=(ImageView)row.findViewById(R.id.image);
        icon.setImageResource(arr_imagesIn[position]);

        return row;
    }
}