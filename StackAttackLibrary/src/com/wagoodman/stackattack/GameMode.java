package com.wagoodman.stackattack;

import java.util.HashMap;

import com.wagoodman.stackattack.RollableItem;

public enum GameMode implements RollableItem
{
	CLASSIC					("Classic", 7000)	,
	//TIMED_LEVELS			("Timed Levels", 7000)	,
	//MATCH_LEVELS			("Match Levels", 7000)	,
	RACE_AGAINST_THE_CLOCK	("Race Against the Clock", 90000)	;
	
	private static final HashMap<String, GameMode> mRevLookup;
	private static final int length;
	static
	{
		mRevLookup = new HashMap<String, GameMode>();
		for (GameMode mode : values())
		{
			mRevLookup.put( mode.getTitle(), mode );
		}
		length = values().length;
	}
	
	private String mTitle;
	private Integer mEOGDuration;
	
	GameMode(String title, Integer eog)
	{
		mTitle = title;
		mEOGDuration = eog;
	}
	
	@Override
	public String getTitle()
	{
		return mTitle;
	}
	
	public Integer getEOGDuration()
	{
		return mEOGDuration;
	}
	
	@Override
	public int getIndex()
	{
		return ordinal();
	}
	
	public static GameMode getMode(String key)
	{
		return mRevLookup.get(key);
	}
	
	public static GameMode getMode(int idx)
	{
		return values()[idx];
	}
	
	// messy , but im a little rushed here...
	@Override
	public String[] getAllTitles()
	{
		String[] ret = new String[values().length];
		int idx=0;
		for (GameMode mode : values())
		{
			ret[idx] = mode.getTitle();
			idx++;
		}
		return ret;
	}


	@Override
	public String getNext(String curValue) 
	{
		return values()[ getNext( getMode(curValue).ordinal() )  ].getTitle();
	}

	public GameMode getNext() 
	{
		return values()[ getNext( ordinal() ) ];
	}
	
	@Override
	public int getNext(int curIdx) 
	{
		if (curIdx < 0)
			return -1;
		
		return values()[ (curIdx + 1) % length ].ordinal();
	}

	@Override
	public String getPrev(String curValue) 
	{
		return values()[ getPrev( getMode(curValue).ordinal() )  ].getTitle();
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
	
	public GameMode getPrev() 
	{
		return values()[ getPrev( ordinal() ) ];
	}
	
}
