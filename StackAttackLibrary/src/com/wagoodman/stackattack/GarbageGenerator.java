package com.wagoodman.stackattack;

import java.text.DecimalFormat;

public class GarbageGenerator
{
	public Boolean 		isEnabled = false;
	
	// Timers
	public Long 		mNextCheckPoint = (long) 0;		// millstime; Next checkpoint time
	private Integer 	mCurrentWaitTime = 10;			// seconds; Random time between 5 & 30 seconds two wait before hitting a check point
	private Integer 	mMinTimer = 5;					// seconds;
	private Integer 	mMaxTimer = 30;					// seconds;
	
	// Points
	private Double	 	mCurrentQueuePoints = 0.0;		// When this reaches threshold, take action
	private Integer 	mQueueThreshold = 3;			// 
	private Integer 	mFreePointIncrement = 1;		// Upon every 
	
	
	
	public GarbageGenerator()
	{
		iterateCheckPointTimer(System.currentTimeMillis());
	}
	
	public void incrementGenerationPoints(double incPoints)
	{
		mCurrentQueuePoints += incPoints;
	}
	
	private int getGarbageCount()
	{
		return (int) (mCurrentQueuePoints/mQueueThreshold);
	}
	
	private int resetWithGarbageBlockCount()
	{
		int garbageCount = getGarbageCount();
		mCurrentQueuePoints = 0.0;
		return garbageCount;
	}
	
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
		return "\nGarbage Generator\n"+
				"QueuePoints: " + twoDForm.format(mCurrentQueuePoints) + "\n"+
				"Expct Cnt  : " + getGarbageCount() + "\n"+
				"chkPoint   : " + (mNextCheckPoint-System.currentTimeMillis()) + "\n";
	}
	
	
	/**
	 * 
	 * 
	 * @param now
	 * @return		The number of garbage blocks to generate
	 */
	public int update(long now)
	{
		if (isEnabled)
		{
			if (now >= mNextCheckPoint /*&& !isPaused*/)
			{
				// iterate!
				iterateCheckPointTimer(now);
			
				// check for garbage generation event
				if (mCurrentQueuePoints >= mQueueThreshold)
					return resetWithGarbageBlockCount();
				
				incrementGenerationPoints(mFreePointIncrement);
				
			}
		}
		
		return 0;
		
	}
	
}
