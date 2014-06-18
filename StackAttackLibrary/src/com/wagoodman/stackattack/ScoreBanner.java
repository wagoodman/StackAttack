package com.wagoodman.stackattack;


import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.FontManager;
import com.wagoodman.stackattack.GameMode;
import com.wagoodman.stackattack.GameState;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TextTransform;
import com.wagoodman.stackattack.TimeModule;

import android.content.Context;
import android.util.SparseArray;

public class ScoreBanner 
{
	private final MainActivity game;
	private final Context mContext;
	
	private Boolean mCommitted = false;
	private Boolean mEOGStatsShown = false;
	
	// Indexes 0 - 2 only!
	private SparseArray<GLButton<Void>> mTitles = new SparseArray<GLButton<Void>>();
	// Indexes 0 - 3 only!
	private SparseArray<GLButton<Void>> mValues = new SparseArray<GLButton<Void>>();
	
	// used for game over count downs
	public GLButton<Void> mBannerText;
	public Boolean mBannerVisable = true;
	
	public Boolean mScoreVisable = true;
	
	private String	mTitleFont = FontManager.MENUMINORITEM_FONT,//FontManager.MENUMAJORITEM_FONT,
					mValueFont = FontManager.MENUMINORITEM_FONT;
	
	private float STARSOFFSCREEN_LEFT = -200f;
	
	public ScoreBanner(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
	}
	
	private Driver mStarDriver;
	
	
	private float leftPos0;
	private float leftPos1;
	private float leftPos2;
	private float rightPos0;
	private float rightPos1;
	private float[] xPos;
	
	
	private float bottom;
	private float middle;
	private float top;
	private float topMid;
	private float bottomMid;
	
	// used to store titles
	String title = "";
	
	public void setDimensions()
	{
		for(int i = 0; i < mTitles.size(); i++) 
		{
			mTitles.get(mTitles.keyAt(i)).setLabelDimensions();
			mTitles.get(mTitles.keyAt(i)).setFontDimensions();		
		}
		
		for(int i = 0; i < mValues.size(); i++) 
		{
			mValues.get(mValues.keyAt(i)).setLabelDimensions();
			mValues.get(mValues.keyAt(i)).setFontDimensions();	
		}
		
		mBannerText.setLabelDimensions();
		mBannerText.setFontDimensions();		
		
	}
	
	public void clearBanner()
	{
		mCommitted = false;
		mTitles.clear();
		mValues.clear();
	}
	
	public void outro()
	{
		outro(false);
	}
	
	public void intro()
	{
		intro(false);
	}
	
	public void outro(Boolean force)
	{
		if (mScoreVisable || force)
		{
			mScoreVisable = false;
			GLButton<Void> tmp;
			for(int i = 0; i < mTitles.size(); i++) 
			{
				tmp = mTitles.get(mTitles.keyAt(i));
				//tmp.lockLabel();
				tmp.outro();
			}
			
			for(int i = 0; i < mValues.size(); i++) 
			{
				tmp = mValues.get(mValues.keyAt(i));
				//tmp.lockLabel();
				tmp.outro();
			}
		}
	}
	
	public void intro(Boolean force)
	{
		if (!mScoreVisable || force)
		{
			mScoreVisable = true;
			GLButton<Void> tmp;
			for(int i = 0; i < mTitles.size(); i++) 
			{
				tmp = mTitles.get(mTitles.keyAt(i));
				//tmp.unlockLabel();
				tmp.intro();
			}
			
			for(int i = 0; i < mValues.size(); i++) 
			{
				tmp = mValues.get(mValues.keyAt(i));
				//tmp.unlockLabel();
				tmp.intro();
			}
		}
	}
	
	private float	scratchFontHeight,
					scratchTitleWidth,
					valueScaleHeight=0.75f, 
					titleScaleHeight=0.75f,
					scoreScaleHeight=1f,
					bannerScaleHeight=1.5f;
	
