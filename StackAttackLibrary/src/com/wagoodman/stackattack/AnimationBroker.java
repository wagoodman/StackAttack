package com.wagoodman.stackattack;


import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


import com.wagoodman.stackattack.Coord;
import com.wagoodman.stackattack.DropSection;
import com.wagoodman.stackattack.Orientation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.World;

import android.content.Context;
import android.util.SparseArray;


public final class AnimationBroker
{

	private final Context mContext;
	private final MainActivity game;
	
	// Animations to be processed: 
	private TransformState[]  mCompanyFront;
	public static final int QUEUESIZE = 20;					// maximum concurrent operations
	public PriorityQueue<Integer> mAvailableIndexes;
	
	// trigger cleanup (after destroy); this is the time that the board should delete the block
	public long mTimeOfDeath = -1;
	
	// Current Absolute Position / Rotation / Size
	public float[][]	mCurrentPoints;		// {	
											//		Trans 	{x,y,z} in gl pos units	; 
											// 		Rotate	{r,p,y} in degrees		;
											// 		Scale	{x,y,z} multiplier		;
											// }
	
	// array keys
	public static final int POS	= 0;
	public static final int ROT	= 1;
	public static final int SIZ	= 2;
	public static final int X	= 0;
	public static final int Y	= 1;
	public static final int Z	= 2;
	
	private Boolean moveUp = false;
	
	//private Boolean gl3dTextYOffsetCompensation = false;
	
	public AnimationBroker(Context context)
	{
		// get the game object from context
		mContext = context;
		game = (MainActivity) (mContext);
		
		init();
		
	}
	
	
	public AnimationBroker(Context context, int row, int col)
	{
		// get the game object from context
		mContext = context;
		game = (MainActivity) (mContext);
		
		init();
		
		moveUp = true;
		
		setPositionByRC(row, col, 0);
		
		
		
	}
	
	
	public AnimationBroker(Context context, int row, int col, Boolean move)
	{
		// get the game object from context
		mContext = context;
		game = (MainActivity) (mContext);
		
		init();
		
		moveUp = move;
		
		setPositionByRC(row, col, 0);
		
		
		
	}
	/*
	public AnimationBroker(Context context, int row, int col, Boolean move, Boolean yCompensate)
	{
		// get the game object from context
		mContext = context;
		game = (CA_Game) (mContext);
		
		init();
		
		if (yCompensate == true)
		{
			rowCompensation = game.getWorld().mBoards.getCurrentBoard().mCurrentRowIndex;
			row -= rowCompensation;
		}
		
		moveUp = move;
		
		setPositionByRC(row, col, 0);
		
		
		
		//gl3dTextYOffsetCompensation = yCompensate;
		
	}
	*/
	private void init()
	{
		flushCurrentAnimations();

		mCurrentPoints = new float[3][3];
		mCurrentPoints[POS]	= new float[] {0,0,0};	// Translate
		mCurrentPoints[ROT]	= new float[] {0,0,0};	// Rotate
		mCurrentPoints[SIZ]	= new float[] {1,1,1};	// Scale
	}	
	
	public void flushCurrentAnimations()
	{
		// initialize company front;
		mCompanyFront = new TransformState[QUEUESIZE];
		
		mAvailableIndexes = new PriorityQueue<Integer>();
		for (int idx=0; idx < QUEUESIZE; idx++)
			mAvailableIndexes.add(idx);
	}
	
	
	
	
	
	
	// ONLY [Row,Col] 
	private Coord<Float> convertRCToPx(int row, int col)
	{
		// assumes origin @ bottom left
		
		if (moveUp)
			return new Coord<Float> (
				(col*game.getWorld().mScreenBlockLength) + (game.getWorld().mScreenBlockLength/2f)
				,
				(row*game.getWorld().mScreenBlockLength) + (game.getWorld().mScreenBlockLength/2f) - game.getWorld().mBoards.mCurrentGlobalRowIndex*(game.getWorld().mScreenBlockLength)
				);	
		else
			return new Coord<Float> (
					(col*game.getWorld().mScreenBlockLength) + (game.getWorld().mScreenBlockLength/2f)
					,
					(row*game.getWorld().mScreenBlockLength) + (game.getWorld().mScreenBlockLength/2f)
					);	
	}
	
	
	
	// ONLY [X,Y] @ current depth
	/*
	private float[] convertPxToGl(Float pixX, Float pixY)
	{
		return convertPxToGl(pixX, pixY, mCurrentPoints[POS][Z]);
	}
	*/
	private float[] convertPxToGl(Coord<Float> pixXY, Boolean invert)
	{
		if (invert)
			return convertPxToGl(pixXY.getX(), game.getWorld().mScreenHeight-pixXY.getY(), mCurrentPoints[POS][Z]);
		else
			return convertPxToGl(pixXY.getX(), pixXY.getY(), mCurrentPoints[POS][Z]);
	}
	
