package com.learner.viewpageranimation;

import com.example.leanerdemo.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class ScreenSlideActivity extends FragmentActivity{

	private static final int NUM_PAGES = 5 ;
	private ViewPager viewPager ;
	private PagerAdapter mPagerAdapter ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_slide_page);
		
		//初始化viewpager和adapter
		viewPager = (ViewPager)findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		//设置自定义动画
		viewPager.setAdapter(mPagerAdapter);
		viewPager.setPageTransformer(true, new DepthPageTransformer());
	}
	
	@Override
	public void onBackPressed() {
		if(viewPager.getCurrentItem() == 0){
			// 如果用户当前正在看第一步（也就是第一页），那就要让系统来处理返回按钮。
            //这个是结束（finish()）当前活动并弹出回退栈。
            super.onBackPressed();
		}else{
			  // 否则，返回前一页
			viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
		}
	}
	
	
	
	class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{

		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Bundle data = new Bundle() ;
			data.putLong("position",position);
			ScreenSlideFragment fragment = new ScreenSlideFragment();
			fragment.setArguments(data);
			return fragment ;
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	
	
	
}