	// locks in what fields are used, and what is in which field
	public void commitBanner()
	{
		mEOGStatsShown = false;
		mTitles.clear();
		mValues.clear();
		resetStats();
		

		leftPos0 = game.getWorld().mDropSection.mXBorderOffset;
		leftPos1 = game.getWorld().mDropSection.mXBorderOffset + game.getWorld().mScreenWidth*0.4f;
		leftPos2 = 0; // THIS IS NOT VALID
		rightPos0 = game.getWorld().mScreenWidth - game.getWorld().mDropSection.mXBorderOffset;
		rightPos1 = game.getWorld().mScreenWidth - game.getWorld().mDropSection.mXBorderOffset - game.getWorld().mScreenWidth*0.3f;
		
		
		xPos = new float[] {leftPos0, leftPos1, leftPos2, rightPos0};
		
		bottom = -game.getWorld().mDropSection.mYPixFoldedHeight;
		middle = bottom + (-bottom/2f);
		top = 0;
		topMid = middle + (-middle/2.5f);		// not quite 3/4
		bottomMid = middle + (middle/2.5f);		// not quite 1/4
		
		// used to store titles
		String title = "";
		
		scratchFontHeight = game.getWorld().mDropSection.mFonts.getFontHeight(FontManager.MENUMINORITEM_FONT);
		
		if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.CLASSIC)
		{
			// RUNNING TIME
			
			title = "Running Time";
			scratchTitleWidth = game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, title);
			mTitles.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_TITLE_COLOR,
					title, 
					(int) (leftPos0) ,	// x
					(int) ( topMid - ((scratchFontHeight*titleScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					titleScaleHeight    	// fontScale
				)
			);
			
			mValues.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_VALUE_COLOR,
					"0", 
					(int) (leftPos0) ,	// x
					(int) ( bottomMid - ((scratchFontHeight*valueScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					valueScaleHeight    	// fontScale
				)
			);
			
		}
		/*
		else if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.MATCH_LEVELS)
		{
			// LEVEL
			
			title = "Level";
			scratchTitleWidth = game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, title);
			mTitles.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_TITLE_COLOR,
					title, 
					(int) (leftPos0) ,	// x
					(int) ( topMid - ((scratchFontHeight*titleScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					titleScaleHeight    	// fontScale
				)
			);
			
			mValues.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_VALUE_COLOR,
					"1", 
					(int) (leftPos0) ,	// x
					(int) ( bottomMid - ((scratchFontHeight*valueScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					valueScaleHeight    	// fontScale
				)
			);
			
			leftPos1 = leftPos0 + scratchTitleWidth + game.getWorld().mDropSection.mXBorderOffset;
			xPos[1] = leftPos1;
			
			// Matches Remaining
			title = "Matches Left";
			scratchTitleWidth = game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, title);
			mTitles.put(1, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_TITLE_COLOR,
					title, 
					(int) (leftPos1) ,	// x
					(int) ( topMid - ((scratchFontHeight*titleScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					titleScaleHeight    	// fontScale
				)
			);
			
			mValues.put(1, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_VALUE_COLOR,
					"0", 
					(int) (leftPos1) ,	// x
					(int) ( bottomMid - ((scratchFontHeight*valueScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					valueScaleHeight    	// fontScale
				)
			);
			
			
		}
		*/
		else if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
		{
			// SECONDS LEFT
			title = "Seconds Left";
			scratchTitleWidth = game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, title);
			mTitles.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_TITLE_COLOR,
					title, 
					(int) (leftPos0) ,	// x
					(int) ( topMid - ((scratchFontHeight*titleScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					titleScaleHeight    	// fontScale
				)
			);
			
			mValues.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_VALUE_COLOR,
					"0", 
					(int) (leftPos0) ,	// x
					(int) ( bottomMid - ((scratchFontHeight*valueScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					valueScaleHeight    	// fontScale
				)
			);
		}
		/*
		else if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.TIMED_LEVELS)
		{
			// LEVEL
			title = "Level";
			scratchTitleWidth = game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, title);
			mTitles.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_TITLE_COLOR,
					title, 
					(int) (leftPos0) ,	// x
					(int) ( topMid - ((scratchFontHeight*titleScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					titleScaleHeight    	// fontScale
				)
			);
			
			mValues.put(0, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_VALUE_COLOR,
					"1", 
					(int) (leftPos0) ,	// x
					(int) ( bottomMid - ((scratchFontHeight*valueScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					valueScaleHeight    	// fontScale
				)
			);
			
			leftPos1 = leftPos0 + scratchTitleWidth + game.getWorld().mDropSection.mXBorderOffset;
			
			// Time Remaining
			title = "Time Left";
			scratchTitleWidth = game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, title);
			mTitles.put(1, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_TITLE_COLOR,
					title, 
					(int) (leftPos1) ,	// x
					(int) ( topMid - ((scratchFontHeight*titleScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					titleScaleHeight    	// fontScale
				)
			);
			
			mValues.put(1, 
			new GLButton<Void>(
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_VALUE_COLOR,
					"0", 
					(int) (leftPos1) ,	// x
					(int) ( bottomMid - ((scratchFontHeight*valueScaleHeight)/2f)  ) , 	// y
					true, 	// left just
					valueScaleHeight    	// fontScale
				)
			);
			
		}
		*/
		
		// SCORE

		scratchFontHeight = game.getWorld().mDropSection.mFonts.getFontHeight(FontManager.SCORE_FONT);
		mValues.put(3, 
		new GLButton<Void>(
				mContext, 
				FontManager.SCORE_FONT, 
				FontManager.BANNER_SCORE_COLOR,
				"0", 
				(int) (rightPos0) ,	// x
				(int) ( middle - ((scratchFontHeight*scoreScaleHeight)/2f)  ) , 	// y
				false, 	// left just
				scoreScaleHeight    	// fontScale
			)
		);
		
		
		
		// game over timer
		scratchFontHeight = game.getWorld().mDropSection.mFonts.getFontHeight(FontManager.SCORE_FONT);
		mBannerText = new GLButton<Void>(
				mContext, 
				FontManager.SCORE_FONT, 
				FontManager.BANNER_SCORE_COLOR,
				game.getWorld().mBoards.mGameState.mScoreBannerText, 
				(int) (game.getWorld().mScreenWidth/2 - (game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.SCORE_FONT, game.getWorld().mBoards.mGameState.mScoreBannerText)*bannerScaleHeight)/2) ,	// x
				(int) ( middle - ((scratchFontHeight*bannerScaleHeight)/2f)  ) , 	// y
				true, 	// left just
				bannerScaleHeight    	// fontScale
			);
		
		mBannerText.mIntroTransform = TextTransform.RollInFromLeft;
		mBannerText.mOutroTransform = TextTransform.VacuumOutRight;		
		
		mBannerVisable = false;
		mBannerText.outro();
		
		mStarDriver = new Driver(MotionEquation.LOGISTIC, 750, 0f );
		showBannerStars();
		
		
		// set intro/outro
		for(int i = 0; i < mTitles.size(); i++) 
		{
			// right
			if (i > 1)
			{
				mTitles.get(mTitles.keyAt(i)).mIntroTransform = TextTransform.RollInFromLeft;
				mTitles.get(mTitles.keyAt(i)).mOutroTransform = TextTransform.VacuumOutRight;
			}
			// left
			else
			{
				mTitles.get(mTitles.keyAt(i)).mIntroTransform = TextTransform.RollInFromLeft;
				mTitles.get(mTitles.keyAt(i)).mOutroTransform = TextTransform.VacuumOutRight;
			}
		}
		
		for(int i = 0; i < mValues.size(); i++) 
		{
			// right
			if (i > 1)
			{
				mValues.get(mValues.keyAt(i)).mIntroTransform = TextTransform.RollInFromLeft;
				mValues.get(mValues.keyAt(i)).mOutroTransform = TextTransform.VacuumOutRight;
			}
			// left
			else
			{
				mValues.get(mValues.keyAt(i)).mIntroTransform = TextTransform.RollInFromLeft;
				mValues.get(mValues.keyAt(i)).mOutroTransform = TextTransform.VacuumOutRight;
			}
		}
		
		setDimensions();
		mCommitted = true;
		
	}
	
	
	public void showBanner()
	{
		showBanner(false, true);
	}
	
