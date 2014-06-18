package com.wagoodman.stackattack;

import com.wagoodman.stackattack.AnimationBroker;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TransformType;

import android.content.Context;

public class WoozyManager 
{
	private static final String TAG = "BonusBlockManager";
	private static final Boolean debug = false;
	private final MainActivity game;
	private final Context mContext;
	
	public Boolean mForeverWoozy = false;
	
	public Boolean mEnabled = false;
	public int mRandomStackCount = 20;
	
	WoozyManager(Context ctxt)
	{
		// get the game object from context
		mContext = ctxt;
		game = (MainActivity) (mContext);
		
		// animator
		resetAnimationVars(mRandomStackCount);
		
		mForeverWoozy = game.getPreferences().getBoolean("woozyMode", false);
		
		if (mForeverWoozy)
		{
			GameState.mMultiplierBase = 2;
			mEnabled = true;
			start();
		}
		else
		{
			GameState.mMultiplierBase = 1;
		}
	}

	
	// Timers
	public static final Integer 	mMinIndex = -1;	
	public static final Integer 	mMinTimer = 200;	// milliseconds;
	public static final Integer 	mMaxTimer = 1500;	// milliseconds;
	
	
	private int getRandomPeriod()
	{
		//return 1000;
		return mMinTimer + (int)(Math.random() * ((mMaxTimer - mMinTimer) + 1)) ;
	}
	
	
	// return -1 to maxIndex
	private int getRandomIndex()
	{
		//return 0;
		return mMinIndex + (int)(Math.random() * ((mRandomStackCount - mMinIndex) + 1)) ;
	}

	
	public int getIndexFromId(String id, int animationType, int xyz)
	{
		if (animationType == AnimationBroker.POS)
		{
			if (xyz == AnimationBroker.X)
			{
				return ((int) id.charAt(0)) % mRandomStackCount;
			}
			else if (xyz == AnimationBroker.Y)
			{
				return ((int) id.charAt(1)) % mRandomStackCount;
			}
			else if (xyz == AnimationBroker.Z)
			{
				return ((int) id.charAt(2)) % mRandomStackCount;
			}
		}
		else if (animationType == AnimationBroker.ROT)
		{
			if (xyz == AnimationBroker.X)
			{
				return ( ((int) id.charAt(0)) + ((int) id.charAt(2)) ) % mRandomStackCount;
			}
			else if (xyz == AnimationBroker.Y)
			{
				return ( ((int) id.charAt(1)) + ((int) id.charAt(1)) ) % mRandomStackCount;
			}
			else if (xyz == AnimationBroker.Z)
			{
				return ( ((int) id.charAt(2)) + ((int) id.charAt(0)) ) % mRandomStackCount;
			}
		}
		else if (animationType == AnimationBroker.SIZ)
		{
			if (xyz == AnimationBroker.X)
			{
				return ( ((int) id.charAt(0)) + ((int) id.charAt(0)) ) % mRandomStackCount;
			}
			else if (xyz == AnimationBroker.Y)
			{
				return ( ((int) id.charAt(1)) + ((int) id.charAt(2)) ) % mRandomStackCount;
			}
			else if (xyz == AnimationBroker.Z)
			{
				return ( ((int) id.charAt(2)) + ((int) id.charAt(1)) ) % mRandomStackCount;
			}
		}
		
		return -1;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Animations
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Boolean[] isTransforming = new Boolean[mRandomStackCount];
	public Boolean[] mTriggerStopOnNextPeriod = new Boolean[mRandomStackCount];
	public Boolean[] mOnUpturn = new Boolean[mRandomStackCount];
	public float[] mResultProgress = new float[mRandomStackCount];
	public MotionEquation[] mMotionEq = new MotionEquation[mRandomStackCount];
	public long[] mStartTime = new long[mRandomStackCount];
	public long[] mTriggerStartTime = new long[mRandomStackCount];
	public float[] mStartPoint = new float[mRandomStackCount];
	public float[] mEndPoint = new float[mRandomStackCount];
	public double[] mPeriod = new double[mRandomStackCount];
	
	private void resetAnimationVars(int count)
	{
		mRandomStackCount = count;
		
		for (int idx=0; idx < mRandomStackCount; idx++)
		{
			isTransforming[idx] = false;
			mTriggerStopOnNextPeriod[idx] = false;
			mResultProgress[idx] = 0;
			mStartPoint[idx] = 0;
			mEndPoint[idx] = 1;
			mOnUpturn[idx] = true;
			mMotionEq[idx] = MotionEquation.LOGISTIC;	//oscillation 
			mTriggerStartTime[idx] = -1;
		}
	}
	
	
	private Boolean anyIndexesTransforming()
	{
		for (int idx=0; idx < mRandomStackCount; idx++)
		{
			// currently transforming
			if (isTransforming[idx] == true)
				return true;
			// soon to be scheduled
			if (mTriggerStartTime[idx] != -1)
				return true;
		}
		return false;
	}
	
	public void updateAnimator(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		
		if (primaryThread)
		{
			if (!mEnabled)
				return;
			
			//game.text = "";
			for (int idx=0; idx < mRandomStackCount; idx++)
			{
				if (isTransforming[idx])
				{
					mResultProgress[idx] = (float) MotionEquation.applyFinite(
							TransformType.TRANSLATE, 
							mMotionEq[idx], 
							(long) Math.max( 0, Math.min( now - mStartTime[idx] , mPeriod[idx] ) ),
							mPeriod[idx],
							mStartPoint[idx], 
							mEndPoint[idx]
							);
					
					//game.text += "% " + idx + " : " + mResultProgress[idx] + "\n";
					
					if (Math.max( 0, Math.min( now - mStartTime[idx] , mPeriod[idx] ) ) == mPeriod[idx])
					{
						
						
						if (!mTriggerStopOnNextPeriod[idx])
						{							
							// flip around
							if (mOnUpturn[idx])
							{
								mOnUpturn[idx] = false;
								mEndPoint[idx] = 0;
								mStartPoint[idx] = 1;
							}
							else
							{
								mOnUpturn[idx] = true;
								mEndPoint[idx] = 1;
								mStartPoint[idx] = 0;
							}
							
							// restart
							mTriggerStartTime[idx] = -1;
							mStartTime[idx] = now;
							
						}
						else if (mTriggerStopOnNextPeriod[idx] && mOnUpturn[idx] == true)
						{
							mOnUpturn[idx] = false;
							mEndPoint[idx] = 0;
							mStartPoint[idx] = 1;
							
							// restart
							mTriggerStartTime[idx] = -1;
							mStartTime[idx] = now;
							
						}
						else if ( mTriggerStopOnNextPeriod[idx] )
						{
							//game.text += "% " + idx + " : " + mResultProgress[idx] + "\n";
							
							stopNow(idx);
						}
					}		
				}				
			}
			//game.textviewHandler.post( game.updateTextView );
		}
		
		else if (secondaryThread)
		{
			
			if (!anyIndexesTransforming())
			{
				mEnabled = false;
			}
			
			// trigger start
			for (int idx=0; idx < mRandomStackCount; idx++)
			{
				if (!isTransforming[idx])
				{
					if (System.currentTimeMillis() >= mTriggerStartTime[idx] && mTriggerStartTime[idx] != -1)
					{
						start(idx);
					}
				}
			}
			
		}
	}
	
	public void start()
	{
		resetAnimationVars(mRandomStackCount);
		
		for (int idx=0; idx < mRandomStackCount; idx++)
		{
			start(idx, null , null );
		}
		
	}
	
	private void start(int idx, Integer delay, Integer period)
	{
		mEnabled = true;
		
		if (period != null)
			mPeriod[idx] = period;
		else
			mPeriod[idx] = getRandomPeriod();

		if (delay != null)
			mTriggerStartTime[idx] = delay + System.currentTimeMillis();
		else
			mTriggerStartTime[idx] = getRandomPeriod() + System.currentTimeMillis();
		
		// everything has been scheduled!... just sit back and wait for triggers to fire (don't manually start!)
		
	}
	
	private void start(int idx)
	{
		mStartTime[idx] = System.currentTimeMillis();
		isTransforming[idx] = true;
	}
	
	public void stop()
	{
		if (!mForeverWoozy)
		{
			for (int idx=0; idx < mRandomStackCount; idx++)
			{
				stop(idx);
			}
		}
	}
	
	private void stop(int idx)
	{
		if (!mForeverWoozy)
			mTriggerStopOnNextPeriod[idx] = true;
	}
	
	private void stopNow(int idx)
	{
		if (!mForeverWoozy)
		{
			isTransforming[idx] = false;
			mTriggerStopOnNextPeriod[idx] = false;
		}
	}
	
	
}