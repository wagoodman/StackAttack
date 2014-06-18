package com.wagoodman.stackattack;

import java.util.ArrayList;
import java.util.Iterator;
import javax.microedition.khronos.opengles.GL10;



import android.content.Context;
import android.util.SparseIntArray;

public class BoardManager extends VirtualShape implements Iterable<Board>
{
	
	private Object mBoardMembersMutex = new Object();
	public ArrayList<Board> mBoardMembers = new ArrayList<Board>();
	
	private static final long serialVersionUID = 3700206787109111082L;
	private static final Boolean debug = false;
	private static final String TAG = "BoardMGR";

	public Boolean mEnableBoardProgression = true;
	
	// regulateIterationSpeed()
	private double[] mValidRPM = {2, 4, 4, 5, 6, 7, 8, 9, 10, 12, 15, 18, 20, 25, 30, 35};	// temp, game mode / difficulty really overrides this
	public int mRPMIndex = 0;
	
	// updateBoardProgression()
	public double mRowPerMin = mValidRPM[0];			// speed at which a row is added to the board per minute
	private long   mRowIterationStartTime = System.currentTimeMillis();
	private int    mRowIterationDuration = 10000;		// duration for each row iteration
	public double  mRowIterationPercentage = 0; 		// from 0 to 1, how complete is this iteration
	private int mIterationTimeElapsed = 0;

	
	// used for translating boards
	private Boolean mQuickenBoardProgression = false;
	public Boolean mPauseBoardProgression = false;
	private int mPauseBoardHoldElapsedTime = 0;
	private long mPauseBoardStartTime = 0;
	private int mPauseBoardDuration = 0;
	private long mResumeBoardTriggerTime = -1;		// pause the board until this time
	public float  mCurrentGLYOffset = 0;
	
	public int mCurrentGlobalRowIndex = 0; 
	
	
	// Board Score
	
	public GameState mGameState;
	private Object mBoardIncrementMutex = new Object();
	
	private BoardSideStatusManager mBoardStatusInfo;
	
	
	public BoardManager(Context ctxt)
	{
		super(ctxt);
		mGameState = new GameState(mContext);	
		mBoardStatusInfo = new BoardSideStatusManager(mContext);
	}
	
	public BoardManager(Context ctxt, GameDifficulty gd, GameMode gm)
	{
		super(ctxt);
		mGameState = new GameState(mContext, gd, gm);	
		mBoardStatusInfo = new BoardSideStatusManager(mContext);
	}
	
	
	public Iterator<Board> iterator() 
	{        
        return mBoardMembers.iterator();
    }
	
	// diff/mode INTERFACE
	
	public void setRPMValues(double[] rpmValues, int currentIdx)
	{
		mValidRPM = rpmValues.clone();
		mRowPerMin = mValidRPM[0];
		mRPMIndex = currentIdx;
		updateIterationSpeed();
	}
	public void setRPMValues(double[] rpmValues)
	{
		mValidRPM = rpmValues.clone();
		mRowPerMin = mValidRPM[0];
		mRPMIndex = 0;
		updateIterationSpeed();
	}
	
	public void enableGarbage()
	{
		for (int idx=0 ; idx < mBoardMembers.size() ; idx++)
		{
			mBoardMembers.get(idx).mGarbageGenerator.isEnabled = true;
		}
	}
	
	public void disableGarbage()
	{
		for (int idx=0 ; idx < mBoardMembers.size() ; idx++)
		{
			mBoardMembers.get(idx).mGarbageGenerator.isEnabled = false;
		}
	}
	
	public void enableBonusBlocks()
	{
		for (int idx=0 ; idx < mBoardMembers.size() ; idx++)
		{
			mBoardMembers.get(idx).mBonusBlockManager.isEnabled = true;
		}
	}
	
	public void disableBonusBlocks()
	{
		for (int idx=0 ; idx < mBoardMembers.size() ; idx++)
		{
			mBoardMembers.get(idx).mBonusBlockManager.isEnabled = false;
		}
	}
	
	// game INTERFACE
	
	public void startGame()
	{
		startGame(false);
	}
	
	public void startGame(Boolean doTutorial)
	{
		// no two finger movement
		lock();
		
		// always enable board progression
		mEnableBoardProgression = true;
		
		// set upright, just in case the last game ended while upside down and did not right the board
		game.getWorld().mBoards.setNextOrientation(Orientation.NORMAL);
		game.setGlobalOrient( Orientation.NORMAL );
	
		mGameState.startGame(!doTutorial);
		

		if (!doTutorial)
		{
			// now that a game state has been saved, commit the banner!
			game.getWorld().mDropSection.mScoreBanner.commitBanner();
		}

		if (!doTutorial)
		{

			for (int idx=0; idx < mBoardMembers.size(); idx++)
			{
				// make a new board
				mBoardMembers.set(idx, new Board(mContext));	
	
				if (!mBoardMembers.get(idx).isPopualted())
				{
					mBoardMembers.get(idx).populateBoard();
				}	
			}
		}
		else
		{
			
		}
		
		if (!doTutorial)
			tour();
		
	}
	
