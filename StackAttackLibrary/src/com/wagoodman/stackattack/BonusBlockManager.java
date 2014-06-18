package com.wagoodman.stackattack;

import java.text.DecimalFormat;

import com.wagoodman.stackattack.BlockValue;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TransformType;


import android.content.Context;

public class BonusBlockManager 
{
	private static final String TAG = "BonusBlockManager";
	private static final Boolean debug = false;
	private final MainActivity game;
	private final Context mContext;
	
	public WoozyManager mWoozyManager;
	
	public Boolean isEnabled = true;
	
	public BonusBlockManager(Context ctxt)
	{
		// get the game object from context
		mContext = ctxt;
		game = (MainActivity) (mContext);
		
		// generator
		iterateCheckPointTimer(System.currentTimeMillis());
		
		// animator
		resetAnimationVars();
		
		mWoozyManager = new WoozyManager(mContext);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Generator
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// Timers
	public Long 		mNextCheckPoint = (long) 0;		// millstime; Next checkpoint time
	private Integer 	mCurrentWaitTime = 3;			// seconds; Random time between 5 & 90 seconds two wait before hitting a check point
	private Integer 	mMinTimer = 2;					// seconds;
	private Integer 	mMaxTimer = 90;					// seconds;
	
	
	private int getRandomDuration()
	{
		//return 5;
		return mMinTimer + (int)(Math.random() * ((mMaxTimer - mMinTimer) + 1));
	}
	
	private void iterateCheckPointTimer(long now)
	{
		mNextCheckPoint = now + (mCurrentWaitTime*1000);
		mCurrentWaitTime = getRandomDuration();
	}
	
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	public String report()
	{
		return "\nBonusBlock Generator\n"+
				"chkPoint   : " + (mNextCheckPoint-System.currentTimeMillis()) + "\n";
	}
	
	/** 
	 * @param now
	 * @return		Determines when to generate a bonus block
	 */
	public Boolean updateGenerator(long now)
	{
		if (now >= mNextCheckPoint /*&& !isPaused*/)
		{
			// iterate!
			iterateCheckPointTimer(now);
			
			if (isEnabled)
				return true;
			else
				return false;
		}
		return false;	
	}
	
	//public void commit(Block block, Coord<Integer> coords)
	//{	
	//}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Animations
	///////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public Boolean[] isTransforming = new Boolean[BlockValue.values().length];
	public Boolean[] hasCalledStop = new Boolean[BlockValue.values().length];
	public long[] mTriggerStopAction = new long[BlockValue.values().length];
	public float[] mResultProgress = new float[BlockValue.values().length];
	public MotionEquation[] mMotionEq = new MotionEquation[BlockValue.values().length];
	public long[] mStartTime = new long[BlockValue.values().length];
	public float[] mStartPoint = new float[BlockValue.values().length];
	public float[] mEndPoint = new float[BlockValue.values().length];
	public int[] mDuration = new int[BlockValue.values().length];
	
	private void resetAnimationVars()
	{
		for (int idx=0; idx < BlockValue.length; idx++)
		{
			isTransforming[idx] = false;
			mTriggerStopAction[idx] = -1;
			mResultProgress[idx] = 0;
			hasCalledStop[idx] = false;
		}
	}
	
	public Boolean isProcessingRowIncrementRestrictedBonusBlock()
	{
		
		for (int idx=0; idx < BlockValue.length; idx++)
		{
			if(isTransforming[idx] == true)
				// only block the long ones
				if (idx != BlockValue.DRUNK.ordinal() /*&& idx != BlockValue.UPSIDEDOWN.ordinal()*/)
					return true;
		}
		
		return false;
	}
	
	public Boolean isProcessingBonusBlock(BlockValue bv)
	{
		return isTransforming[ bv.ordinal() ];
	}
	
	public void updateAnimator(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		
		if (primaryThread)
		{
			//game.text = "";
			for (int idx=0; idx < BlockValue.length; idx++)
			{
				if (isTransforming[idx])
				{
					mResultProgress[idx] = (float) MotionEquation.applyFinite(
							TransformType.TRANSLATE, 
							mMotionEq[idx], 
							Math.max( 0, Math.min( now - mStartTime[idx] , mDuration[idx] ) ), 
							mDuration[idx], 
							mStartPoint[idx], 
							mEndPoint[idx]
							);
					
					//game.text += "% " + idx + " : " + mResultProgress[idx] + "\n";
					
					if (Math.max( 0, Math.min( now - mStartTime[idx] , mDuration[idx] ) ) == mDuration[idx])
					{
						stop(idx);
						mResultProgress[idx] = mEndPoint[idx];
					}		
				}				
			}
			//game.textviewHandler.post( game.updateTextView );
		}
		
		else if (secondaryThread)
		{
			for (int idx=0; idx < BlockValue.length; idx++)
			{
				// keep inactive boards from being processed for too long
				if (isTransforming[idx])
				{
					if (Math.max( 0, Math.min( now - mStartTime[idx] , mDuration[idx] ) ) == mDuration[idx])
					{
						stop(idx);
						mResultProgress[idx] = mEndPoint[idx];
					}	
				}
				
				if ( now >= mTriggerStopAction[idx] && mTriggerStopAction[idx] != -1)
				{
					// reset flag
					mTriggerStopAction[idx] = -1;
					
					// handle
					
					if (idx == BlockValue.DRUNK.ordinal())
					{
						isTransforming[idx] = false;
					}
					/*
					else if (idx == BlockValue.UPSIDEDOWN.ordinal())
					{
						game.world.mBoards.setNextOrientation(Orientation.NORMAL);
						game.mGlobalOrient = Orientation.NORMAL;
					}
					*/
						
				}
			}
			
		}
			
		// update drunk animation drivers
		mWoozyManager.updateAnimator(now, primaryThread, secondaryThread);
		
	}
	
	public void start(BlockValue bv, float start, float end, Integer duration, MotionEquation eq)
	{
		
		int idx = bv.ordinal();
		
		if (bv == BlockValue.DRUNK)
		{
			mWoozyManager.start();
		}
		
		if (duration != null)
			mDuration[idx] = duration;
		else
			mDuration[idx] = bv.mDuration;
		
		hasCalledStop[idx] = false;
		mMotionEq[idx] = eq;
		mStartPoint[idx] = start;
		mEndPoint[idx] = end;
		mStartTime[idx] = System.currentTimeMillis();
		isTransforming[idx] = true;
	}
	
	
	public void stop(int idx)
	{
		if (!hasCalledStop[idx])
		{
			if (idx == BlockValue.DRUNK.ordinal())
			{
				// stop later...
				mTriggerStopAction[idx] = System.currentTimeMillis() + WoozyManager.mMaxTimer;
				mWoozyManager.stop();
			}
			else
			{
				// stop now!
				mTriggerStopAction[idx] = System.currentTimeMillis();
				isTransforming[idx] = false;
			}
		}
		
		hasCalledStop[idx] = true;
		
	}
	
	
}