	public void hideBanner()
	{
		hideBanner(false, true);
	}
	
	public void showBanner(Boolean force, Boolean triggerEvent)
	{
		if (!game.getWorld().mTutorialState.isInTutorial)
		{
			if (!mBannerVisable || force)
			{
				if (triggerEvent)
					game.getWorld().mBoards.mGameState.TopOfBoardFilledEvent();
				if (mBannerText != null)
				{
					mBannerVisable = true;
					mBannerText.intro();
				}
				hideBannerStars();
			}
		}
	}
	
	public void hideBanner(Boolean force, Boolean triggerEvent)
	{
		if (mBannerVisable || force)
		{
			if (triggerEvent)
				game.getWorld().mBoards.mGameState.TopOfBoardEmptiedEvent();
			if (mBannerText != null)
			{
				mBannerVisable = false;
				mBannerText.outro();
			}
			showBannerStars();
		}
	}
	
	
	public void showBannerStars()
	{
		if (mStarDriver != null)
		{
			mStarDriver.start(game.getWorld().mScreenWidth, rightPos1, MotionEquation.LOGISTIC, 750, null );
		}
	}
	
	public void hideBannerStars()
	{
		if (mStarDriver != null)
		{
			mStarDriver.start(rightPos1, game.getWorld().mScreenWidth, MotionEquation.LOGISTIC, 750, null );
		}
	}
	
	public void addScoreBannerItem(int idx, String title, String value, Boolean intro)
	{

		scratchTitleWidth = game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, title);
		
		GLButton<Void> btTitle = new GLButton<Void>(
				mContext, 
				FontManager.MENUMINORITEM_FONT, 
				FontManager.BANNER_TITLE_COLOR,
				title, 
				(int) (xPos[idx]) ,	// x
				(int) ( topMid - ((scratchFontHeight*titleScaleHeight)/2f)  ) , 	// y
				true, 	// left just
				titleScaleHeight    	// fontScale
			);
		
		
		
		GLButton<Void> btValue = new GLButton<Void>(
				mContext, 
				FontManager.MENUMINORITEM_FONT, 
				FontManager.BANNER_VALUE_COLOR,
				value, 
				(int) (xPos[idx]) ,	// x
				(int) ( bottomMid - ((scratchFontHeight*valueScaleHeight)/2f)  ) , 	// y
				true, 	// left just
				valueScaleHeight    	// fontScale
			);
		

		btTitle.setLabelDimensions();
		btTitle.setFontDimensions();		
		
