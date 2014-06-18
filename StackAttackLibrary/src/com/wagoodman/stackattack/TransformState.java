package com.wagoodman.stackattack;

import com.wagoodman.stackattack.Identity;


public class TransformState extends Identity
{
	///////////////////////////////////////////////////////////////////////////////////
	// General
	
	// determination if this state is currently being acted upon & when it started
	public Boolean					isTransforming	= false;	// true if this state is being acted upon
	public Boolean					isCompleted		= false;	// set to true upon completion (called in stop)
	public long						mStartTime		= -1;
	public final TransformType		mTransform;		// Translate, Rotate, Scale...
	public final Boolean[]			mDirEnable;		// Which directions are enabled; X, Y, Z... (R, P, Y, respectively)
	public final TimeDomain			mTimeDomain;	// Finite, Continuous...
	public final MotionEquation[]	mEquation;		// Which equation of motion to use while transforming...
	public final int[]				mTriggerDelay;	// milliseconds to wait after start() is called before animating (for x, y, z)
	
	public Boolean					mTriggerStopOnNextPeriod = false;	// continuous transforms only; triggers stop.
	
	public String toString()
	{
		return
		"("+
		"Type: " + 	mTransform 	+ ", " +
		"Dir : " + 	mDirEnable 	+ ", " +
		"Time: " + 	mTimeDomain + ", " +
		"Eq  : " + 	mEquation 	+ ") ";
	}

	
	// Specialised, used only by AnimationBroker
	
	public void start(long now)
	{
		isTransforming = true;
		mStartTime = now;
	}
	
	public void stop()
	{
		if (mTimeDomain == TimeDomain.FINITE)
		{
			isTransforming = false;
			isCompleted = true;
		}
		else
		{
			mTriggerStopOnNextPeriod = true;
		}
	}
	
	
	/**
	 * Finite Transform: returns the time t between 0 and mDuration, discontinuing the transform upon one period
 	 * Continuous Transform: returns the time t between 0 and mDuration, continues the function on every period
 	 *  
	 * @param now
	 * @return
	 */

	public long getRunningTime(long now, int dir)
	{

		
		if (this.mTimeDomain == TimeDomain.FINITE)
		{
			if (isTransforming == true)
			{
				// stop
				if ( 
						( now - ( mStartTime + mTriggerDelay[0] )) >= mDuration[0] && 
						( now - ( mStartTime + mTriggerDelay[1] )) >= mDuration[1] && 
						( now - ( mStartTime + mTriggerDelay[2] )) >= mDuration[2] 
					)
						stop();
			}
			
			// Return a value between -X and mDuration (not past it); negative numbers are valid due to a desired delay before the animation starts (e.g. T-100 ms)
			// delay validated in animation broker
			return Math.min( now - ( mStartTime + mTriggerDelay[dir] ) , mDuration[dir] + mTriggerDelay[dir] );
			
		}
		// continuous
		else
		{
			if (isTransforming == true)
			{
				// done? restart or stop?
				if ( (now - ( mStartTime + mTriggerDelay[dir] )) >= (mPeriod[dir] + mTriggerDelay[dir]))
				{
					if (!mTriggerStopOnNextPeriod)
					{
						// restart
						mTriggerDelay[dir] = 0;
						mStartTime = now;
					}
					else
					{
						// stop
						isTransforming = false;
						isCompleted = true;
					}
				}

			}
			
			return (long) Math.min( now - ( mStartTime + mTriggerDelay[dir] ) , mPeriod[dir] + mTriggerDelay[dir] );
			
		}
		
		
		
		
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////
	// Continuous Transform
	
	public double[]	mRate = {0,0,0};			// {x,y,z} or {r,p,y} per unit
	public double[]	mPeriod = {1000,1000,1000};	// base unit for rate; default is milliseconds

	public TransformState(TransformType transform, Boolean[] direction, TimeDomain time, MotionEquation[] equation, float[] start, double[] rate, double[] period, int[] triggerDelay)
	{
		mTransform = transform;
		mDirEnable = direction;
		mTimeDomain = time;
		mEquation = equation;
		
		mStartPoint = start;
		mRate = rate;
		mPeriod = period;
		
		mTriggerDelay = triggerDelay;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////
	// Finite Transform

	public float[]	mStartPoint = {0,0,0};			// {x,y,z} / {r,p,y}
	public float[]	mEndPoint	= {0,0,0};			// {x,y,z} / {r,p,y}
	public int[]	mDuration	= {1000,1000,1000};	// in milliseconds; default to 1 second
	
	public TransformState(TransformType transform, Boolean[] direction, TimeDomain time, MotionEquation[] equation, float[] start, float[] end, int[] dur, int[] triggerDelay)
	{
		mTransform = transform;
		mDirEnable = direction;
		mTimeDomain = time;
		mEquation = equation;
		
		mStartPoint = start;
		mEndPoint = end;
		
		mTriggerDelay = triggerDelay;
		
		// Forced validation; do not fail
		for (int idx=0; idx<dur.length ;idx++)
		{
			if (dur[idx] > 0)
				mDuration[idx] = dur[idx];
			else
				mDuration[idx] = 1;
		}
	}

	
	
}
