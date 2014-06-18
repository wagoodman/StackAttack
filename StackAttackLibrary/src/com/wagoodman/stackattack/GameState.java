package com.wagoodman.stackattack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


import com.wagoodman.stackattack.Animation;
import com.wagoodman.stackattack.DropSection;
import com.wagoodman.stackattack.FontManager;
import com.wagoodman.stackattack.GL3DText;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.World;

import android.content.Context;
import android.util.SparseIntArray;

public class GameState 
{
	private final Context mContext;
	private final MainActivity game;
	
	public Boolean hasGameStarted = false;
	
	public static final GameDifficulty DEFAULT_DIFFICULTY = GameDifficulty.NORMAL;
	public static final GameMode DEFAULT_MODE = GameMode.CLASSIC;
	public GameDifficulty mGameDiff = DEFAULT_DIFFICULTY;
	public GameMode mGameMode = DEFAULT_MODE;
	
	public int mEndOfGameDuration = 7000;	// hold for 7 seconds when the board is full before declaring game over (default)
	public TimeModule mEndOfGameTimer =  new TimeModule(false);	
	
	// simply a running time
	public TimeModule mGameTimer = new TimeModule(false);
	
	public Object mScoreMutex = new Object();
	public Integer mScoreValue = 0;
	public Integer mFinalScoreValue = 0;
	public ArrayList<Integer> mScoreComboMultiplier = new ArrayList<Integer>();
	
	public int mSpeedIncrementDuration;	// every X mill seconds, the board should add rows faster
	public TimeModule mSpeedIncrementTimer = new TimeModule(false);
	
	// Statistics
	public Integer mCurrentLevel = 1;
	public Integer mTotalMatchedBlocks = 0;
	public Integer mTotalMatches = 0;
	public Integer mLargestMatch = 0;
	public Integer mMaxMultiplier = 0;
	private SparseIntArray mMatchComboCount = new SparseIntArray();
	private SparseIntArray mLevelMatchCount = new SparseIntArray();
	
	public Object mLevelMutex = new Object();
	private SparseIntArray mLevelScore = new SparseIntArray();
	
	public int mCurrentLevelMatchCount;
	
	// Star Counts
	public int mMinStarCount = 0;
	public int mBlocksDestoyedStars = 0;
	public int mLargestMatchStars = 0;
	public int mRowsPlayedStars = 0;
	
	// Point Control
	public static int mMultiplierBase = 1;
	public Boolean mNegatePoints = false;
	
	public static String BANNERTEXT_NORMAL = "       ";
	public static String BANNERTEXT_RATC = "Top Hit! No Points!";
	public String mScoreBannerText = " ";
	
	
	GameState(Context ctxt)
	{
		mContext = ctxt;
		game = (MainActivity) ctxt;
		
		init();
	}
	
	GameState(Context ctxt, GameDifficulty gd, GameMode gm)
	{
		mContext = ctxt;
		game = (MainActivity) ctxt;
		
		mGameDiff = gd;
		mGameMode = gm;
		
		init();
	}
	
	private void init()
	{
		hasGameStarted = false;
		mCurrentLevel = 1;
		mTotalMatches = 0;
		mMaxMultiplier= 0;
		mMatchComboCount = new SparseIntArray();
		mLevelMatchCount = new SparseIntArray();
		mLevelScore = new SparseIntArray();
		mScoreComboMultiplier = new ArrayList<Integer>();
		resetMultiplier();
	}

	public void setRPMDuration(int dur)
	{
		mSpeedIncrementDuration = dur;
	}
	
	public void nextDifficulty()
	{
		mGameDiff = mGameDiff.getNext();
	}
	
	public void nextMode()
	{
		game.getWorld().mDropSection.mScoreBanner.clearBanner();
		mGameMode = mGameMode.getNext();
	}
	
	public void prevDifficulty()
	{
		mGameDiff = mGameDiff.getPrev();
	}
	
	public void prevMode()
	{
		game.getWorld().mDropSection.mScoreBanner.clearBanner();
		mGameMode = mGameMode.getPrev();
	}
	
	
	public void startGame()
	{
		startGame(true);
	}
	
