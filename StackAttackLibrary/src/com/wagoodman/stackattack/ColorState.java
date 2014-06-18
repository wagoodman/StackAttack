package com.wagoodman.stackattack;


public class ColorState
{
	public Boolean				isTransforming	= false;	// true if this state is being acted upon
	public Boolean				isCompleted		= false;	// set to true upon completion (called in stop)
	public long					mStartTime		= -1;
	
	public final MotionEquation[]	mEquation;		// Which equation of motion to use while transforming...
	public final Color				mStartColor;
	public final float[]			mStartColorAmbient;
	public final float[]			mStartColorDiffuse;
	public final Color				mEndColor;
	public final float[]			mEndColorAmbient;
	public final float[]			mEndColorDiffuse;
	public final int[]				mDuration;		// in milliseconds; default to 1 second
	public final int[]				mTriggerDelay;	// milliseconds to wait after start() is called before starting the transition; [R, G, B]
	
	public ColorState(MotionEquation[] equation, Color start, Color end, int[] dur, int[] triggerDelay)
	{
		mEquation = equation;
		
		mStartColor = start;
		mStartColorAmbient = start.ambient().clone();
		mStartColorDiffuse = start.diffuse().clone();
		
		mEndColor = end;
		mEndColorAmbient = end.ambient().clone();
		mEndColorDiffuse = end.diffuse().clone();
		
		mDuration = dur;
		mTriggerDelay = triggerDelay;
	}
	
	// Specialised, used only by ColorBroker
	
	public void start(long now)
	{
		isTransforming = true;
		mStartTime = now;
	}
	
	public void stop()
	{
		isTransforming = false;
		isCompleted = true;
	}
	
	/**
	 * Finite Transform: returns the time t between 0 and mDuration, discontinuing the transform upon one period
 	 * The RGB values are delayed respective to the triggerDelay
 	 *  
	 * @param now
	 * @param componenet   R=0, G=1, B=2, A=3
	 * @return
	 */
	public long getRunningTime(long now, int component)
	{
		
		if (isTransforming == true)
			if ( 
					( now - ( mStartTime + mTriggerDelay[0] )) >= mDuration[0] && 
					( now - ( mStartTime + mTriggerDelay[1] )) >= mDuration[1] && 
					( now - ( mStartTime + mTriggerDelay[2] )) >= mDuration[2] &&
					( now - ( mStartTime + mTriggerDelay[2] )) >= mDuration[3] 
				)
					stop();
		
		// Return between 0 and duration
		// delay validated in animation broker
		return Math.min( Math.max( now - ( mStartTime + mTriggerDelay[component] ) , 0 )  , mDuration[component] );
	}
	
}