		btTitle.mIntroTransform = TextTransform.RollInFromLeft;
		btTitle.mOutroTransform = TextTransform.VacuumOutRight;

		btValue.setLabelDimensions();
		btValue.setFontDimensions();	
		
		btValue.mIntroTransform = TextTransform.RollInFromLeft;
		btValue.mOutroTransform = TextTransform.VacuumOutRight;
		
		if (intro == true)
		{
			btTitle.intro();
			btValue.intro();
		}
		
		mTitles.put(idx,btTitle);
		mValues.put(idx,btValue);
	}
	
	public void doPostEndGame()
	{
		
		// add difficulty to score banner
		
		String title = "Difficulty";
		String value = game.getWorld().mBoards.mGameState.mGameDiff.toString() + " (x" + String.valueOf(game.getWorld().mBoards.mGameState.mGameDiff.getScoreMultiplier()) + ")";
		
		addScoreBannerItem(1, title, value, true);
		
		
		// Show Stats
		
		introEndOfGameStats();
		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	// STATS
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private SparseArray<GLButton<Void>> mStatsTitles = new SparseArray<GLButton<Void>>();
	private SparseArray<GLButton<Void>> mStatsValues = new SparseArray<GLButton<Void>>();
	private SparseArray<Integer> mStatDiverValues = new SparseArray<Integer>();
	private SparseArray<Driver> mStatDrivers = new SparseArray<Driver>();
	private SparseArray<Integer> mStatStarDiverValues = new SparseArray<Integer>();
	private SparseArray<Driver> mStatStarDrivers = new SparseArray<Driver>();
	private Driver mFinalStarDriver;
	private int introDelay = (int) ((DropSection.ENDGAME_DURATION/5)*0.75);
	private Boolean showStats = false;
	
	private Driver mScoreDriver;
	
	private float yDelim;
	
	// TO DO:
		// make value drivers.... (INT + Double!!!) that roll the stats in from 0-VALUE. constat delim. logistic time cont. varied duration (based on count from 0-VALUE and delim time)
		// intro
		// use value drivers on stats....
		// use value driver on Final score (score multiplier). need other animation to show its coming from game difficulty.... (mult count down?... need double)
	
	public void resetStats()
	{
		showStats = false;
		mStatsTitles.clear();
		mStatsValues.clear();
		mStatDiverValues.clear();
		mStatDrivers.clear();
		mStatStarDrivers.clear();
		mStatStarDiverValues.clear();
		
		// Final Star Set Driver
		mFinalStarDriver = new Driver(MotionEquation.LOGISTIC, 1500, -200f );
	}
	
	
	public void setStatsDimensions()
	{
		for(int i = 0; i < mStatsTitles.size(); i++) 
		{
			mStatsTitles.get(mStatsTitles.keyAt(i)).setLabelDimensions();
			mStatsTitles.get(mStatsTitles.keyAt(i)).setFontDimensions();		
		}
		
		for(int i = 0; i < mStatsValues.size(); i++) 
		{
			mStatsValues.get(mStatsValues.keyAt(i)).setLabelDimensions();
			mStatsValues.get(mStatsValues.keyAt(i)).setFontDimensions();	
		}
	}
	
	
	public void setStats( float statSpace )
	{
		// 0: Matches
		// 1: Largest Match
		// 2: Highest Multiplier
		// 3: Rows Played
		
		float titleX = (float) (game.getWorld().mScreenWidth*0.4f + game.getWorld().mDropSection.mXBorderOffset);
		float valueX = (float) (game.getWorld().mScreenWidth*0.4f - game.getWorld().mDropSection.mXBorderOffset);
		yDelim = (float) (statSpace/5);
		
		String[] titles = {"Blocks Destroyed", /*"Total Matches",*/ "Largest Single Match", "Rows Played"};//, "Final Score (x"+String.valueOf(game.getWorld().mBoards.mGameState.mGameDiff.getScoreMultiplier())+")"};
		
		// figure driver time delims
		int timeDelim = 25;
		int minDur = 500;
		int maxDur = 3000;
		//game.text = "";
		
		for (int idx=0 ; idx < titles.length; idx++)
		{
			float titleHeight = titleScaleHeight;
			float valueHeight = scoreScaleHeight;	
			
			// grab stats
			if (idx == 0)
			{
				// Blocks Destroyed
				mStatDiverValues.put(idx, (int)( game.getWorld().mBoards.mGameState.mTotalMatchedBlocks ) );
				mStatStarDiverValues.put(idx, game.getWorld().mBoards.mGameState.mBlocksDestoyedStars);
			}
			/*
			else if (idx == 1)
			{
				// Matches
				mStatDiverValues.put(idx, (int)( game.getWorld().mBoards.mGameState.mTotalMatches ) );
			}
			*/
			else if (idx == 1)
			{
				// Largest Match
				mStatDiverValues.put(idx, (int)( game.getWorld().mBoards.mGameState.mLargestMatch ) );
				mStatStarDiverValues.put(idx, game.getWorld().mBoards.mGameState.mLargestMatchStars);
			}
			else if (idx == 2)
			{
				// Rows Played
				mStatDiverValues.put(idx, (int)( game.getWorld().mBoards.mCurrentGlobalRowIndex ) );
				mStatStarDiverValues.put(idx, game.getWorld().mBoards.mGameState.mRowsPlayedStars);
			}
			/*
			else if (idx == 4)
			{
				// Final Score
				mStatDiverValues.append(idx, (int)( game.getWorld().mBoards.mGameState.mScoreValue * game.getWorld().mBoards.mGameState.mGameDiff.getScoreMultiplier() ) );
				titleHeight = scoreScaleHeight*1.5f;
				valueHeight = scoreScaleHeight*1.5f;	
			}
			*/
			
			// figure the driver duration
			int duration = (int) (timeDelim * mStatDiverValues.get(idx));
			
			
			
			
			// set titles
			mStatsTitles.put(idx, new GLButton<Void>(
					false,
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_TITLE_COLOR,
					titles[idx], 
					(int) (titleX) ,	// x
					(int) ( bottom - (idx+1)*yDelim ) , 	// y
					true, 	// left just
					titleHeight    	// fontScale
				)
			);
			
			// set initial values
			mStatsValues.put(idx, new GLButton<Void>(
					false,
					mContext, 
					FontManager.MENUMINORITEM_FONT, 
					FontManager.BANNER_VALUE_COLOR,
					"0", 
					(int) (valueX) ,	// x
					(int) ( bottom - (idx+1)*yDelim ) , 	// y
					false, 	// left just
					valueHeight    	// fontScale
				)
			);
			
			mStatsTitles.get(idx).mIntroTransform = TextTransform.MoveDiagLeftIn;
			mStatsTitles.get(idx).mOutroTransform = TextTransform.MoveDiagLeftOut;
			
			mStatsValues.get(idx).mIntroTransform = TextTransform.RollInFromLeft;
			mStatsValues.get(idx).mOutroTransform = TextTransform.VacuumOutRight;
			
			//game.text += idx + " : " + yDelim + " * " + (idx+1) + " = " + ((idx+1)*yDelim) + "\n";
			
			// init new drivers
			mStatDrivers.put(idx,
					new Driver(MotionEquation.LOGISTIC, Math.min( Math.max(duration, minDur), maxDur), 0f )
			);
			
			mStatStarDrivers.put(idx,
					new Driver(MotionEquation.LOGISTIC, maxDur/2, STARSOFFSCREEN_LEFT )
			);
			
		}
		
		
		//game.textviewHandler.post( game.updateTextView );
		
		mScoreDriver = new Driver(MotionEquation.LOGISTIC, 1500, 0f );
		
		// Final Star Set Driver
		mFinalStarDriver = new Driver(MotionEquation.LOGISTIC, maxDur/2, -200f );
		
		setStatsDimensions();
	}
	
	public void introInGameStats(int duration)
	{
		// Show Fog Screen
		
		float topPerc = (float) ( DropSectionState.FOLDED.getHeight()/2.0f + DropSectionState.UNFOLDED.getHeight() + (game.getWorld().mDropSection.mPixBorderHeight/game.getWorld().mScreenHeight) );
		float botPerc = 2f;
		
		game.getWorld().mDropSection.mStatsFogScreen.set(game.getWorld().mScreenHeight, 0);
		
		game.getWorld().mDropSection.mStatsFogScreen.start(
				topPerc, 
				botPerc, 
				duration
				);
		
		game.getWorld().mDropSection.mStatsFogScreen.start(Color.TRANSPARENT, Color.GHOSTDARKBLACK, duration);
		
		
		// get stats
		
		setStats( (float) ((1f - DropSectionState.UNFOLDED.getHeight()) * game.getWorld().mScreenHeight) );
		
		// Show Stats
		
		int delay = 100;
		
		for(int idx = 0; idx < mStatsTitles.size(); idx++) 
		{
			mStatDrivers.get(idx).start(0f , (float) mStatDiverValues.get(idx), MotionEquation.LOGISTIC, 800 , (idx+1)*delay);
			mStatStarDrivers.get(idx).start(STARSOFFSCREEN_LEFT ,  (float) (game.getWorld().mDropSection.mXBorderOffset) , MotionEquation.LOGISTIC, 800 , (idx+1)*delay);
			mStatsTitles.get(idx).triggerIntro(delay*idx);
			mStatsValues.get(idx).triggerIntro(delay*idx);
		}
		
		showStats = true;
		
	}
	
	public void outroInGameStats(Integer duration)
	{
		if (duration == null)
			duration = 100;
		
		game.getWorld().mDropSection.mStatsFogScreen.hide(duration);
		
		for(int idx = 0; idx < mStatsTitles.size(); idx++) 
		{
			mStatStarDrivers.get(idx).start((float) (game.getWorld().mDropSection.mXBorderOffset), STARSOFFSCREEN_LEFT , MotionEquation.LOGISTIC, 1, null );
			mStatsTitles.get(idx).outro(1); // not stock duration
			mStatsValues.get(idx).outro(1); // not stock duration
		}
		
		showStats = false;
	}
	
	public void introEndOfGameStats()
	{
		// dont show stats if it's already shown!
		if (mEOGStatsShown)
			return;
		
		// no need to reset the stats if they are already being shown
		if (!showStats)
		{
			//setStats(game.getWorld().mScreenHeight*0.6f);
			setStats( (float) (game.getWorld().mScreenHeight*( 1f - DropSectionState.PEEK_UNFOLDEDSCORE.getHeight() - DropSectionState.FOLDED.getHeight()/2 )));
		}
		
		// Fog Screen
		
		float fudge = (float) (yDelim/game.getWorld().mScreenHeight)*0.2f;
		float topPerc = (float) ( DropSectionState.FOLDED.getHeight()/2.0f + DropSectionState.PEEK_UNFOLDEDSCORE.getHeight() ) + fudge;
		float botPerc = (float) ( 1 - DropSectionState.FOLDED.getHeight()/2.0f ) - fudge  ;
		
		game.getWorld().mDropSection.mStatsFogScreen.start(
				topPerc, 
				botPerc, 
				(introDelay*6)/2
				);
		
		//game.getWorld().mDropSection.mStatsBackdrop.start(Color.TRANSPARENT, Color.GHOSTBLACK, introDelay*6);
		game.getWorld().mDropSection.mStatsFogScreen.start(Color.GHOSTBLACK, introDelay*6);
		
		
		// Show Stats
		
		for(int idx = 0; idx < mStatsTitles.size(); idx++) 
		{
			// only intro if the stats are already being shown
			if (!showStats)
			{
				mStatStarDrivers.get(idx).start( STARSOFFSCREEN_LEFT ,  (float) (game.getWorld().mDropSection.mXBorderOffset) , MotionEquation.LOGISTIC, 800 , (idx+1)*introDelay);
				mStatDrivers.get(idx).start(0f , (float) mStatDiverValues.get(idx), MotionEquation.LOGISTIC, null , (idx+1)*introDelay);
				mStatsTitles.get(idx).triggerIntro(introDelay*idx);
				mStatsValues.get(idx).triggerIntro(introDelay*idx);
			}
		}
		
		// final star set
		mFinalStarDriver.start(-3f ,  1f , MotionEquation.LOGISTIC, 1500 , 4*introDelay);
		
		// Score board
		
		if (game.getWorld().mBoards.mGameState.mScoreValue != 0 && game.getWorld().mBoards.mGameState.mGameDiff.getScoreMultiplier() != 1)
		{
			int scoreDelay = (mStatsTitles.size() + 1)*introDelay;
			
			mScoreDriver.start(
				(float) (game.getWorld().mBoards.mGameState.mScoreValue),
				(float) (game.getWorld().mBoards.mGameState.mFinalScoreValue), 
				MotionEquation.LOGISTIC, 
				null, 
				scoreDelay 
			);
			
			try {
				mValues.get(1).triggerClickAnimation( scoreDelay );
			} catch (NullPointerException e){}
		}
		
		// hide banner stars
		hideBannerStars();
		
		showStats = true;
		mEOGStatsShown = true;
	}
	
	private void drawStats(GL10 gl, float pixYOffset)
	{
		//game.text = "";
		
		for(int i = 0; i < mStatsTitles.size(); i++) 
		{
			//game.text += "   title\n";
			mStatsTitles.get(mStatsTitles.keyAt(i)).draw(gl, pixYOffset);
		}
		for(int i = 0; i < mStatsValues.size(); i++) 
		{
			//game.text += "   value\n";
			mStatsValues.get(mStatsValues.keyAt(i)).draw(gl, pixYOffset);
		}
		
		for(int i = 0; i < mStatStarDiverValues.size(); i++) 
		{
			game.getWorld().mStarSet.draw(gl, 
					mStatStarDiverValues.get(mStatStarDrivers.keyAt(i)), // # stars
					mStatStarDrivers.get(mStatStarDrivers.keyAt(i)).mCurrent , // X pos
						// y pos:
						mStatsValues.get(mStatsValues.keyAt(i)).yPos -
						game.getWorld().mStarSet.mScaledHeight[mStatStarDiverValues.get(mStatStarDrivers.keyAt(i))]/4+ 
						pixYOffset
					);	
		}
		
		
		// Final star set
		if (mFinalStarDriver != null && (game.getIsGameOver() || !game.getIsGameStarted()))
		{
			for (int i = 0; i < 3 ; i++)
			{
			game.getWorld().mFinalStarSet.draw(gl, 
					i<game.getWorld().mBoards.mGameState.mMinStarCount, // filled or not?
					 game.getWorld().mFinalStarSet.mStarPos[i]*mFinalStarDriver.mCurrent , // X pos
						// y pos:
					 
					 	// relative
					    (float) ( - (DropSectionState.FULL.getHeight() - DropSectionState.PEEK.getHeight() )*game.getWorld().mScreenHeight +
					    		(DropSectionState.FOLDED.getHeight()/4f )*game.getWorld().mScreenHeight -  game.getWorld().mFinalStarSet.mScaledHeight[0]/2f + game.getWorld().mDropSection.mPixBorderHeight*2  + 
								pixYOffset)
					 
					// absolute
					 /*
					    (float) (DropSectionState.FOLDED.getHeight()/4f*game.getWorld().mScreenHeight -
								game.getWorld().mFinalStarSet.mScaledHeight[0]/2f + game.getWorld().mDropSection.mPixBorderHeight*2)
								*/
					);	
			}
		}
		
		//game.textviewHandler.post( game.updateTextView );
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	//private long scratchElapsed;
	private GLButton<Void> tmpBtn;
	private Driver tmpDrv;
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		if (mCommitted)
		{
			
			//mValues.get(0).update(now, primaryThread, secondaryThread);
			
			// Score Banner
			
			for(int i = 0; i < mTitles.size(); i++) 
			{
				mTitles.get(mTitles.keyAt(i)).update(now, primaryThread, secondaryThread);
			}
			
			for(int i = 0; i < mValues.size(); i++) 
			{
				mValues.get(mValues.keyAt(i)).update(now, primaryThread, secondaryThread);
			}
			
			// Stats
			
			for(int i = 0; i < mStatsTitles.size(); i++) 
			{
				mStatsTitles.get(mStatsTitles.keyAt(i)).update(now, primaryThread, secondaryThread);
			}
			
			for(int i = 0; i < mStatsValues.size(); i++) 
			{
				mStatsValues.get(mStatsValues.keyAt(i)).update(now, primaryThread, secondaryThread);
			}
		
			
			for(int i = 0; i < mStatStarDrivers.size(); i++) 
			{
				//game.text += i + "\n";
				mStatStarDrivers.get(mStatStarDrivers.keyAt(i)).update(now, primaryThread, secondaryThread);
			}
			
			//game.text = "";
			for(int i = 0; i < mStatDrivers.size(); i++) 
			{
				//game.text += i + "\n";
				mStatDrivers.get(mStatDrivers.keyAt(i)).update(now, primaryThread, secondaryThread);
			
				if (primaryThread)
				{
					tmpDrv = mStatDrivers.get(i);
					tmpBtn = mStatsValues.get(i);
					
					if (tmpDrv != null && tmpBtn != null)
					{
						if (tmpBtn.mLabel != String.valueOf((int)(tmpDrv.mCurrent)))
						{
							mStatsValues.get(i).setLabel( String.valueOf((int)(mStatDrivers.get(i).mCurrent)) );
						}
					}
				}
				
			}
			
			
			// update score
			if (mScoreDriver != null)
			{
				mScoreDriver.update(now, primaryThread, secondaryThread);
				
				if (primaryThread)
				{
					if (mScoreDriver.isTransforming)
					{
						if (mValues.get(3).mLabel != String.valueOf((int)(mScoreDriver.mCurrent)))
						{
							mValues.get(3).setLabel( String.valueOf((int)(mScoreDriver.mCurrent)) );
						}
					}
				}
			}
			
			// update star drivers
			if (mStarDriver != null)
			{
				mStarDriver.update(now, primaryThread, secondaryThread);
			}
			if (mFinalStarDriver != null && (game.getIsGameOver() || !game.getIsGameStarted()))
			{
				mFinalStarDriver.update(now, primaryThread, secondaryThread);
				//game.text = mFinalStarDriver.mCurrent  + "\n";
				//game.textviewHandler.post( game.updateTextView );
			}
			//game.textviewHandler.post( game.updateTextView );
			
			// EOG Timer Banner
			
			mBannerText.update(now, primaryThread, secondaryThread);
		
			
			if (primaryThread)
			{
				//game.text = (game.getWorld().mBoards.mGameState.mEndOfGameDuration -  game.getWorld().mBoards.mGameState.mEndOfGameTimer.getElapsedTime()) + "\n";
				//game.textviewHandler.post( game.updateTextView );
				
				if (mBannerVisable)
				{
					if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.RACE_AGAINST_THE_CLOCK && (game.getWorld().mBoards.mGameState.mEndOfGameDuration -  game.getWorld().mBoards.mGameState.mEndOfGameTimer.getElapsedTime()) > 10000 )
						mBannerText.setLabel(game.getWorld().mBoards.mGameState.mScoreBannerText);
					else
						mBannerText.setLabel( String.valueOf( TimeModule.formatFineInterval( game.getWorld().mBoards.mGameState.mEndOfGameDuration - game.getWorld().mBoards.mGameState.mEndOfGameTimer.getElapsedTime() ) ) );
				}
			}
			
			else if (secondaryThread)
			{
				/*
				mValues.get(0).setLabel( String.valueOf( game.getWorld().mBoards.mGameState.mGameTimer.getFormattedElapsedTime() ) );
				
				GLButton<Void> tmp = mValues.get(0);
				game.text = String.valueOf(tmp.mWidth) + "   " + String.valueOf(game.getWorld().mDropSection.mFonts.getStringWidth(tmp.mFontName, tmp.mLabel)*tmp.mFontScaleOffset);
				game.textviewHandler.post( game.updateTextView );
				*/
				
				//game.text = "";
				
				if (!game.getIsGameOver())
				{
					if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.CLASSIC)
					{
						// running time
						mValues.get(0).setLabel( String.valueOf( game.getWorld().mBoards.mGameState.mGameTimer.getFormattedElapsedTime() ) );
						
						//game.text += mValues.get(0).mLabelEngine.mWordPercentage + "\n";
						//game.text += mTitles.get(0).mLabelEngine.mWordPercentage + "\n";
					}
					/*
					else if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.MATCH_LEVELS)
					{
						// level
						mValues.get(0).setLabel( String.valueOf( game.getWorld().mBoards.mGameState.mCurrentLevel ) );
						
						// matches
						mValues.get(1).setLabel( game.getWorld().mBoards.mGameState.mCurrentLevelMatchCount  + " of " + game.getWorld().mBoards.mGameState.mGameDiff.getLevelMatchQuota() );
					}
					*/
					else if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
					{
						// seconds left
						mValues.get(0).setLabel( String.valueOf( TimeModule.formatInterval( game.getWorld().mBoards.mGameState.mEndOfGameDuration - game.getWorld().mBoards.mGameState.mEndOfGameTimer.getElapsedTime() ) ) );
					
						if (!mBannerVisable)
						{
							if ((game.getWorld().mBoards.mGameState.mEndOfGameDuration -  game.getWorld().mBoards.mGameState.mEndOfGameTimer.getElapsedTime()) < 10000)
							{
								game.getWorld().mBoards.mGameState.mScoreBannerText = GameState.BANNERTEXT_NORMAL;
								mBannerText.setLabel(GameState.BANNERTEXT_NORMAL);
								
								outro();	// get rid of score
								showBanner(false, false);
								hideBannerStars();
								mBannerText.xPos = (int) (game.getWorld().mScreenWidth/2 - (game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.SCORE_FONT, game.getWorld().mBoards.mGameState.mScoreBannerText)*bannerScaleHeight)/2);
								mBannerText.setLabelDimensions();
							}
						}
					}
					/*
					else if (game.getWorld().mBoards.mGameState.mGameMode == GameMode.TIMED_LEVELS)
					{
						// level
						mValues.get(0).setLabel( String.valueOf( game.getWorld().mBoards.mGameState.mCurrentLevel ) );
						
						// seconds
						scratchElapsed = game.getWorld().mBoards.mGameState.mSpeedIncrementDuration - game.getWorld().mBoards.mGameState.mSpeedIncrementTimer.getElapsedTime();
						if (scratchElapsed > 0)
							mValues.get(1).setLabel( TimeModule.formatInterval( scratchElapsed ) );
						else
							mValues.get(1).setLabel("Leveling Up");
					}
					*/
					
					// score
					mValues.get(3).setLabel( String.valueOf( game.getWorld().mBoards.mGameState.mScoreValue ) );
				}
							
				//game.textviewHandler.post( game.updateTextView );
				
			}
		}
		
	}
	

	
	public void draw(GL10 gl, float pixYOffset)
	{

		if (mCommitted)
		{
			//game.text = game.getWorld().mDropSection.mBottomPercentage[DropSection.MENU_DRIVER] + "\n";
			if (game.getWorld().mDropSection.mBottomPercentage[DropSection.MENU_DRIVER] < 1f)
			{
				//game.text += "\n\nDRAW";
				
				for(int i = 0; i < mTitles.size(); i++) 
					mTitles.get(mTitles.keyAt(i)).draw(gl, pixYOffset);
				
				for(int i = 0; i < mValues.size(); i++) 
					mValues.get(mValues.keyAt(i)).draw(gl, pixYOffset);
				
				mBannerText.draw(gl, pixYOffset);
				
				// Stats!
				drawStats(gl, pixYOffset);
				

				if (mStarDriver.mCurrent < game.getWorld().mScreenWidth)
				{

					// Draw Stars
					game.getWorld().mStarSet.draw(gl, 
							game.getWorld().mBoards.mGameState.mMinStarCount, 
							mStarDriver.mCurrent  , 
								bottom + game.getWorld().mDropSection.mPixBorderHeight + 
								(game.getWorld().mDropSection.mYPixFoldedHeight - game.getWorld().mStarSet.mScaledHeight[game.getWorld().mBoards.mGameState.mMinStarCount])/2 + 
								pixYOffset
							);
				}	
			}
			//game.textviewHandler.post( game.updateTextView );

		}

		
	}
	
}