	public void doStartGame()
	{
		// allow two finger movement
		unlock();
		
		// show start
		mGameState.doStartGame();
		
		// resume
		mRowIterationStartTime = System.currentTimeMillis();	// reset!
		resumeBoardProgression(true, true);
	}
	
	public void endGame()
	{
		
		// clear indicators
		mBoardStatusInfo.endGame();
		
		game.getWorld().mMenus.transitionToMenu(MenuManager.ENDOFGAMEMENU, true, DropSection.ENDGAME_DURATION, DropSection.SLOW_EQ);
		transitionDown(DropSection.SLOW_EQ, DropSection.ENDGAME_DURATION);
		
		// trigger end of game for each board
		for (int idx=0 ; idx < mBoardMembers.size() ; idx++)
		{
			if (mCurrentFaces.contains((Object)idx))
			{
				// show animation
				mBoardMembers.get(idx).destroy(true);
			}
			else
			{
				// dont animate
				mBoardMembers.get(idx).destroy(false);
			}
		}

	}
	
	
	public void pauseGame()
	{
		mHorizontalTransformingEnabled = false;
		pauseBoardProgression();
		
		mGameState.pause(getId());
	}
	
	public void resumeGame()
	{
		mHorizontalTransformingEnabled = true;
		resumeBoardProgression(true, false);
		
		mGameState.resume(getId());
	}
	
	
	public boolean addBoard(Board board)
	{
		try
		{
			if (mBoardMembers.add(board))
			{
				updateFaces( mBoardMembers.size() );
				return true;
			}
		}
		catch(Exception e)
		{
			//Log.e(e.toString());
		}
		
		return false;
	
	}
	

	public void addBoard(int idx, Board board)
	{

		try
		{
			mBoardMembers.add(idx, board);
			// mind the virtual shape!
			updateFaces( mBoardMembers.size() );
		}
		catch(Exception e)
		{
			//Log.e(e.toString());
		}
		
	}
	
	public void clearAllBoards()
	{

		try
		{
			mBoardMembers.clear();
			// mind the virtual shape!
			updateFaces( mBoardMembers.size() );
		}
		catch(Exception e)
		{
			//Log.e(e.toString());
		}
		
	}
	
	
	/**
	 * Returns the board that can be seen (represented by mCurrentFace)
	 * 
	 * @return
	 */
	public Board getCurrentBoard()
	{
		try
		{
			return mBoardMembers.get( (int) Math.round(mCurrentFace) % numFaces );
		}
		catch(Exception e)
		{
			
		}
		
		return null;
	}
	

	public Board getBoard(int index)
	{
		try
		{
			return mBoardMembers.get( index );
		}
		catch(Exception e)
		{
			
		}
		
		return null;
	}
	

