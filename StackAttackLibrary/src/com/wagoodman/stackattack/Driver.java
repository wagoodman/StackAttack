package com.wagoodman.stackattack;

import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.TransformType;

public class Driver 
{

	private final MotionEquation	DEFAULT_EQ;
	private final Integer			DEFAULT_DURATION;
	
	public Boolean isTransforming = false;
	private long mStartTime;
	private int mDuration = 500;
	private float mStartPoint;
	private float mEndPoint;
	private MotionEquation mEq;
	
	public float mCurrent = 0f;
	
	private long triggerStartTime = -1;
	
	Driver(MotionEquation eq, Integer dur, Float current)
	{
		DEFAULT_EQ = eq;
		DEFAULT_DURATION = dur;
		mCurrent = current;
	}
	
	Driver(MotionEquation eq, Integer dur, Float start, Float end, Integer delay, Boolean startNow)
	{
		DEFAULT_EQ = eq;
		DEFAULT_DURATION = dur;
		
		if (startNow)
			start();
		else
			start(start, end, eq, dur, delay);
	}
	
	
	public void start(Float start, Float end, MotionEquation eq, Integer dur, Integer delay)
	{
		// stop any previous transforms
		stop();
		
		initStart(start, end, eq, dur);
		
		if (delay == null)
			start();
		else
			triggerStartTime = System.currentTimeMillis() + delay;
	}
	
	private void initStart(Float start, Float end, MotionEquation eq, Integer dur)
	{
		if (start == null)
			mStartPoint = mCurrent;
		else 
			mStartPoint = start;
		
		if (eq == null)
			mEq = DEFAULT_EQ;
		else
			mEq = eq;
		
		if (dur == null)
			mDuration = DEFAULT_DURATION;
		else
			mDuration = dur;
		
		mEndPoint = end;	
	}
	
	public void start()
	{
		mStartTime = System.currentTimeMillis();
		isTransforming = true;
	}
	
	public void stop()
	{
		isTransforming = false;
	}
	
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		if (primaryThread)
		{
			if (isTransforming)
			{
				mCurrent = (float) MotionEquation.applyFinite(
						TransformType.TRANSLATE, 
						mEq, 
						Math.max( 0, Math.min( now - mStartTime , mDuration ) ), 
						mDuration, 
						mStartPoint, 
						mEndPoint
						);
				if (Math.max( 0, Math.min( now - mStartTime , mDuration ) ) == mDuration)
				{
					stop();
					// prevent settling in the wrong place
					mCurrent = mEndPoint;
				}
			}	
		}
		
		else if (secondaryThread)
		{
			if (triggerStartTime != -1)
			{
				if (triggerStartTime <= now)
				{
					// assumed already init'd
					start();
					
					// reset trigger
					triggerStartTime = -1;
				}
			}
		}
		 
		
	}
	
	
}
