package com.wagoodman.stackattack;

import java.util.Random;

public enum BlockValue 
{
	// texture scale, texture id, animation duration (total)
	NORMAL		(1f, 	R.drawable.white,				2000),
	SHUFFLE		(0.5f, 	R.drawable.shuffle128border,	2000),
	ROWBOMB		(0.5f, 	R.drawable.bomb128row,			2000),
	COLORBOMB	(0.5f, 	R.drawable.bomb128color,		1500),
	RANDOMBOMB	(0.5f, 	R.drawable.bomb128random,		3000),
	//UPSIDEDOWN	(0.5f, 	R.drawable.cycle128,			20000),
	DRUNK		(0.5f, 	R.drawable.beer128,				25000);
	
	public static final int BOMB_WAVE_DURATION = 400;
	public static final int BOMB_WAVE_DELAY = 1500;
	
	public static final int length = values().length;
	
	private static final Random randObjPool = new Random( System.currentTimeMillis() );
	
	public final float	mTexScale;
	public final int	mDrawableId;
	public final int	mDuration;
	
	BlockValue(float scale, int id, int duration)
	{
		mTexScale = scale;
		mDrawableId = id;
		mDuration = duration;
	}
	
	
	public static BlockValue getRandomBonusValue() 
	{
		int minPick = 1; // Don't Include NORMAL
		int maxPick = BlockValue.values().length - 1; 
		return BlockValue.values()[ minPick + randObjPool.nextInt(maxPick) ];
	}
	
	
}
