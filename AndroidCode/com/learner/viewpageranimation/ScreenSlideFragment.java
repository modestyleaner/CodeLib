package com.learner.viewpageranimation;

import com.example.leanerdemo.R;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ScreenSlideFragment extends Fragment{

	
	private static final int [] BACK_COLOR =  new int [] {Color.BLUE,Color.GRAY,Color.GREEN,Color.RED,Color.MAGENTA} ; 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page,container,false);
		Bundle data = getArguments() ;
		int position = (int) data.getLong("position");
		viewGroup.setTag(position+"");
		viewGroup.setBackgroundColor(BACK_COLOR[position]);		
		return viewGroup;
	}
}