	private Coord<Integer> numberDropCoord = new Coord<Integer>(MainActivity.ROWCOUNT/2, MainActivity.COLCOUNT/2 - 2);
	public void startGame(Boolean showIntro)
	{
		
		commitState();	
		
		if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
			mScoreBannerText = BANNERTEXT_RATC;
		else
			mScoreBannerText = BANNERTEXT_NORMAL;
		
		game.getWorld().mWidgetLayer.clearItems();

		
		if (showIntro)
		{
		
			Coord<Integer> titleHintDropCoord = new Coord<Integer>(MainActivity.ROWCOUNT -1 , MainActivity.COLCOUNT/2 - 3);
			
			game.getWorld().mWidgetLayer.addItem(
					"...Drag top bar down to pause...",
					new GL3DText
					(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"...Drag top bar down to pause...",
						true,
						FontManager.BANNER_TITLE_COLOR,
						titleHintDropCoord.getRow(),
						titleHintDropCoord.getCol(),
						0.75f //scale
					),
					
					Animation.SQUISHROCKFLIPOVERSPAWN, titleHintDropCoord, titleHintDropCoord, 1000, 1000*0   , null,
					Animation.TILTBUBBLEUP, titleHintDropCoord, titleHintDropCoord, 2000, 600 + 1000*3  , null
					);

			game.getWorld().mWidgetLayer.addItem(
					"3",
					new GL3DText
					(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"3",
						true,
						FontManager.BANNER_VALUE_COLOR,
						numberDropCoord.getRow(),
						numberDropCoord.getCol(),
						1f //scale
					),
					
					Animation.SQUISHROCKFLIPOVERCENTERSPAWN, numberDropCoord, numberDropCoord, 1000, 1000*0   , null,
					Animation.TILTFLOATUP, numberDropCoord, numberDropCoord, 1600, 600 + 1000*0 , null
				);
		

			
			game.getWorld().mWidgetLayer.addItem(
					"2",
					new GL3DText
					(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"2",
						true,
						FontManager.BANNER_VALUE_COLOR,
						numberDropCoord.getRow(),
						numberDropCoord.getCol(),
						1f //scale
					),
					
					Animation.SQUISHROCKFLIPOVERCENTERSPAWN, numberDropCoord, numberDropCoord, 1000, 1000*1   , null,
					Animation.TILTFLOATUP, numberDropCoord, numberDropCoord, 1600, 600 + 1000*1  , null
				);
			
			game.getWorld().mWidgetLayer.addItem(
					"1",
					new GL3DText
					(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"1",
						true,
						FontManager.BANNER_VALUE_COLOR,
						numberDropCoord.getRow(),
						numberDropCoord.getCol(),
						1f //scale
					),
					
					Animation.SQUISHROCKFLIPOVERCENTERSPAWN, numberDropCoord, numberDropCoord, 1000, 1000*2   , null,
					Animation.TILTFLOATUP, numberDropCoord, numberDropCoord, 1600, 600 + 1000*2  , null
				);
			
			game.getWorld().mWidgetLayer.addItem(
					"Go!",
					new GL3DText
					(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"Go!",
						true,
						FontManager.BANNER_VALUE_COLOR,
						numberDropCoord.getRow(),
						numberDropCoord.getCol(),
						1f //scale
					),
					
					Animation.SQUISHROCKFLIPOVERCENTERSPAWN, numberDropCoord, numberDropCoord, 1000, 1000*3   , null,
					Animation.TILTFLOATUP, numberDropCoord, numberDropCoord, 2000, 600 + 1000*3  , null
				);
		}
		
	}
	
	
	// TEMP TMEP TEMP
	//public void doStartGame()
	//{
	//	
	//}
	
	// TEMP TMEP TMEP
	public void doStartGame()
	//public void startGame()
	{
		// TMEP TEMP TEMP
		//commitState();
		
		//mGameTimer.startOrResume();
		// always counting
		if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
		{
			mEndOfGameTimer.start();
		}
		
		hasGameStarted = true;
		
	}
	