	public Board removeBoard(int idx)
	{
		try
		{
			if (mBoardMembers.get(idx) != null)
			{
				// mind the virtual shape!
				updateFaces( mBoardMembers.size()-1 );
				return mBoardMembers.remove(idx);
			}
		}
		catch(Exception e)
		{
			//Log.e(e.toString());
		}
		
		return null;
	
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Virtual Shape Delegators

	private float tmpDegree;
	private float tmpCurrentFace;
	private int tmpFace;
	public Boolean hintHorizontalDirection(float deltaDegree)
	{
		if (isLocked)
			return false;
		
		if (deltaDegree + mCurrentHorizontalDegreeOffset < 0)
			tmpDegree = mCurrentHorizontalDegreeOffset + (deltaDegree + 360);
		else
			tmpDegree = mCurrentHorizontalDegreeOffset + deltaDegree;
		
		tmpCurrentFace =  (tmpDegree/mDelimitingDegree) == numFaces ? 0 :  (tmpDegree/mDelimitingDegree)  ;

		tmpFace = (int)(tmpCurrentFace);
		
		// moving right
		if (deltaDegree >= 0)
			tmpFace++;
		
		
		/*
		game.text = "\n\n\n\n\n\n\nMembers:\n";
		for (int idx=0; idx < mBoardMembers.size(); idx++)
		{
			game.text += "   " + idx + " : " + mBoardMembers.get(idx).getId() + "\n";
		}
		game.text += "\n";
		game.text += "Current Face: " + tmpCurrentFace + "\n";
		game.text += "TMP Face: " + tmpFace + "\n";
		game.text += "DELTA   : " + deltaDegree + "\n";
		game.textviewHandler.post( game.updateTextView );
		*/
		
		
		if (mBoardMembers.size() > tmpFace && tmpFace >= 0)
		{
			if ( mBoardMembers.get( tmpFace ) != null )
			{
				augmentHorizontalOffsetDegree(deltaDegree);
				
				//game.text += "HINT ACCPTED\n";
				//game.textviewHandler.post( game.updateTextView );
				
				return true;
			}
		}
		
		//game.text += "HINT REJECTED\n";
		//game.textviewHandler.post( game.updateTextView );
		
		return false;
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Board Delegators (available to current board only!)
	
	Board board_setNextOrient;
	public void setNextOrientation(Orientation orient)
	{
		// only current board
		/*
		Board board = getCurrentBoard();
		if (board != null)
			board.setNextOrientation(orient);
		*/
		
		// all boards
		for (int idx=0 ; idx < mBoardMembers.size() ; idx++)
		{
			board_setNextOrient = mBoardMembers.get(idx);
			board_setNextOrient.setNextOrientation(orient);
		}
		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Block Delegators (available to current board only!)
	
	
	public Boolean pickupBlock( Coord<Integer> rowcol )
	{
		Boolean ret = false;
		
		Board board = getCurrentBoard();
		if (board != null)
			ret = board.pickupBlock(rowcol);
		
		// play sound
		if (ret)
			game.getWorld().playSound(World.SOUND_PICKUP);
		
		return ret;
	}
	

	
	public Boolean dropActiveBlock(Coord<Integer> fingerRowCol)
	{
		Boolean ret = false;
		
		Board board = getCurrentBoard();
		if (board != null)
			ret = board.dropActiveBlock(fingerRowCol, false);
		
		// play sound
		if (ret)
			game.getWorld().playSound(World.SOUND_DROP);
		
		return ret;
	}
	
	public Boolean swapBlocks(Coord<Integer> rowcol1, Coord<Integer> rowcol2)
	{
		Board board = getCurrentBoard();
		if (board != null)
			return board.swapBlocks(rowcol1, rowcol2);
		
		
		return false;
	}

	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update & Draw functions used by worker threads
	
	private Boolean isSpringing = false;
	private long	mSpringStartTime;
	private int		mSpringNumBlocks = 1;
	public void startGarbageDropSpringAnimation(int numGarbageBlocks, int delay)
	{
		if (!isSpringing)
		{
			// collect info
			mSpringNumBlocks = numGarbageBlocks;
			
			// start
			if (delay == 0)
				mSpringStartTime = System.currentTimeMillis();
			else
				mSpringStartTime = System.currentTimeMillis() + delay;
			isSpringing = true;
		}
	}
	
	private float t = 0;
	private final double k=0.01, m=80.0, c=1.0, a=-0.2, wo=Math.sqrt(k/m), T= c/(2.0*m*wo), u=wo*Math.sqrt(1-(T*T));
	private float processSpringAnimation(long now)
	{
		if (isSpringing)
		{
			t = now - mSpringStartTime;
			
			// duration = 1000
			if (t >= 1000)
				isSpringing = false;
			else if (t < 0)
				return 0;
			
			/*
			t = var('t')
			k = 0.02
			m = 100
			wo = sqrt(k/m) 
			c = 1
			T = (c/(2*m*wo))
			u = wo*sqrt(1-(T^2))
			numBlocks = 3
			a = -5 * numBlocks
			x = a*sin(u*t)*e^(-T*wo*t)/3
			duration = 1000#10+4*wo*m*(1/T)
			plot(x, (t,0,duration))
			*/
			
			return  (float) (mSpringNumBlocks*a*Math.sin(u*t)*Math.pow(Math.E, (-T*wo*t)/3.0))  * game.getWorld().mGLBlockLength ;
		}
		
		return 0;
	}
	
	
	// control iteration (not duration)
	public void regulateIterationSpeed(long now)
	{
		// query the game state, ready to speed up?
		if ( mGameState.isReadyToSpeedUp(now) )
		{
			// up the rpm
			mRPMIndex++;
			mRowPerMin = mValidRPM[ Math.min( mRPMIndex, mValidRPM.length -1 ) ];
			updateIterationSpeed();
		}

	}
	
	public void setIterationSpeed(int rpmIndex)
	{
		mRPMIndex = rpmIndex;
		mRowPerMin = mValidRPM[ Math.min( mRPMIndex, mValidRPM.length -1 ) ];
		updateIterationSpeed();
	}
	
	// control duration
	public void updateIterationSpeed()
	{
		mRowIterationDuration = (int) ((60.0/mRowPerMin)*1000);
	}
	
	
	
	private Orientation prevOrientation = Orientation.NORMAL, 
						pausedOrientation = Orientation.NORMAL,
						currentOrientation = Orientation.NORMAL;
	
	
	public void pauseBoardProgression(int duration)
	{
		/*
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		for (int idx=0; idx < stackTraceElements.length ; idx++)
		{
			game.text = "trace: " + idx + "  --->   " + "method: " + stackTraceElements[idx].getMethodName() + "\n" + game.text;
		}
		game.text = "--------------------\n" + game.text;
		game.textviewHandler.post( game.updateTextView );
		*/
		
		if (mPauseBoardProgression == false)
		{
			pausedOrientation = game.getGlobalOrient();
			// only reset pause timer if is not already paused
			pauseBoardProgression();		
			
			mResumeBoardTriggerTime = mPauseBoardStartTime + duration;
		}
		else
		{
			double curWaitTime = mResumeBoardTriggerTime - System.currentTimeMillis();
			
			if (curWaitTime-duration >=0 )
			{
				// already paused, just increment trigger
				mResumeBoardTriggerTime += Math.min( curWaitTime-duration , duration);
			}
			else
			{
				mResumeBoardTriggerTime = System.currentTimeMillis() + duration;
			}
		}
		
		
	}
	
	public void pauseBoardProgression()
	{
		mPauseBoardHoldElapsedTime = mIterationTimeElapsed;
		mPauseBoardProgression = true;
		mPauseBoardStartTime = System.currentTimeMillis();
	}
	
	// return: did it work?
	public Boolean resumeBoardProgression()
	{
		return resumeBoardProgression(false, false);
	}

	// return: did it work?
	public Boolean resumeBoardProgression(Boolean force, Boolean reset)
	{
		
		// only resume if there is more game to play! (or not paused by menu)
		if (!game.getIsGameOver() && !game.getIsGamePaused())
		{
			if (
					(!force && mPauseBoardProgression && System.currentTimeMillis() >= mResumeBoardTriggerTime && mResumeBoardTriggerTime != -1)
					||
					force
				)
			{
				// keep track of running pauses (until the row increments)
				if (!reset)
					mPauseBoardDuration += (int) (System.currentTimeMillis() - mPauseBoardStartTime);
				else
					mPauseBoardDuration = 0;
				
				mResumeBoardTriggerTime = -1;
				
				mPauseBoardProgression = false;
				
				return true;
			}
		}
		
		return false;
	}
	
	public void quickenBoardProgression()
	{
		mQuickenBoardProgression = true;
	}
	
	
	public void resetBoardProgression()
	{
		
		// reset! 
		mRowIterationStartTime = System.currentTimeMillis(); 	// This gets augmented for pauses HERE!
		mPauseBoardDuration = 0;
		mQuickenBoardProgression = false;
		
		// update this global index
		synchronized (mBoardIncrementMutex)
		{
			mCurrentGlobalRowIndex = 0;
			
			// reversed orientation means the offset does *not* reset to 0!
			//mCurrentGLYOffset = 0;
			
			mRowIterationPercentage = 0;
			if (currentOrientation == Orientation.NORMAL)
				mCurrentGLYOffset = (float) (mRowIterationPercentage*game.getWorld().mGLBlockLength);
			else
				mCurrentGLYOffset = (float) (game.getWorld().mGLBlockLength - (mRowIterationPercentage*game.getWorld().mGLBlockLength));
		
		}
		
	}
	
	private void setGLYOffset(long now)
	{

		// quickly get to the next iteration
		if (mQuickenBoardProgression && !mPauseBoardProgression)
			mRowIterationStartTime -= mRowIterationDuration/20;
		
		// determine elapsed time (based off of current / previous pauses)
		if (!mPauseBoardProgression)
			mIterationTimeElapsed = (int) Math.max( 0, now - mRowIterationStartTime ) - mPauseBoardDuration;
		else
			mIterationTimeElapsed =  mPauseBoardHoldElapsedTime;
		
		mIterationTimeElapsed = (int) Math.max( 0, Math.min(mRowIterationDuration, mIterationTimeElapsed)  );
		
		// figure how far through the progression we are
		mRowIterationPercentage = mIterationTimeElapsed / (double) mRowIterationDuration;

		/*
		game.text = "\n\n\n\n\n\n\n" + now + "\n";
		game.text += "paused? " + mPauseBoardProgression + "\n";
		game.text += "  %   : " + (int)(mRowIterationPercentage*100) + "\n";
		game.text += "Start : " + mRowIterationStartTime + "\n";
		game.text += "It Elp: " + mIterationTimeElapsed + " : " + ( (now - mRowIterationStartTime ) - mPauseBoardDuration) +  "\n";
		game.text += "Ps Elp: " + mPauseBoardHoldElapsedTime + "\n";
		game.text += "Ps Dur: " + mPauseBoardDuration + "\n";
		game.textviewHandler.post( game.updateTextView );
		*/
		
		/*
		game.text  = "  %   : " + (int)(mRowIterationPercentage*100) + "\n";
		game.text += "Start : " + mRowIterationStartTime + "\n";
		game.text += "It Elp: " + mIterationTimeElapsed + "\n";
		game.text += "Ps Elp: " + mPauseBoardHoldElapsedTime + "\n";
		game.text += "Ps Dur: " + mPauseBoardDuration + "\n";
		game.text += "EOG   : " + (now - mEndOfGameStartTime) + "\n";
		game.text += "EOG St: " + (mEndOfGameStartTime) + "\n";
		game.textviewHandler.post( game.updateTextView );
		*/
		
		// figure the distance proportional to the progression
		if (currentOrientation == Orientation.NORMAL)
			mCurrentGLYOffset = (float) (mRowIterationPercentage*game.getWorld().mGLBlockLength);
		else
			mCurrentGLYOffset = (float) (game.getWorld().mGLBlockLength - (mRowIterationPercentage*game.getWorld().mGLBlockLength));
		
		// continue springing! 
		if (isSpringing)
		{
			if (currentOrientation == Orientation.NORMAL)
				mCurrentGLYOffset += processSpringAnimation(now);
			else
				mCurrentGLYOffset -= processSpringAnimation(now);
		}
	}
	
	public Boolean areAllTopRowsEmpty()
	{
		for (Board board : mBoardMembers)
		{
			if (board.isTopRowEmpty() == false)
				return false;
		}
		return true;
	}
	
	Boolean update_increment, update_preventEOGTimer;
	private void updateRowProgression(long now)
	{
		//game.text = mPauseBoardProgression + "\n";
		//game.text += "now >= mResumeBoardTriggerTime && mResumeBoardTriggerTime != -1\n";
		//game.text += "now                       : " + now + "\n"; 
		//game.text += "mResumeBoardTriggerTime   : " + mResumeBoardTriggerTime + "\n"; 
		//game.text += "trig - now                : " + (mResumeBoardTriggerTime-now) + "\n"; 
		//game.textviewHandler.post( game.updateTextView );
		
		// no no, do things....
		// If paused, do nothing!
		//if (game.isGamePaused)
		//	return;
		
		if (!mEnableBoardProgression)
			return;
		
		// trigger resume (if needed)
		if (mPauseBoardProgression && now >= mResumeBoardTriggerTime && mResumeBoardTriggerTime != -1)
			resumeBoardProgression(true, false);

		
		// set orientation
		if (mPauseBoardProgression)
			currentOrientation = pausedOrientation;
		else
			currentOrientation = game.getGlobalOrient();
		
		//game.text = currentOrientation + "   " + mPauseBoardProgression;
		//game.textviewHandler.post( game.updateTextView );
		
		
		// settle from current position on board flip
		if (prevOrientation != currentOrientation)
		{
			mRowIterationStartTime = (long) (now - ((1-mRowIterationPercentage)*mRowIterationDuration));
			
		}
		prevOrientation = currentOrientation;
	
		// TEMP TEMP TEMP
		// end of game countdown
		//if ( mEndOfGameStartTime != -1)
		//{
			// end game!
		//	game.text = (now-mEndOfGameStartTime) + " > " + mEndOfGameDuration;
		//	game.textviewHandler.post( game.updateTextView );
		//}
		
		
		// regulate Offset and supporting variables
		setGLYOffset(now);
		
		//game.text = "\n\n\n\n\n\n\n";
		
		//game.text = isSpringing + "  :  " + processSpringAnimation(now);
		//game.textviewHandler.post( game.updateTextView );
		
		
		//game.text = elapsed + " >= " + mRowIterationDuration + "... ";
		
		// restart
		if (Math.max( 0, Math.min( mIterationTimeElapsed, mRowIterationDuration+10 ) ) >= mRowIterationDuration)
		{
			//game.text += "Restarting";
			
			// add new row... pause first!
			pauseBoardProgression();
			update_increment = true;
			for (Board board : mBoardMembers)
				if (!board.readyToIncrementBoard())
					update_increment = false;

			
			//game.text += "\nReady To Increment: " + String.valueOf(update_increment) + "\n";
			
					
			// if ready to increment, do so & resume/reset
			if (update_increment)
			{
				// reset score board if needed!
				
				//game.text += "   " + "UPDATE...\n";
				
				//game.text += mCurrentGlobalRowIndex + " > " + (mCurrentGlobalRowIndex+1) + "  at  " + (now - game.mGameStartTime) + "\n";
				//game.textviewHandler.post( game.updateTextView );
				
				// change speed before resetting variables
				regulateIterationSpeed(now);
				
				// reset! 
				mRowIterationStartTime = System.currentTimeMillis(); 	// This gets augmented for pauses HERE!
				mPauseBoardDuration = 0;
				mQuickenBoardProgression = false;
				game.getWorld().mBoards.mGameState.incrementRowAction();
				
				// update this global index
				synchronized (mBoardIncrementMutex)
				{
					if (currentOrientation == Orientation.NORMAL)
						mCurrentGlobalRowIndex++;
					else
						mCurrentGlobalRowIndex--;
					
					// reversed orientation means the offset does *not* reset to 0!
					//mCurrentGLYOffset = 0;
					
					mRowIterationPercentage = 0;
					if (currentOrientation == Orientation.NORMAL)
						mCurrentGLYOffset = (float) (mRowIterationPercentage*game.getWorld().mGLBlockLength);
					else
						mCurrentGLYOffset = (float) (game.getWorld().mGLBlockLength - (mRowIterationPercentage*game.getWorld().mGLBlockLength));
				
					// increment
					for (Board board : mBoardMembers)
					{
						board.incrementBoard(mCurrentGlobalRowIndex);
					}
					
					synchronized(mGameState.mLevelMutex)
					{
						// Set Rows Played Stats
						mGameState.mRowsPlayedStars = mGameState.mGameDiff.getRowsPlayedStarStat(game.getWorld().mBoards.mCurrentGlobalRowIndex);
						mGameState.mMinStarCount = Math.min(mGameState.mRowsPlayedStars, Math.min(mGameState.mBlocksDestoyedStars, mGameState.mLargestMatchStars));
					}
				}
				
				resumeBoardProgression(true, false);

			}
			else
			{
				if (mGameState.mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
				{
					game.getWorld().mBoards.mGameState.cannotIncrementRowAction( false );
				}
				else
				{
					// not ready to increment; start end of game timer (if it is not already started)
					if ( !game.getWorld().mBoards.mGameState.mEndOfGameTimer.isStarted )
					{
						
						//game.text += "   " + "Timer **NOT** Started...   ";
						
						update_preventEOGTimer = false;
						for (Board board : mBoardMembers)
							if (board.mGroups.isAnyGroupMelting() || board.mBonusBlockManager.isProcessingRowIncrementRestrictedBonusBlock())
								update_preventEOGTimer = true;
								
						//game.text += "preventTimer: " + String.valueOf(update_preventEOGTimer) + "\n";
								
						// start end of game timer (unless there is melting garbage / bonus blocks being matched)
						game.getWorld().mBoards.mGameState.cannotIncrementRowAction( update_preventEOGTimer );
					}
					// end of game timer stated... should it still be going?
					else if ( game.getWorld().mBoards.mGameState.mEndOfGameTimer.isStarted )
					{
						//game.text += "   " + "Timer Started...   ";
						
						update_preventEOGTimer = false;
						for (Board board : mBoardMembers)
							if (board.mGroups.isAnyGroupMelting() || board.mBonusBlockManager.isProcessingRowIncrementRestrictedBonusBlock())
								update_preventEOGTimer = true;
						
						//game.text += "preventTimer: " + String.valueOf(update_preventEOGTimer) + "\n";
								
						// pause end of game timer if there is any melting blocks / bonus blocks being matched
						if (update_preventEOGTimer && !game.getWorld().mBoards.mGameState.mEndOfGameTimer.isPaused)
						{
							game.getWorld().mBoards.mGameState.mEndOfGameTimer.pause();
						}
						// resume end of game timer if there is nothing preventing EOG
						else if (!update_preventEOGTimer && game.getWorld().mBoards.mGameState.mEndOfGameTimer.isPaused)
						{
							game.getWorld().mBoards.mGameState.mEndOfGameTimer.resume();
						}
						
					}
				}
			}

		}
		
		//game.text += "\nRow Index: " + mCurrentGlobalRowIndex;
		
		//game.textviewHandler.post( game.updateTextView );
		
	}
	
	private Board scratchVisibleUpdateBoard, scratchSecondaryUpdateBoard;
	private SparseIntArray highestRowCounts = new SparseIntArray();
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		// TEMP TEMP TEMP
		//if (secondaryThread)
		//	game.text = "\n\n\n\n\n\n\nVisible Update (Secondary):\n";
		
		/*
		if (primaryThread)
		{
			game.text = "\n\n\n\n\n\n\n" + now + "\n";
			for (int face=0; face<numFaces; face++)
			{
				game.text = "Face: " + face + "   " + mBoardMembers.get(face).getId() +  "\n";
				game.textviewHandler.post( game.updateTextView );
			}
		}
		*/
		
		// update for all faces...
		
		// row iterations...
		if (primaryThread)
			updateRowProgression(now);
		
		
		// update for only some faces...
		
		for (int face=0; face<numFaces; face++)
		{
			// select visible, adjacent boards (only update these)
			if (
					// left moving 2 faces
					( mCurrentFace > (face-1) && mCurrentFace <= (face) ) ||
					
					// right moving 2 faces
					( mCurrentFace < (face+1) && mCurrentFace >= (face) ) ||
					
					// or moving across 360-0 barrier (either direction)
					( (face==0 || face==numFaces-1) && mCurrentFace > (numFaces-1) )
					
				)
			{

				if (primaryThread)
				{
					if (!mCurrentFaces.contains(face))
						mCurrentFaces.add(face);
					
					// update virtual shape
					updateVS(now, primaryThread, secondaryThread);
					
				}

				
				// update **visible** face, pass whether or not the virtual shape is transforming to determine if interactable
				//try
				//{
					if (face < mBoardMembers.size())
					{
						scratchVisibleUpdateBoard = mBoardMembers.get(face);
						if (scratchVisibleUpdateBoard != null)
						{
							scratchVisibleUpdateBoard.update(now, primaryThread, secondaryThread, !isTransformingHorizontal, true);
							// TEMP TEMP TEMP
							//if (secondaryThread)
							//	game.text += "   "+face+"\n";
						}
					}
				//}
				//catch (Exception e)
				//{
				//	Log.e("BOARD UPDATE", e.toString());
				//}
				
				
			}
			else if (primaryThread && mCurrentFaces.contains(face))
			{
				mCurrentFaces.remove((Object) face);
			}
		}
		
		if (secondaryThread)
		{
			
			
			// TEMP TEMP TEMP
			//game.text += "\nNON-Visible Update (Secondary):\n";
			
			highestRowCounts.clear();
			
			// for all boards...
			for (int face=0 ; face < mBoardMembers.size() ; face++)
			{
				scratchSecondaryUpdateBoard = mBoardMembers.get(face);
				
				// update all non visable boards
				if (
						// not!
						!(
							// left moving 2 faces
							( mCurrentFace > (face-1) && mCurrentFace <= (face) ) ||
							
							// right moving 2 faces
							( mCurrentFace < (face+1) && mCurrentFace >= (face) ) 
						)
					)
				{
					//scratchSecondaryUpdateBoard.update(now, primaryThread, secondaryThread, !isTransformingHorizontal);
					// fake process the non-visable boards as primary updaters. Less work for the developer! Joy me!
					scratchSecondaryUpdateBoard.update(now, true, true, !isTransformingHorizontal, false);
					// TEMP TEMP TEMP
					//game.text += "   "+face+"\n";
				}
				
				// TEMP TEMP TEMP
				//if (idx == 0 && game.getWorld().mDropSection.mLeftBoardIndicator != null)
				//	game.getWorld().mDropSection.mLeftBoardIndicator.updateRow(scratchSecondaryUpdateBoard.getHighestRow());
				
				// collect side indicator counts
				highestRowCounts.append(face, scratchSecondaryUpdateBoard.getHighestRow());
				
				//game.text += idx + " (" + scratchSecondaryUpdateBoard.getId() + ") : " + scratchSecondaryUpdateBoard.getHighestRow() + " of " + CA_Game.ROWCOUNT + "\n";
				
				// delete if needed
				if (scratchSecondaryUpdateBoard.isDeleted)
				{
					// to keep draw/update in check!
					if (mCurrentFaces.contains((Object)face))
						mCurrentFaces.remove((Object)face);
					
					// never delete the first board
					if (face == 0)
					{
						mBoardMembers.set(0, new Board(mContext));
						mCurrentFaces.add(0,0);
					}
					else
					{
						synchronized(mBoardMembersMutex)
						{
							removeBoard(face);
						}
					}
				}
			}
			
			// update the side board indicators
			mBoardStatusInfo.update(highestRowCounts, mCurrentFaces);
			
			// TEMP TEMP TEMP
			//game.textviewHandler.post( game.updateTextView );
			
			
			/// why temp???
			// TEMP TEMP TEMP all boards updated, so update game state
			game.getWorld().mBoards.mGameState.update(now);
		}
		
		/*
		game.text = "";
		for (Integer face : mCurrentFaces)
		{
			game.text += face + "  \n" + mBoardMembers.get(face).report() + "  \n\n";
		}
		game.textviewHandler.post( game.updateTextView );
		*/
		
	}
	

	float scratchYOffset = 0;
	Board scratchDrawBoard;
	public void draw(GL10 gl)
	{


		
		// drawing process for all faces...
		
		synchronized (mBoardIncrementMutex)
		{
			// row iterations...
			scratchYOffset = mCurrentGLYOffset + (game.getWorld().mGLBlockLength*0.9917f)*mCurrentGlobalRowIndex;
			//gl.glTranslatef(0, mCurrentGLYOffset + game.getWorld().mGLBlockLength*mCurrentGlobalRowIndex, 0);
			//gl.glTranslatef(0, mCurrentGLYOffset + game.getWorld().mGLBlockLength*game.getWorld().mBoards.getCurrentBoard().mCurrentRowIndex, 0);

		}
		
		
		// drawing process for some faces...
		
		
		//game.text = "";
		
		
		synchronized(mBoardMembersMutex)
		{
			
			
			/*
			gl.glPushMatrix();
			
			
			// Rotate to face about a point in space... change 'mVirtualShapeCenterDepth' to a positive or negative
			// distance to rotate about that point (like on the outside or inside of a cube)
			gl.glTranslatef(0, 0, mVirtualShapeCenterDepth);
			
			gl.glTranslatef(0, -(mCurrentGLYOffset), 0);
			
			// translate to...
			gl.glTranslatef(0, 
					(mCurrentGLYOffset + game.getWorld().mGLBlockLength*(CA_Game.ROWCOUNT/4)), // bottom of the board (ish)
					-mVirtualShapeCenterDepth + mBoardDepth //at the board depth
					);

			
			gl.glTranslatef(0, mCurrentVerticalOffset, 0 );

			
			gl.glRotatef( -90 + mCurrentVerticalDegreeOffset , 1, 0, 0); 	// Vertical
			
			// get off the board and move back to the center
			gl.glTranslatef(0, 
					-(mCurrentGLYOffset + game.getWorld().mGLBlockLength*(CA_Game.ROWCOUNT/4)), 
					mVirtualShapeCenterDepth - mBoardDepth
					);

			// put vertical offset back
			//gl.glTranslatef(0, mCurrentGLYOffset + game.getWorld().mGLBlockLength*mCurrentGlobalRowIndex, 0);
							
			// reset to shape center
			gl.glTranslatef(0, 0, -mVirtualShapeCenterDepth);
			
			gl.glPushMatrix();
			// go to max depth
			gl.glTranslatef(0, 0,  mBackdropDepth );
			mBackgroundBlock.draw(gl);
			// reset maxDepth
			//gl.glTranslatef(0, 0, -mBackdropDepth );
			gl.glPopMatrix();

				
			gl.glPopMatrix();
			*/
			
			
			
			for (Integer face : mCurrentFaces)
			{
	
				//game.text += String.valueOf(face) + ", ";
				
				
				gl.glPushMatrix();
				
				
				// Rotate to face about a point in space... change 'mVirtualShapeCenterDepth' to a positive or negative
				// distance to rotate about that point (like on the outside or inside of a cube)
				gl.glTranslatef(0, 0, mVirtualShapeCenterDepth);
				gl.glRotatef( (float) (face * mDelimitingDegree) - mCurrentHorizontalDegreeOffset, 0, 1, 0); 	//Horizontal
				
				// cancel vertical offset before rotating in the vertical direction
				//gl.glTranslatef(0, -(mCurrentGLYOffset + game.getWorld().mGLBlockLength*mCurrentGlobalRowIndex), 0);
				
				// translate to...
				gl.glTranslatef(0, 
						-(mCurrentGLYOffset + game.getWorld().mGLBlockLength*MainActivity.ROWCOUNT/4), // bottom of the board (ish)
						-mVirtualShapeCenterDepth + mBoardDepth //at the board depth
						);
	
				
				gl.glTranslatef(0, mCurrentVerticalOffset, 0 );
	
				
				gl.glRotatef( mCurrentVerticalDegreeOffset, 1, 0, 0); 	// Vertical
				
				// get off the board and move back to the center
				gl.glTranslatef(0, 
						(mCurrentGLYOffset + game.getWorld().mGLBlockLength*MainActivity.ROWCOUNT/4), 
						mVirtualShapeCenterDepth - mBoardDepth
						);
	
				// put vertical offset back
				//gl.glTranslatef(0, mCurrentGLYOffset + game.getWorld().mGLBlockLength*mCurrentGlobalRowIndex, 0);
								
				// reset to shape center
				gl.glTranslatef(0, 0, -mVirtualShapeCenterDepth);
				
				gl.glPushMatrix();
				// go to max depth
				gl.glTranslatef(0, 0,  mBackdropDepth );
				mBackgroundBlock.draw(gl);
				// reset maxDepth
				//gl.glTranslatef(0, 0, -mBackdropDepth );
				gl.glPopMatrix();
				
				// all faces translate to the same height
				gl.glTranslatef(0, scratchYOffset, 0);
				
				// add boarder
				gl.glTranslatef(0, 0, mVirtualShapeBoarder );
				
				
				// draw board
				//try
				//{
					
							
					if (face < mBoardMembers.size())
					{
						scratchDrawBoard = mBoardMembers.get(face);
						if (scratchDrawBoard != null)
						{
							scratchDrawBoard.draw(gl);
							
							
							
						}
					}
					
					
				//}
				//catch (Exception e)
				//{
				//	Log.e("BOARD DRAW", e.toString());
				//}
				
				
				// draw widgets
				gl.glPushMatrix();
				
				// draw 2D upon the last current face; 3D all the time
				if (mCurrentFaces.size() > 0)
					game.getWorld().mWidgetLayer.draw(gl, face == mCurrentFaces.get(mCurrentFaces.size() - 1));
				
				gl.glPopMatrix();
				
				gl.glPopMatrix();
				
			}


			

			
		}
			
		
		//game.textviewHandler.post( game.updateTextView );
		
		
	}

	

}
