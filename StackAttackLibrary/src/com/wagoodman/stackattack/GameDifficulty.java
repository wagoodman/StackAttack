package com.wagoodman.stackattack;

import java.util.HashMap;

import com.wagoodman.stackattack.RollableItem;


public enum GameDifficulty implements RollableItem
{
	EASY	(
			"Easy: Walk in the Park",	//Title
			1,							//boardCount
			20, 						//matchCountPerLevel
			30000,						//rpmDur
			new double[] {3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 17.5, 20},
			0.5,							//score multiplier
			new int[] {50, 350, 700},	// Blocks Destroy Stars
			new int[] {3, 4, 5},		// Largest Single Match Stars
			new int[] {10, 60, 130}		// Rows Played Stars
			) ,
			
	NORMAL	(
			"Norm: Hey, not so Rough",	//Title
			1,							//boardCount
			30, 						//matchCountPerLevel
			30000,						//rpmDur
			new double[] {5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17.5, 20, 22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40},
			1.0,						//score multiplier
			new int[] {75, 350, 900},	// Blocks Destroy Stars
			new int[] {3, 4, 5},		// Largest Single Match Stars
			new int[] {10, 60, 130}		// Rows Played Stars
			) ,
			
	HARD	(
			"Hard: Dude, Really?",		//Title	
			2,							//boardCount
			40, 						//matchCountPerLevel
			30000,						//rpmDur
			new double[] {7, 8, 9, 10, 12.5, 15, 17.5, 20, 22.5, 25, 27.5, 30, 32.5, 35, 37.5, 40},
			1.5,						//score multiplier
			new int[] {100, 500, 1000},	// Blocks Destroy Stars
			new int[] {4, 5, 7},		// Largest Single Match Stars
			new int[] {10, 60, 130}		// Rows Played Stars
			) ,
			
	INSANE	(
			"Insane: Stack Attack!",	//Title
			3,							//boardCount
			50, 						//matchCountPerLevel
			30000,						//rpmDur
			new double[] {10, 12.5, 15, 17.5, 20, 25, 30, 35, 40, 45, 50},
			2.0,						//score multiplier
			new int[] {150, 900, 1500},	// Blocks Destroy Stars
			new int[] {5, 6, 8},		// Largest Single Match Stars
			new int[] {10, 60, 130}		// Rows Played Stars
			);
	
	private static final HashMap<String, GameDifficulty> mRevLookup;
	private static final int length;
	static
	{
		mRevLookup = new HashMap<String, GameDifficulty>();
		for (GameDifficulty diff : values())
		{
			mRevLookup.put( diff.getTitle(), diff );
		}
		length = values().length;
	}
	
	private String mTitle;
	private int mBoardCount;
	private int mLevelMatchesQuota;
	private int mRPMIncrementDuration;
	private double[] mRPMValues;
	private double mScoreMultiplier;
	private int[] mBlocksDestoyedStars;
	private int[] mLargestMatchStars;
	private int[] mRowsPlayedStars;
	
	GameDifficulty(String title, int boardCount, int matchCountPerLevel, int rpmDur, double[] rpmVals, double mult, int[] blocksDestoyedStars, int[] largestMatchStars, int[] rowsPlayedStars)
	{
		mTitle = title;
		mBoardCount = boardCount;
		mLevelMatchesQuota = matchCountPerLevel;
		mRPMIncrementDuration = rpmDur;
		mRPMValues = rpmVals;
		mScoreMultiplier = mult;
		mBlocksDestoyedStars = blocksDestoyedStars;
		mLargestMatchStars = largestMatchStars;
		mRowsPlayedStars = rowsPlayedStars;
	}
	
	// returns star value
	public int getBlockDestroyStarStat(int value)
	{
		for (int starIdx=mBlocksDestoyedStars.length-1; starIdx >= 0 ; starIdx--)
		{
			if (value >= mBlocksDestoyedStars[starIdx])
			{
				return starIdx + 1;
			}
		}
		
		return 0;
	}

	// returns star value
	public int getLargestMatchStarStat(int value)
	{
		for (int starIdx=mLargestMatchStars.length-1; starIdx >= 0 ; starIdx--)
		{
			if (value >= mLargestMatchStars[starIdx])
			{
				return starIdx + 1;
			}
		}
		
		return 0;
	}

	// returns star value
	public int getRowsPlayedStarStat(int value)
	{
		for (int starIdx=mRowsPlayedStars.length-1; starIdx >= 0 ; starIdx--)
		{
			if (value >= mRowsPlayedStars[starIdx])
			{
				return starIdx + 1;
			}
		}
		
		return 0;
	}
	
	public int getBoardCount()
	{
		return mBoardCount;
	}
	
	public int getIncrementDuration()
	{
		return mRPMIncrementDuration;
	}
	
	public int getLevelMatchQuota()
	{
		return mLevelMatchesQuota;
	}
	
	public double[] getRpmValues()
	{
		return mRPMValues;
	}
	
	public double getScoreMultiplier()
	{
		return mScoreMultiplier;
	}
	
	@Override
	public String getTitle()
	{
		return mTitle;
	}
	
	@Override
	public int getIndex()
	{
		return ordinal();
	}
	
	public static GameDifficulty getDifficulty(String key)
	{
		return mRevLookup.get(key);
	}
	
	public static GameDifficulty getDifficulty(int idx)
	{
		return values()[idx];
	}
	
	// messy , but im a little rushed here...
	@Override
	public String[] getAllTitles()
	{
		String[] ret = new String[values().length];
		int idx=0;
		for (GameDifficulty diff : values())
		{
			ret[idx] = diff.getTitle();
			idx++;
		}
		return ret;
	}

	@Override
	public String getNext(String curValue) 
	{
		return values()[ getNext( getDifficulty(curValue).ordinal() )  ].getTitle();
	}

	@Override
	public int getNext(int curIdx) 
	{
		if (curIdx < 0)
			return -1;
		
		return values()[ (curIdx + 1) % length ].ordinal();
	}

	public GameDifficulty getNext() 
	{
		return values()[ getNext( ordinal() ) ];
	}
	
	@Override
	public String getPrev(String curValue) 
	{
		return values()[ getPrev( getDifficulty(curValue).ordinal() )  ].getTitle();
	}

	@Override
	public int getPrev(int curIdx) 
	{
		if (curIdx < 0)
			return -1;
		else if (curIdx == 0)
			curIdx = length;
		
		return values()[ (curIdx - 1) % length ].ordinal();
	}
	
	public GameDifficulty getPrev() 
	{
		return values()[ getPrev( ordinal() ) ];
	}

	
}