	// ONLY [X,Y] @ depth=board depth + offset
	private float[] convertPxToGl(Float pixX, Float pixY, Float glDepthOffset)
	{
		float[] planeDimensions = game.getWorld().getPlaneDimensions( -game.getWorld().mBoards.mBoardDepth + glDepthOffset );
		
		//game.text  = "Px: " + pixX + ", " + pixY + "\n";
		//game.text += "GL: " + String.valueOf((pixX/game.getWorld().mScreenWidth)*planeDimensions[X] - (planeDimensions[X]/2f)) + ", " + 
		//			String.valueOf(((game.getWorld().mScreenHeight - pixY)/game.getWorld().mScreenHeight)*planeDimensions[Y] - (planeDimensions[Y]/2f)) + "\n";
		//game.textviewHandler.post( game.updateTextView );
		
		// assumes origin @ center screen
		return new float[] { 
				(pixX/game.getWorld().mScreenWidth)*planeDimensions[X] - (planeDimensions[X]/2f)
				, 
				((game.getWorld().mScreenHeight - pixY)/game.getWorld().mScreenHeight)*planeDimensions[Y] - (planeDimensions[Y]/2f)
				};
	}
	
	
	
	
	public void setPositionByGl(Float glX, Float glY, Float glZ )
	{
		synchronized(mCurrentPoints[POS])
		{
			mCurrentPoints[POS][X] = glX;
			mCurrentPoints[POS][Y] = glY;
			mCurrentPoints[POS][Z] = glZ;	
		}
	}
	
	
	// [x pix, y pix] ; board depth = 0, + is closer to camera, - behind board ; depth unit = multiples of world depth units
	public void setPositionByPix(Float pixX, Float pixY, float glDepthOffset)
	{
		float[] glPoint = convertPxToGl(pixX, game.getWorld().mScreenHeight-pixY, glDepthOffset);
		
		setPositionByGl( glPoint[X], glPoint[Y], glDepthOffset);
		
	}

	// [x pix, y pix] ; board depth = 0, + is closer to camera, - behind board ; depth unit = multiples of world depth units
	public void setPositionByPix(Coord<Float> pixXY, float glDepthOffset)
	{
		setPositionByPix(pixXY.getX(), pixXY.getY(), glDepthOffset);
	}

	// [x pix, y pix] ; keep current depth
	public void setPositionByPix(Coord<Float> pixXY)
	{
		setPositionByPix(pixXY.getX(), pixXY.getY(), mCurrentPoints[POS][Z]);
	}

	// [x pix, y pix] ; keep current depth
	public void setPositionByPix(Float pixX, Float pixY)
	{
		setPositionByPix(pixY, pixX, mCurrentPoints[POS][Z]);
	}
	
	// [row, col] ; board depth = 0, + is closer to camera, - behind board ; depth unit = multiples of world depth units
	public void setPositionByRC(int row, int col, float glDepthOffset)
	{
		setPositionByPix( convertRCToPx(row, col), glDepthOffset );
	}
	
	
	public void setRotation(float roll, float pitch, float yaw)
	{
		synchronized(mCurrentPoints[ROT])
		{
			mCurrentPoints[ROT][X] = roll;
			mCurrentPoints[ROT][Y] = pitch;
			mCurrentPoints[ROT][Z] = yaw;	
		}
	}
	
	public void setScale(float x, float y, float z)
	{
		synchronized(mCurrentPoints[SIZ])
		{
			mCurrentPoints[SIZ][X] = x;
			mCurrentPoints[SIZ][Y] = y;
			mCurrentPoints[SIZ][Z] = z;	
		}
	}
	
	
	/**
	 * Adds the item to the queue.
	 * 
	 * @param item
	 * @return			The index of the added item. -1 if it could not be added.
	 */
	
	public int add(TransformState item)
	{
		if (mAvailableIndexes.peek() == null)
			return -1;
		
		int idx = mAvailableIndexes.remove();
		mCompanyFront[idx] = item;

		return idx;
	}
	
	
	
	/**
	 * Internal method to expand the company front array. (see draft 3, ArrayQueue)
	 */
	/*
	private void DoubleQueue()
	{
		TransformState[] newArray;

		newArray = (TransformState[])new Object[mCompanyFront.length * 2];

		// Copy elements that are logically in the queue
		for (int i = 0; i < currentSize; i++, front = increment(front))
			newArray[i] = theArray[front];

		mCompanyFront = newArray;
		front = 0;
		back = currentSize - 1;
	}
	*/
	
	
	// implement occupied indexes to loop across to save time (instead of skipping null values)
	