	public void endGame()
	{
		endGame(true);
	}
	
	public void endGame(Boolean showEndGameText)
	{
		
		mGameTimer.pause();
		
		// show in game stats
		game.getWorld().mDropSection.mScoreBanner.intro();
		game.getWorld().mDropSection.mScoreBanner.hideBanner();
		
		mFinalScoreValue = (int) (mScoreValue * mGameDiff.getScoreMultiplier());
		
		if (showEndGameText == true)
		{
			game.getWorld().mWidgetLayer.addItem(
					"Game Over",
					new GL3DText
					(
						mContext,
						FontManager.MENUMAJORITEM_FONT,
						"Game Over",
						true,
						FontManager.BANNER_VALUE_COLOR,
						MainActivity.ROWCOUNT/2,
						MainActivity.COLCOUNT/2 - 2,
						1f //scale
					),
					
					Animation.SQUISHROCKFLIPIN, null, new Coord<Integer>(MainActivity.ROWCOUNT/2, MainActivity.COLCOUNT/2 - 2), 2000, 0   , null,
					Animation.SQUISHROCKFLIPOUT, null, new Coord<Integer>(MainActivity.ROWCOUNT/2, MainActivity.COLCOUNT/2 - 2), 2000, World.ENDOFGAMEDELAY + DropSection.ENDGAME_DURATION  , null
					
					//Animation.DROPSPAWN, null, new Coord<Integer>(CA_Game.ROWCOUNT/2, CA_Game.COLCOUNT/2 - 2), 1000, 0   , null
					//Animation.ROCKFLIPOUT, null, new Coord<Integer>(CA_Game.ROWCOUNT/2, CA_Game.COLCOUNT/2), 3000, World.ENDOFGAMEDELAY + DropSection.ENDGAME_DURATION, null
					//Animation.TILTFLOATUP      , null, new Coord<Integer>(midRow, midCol), 2000, 1500, null
					//Animation.TILTBUBBLEUP      , null, new Coord<Integer>(CA_Game.ROWCOUNT/2, CA_Game.COLCOUNT/2), 3000, World.ENDOFGAMEDELAY + DropSection.ENDGAME_DURATION, null
				);
		}
		//game.getWorld().mBoards.startGarbageDropSpringAnimation(3, 1000);
		
		//game.getWorld().mWidgetLayer.getItem("Game Over").mAnimation.queueAnimation( Animation.TEXTWOBBLE, null, null, null, null, null);
		
		
		// always counting... except at the end of the game
		if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
		{
			mEndOfGameTimer.pause();
		}
	}
	
	public void doEndGame()
	{
		try
		{
			game.getWorld().mWidgetLayer.getGLItem("Game Over").mAnimation.queueAnimation( Animation.GAMEOVERFOLLOWBOARDROTATION, null, null, DropSection.ENDGAME_DURATION, 0, null);
		}
		catch(Exception e)
		{
			//Log.e("GameState:doEndGame", e.toString());
		}
	}

	
	public void pause(String lockId)
	{
		mSpeedIncrementTimer.pause(lockId);
		mGameTimer.pause(lockId);	
		mEndOfGameTimer.pause(lockId);
	}
	
	public void resume(String lockId)
	{
		mSpeedIncrementTimer.startOrResume(lockId);
		mGameTimer.startOrResume(lockId);	
		mEndOfGameTimer.resume(lockId);
	}
	
	public void pause()
	{
		mSpeedIncrementTimer.pause();
		mGameTimer.pause();	
		mEndOfGameTimer.pause();
	}
	
	public void resume()
	{
		mSpeedIncrementTimer.startOrResume();
		mGameTimer.startOrResume();	
		mEndOfGameTimer.resume();
	}
	
