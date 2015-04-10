package com.learner.common.customview;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;
/**
 * 可以滚动删除ListView的item
 * Item的布局文件注意：需要用LinearLayout来套住我们的item的布局，这点需要注意一下，不然滚动的只是TextView。例如
 *     <?xml version="1.0" encoding="UTF-8"?>  
 *   <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"  
 *       android:layout_width="fill_parent"  
 *       android:layout_height="wrap_content" >  
 *     
 *     <LinearLayout  
 *            android:layout_width="fill_parent"  
 *           android:layout_height="wrap_content"  
 *          android:background="@drawable/friendactivity_comment_detail_list2" >  
 *    
 *          <TextView  
 *              android:id="@+id/list_item"  
 *              android:layout_width="match_parent"  
 *              android:layout_height="wrap_content"  
 *              android:layout_margin="15dip" />  
 *      </LinearLayout>  
 *     
 *  </LinearLayout>  
 *   
 * @author zhaopf
 *
 */
public class SlideCutListView  extends ListView{

	private int slidePosition; //当前滑动的listview的position
	private int downX ; //手指按下的x坐标
	private int downY ; // 手指按下的y坐标
	private int screenWidth ;
	private View itemView ;
	private Scroller mSCroller ;
	private static final int SNAP_VELOCITY = 600 ;	
	private VelocityTracker velocityTracker ;
	private boolean isSlide  = false ;
	private int mTouchSlop ; //用户滑动的最小距离
	private RemoveListener mRemoveListener ;
	private RemoveDirection removeDirection ;
	//枚举值，滑动删除的方向
	public enum RemoveDirection{
		RIGHT,LEFT ;
	}
	
	
	public SlideCutListView(Context context) {
		this(context,null);
	}
	
	public SlideCutListView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}
	
	public SlideCutListView(Context context, AttributeSet attrs, int defStyle) {  
		super(context, attrs, defStyle);
		screenWidth = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		mSCroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}
	

	public void setmRemoveListener(RemoveListener mRemoveListener) {
		this.mRemoveListener = mRemoveListener;
	}


	/** 
     * 分发事件，主要做的是判断点击的是那个item, 以及通过postDelayed来设置响应左右滑动事件 
     */  
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN:
			addVelocityTracker(ev);
			//假如滑动还没有结束，则直接返回
			if(!mSCroller.isFinished()){
				return super.dispatchTouchEvent(ev);
			}
			downX = (int)ev.getX();
			downY = (int)ev.getY();
			
			slidePosition = pointToPosition(downX,downY);
			
			if(slidePosition == AdapterView.INVALID_POSITION){
				return super.dispatchTouchEvent(ev);
			}
			//获取itemView
			itemView = getChildAt(slidePosition - getFirstVisiblePosition());
			break;
		case MotionEvent.ACTION_MOVE:
			//判断是否滑动
			if(Math.abs(getScrollVelocity()) > SNAP_VELOCITY  
                    || (Math.abs(ev.getX() - downX) > mTouchSlop && Math  
                            .abs(ev.getY() - downY) < mTouchSlop)) {  
				isSlide = true ;
			}
			break;
		case MotionEvent.ACTION_UP:
			recycleVelocityTracker(); 
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
			requestDisallowInterceptTouchEvent(true);
			addVelocityTracker(ev);
			final int action = ev.getAction();
			int x = (int)ev.getX() ;
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				MotionEvent cancelEvent = MotionEvent.obtain(ev);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				onTouchEvent(ev);
				
				int deltaX = downX - x ;
				downX = x ;
				
				//手指拖动itemView滚动，deltaX > 0 ，则左滑，否则右滑
				itemView.scrollBy(deltaX, 0);
				return true ;  //拖动的时候ListView不滚动  
			case MotionEvent.ACTION_UP:
				int velocityX = getScrollVelocity();
				if (velocityX > SNAP_VELOCITY) {  
                    scrollRight();  
                } else if (velocityX < -SNAP_VELOCITY) {  
                    scrollLeft();  
                } else {  
                    scrollByDistanceX();  
                } 
				recycleVelocityTracker() ;
				// 手指离开的时候就不响应左右滚动
				isSlide = false ;
				break;
			}
		}
        //否则直接交给ListView来处理onTouchEvent事件  
		return super.onTouchEvent(ev);
	}
	
	 /** 
     * 根据手指滚动itemView的距离来判断是滚动到开始位置还是向左或者向右滚动 
     */  
	private void scrollByDistanceX() {
		 // 如果向左滚动的距离大于屏幕的二分之一，就让其删除  
        if (itemView.getScrollX() >= screenWidth / 2) {  
            scrollLeft();  
        } else if (itemView.getScrollX() <= -screenWidth / 2) {  
            scrollRight();  
        } else {  
            // 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动  
            itemView.scrollTo(0, 0);  
        }  
	}
	/** 
     * 向左滑动，根据上面我们知道向左滑动为正值 
     */ 
	private void scrollLeft() {
		removeDirection = RemoveDirection.LEFT;
		final int delta = (screenWidth - itemView.getScrollX());
		//调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item  
		mSCroller.startScroll(itemView.getScrollX(), 0,delta,0,Math.abs(delta));
		postInvalidate() ;
	}

	/** 
     * 往右滑动，getScrollX()返回的是左边缘的距离，就是以View左边缘为原点到开始滑动的距离，所以向右边滑动为负值 
     */  
	private void scrollRight() {
		removeDirection = RemoveDirection.RIGHT;
		final int delta = (screenWidth + itemView.getScrollX());
		//调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item  
		mSCroller.startScroll(itemView.getScrollX(), 0,-delta,0,Math.abs(delta));
		postInvalidate() ;
	}

	
	@Override
	public void computeScroll() {
		// 调用startScroll的时候scroller.computeScrollOffset()返回true，
		if(mSCroller.computeScrollOffset()){
			//让ListView 根据当前的滚动偏移量进行滚动
			itemView.scrollTo(mSCroller.getCurrX(),mSCroller.getCurrY());
			postInvalidate();
			
			//滚动动画结束时，调用回调接口
			if(mSCroller.isFinished()){
				if(mRemoveListener == null){
					throw new NullPointerException("RemoveListener is null, we should called setRemoveListener()");
				}
				itemView.scrollTo(0,0);
				mRemoveListener.removeItem(removeDirection, slidePosition);  
			}
		}
	}
	
	
	private void recycleVelocityTracker() {
		if(velocityTracker!=null){
			velocityTracker.recycle();
			velocityTracker = null ;
		}		
	}

	private int getScrollVelocity() {
		velocityTracker.computeCurrentVelocity(1000);
		int velocity = (int)velocityTracker.getXVelocity() ;
		return velocity ;
	}

	/** 
     * 添加用户的速度跟踪器 
     *  
     * @param event 
     */ 
	private void addVelocityTracker(MotionEvent ev) {
		if(velocityTracker == null){
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(ev);		
	}


	public interface RemoveListener{
		public void removeItem(RemoveDirection direction, int position);  
	}

}