	public void processAnimationElements(long now)
	{

		for (int idx=0; idx<mCompanyFront.length ;idx++)
		{
			// dont process null indexes
			if (mCompanyFront[idx] != null)
			{
			
				// check if complete, if so remove it
				if (mCompanyFront[idx].isCompleted)
				{
					// continuous transforms dont have endpoints to stop on, they wait for the next period
					if (mCompanyFront[idx].mTimeDomain == TimeDomain.FINITE)
					{
						// set position to final (without this, it may not zero in on final end pos)
						if (mCompanyFront[idx].mTransform == TransformType.TRANSLATE)
						{
							synchronized(mCurrentPoints[POS])
							{
								if (mCompanyFront[idx].mDirEnable[X])
									mCurrentPoints[POS][X] = mCompanyFront[idx].mEndPoint[X];
								if (mCompanyFront[idx].mDirEnable[Y])
									mCurrentPoints[POS][Y] = mCompanyFront[idx].mEndPoint[Y];
								if (mCompanyFront[idx].mDirEnable[Z])
									mCurrentPoints[POS][Z] = mCompanyFront[idx].mEndPoint[Z];
							}
						}
						else if (mCompanyFront[idx].mTransform == TransformType.ROTATE)
						{
							synchronized(mCurrentPoints[ROT])
							{
								if (mCompanyFront[idx].mDirEnable[X])
									mCurrentPoints[ROT][X] = mCompanyFront[idx].mEndPoint[X];
								if (mCompanyFront[idx].mDirEnable[Y])
									mCurrentPoints[ROT][Y] = mCompanyFront[idx].mEndPoint[Y];
								if (mCompanyFront[idx].mDirEnable[Z])
									mCurrentPoints[ROT][Z] = mCompanyFront[idx].mEndPoint[Z];
							}
						}
						else if (mCompanyFront[idx].mTransform == TransformType.SCALE)
						{
							synchronized(mCurrentPoints[SIZ])
							{
								if (mCompanyFront[idx].mDirEnable[X])
									mCurrentPoints[SIZ][X] = mCompanyFront[idx].mEndPoint[X];
								if (mCompanyFront[idx].mDirEnable[Y])
									mCurrentPoints[SIZ][Y] = mCompanyFront[idx].mEndPoint[Y];
								if (mCompanyFront[idx].mDirEnable[Z])
									mCurrentPoints[SIZ][Z] = mCompanyFront[idx].mEndPoint[Z];
							}
						}
					}
					// destroy & make space available
					mCompanyFront[idx] = null;
					mAvailableIndexes.add(idx);
	
				}
				// not complete, keep going (or start)
				else 
				{
					
					// start animation if needed
					if (!mCompanyFront[idx].isTransforming)
					{
						mCompanyFront[idx].start(now);
					}
						
					// apply transform; update x,y,z
					
					synchronized(  mCurrentPoints[ mCompanyFront[idx].mTransform.ordinal() ]  )
					{
						
						if (mCompanyFront[idx].mDirEnable[X])
						{
							if (mCompanyFront[idx].getRunningTime(now, X) >= 0)
							{
								if (mCompanyFront[idx].mTimeDomain == TimeDomain.FINITE) 
									mCurrentPoints[ mCompanyFront[idx].mTransform.ordinal() ][X] = (float)
									MotionEquation.applyFinite(
											mCompanyFront[idx].mTransform,
											mCompanyFront[idx].mEquation[X], 
											mCompanyFront[idx].getRunningTime(now, X), 
											mCompanyFront[idx].mDuration[X], 
											mCompanyFront[idx].mStartPoint[X], 
											mCompanyFront[idx].mEndPoint[X]
												);
								// continuous
								else
									mCurrentPoints[ mCompanyFront[idx].mTransform.ordinal() ][X] = (float)
									MotionEquation.applyContinuous(
											mCompanyFront[idx].mTransform,
											mCompanyFront[idx].mEquation[X], 
											mCompanyFront[idx].getRunningTime(now, X), 
											mCompanyFront[idx].mRate[X], 
											mCompanyFront[idx].mPeriod[X], 
											mCompanyFront[idx].mStartPoint[X]
												);
							}
						}
						
						if (mCompanyFront[idx].mDirEnable[Y])
						{
							if (mCompanyFront[idx].getRunningTime(now, Y) >= 0)
							{
								if (mCompanyFront[idx].mTimeDomain == TimeDomain.FINITE) 
									mCurrentPoints[ mCompanyFront[idx].mTransform.ordinal() ][Y] = (float)
									MotionEquation.applyFinite(
											mCompanyFront[idx].mTransform,
											mCompanyFront[idx].mEquation[Y], 
											mCompanyFront[idx].getRunningTime(now, Y), 
											mCompanyFront[idx].mDuration[Y], 
											mCompanyFront[idx].mStartPoint[Y], 
											mCompanyFront[idx].mEndPoint[Y]
											);
								// continuous
								else
									mCurrentPoints[ mCompanyFront[idx].mTransform.ordinal() ][Y] = (float)
									MotionEquation.applyContinuous(
											mCompanyFront[idx].mTransform,
											mCompanyFront[idx].mEquation[Y], 
											mCompanyFront[idx].getRunningTime(now, Y), 
											mCompanyFront[idx].mRate[Y], 
											mCompanyFront[idx].mPeriod[Y], 
											mCompanyFront[idx].mStartPoint[Y]
												);
							}
						}
						
						if (mCompanyFront[idx].mDirEnable[Z]) // user cannot manipulate Z directly, so no override for it!
						{
							if (mCompanyFront[idx].getRunningTime(now, Z) >= 0)
							{
								if (mCompanyFront[idx].mTimeDomain == TimeDomain.FINITE) 
									mCurrentPoints[ mCompanyFront[idx].mTransform.ordinal() ][Z] = (float)
									MotionEquation.applyFinite(
											mCompanyFront[idx].mTransform,
											mCompanyFront[idx].mEquation[Z], 
											mCompanyFront[idx].getRunningTime(now, Z), 
											mCompanyFront[idx].mDuration[Z], 
											mCompanyFront[idx].mStartPoint[Z], 
											mCompanyFront[idx].mEndPoint[Z]
											);
								// continuous
								else
									mCurrentPoints[ mCompanyFront[idx].mTransform.ordinal() ][Z] = (float)
									MotionEquation.applyContinuous(
											mCompanyFront[idx].mTransform,
											mCompanyFront[idx].mEquation[Z], 
											mCompanyFront[idx].getRunningTime(now, Z), 
											mCompanyFront[idx].mRate[Z], 
											mCompanyFront[idx].mPeriod[Z], 
											mCompanyFront[idx].mStartPoint[Z]
												);
							}
						}
							
					} // end sync'd
					

				}
			}	
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// Queue Multiple, Specific Animations

	

	
	HashMap< Animation , SparseArray<String> > queueElements = new HashMap< Animation , SparseArray<String> >();
	
	// temp's
	private float[] startPos, endPos, middlePos1, middlePos2;
	Coord<Float> pixCoord;
	private int rowCompensation;
	
	public Boolean queueAnimation(Animation anim, Coord<Integer> fromRowCol, Coord<Integer> toRowCol, Integer duration, Integer delay, Orientation orient)
	{
		TransformState[] states = null;
	
		/*
		if (gl3dTextYOffsetCompensation == true)
		{
			rowCompensation = game.getWorld().mBoards.getCurrentBoard().mCurrentRowIndex;
			if (fromRowCol != null)
				fromRowCol.setRow( fromRowCol.getRow() - rowCompensation );
			if (toRowCol != null)
				toRowCol.setRow( toRowCol.getRow() - rowCompensation );
		}
		*/
		
		// Fetch Animation States...
		
		if (anim == Animation.RESTORESTEADYSTATE)
		{
			// cancel previous state restore
			cancelAnimations( queueElements.get(Animation.RESTORESTEADYSTATE) );
			
			states = Animation.queueRestoreSteadyState(mCurrentPoints);
			
		}
		else if (anim == Animation.PICKUP)
		{
			// cancel previous drop
			cancelAnimations( queueElements.get(Animation.DROP) );
			
			states = Animation.queuePickup(mCurrentPoints);
			
		}
		else if ( anim == Animation.DROP )
		{
			// cancel pickup
			cancelAnimations( queueElements.get(Animation.PICKUP) );
			
			states = Animation.queueDrop(mCurrentPoints);
		}

		else if ( anim == Animation.SWAPTO )
		{
			// cancel previous swap
			cancelAnimations( queueElements.get(Animation.SETTLETO), false );
			cancelAnimations( queueElements.get(Animation.SWAPTO) );
			
			pixCoord = convertRCToPx(fromRowCol.getRow(), fromRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			startPos = mCurrentPoints[POS].clone();
			
			states = Animation.queueSwapTo(mCurrentPoints, fromRowCol, toRowCol, startPos, endPos, duration, game.getGlobalOrient());
			
		}
		else if ( anim == Animation.SETTLETO )
		{
			// cancel previous settle to
			cancelAnimations( queueElements.get(Animation.SETTLETO) );
			cancelAnimations( queueElements.get(Animation.SWAPTO), false );
			
			if (orient == Orientation.NORMAL)
				delay = (toRowCol.getRow()+toRowCol.getCol())*20;
			else
				delay = ((MainActivity.ROWCOUNT-toRowCol.getRow())+toRowCol.getCol())*20;

			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			startPos = mCurrentPoints[POS].clone();
			
			states = Animation.queueSettleTo(mCurrentPoints, startPos, endPos, delay);
			
		}
		else if ( anim == Animation.DROPSPAWN )
		{
			// multiple blocks spawn together, dont delay!
			/*
			if (orient == Orientation.NORMAL)
				delay = (toRowCol.getRow()+toRowCol.getCol())*20;
			else
				delay = ((CA_Game.ROWCOUNT-toRowCol.getRow())+toRowCol.getCol())*20;
			*/
			if (delay == null)
				delay = 0;
			
			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );

			if (game.getGlobalOrient() == Orientation.NORMAL)
				startPos = convertPxToGl( pixCoord.getX(), -game.getWorld().mScreenBlockLength, 0f );
			else
				startPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight+game.getWorld().mScreenBlockLength, 0f );

			// start off the board! Immediately!
			setPositionByGl( startPos[X], startPos[Y], 0f );
			
			states = Animation.queueDropSpawn(startPos, endPos, duration, delay);
			
		}
				
		else if ( anim == Animation.GROWSPAWN )
		{
			
			if (orient == Orientation.NORMAL)
				delay = (toRowCol.getRow()+toRowCol.getCol())*20;
			else
				delay = ((MainActivity.ROWCOUNT-toRowCol.getRow())+toRowCol.getCol())*20;
			
			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );

			if (orient == Orientation.NORMAL)
				startPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight+game.getWorld().mScreenBlockLength, 0f );
			else
				startPos = convertPxToGl( pixCoord.getX(), -game.getWorld().mScreenBlockLength, 0f );
			
			// start off the board! Immediately!
			setPositionByGl( startPos[X], startPos[Y], 0f );
			
			states = Animation.queueGrowSpawn(startPos, endPos, delay);
			
		}
		
