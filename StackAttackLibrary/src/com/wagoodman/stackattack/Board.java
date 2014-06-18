package com.wagoodman.stackattack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import javax.microedition.khronos.opengles.GL10;


import android.content.Context;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class Board extends Identity
{
	private static final String TAG = "Board";
	private static final Boolean debug = false;
	private final MainActivity game;
	private final Context mContext;
	public Boolean isPopulated = false;
	
	
	// Component Managers
	GridManager 		mGrid;
	GroupManager 		mGroups;
	ShapeManager 		mBlocks;
	GarbageGenerator	mGarbageGenerator;
	
	// State
	public Boolean isDeleted = false;
	public long triggerDelete = -1; 
	
	// ...Tangibility / State
	public Boolean 	isInteractable = true;
	public Boolean	isVisible = true;
	public Boolean	isSettlable = true;
	public Boolean	isFlippable = true;
	public ConcurrentSkipListSet<Integer>	mSettleEventTriggers = new ConcurrentSkipListSet<Integer>();	// appended when a new event occurs that calls for a settle (not verbose), report the col to settle
	public TreeMap< Long, HashSet<String> > mBlockDeathTriggers = new TreeMap< Long, HashSet<String> >();	// settle deletes blocks from grid before settling
	private HashSet<String> mTopRowBlocks = new HashSet<String>();
	
	public Boolean	mBoardMatchTrigger = false;
	public Object	mBoardModifierMutex = new Object();
	// Valid Settle Event Triggers: Drop active block ...
	
	// keep track of *this* boards orientation
	private Orientation mOrient = Orientation.NORMAL;
	private Orientation mNextOrient = Orientation.NORMAL;
	
	// Active block from BlockManager... DropRow
	public ConcurrentHashMap<Integer, Integer>	mActiveDropRow;
	
	// keep track of how many rows have been added
	public int mCurrentRowIndex = 0;
	
	// Row On Deck: an ordered arraylist of the ID's of each block to be added to the bottom of the board
	ArrayList<GridElement> mRowOnDeck;
	
	private Boolean mRelocateRODTrigger = false;
	
	private Boolean mGenerateBonusBlock = false;
	
	// random pool seeded by board Id
	private final Random randObjPool  = new Random( (long)(BaseConverterUtil.tolkenizeStringToInt(getId())) );
	public BonusBlockManager mBonusBlockManager;
	
	Board(Context ctxt)
	{
		// get the game object from context
		mContext = ctxt;
		game = (MainActivity) (mContext);
		init();
		
	}
		
	public void init()
	{
		isPopulated = false;
		isDeleted = false;
		mGrid = new GridManager(mContext);
		mGroups = new GroupManager(mContext);
		mBlocks = new ShapeManager(mContext);
		mGarbageGenerator = new GarbageGenerator();
		mBonusBlockManager = new BonusBlockManager(mContext);
	}
	
	/*
	private void DEBUG(String logString)
	{
		if (debug == true) Log.i(TAG, logString);
	}

	private void ERROR(String logString)
	{
		if (debug == true) Log.e(TAG, logString);
	}
	*/
	
	public synchronized Boolean spawnBlock( Block block, int row, int col, Animation anim, int duration )
	{
		if (mGrid.setBlockId(row, col, block.getId()))
		{
			block.mAnimation.queueAnimation(anim, null, new Coord<Integer>(row, col), duration, null, mOrient );
			
			if ( mBlocks.put(block) ) 
			{
				return true;
			}
			else
				mGrid.remove(block);
		}
		
		return false;
	}
	
	public synchronized Boolean spawnGroup( ArrayList<Block> blocks, int row, ArrayList<Integer> cols, Animation anim, int duration )
	{
		return spawnGroup(blocks, row, cols, anim, duration, false);
	}
	
	public synchronized Boolean spawnGroup( ArrayList<Block> blocks, int row, ArrayList<Integer> cols, Animation anim, int duration, Boolean forceNormal )
	{
		// check basic input validity
		if (blocks.size() != cols.size())
			return false;
		
		// check that there are no blocks where the group is going
		for (Integer col : cols)
		{
			if (!mGrid.isEmpty(row, col))
				return false;
		}
	
		
		// generate a group id
		String groupId = BaseConverterUtil.Base62Random();
		
		// add the blocks
		for (Integer col : cols)
		{
			Block block = blocks.remove(0);
			
			// set blocks group id
			block.setGroupId(groupId);
			// add to playable grid
			mGrid.setBlockId(row, col, block.getId());
			// add to group
			mGroups.add(groupId, block.getId(), col);
			
			
			// queue animation (spawn)
			if (anim != null)
			{
				//block.mAnimation.setScale(0.7f, 0.7f, 0.7f);
				//block.mAnimation.setScale(0.1f, 0.1f, 0.1f);
				block.mAnimation.setScale(0, 0, 0);
				block.mAnimation.queueAnimation(anim, null, new Coord<Integer>(row, col), duration, null, mOrient );
			}
			
			// make block drawable
			mBlocks.put(block);

				
		}
		
		
		// generate a wrapper block (blockId = groupId)
		
		if (anim != null)
		{
			// select garbage type at random (either normal or barrier)
			if (forceNormal)
				mGroups.setGroupType(groupId, GarbageType.NORMAL);
			else
				mGroups.pickGroupType(groupId);
			
			// make wrapper
			Block wrapper = new Block(mContext, World.BLOCKTYPES.GROUPWRAPPER, mGroups.getColor(groupId), row, mGroups.getMiddleCol(groupId) , true, false);
			wrapper.setId(groupId);
			
			// scale wrapper around garbage
			//wrapper.mAnimation.setScale(4f, 1f, 1f);		
			//wrapper.mAnimation.setScale(3.5f, 1f, 1f);		
			
			// queue animation (spawn)
			wrapper.mAnimation.queueAnimation(anim, null, new Coord<Integer>(row, mGroups.getMiddleCol(groupId) ) , duration, null, mOrient );
			
			// set wrapper to drawable elements
			mBlocks.put( wrapper );
		}
		
		return true;
		
	}
	
	public Boolean stripGroup(String groupId)
	{
		try
		{
		
			ArrayList<String> members = mGroups.getGroupMembers(groupId);
			ArrayList<Integer> cols	= mGroups.getGroupMemberCols(groupId);
			mGroups.removeGroup(groupId);
			GLShape blockMember;
			
			// make block interactable & settlable
			for (String blockMemberId : members)
			{
				blockMember = mBlocks.get(blockMemberId);
				blockMember.isInteractable = true;
				blockMember.isFrozen = false;
				blockMember.setGroupId(null);
			}
			
			// trigger settle cols
			synchronized(mSettleEventTriggers)
			{
				for (Integer col : cols)
				{
					mSettleEventTriggers.add( col );
				}
			}
			
			return true;
			
		}
		catch (Exception e)
		{
			//appendErr("stripGroup: " + e.toString() );
		}
		
		return false;
	}
	
	
	public GLShape getShape( int row, int col )
	{
		try
		{
			return mBlocks.get(mGrid.getBlockId(row, col));
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	public GLShape getShape( Coord<Integer> rowcol )
	{
		try
		{
			return mBlocks.get(mGrid.getBlockId(rowcol.getRow(), rowcol.getCol()));
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	
	
	public Block getBlock( int row, int col )
	{
		return (Block) getShape(row, col);
	}
	
	public Block getBlock( Coord<Integer> rowcol )
	{
		return (Block) getShape( rowcol );
	}
	

	
	public synchronized void resetBoard()
	{
		
	}
	
	public synchronized void populateBoard()
	{

		
		int   minFillLevel = 1;
		float maxFillRatio = 0.6f;		//Only fill up to xx% of rows in a col (at most)!
		int   maxFillLevel = (int) (MainActivity.ROWCOUNT * maxFillRatio);


		for (int col=0; col < MainActivity.COLCOUNT; col++)
		{
			//TMP!
			//int curFillLevel = CA_Game.ROWCOUNT;
			int curFillLevel = minFillLevel + (int)(Math.random() * ((maxFillLevel - minFillLevel) + 1));
			//int curFillLevel = Math.max( col , 0 );
			
			Color lastcolorPick = Color.NONE;
			
			for (int row=0; row < curFillLevel; row++)
			{
				Boolean goodcolorPick = false;
				Color colorPick = Color.NONE;
				
				// colorPick a color! avoid two consecutive blocks with same color (row & col)
				do
				{
					colorPick = Color.pickColorExcept(lastcolorPick);
					
					if (col!=0)
					{
						Color beneath = Color.NONE;
						Block leftBlock = getBlock(row, col-1);
						
						if (leftBlock != null)
							beneath = leftBlock.mColor.mCurrentColor;
							
						if (beneath != colorPick)
							goodcolorPick = true;
						
					}
					else
					{
						goodcolorPick = true;
					}
					
				}
				while (!goodcolorPick);
				
				// Create the new block!
				spawnBlock( 
						new Block(mContext, colorPick, row, col),  // not visible, is interactable 
						row, col, Animation.GROWFLIPSPAWN, 600
						);

				// remember last colorPick; don't colorPick again this color consecutively!
				lastcolorPick = colorPick;
				
			} // end for rows...
			
		} // end for cols...
	
		// the row that will rise from below the screen
		generateRowOnDeck();
		
		// set the board state as populated
		isPopulated = true;
		
		// TEMP TEMP TEMP
		//generateGarbage( 2 );
		
		//game.text = "Populated!";
		//e.textviewHandler.post( game.updateTextView );
	}
	
	public Boolean isPopualted()
	{
		return isPopulated;
	}
	
	/**
	 * Pick valid new blocks to add to the bottom of the board (later). Hold off on adding them to the grid manager,
	 * Only add them to the block manager. So these blocks are visible (partially) but have no impact on the
	 * game and are not interactable.
	 * 
	 * @return	success?
	 */
	
	public synchronized Boolean generateRowOnDeck()
	{
		//appendLog("Generate ROD...");
		
		mRowOnDeck = new ArrayList<GridElement>();
		
		int bonusCol = randObjPool.nextInt(MainActivity.COLCOUNT);
		
		for (int col=0; col < MainActivity.COLCOUNT; col++)
		{
			Color except = Color.NONE;
			Block aboveBlock = getBlock(0, col);
			
			if (aboveBlock != null)
				except = aboveBlock.mColor.mCurrentColor;
			
			Block newBlock = null;
			
				// TEMP TEMP TEMP
				/*
				if (1 == col || 3 == col || 4 == col)
				{
					
					if (mOrient == Orientation.NORMAL)
						newBlock = new Block(mContext, Color.RED, -1, col, true, false); // visible, not interactable
					else
						newBlock = new Block(mContext, Color.RED, StackAttack.ROWCOUNT, col, true, false); // visible, not interactable
				}
				else if (2 == col || 5 == col)
				{
					
					if (mOrient == Orientation.NORMAL)
						newBlock = new Block(mContext, Color.BLUE, -1, col, true, false); // visible, not interactable
					else
						newBlock = new Block(mContext, Color.BLUE, StackAttack.ROWCOUNT, col, true, false); // visible, not interactable
				}
				*/
			//else
			//{
				if (mOrient == Orientation.NORMAL)
					newBlock = new Block(mContext, Color.pickColorExcept(except), -1, col, true, false); // visible, not interactable
				else
					newBlock = new Block(mContext, Color.pickColorExcept(except), MainActivity.ROWCOUNT, col, true, false); // visible, not interactable
			//}
				
			
			newBlock.isSeeThru = true;
			
			// spawn!
			
			if (mOrient == Orientation.NORMAL)
				newBlock.mAnimation.queueAnimation(Animation.ROWONDECKSPAWN, new Coord<Integer>(-2, col), new Coord<Integer>(-1, col), null, null, mOrient );
			else
				newBlock.mAnimation.queueAnimation(Animation.ROWONDECKSPAWN, new Coord<Integer>(MainActivity.ROWCOUNT+1, col), new Coord<Integer>(MainActivity.ROWCOUNT, col), null, null, mOrient );
			
			// TEMP TEMP TEMP
			//if (1 == col)
			if (bonusCol == col)
			{
				// should a bonus block be created?
				if (mGenerateBonusBlock == true)
				{
					
					// Tag Bonus Block
					newBlock.mBlockValue = BlockValue.getRandomBonusValue();
					//newBlock.mBlockValue = BlockValue.SHUFFLE;
					
					// reset flag
					mGenerateBonusBlock = false;
				}
			}
			
			mBlocks.put(newBlock);
			
			if (mOrient == Orientation.NORMAL)
				mRowOnDeck.add( new GridElement( mContext, -1, col,  newBlock.getId()) );
			else
				mRowOnDeck.add( new GridElement( mContext, MainActivity.ROWCOUNT , col,  newBlock.getId()) );
			
			
		}
		

		
		return true;
		
	}
	
	public synchronized void deleteRowOnDeck()
	{
		if (mRowOnDeck != null)
		{
			for (GridElement ge : mRowOnDeck)
			{
				try
				{
					destroyBlock( ge.getBlockId() );
				}
				catch (Exception e) {}
			}
			
			mRowOnDeck = new ArrayList<GridElement>();
		}
	}
	
	/**
	 * When the board flips, the row on deck needs to move to the opposite side of the board.
	 * This is the equivalent of settleBoard() for the row on deck blocks.
	 */
	
	public void relocateRowOnDeck()
	{
		//game.text = "Trigger: " + mOrient + "\n";
        //game.textviewHandler.post( game.updateTextView );
		
		// Trigger Animation
		for (GridElement ge : mRowOnDeck)
		{
			Block block = (Block) mBlocks.get(ge.getBlockId());
			if (block != null)
			{
				// only the col coord matters!
				block.mAnimation.queueAnimation(Animation.ROWONDECKRELOCATE,  new Coord<Integer>(0, ge.getCol()), null, null, null, mOrient );
			}
		}
		
		// reset trigger
		mRelocateRODTrigger = false;
		
	}
	
	
	private GLShape block_isTopRowEmpty;
	public Boolean isTopRowEmpty()
	{
		
		if (mOrient == Orientation.NORMAL)
		{
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				if ( !mGrid.isEmpty(MainActivity.ROWCOUNT -1, col) )
				{
					block_isTopRowEmpty = mBlocks.get(mGrid.get(MainActivity.ROWCOUNT -1, col));
					if (block_isTopRowEmpty != null)
					{
						// dont include matching blocks! they are dying anyway
						if (block_isTopRowEmpty.isMatching)
						{
							continue;
						}
						else
						{
							return false;
						}
					}
					else
					{
						return false;
					}
				}
			}
		}
		else
		{
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				if ( !mGrid.isEmpty(0, col) )
				{
					block_isTopRowEmpty = mBlocks.get(mGrid.get(0, col));
					if (block_isTopRowEmpty != null)
					{
						// dont include matching blocks! they are dying anyway
						if (block_isTopRowEmpty.isMatching)
						{
							continue;
						}
						else
						{
							return false;
						}
					}
					else
					{
						return false;
					}
				}
			}
		}
		
		
		return true;
		
	}
	
	
	private GLShape block_getBoardPercentFull;

	public int getHighestRow()
	{
		
		if (mOrient == Orientation.NORMAL)
		{
			for (int row=MainActivity.ROWCOUNT-1; row >= 0; row--)
			{
			
				for (int col=0; col < MainActivity.COLCOUNT; col++)
				{
					if ( !mGrid.isEmpty(row, col) )
					{
						block_getBoardPercentFull = mBlocks.get(mGrid.get(row, col));
						if (block_getBoardPercentFull != null)
						{
							// dont include matching blocks! they are dying anyway
							if (block_getBoardPercentFull.isMatching)
							{
								continue;
							}
							else
							{
								return row;
							}
						}
						else
						{
							return row;
						}
					}
				}
			}
		}
		else
		{
			for (int row=0; row < MainActivity.ROWCOUNT; row++)
			{
			
				for (int col=0; col < MainActivity.COLCOUNT; col++)
				{
					if ( !mGrid.isEmpty(row, col) )
					{
						block_getBoardPercentFull = mBlocks.get(mGrid.get(row, col));
						if (block_getBoardPercentFull != null)
						{
							// dont include matching blocks! they are dying anyway
							if (block_getBoardPercentFull.isMatching)
							{
								continue;
							}
							else
							{
								return row;
							}
						}
						else
						{
							return row;
						}
					}
				}
			}
		}
		
		return 0;		
		
	}

	public HashSet<String> getTopRowBlocks()
	{
		HashSet<String> topRow = new HashSet<String>();
		
		
		if (mOrient == Orientation.NORMAL)
		{
			for (int col=0; col < MainActivity.COLCOUNT; col++)
				if ( !mGrid.isEmpty(MainActivity.ROWCOUNT -1, col) )
					topRow.add(mGrid.getBlockId(MainActivity.ROWCOUNT-1, col));
		}
		else
		{
			for (int col=0; col < MainActivity.COLCOUNT; col++)
				if ( !mGrid.isEmpty(0, col) )
					topRow.add(mGrid.getBlockId(0, col));
		}
		
		/*
		if (mOrient == Orientation.NORMAL)
		{
			for (int col=0; col < CA_Game.COLCOUNT; col++)
			{
				if ( !mGrid.isEmpty(CA_Game.ROWCOUNT -1, col) )
				{
					block_isTopRowEmpty = mBlocks.get(mGrid.get(CA_Game.ROWCOUNT -1, col));
					if (block_isTopRowEmpty != null)
					{
						// dont include matching blocks! they are dying anyway
						if (block_isTopRowEmpty.isMatching)
						{
							continue;
						}
						else
						{
							topRow.add(mGrid.getBlockId(CA_Game.ROWCOUNT-1, col));
						}
					}
					else
					{
						topRow.add(mGrid.getBlockId(CA_Game.ROWCOUNT-1, col));
					}
				}
			}
		}
		else
		{
			for (int col=0; col < CA_Game.COLCOUNT; col++)
			{
				if ( !mGrid.isEmpty(0, col) )
				{
					block_isTopRowEmpty = mBlocks.get(mGrid.get(0, col));
					if (block_isTopRowEmpty != null)
					{
						// dont include matching blocks! they are dying anyway
						if (block_isTopRowEmpty.isMatching)
						{
							continue;
						}
						else
						{
							topRow.add(mGrid.getBlockId(0, col));
						}
					}
					else
					{
						topRow.add(mGrid.getBlockId(0, col));
					}
				}
			}
		}
		*/
		return topRow;
		
	}
	
	
	/**
	 * If there are blocks in the top row, then no.
	 * if there is an active block picked up. then no.
	 * 
	 * @return
	 */
	GLShape block_readyToIncrementBoard;
	public Boolean readyToIncrementBoard()
	{	
		//game.text = "\n\n\n\n\n\n\nreadyToIncrementBoard  ("  + System.currentTimeMillis() + ")\n";
		//game.textviewHandler.post( game.updateTextView );
		
		if (game.getIsGameOver())
			return false;
		
		if (mBlocks.mActiveBlock != null)
			return false;
		
		if (mGroups.isAnyGroupMelting())
			return false;
		
		if (mBonusBlockManager.isProcessingRowIncrementRestrictedBonusBlock())
			return false;
		
		HashSet<String> currentTopRow = getTopRowBlocks(), difference = new HashSet<String>();
		
		//game.text += "Top Row       : " + mTopRowBlocks + "\n";
		
		// generate difference, a set of new top row blocks
        for (String id:currentTopRow)
        {
        	block_readyToIncrementBoard = mBlocks.get(id);
        	if (block_readyToIncrementBoard != null)
        	{
        		// dont include matching blocks
	        	if (block_readyToIncrementBoard.isMatching)
	        	{
	        		continue;
	        	}
        	}
        	
        	if (!mTopRowBlocks.contains(id)) 
        	{
        		difference.add(id);
        	}
        }
        
        //game.text += "Diff          : " + difference + "\n";
        		
        // start wobbling new blocks
        
        for (String id:difference)
        {
        	block_readyToIncrementBoard = mBlocks.get(id);
        	if (block_readyToIncrementBoard != null)
        	{
	        	if (!block_readyToIncrementBoard.isFrozen && block_readyToIncrementBoard.isInGroup() == false)
	        	{
	        		block_readyToIncrementBoard.mAnimation.queueAnimation(Animation.WOBBLE, null, null, null, null, null);
	        	}
        	}
        }
        		
        // add new blocks to known set
		mTopRowBlocks.addAll(difference);
		
		//game.text += "Top Row + Diff: " + mTopRowBlocks + "\n";
        //game.textviewHandler.post( game.updateTextView );
        		
		if (mTopRowBlocks.size() == 0)
			return true;
		
		return false;
		
		
	}
	
	
	/**
	 * As the board moves up a row is added to the bottom and another is removed from the top.
	 * This method is responsible for managing such transactions by interfacing with the
	 * grid and block manager.
	 * 
	 * @return	success?
	 */
	public synchronized Boolean incrementBoard(int rowIndex)
	{
		
		// can the top be removed? If there are blocks in the top row, then no.
		// otherwise, remove!
		// this causes a bug: if the globalrowindex in boardManager has been incremented, this exiting prematurly makes the boards get drawn wrong!
		//if ( !readyToIncrementBoard() )
		//	return false;
		// instead, drop the active block (forced)
		dropActiveBlock(true, null, false);
	
		synchronized (mBoardModifierMutex)
		{
			
			//appendLog("Increment Board...");
			
			
			// dont allow settling & progression
			//pauseBoardSettle();
			game.getWorld().mBoards.pauseBoardProgression();
			
			// reset score multiplier
			game.getWorld().mBoards.mGameState.resetMultiplier();
			
			
			// remove the old empty row from top of board
			if (mOrient == Orientation.NORMAL)
				mGrid.remove( MainActivity.ROWCOUNT -1 );
			else
				mGrid.remove( 0 );
			
			// change Blocks to be interactable & not seeThru
			if (mRowOnDeck != null && mBlocks != null)
			{
				for (GridElement ge : mRowOnDeck)
				{
					Block block = (Block) mBlocks.get(ge.getBlockId());
					if (block != null)
					{
						block.isInteractable = true;
						block.isSeeThru = false;
					}
				}
			}
			
			// increment row index for reference
			mCurrentRowIndex = rowIndex;
			
			
			// add row on deck to the grid; blocks are already in the block manager (since they are on the screen)
			if (mOrient == Orientation.NORMAL)
				mGrid.add(0, mRowOnDeck);
			else
				mGrid.add(MainActivity.ROWCOUNT-1, mRowOnDeck);
			
			// generate the next row on deck
			generateRowOnDeck();
			//if (!generateRowOnDeck())
			//	appendErr("ROD ERROR!");
			
			
			// allow settling & resume progression
			//resumeBoardSettle(true);
			game.getWorld().mBoards.resumeBoardProgression();
			
			// a match from the row on deck may have occurred
			mBoardMatchTrigger = true;
			
			// TMP TMP TMP
			//generateGarbage( 3 );
			
		}
		
		return true;
	}
	
	public synchronized void generateGarbage(int count)
	{
		generateGarbage(count, false);
	}
	
	public synchronized void generateGarbage(int count, Boolean forceNormal)
	{
		if (count == 0)
			return;
		
		//if (game.getWorld().mBoards.mPauseBoardProgression)
		//	return;
		
		int generated = 0;
		for (int idx=0; idx < count; idx++)
		{
			// generate garbage, if not able to, then keep the points
			if (!generateGarbage(forceNormal))
				mGarbageGenerator.incrementGenerationPoints(3);
			else
				generated++;
		}
		
		if (generated > 0)
			game.getWorld().mBoards.startGarbageDropSpringAnimation(generated, 350);
	
		/*
		if (count > 0)
		{
			game.text  = generated + " of " + count + "\n";
			game.text += "Next: " + (mGenerator.mNextCheckPoint-System.currentTimeMillis()) + "\n";
			game.textviewHandler.post( game.updateTextView );
		}
		*/
		
		
	}
	
	SparseIntArray lowestColPotential;
	
	public synchronized Boolean generateGarbage()
	{
		return generateGarbage(false);
	}
	
	public synchronized Boolean generateGarbage(Boolean forceNormal)
	{
		////appendLog("Generating Garbage...");
		
		
		int size = 3; // ALWAYS ODD NUMBER
		
		
		synchronized (mBoardModifierMutex)
		{
			// find the lowest locations to drop the garbage	
			
			// col : lowest empty row
			lowestColPotential = new SparseIntArray();
			int row = 0;
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				// top to bottom
				for (int idx=MainActivity.ROWCOUNT-1; idx >= 0; idx--)
				{
					
					if (mOrient == Orientation.NORMAL)
						row = idx;
					else
						row = MainActivity.ROWCOUNT - (idx+1);
					
					if (mGrid.getBlockId(row, col) != null)
					{
						if (mOrient == Orientation.NORMAL)
						{
							if ( (row+1) < MainActivity.ROWCOUNT  )
								lowestColPotential.put(col, row+1);
						}
						else
						{
							if ( (row-1) >= 0 )
								lowestColPotential.put(col, row-1);
						}
						break;
					}
				}
			}
			
			//game.text = String.valueOf( lowestColPotential );
			//game.textviewHandler.post( game.updateTextView );
			
			
			// pick a random col, keep trying until im out of options (only pick valid cols; eg. end cols don't work)
			ArrayList<Integer> cols = new ArrayList<Integer>();
			for (int i=((size-1)/2); i < MainActivity.COLCOUNT - ((size-1)/2) -1; i++)
				cols.add(i);
			Collections.shuffle(cols);
			
			//game.text = String.valueOf( cols ) + "\n";
			//game.textviewHandler.post( game.updateTextView );
			
			// find a spot to drop the garbage
			int selectedRow = -1;
			ArrayList<Integer> selectedCols = new ArrayList<Integer>();
			for (Integer midCol : cols)
			{
				// reset
				selectedCols.clear();
				
				//game.text += "Trying MidCol: " + midCol + "\n";
				//game.textviewHandler.post( game.updateTextView );
				
				
				// only try if this is a valid col (not full)
				if ( lowestColPotential.indexOfKey(midCol) != -1 )
				{
					// keep trying this col (move up the rows)
					row = 0;
					for (int idx=0; row < MainActivity.ROWCOUNT && row >= 0 && selectedRow == -1; idx++)
					{
						
						
						if (mOrient == Orientation.NORMAL)
							row = lowestColPotential.get(midCol) + idx;
						else
							row = lowestColPotential.get(midCol) - idx;
						
						if (!(row < MainActivity.ROWCOUNT && row >= 0))
							break;
						
						//game.text += "ROW: " + row + "\n";
						//game.textviewHandler.post( game.updateTextView );
						
						
						for (int col=midCol-((size-1)/2); col <= midCol+((size-1)/2); col++)
						{
							if (mGrid.getBlockId(row, col) == null)
							{
								// there may be a garbage block ledge above this location
								if ( lowestColPotential.indexOfKey(col) != -1 )
								{
									if (row >= lowestColPotential.get(col))
									{
										if (!selectedCols.contains(col))
											selectedCols.add(col);
										else 
											continue; // for some reason this selection was bad... keep checking
									}
								}
							}
							else
								break;					
						}
						
						if (selectedCols.size() == size)
						{							
							// check if there is at least one col that has lowest potential of the current row 
							for (Integer col : selectedCols)
							{
								if ( lowestColPotential.get(col) == row )
									selectedRow = row;
							}
						}
					}
					
					if (selectedRow != -1)
						break;
				}
			}
			
			// was there a selection made? 
			if (selectedRow == -1 || selectedCols.size() != size)
				return false;
			
			
			//game.text += "\nSuccess! Row: " + selectedRow + " Cols: " + selectedCols + "\n";
			//game.textviewHandler.post( game.updateTextView );
			
			
			// generate
			ArrayList<Block> blocks = new ArrayList<Block>();
			
			for (int idx=0; idx < size; idx++)
			{
				blocks.add( new Block(mContext, Color.pickColor(), selectedRow, selectedCols.get(idx), true, false) );
			}
			
			// make the group
			spawnGroup(blocks, selectedRow, selectedCols, Animation.DROPSPAWN, 400, forceNormal);

			return true;
		}
	}
	
	
	
	private final int mMeltDuration = 300; //200
	private int mInflateColDelayDelim = 600; //300
	private final int mInflateDuration = 1800; //1000
	private int mInflateRowDelayDelim = mInflateDuration; 
	private final int mUnitMeltInflateTime = mInflateDuration;
	
	public synchronized Boolean meltGarbage(String groupId, int rowDelayIdx)
	{

		try
		{
			if (game.getWorld().mTutorialState.isInTutorial  || game.getWorld().mBoards.mGameState.mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
			{
				//destroyGroup(groupId);
				quickMeltGroup(groupId, rowDelayIdx);
				return true;
			}
			
			ArrayList<String> members = mGroups.getGroupMembers(groupId);
			//mGroups.removeGroup(groupId);
			
			// check if the group is being melted already
			if ( mBlocks.get(members.get(0)).isFrozen )
				return false;
			
			
			try
			{
				// melt wrapper...
				mBlocks.get(groupId).mAnimation.queueAnimation(Animation.MELTWRAPPER, null, null, mMeltDuration,  mInflateRowDelayDelim*rowDelayIdx, null);
			}
			catch (Exception e) {}
			
			
			// get block ordering...
			HashMap<String, Float> lang = new HashMap<String, Float>();
			for (String member : members)
				lang.put(member, mBlocks.get(member).mAnimation.mCurrentPoints[0][0]);
			
			ArrayList<String> keys = new ArrayList<String>(lang.keySet());
			
			//Sort keys by values.
			final HashMap<String, Float> langForComp = lang;
			Collections.sort(keys, 
				new Comparator<String>(){
					public int compare(String left, String right){
						return langForComp.get(left).compareTo(langForComp.get(right));
					}
				});
			
			
			// inflate members...
			GLShape block;
			Color colorPick = Color.NONE;
			int idx = 0;
			for(Iterator<String> i=keys.iterator(); i.hasNext();)
			{
				block = mBlocks.get(i.next());
				
				// pick color
				colorPick = Color.pickColorExcept(colorPick);
		
				// start melt animation
				block.mAnimation.queueAnimation(Animation.MELTMEMBERS, null, null, mInflateDuration, mInflateColDelayDelim*idx + mInflateRowDelayDelim*rowDelayIdx, null);
				
				// fade from grey to new color pick
				block.mColor.enqueue( 
					new ColorState( 
							new MotionEquation[] {MotionEquation.LOGISTIC, MotionEquation.LOGISTIC, MotionEquation.LOGISTIC, MotionEquation.LINEAR}, 
							Color.BLACK, 
							block.mColor.mCurrentColor,
							new int[] {mInflateDuration, mInflateDuration, mInflateDuration, mInflateDuration}, 
							new int[] {
								mInflateColDelayDelim*idx + mInflateRowDelayDelim*rowDelayIdx,
								mInflateColDelayDelim*idx + mInflateRowDelayDelim*rowDelayIdx,
								mInflateColDelayDelim*idx + mInflateRowDelayDelim*rowDelayIdx,
								mInflateColDelayDelim*idx + mInflateRowDelayDelim*rowDelayIdx
								}
							));	
				
				idx++;
			}
			
		
			// always return true (for now)
			return true;
		}
		catch (Exception e) {}
		
		return false;
	}
	
	
	public synchronized void lockBoard()
	{
		synchronized (mBoardModifierMutex)
		{
			dropActiveBlock(true, null, false);	// drop any active blocks; true=forced
	
			isFlippable = false; 
			isSettlable = false;
			isInteractable = false;
			
		}
		
	}
	
	public synchronized void unlockBoard()
	{
		synchronized (mBoardModifierMutex)
		{
			isFlippable = true; 
			isSettlable = true;
			isInteractable = true;
		}
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// BLOCK DELEGATORS
	

	
	public synchronized Boolean pickupBlock( Coord<Integer> rowcol )
	{
		
		Block block = getBlock(rowcol);
		if (block != null && isInteractable)
		{
			
			//game.text = "Pickup " + block.mColor.mCurrentColor  + "\n" + game.text;
			//game.textviewHandler.post( game.updateTextView );
			
			if (!block.isInteractable)
				return false;
			
			// pickup!...
			
			
			synchronized (mBoardModifierMutex)
			{
			
				// animate
				if (block.mAnimation.queueAnimation(Animation.PICKUP, null, null, null, null, null))
				{
					
					// if there is still an active block, drop it
					if (mBlocks.hasActiveBlock())
						dropActiveBlock(rowcol, false);
					
					// dont allow settling while a block is picked up
					pauseBoardSettle();
					
					// set active block
					mBlocks.setActive(block.getId(), rowcol);
					
					// populate drop row
					populateActiveDropRow(block.getId());
									
					//game.text = "Pickup " + block.mColor.mCurrentColor + "  r: " + rowcol.getRow() + ", c: " + rowcol.getCol() +"\n" + game.text;
					//game.textviewHandler.post( game.updateTextView );
					
					return true;
				}
				
			}
			
		}
		/*
		else if (block == null && isInteractable)
		{
			newBlock( 
					new Block(mContext, Color.pickColor(), rowcol.getRow(), rowcol.getCol(), true, true), 
					rowcol.getRow(), rowcol.getCol(), false
					);
		}*/
		
		return false;
	}
	/*
	public Boolean dragActiveBlock( int pixX, int pixY )
	{

		Block block = mBlocks.mActiveBlock;
		if (block != null && isInteractable)
		{
			Coord<Integer> RC = game.getWorld().convertPixToRC(pixX, pixY);
		
			// drag manually
			//block.mAnimation.setPositionByPix( new int[] {pixX, game.getWorld().mScreenHeight-pixY});
			
			// don't drag manually
			block.mAnimation.setPositionByPix( 
					new Coord<Float> (
							(float) pixX, 
							(float) ((mActiveDropRow.get(RC.getCol()) * game.getWorld().mScreenBlockLength ) + 0.5*game.getWorld().mScreenBlockLength) 
							)
					);
			
			return true;
		}
		
		return false;
	}
	*/
	
	public synchronized Boolean dropActiveBlock(Coord<Integer> fingerRowCol, Boolean forceVibrate)
	{
		return dropActiveBlock(false, fingerRowCol, forceVibrate);
	}
	
	public synchronized Boolean dropActiveBlock(Boolean forceDrop, Coord<Integer> fingerRowCol, Boolean forceVibrate)
	{

		Block block = mBlocks.mActiveBlock;
		if (block != null && (isInteractable || forceDrop))
		{
			
			if (!block.isInteractable && !forceDrop)
				return false;
			
			// animate
			synchronized (mBoardModifierMutex)
			{
				if (block.mAnimation.queueAnimation(Animation.DROP, null, null, null, null, null))
				{
					if (forceVibrate)
					{
						// flash 2x...
						game.getWorld().mBoards.mBackgroundBlock.flash(Color.RED, (int) game.getErrorVibratePattern()[1]*2);
						
						// vibrate 3x...
						try
						{
							game.getVibratorHandler().post( game.getErrorVibratorSequence() );	
						}
						catch (Exception e)
						{
							//Log.e("Vibrator", "Could not vibrate! " + e.toString());
						}
						
						//game.text = "Drop: " + fingerRowCol;
						//game.textviewHandler.post( game.updateTextView );
						
					}
				    else if (!forceDrop && fingerRowCol != null)
					{
						// purly vertical movement is not possible! warn the user
						if (fingerRowCol.getRow() != mBlocks.mActiveBlockStartCoord.getRow() && fingerRowCol.getCol() == mBlocks.mActiveBlockStartCoord.getCol())
						{
							// flash 2x...
							game.getWorld().mBoards.mBackgroundBlock.flash(Color.RED, (int) game.getErrorVibratePattern()[1]*2);
							
							// vibrate 3x...
							try
							{
								game.getVibratorHandler().post( game.getErrorVibratorSequence() );	
							}
							catch (Exception e)
							{
								//Log.e("Vibrator", "Could not vibrate! " + e.toString());
							}
							
							//game.text = "Drop: " + fingerRowCol;
							//game.textviewHandler.post( game.updateTextView );
							
							// Tutorial
							try
							{
								if (game.getWorld().mTutorialState.isInTutorial)
								{
									game.getWorld().mTutorialState.moveHint(mBlocks.mActiveBlockStartCoord.getRow(), mBlocks.mActiveBlockStartCoord.getCol(), 500 , 2000 );
								}
							}
							catch(Exception e)
							{
								//Log.e("MoveHint", "Failed!  " + e.toString());
							}
							
						}
					}
					
					// deactivate
					mBlocks.setInactive(block.getId());
					
					// set trigger for match check
					mBoardMatchTrigger = true;
					
					// reset 
					mActiveDropRow = null;
					
					// allow board settling again (if there is no existing)
					resumeBoardSettle();
	
					
					//game.text = "Drop " + block.mColor.mCurrentColor  + "\n" + game.text;
					//game.textviewHandler.post( game.updateTextView );
					
					return true;
				}
			}
			/*
			else
			{
				game.text +="\nBADQANIMATION: " + String.valueOf(block.mAnimation.mAvailableIndexes.size());
			}
			*/
			
		}
		
		return false;
	}
	
	
	
	
	private synchronized void cancelWobble(String blockId)
	{
		mBlocks.get(blockId).mAnimation.cancelAnimation(Animation.WOBBLE);
		// DONT DO THIS!
		//mBlocks.get(blockId).mAnimation.queueAnimation(Animation.RESTORESTEADYSTATE,null,null,null,null,null);
	}
	
	
	public synchronized Boolean swapBlocks(Coord<Integer> rowcol1, Coord<Integer> rowcol2)
	{
		return swapBlocks( rowcol1,  rowcol2, false, null);
	}
	
	
	Boolean animationResult1_swap, animationResult2_swap;
	Block block1_swap, block2_swap;
	Boolean block1_isMatching = false, block2_isMatching = false;
	public synchronized Boolean swapBlocks(Coord<Integer> rowcol1, Coord<Integer> rowcol2, Boolean force, Integer duration)
	{
		
		if (isInteractable)
		{	
			// only swap adjacent cols
			if (force == false)
				if (Math.abs(rowcol1.getCol()-rowcol2.getCol()) > 1)
					return false;
			
			synchronized (mBoardModifierMutex)
			{
				
				block1_swap = getBlock(rowcol1); 
				block2_swap = getBlock(rowcol2);
				
				
				if (block1_swap != null)
				{
					if (block1_swap.isFrozen)
						return false;
					if (!block1_swap.isInteractable && !block1_swap.isMatching)
						return false;
					if (block1_swap.isMatching)
						block1_isMatching = true;
				}
				
				if (block2_swap != null)
				{
					if (block2_swap.isFrozen)
						return false;
					if (!block2_swap.isInteractable && !block2_swap.isMatching)
						return false;
					if (block2_swap.isMatching)
						block2_isMatching = true;
				}
				
				if (block2_swap != null && block1_swap != null)
				{
					if (block1_swap.isMatching && block2_swap.isMatching)
						return false;
				}
				
				//appendLog("Swap Block: " + rowcol1 + " ---> " + rowcol2);
			
				if (mGrid.swapBlocks(rowcol1, rowcol2))
				{
					block1_swap = getBlock(rowcol2); 
					block2_swap = getBlock(rowcol1);
					animationResult1_swap = false;
					animationResult2_swap = false;
				
					
					// animate!
					if (block1_swap != null)
						if (!block1_swap.isMatching)
							animationResult1_swap = block1_swap.mAnimation.queueAnimation(Animation.SWAPTO, rowcol2, rowcol1, duration, null, null);
					
					
					// animate!
					if (block2_swap != null)
						if (!block2_swap.isMatching)
							animationResult2_swap = block2_swap.mAnimation.queueAnimation(Animation.SWAPTO, rowcol1, rowcol2, duration, null, null);		
					
					
					/*
					if (animationResult1)
						game.text += "SWAP Block1: " + block1.mColor.mCurrentColor + " to " + rowcol2 + " : " + animationResult1 + "\n";
					else
						game.text += "SWAP Block1: NULL to " + rowcol2 +  " : " + animationResult1 + "\n";
					if (animationResult2)
						game.text += "SWAP Block2: " + block2.mColor.mCurrentColor + " to " + rowcol1 + " : " + animationResult2 + "\n";
					else
						game.text += "SWAP Block2: NULL to " + rowcol1 + " : " + animationResult2 + "\n";
					game.textviewHandler.post( game.updateTextView );
					*/
					
					// did at least one block swap? 
					if (
							animationResult1_swap && animationResult2_swap ||
							animationResult1_swap && !animationResult2_swap && block2_swap == null ||
							!animationResult1_swap && animationResult2_swap && block1_swap == null ||
							animationResult1_swap && !animationResult2_swap && block2_isMatching   ||
							!animationResult1_swap && animationResult2_swap && block1_isMatching  
						)
					{
						//game.text = "SWAP: " + rowcol1 + " ---> " + rowcol2;
						//game.textviewHandler.post( game.updateTextView );
						
						// queue new settle event
						synchronized(mSettleEventTriggers)
						{
							mSettleEventTriggers.add( rowcol1.getCol() );
							mSettleEventTriggers.add( rowcol2.getCol() );
						}
						
						// check if swapped block was wobbling on top row, stop wobbling if not on top row
						
						if (block1_swap != null)
						{
							if (mOrient == Orientation.NORMAL)
							{
								if ( mTopRowBlocks.contains(block1_swap.getId()) && rowcol2.getRow() < MainActivity.ROWCOUNT-1 )
								{
									mTopRowBlocks.remove(block1_swap.getId());
									cancelWobble(block1_swap.getId());
								}
							}
							else
							{
								if ( mTopRowBlocks.contains(block1_swap.getId()) && rowcol2.getRow() > 0 )
								{
									mTopRowBlocks.remove(block1_swap.getId());
									cancelWobble(block1_swap.getId());
								}
							}
						}
						if (block2_swap != null)
						{
							if (mOrient == Orientation.NORMAL)
							{
								if ( mTopRowBlocks.contains(block2_swap.getId()) && rowcol1.getRow() < MainActivity.ROWCOUNT-1 )
								{
									mTopRowBlocks.remove(block2_swap.getId());
									cancelWobble(block2_swap.getId());
								}
							}
							else
							{
								if ( mTopRowBlocks.contains(block2_swap.getId()) && rowcol1.getRow() > 0 )
								{
									mTopRowBlocks.remove(block2_swap.getId());
									cancelWobble(block2_swap.getId());
								}
							}
							
						}
						
						return true;
					}
					
					// didn't work, swap back...
					else
					{
						mGrid.swapBlocks(rowcol1, rowcol2);
						
						// animate!
						if (block1_swap != null && animationResult1_swap)
						{
							block1_swap.mAnimation.queueAnimation(Animation.SWAPTO, rowcol1, rowcol2, duration, null, null);
						}
						
						// animate!
						if (block2_swap != null && animationResult2_swap)
						{
							block2_swap.mAnimation.queueAnimation(Animation.SWAPTO, rowcol2, rowcol1, duration, null, null);
						}
						
					}
				}
		
			}// sync
				
		}
		
		
		return false;
	}
	
	Block block_settle;
	Boolean animationResult_settle;
	public synchronized Boolean settleBlock(Coord<Integer> fromRowCol, Coord<Integer> toRowCol)
	{
		// DONT check for isSettelable here... it may break the settleBoard() algorithm (race condition)

		// only settle in same col
		if (Math.abs(fromRowCol.getCol()-toRowCol.getCol()) != 0)
		{
			//appendErr( "BAD COL..." );
			//game.textviewHandler.post( game.updateTextView );
			return false;
		}
		
		// dont settle if there is something there!
		if (!mGrid.isEmpty(toRowCol.getRow(), toRowCol.getCol()))
		{
			//appendErr( "OCCUPIED..." + fromRowCol + " ---> " + toRowCol );
			//game.textviewHandler.post( game.updateTextView );
			return false;
		}
		
		block_settle = getBlock(fromRowCol);
		
		if (block_settle != null)
		{
			if (block_settle.isFrozen)
			{
				/*
				appendErr( "FROZEN..." + fromRowCol );
				game.textviewHandler.post( game.updateTextView );
				*/
				return false;
			}
		}
		else
		{
			//appendErr( "NO BLOCK..." );
			//game.textviewHandler.post( game.updateTextView );
			return false;
		}

		
		
		if (mGrid.swapBlocks(fromRowCol, toRowCol))
		{
			// the initial swap was successful, get the drawable object from the new position
			
			animationResult_settle = false;
				
			// animate!
			if (block_settle != null)
				animationResult_settle = block_settle.mAnimation.queueAnimation(Animation.SETTLETO, fromRowCol, toRowCol, null, null, mOrient);					
			
			// drop wrapper
			if (block_settle != null)
			{
				if (block_settle.getGroupId() != null)
				{
					
					if (fromRowCol.getCol() == mGroups.getMiddleCol(block_settle.getGroupId()))
					{
						// this is the middle col, drop the wrapper
						try
						{
							mBlocks.get(block_settle.getGroupId()).mAnimation.queueAnimation(Animation.SETTLETO, fromRowCol, toRowCol, null, null, mOrient);		
						}
						catch (Exception e)
						{
							
						}
					}
				}
			}
			
			
			// did the animation start?
			if ( animationResult_settle )
			{
				//game.text = "Settle: " + fromRowCol + " ---> " + toRowCol;
				//game.textviewHandler.post( game.updateTextView );
				
				// check for wobbling of blocks in the top row, if the block has dropped, then stop wobbling!
				if ( mTopRowBlocks.remove(block_settle.getId()) )
				{
					cancelWobble(block_settle.getId());
				}
				
				return true;
			}
			
			// didn't work, swap back...
			else
			{
				mGrid.swapBlocks(fromRowCol, toRowCol);

			}
		}
		
		//appendErr( "CAN'T SWAP..." );
		//game.textviewHandler.post( game.updateTextView );
		return false;
	}
	
	private int mMatchAnimationTime = 200;
	private int mMatchFlashDelay = 400;
	private int mMatchNumFlashes = 2;
	private int mMatchFlashDelim = mMatchFlashDelay/(mMatchNumFlashes*2);
	private int mMatchTotalMatchAnimationTime = mMatchFlashDelay + mMatchAnimationTime*3;
	
	
	GLShape block_match;
	private synchronized BlockInfo matchBlock(Coord<Integer> location, int delay)
	{
		block_match = mBlocks.get( mGrid.getBlockId(location) );
		if (block_match != null)
		{
			if (!block_match.isMatching && block_match.isInteractable)
			{
				block_match.isMatching = true;
				block_match.isInteractable = false;
				
				// may be on top row, try to remove it from the toprowlist
				mTopRowBlocks.remove(block_match.getId());
				
				// Flash
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								block_match.mColor.mCurrentColor, 
								Color.WHITE,
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));	
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								Color.WHITE,
								block_match.mColor.mCurrentColor, 
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));	
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								block_match.mColor.mCurrentColor, 
								Color.WHITE,
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));	
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								Color.WHITE,
								block_match.mColor.mCurrentColor, 
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));
				
				// fix: if active block gets matched but not droped first (no event fires) then this drops it. Otherwise
				// 'readytoincrementboard()' will fail until the player interacts with the board again
				if ( mBlocks.mActiveBlock != null && block_match.getId() == mBlocks.mActiveBlock.getId())
				{
					dropActiveBlock(true, location, false);
				}
				
				// Move
				if (block_match.mBlockValue != null && block_match.mBlockValue != BlockValue.NORMAL)
					return new BlockInfo(block_match.getId(), location, block_match.mBlockValue);
				else
					block_match.mAnimation.queueAnimation(Animation.MATCH, null, null, mMatchAnimationTime, delay, null);
				
			}
		}
		
		return null;
	}
	
	private synchronized Boolean matchBlock(GLShape block_match, int delay)
	{
		if (block_match != null)
		{
			if (!block_match.isMatching && block_match.isInteractable)
			{
				block_match.isMatching = true;
				block_match.isInteractable = false;
				
				// may be on top row, try to remove it from the toprowlist
				mTopRowBlocks.remove(block_match.getId());
				
				// Flash
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								block_match.mColor.mCurrentColor, 
								Color.WHITE,
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));	
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								Color.WHITE,
								block_match.mColor.mCurrentColor, 
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));	
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								block_match.mColor.mCurrentColor, 
								Color.WHITE,
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));	
				block_match.mColor.enqueue( 
						new ColorState( 
								new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
								Color.WHITE,
								block_match.mColor.mCurrentColor, 
								new int[] {mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim, mMatchFlashDelim},
								new int[] {0,0,0,0}	// relative to FIFO q
								));

				// fix: if active block gets matched but not droped first (no event fires) then this drops it. Otherwise
				// 'readytoincrementboard()' will fail until the player interacts with the board again
				if (mBlocks.mActiveBlock != null && block_match.getId() == mBlocks.mActiveBlock.getId())
				{
					dropActiveBlock(true, mGrid.getBlockPosition(block_match.getId()), false);
				}
				
				// Move
				block_match.mAnimation.queueAnimation(Animation.MATCH, null, null, mMatchAnimationTime, delay, null);
				
				return true;
				
			}
		}

		return false;
		
	}
	
	
	public synchronized void destroyGroup(String groupId)
	{
		// melt any garbage

		mBlocks.get(groupId).mAnimation.queueAnimation(Animation.MELTWRAPPER, null, null, 500,  0, null);
		
		for (String memberId : mGroups.getGroupMembers(groupId))
		{
			destroyBlock( memberId );
		}
			
		
	}
	
	public synchronized void quickMeltGroup(String groupId, int groupIdx)
	{
		// melt any garbage

		mBlocks.get(groupId).mAnimation.queueAnimation(Animation.MELTWRAPPER, null, null, 500,  0, null);
		int idx=0;
		for (String memberId : mGroups.getGroupMembers(groupId))
		{
			mBlocks.get(memberId).mAnimation.queueAnimation(Animation.MELTMEMBERS, null, null, (int)( mInflateDuration*0.7 ), 300*groupIdx + 100*idx, null);
			idx++;
		}
			
		
	}
	
	public synchronized void destroyBlock(String blockId)
	{
		GLShape block = mBlocks.get(blockId );
		if (block != null)
		{
			mGrid.deleteBlock(blockId);
			block.mAnimation.queueAnimation(Animation.MATCH, null, null, mMatchAnimationTime, 0, null);
		}
	}
	
	/*
	private synchronized void destroyBlock(String blockId, int duration)
	{
		mBlocks.get( blockId ).mAnimation.queueAnimation(Animation.DESTROY,  mGrid.getBlockPosition(blockId) , null, duration, null, null);
	}
	*/
	
	private synchronized void destroyBlock(int row, int col, int duration)
	{
		destroyBlock( new Coord<Integer>(row, col), duration);
	}
	
	private synchronized void destroyBlock(Coord<Integer> location, int duration)
	{
		mBlocks.get( mGrid.getBlockId(location) ).mAnimation.queueAnimation(Animation.DESTROY, location, null, duration, null, null);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// BASIC BOARD MANIPULATORS

	
	String blockId_populateActiveDropRow = null;
	GLShape block_populateActiveDropRow;
	private Boolean populateActiveDropRow(String id)
	{
		
		try
		{
			Coord<Integer> fingerCoord = mGrid.getBlockPosition(id);
		
			mActiveDropRow = new ConcurrentHashMap<Integer, Integer>();
			mActiveDropRow.put(fingerCoord.getCol(), fingerCoord.getRow());
			
			//DEBUG(String.valueOf(fingerCoord));
			
			int potentialOffset;
			
			if (mOrient == Orientation.NORMAL)
				potentialOffset = -1;
			else
				potentialOffset = 1;
			
			//set droprow... left
			for (int col=fingerCoord.getCol()-1; col >= 0; col--)
			{
				int row = mActiveDropRow.get(col+1) + potentialOffset;  //start at highest possible potential -1
				
				// get
				blockId_populateActiveDropRow = mGrid.getBlockId(row-potentialOffset, col);
				
				// check for immovable blocks
				if (blockId_populateActiveDropRow != null)
				{
					block_populateActiveDropRow = mBlocks.get(blockId_populateActiveDropRow);
					if (block_populateActiveDropRow != null)
					{
						// commeted out: any block should be swapped with
						//if ((!block_populateActiveDropRow.isMatching && !block_populateActiveDropRow.isInteractable) || block_populateActiveDropRow.isFrozen)
						{
							//place on top of non-empty row
							mActiveDropRow.put(col,row-potentialOffset );
							
							continue;
						}
					}
				}
				
				//look for first non-empty row
				while( (row >= 0 && mOrient == Orientation.NORMAL) || (row < MainActivity.ROWCOUNT && mOrient == Orientation.REVERSE) )
				{
					// get
					blockId_populateActiveDropRow = mGrid.getBlockId(row, col);
					
					// increment
					if (mOrient == Orientation.NORMAL)
						row--;
					else
						row++; 
					
					// check
					if (blockId_populateActiveDropRow != null)
					{
						block_populateActiveDropRow = mBlocks.get(blockId_populateActiveDropRow);
						if (block_populateActiveDropRow != null)
							if (block_populateActiveDropRow.isMatching == true)
								continue;
					}
					else if ( blockId_populateActiveDropRow == null )
					{
						continue;
					}
					
					// decrement
					if (mOrient == Orientation.NORMAL)
						row++;
					else
						row--; 
					
					break;
				}
					
				
				//place on top of non-empty row
				mActiveDropRow.put(col,row - potentialOffset);
			}
			
			//set droprow... right
			for (int col=fingerCoord.getCol()+1; col < MainActivity.COLCOUNT; col++)
			{
				int row = mActiveDropRow.get(col-1) + potentialOffset;  //start at highest possible potential -1
				
				// get
				blockId_populateActiveDropRow = mGrid.getBlockId(row-potentialOffset, col);
				
				// check for immovable blocks
				if (blockId_populateActiveDropRow != null)
				{
					block_populateActiveDropRow = mBlocks.get(blockId_populateActiveDropRow);
					if (block_populateActiveDropRow != null)
					{
						// commeted out: any block should be swapped with
						//if ((!block_populateActiveDropRow.isMatching && !block_populateActiveDropRow.isInteractable) || block_populateActiveDropRow.isFrozen)
						{
							//place on top of non-empty row
							mActiveDropRow.put(col,row-potentialOffset);
							
							continue;
						}
					}
				}
				
				//look for first non-empty row
				while( (row >= 0 && mOrient == Orientation.NORMAL) || (row < MainActivity.ROWCOUNT && mOrient == Orientation.REVERSE) )
				{
					// get
					blockId_populateActiveDropRow = mGrid.getBlockId(row, col);
					
					// increment
					if (mOrient == Orientation.NORMAL)
						row--;
					else
						row++; 
					
					// check
					if (blockId_populateActiveDropRow != null)
					{
						block_populateActiveDropRow = mBlocks.get(blockId_populateActiveDropRow);
						if (block_populateActiveDropRow != null)
							if (block_populateActiveDropRow.isMatching == true)
								continue;
					}
					else if ( blockId_populateActiveDropRow == null)
					{
						continue;
					}
					
					// decrement
					if (mOrient == Orientation.NORMAL)
						row++;
					else
						row--; 
					
					break;
				}
	
				//place on top of non-empty row
				mActiveDropRow.put(col,row - potentialOffset);
			}
			
			/*
			game.text = "";
			for (int col = 0; col < CA_Game.COLCOUNT; col++)
			{
				game.text += "col: "+col+" >>> row: "+mActiveDropRow.get(col) + "\n";
			}
			game.textviewHandler.post( game.updateTextView );
			*/
			
			return true;
		}
		catch(Exception e)
		{
			//ERROR(e.toString());
		}
		
		// failed, reset
		mActiveDropRow = null;
		return false;

	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// For Board Updater Thread
	
	
	
	//##########################################################################################################
	// SETTLE
	//##########################################################################################################	
	
	private synchronized void settleEntireBoard()
	{
		//relocateRowOnDeck();
		
		for (int col = 0; col < MainActivity.COLCOUNT; col++)
			mSettleEventTriggers.add(col);
		
		// allow the board thread to do this
		//settleBoard();
	
	}
	
	
	/**
	 * Settle only columns that have triggered an event change. (via mSettleEventTriggers)
	 * 
	 	X	Assumption: groups are only rectangles
		X	Settle Rows until group reached (bottom -> up); commit by row iteration end
		X	continue settling rows, skipping cols where groups had previously (or currently) reached (push group id & row first seen onto ordered list)
		X	continue to top of the board; there should be a section of blocks settled to either side and below most groups (except for group-on-group and blocks between them)
		X	pop group id/row first seen (for lowest row): 
		X	(for) per group, starting from minimum row and going up: count settle amount per group member, commit minimum value found
		X		now continue settling anything above group (only in group member cols); start from row the group was first seen
		X		terminate iteration when another group has been found or board top hit
		X	upon complete settle, check for a match of 3 or more
	 * 
	 */
	private ConcurrentSkipListSet<Integer> localSettleCols = new ConcurrentSkipListSet<Integer>();
	private Boolean settleBlockResult;
	private SparseIntArray colDropCount;
	private synchronized void settleBoard()
	{
		
		try
		{
		
			synchronized (mBoardModifierMutex)
			{

				//game.text = "";
				
				//game.text = String.valueOf(localSettleCols) + "\n";
				
				
				// strip melted groups
				HashSet<String> deadGroups = mGroups.reapDeadGroups(System.currentTimeMillis());
				for (String groupId : deadGroups)
				{
					synchronized(mSettleEventTriggers)
					{
						mSettleEventTriggers.addAll( mGroups.getGroupMemberCols(groupId) );
					}
					stripGroup(groupId);
				}
				
				
				// delete dead blocks from grid
				while(mBlockDeathTriggers.size() > 0) 
				{
					
					if (mBlockDeathTriggers.firstKey() <= System.currentTimeMillis())
					{
		
						Long key = mBlockDeathTriggers.firstKey();
						HashSet<String> value = mBlockDeathTriggers.remove( key );
						
						for (String blockId : value)
			        	{
							try
							{
				        		// delete block from grid
								Coord<Integer> blockCoord = mGrid.getBlockPosition(blockId);
								// this causes a race condition while matching
								mGrid.deleteBlock( blockCoord );
								//mGrid.deleteBlock(blockId);
								
								synchronized(mSettleEventTriggers)
								{
									mSettleEventTriggers.add( blockCoord.getCol() );
								}
								
								// delete block from drawables (doing this here prevents a race condition)
								mBlocks.kill(blockId);
								
								//game.text += "\tDELETE BLOCK\n";
								//game.textviewHandler.post( game.updateTextView );	
							}
							catch(Exception e)
							{
								//ERROR("DELETE BLOCK: "+e.toString());
							}
			        	}	
					}
					else
						break;
				}
	
				
				
				// copy and reset data
				synchronized (mSettleEventTriggers)
				{
					localSettleCols = mSettleEventTriggers.clone();
					mSettleEventTriggers.clear();
				}
				
				// expand triggers to include all group cols
				HashSet<Integer> newCols = new HashSet<Integer>();
				for (Integer col : localSettleCols)
				{
					for ( HashSet<Integer> cols : mGroups.colValues() )
					{
						for ( Integer groupCol : cols )
						{
							if (groupCol == col)
							{
								newCols.addAll(cols);
								break;
							}
						}
					}
				}
				localSettleCols.addAll(newCols);
	
				
				
				//appendLog("Settle Board: " + localSettleCols);
				
				
				
				//game.text += String.valueOf(mSettleEventTriggers) + "\n";
				
				// count the number of rows to drop each col index by (running count)
				// col : null count
				colDropCount = new SparseIntArray(); 
				// init
				for (Integer col : localSettleCols)
				{
					colDropCount.put(col, 0);
				}
				
				// when a group is encountered, skip all member cols
				// groupId : row encountered on
				HashMap<String, Integer> encounteredGroups = new HashMap<String, Integer>();
				HashSet<Integer> skipCols = new HashSet<Integer>();
				int row;
				for (int idx=0; idx < MainActivity.ROWCOUNT; idx++)
				{
					
					if (mOrient == Orientation.NORMAL)
						row = idx;
					else
						row = MainActivity.ROWCOUNT - (idx+1);
					
					for (Integer col : localSettleCols)
					{
						
						if ( mGrid.isEmpty(row, col) )
						{
							colDropCount.put(col, colDropCount.get(col)+1 );
						}
						else
						{
							// check for group membership
							String blockId = mGrid.get(row, col).getBlockId();
							String groupId = mGroups.getGroup(blockId);
							
							//game.text += row+","+col+":"+mGrid.get(row, col).getBlockId()+",   "+groupId + "\n";
							//game.textviewHandler.post( game.updateTextView );
							
							// found a group? if so, skip this col from now on
							if (groupId != null)
							{
								encounteredGroups.put(groupId, row);
								skipCols.add(col);
								
								// dont drop this block! reset null count
								colDropCount.put(col, 0 );
							}
							
							GLShape block = mBlocks.get(blockId);
							if ( block != null )
							{
								if (block.isMatching)
								{
									// dont drop this block! reset null count
									colDropCount.put(col, 0 );
								}
							}
							
							// is there anything to commit? dont commit any columns we are skipping (due to a group)
							if (colDropCount.get(col) > 0 /*&& !skipCols.contains(col)*/)
							{
							
								if (mOrient == Orientation.NORMAL)
									settleBlockResult = settleBlock(
											new Coord<Integer> (row, col), 
											new Coord<Integer> (row-colDropCount.get(col), col)
											);
								else
									settleBlockResult = settleBlock(
											new Coord<Integer> (row, col), 
											new Coord<Integer> (row+colDropCount.get(col), col)
											);

								
								//if (!settleBlockResult)
								//{
								//	game.text = "Settle (1): " + row+", " + col + " ---> " + (row-colDropCount.get(col)) + ", " + col + " : " + settleBlockResult + "\n\n" + game.text;
								//	game.textviewHandler.post( game.updateTextView );
								//}
								
							}
							
							
						}
					}
				}
				
				//game.text += "\n" + mGroups;
				//game.textviewHandler.post( game.updateTextView );
				
				//game.text = "";
				
				// start from the group of the smallest row and move upwards
				if (mOrient == Orientation.NORMAL)
					encounteredGroups = sortHashMapByValues(encounteredGroups, true);
				else
					encounteredGroups = sortHashMapByValues(encounteredGroups, false);
				
				
				//game.text = encounteredGroups + "\n" + game.text;
				//game.textviewHandler.post( game.updateTextView );
				
				
				// now settle groups; groups are in order from lowest row first highest last
				for (String groupId : encounteredGroups.keySet())
				{
					
					colDropCount = new SparseIntArray(); 
					// init
					for (Integer col : mGroups.getGroupMemberCols(groupId))
					{
						colDropCount.put(col, 0 );
					}
					
					// discover minimal drop for group
					Boolean foundGroup = false;
					for (int idx=0 ; idx < MainActivity.ROWCOUNT && !foundGroup; idx++)
					{
						if (mOrient == Orientation.NORMAL)
							row = idx;
						else
							row = MainActivity.ROWCOUNT - (idx+1);
						
						for (Integer col : mGroups.getGroupMemberCols(groupId))
						{
							String currentBlockGroup = mGroups.getGroup(mGrid.get(row, col).getBlockId());
							if ( mGrid.isEmpty(row, col) )
							{
								colDropCount.put(col, colDropCount.get(col)+1 );
							}
							else if (groupId == currentBlockGroup)
							{
								// found this group... done!
								foundGroup = true;
								break;
							}
							else if (currentBlockGroup != null)
							{
								// found another group
								colDropCount.put(col, 0 );
							}
								
							
						}
					}
					
					Integer minimalDrop = MainActivity.ROWCOUNT;
					for(int i = 0; i < colDropCount.size(); i++) 
					{
						minimalDrop = Math.min(minimalDrop,  colDropCount.get(colDropCount.keyAt(i))  );
					}

						
					
					//game.text = groupId + " : "+minimalDrop + "  of  " + colDropCount +"\n" + game.text;
					//game.textviewHandler.post( game.updateTextView );
							
					
					// commit smallest drop for group member blocks + blocks above
					//foundGroup = false;
					skipCols = new HashSet<Integer>();
					row = 0;
					for (int idx=0 ; row < MainActivity.ROWCOUNT && row >= 0 /*&& !foundGroup*/; idx++)
					{
						if (mOrient == Orientation.NORMAL)
							row = encounteredGroups.get(groupId) + idx;
						else
							row = encounteredGroups.get(groupId) - idx;
						
						if (!(row < MainActivity.ROWCOUNT && row >= 0))
							break;
						
						for (Integer col : mGroups.getGroupMemberCols(groupId))
						{
							if (!skipCols.contains(col))
							{
								if ( mGrid.isEmpty(row, col) )
								{
									colDropCount.put(col, colDropCount.get(col)+1 );
								}
								else
								{
									String groupFound = mGroups.getGroup(mGrid.get(row, col).getBlockId());
									if (groupFound != null && groupFound != groupId)
									{
										//foundGroup = true;
										//break;
										skipCols.addAll(mGroups.getGroupMemberCols(groupFound));
									}
									
									// is there anything to commit?
									if (colDropCount.get(col) > 0  && !skipCols.contains(col) && minimalDrop > 0)
									{	
										
										if (mOrient == Orientation.NORMAL)
											settleBlockResult = settleBlock(
													new Coord<Integer> (row, col), 
													new Coord<Integer> (row-minimalDrop, col)
													);
										else
											settleBlockResult = settleBlock(
													new Coord<Integer> (row, col), 
													new Coord<Integer> (row+minimalDrop, col)
													);

										
										//if (!settleBlockResult)
										//{
										//	game.text = "Settle (2): " + row+", " + col + " ---> " + (row+minimalDrop) + ", " + col + " : " + settleBlockResult + "\n" + game.text;
										//	game.textviewHandler.post( game.updateTextView );
										//}
										
									}
								}
							}
						}
					}
				}
				
				
				// trigger match check
				mBoardMatchTrigger = true;
				
			}

		
		}// end try
		catch(Exception e)
		{
			//appendErr( "SETTLE TRY,CATCH...");
			//ERROR(e.toString());
		}
			
	}
	
	/**
	 * This is another way to sort a HashMap. This way is more useful as it sorts the HashMap and keeps the duplicate values as well.
	 * link: http://lampos.net/sort-hashmap
	 * 
	 * @param passedMap
	 * @return
	 */
	
	LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	List<String> mapKeys;
	List<Integer> mapValues;
	Object key;
	String comp1;
	String comp2;
	Object val;
	Iterator<Integer> valueIt;
	Iterator<String> keyIt;
	private LinkedHashMap<String, Integer> sortHashMapByValues(HashMap<String, Integer> passedMap, Boolean ascending) {
	    mapKeys = new ArrayList<String>(passedMap.keySet());
	    mapValues = new ArrayList<Integer>(passedMap.values());
	    Collections.sort(mapValues);
	    Collections.sort(mapKeys);
	    
	    if (!ascending)
	    	Collections.reverse(mapValues);
	    
	    sortedMap.clear();
	    
	    valueIt = mapValues.iterator();
	    while (valueIt.hasNext()) {
	        val = valueIt.next();
	        keyIt = mapKeys.iterator();
	        
	        while (keyIt.hasNext()) {
	            key = keyIt.next();
	            comp1 = passedMap.get(key).toString();
	            comp2 = val.toString();
	            
	            if (comp1.equals(comp2)){
	                passedMap.remove(key);
	                mapKeys.remove(key);
	                sortedMap.put((String)key, (Integer)val);
	                break;
	            }

	        }

	    }
	    return sortedMap;
	}

	
	
	//##########################################################################################################
	// BONUS BLOCKS
	//##########################################################################################################	
	
	
	int bonusMatchEvent_maxdur = 0;
	HashSet<String> bonusBlockIds = new HashSet<String>();
	HashSet<String> deadBlockIds = new HashSet<String>();
	HashSet<BlockValue> handeledBvs = new HashSet<BlockValue>();
	private int bonusMatchEvent(HashSet<BlockInfo> binfoSet)
	{
		int numBonusBlocksDestroyed = 0;
		
		// remove any nulls from set, check for empty set
		if (binfoSet.contains(null))
			binfoSet.remove(null);
		if (binfoSet.size() == 0)
			return numBonusBlocksDestroyed;
		
		// reset...
		bonusBlockIds.clear();
		handeledBvs.clear();
		
		// get bonus block ids
		bonusMatchEvent_maxdur = 1000;
		//game.text = "Bonus Blocks:\n";
		for (BlockInfo info : binfoSet)
		{
			if (info != null)
			{
				//game.text += "   " + info.mBlockId + " : " +info.mBlockValue + "\n";
				
				bonusBlockIds.add( info.mBlockId );
				if (/*info.mBlockValue != BlockValue.UPSIDEDOWN &&*/ info.mBlockValue != BlockValue.DRUNK )
					bonusMatchEvent_maxdur = Math.max(bonusMatchEvent_maxdur, info.mBlockValue.mDuration);
			}
			
		}
		//game.textviewHandler.post( game.updateTextView );

		
		
		
		for (BlockInfo info : binfoSet)
		{

			if (info != null)
			{
				// for deleting blocks
				Long timeOfDeath = System.currentTimeMillis() + bonusMatchEvent_maxdur;
				HashSet<String> deadBlockIds = new HashSet<String>();
				
				
				// alwasy destroy the block
				mBlocks.get( info.mBlockId ).mAnimation.queueAnimation(Animation.BONUS_BOMB_SPIN_DESTROY,  null , null, mMatchAnimationTime*8, mMatchAnimationTime, null);
				
				deadBlockIds.add(info.mBlockId);				
				
				// might have contributed to the top row, attempt to remove it
				mTopRowBlocks.remove(info.mBlockId);
				
				// needs:
				
				// triggers for:
						// settling
						// board resuming
						// cancel drunk wobbling
				
				if (info.mBlockValue == BlockValue.COLORBOMB)
				{

					// pause board, trigger resume upon end of all animations
					game.getWorld().mBoards.pauseBoardProgression(bonusMatchEvent_maxdur);
					pauseBoardSettle(bonusMatchEvent_maxdur);
					
					// freeze entire board, trigger for unfreeze after the next settle
					// DONT MATCH THE ORIGINAL BLOCKS. match them all together!
					// find all colors on the board
					// queue blocks for "elimination"... drop outs? NO: see alternative
					//		Alternative: swap blocks of the same color as the bonus block to other positions next to one another (around the bonus block)
					//					 make it a typical match! (force override of swap! with z-offset)
					// queue settle after blocks have been deleted

					
					ArrayList<String> blocksWithSameColor = mBlocks.getAllBlockIdsWithColor( mBlocks.get( info.mBlockId ).mColor.mCurrentColor );
					GLShape colorBombBlock;
					for (String blockId : blocksWithSameColor)
					{
						colorBombBlock = mBlocks.get(blockId);
						if (matchBlock( colorBombBlock, 0 ) == true )
						{
							colorBombBlock.isInteractable = false;
							deadBlockIds.add( blockId );
							// might have contributed to the top row, attempt to remove it
							mTopRowBlocks.remove(blockId);
						}
					}					

					// collect general statistics and Score!
					numBonusBlocksDestroyed += blocksWithSameColor.size();
					
					
				}
				else if (info.mBlockValue == BlockValue.DRUNK)
				{
					// if this is already being processed, dont do it again!
					if (mBonusBlockManager.isProcessingBonusBlock(info.mBlockValue) || mBonusBlockManager.mWoozyManager.mForeverWoozy)
						continue;
					
					// Idea: Should this be based on a global skew value for drawing blocks??? queueing this many animations is expensive :( (skew would be a lot better)
					// skew could be based on random sampling for each block
					
					// DONT PAUSE BOARD
					// all/some current, non-garbage blocks, should start "drunk-wobble" animation with random sampling of rates
					// add trigger to cancel animations after a animation period
					// Note: new blocks added to the bottom of the board should be drunk, garbge block members should be sober? (garbage = drunk) 
					
					
					numBonusBlocksDestroyed += 0;
					
				}
				else if (info.mBlockValue == BlockValue.RANDOMBOMB)
				{
					// allow this to be process on "bomb detinating bomb" approach; BUT NOT WITHIN THE SAME MATCH
					if (handeledBvs.contains(info.mBlockValue))
						continue;

					// pause board, trigger resume upon end of all animations
					game.getWorld().mBoards.pauseBoardProgression(bonusMatchEvent_maxdur);
					pauseBoardSettle(bonusMatchEvent_maxdur);
					
					// pause board for period of animation
					// select 10 random blocks from the mBlocks data structure. 
					// start swell/shake effect on bonus block, then rapid shrink/fall back (delete bonus block)
					// spring entire board back and forth (z dir, depth) via board manager
					// start short "wave" animation with the bonus block at the epicenter
					// eliminate blocks when wave hits them (hard coded timing?)
					// trigger settle of entire board after blocks have been deleted
					
					double destroyGroup = 0.4;
					ArrayList<Coord<Integer>> destroySpaces = mGrid.getRandomFilledBlockPositions(destroyGroup);
					
					// destroy playable board
					GLShape destroyBlock;
					Coord<Integer> destroyPos;

					
					for (int row=0; row < MainActivity.ROWCOUNT; row++)
					{
						for (int col=0; col < MainActivity.COLCOUNT; col++)
						{
							try
							{
								destroyPos = new Coord<Integer>(row, col);
								destroyBlock = mBlocks.get(mGrid.getBlockId(row, col));
								if (!destroySpaces.contains(destroyPos))
								{
									if (!destroyBlock.isFrozen && destroyBlock.isInteractable)
										destroyBlock.mAnimation.queueAnimation(Animation.BONUS_WAVE_EPICENTER,  info.mBlockCoord , destroyPos, BlockValue.BOMB_WAVE_DURATION, BlockValue.BOMB_WAVE_DELAY, null);
								}
								else
								{
									if (!destroyBlock.isFrozen && destroyBlock.isInteractable)
									{
										destroyBlock.mAnimation.queueAnimation(Animation.BONUS_WAVE_EPICENTER_DESTROY,  info.mBlockCoord , destroyPos, BlockValue.BOMB_WAVE_DURATION, BlockValue.BOMB_WAVE_DELAY, null);
										destroyBlock.isInteractable = false;
										deadBlockIds.add( mGrid.getBlockId(destroyPos) );
										// might have contributed to the top row, attempt to remove it
										mTopRowBlocks.remove(destroyBlock.getId());
									}
								}
							}
							catch (Exception e)
							{
								// ignore null blocks
							}
						}
					}
					
					
					numBonusBlocksDestroyed += deadBlockIds.size() - 1; //dont include bonus block
					
					
					
				}
				else if (info.mBlockValue == BlockValue.ROWBOMB)
				{
					// pause board, trigger resume upon end of all animations
					game.getWorld().mBoards.pauseBoardProgression(bonusMatchEvent_maxdur);	
					pauseBoardSettle(bonusMatchEvent_maxdur);
					
					// pause board for period of animation
					// swing bonus block row out from board (rotating about the bonus block), achieved with a special draw function:
					//			Translate to poll
					//			rotate about poll
					//			while blocks:
					//				push
					//					translate to absolute block position & draw
					//				pop, do next block...
					// match blocks from top to bottom of stack, staggered
					
					int row = info.mBlockCoord.getRow();
					
					GLShape destroyRowBlock;
					Coord<Integer> destroyPos;
					
					
					for (int col=0; col < MainActivity.COLCOUNT; col++)
					{
						try
						{
							destroyPos = new Coord<Integer>(row, col);
							destroyRowBlock = mBlocks.get(mGrid.getBlockId(destroyPos));
							
							destroyRowBlock.isInteractable = false;
							
							//if (matchBlock(destroyRowBlock, (int)(col*((bonusMatchEvent_maxdur-mMatchAnimationTime)/(float)(StackAttack.COLCOUNT)))) )
							//	deadBlockIds.add( mGrid.getBlockId(destroyPos) );
							
							if (col != info.mBlockCoord.getCol())
								destroyRowBlock.mAnimation.queueAnimation(Animation.BONUS_BOMB_SPIN_DESTROY,  null , null, mMatchAnimationTime*4, 0, null);
							
							if (!destroyRowBlock.isMatching)
								deadBlockIds.add( destroyRowBlock.getId() );
							
							// might have contributed to the top row, attempt to remove it
							mTopRowBlocks.remove( destroyRowBlock.getId());
						}
						catch (Exception e)
						{
							// ignore null blocks
							//Log.e("RowBomb", e.toString());
						}
						
						//game.text = "\n\n\n\n\n\n\n";
						//game.text += deadBlockIds.toString() +"\n";
						//game.textviewHandler.post( game.updateTextView );
						
					}
					
					numBonusBlocksDestroyed += deadBlockIds.size() - 1; //dont include bonus block
					
				}
				else if (info.mBlockValue == BlockValue.SHUFFLE)
				{
					// if this is already being processed, dont do it again!
					if (mBonusBlockManager.isProcessingBonusBlock(info.mBlockValue))
						continue;

					// pause board, trigger resume upon end of all animations
					game.getWorld().mBoards.pauseBoardProgression(bonusMatchEvent_maxdur);
					pauseBoardSettle(bonusMatchEvent_maxdur);
					
					// pause board for duration
					// select 1/3 of board blocks for color swapping, select 1/3 of bloard blocks for location swapping
					// swap/change color with random sampling for duration
					// trigger settle after max duration
					double totalShuffleGroup = 0.8;
					ArrayList<Coord<Integer>> shuffleSpaces = mGrid.getRandomFilledBlockPositions(totalShuffleGroup);
					int shuffleCount = (int) (shuffleSpaces.size()*totalShuffleGroup*1.0);
					int colorCount = (int) (shuffleSpaces.size()*totalShuffleGroup*0.5);
					int randomInterval = 0;
					Coord<Integer> swapDeck = null;
					for (int idx=0; idx < shuffleSpaces.size(); idx++)
					{
						
						String blockId = mGrid.getBlockId(shuffleSpaces.get(idx));
						GLShape shuffleBlock = mBlocks.get(blockId);
						
						if (shuffleBlock.isInteractable)
						{
							
							if (idx < colorCount)
							{
								// color...
								if (shuffleBlock != null)
								{
									randomInterval = (int) (info.mBlockValue.mDuration*0.7 + (int)(Math.random() * ((bonusMatchEvent_maxdur - (info.mBlockValue.mDuration*0.7) - 10)  )));
									
									shuffleBlock.mColor.enqueue(
										new ColorState( 
												new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
												shuffleBlock.mColor.mCurrentColor, 
												Color.pickColorExcept(shuffleBlock.mColor.mCurrentColor),
												new int[] {randomInterval, randomInterval, randomInterval, randomInterval},
												new int[] {0,0,0,0}	// relative to FIFO q
												)
										);
									
									// DO NOT LOCK IT!
									//shuffleBlock.isInteractable = false;
								}
							}
							
							
							if (idx < shuffleCount)
							{
								// shuffle...
								if (swapDeck == null)
								{
									// get first block...
									swapDeck = shuffleSpaces.get(idx);
									continue;
								}
								else
								{
									// commit swap...
									randomInterval = (int) (info.mBlockValue.mDuration*0.5 + (int)(Math.random() * ((bonusMatchEvent_maxdur - (info.mBlockValue.mDuration*0.5) - 10))));
									if (swapBlocks(swapDeck, shuffleSpaces.get(idx), true, randomInterval))
										swapDeck = null;
								}
							}
						}

					}
					
					
					numBonusBlocksDestroyed += 0;
					
				}
				/*
				else if (info.mBlockValue == BlockValue.UPSIDEDOWN)
				{
					// if this is already being processed, dont do it again!
					//if (mBonusBlockManager.isProcessingBonusBlock(info.mBlockValue))
					//	continue;
					
					// IDEA: should board remain frozen???
					
					// DONT PAUSE BOARD
					// set *global* orientation to reverse, propagate changes
					// trigger settle
					// trigger reset of orientation upon blockvalue duration
					// trigger settle
					
					//game.getWorld().mBoards.pauseBoardProgression(bonusMatchEvent_maxdur);
					
					// allow this to be process on "bomb detinating bomb" approach; BUT NOT WITHIN THE SAME MATCH
					if (handeledBvs.contains(info.mBlockValue))
						continue;
					
					if (game.mGlobalOrient == Orientation.NORMAL)
					{
						game.getWorld().mBoards.setNextOrientation(Orientation.REVERSE);
						game.mGlobalOrient = Orientation.REVERSE;
					}
					else
					{
						game.getWorld().mBoards.setNextOrientation(Orientation.NORMAL);
						game.mGlobalOrient = Orientation.NORMAL;
					}
					
					numBonusBlocksDestroyed += 0;
				
					
				}
				*/
				
				// start transform
				mBonusBlockManager.start(info.mBlockValue , 0, 100f, null, MotionEquation.LOGISTIC);
				
				// at now + maxdelay, delete given blockIds from grid (not drawables, thats done by the block thread)
				mBlockDeathTriggers.put( timeOfDeath , deadBlockIds );
				
				// dont handle this bv within this match again
				handeledBvs.add(info.mBlockValue);
				
			}
			
		}
		
		// reap necessary info for each bonus block type (rows, random block locations, etc...)
		
		// tag block object (to let the owning board know not to draw the block) and to initiate processing in draw() and update()
		
		// 
		
		return numBonusBlocksDestroyed;
		
	}
	
	
	
	
	//##########################################################################################################
	// MATCHING
	//##########################################################################################################	
	
	
	
	/**
	 * Searches entire board for matches, commits them, and returns the score equivalent of any matches found;
	 * 
	 * @return		Points the match was worth (if any match found), else 0
	 */
	private SparseArray< HashSet<Integer> > rowMatches;
	private SparseArray< HashSet<Integer> > colMatches;
	private HashSet<Coord<Integer>> matches= new HashSet<Coord<Integer>>();
	private HashSet<Integer> rowSet = new HashSet<Integer>();
	private HashSet<Integer> colSet = new HashSet<Integer>();
	private HashSet<BlockInfo> binfoSet = new HashSet<BlockInfo>();
	private int matchBoard_bonusBlockMatches = 0;
	
	
	private synchronized void matchBoard(HashSet<Coord<Integer>> forceMatches, Boolean isVisible, Set<Integer> eventCols)
	{
		
		// reset trigger
		mBoardMatchTrigger = false;
		
		matches.clear();
		rowSet.clear();
		colSet.clear();
		binfoSet.clear();
		
		if (!game.getIsGameOver())
		{
			synchronized (mBoardModifierMutex)
			{
				rowMatches = matchRows();
				colMatches = matchCols();
				
				
				/*
				game.text = "ROW MATCHES\n";
				game.text += rowMatches + "\n\n";
				game.text += "COL MATCHES\n";
				game.text += colMatches + "\n\n";
				game.textviewHandler.post( game.updateTextView );
				*/
		
				
				// cross correlate matches
				int delayDelimiter = 200;
				int maxDelay = 0;
				int idx;
				
				// combine row findings
				for(int i = 0; i < rowMatches.size(); i++) 
				{
					int row = rowMatches.keyAt(i);
					
					rowSet.add(row);
					
					idx = 1;
					for (Integer col : rowMatches.get(row) )
					{
						colSet.add(col);
						
						// add match to collective
						Coord<Integer> match = new Coord<Integer>(row, col);
						if (matches.add( match ))
						{
							binfoSet.add( matchBlock(match, idx*delayDelimiter));	
							maxDelay = Math.max(maxDelay, idx*delayDelimiter+mMatchTotalMatchAnimationTime);
						}
						idx++;
					}
				}

				// combine col findings
				for(int i = 0; i < colMatches.size(); i++) 
				{
					int col = colMatches.keyAt(i);
					
					colSet.add(col);
					
					idx = 1;
					for (Integer row : colMatches.get(col) )
					{
						rowSet.add(row);
						
						// add match to collective
						Coord<Integer> match = new Coord<Integer>(row, col);
						if (matches.add( match ))
						{
							binfoSet.add(  matchBlock(match, idx*delayDelimiter) );
							maxDelay = Math.max(maxDelay, idx*delayDelimiter+mMatchTotalMatchAnimationTime);
						}
						idx++;
					}
				}
			    
	
			    // delete matched block from grid 
				if (matches.size() > 0)
				{
					// at now + maxdelay, delete given blockIds from grid (not drawables, thats done by the block thread)
					Long key = System.currentTimeMillis() + maxDelay;
					HashSet<String> value = new HashSet<String>();
					for (Coord<Integer> coord : matches )
					{
						value.add( mGrid.getBlockId(coord) );
					}
					mBlockDeathTriggers.put( key , value );
					
				}
			    
			   
			    /*
			    game.text += matches;
			    game.textviewHandler.post( game.updateTextView );
				*/
			    
			    // next to garbage? break up garbage (destroy)
			    meltAdjacentGarbage(matches);
			}
		}
		
		
		if (matches.size() > 0)
		{
			if (binfoSet.size() > 0)
			{
				matchBoard_bonusBlockMatches = bonusMatchEvent(binfoSet);
			}
			
			// queue garbage
			mGarbageGenerator.incrementGenerationPoints( game.getWorld().mBoards.mGameState.getMaxMultiplier(colSet)*Math.max(0.1,matches.size()-3) );

			// collect general statistics and Score!
			game.getWorld().mBoards.mGameState.reapStatistics( matches.size() , matchBoard_bonusBlockMatches, rowSet, colSet, isVisible  );
			
		}
		else
		{
			// no matches while checking the board for matches!
			
			// only if the board progression is not paused (rapid matching)... OR board is paused since the top row is full 
			if (!game.getWorld().mBoards.mPauseBoardProgression || mTopRowBlocks.size() != 0  )
			{
				// reset score multiplier
				game.getWorld().mBoards.mGameState.resetMultiplier(eventCols);
			}
		}


		
		
		
	}
	
	
	/**
	 * return a list of match entries in each row
	 * 
	 * @return		Row matched: list of cols
	 */
	private SparseArray< HashSet<Integer> > rowMembers;
	private synchronized SparseArray<HashSet<Integer>> matchRows()
	{
		rowMembers = new SparseArray< HashSet<Integer> > ();
		
		for (int row=0; row < MainActivity.ROWCOUNT; row++)
		{
			Color lastColor = Color.NONE;
			int runningCount = 0;
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
	
				Color thisColor = Color.NONE;
				String blockId = mGrid.getBlockId(row, col);
				if (blockId != null)
				{
					GLShape block = mBlocks.get(blockId); 
					if (block != null)
					{
						if (block.isInteractable && !block.isFrozen && !block.isDead)
						{
							thisColor = block.mColor.mCurrentColor;
							
							// prevents non playable colors from matching (turorial)
							if (!thisColor.isUsable)
								thisColor = Color.NONE;
							
							if (thisColor == lastColor && thisColor != Color.NONE)
							{
								// found matching row
								runningCount++;
							}
							else
							{
								// end of run, is it big enough for match?
								if (runningCount >= 2)
								{
									if (rowMembers.indexOfKey(row) < 0)
										rowMembers.put(row, new HashSet<Integer>());
									for (int idx=col-runningCount-1; idx < col; idx++)
										rowMembers.get(row).add(idx);
								}
								
								runningCount = 0;
								
							}
						}
						else
						{
							// end of run, is it big enough for match?
							if (runningCount >= 2)
							{
								if (rowMembers.indexOfKey(row) < 0)
									rowMembers.put(row, new HashSet<Integer>());
								for (int idx=col-runningCount-1; idx < col; idx++)
									rowMembers.get(row).add(idx);
							}
							
							runningCount = 0;
							
						}
					}
					else
					{
						lastColor = Color.NONE;
						
						// end of run, is it big enough for match?
						if (runningCount >= 2)
						{
							if (rowMembers.indexOfKey(row) < 0)
								rowMembers.put(row, new HashSet<Integer>());
							for (int idx=col-runningCount-1; idx < col; idx++)
								rowMembers.get(row).add(idx);
						}

						runningCount = 0;
					}
				}
				else
				{
					// end of run, is it big enough for match?
					if (runningCount >= 2)
					{
						if (rowMembers.indexOfKey(row) < 0)
							rowMembers.put(row, new HashSet<Integer>());
						for (int idx=col-runningCount-1; idx < col; idx++)
							rowMembers.get(row).add(idx);
					}

					runningCount = 0;
				}
				lastColor = thisColor;

			}
			
			if (runningCount >= 2)
			{
				if (rowMembers.indexOfKey(row) < 0)
					rowMembers.put(row, new HashSet<Integer>());
				for (int idx=MainActivity.COLCOUNT-runningCount-1; idx < MainActivity.COLCOUNT; idx++)
					rowMembers.get(row).add(idx);
			}
			
		}
		
		return rowMembers;
	}
	
	/**
	 * return a list of match entries in each col
	 * 
	 * @return		Col matched: list of rows
	 */
	private SparseArray< HashSet<Integer> > colMembers;
	private synchronized SparseArray<HashSet<Integer>> matchCols()
	{
		colMembers = new SparseArray< HashSet<Integer> > ();
		
		//game.text = "";
		
		for (int col=0; col < MainActivity.COLCOUNT; col++)
		{
			Color lastColor = Color.NONE;
			int runningCount = 0;
			for (int row=0; row < MainActivity.ROWCOUNT; row++)
			{

				Color thisColor = Color.NONE;
				String blockId = mGrid.getBlockId(row, col);
				if (blockId != null)
				{
					GLShape block = mBlocks.get(blockId);
					if (block != null)
					{
						if (block.isInteractable && !block.isFrozen && !block.isDead)
						{
							thisColor = block.mColor.mCurrentColor;
							
							// prevents non playable colors from matching (turorial)
							if (!thisColor.isUsable)
								thisColor = Color.NONE;
								
							if (thisColor == lastColor && thisColor != Color.NONE )
							{
								// found matching row
								runningCount++;
								//game.text += col + ", " + row+ " : " + runningCount + "\n";
								//game.textviewHandler.post( game.updateTextView );
							}
							else
							{
								// end of run, is it big enough for match?
								if (runningCount >= 2)
								{
									if (colMembers.indexOfKey(col) < 0)
										colMembers.put(col, new HashSet<Integer>());
									for (int idx=row-runningCount-1; idx < row; idx++)
										colMembers.get(col).add(idx);
								}
								
								runningCount = 0;
							}
							
						}
						else
						{
							// end of run, is it big enough for match?
							if (runningCount >= 2)
							{
								if (colMembers.indexOfKey(col) < 0)
									colMembers.put(col, new HashSet<Integer>());
								for (int idx=row-runningCount-1; idx < row; idx++)
									colMembers.get(col).add(idx);
							}
							
							runningCount = 0;
						}
					}
					else
					{
						lastColor = Color.NONE;
						
						// end of run, is it big enough for match?
						if (runningCount >= 2)
						{
							if (colMembers.indexOfKey(col) < 0)
								colMembers.put(col, new HashSet<Integer>());
							for (int idx=row-runningCount-1; idx < row; idx++)
								colMembers.get(col).add(idx);
						}
						
						runningCount = 0;
					}
					
					
					
				}
				else
				{
					
					// end of run, is it big enough for match?
					if (runningCount >= 2)
					{
						if (colMembers.indexOfKey(col) < 0)
							colMembers.put(col, new HashSet<Integer>());
						for (int idx=row-runningCount-1; idx < row; idx++)
							colMembers.get(col).add(idx);
					}
					
					runningCount = 0;
				}
				lastColor = thisColor;

			}
			
			if (runningCount >= 2)
			{
				if (colMembers.indexOfKey(col) < 0)
					colMembers.put(col, new HashSet<Integer>());
				for (int idx=MainActivity.ROWCOUNT-runningCount-1; idx < MainActivity.ROWCOUNT; idx++)
					colMembers.get(col).add(idx);
				
			}
			
			
		}
		
		return colMembers;
	}
	
	
	
	//##########################################################################################################
	// MELTING
	//##########################################################################################################	
	
	
	public synchronized void meltAdjacentGarbage(HashSet<Coord<Integer>> matches)
	{
		ArrayList<Coord<Integer>> matchesList = new ArrayList<Coord<Integer>>(matches);
		HashMap<String, Integer> groupIds = new HashMap<String, Integer>(); //Group, Row
		int row = 0;
		int col = 0;
		String groupId;
		Coord<Integer> match;
		
		
		for (int idx=0; idx < matchesList.size(); idx++)
		{
			match = matchesList.get(idx);
			row = match.getRow();
			col = match.getCol();
			
			try {
				// get the block, get that blocks group, add it to the set
				groupId = mGroups.getGroup(mGrid.get(row+1, col).getBlockId());
				groupIds.put(groupId, row+1);
				HashSet<Coord<Integer>> coords = mGroups.getCoords(groupId, row+1);
				if (!matchesList.containsAll(coords))
					matchesList.addAll(coords);
			} catch (Exception e) { }

			try {
				// get the block, get that blocks group, add it to the set
				groupId = mGroups.getGroup(mGrid.get(row-1, col).getBlockId());
				groupIds.put(groupId, row-1);
				HashSet<Coord<Integer>> coords = mGroups.getCoords(groupId, row-1);
				if (!matchesList.containsAll(coords))
					matchesList.addAll(coords);
			} catch (Exception e) { }

			try {
				// get the block, get that blocks group, add it to the set
				groupId = mGroups.getGroup(mGrid.get(row, col+1).getBlockId());
				groupIds.put(groupId, row);
				HashSet<Coord<Integer>> coords = mGroups.getCoords(groupId, row);
				if (!matchesList.containsAll(coords))
					matchesList.addAll(coords);
			} catch (Exception e) { }

			try {
				// get the block, get that blocks group, add it to the set
				groupId = mGroups.getGroup(mGrid.get(row, col-1).getBlockId());
				groupIds.put(groupId, row);
				HashSet<Coord<Integer>> coords = mGroups.getCoords(groupId, row);
				if (!matchesList.containsAll(coords))
					matchesList.addAll(coords);
			} catch (Exception e) { }
		}
		
		
		
		// get row ordering...
		
		ArrayList<String> keys = new ArrayList<String>(groupIds.keySet());
		
		//Sort keys by values.
		final HashMap<String, Integer> langForComp = groupIds;
		
		if (mOrient == Orientation.NORMAL)
			Collections.sort(keys, 
				new Comparator<String>(){
					public int compare(String left, String right){
						return langForComp.get(left).compareTo(langForComp.get(right));
					}
				});
		else
			Collections.sort(keys, 
					new Comparator<String>(){
						public int compare(String left, String right){
							return langForComp.get(right).compareTo(langForComp.get(left));
						}
					});
		
		// get rid of bad objects...
		if (keys.contains(null))
			keys.remove(null);
				
		// set a tmp pause... considder melt time and melt, then set final time
		//game.getWorld().mBoards.pauseBoardProgression(100);
		//pauseBoardSettle(100);
		
		
		// inflate members & melt garbage...
		String group;
		int idx = 0;

		ArrayList<String> meltedGroups = new ArrayList<String>();
		for(Iterator<String> i=keys.iterator(); i.hasNext();)
		{
			group = i.next();
			if (group != null)
			{
				if (meltGarbage(group, idx)) // pass in row index
				{
					idx++;
					
					meltedGroups.add(group);
					
					// If a barrier garbage type is hit, bail out on melting...
					if (mGroups.getGroupType(group) == GarbageType.BARRIER)
						break;
				}
				else
				{ 
					// already melting
					break;
				}
			}
		}
		
		// delay board movement & settling while melting!
		
		if (idx > 0)
		{
			// pause for a little longer than what it takes to settle (ensures animations complete) + extra
			int waitTime = 0;
			if (game.getWorld().mTutorialState.isInTutorial || game.getWorld().mBoards.mGameState.mGameMode == GameMode.RACE_AGAINST_THE_CLOCK)
			{
				waitTime = (int) ( mUnitMeltInflateTime*0.7 );
				game.getWorld().mBoards.pauseBoardProgression( waitTime );
			}
			else
			{
				waitTime = (idx)*mUnitMeltInflateTime + 200;
				game.getWorld().mBoards.pauseBoardProgression( waitTime );
			}
			
			Long now = System.currentTimeMillis();
			
			for (String meltedGroupId : meltedGroups)
			{
				// freeze members
				for (String blockId : mGroups.getGroupMembers(meltedGroupId))
				{
					mBlocks.get(blockId).isFrozen = true;
				}
				
				// mark group for reaping
				mGroups.markGroupAsDeadAt(meltedGroupId, now + waitTime);
			}
			
			//appendLog("Melted Groups: " + meltedGroups.size());
			
			//pauseBoardSettle((idx)*mUnitMeltInflateTime + 200);
			/*
			game.text  = "idx : " + idx + "\n";
			game.text += "dur : " + mInflateRowDelayDelim + "\n";
			game.text += "wait: " + ((idx)*mInflateRowDelayDelim + 200) + "\n";
			game.textviewHandler.post( game.updateTextView );
			*/
			
		}
		
		
		
		
	}
	
	
	
	
	//##########################################################################################################
	// DESTROY
	//##########################################################################################################	
	
	
	
	public synchronized void destroy(Boolean show)
	{
		lockBoard();
		
		int duration = 700;
		
		if (show)
		{
			// destroy row on deck
			for (GridElement ge : mRowOnDeck)
			{
				// special case, not on board
				mBlocks.get( ge.getBlockId() ).mAnimation.queueAnimation(Animation.DESTROY, new Coord<Integer>( -1, ge.getCol() ), null, duration, null, null);
			}
			
			// destroy playable board
			for (int row=0; row < MainActivity.ROWCOUNT; row++)
			{
				for (int col=0; col < MainActivity.COLCOUNT; col++)
				{
					try
					{
						destroyBlock(row, col, duration);
					}
					catch (Exception e)
					{
						// ignore null blocks
					}
				}
			}
			
			// destroy wrapper
			for (String wrapperId : mGroups.keys() ) 
			{
				Integer col =  mGroups.getGroupMemberCols(wrapperId).get( (mGroups.getGroupMemberCols(wrapperId).size()-1) /2 );
				
				try
				{
					// special case, not on board
					mBlocks.get(wrapperId).mAnimation.queueAnimation(Animation.DESTROY, new Coord<Integer>(-1, col), null, duration, null, null);
				}
				catch (Exception e)
				{
					// ignore if the wrapper is already destroyed (on melting)
				}
				
			}
		}
		
		triggerDelete = System.currentTimeMillis() + duration*8;
		
	}
	
	private long mPauseBoardSettleStartTime = -1;
	private long mResumeBoardSettleTriggerTime = -1;
	public synchronized void pauseBoardSettle()
	{
		isSettlable = false;
		mPauseBoardSettleStartTime = System.currentTimeMillis();
	}

	
	public void pauseBoardSettle(int duration)
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
		
		if (isSettlable == true)
		{
			// only reset pause timer if is not already paused
			pauseBoardSettle();		
			
			mResumeBoardSettleTriggerTime = mPauseBoardSettleStartTime + duration;
		}
		else
		{
			double curWaitTime = mResumeBoardSettleTriggerTime - System.currentTimeMillis();
			
			if (curWaitTime-duration >=0 )
			{
				// already paused, just increment trigger
				mResumeBoardSettleTriggerTime += Math.min( curWaitTime-duration , duration);
			}
			else
			{
				mResumeBoardSettleTriggerTime = System.currentTimeMillis() + duration;
			}
		}
		
		
	}
	
	public synchronized void resumeBoardSettle()
	{
		resumeBoardSettle(false);
	}
	
	
	public void resumeBoardSettle(Boolean force)
	{

		if (
				(!force && !isSettlable && System.currentTimeMillis() >= mResumeBoardSettleTriggerTime)
				||
				force
			)
		{
			
			mResumeBoardSettleTriggerTime = -1;
			
			isSettlable = true;
		}
		
	}
	
	
	
	
	//##########################################################################################################
	// DEBUG CODE
	//##########################################################################################################	
	
	
	
	
	public String fpsScore()
	{
		return 
		
		game.getWorld().mBoards.mGameState.mEndOfGameTimer.isStarted ?
				
		// FPS
		game.getWorld().mBlockUpdater.mFPSReadout +
		
		// Score
		"Score: "+game.getWorld().mBoards.mGameState.mScoreValue + "\n" +
		"Mult : x"+game.getWorld().mBoards.mGameState.mScoreComboMultiplier + "\n" +
		"EOG  : " + game.getWorld().mBoards.mGameState.mEndOfGameTimer.getElapsedTime() + "\n" + 
		
		//Garbage Report
		mGarbageGenerator.report()
		
		:
		
		// FPS
		game.getWorld().mBlockUpdater.mFPSReadout +
		
		// Score
		"Score: "+game.getWorld().mBoards.mGameState.mScoreValue + "\n" +
		"Mult : x"+game.getWorld().mBoards.mGameState.mScoreComboMultiplier + "\n" +
		"EOG  : --- Waiting ---\n" + 
		
		//Garbage Report
		mGarbageGenerator.report();
			

	}
	
	
	//TEMP TEMP TEMP
	String brd, line, bid;
	int error;
	
	public String asciiBoard()
	{
		
		// Board!
		brd = "";
		BlockValue tmpBv;
		error = 0;
		for (int row=0; row < MainActivity.ROWCOUNT; row++)
		{
			line = "";
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				bid = mGrid.getBlockId(row, col);
				
				try
				{
					if (bid != null)
					{
						tmpBv = mBlocks.get(bid).mBlockValue;
					}
					else
					{
						tmpBv = BlockValue.NORMAL;
					}
				}
				catch(Exception e)
				{
					tmpBv = BlockValue.NORMAL;
					error += 1;
				}
				
				
				if (bid == null)
				{
					line += "|   ";
				}
				else if ( tmpBv != BlockValue.NORMAL)
				{
					line += "| B ";
				}
				else
				{
					if (mGroups.isInAnyGroup(bid))
						line += "| G ";
					else
						line += "| X ";
				}
				

				
			}
			brd = line + "|\n" + brd;
		}
		
		return brd+"-----------------------------"+"\nerrors: " + String.valueOf(error);
		
	}
	
	
	int leftWidth = 30, rightWidth = 30;
	String rpt = "";
	public String reportPane(String left, String right)
	{
		rpt = "";
		int max = 0;
		
		String[] leftLines  = left.split("\\r?\\n");
		String[] rightLines = right.split("\\r?\\n");
		
		max = Math.max(leftLines.length, rightLines.length);
		
		for (int idx=0 ; idx < max ; idx++)
		{
			// left
			if (idx < leftLines.length)
				rpt += String.format("%1$-" + leftWidth + "s", leftLines[idx]);
			else
				rpt += String.format("%1$-" + leftWidth + "s", " ");
					
			// right
			if (idx < rightLines.length)
				rpt += String.format("%1$-" + rightWidth + "s", rightLines[idx]);
			else
				rpt += String.format("%1$-" + rightWidth + "s", " ");
			
			
			rpt += "\n";
		}
		
		
		return rpt;
		
	}
	
	
	public String report()
	{
		// all 4
		//return reportPane( asciiBoard(), fpsScore() ) + "\n" + reportPane( game.log, game.err );
		
		// only 2
		//return reportPane( asciiBoard(),  game.err );
		
		// only 1
		//return "\n\n\n\n\n\n\n" + fpsScore() + "\n\nSettlable: " + isSettlable + "\nPaused: " + game.getWorld().mBoards.mPauseBoardProgression + "\nOrientation: " + mOrient;
		
		return asciiBoard();
		
		//return getId() + "\n\n" + asciiBoard() + "\n\n" + game.getWorld().mBoards.mCurrentGlobalRowIndex + "    " + game.getWorld().mBoards.mCurrentGLYOffset;
		
		//return getId() + "\n\n" + asciiBoard() + "\n\nPaused: " + game.getWorld().mBoards.mPauseBoardProgression + "\n\nMatchBoard() Calls:" + String.valueOf(calledMatchBoard);
		
		//return getId() + "\n\n" + asciiBoard() + "\n\n" + mBonusBlockManager.report() + "\n\n" + "Generate Bonus Block: " + String.valueOf(mGenerateBonusBlock) + "  " + String.valueOf(TMPTMPTMP_BonusBlockCount);
	}
	
	/*
	int limit = 35*35;
	private void appendLog(String text)
	{
		
		if (game.log.length() > limit)
			game.log = text + "\n" + game.log.substring(0, limit);
		else
			game.log = text + "\n" + game.log;
		
		DEBUG(text);
	}
	
	private void appendErr(String text)
	{
		
		if (game.err.length() > limit)
			game.err = text + "\n" + game.err.substring(0, limit);
		else
			game.err = text + "\n" + game.err;
		
		ERROR(text);
	}
	*/
	
	
	
	//##########################################################################################################
	// ORIENTATION
	//##########################################################################################################	
	
	
	
	
	public synchronized void setNextOrientation(Orientation orient)
	{
		mNextOrient = orient;
	}
	
	
	public synchronized void setOrientation()
	{
		synchronized (mBoardModifierMutex)
		{
			
			if (!isFlippable)
			{
				if (!game.getWorld().mBoards.mPauseBoardProgression)
				{
					isFlippable = true;
					if (isInteractable && isSettlable)
					{
						settleEntireBoard();	
					}
				}
				else
					return;
			}
			else
			{
				if (game.getWorld().mBoards.mPauseBoardProgression)
					isFlippable = false;
			}
			
			
			// keep next orientation ready
			if (mOrient != mNextOrient)
			{

				// drop active block on orientation change
				if (isInteractable && mBlocks.mActiveBlock != null)
					dropActiveBlock(true, null, false);
					

				// set orientation
				if (isInteractable && isSettlable)
				{
				
					//game.text = String.valueOf(mOrient);
					//game.textviewHandler.post( game.updateTextView );
					
					mOrient = mNextOrient;
					mRelocateRODTrigger = true;
					settleEntireBoard();
					
				}
			}
		}
	}
	
	
	
	
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// FOR GAME THREADS
	
	
	
	
	
	
	//DecimalFormat twoDForm = new DecimalFormat("#.##");
	private ConcurrentSkipListSet<Integer> update_settleEventCols;
	
	public synchronized void update(long now, Boolean primaryThread, Boolean secondaryThread, Boolean interactable, Boolean isVisable)
	{
		// WHY WAS THIS HERE???
		// ensure it is safe to continue updating
		//if (interactable != isInteractable)
		//	isInteractable = interactable;
		
		if (primaryThread)
		{
			//twoDForm.setMinimumFractionDigits(2);
			///game.text = "";
			

			
			for (GLShape block : mBlocks.values())
			{
				if (!block.isDead)
				{
					block.update(now, primaryThread, secondaryThread);
				}
				else
				{
					// delete block from drawables
					//String blockId = block.getId();
					try
					{
						// delete block
						mBlocks.remove(block);
					} 
					catch (Exception e)
					{
						//appendErr("UPDATE: failed to delete block!");
					}

					
				}
				
				/*
				game.text += block.mColor.mCurrentColor + ", ";
				
				game.text += "["+twoDForm.format(block.mAnimation.mCurrentPoints[0][0])+", "+twoDForm.format(block.mAnimation.mCurrentPoints[0][1])+", "+twoDForm.format(block.mAnimation.mCurrentPoints[0][2])+"] " + "";
				game.text += "["+twoDForm.format(block.mAnimation.mCurrentPoints[1][0])+", "+twoDForm.format(block.mAnimation.mCurrentPoints[1][1])+", "+twoDForm.format(block.mAnimation.mCurrentPoints[1][2])+"] " + "";
				game.text += "["+twoDForm.format(block.mAnimation.mCurrentPoints[2][0])+", "+twoDForm.format(block.mAnimation.mCurrentPoints[2][1])+", "+twoDForm.format(block.mAnimation.mCurrentPoints[2][2])+"] " + "\n";
	
				try
				{
					game.text += "\t Animation Time: " + twoDForm.format(block.mAnimation.getCompanyFrontAnimation(Animation.PICKUP).getRunningTime(now, 1) ) + "\n";
				}
				catch (Exception e)
				{
					//pass
				}
				*/
				
			}
			
			//game.textviewHandler.post( game.updateTextView );
	
			
			//if (mBlocks.mActiveBlock != null)
			//	game.text = "[ "+String.valueOf(game.getWorld().hasFinger[0]) + ", "+String.valueOf(game.getWorld().hasFinger[1])+" ]   " + mBlocks.mActiveBlock.mColor.mCurrentColor;
			//else
			//	game.text = "[ "+String.valueOf(game.getWorld().hasFinger[0]) + ", "+String.valueOf(game.getWorld().hasFinger[1])+" ]";
			//game.textviewHandler.post( game.updateTextView );
			
			
			
			//if (mBlocks.mActiveBlock != null)
			//	game.text = "[ "+String.valueOf(mBlocks.mActiveBlock.mAnimation.mCurrentPoints[0][2]) +" ]   " + mBlocks.mActiveBlock.mColor.mCurrentColor + "     " + game.getWorld().mBoards.mBoardDepth;
			//game.textviewHandler.post( game.updateTextView );
			
			
			
			// drop the active block if...
			// invalid (prevents multiple active blocks) or ...
			if (mBlocks.mActiveBlock != null && game.getWorld().hasFinger[0] == false)
			{
				dropActiveBlock(null, false);
			}
			// drop the active block if...
			// reached timeout
			else if (mBlocks.mActiveBlock != null && ((mBlocks.mActiveBlockStartTime+ShapeManager.mActiveBlockExpirationDuration) < now) && mBlocks.mActiveBlockStartTime != -1 )
			{
				dropActiveBlock(null, true);
			}
			

		}
		
		if (secondaryThread)
		{
			//game.debug("Game: " + game.getWorld().mBoards.mGameState.mGameMode,false);
			//game.debug("Diff: " + game.getWorld().mBoards.mGameState.mGameDiff,true);
			
			if( triggerDelete < System.currentTimeMillis() && triggerDelete != -1 && !isDeleted)
			{
				isDeleted = true;
			}
			
			// don't settle the board if the game is over
			if (!game.getIsGameOver())
			{

				// set flippable state and next orientation (if needed)
				setOrientation();
				
				// generate garbage (only if not paused!)
				if (!game.getWorld().mBoards.mPauseBoardProgression)
				{
					// garbage blocks
					generateGarbage( mGarbageGenerator.update(now) );
					
					// bonus blocks
					if (mGenerateBonusBlock == false)
					{
						// keep the timer data
						mGenerateBonusBlock = mBonusBlockManager.updateGenerator(now);
					}
					else
					{
						// dont keep what the timer says to do
						mBonusBlockManager.updateGenerator(now);
					}
				}
				// trigger resume settling (if needed)
				/*
				if (!isSettlable)
					if (now >= mResumeSettleTriggerTime && mResumeSettleTriggerTime != -1)
						resumeBoardSettle(false);
				*/
				
				if (mRelocateRODTrigger)
					if (isFlippable && isSettlable && isInteractable)
						relocateRowOnDeck();
				
				
				if (isSettlable)
				{
					update_settleEventCols = mSettleEventTriggers.clone();
					
					if (mSettleEventTriggers.size() > 0)
					{
						settleBoard();
					}
					else if (mBlockDeathTriggers.size() > 0)
					{
						if (mBlockDeathTriggers.firstKey() <= now)
						{
							settleBoard();
						}
					}
					else if (mGroups.hasReapableGroups(now))
					{
						settleBoard();
					}
					
				}
				else
				{
					// trigger resume (if needed)
					if (now >= mResumeBoardSettleTriggerTime && mResumeBoardSettleTriggerTime != -1)
					{
						resumeBoardSettle(true);
					}
				}
				
				// Look for matching blocks!
				if (mBoardMatchTrigger)
				{
					matchBoard(null, isVisable, update_settleEventCols);
				}
				
				/*
				game.text = report();
				game.textviewHandler.post( game.updateTextView );
				*/
				
				/*
				game.text  = "can settle     : " + isSettlable + "\n";
				//game.text += "trigger settle : " + mResumeSettleTriggerTime + "\n";
				if (mResumeSettleTriggerTime != -1)
					game.text += "countdown      : " + (mResumeSettleTriggerTime-now) + "\n";
				game.textviewHandler.post( game.updateTextView );
				*/
				
			}
			
		}
		
		// update bonus value progression
		mBonusBlockManager.updateAnimator(now, primaryThread, secondaryThread);
		
		
	}
	
	
	
	
	
	// Pos,Rot,Siz x X,Y,Z
	private float[][] tempOffset = new float[3][3];
	private int	      tempIndex = 0;
	public void draw(GL10 gl)
	{
		if (isVisible)
		{
			//game.text = "";
			
			// Elements will have a reference of 0 depth within their own gl relative frame when at the board depth
			gl.glTranslatef(0, 0, game.getWorld().mBoards.mBoardDepth);
			
			for (GLShape block : mBlocks.values())
			{
				if (!block.isDead)
				{
					/*
					if (
							block.mAnimation.mCurrentPoints[2][0] != 1f	||
							block.mAnimation.mCurrentPoints[2][1] != 1f	||
							block.mAnimation.mCurrentPoints[2][2] != 1f
						)
					{
						game.text += block.getId() + " : " + 
								block.mAnimation.mCurrentPoints[2][0] + " , " +
								block.mAnimation.mCurrentPoints[2][1] + " , " +
								block.mAnimation.mCurrentPoints[2][2] + " , " +
								"\n";
						
					}
					*/
					gl.glPushMatrix();
					
					if (mBonusBlockManager.isProcessingBonusBlock(BlockValue.DRUNK) || mBonusBlockManager.mWoozyManager.mForeverWoozy)
					{
						for (int transType = 0; transType < 3; transType++)
						{
							for (int xyz = 0; xyz < 3; xyz++)
							{
								tempIndex = mBonusBlockManager.mWoozyManager.getIndexFromId(block.getId(), transType, xyz);
								
								if (transType == AnimationBroker.POS)
									tempOffset[transType][xyz] = mBonusBlockManager.mWoozyManager.mResultProgress[tempIndex] * game.getWorld().mGLBlockLength*0.1f;
								else if (transType == AnimationBroker.ROT)
									tempOffset[transType][xyz] = mBonusBlockManager.mWoozyManager.mResultProgress[tempIndex] * 20f;
								else if (transType == AnimationBroker.SIZ)
									tempOffset[transType][xyz] = mBonusBlockManager.mWoozyManager.mResultProgress[tempIndex] * 0.2f;
							}
						}
						
						block.draw(gl, tempOffset);
						
					}
					else
					{
						block.draw(gl);
					}
					
					gl.glPopMatrix();
				}
			}
			
			//game.textviewHandler.post( game.updateTextView );

		}
	}
}