	public void commitState()
	{
		//game.text = game.getWorld().mBoards.mBoardMembers.size()+"\n";
		//game.textviewHandler.post( game.updateTextView );
		
		// Mode
		mEndOfGameDuration = mGameMode.getEOGDuration();
		
		// Diff
		setRPMDuration( mGameDiff.getIncrementDuration() );
		game.getWorld().mBoards.setRPMValues( mGameDiff.getRpmValues() );
		
		// Commit
		game.getWorld().createBoards( mGameDiff.getBoardCount() );

		//game.text += game.getWorld().mBoards.mBoardMembers.size()+"\n";
		//game.textviewHandler.post( game.updateTextView );
	}
	
	public void nextLevel()
	{		
		synchronized(mLevelMutex)
		{
			mCurrentLevel++;
			mCurrentLevelMatchCount = 0;
		}
		
	}
	
	public Boolean isReadyToSpeedUp(long now)
	{
		if (mGameMode == GameMode.CLASSIC || mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
		{
			if ( mSpeedIncrementTimer.hasElapsed(now, mSpeedIncrementDuration) )
			{
				// reset running time
				mSpeedIncrementTimer.restart();
				
				return true;
			}
		}
		/*
		else if (mGameMode == GameMode.TIMED_LEVELS)
		{
			if ( mSpeedIncrementTimer.hasElapsed(now, mSpeedIncrementDuration) )
			{
				// reset running time
				mSpeedIncrementTimer.restart();
				
				synchronized(mLevelMutex)
				{
					mCurrentLevel++;
				}
				return true;
			}
		}*/
		/*
		else if (mGameMode == GameMode.MATCH_LEVELS)
		{
			if (mDetectedLevelTransition)
			{
				mDetectedLevelTransition = false;
				
				return true;
			}
		}*/
		
		return false;
	}
	
	private double average(HashSet<Integer> set)
	{
		double sum = 0;
		
		if (set.size() == 0)
			return 0;
		
		for (Integer item : set) 
			sum += item;
		return sum / set.size();
	}

	private Integer min(HashSet<Integer> set)
	{
		int min = Integer.MAX_VALUE;
		for (Integer item : set) 
		{
			if (item < min)
				min = item;
		}
		return min;
	}
	
	public void resetMultiplier(Set<Integer> cols)
	{
		synchronized(mScoreMutex)
		{
			for (Integer col : cols)
			{
				mScoreComboMultiplier.set(col, mMultiplierBase);
			}
		}
	}
	
	public void resetMultiplier()
	{
		synchronized(mScoreMutex)
		{
			if (mScoreComboMultiplier.size() == 0)
			{
				for (int idx=0; idx < MainActivity.COLCOUNT; idx++)
					mScoreComboMultiplier.add(idx, mMultiplierBase);
			}
			else
			{
				for (int idx=0; idx < MainActivity.COLCOUNT; idx++)
					mScoreComboMultiplier.set(idx, mMultiplierBase);
			}
		}
	}
	
	public int getMaxMultiplier(Set<Integer> cols)
	{
		int max = 0;
		for (Integer col : cols)
		{
			max = Math.max(max, mScoreComboMultiplier.get(col));
		}
		return max;
	}
	
	private int getMaxMultiplier()
	{
		int max = 0;
		for (int idx=0; idx < mScoreComboMultiplier.size(); idx++)
		{
			max = Math.max(max, mScoreComboMultiplier.get(idx));
		}
		return max;
	}
	
	private void incrementMultiplier()
	{
		for (int idx=0; idx < mScoreComboMultiplier.size(); idx++)
		{
			mScoreComboMultiplier.set(idx, mScoreComboMultiplier.get(idx) + 1);
		}
	}
	
	public void TopOfBoardFilledEvent()
	{
		if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
			mNegatePoints = true;
	}
	
	public void TopOfBoardEmptiedEvent()
	{
		if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
			mNegatePoints = false;
	}
	
	private int currentMatchCount, matchScore, currentLevelPoints/*, numBlocks*/, midCol, midRow, curMultiplier;
	private Random intGenerator = new Random();
	private int heightModifier = 0;
	public void reapStatistics(int numBlocks, int numPointOnlyBlocks, HashSet<Integer> rowMatches, HashSet<Integer> colMatches, Boolean isVisible)
	{
		
		synchronized(mScoreMutex)
		{
			
			if (colMatches != null)
				midCol = (int) (average(colMatches));
			else
				midCol = MainActivity.COLCOUNT/2;
			if (rowMatches != null)
				midRow = (int) (average(rowMatches));
			else
				midRow = MainActivity.ROWCOUNT/2;
			
			/*
			game.text += "Col: " + getHashSetValues(colMatches) + "\n";
			game.text += "Row: " + getHashSetValues(rowMatches) + "\n";			
			//game.text += average(colMatches) + "+" + min(colMatches) + "=" + midCol + "  ,  " + average(rowMatches) + "+" + min(rowMatches) + "=" + midRow + "\n";
			game.text += midCol + "  ,  " + midRow + "\n";
			game.text += "\n";
			game.textviewHandler.post( game.updateTextView );
			*/
			
			//numBlocks = matches.size();
			
			// dontn count blocks if you are on teh top row of raceagainsttheclock
			if (mNegatePoints)
				numBlocks = 0;
			
			
			// gets the current count, returns 0 if the key does not exist
			currentMatchCount = mMatchComboCount.get(numBlocks, 0) + 1;
			mMatchComboCount.put(numBlocks, currentMatchCount);
			
			String ps = String.valueOf(mScoreValue);
			
			synchronized(mLevelMutex)
			{
				// figure the current multiplier
				curMultiplier = getMaxMultiplier(colMatches);
				
				// gets the current count, returns 0 if the key does not exist
				mCurrentLevelMatchCount = mLevelMatchCount.get(mCurrentLevel, 0) + 1;
				mLevelMatchCount.put(mCurrentLevel, mCurrentLevelMatchCount);
				
				// keep total tally of matches
				mTotalMatchedBlocks += numBlocks + numPointOnlyBlocks;
				mTotalMatches += 1;
				
				// keep track of the largest number of blocks in a match
				mLargestMatch = Math.max(mLargestMatch, numBlocks);
				
				// keep total score
				matchScore = (numBlocks+numPointOnlyBlocks)*curMultiplier;
				mScoreValue += matchScore;
				
				// gets the current count, returns 0 if the key does not exist
				currentLevelPoints = mLevelScore.get(mCurrentLevel, 0) + matchScore;
				mLevelScore.put(mCurrentLevel, currentLevelPoints);
				
				
				// determine stars
				
				mBlocksDestoyedStars = mGameDiff.getBlockDestroyStarStat(mTotalMatchedBlocks);
				mLargestMatchStars = mGameDiff.getLargestMatchStarStat(mLargestMatch);
				mMinStarCount = Math.min(mRowsPlayedStars, Math.min(mBlocksDestoyedStars, mLargestMatchStars));
				// Not Here! Do this in updateRowProgression() in the BoardManager
				//mRowsPlayedStars = mGameDiff.getRowsPlayedStarStat(game.getWorld().mBoards.mCurrentGlobalRowIndex);
				
			}
			
			//game.text += ps + " + ( " + numBlocks + " * " +  mScoreComboMultiplier + " ) = " + mScoreValue + "\n" ;
			//game.textviewHandler.post( game.updateTextView );
			
			
			if (isVisible)
			{
				if (mNegatePoints)
				{
					// show widget (matchScore)
					game.getWorld().mWidgetLayer.addItem(
						String.valueOf(intGenerator.nextInt()),
						new GL3DText
						(
							mContext,
							FontManager.MENUMINORITEM_FONT,
							"+0!",
							true,
							FontManager.ERROR_COLOR,
							midRow,
							midCol,
							1f //scale
						),
						Animation.SQUISHROCKFLIPOVERSPAWN, null, new Coord<Integer>(midRow, midCol), 2000, 0   , null,
						//Animation.ROCKFLIPOUT      , null, new Coord<Integer>(midRow, midCol), 1000, 1500, null
						//Animation.TILTFLOATUP      , null, new Coord<Integer>(midRow, midCol), 2000, 1500, null
						Animation.TILTBUBBLEUP      , null, new Coord<Integer>(midRow, midCol), 2000, 1500, null
					);
				}
				else
				{
					// show widget (matchScore)
					game.getWorld().mWidgetLayer.addItem(
						String.valueOf(intGenerator.nextInt()),
						new GL3DText
						(
							mContext,
							FontManager.MENUMINORITEM_FONT,
							"+"+String.valueOf(numBlocks),
							true,
							FontManager.BANNER_VALUE_COLOR,
							midRow,
							midCol,
							1f //scale
						),
						Animation.SQUISHROCKFLIPOVERSPAWN, null, new Coord<Integer>(midRow, midCol), 2000, 0   , null,
						//Animation.ROCKFLIPOUT      , null, new Coord<Integer>(midRow, midCol), 1000, 1500, null
						//Animation.TILTFLOATUP      , null, new Coord<Integer>(midRow, midCol), 2000, 1500, null
						Animation.TILTBUBBLEUP      , null, new Coord<Integer>(midRow, midCol), 2000, 1500, null
					);
					
					
					if (curMultiplier >= 2)
					{
						//game.text += mScoreComboMultiplier+" : " + heightModifier +"\n";
						//game.textviewHandler.post( game.updateTextView );
						
						// show widget (matchScore)
						game.getWorld().mWidgetLayer.addItem(
							String.valueOf(intGenerator.nextInt()),
							new GL3DText
							(
								mContext,
								FontManager.MENUMINORITEM_FONT,
								"x"+String.valueOf(curMultiplier)+"!",
								true,
								FontManager.BANNER_TITLE_COLOR,
								MainActivity.ROWCOUNT/2 + heightModifier, //mid row
								MainActivity.COLCOUNT/2,			// mid col
								1f //scale
							),
							Animation.SQUISHROCKFLIPOVERSPAWN, null, new Coord<Integer>(MainActivity.ROWCOUNT/2 + heightModifier, MainActivity.COLCOUNT/2), 2000, 0   , null,
							Animation.TILTBUBBLEUP      , null, new Coord<Integer>(MainActivity.ROWCOUNT/2 + heightModifier, MainActivity.COLCOUNT/2), 2000, 1500, null
						);
						
						if (heightModifier >= 3)
						{
							heightModifier = -1;
						}
						
						heightModifier++;
						
						// avoid 0
						if (heightModifier == 0)
						{
							heightModifier++;
						}
						
					}
				}
			}
			
			// OLD FUNCTIONALITY
			/*
			if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
			{
				//game.text += "added! " + matchScore + "\n";
				//game.text += mEndOfGameTimer.getElapsedTime() + "\n";
				mEndOfGameTimer.addSeconds(matchScore*1000); // conv to mill seconds
				//game.text += mEndOfGameTimer.getElapsedTime() + "\n";
				//game.textviewHandler.post( game.updateTextView );
			}
			*/
			
			// collect the largest multiplier
			mMaxMultiplier = Math.max(mMaxMultiplier, getMaxMultiplier());
			
			// increment the score multiplier for next match
			incrementMultiplier();
		}
		
	}
	
	public void incrementRowAction()
	{
		if (game.getIsGameOver() == false)
		{
			
			if (mGameMode == GameMode.CLASSIC /*|| mGameMode == GameMode.MATCH_LEVELS*/ /*|| mGameMode == GameMode.TIMED_LEVELS*/)
			{
				if (mEndOfGameTimer.isStarted)
				{
					mEndOfGameTimer.reset();
					game.getWorld().mDropSection.mScoreBanner.intro();
					game.getWorld().mDropSection.mScoreBanner.hideBanner();
				}
			}
			else if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
			{
				game.getWorld().mDropSection.mScoreBanner.intro();
				game.getWorld().mDropSection.mScoreBanner.hideBanner();
			}
		}
	}
	
	public void cannotIncrementRowAction( Boolean isCurrentBoardSettleable)
	{
		// dont start timer if there is melting blocks
		if (isCurrentBoardSettleable)
			return;
		
		if (game.getIsGameOver() == false)
		{
			if (!game.getWorld().mBoards.areAllTopRowsEmpty())
			{
				if (mGameMode == GameMode.CLASSIC /*|| mGameMode == GameMode.MATCH_LEVELS*/ /*|| mGameMode == GameMode.TIMED_LEVELS*/)
				{
					mEndOfGameTimer.restart();
					game.getWorld().mDropSection.mScoreBanner.outro();
					game.getWorld().mDropSection.mScoreBanner.showBanner();
				}
				else if (mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
				{
					game.getWorld().mDropSection.mScoreBanner.outro();
					game.getWorld().mDropSection.mScoreBanner.showBanner();
				}
			}
		}
	}
	
	private String getSparseIntArrayValues(SparseIntArray sparseArray)
	{
		String ret = "[ ";
		int key = 0;
		for(int i = 0; i < sparseArray.size(); i++) {
		   key = sparseArray.keyAt(i);
		   Object obj =sparseArray.get(key);
		   ret += String.valueOf(key) + ":" + String.valueOf(obj) + " ";
		}
		
		return ret + " ]";
	}
	
	private String getHashSetValues(HashSet<Integer> set)
	{
		String ret = "[ ";
		int key = 0;
		for(Integer i : set) {
		   ret += String.valueOf(i) + ", ";
		}
		return ret + " ]";
	}
	
	public void update(long now)
	{
		/*
		// TEMP TEMP TEMP report
		game.text  = "Statistics\n";
		game.text += "Diff:             " + mGameDiff + "\n";
		game.text += "Mode:             " + mGameMode + "\n";
		game.text += "Score:            " + mScoreValue + "\n";
		game.text += "mCurrentLevel:    " + mCurrentLevel + "\n";
		game.text += "CurrentRPMIdx:    " + game.getWorld().mBoards.mRPMIndex + "\n";
		game.text += "CurrentRPM:       " + game.getWorld().mBoards.mRowPerMin + "\n";
		game.text += "mLevelScore:      " + getSparseIntArrayValues(mLevelScore) + "\n";
		game.text += "mMatchComboCount: " + getSparseIntArrayValues(mMatchComboCount) + "\n";
		game.text += "mLevelMatchCount: " + getSparseIntArrayValues(mLevelMatchCount) + "\n";
		game.text += "mMaxMultiplier:   " + mMaxMultiplier + "\n";
		game.text += "Multiplier:       " + mScoreComboMultiplier + "\n";
		game.text += "EOG Timer:        " + mEndOfGameTimer.getElapsedTime(now) + "\n";
		game.text += "Time Remaining:   " + (mEndOfGameDuration - mEndOfGameTimer.getElapsedTime(now)) + "\n";
		
		game.textviewHandler.post( game.updateTextView );
		*/
		
		//game.text = String.valueOf( mScoreComboMultiplier );
		//game.textviewHandler.post( game.updateTextView );
		
		// check for end of game condition; only trigger once!
		if ( mEndOfGameTimer.hasElapsed(now, mEndOfGameDuration) && !game.getIsGameOver())
		{

			// end game!
			mEndOfGameTimer.reset();
			game.endGame();
			
			
		}
		else if (!game.getIsPaid() && mScoreValue >= game.getFreeVersionMaxPoints() && !game.getIsGameOver() && game.getHasShownSupportMeDialog() == false)
		{

			// end game!
			//mEndOfGameTimer.reset();
			//game.endGame();
			
			game.showBuyMeDialog();
			game.setHasShownSupportMeDialog(true);
			game.getWorld().pauseWorld(true);
		
		}
		
		//game.debug("Paid:"+game.getIsPaid() +"\n");
		
		/*
		if (mGameMode == GameMode.MATCH_LEVELS)
		{

			if (mLevelMatchCount.get(mCurrentLevel, 0) >= mGameDiff.getLevelMatchQuota())
			{
				
				// no no, let the speed take over on the next pickup
				// get to the next level!
				//game.getWorld().mBoards.quickenBoardProgression();
				
				nextLevel();
			}
			
		}
		*/
		
		
	}
	
	
	
	
	
}