		else if ( anim == Animation.GROWFLIPSPAWN )
		{
			if (delay == null)
			{
				if (orient == Orientation.NORMAL)
					delay = (toRowCol.getRow()+toRowCol.getCol())*20;
				else
					delay = ((MainActivity.ROWCOUNT-toRowCol.getRow())+toRowCol.getCol())*20;
			}
			
			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );

			if (orient == Orientation.NORMAL)
				startPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight+game.getWorld().mScreenBlockLength, 0f );
			else
				startPos = convertPxToGl( pixCoord.getX(), -game.getWorld().mScreenBlockLength, 0f );
			
			// start off the board! Immediately!
			setPositionByGl( startPos[X], startPos[Y], 0f );
			
			states = Animation.queueGrowFlipSpawn(startPos, endPos, duration, delay);
			
		}
		
		else if ( anim == Animation.FIREWORKSPAWN )
		{

			if (orient == Orientation.NORMAL)
				delay = (toRowCol.getRow()+toRowCol.getCol())*40;
			else
				delay = ((MainActivity.ROWCOUNT-toRowCol.getRow())+toRowCol.getCol())*40;
			
			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
	
			if (orient == Orientation.NORMAL)
				startPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight+game.getWorld().mScreenBlockLength, 0f );
			else
				startPos = convertPxToGl( pixCoord.getX(), -game.getWorld().mScreenBlockLength, 0f );
	
			middlePos1 = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight/2f, 0f );
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
	
			states = Animation.queueFireworksSpawn(startPos, middlePos1, endPos, delay);
			
		}
		
		else if ( anim == Animation.ROWONDECKSPAWN)
		{
			// without flipping
			
			float[] planeDim = game.getWorld().getPlaneDimensions( -game.getWorld().mBoards.mBoardDepth );
			
			pixCoord = convertRCToPx(fromRowCol.getRow(), fromRowCol.getCol());
			startPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			
			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
		
			delay = (int) (Math.abs(startPos[X]/planeDim[X])*200);
			

			// start off the board! Immediately!
			setPositionByGl( startPos[X], startPos[Y], 0f );
			
			states = Animation.queueRowOnDeckSpawn(startPos, endPos, delay);
			
		}
		
		else if ( anim == Animation.ROWONDECKRELOCATE)
		{			

			// with flipping
			
			if (orient == Orientation.NORMAL)
				startPos = convertPxToGl(convertRCToPx(MainActivity.ROWCOUNT, fromRowCol.getCol()), true );
			else
				startPos = convertPxToGl(convertRCToPx(-1, fromRowCol.getCol()), true);

			if (orient == Orientation.NORMAL)
			{
				middlePos1 = convertPxToGl(convertRCToPx(MainActivity.ROWCOUNT+1, fromRowCol.getCol()), true);
				middlePos2 = convertPxToGl(convertRCToPx(-2, fromRowCol.getCol()), true);
			}
			else
			{
				middlePos1 = convertPxToGl(convertRCToPx(-2, fromRowCol.getCol()), true);
				middlePos2 = convertPxToGl(convertRCToPx(MainActivity.ROWCOUNT+1, fromRowCol.getCol()), true);
			}

			
			if (orient == Orientation.NORMAL)
				endPos = convertPxToGl(convertRCToPx(-1, fromRowCol.getCol()), true);
			else
				endPos = convertPxToGl(convertRCToPx(MainActivity.ROWCOUNT, fromRowCol.getCol()), true);


			float[] planeDim = game.getWorld().getPlaneDimensions( -game.getWorld().mBoards.mBoardDepth );
			delay = (int) (Math.abs(startPos[X]/planeDim[X])*200);
			
			states = Animation.queueRowOnDeckRelocate(startPos, middlePos1, middlePos2, endPos, delay);
			
		}
		else if (anim == Animation.MATCH)
		{
			// duration = stageDuration, there are 4 stages
			states = Animation.queueMatch(mCurrentPoints, delay, duration);
			
			// DONT kill the block here! let the grid object get deleted upon settle, then set the delete flag 
			//mTimeOfDeath = System.currentTimeMillis() + delay + duration*5;
		}
		else if ( anim == Animation.MELTWRAPPER )
		{
			states = Animation.queueMeltWapper(mCurrentPoints, duration, delay);
			mTimeOfDeath = System.currentTimeMillis() + delay + duration*2;
		}
		else if ( anim == Animation.MELTMEMBERS )
		{
			states = Animation.queueMeltMembers(mCurrentPoints, duration, delay);
			//game.getWorld().mBoards.lengthenEndOfGameTimer(duration); ???????????????
		}
		else if (anim == Animation.WOBBLE)
		{
			// cancel previous wobble
			cancelAnimations( queueElements.get(Animation.WOBBLE) );
			
			states = Animation.queueWobble(mCurrentPoints);
			
		}
		else if (anim == Animation.DESTROY)
		{
			// duration = stageDuration, there are 4 stages
			//delay = (duration/6) * ( fromRowCol.getRow() + fromRowCol.getCol() + 1 ) ;
			delay = (duration/6) * ( (MainActivity.ROWCOUNT - fromRowCol.getRow()) + fromRowCol.getCol() + 1 ) ;
			states = Animation.queueDestroy(mCurrentPoints, delay, duration);
			mTimeOfDeath = System.currentTimeMillis() + duration*8; 
		}
		
		
		
		
		
		// Bonus Blocks
		
		else if (anim == Animation.BONUS_BOMB_SPIN_DESTROY)
		{
			states = Animation.queueBonusBombSpinDestroy(mCurrentPoints, duration, delay);
			//mTimeOfDeath = System.currentTimeMillis() + duration + delay; 
		}
		
		else if (anim == Animation.BONUS_WAVE_EPICENTER)
		{
			
			float[] planeDim = game.getWorld().getPlaneDimensions( -game.getWorld().mBoards.mBoardDepth );
			
			// epicenter reference
			pixCoord = convertRCToPx(fromRowCol.getRow(), fromRowCol.getCol());
			startPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			
			// current block position
			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
		
			delay = (int) ( Math.sqrt( Math.pow( Math.abs(startPos[X] - endPos[X]) , 2)  +  Math.pow( Math.abs(startPos[Y] - endPos[Y]) , 2) ) * delay);
			
			states = Animation.queueBonusWaveEpicenter( mCurrentPoints, duration, delay );
			
		}
		else if (anim == Animation.BONUS_WAVE_EPICENTER_DESTROY)
		{
			
			float[] planeDim = game.getWorld().getPlaneDimensions( -game.getWorld().mBoards.mBoardDepth );
			
			// epicenter reference
			pixCoord = convertRCToPx(fromRowCol.getRow(), fromRowCol.getCol());
			startPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			
			// current block position
			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			delay = (int) ( Math.sqrt( Math.pow( Math.abs(startPos[X] - endPos[X]) , 2)  +  Math.pow( Math.abs(startPos[Y] - endPos[Y]) , 2) ) * delay);
			
			/*
			//     |
			//  1  |  2
			//     |
			// ----X-----
			//     |
			//  3  |  4
			//     |
			
			
			// 2
			if (toRowCol.getRow() >= fromRowCol.getRow() && toRowCol.getCol() >= fromRowCol.getCol())
			{
				pixCoord = convertRCToPx(StackAttack.ROWCOUNT+2, StackAttack.COLCOUNT+2);
			}
			// 4
			else if (toRowCol.getRow() <= fromRowCol.getRow() && toRowCol.getCol() >= fromRowCol.getCol())
			{
				pixCoord = convertRCToPx(-2, StackAttack.COLCOUNT+2);
			}
			// 1
			else if (toRowCol.getRow() >= fromRowCol.getRow() && toRowCol.getCol() <= fromRowCol.getCol())
			{
				pixCoord = convertRCToPx(StackAttack.ROWCOUNT+2, -2);
			}
			// 3
			else if (toRowCol.getRow() <= fromRowCol.getRow() && toRowCol.getCol() <= fromRowCol.getCol())
			{
				pixCoord = convertRCToPx(-2,-2);
			}			
			endPos = convertPxToGl( pixCoord.getX(), game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			*/
			
			states = Animation.queueBonusWaveEpicenterDestroy( mCurrentPoints/*, endPos*/, duration, delay );
			mTimeOfDeath = System.currentTimeMillis() + duration*7; 
			
		}
		
		
		
		
		
		
		
		
		
		// 3D Text
		
		else if ( anim == Animation.FLIPUPSPAWN )
		{

			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueFlipUpSpawn( delay, duration );
			
		}
		
		else if ( anim == Animation.FLIPOVERSPAWN )
		{

			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueFlipOverSpawn( delay, duration );
			
		}
		
		else if ( anim == Animation.SQUISHROCKFLIPOVERSPAWN )
		{

			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueSquishRockFlipOverSpawn( delay, duration );
			
		}
		
		else if ( anim == Animation.SQUISHROCKFLIPOVERCENTERSPAWN)
		{

			startPos  = convertPxToGl( game.getWorld().mScreenWidth/2, game.getWorld().mScreenHeight/2, 0f );
			endPos  = convertPxToGl( game.getWorld().mScreenWidth*0.65f , game.getWorld().mScreenHeight/2, 0f );
			
			// start center board immediately!
			setPositionByGl( startPos[X], startPos[Y], 0f );
			// start with hidden in plane site
			setRotation(0,-90,0);
			
			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueSquishRockFlipOverCenterSpawn( mCurrentPoints, endPos, delay, duration );
			
		}
		
		else if ( anim == Animation.ROCKFLIPOUT )
		{

			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueRockFlipOut( mCurrentPoints, delay, duration );
			
		}
		
		
		else if ( anim == Animation.SPINDEFORM )
		{

			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueSpinDeform( mCurrentPoints, delay, duration );
			
		}
		
		else if ( anim == Animation.TILTFLOATUP )
		{

			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			float z = World.mMaxDepth;
			
			states = Animation.queueTiltFloatup( mCurrentPoints, z, delay, duration );
			
		}	
		
		else if ( anim == Animation.TILTBUBBLEUP )
		{

			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			float z = World.mMaxDepth;
			
			states = Animation.queueTiltBubbleup( mCurrentPoints, z, delay, duration );
			
		}	
		
		else if (anim == Animation.GAMEOVERFOLLOWBOARDROTATION)
		{
			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = DropSection.ENDGAME_DURATION;
			
			states = Animation.queueGameOverFollowBoardRotation( mCurrentPoints, delay, duration);
		}
		
		else if (anim == Animation.TEXTWOBBLE)
		{
			cancelAnimations( queueElements.get(Animation.TEXTWOBBLE) );
			
			states = Animation.queueTextWobble( mCurrentPoints );
		}		
		
		else if ( anim == Animation.SQUISHROCKFLIPIN )
		{

			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			startPos = convertPxToGl( game.getWorld().mScreenWidth/2 , game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			
			endPos = convertPxToGl( pixCoord.getX() , game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			
			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueSquishRockFlipIn(startPos, endPos, delay, duration);
			
		}
		
		else if ( anim == Animation.SQUISHROCKFLIPOUT )
		{

			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( game.getWorld().mScreenWidth/2 , game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			
			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueSquishRockFlipOut(mCurrentPoints, endPos, delay, duration);
			
		}
		
		else if ( anim == Animation.SQUISHROCKFLIPTHREW )
		{

			pixCoord = convertRCToPx(toRowCol.getRow(), toRowCol.getCol());
			endPos = convertPxToGl( game.getWorld().mScreenWidth/2 , game.getWorld().mScreenHeight-pixCoord.getY(), 0f );
			
			if (delay == null)
				delay = 0;
			
			if (duration == null)
				duration = 600;
			
			states = Animation.queueSquishRockFlipThrew(mCurrentPoints, endPos, delay, duration);
			
		}
		
		
		
		// Apply Animation...
		
		return queueAnimation(anim, states);
		
	}
	
	SparseArray<String> elements;
	private Boolean queueAnimation(Animation anim, TransformState[] states)
	{
		
		// was there a valid animation selection?
		if (states != null)
		{
			// init
			int index = -1;
			Boolean ret = states.length > 0;
			
			// was there at least one new animation?
			if (ret)
			{
				// get current history of animation locations
				elements = queueElements.get(anim);
				
				// clear history
				if (elements != null)
					elements.clear();
				else
					elements = new SparseArray<String>();
				
				// for all animations...
				for (int idx=0; idx<states.length; idx++)
				{
					// add new animation to company front
					index = add( states[idx] );
					
					// keep history if valid
					if (index == -1)
						ret = false;
					else
						elements.put(index, states[idx].getId() );
				}
				
				// replace history
				queueElements.put(anim, elements);
				
			}
			
			// return animation addition attempt
			return ret;
		}
		
		// never selected an animation to attempt
		return false;
		
		
	}
	
	
	
	
	
	
	// TEMP TEMP TEMP
	/*
	public TransformState getCompanyFrontAnimation(Animation anim)
	{
		

		try
		{
			for (Map.Entry<Integer, String> entry : queueElements.get(anim).entrySet()) 
			{
			    index 	= entry.getKey();
			    id 		= entry.getValue();
		        
				// kill currently running animation
				if (index >= 0 && index < QUEUESIZE)
				{
					
					if (mCompanyFront[index] != null)
					{
						if (mCompanyFront[index].getId() == id && mCompanyFront[index].isCompleted == false)
						{
							return mCompanyFront[index];
						}
					}
					
				}
			}
		
		
		}
		catch(Exception e)
		{
			//pass
		}

		
		return null;
		
	}
	*/
	
	
	public Boolean cancelAnimation(Animation anim)
	{
		//game.text += "CANCEL ANIMATION:"+anim+"...";
		//game.textviewHandler.post( game.updateTextView );
		
		if (queueElements.containsKey(anim))
		{
			cancelAnimations( queueElements.get(anim) );
			
			//game.text += "True! \n";
			//game.textviewHandler.post( game.updateTextView );
			
			return true;
		}
		
		//game.text += "FALSE! \n";
		//game.textviewHandler.post( game.updateTextView );
		
		return false;
	}
	
	
	
	private float[] curPos;
	private Integer index;
	private String  id;

	private void cancelAnimations(SparseArray<String> elements)
	{
		cancelAnimations(elements, true);
	}
	
	private void cancelAnimations(SparseArray<String> elements, Boolean toEnd)
	{

		
		// dont process nothing!
		if (elements == null)
			return;
		
		for(int i = 0; i < elements.size(); i++) 
		{
			index = elements.keyAt(i);
			id = elements.get(index);
	        
			// kill currently running animation
			if (index >= 0 && index < QUEUESIZE)
			{
				
				try
				{
				
					if (mCompanyFront[index] != null)
					{
						if (mCompanyFront[index].getId() == id && mCompanyFront[index].isCompleted == false)
						{

							
							if (toEnd == false)
							{
								// the previous swap is still animating, cancel it
								mCompanyFront[index].stop();
							}
							
							// set position / rotation / size
							else if (toEnd == true)
							{
								// the previous swap is still animating, cancel it
								curPos = mCompanyFront[index].mEndPoint.clone();
								mCompanyFront[index].stop();
								
								if (mCompanyFront[index].mTransform == TransformType.TRANSLATE)
								{
									synchronized(mCurrentPoints[POS])
									{
										if (mCompanyFront[index].mDirEnable[X])
											mCurrentPoints[POS][X] = curPos[X];
										if (mCompanyFront[index].mDirEnable[Y])
											mCurrentPoints[POS][Y] = curPos[Y];
										if (mCompanyFront[index].mDirEnable[Z])
											mCurrentPoints[POS][Z] = curPos[Z];
									}
								}
								else if (mCompanyFront[index].mTransform == TransformType.ROTATE)
								{
									
									if (mCompanyFront[index].mTimeDomain == TimeDomain.FINITE)
									{
										synchronized(mCurrentPoints[ROT])
										{
											if (mCompanyFront[index].mDirEnable[X])
												mCurrentPoints[ROT][X] = curPos[X];
											if (mCompanyFront[index].mDirEnable[Y])
												mCurrentPoints[ROT][Y] = curPos[Y];
											if (mCompanyFront[index].mDirEnable[Z])
												mCurrentPoints[ROT][Z] = curPos[Z];
										}
									}
									else
									{
										// graceful!
										queueAnimation(Animation.RESTORESTEADYSTATE, Animation.queueRestoreSteadyState(mCurrentPoints));
									}
								}
								else if (mCompanyFront[index].mTransform == TransformType.SCALE)
								{
									if (mCompanyFront[index].mTimeDomain == TimeDomain.FINITE)
									{
										// should this be commented out?
										synchronized(mCurrentPoints[SIZ])
										{
											if (mCompanyFront[index].mDirEnable[X])
												mCurrentPoints[SIZ][X] = curPos[X];
											if (mCompanyFront[index].mDirEnable[Y])
												mCurrentPoints[SIZ][Y] = curPos[Y];
											if (mCompanyFront[index].mDirEnable[Z])
												mCurrentPoints[SIZ][Z] = curPos[Z];
										}
									}
									else
									{
										// graceful!
										queueAnimation(Animation.RESTORESTEADYSTATE, Animation.queueRestoreSteadyState(mCurrentPoints));
									}
	
									
								}
							}
						}
					}
				
				
				}
				catch(Exception e)
				{
					//pass
				}
			}
	    }
	}
	
	
	
	
	
}
