package com.wagoodman.stackattack;


import com.wagoodman.stackattack.Coord;
import com.wagoodman.stackattack.Orientation;
import com.wagoodman.stackattack.World;



public enum Animation
{
	RESTORESTEADYSTATE,	// get back to a typical sized/rotated block 
	
	// Block Animations
	
	PICKUP,				// when a user places a finger on an interactable block
	DROP,				// when a user lifts a finger on a picked up block
	SWAPTO,				// used to swap the position of two blocks (when user drags finger)
	SETTLETO,			// used to implement gravity on the board
	DROPSPAWN,			// when a block spawns onto the board from the top
	GROWSPAWN,			// when a block spawns onto the board from the bottom
	GROWFLIPSPAWN,		// when a block spawns onto the board from the bottom while turning over and up (corkscrew)
	FIREWORKSPAWN,		// NOT USED
	ROWONDECKRELOCATE,	// used upon flipping of the board
	ROWONDECKSPAWN,		// used for spawning ROD
	MATCH,				// used when a match is found on the board
	MELTMEMBERS,		// used to inflate group members upon a matching next to a group
	MELTWRAPPER,		// used to deflate the wrapper containing a group
	WOBBLE,				// when blocks hit the top of the board
	DESTROY,
	
	// Bonus Animations
	
	BONUS_BOMB_SPIN_DESTROY,
	BONUS_WAVE_EPICENTER,
	BONUS_WAVE_EPICENTER_DESTROY,
	
	// 3D Text
	
	FLIPUPSPAWN,		// when 3d text flips into place (sit up out from the grave)
	FLIPOVERSPAWN,		// when 3d text flips into place (page flip)
	SQUISHROCKFLIPOVERSPAWN,	// same as flipoverspawn, but with spring
	SQUISHROCKFLIPOVERCENTERSPAWN,
	ROCKFLIPOUT,		// same as flipoverspawn, but with falling back as an outro (and no spring)
	SPINDEFORM,
	TILTFLOATUP,
	TILTBUBBLEUP,
	GAMEOVERFOLLOWBOARDROTATION,
	TEXTWOBBLE,
	SQUISHROCKFLIPOUT,
	SQUISHROCKFLIPTHREW,
	SQUISHROCKFLIPIN,
	;			// end of game / match blocks

	
	
	
	// array keys
	private static final int POS	= 0;
	private static final int ROT	= 1;
	private static final int SIZ	= 2;
	private static final int X		= 0;
	private static final int Y		= 1;
	private static final int Z		= 2;

	
	
	public static TransformState[] queueRestoreSteadyState( float[][] currentPoints )
	{
	
		
		
		return new TransformState[] 
		{
			new TransformState(
				TransformType.ROTATE, 												// Translate
				new Boolean[] {true, true, true}, 									// Z Dir only
				TimeDomain.FINITE, 													// Timed Animation
				new MotionEquation[] {
						MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
						MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
						MotionEquation.SPRING_UNDAMP_ALT_K4_5
				},
				currentPoints[ROT].clone(),								// start point
				new float[] {
						0,0,0
							}, 
				new int[] {100,200,300}, 									// duration: 400 ms
				new int[] {0,0,0}											// no trigger delay
			) // end TransformState 
	
			,
				
			 new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {true, true, true}, 						// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
						MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
						MotionEquation.SPRING_UNDAMP_ALT_K4_5
						}, 	
				currentPoints[SIZ].clone(),							// start point: current position
				new float[] {
						1.0f,					 						// end X 
						1.0f, 											// end Y 
						1.0f											// end Z 
							}, 
				new int[] {100,200,300}, 								// duration: 700 ms
				new int[] {0,0,0}										// no trigger delay
			) // end TransformState 

		};
		
		
		
		
	}
	
	
	
	
	public static TransformState[] queuePickup( float[][] currentPoints )
	{

		return new TransformState[] 
		{
	
			new TransformState(
				TransformType.TRANSLATE, 												// Translate
				new Boolean[] {false, false, true}, 									// Z Dir only
				TimeDomain.FINITE, 														// Timed Animation
				new MotionEquation[] {
						null, 
						null,
						MotionEquation.SPRING_UNDAMP_ALT_K9_5
						//MotionEquation.SIN
				},
				currentPoints[POS].clone(),								// start point: current position
				new float[] {
						currentPoints[POS][X], 							// end X = start X
						currentPoints[POS][Y], 							// end Y = start Y
						0 + World.mDepthUnit*2		// default board depth + 2 depth units
							}, 
				new int[] {1,1,400}, 										// duration: 400 ms
				new int[] {0,0,0}											// no trigger delay
			) // end TransformState 

			,
			
			new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {true, true, false}, 						// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						MotionEquation.SPRING_UNDAMP_ALT_K9_5,
						MotionEquation.SPRING_UNDAMP_ALT_K9_5,
						MotionEquation.SPRING_UNDAMP_ALT_K9_5
						//MotionEquation.SIN, 
						//MotionEquation.SIN, 
						//MotionEquation.SIN, 
						}, 	
				currentPoints[SIZ].clone(),							// start point: current position
				new float[] {
						2f,					 						// end X 
						2f, 										// end Y 
						0.5f										// end Z 
							}, 
				new int[] {400,400,400},								// duration: 400 ms
				new int[] {0,0,0}										// no trigger delay
			) // end TransformState 
			
			
			,
			
			new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {false, true, false}, 					// direction
					TimeDomain.CONTINUOUS, 								// Timed Animation
					new MotionEquation[] {
							MotionEquation.SIN, 			
							MotionEquation.SIN,
							MotionEquation.SIN
							}, 	
					currentPoints[ROT].clone(),							// start point: current position
					new double[] {
							20, 			// rate X 
							20, 			// rate Y 
							20				// rate Z
								}, 
					new double[] {
							400, 			// unit X 
							400, 			// unit Y 
							400				// unit Z
								}, 
					new int[] {0, 0, 0}		// use given delay
				) // end TransformState 
			
		};
			 
	}


	public static TransformState[] queueDrop( float[][] currentPoints  )
	{
		
		return new TransformState[] 
		{
			new TransformState(
				TransformType.TRANSLATE, 												// Translate
				new Boolean[] {false, false, true}, 									// Z Dir only
				TimeDomain.FINITE, 														// Timed Animation
				new MotionEquation[] {
						null, 
						null, 
						/*MotionEquation.SIN_OVERSHOOT_SLOW,*/ 			// Z Bounce EQ
						//MotionEquation.LINEAR
						MotionEquation.SPRING_UNDAMP_ALT_K9_5
				},
				currentPoints[POS].clone(),								// start point: current position
				new float[] {
						currentPoints[POS][X], 							// end X = start X
						currentPoints[POS][Y], 							// end Y = start Y
						//mCurrentPoints[POS][Z] - World.mDepthUnit*3	// end Z = start Z - 3 depth unit
						0												// default board depth
							}, 
				new int[] {1,1,400}, 										// duration: 400 ms
				new int[] {0,0,0}											// no trigger delay
			) // end TransformState 
	
			,
				
			 new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {true, true, true}, 						// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						MotionEquation.SPRING_UNDAMP_P4, 
						MotionEquation.SPRING_UNDAMP_P4, 
						MotionEquation.SPRING_UNDAMP_P4
						}, 	
				currentPoints[SIZ].clone(),							// start point: current position
				new float[] {
						1.0f,					 						// end X 
						1.0f, 											// end Y 
						1.0f											// end Z 
							}, 
				new int[] {300,700,900}, 								// duration: 700 ms
				new int[] {0,0,0}										// no trigger delay
			) // end TransformState 

		};


	}
	
	
	

	public static TransformState[] queueSwapTo(float[][] currentPoints, Coord<Integer> fromRowCol, Coord<Integer> toRowCol, float[] startPos, float[] endPos, Integer duration, Orientation orient)
	{

		
		// determine how to move to the new coordinate
		int defaultDuration;
		if (duration == null)
			defaultDuration = 400;
		else
			defaultDuration = duration;
		
		int xDur = defaultDuration;		// total animation duration in x direction
		int yDur = defaultDuration;		// total animation duration in y direction
		int xDelay = 0;					// ms to wait before starting animation in x direction
		int yDelay = 0;					// ms to wait before starting animation in y direction
		
		int deltaRowCount = Math.abs(fromRowCol.getRow() - toRowCol.getRow());
		int deltaColCount = 1;	// always swapping on single column change
		
		MotionEquation yEq = MotionEquation.SPRING_UNDAMP_ALT_K4_5;
		MotionEquation xEq = MotionEquation.SPRING_UNDAMP_ALT_K4_5;
		
		// cross-row swap; swap around a corner
		if (fromRowCol.getRow() != toRowCol.getRow())
		{
			
			xDur	= (defaultDuration/(deltaRowCount + deltaColCount))*deltaColCount;
			yDur 	= (defaultDuration/(deltaRowCount + deltaColCount))*deltaRowCount; 
			
			xEq = MotionEquation.LOGISTIC;
			
			if (
					fromRowCol.getRow() > toRowCol.getRow() && orient == Orientation.NORMAL  ||
					fromRowCol.getRow() < toRowCol.getRow() && orient == Orientation.REVERSE  
				)
			{
				// move up some rows first, then over (overlap by a half block)
				xDelay	= (int) ((defaultDuration/(deltaRowCount + deltaColCount))*(deltaRowCount-0.5));
				yEq = MotionEquation.SIN;
			}
			else
			{
				// move over first, then move down some rows  (overlap by a half block)
				yDelay	= (int) ((defaultDuration/(deltaRowCount + deltaColCount))*(deltaColCount-0.5));
				yEq = MotionEquation.SPRING_UNDAMP_ALT_K4_5;
			}
			
		} // end else
		
		
		
		return new TransformState[] 
		{ 
			new TransformState(
				TransformType.TRANSLATE, 							// Translate
				new Boolean[] {true, true, false}, 					// X & Y dir
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						xEq, 			// X & Y Bounce EQ
						yEq, 			// X & Y Bounce EQ 
						//MotionEquation.LINEAR,	//debug
						//MotionEquation.LINEAR,	//debug
						null
						}, 	
				startPos,								// start point: current position or end of current running animation
				new float[] {
						endPos[X], 						// end X = start X + offset
						endPos[Y], 						// end Y = start Y + offset
						currentPoints[POS][Z]			// end Z = start Z
							}, 
				new int[] {xDur		, yDur,		1}, 	// duration (ms)
				new int[] {xDelay	, yDelay,	0}		// no trigger delay
			) // end TransformState 
		};
		
	}
	

	
	
	
	

	
	public static TransformState[] queueSettleTo(float[][] currentPoints, float[] startPos, float[] endPos, int delay)
	{
		
		return new TransformState[] 
		{
			new TransformState(
				TransformType.TRANSLATE, 							// Translate
				new Boolean[] {false, true, false}, 				// Y dir
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						null, 			
						MotionEquation.SPRING_UNDAMP_ALT_K4_5,
						//MotionEquation.LINEAR,
						null
						}, 	
				startPos.clone(),						// start point: current position or end of current running animation
				new float[] {
						endPos[X], 						// end X = start X + offset
						endPos[Y], 						// end Y = start Y + offset
						currentPoints[POS][Z]			// end Z = start Z
							},
				
							// 600
				new int[] {1, 600, 1}, 				// duration (ms)
				new int[] {0, delay,    0}			// use given delay
				
				/*
				new int[] {1, 500, 1}, 				// duration (ms)
				new int[] {0, 0,    0}			// use given delay
				*/
			) // end TransformState 
		};
		
	}

	

	
	

	// drops from top of board to given position
	
	public static TransformState[] queueDropSpawn( float[] startPos, float[] endPos, int duration, int delay )
	{			
		
		return new TransformState[] 
		{
			new TransformState(
				TransformType.TRANSLATE, 							// Translate
				new Boolean[] {false, true, false}, 				// Y dir
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						null, 			
						MotionEquation.LINEAR,
						null
						}, 	
				startPos.clone(),						// start point: current position or end of current running animation
				new float[] {
						endPos[X], 						// end X 
						endPos[Y], 						// end Y 
						0								// end Z = board level
							}, 
				new int[] {1, duration,  1}, 	// duration (ms)
				new int[] {0, delay, 0}		// use given delay
			) // end TransformState 
		};
		
		/*

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, true}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.SPRING_UNDAMP_ALT_K4_5, 			
						MotionEquation.SPRING_UNDAMP_ALT_K4_5,
						MotionEquation.SPRING_UNDAMP_ALT_K4_5
						}, 	
				new float[] {
						45, 					// start X 
						45, 					// start Y 
						45						// start Z
							}, 
				new float[] {
						0, 						// end X 
						0, 						// end Y 
						0						// end Z
							}, 
				new int[] {500, 500, 500}, 	// duration (ms)
				new int[] {0, 100, 200}		// use given delay
			) // end TransformState 
		*/

	}
	
	
	
	
	// grows from bottom of board to given position

	public static TransformState[] queueGrowSpawn( float[] startPos, float[] endPos, int delay )
	{	
		return new TransformState[] 
		{
			new TransformState(
				TransformType.TRANSLATE, 							// Translate
				new Boolean[] {false, true, false}, 				// Y dir
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						null, 			
						MotionEquation.LOGISTIC,
						null
						}, 	
				startPos.clone(),						// start point: current position or end of current running animation
				new float[] {
						endPos[X], 						// end X 
						endPos[Y], 						// end Y 
						0								// end Z = board level
							}, 
				new int[] {1, 600,  1}, 	// duration (ms)
				new int[] {0, delay, 0}		// use given delay
			) // end TransformState 
		};
			
		/*
		TransformState spawn_rotate = 
				new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {true, true, true}, 					// direction
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.SPRING_UNDAMP_ALT_K4_5, 			
							MotionEquation.SPRING_UNDAMP_ALT_K4_5,
							MotionEquation.SPRING_UNDAMP_ALT_K4_5
							}, 	
					new float[] {
							45, 					// start X 
							45, 					// start Y 
							45						// start Z
								}, 
					new float[] {
							0, 						// end X 
							0, 						// end Y 
							0						// end Z
								}, 
					new int[] {500, 500, 500}, 	// duration (ms)
					new int[] {0, 100, 200}		// use given delay
				); // end TransformState 
		*/

	}
	
	
	
	
	// grows from bottom of board to given position

	public static TransformState[] queueGrowFlipSpawn( float[] startPos, float[] endPos, int duration, int delay )
	{	
		return new TransformState[] 
		{
			new TransformState(
				TransformType.TRANSLATE, 							// Translate
				new Boolean[] {false, true, false}, 				// Y dir
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						null, 			
						MotionEquation.LOGISTIC,
						null
						}, 	
				startPos.clone(),						// start point: current position or end of current running animation
				new float[] {
						endPos[X], 						// end X 
						endPos[Y], 						// end Y 
						0								// end Z = board level
							}, 
				new int[] {1, duration,  1}, 	// duration (ms)
				new int[] {0, delay, 0}		// use given delay
			) // end TransformState 
			
			,
			
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {true, true, false}, 				// Y dir
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,			
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC,
							}, 	
					new float[] {-90,-90,0}, 		// start point: current position or end of current running animation
					new float[] {0,0,0}, 		// end point
					new int[] {600, 600,  600}, 	// duration (ms)
					new int[] {delay, delay, delay}		// use given delay
				) // end TransformState 
			
		};
			

	}
	
	
	

	
	
	// drops from top of board to given position

	public static TransformState[] queueFireworksSpawn(float[] startPos, float[] middlePos, float[] endPos, int delay)
	{
		
		int duration = 600;
		
		
		return new TransformState[] 
		{
			new TransformState(
				TransformType.TRANSLATE, 							// Translate
				new Boolean[] {false, true, false}, 				// Y dir
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						null, 			
						MotionEquation.LOGISTIC,
						null
						}, 	
				startPos.clone(),						// start point: current position or end of current running animation
				new float[] {
						middlePos[X], 					// end X 
						middlePos[Y], 					// end Y 
						0								// end Z = board level
							}, 
				new int[] {1, duration+delay,   1}, 	// duration (ms)
				new int[] {0, delay, 0}		// use given delay
			) // end TransformState 
	
			,

			new TransformState(
				TransformType.TRANSLATE, 					// Translate
				new Boolean[] {false, true, false}, 		// Y dir
				TimeDomain.FINITE, 							// Timed Animation
				new MotionEquation[] {
						null, 			
						MotionEquation.LOGISTIC,
						null
						}, 	
				new float[] {
						middlePos[X], 					// start X 
						middlePos[Y], 					// start Y 
						0								// start Z = board level
							}, 
				new float[] {
						endPos[X], 						// end X 
						endPos[Y], 						// end Y 
						0								// end Z = board level
							}, 
				new int[] {1, duration,   	  1}, 			// duration (ms)
				new int[] {0, delay+duration, 0}		// use given delay
			) // end TransformState 
		
		};
		
		/*
		TransformState spawn_rotate = 
				new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {true, true, true}, 					// direction
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.SPRING_UNDAMP_ALT_K4_5, 			
							MotionEquation.SPRING_UNDAMP_ALT_K4_5,
							MotionEquation.SPRING_UNDAMP_ALT_K4_5
							}, 	
					new float[] {
							45, 					// start X 
							45, 					// start Y 
							45						// start Z
								}, 
					new float[] {
							0, 						// end X 
							0, 						// end Y 
							0						// end Z
								}, 
					new int[] {500, 500, 500}, 	// duration (ms)
					new int[] {0, 100, 200}		// use given delay
				); // end TransformState 
		*/

	}
	
	
	
	
	// drops from top of board to given position

	public static TransformState[] queueRowOnDeckRelocate(float[] startPos, float[] middlePos1, float[] middlePos2, float[] endPos, int delay)
	{
		
		int duration = 300;
		
		
		return new TransformState[] 
		{
			new TransformState(
				TransformType.TRANSLATE, 				// Translate
				new Boolean[] {false, true, false}, 	// Y dir
				TimeDomain.FINITE, 						// Timed Animation
				new MotionEquation[] {
						null,			
						MotionEquation.LOGISTIC,
						null
						}, 	
				startPos.clone(),						
				middlePos1.clone(),
				new int[] {1, duration,   1}, 	// duration (ms)
				new int[] {0, delay, 0}				// use given delay
			) // end TransformState 
	
			,
			
			new TransformState(
					TransformType.TRANSLATE, 							// Translate
					new Boolean[] {true, true, false}, 				// Y dir
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR,		
							MotionEquation.LINEAR,
							null
							}, 	
					middlePos1.clone(),
					new float[] {
							middlePos1[X]+5, 					// end X 
							middlePos1[Y], 					// end Y 
							0								// end Z = board level
								}, 
					new int[] {1, 1, 0}, 	// duration (ms)
					new int[] {delay+duration+1, delay+duration+1, 0}		// use given delay
				) // end TransformState 

			,

			new TransformState(
					TransformType.TRANSLATE, 							// Translate
					new Boolean[] {true, true, false}, 				// Y dir
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR,			
							MotionEquation.LINEAR,
							null
							}, 	
					new float[] {
							middlePos1[X]+5, 					// start X 
							middlePos1[Y], 					// start Y 
							0								// start Z = board level
								}, 
					new float[] {
							middlePos2[X]+5, 					// end X 
							middlePos2[Y], 					// end Y 
							0								// end Z = board level
								}, 
					new int[] {1, 1, 0}, 	// duration (ms)
					new int[] {delay+duration+2, delay+duration+2, 0}		// use given delay
				) // end TransformState 
			
			,

			new TransformState(
					TransformType.TRANSLATE, 							// Translate
					new Boolean[] {true, true, false}, 				// Y dir
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR,		
							MotionEquation.LINEAR,
							null
							}, 	
					new float[] {
							middlePos2[X]+5, 					// start X 
							middlePos2[Y], 					// start Y 
							0								// start Z = board level
								}, 
					new float[] {
							middlePos2[X], 					// end X 
							middlePos2[Y], 					// end Y 
							0								// end Z = board level
								}, 
					new int[] {1, 1, 0}, 	// duration (ms)
					new int[] {delay+duration+3, delay+duration+3, 0}		// use given delay
				) // end TransformState 
			
			,
			

			new TransformState(
					TransformType.TRANSLATE, 				// Translate
					new Boolean[] {false, true, false}, 	// Y dir
					TimeDomain.FINITE, 						// Timed Animation
					new MotionEquation[] {
							null,			
							MotionEquation.LOGISTIC,
							null
							}, 	
					middlePos2.clone(),						
					endPos.clone(),
					new int[] {1, duration,   1}, 				// duration (ms)
					new int[] {0, delay+duration+4, 0}		// use given delay
				) // end TransformState 
		
		};


	}
	

	
	
	

	public static TransformState[] queueRowOnDeckSpawn(float[] startPos, float[] endPos, int delay)
	{
		
		int duration = 300;
		
		
		return new TransformState[] 
		{

			new TransformState(
				TransformType.TRANSLATE, 				// Translate
				new Boolean[] {true, true, false}, 		// Y dir
				TimeDomain.FINITE, 						// Timed Animation
				new MotionEquation[] {
						MotionEquation.LOGISTIC,			
						MotionEquation.LOGISTIC,
						null
						}, 	
				new float[] {
						startPos[X], 					// start X 
						startPos[Y], 					// start Y 
						0								// start Z = board level
							}, 
				new float[] {
						endPos[X], 						// end X 
						endPos[Y], 						// end Y 
						0								// end Z = board level
							}, 
				new int[] { duration, duration,   	  1}, 			// duration (ms)
				new int[] { delay+duration+4, delay+duration+4, 0}	// use given delay
			) // end TransformState 
		
		};

	}

	


	
	
	
	
	
	
	
	
	
	
	
	
	public static TransformState[] queueMatch(float[][] currentPoints, int delay, int stageDuration)
	{
		//Flashing: during delay
		//Stage 1: pickup (over all stages)
		//Stage 2: spin left/up 90 deg, crunch X 
		//Stage 3: spin left/up 90 deg, crunch y
		//Stage 4: crunch all, spin all (disappear)
		
		// AL IN ALL: 6 STAGE LENGTH
		
		int flashDelay = 400;
		int numFlashes = 2;
		
		return new TransformState[] 
		{

			
			// STAGE 1
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {false, false, true}, 			// Y dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					currentPoints[POS].clone(),						// start point: current position or end of current running animation
					new float[] {
							currentPoints[POS][X], 							// end X = start X
							currentPoints[POS][Y], 							// end Y = start Y
							//0 + World.mDepthUnit*6		// default board depth + 2 depth units
							0 + World.mDepthUnit*20
							}, 
					new int[] {1, 1, stageDuration*4}, 	// duration (ms)
					new int[] {flashDelay+delay/4, flashDelay+delay/4, flashDelay+delay/4}		// use given delay
				) // end TransformState 
			,
			
			// STAGE 2
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, false, false}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							null,
							null
							}, 	
					currentPoints[SIZ].clone(),							// start point: current position
					new float[] {
							0.2f,					 						// end X 
							1f, 											// end Y 
							1f												// end Z 
								}, 
					new int[] {stageDuration,1,1},	 								// duration: 400 ms
					new int[] {flashDelay+delay,flashDelay+delay,flashDelay+delay}										// no trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, false}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						null
						}, 	
				currentPoints[ROT].clone(),	
				new float[] {
						90, 						// end X 
						90, 						// end Y 
						0						// end Z
							}, 
				new int[] {stageDuration, stageDuration, 1}, 	// duration (ms)
				new int[] {flashDelay+delay,flashDelay+delay,flashDelay+delay}		// use given delay
			) // end TransformState 
			
			,
			
			// STAGE 3
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, true, false}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							null
							}, 	
					new float[] {
							0.2f,					 						// start X 
							1f, 											// start Y 
							1f												// start Z 
								}, 
					new float[] {
							0.2f,					 						// end X 
							0.2f, 											// end Y 
							1f												// end Z 
								}, 
					new int[] {stageDuration,stageDuration,1},	 			// duration (ms)
					new int[] {flashDelay+delay+stageDuration,flashDelay+delay+stageDuration,flashDelay+delay}				// trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, false}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						null
						}, 	
				new float[] {
						90, 					// end X 
						90, 					// end Y 
						0						// end Z
							}, 
				new float[] {
						180, 					// end X 
						180, 					// end Y 
						0						// end Z
							}, 
				new int[] {stageDuration, stageDuration, 1}, 	// duration (ms)
				new int[] {flashDelay+delay+stageDuration, flashDelay+delay+stageDuration, flashDelay+delay}		// use given delay
			) // end TransformState 
			
			,
			
			// STAGE 4
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, true, true}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							}, 	
					new float[] {
							0.2f,					 						// start X 
							0.2f, 											// start Y 
							1f												// start Z 
								}, 
					new float[] {
							0f,					 						// end X 
							0f, 											// end Y 
							0f												// end Z 
								}, 
					new int[] {stageDuration,stageDuration, stageDuration},	 		// duration (ms)
					new int[] { flashDelay+delay+stageDuration*2, flashDelay+delay+stageDuration*2 ,flashDelay+delay+stageDuration*2}	// trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, true}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						MotionEquation.LINEAR, 
						}, 	
				new float[] {
						180, 					// end X 
						180, 					// end Y 
						0						// end Z
							}, 
				new float[] {
						270, 					// end X 
						270, 					// end Y 
						90						// end Z
							}, 
				new int[] {stageDuration, stageDuration, stageDuration}, 	// duration (ms)
				new int[] {flashDelay+delay+stageDuration*2, flashDelay+delay+stageDuration*2, flashDelay+delay+stageDuration*2}		// use given delay
			) // end TransformState 
			
			,
			
			// RESET ROTATION (incase of redestroy?)
			new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {true, true, true}, 					// direction
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 			
							MotionEquation.LINEAR,
							MotionEquation.LINEAR, 
							}, 	
					new float[] {
							0, 					// end X 
							0, 					// end Y 
							0						// end Z
								}, 
					new float[] {
							0, 					// end X 
							0, 					// end Y 
							0						// end Z
								}, 
					new int[] {1, 1, 1}, 	// duration (ms)
					new int[] { flashDelay+delay+stageDuration*3,  flashDelay+delay+stageDuration*3,  flashDelay+delay+stageDuration*3}		// use given delay
				) // end TransformState 
			
		};
	
	}
	
	
	
	
	
	
	
	
	

	
	
	
	
	public static TransformState[] queueMeltWapper( float[][] currentPoints, int duration, int delay  )
	{
		
		return new TransformState[] 
		{
		
			 new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {false, true, false}, 					// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						null,
						MotionEquation.LINEAR, 
						null
						}, 	
				currentPoints[SIZ].clone(),							// start point: current position
				new float[] {
						1.0f,					 						// end X 
						0f, 											// end Y 
						1.0f											// end Z 
							}, 
				new int[] {1,duration,1}, 								// duration: 700 ms
				new int[] {delay, delay, delay}							// no trigger delay
			) // end TransformState 

			
			 ,
			 
			 
			 new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {true, false, true}, 						// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 
						null, 
						MotionEquation.LINEAR
						}, 	
				new float[] {
						1.0f,					 					// start X 
						0f, 										// start Y 
						1.0f										// start Z 
							}, 
				new float[] {
						0f,					 						// end X 
						0f, 										// end Y 
						0f											// end Z 
							}, 
				new int[] {duration/2,duration/2,duration/2}, 								// duration: 700 ms
				new int[] {(int) (delay + duration), (int) (delay + duration), (int) (delay + duration)}		// trigger delay
			) // end TransformState 
			 
		};


	}
	
	
	
	
	
	
	
	
	public static TransformState[] queueMeltMembers( float[][] currentPoints,  int duration, int delay  )
	{
		
		return new TransformState[] 
		{
			// new
			new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {true, true, true}, 					// direction
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC, 			
							MotionEquation.LOGISTIC,
							MotionEquation.SPRING_UNDAMP_ALT_K9_3,
							}, 	
					new float[] {
							-180, 					// end X 
							180, 					// end Y 
							180						// end Z
								}, 
					new float[] {
							0, 					// end X 
							0, 					// end Y 
							0						// end Z
								}, 
					new int[] {(int) (duration*0.6*0.6),(int) (duration*0.8*0.6),(int) (duration*0.6)}, 								// duration: ms
					new int[] {delay, delay, delay}							// no trigger delay
				) // end TransformState 
			,
				
			 new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {true, true, true}, 						// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						MotionEquation.SPRING_UNDAMP_ALT_K9_3, 
						MotionEquation.SPRING_UNDAMP_ALT_K9_3, 
						MotionEquation.SPRING_UNDAMP_ALT_K9_3
						}, 	

				currentPoints[SIZ].clone(),							// start point: current position
				new float[] {
						1.3f,					 						// end X 
						1.3f, 											// end Y 
						1.3f											// end Z 
							}, 
				//new int[] {(int) (duration*0.6),(int) (duration*0.8),(int) (duration)}, 
				new int[] {(int) (duration*0.6*0.6),(int) (duration*0.8*0.6),(int) (duration*0.6)}, 								// duration: ms
				new int[] {delay, delay, delay}							// no trigger delay
			) // end TransformState 
			 
			 
			,
			
			new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {true, true, true}, 						// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						MotionEquation.LOGISTIC, 
						MotionEquation.LOGISTIC, 
						MotionEquation.LOGISTIC
						}, 	
				new float[] {
						1.3f,					 						// end X 
						1.3f, 											// end Y 
						1.3f											// end Z 
							}, 
				new float[] {
						1.0f,					 						// end X 
						1.0f, 											// end Y 
						1.0f											// end Z 
							}, 
				new int[] {(int) (duration*0.6*0.4),(int) (duration*0.8*0.4),(int) (duration*0.4)}, 									// duration: ms
				new int[] {delay+(int) (duration*0.4), delay+(int) (duration*0.4), delay+(int) (duration*0.4)}							// no trigger delay
			) // end TransformState 
			 
		};


	}
	
	
	
	
	
	
	
	public static TransformState[] queueWobble( float[][] currentPoints )
	{

		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {false, true, false}, 					// direction
					TimeDomain.CONTINUOUS, 								// Timed Animation
					new MotionEquation[] {
							null, 			
							MotionEquation.SIN,
							null
							}, 	
					currentPoints[ROT].clone(),							// start point: current position
					new double[] {
							0, 				// rate X 
							5, 			// rate Y 
							0				// rate Z
								}, 
					new double[] {
							0, 				// unit X 
							600, 			// unit Y 
							0				// unit Z
								}, 
					new int[] {0, 0, 0}		// use given delay
				) // end TransformState 
			
			,
			
			new TransformState(
					TransformType.SCALE,	 							// Translate
					new Boolean[] {true, true, false}, 					// direction
					TimeDomain.CONTINUOUS, 								// Timed Animation
					new MotionEquation[] {
							MotionEquation.SIN, 			
							MotionEquation.SIN,
							null
							}, 	
					currentPoints[SIZ].clone(),							// start point: current position
					new double[] {
							-0.1, 				// rate X 
							0.25, 				// rate Y 
							0				// rate Z
								}, 
					new double[] {
							600, 				// unit X 
							600, 			// unit Y 
							0				// unit Z
								}, 
					new int[] {0, 0, 0}		// use given delay
				) // end TransformState 
			
		};
			 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public static TransformState[] queueDestroy(float[][] currentPoints, int delay, int stageDuration)
	{
		//Wave   : Spans over 4 stages, overlaps with 2
		//Stage 1: X
		//Stage 2: spin left/up 90 deg, crunch X 
		//Stage 3: spin left/up 90 deg, crunch y
		//Stage 4: crunch all, spin all (disappear)
		
		// AL IN ALL: 6 STAGE LENGTH
		
		int waveTime = stageDuration*4; // up, down, down, up (sin)
		int waveDepth = 3;
		
		return new TransformState[] 
		{
			// Wave: Pickup
			new TransformState(
					TransformType.TRANSLATE, 				// Translate
					new Boolean[] {false, false, true}, 	// Z dir
					TimeDomain.FINITE, 						// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					currentPoints[POS].clone(),			// start point: current position or end of current running animation
					new float[] {
							currentPoints[POS][X], 		// end X = start X
							currentPoints[POS][Y], 		// end Y = start Y
							0 + World.mDepthUnit*waveDepth		// default board depth + 5 depth units
							}, 
					new int[] {1, 1, stageDuration}, 	// duration (ms)
					new int[] {delay, delay, delay}		// use given delay
				) // end TransformState 
			,
			
			// Wave: Rotate For
			new TransformState(
					TransformType.ROTATE, 				// Translate
					new Boolean[] {true, true, false}, 	// Z dir
					TimeDomain.FINITE, 						// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC,			
							}, 	
					currentPoints[ROT].clone(),			// start point: current position or end of current running animation
					new float[] {
							90, 		
							45, 		
							0 							
							}, 
					new int[] {stageDuration, stageDuration, stageDuration}, 	// duration (ms)
					new int[] {delay, delay, delay}		// use given delay
				) // end TransformState 
			,
			
			// Wave: Drop 1 & 2
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {false, false, true}, 			// Z dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					new float[] {
							currentPoints[POS][X], 		// start X 
							currentPoints[POS][Y], 		// start Y 
							0 + World.mDepthUnit*waveDepth		//
							}, 
					new float[] {
							currentPoints[POS][X], 		// end X = start X
							currentPoints[POS][Y], 		// end Y = start Y
							0 - World.mDepthUnit*waveDepth		// default board depth + 5 depth units
							}, 
					new int[] {1, 1, stageDuration*2}, 	// duration (ms)
					new int[] {delay, delay, delay + stageDuration}		// use given delay
				) // end TransformState 
			
			,
			// Wave: Rotate Back
			new TransformState(
					TransformType.ROTATE, 				// Translate
					new Boolean[] {true, true, false}, 	// Z dir
					TimeDomain.FINITE, 						// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC,			
							}, 	
					new float[] {
							90, 		
							45, 		
							0 							
							}, 
					currentPoints[ROT].clone(),
					new int[] {stageDuration, stageDuration, stageDuration}, 	// duration (ms)
					new int[] {delay+stageDuration, delay+stageDuration, delay+stageDuration}		// use given delay
				) // end TransformState 
			,
			
			// Wave: Settle up
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {false, false, true}, 			// Z dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					new float[] {
							currentPoints[POS][X], 		// start X 
							currentPoints[POS][Y], 		// start Y 
							0 - World.mDepthUnit*waveDepth		// Board Level - 5 depth units
							}, 
					new float[] {
							currentPoints[POS][X], 		// end X = start X
							currentPoints[POS][Y], 		// end Y = start Y
							0 							// Board Level
							}, 
					new int[] {1, 1, stageDuration}, 	// duration (ms)
					new int[] {delay, delay, delay + stageDuration*3}		// use given delay
				) // end TransformState 
			
			,
			
			// STAGE 2
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, false, false}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							null,
							null
							}, 	
					currentPoints[SIZ].clone(),							// start point: current position
					new float[] {
							0.2f,					 						// end X 
							1f, 											// end Y 
							1f												// end Z 
								}, 
					new int[] {stageDuration,1,1},	 								// duration: 400 ms
					new int[] {waveTime/2 + delay,waveTime/2 + delay,waveTime/2 + delay}										// no trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, false}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						null
						}, 	
				currentPoints[ROT].clone(),	
				new float[] {
						90, 						// end X 
						90, 						// end Y 
						0						// end Z
							}, 
				new int[] {stageDuration, stageDuration, 1}, 	// duration (ms)
				new int[] {waveTime/2 + delay,waveTime/2 + delay,waveTime/2 + delay}		// use given delay
			) // end TransformState 
			
			,
			
			// STAGE 3
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, true, false}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							null
							}, 	
					new float[] {
							0.2f,					 						// start X 
							1f, 											// start Y 
							1f												// start Z 
								}, 
					new float[] {
							0.2f,					 						// end X 
							0.2f, 											// end Y 
							1f												// end Z 
								}, 
					new int[] {stageDuration,stageDuration,1},	 			// duration (ms)
					new int[] {waveTime/2 + delay+stageDuration,waveTime/2 + delay+stageDuration,waveTime/2 + delay}				// trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, false}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						null
						}, 	
				new float[] {
						90, 					// end X 
						90, 					// end Y 
						0						// end Z
							}, 
				new float[] {
						180, 					// end X 
						180, 					// end Y 
						0						// end Z
							}, 
				new int[] {stageDuration, stageDuration, 1}, 	// duration (ms)
				new int[] {waveTime/2 + delay+stageDuration, waveTime/2 + delay+stageDuration, waveTime/2 + delay}		// use given delay
			) // end TransformState 
			
			,
			
			// STAGE 4
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, true, true}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							}, 	
					new float[] {
							0.2f,					 						// start X 
							0.2f, 											// start Y 
							1f												// start Z 
								}, 
					new float[] {
							0f,					 						// end X 
							0f, 											// end Y 
							0f												// end Z 
								}, 
					new int[] {stageDuration,stageDuration, stageDuration},	 		// duration (ms)
					new int[] {waveTime/2 + delay+stageDuration*2,waveTime/2 + delay+stageDuration*2 ,waveTime/2 + delay+stageDuration*2}	// trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, true}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						MotionEquation.LINEAR, 
						}, 	
				new float[] {
						180, 					// end X 
						180, 					// end Y 
						0						// end Z
							}, 
				new float[] {
						270, 					// end X 
						270, 					// end Y 
						90						// end Z
							}, 
				new int[] {stageDuration, stageDuration, stageDuration}, 	// duration (ms)
				new int[] {waveTime/2 + delay+stageDuration*2, waveTime/2 + delay+stageDuration*2, waveTime/2 + delay+stageDuration*2}		// use given delay
			) // end TransformState 
			
			,
			
			// RESET ROTATION (incase of redestroy?)
			new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {true, true, true}, 					// direction
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 			
							MotionEquation.LINEAR,
							MotionEquation.LINEAR, 
							}, 	
					new float[] {
							0, 					// end X 
							0, 					// end Y 
							0						// end Z
								}, 
					new float[] {
							0, 					// end X 
							0, 					// end Y 
							0						// end Z
								}, 
					new int[] {1, 1, 1}, 	// duration (ms)
					new int[] {waveTime/2 + delay+stageDuration*3, waveTime/2 + delay+stageDuration*3, waveTime/2 + delay+stageDuration*3}		// use given delay
				) // end TransformState 
			
		};
		
		
	}
	
	
	/*

	public static TransformState[] queueDestroy(float[][] currentPoints, int delay, int stageDuration)
	{
		//Wave   : Spans over 4 stages, overlaps with 2
		//Stage 1: X
		//Stage 2: spin left/up 90 deg, crunch X 
		//Stage 3: spin left/up 90 deg, crunch y
		//Stage 4: crunch all, spin all (disappear)
		
		// AL IN ALL: 6 STAGE LENGTH
		
		int waveTime = stageDuration*4; // up, down, down, up (sin)
		int waveDepth = 5;
		
		return new TransformState[] 
		{
			// Wave: Pickup
			new TransformState(
					TransformType.TRANSLATE, 				// Translate
					new Boolean[] {false, false, true}, 	// Z dir
					TimeDomain.FINITE, 						// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					currentPoints[POS].clone(),			// start point: current position or end of current running animation
					new float[] {
							currentPoints[POS][X], 		// end X = start X
							currentPoints[POS][Y], 		// end Y = start Y
							0 + World.mDepthUnit*waveDepth		// default board depth + 5 depth units
							}, 
					new int[] {1, 1, stageDuration}, 	// duration (ms)
					new int[] {delay, delay, delay}		// use given delay
				) // end TransformState 
			,
			
			// Wave: Drop 1 & 2
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {false, false, true}, 			// Z dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					new float[] {
							currentPoints[POS][X], 		// start X 
							currentPoints[POS][Y], 		// start Y 
							0 + World.mDepthUnit*waveDepth		//
							}, 
					new float[] {
							currentPoints[POS][X], 		// end X = start X
							currentPoints[POS][Y], 		// end Y = start Y
							0 - World.mDepthUnit*waveDepth		// default board depth + 5 depth units
							}, 
					new int[] {1, 1, stageDuration*2}, 	// duration (ms)
					new int[] {delay, delay, delay + stageDuration}		// use given delay
				) // end TransformState 
			
			,
			
			
			// Wave: Settle up
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {false, false, true}, 			// Z dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					new float[] {
							currentPoints[POS][X], 		// start X 
							currentPoints[POS][Y], 		// start Y 
							0 - World.mDepthUnit*waveDepth		// Board Level - 5 depth units
							}, 
					new float[] {
							currentPoints[POS][X], 		// end X = start X
							currentPoints[POS][Y], 		// end Y = start Y
							0 							// Board Level
							}, 
					new int[] {1, 1, stageDuration}, 	// duration (ms)
					new int[] {delay, delay, delay + stageDuration*3}		// use given delay
				) // end TransformState 
			
			,
			
			// STAGE 2
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, false, false}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							null,
							null
							}, 	
					currentPoints[SIZ].clone(),							// start point: current position
					new float[] {
							0.2f,					 						// end X 
							1f, 											// end Y 
							1f												// end Z 
								}, 
					new int[] {stageDuration,1,1},	 								// duration: 400 ms
					new int[] {waveTime/2 + delay,waveTime/2 + delay,waveTime/2 + delay}										// no trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, false}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						null
						}, 	
				currentPoints[ROT].clone(),	
				new float[] {
						90, 						// end X 
						90, 						// end Y 
						0						// end Z
							}, 
				new int[] {stageDuration, stageDuration, 1}, 	// duration (ms)
				new int[] {waveTime/2 + delay,waveTime/2 + delay,waveTime/2 + delay}		// use given delay
			) // end TransformState 
			
			,
			
			// STAGE 3
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, true, false}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							null
							}, 	
					new float[] {
							0.2f,					 						// start X 
							1f, 											// start Y 
							1f												// start Z 
								}, 
					new float[] {
							0.2f,					 						// end X 
							0.2f, 											// end Y 
							1f												// end Z 
								}, 
					new int[] {stageDuration,stageDuration,1},	 			// duration (ms)
					new int[] {waveTime/2 + delay+stageDuration,waveTime/2 + delay+stageDuration,waveTime/2 + delay}				// trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, false}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						null
						}, 	
				new float[] {
						90, 					// end X 
						90, 					// end Y 
						0						// end Z
							}, 
				new float[] {
						180, 					// end X 
						180, 					// end Y 
						0						// end Z
							}, 
				new int[] {stageDuration, stageDuration, 1}, 	// duration (ms)
				new int[] {waveTime/2 + delay+stageDuration, waveTime/2 + delay+stageDuration, waveTime/2 + delay}		// use given delay
			) // end TransformState 
			
			,
			
			// STAGE 4
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, true, true}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							MotionEquation.LINEAR, 
							}, 	
					new float[] {
							0.2f,					 						// start X 
							0.2f, 											// start Y 
							1f												// start Z 
								}, 
					new float[] {
							0f,					 						// end X 
							0f, 											// end Y 
							0f												// end Z 
								}, 
					new int[] {stageDuration,stageDuration, stageDuration},	 		// duration (ms)
					new int[] {waveTime/2 + delay+stageDuration*2,waveTime/2 + delay+stageDuration*2 ,waveTime/2 + delay+stageDuration*2}	// trigger delay
				) // end TransformState 
			
			,
			

			new TransformState(
				TransformType.ROTATE,	 							// Translate
				new Boolean[] {true, true, true}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LINEAR, 			
						MotionEquation.LINEAR,
						MotionEquation.LINEAR, 
						}, 	
				new float[] {
						180, 					// end X 
						180, 					// end Y 
						0						// end Z
							}, 
				new float[] {
						270, 					// end X 
						270, 					// end Y 
						90						// end Z
							}, 
				new int[] {stageDuration, stageDuration, stageDuration}, 	// duration (ms)
				new int[] {waveTime/2 + delay+stageDuration*2, waveTime/2 + delay+stageDuration*2, waveTime/2 + delay+stageDuration*2}		// use given delay
			) // end TransformState 
			
			,
			
			// RESET ROTATION (incase of redestroy?)
			new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {true, true, true}, 					// direction
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LINEAR, 			
							MotionEquation.LINEAR,
							MotionEquation.LINEAR, 
							}, 	
					new float[] {
							0, 					// end X 
							0, 					// end Y 
							0						// end Z
								}, 
					new float[] {
							0, 					// end X 
							0, 					// end Y 
							0						// end Z
								}, 
					new int[] {1, 1, 1}, 	// duration (ms)
					new int[] {waveTime/2 + delay+stageDuration*3, waveTime/2 + delay+stageDuration*3, waveTime/2 + delay+stageDuration*3}		// use given delay
				) // end TransformState 
			
		};
		
		
	}
	 */

	
	
	
	
	
	
	
	
	
	
	// Bonus Blocks
	
	

	
	
	public static TransformState[] queueBonusBombSpinDestroy(  float[][] currentPoints, int duration, int delay )
	{
		return new TransformState[] 
		{
				
		new TransformState(
			TransformType.ROTATE,	 							// Translate
			new Boolean[] {true, true, true}, 					// direction
			TimeDomain.FINITE, 									// Timed Animation
			new MotionEquation[] {
					MotionEquation.LOGISTIC, 			
					MotionEquation.LOGISTIC,
					MotionEquation.LOGISTIC, 
					}, 	
			currentPoints[ROT].clone(),	
			new float[] {
					270, 						// end X 
					180, 						// end Y 
					360						// end Z
						}, 
			new int[] {duration, duration, duration}, 	// duration (ms)
			new int[] {delay, delay, delay}		// use given delay
		) // end TransformState 
		
		,
		
		new TransformState(
				TransformType.SCALE,	 							// Translate
				new Boolean[] {true, true, true}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
						MotionEquation.LOGISTIC, 			
						MotionEquation.LOGISTIC,
						MotionEquation.LOGISTIC, 
						}, 	
				currentPoints[SIZ].clone(),	
				new float[] {
						0, 						// end X 
						0, 						// end Y 
						0						// end Z
							}, 
				new int[] {(int) (duration), (int) (duration), (int) (duration)}, 	// duration (ms)
				new int[] {delay, delay, delay}		// use given delay
			) // end TransformState 
	
		};
	}
	
	public static TransformState[] queueBonusWaveEpicenter(  float[][] currentPoints, int duration, int delay )
	{
		int waveDepth = 5;
		
		return new TransformState[] 
		{
				
		new TransformState(
			TransformType.TRANSLATE,	 							// Translate
			new Boolean[] {false, false, true}, 					// direction
			TimeDomain.FINITE, 									// Timed Animation
			new MotionEquation[] {
				null, 			
				null,
				MotionEquation.LOGISTIC, 
				}, 	
			currentPoints[POS].clone(),	
			new float[] {
				currentPoints[POS][X],
				currentPoints[POS][Y],
				0 + World.mDepthUnit*waveDepth		// default board depth + 5 depth units
						}, 
			new int[] {duration, duration, duration}, 	// duration (ms)
			new int[] {delay, delay, delay}		// use given delay
		) // end TransformState 
		
		,
		
		new TransformState(
				TransformType.TRANSLATE,	 							// Translate
				new Boolean[] {false, false, true}, 					// direction
				TimeDomain.FINITE, 									// Timed Animation
				new MotionEquation[] {
					null, 			
					null,
					MotionEquation.LOGISTIC, 
					}, 	
				new float[] {
					currentPoints[POS][X],
					currentPoints[POS][Y],
					0 + World.mDepthUnit*waveDepth		// default board depth + 5 depth units
							}, 
				currentPoints[POS].clone(),	 
				new int[] {(int) (duration*0.75), (int) (duration*0.75), (int) (duration*0.75)}, 	// duration (ms)
				new int[] {delay + duration, delay + duration, delay + duration}		// use given delay
			) // end TransformState 
		
		};
	}
	
	
	
	public static TransformState[] queueBonusWaveEpicenterDestroy(  float[][] currentPoints, int duration, int delay )
	{
		//Flashing: during delay
		//Stage 1: pickup (over all stages)
		//Stage 2: spin left/up 90 deg, crunch X 
		//Stage 3: spin left/up 90 deg, crunch y
		//Stage 4: crunch all, spin all (disappear)
		
		// AL IN ALL: 6 STAGE LENGTH
		
		int flashDelay = 400;
		int numFlashes = 2;
		int stageDuration = 400;
		
		int waveDepth = 5;
		
		return new TransformState[] 
		{
				
		new TransformState(
			TransformType.TRANSLATE,	 							// Translate
			new Boolean[] {false, false, true}, 					// direction
			TimeDomain.FINITE, 									// Timed Animation
			new MotionEquation[] {
				null, 			
				null,
				MotionEquation.LOGISTIC, 
				}, 	
			currentPoints[POS].clone(),	
			new float[] {
				currentPoints[POS][X],
				currentPoints[POS][Y],
				0 + World.mDepthUnit*waveDepth		// default board depth + 5 depth units
						}, 
			new int[] {duration, duration, duration}, 	// duration (ms)
			new int[] {delay, delay, delay}		// use given delay
		) // end TransformState 
		
		,
			
			// STAGE 1
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {false, false, true}, 			// Y dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC,			
							}, 	
					new float[] {
							currentPoints[POS][X],
							currentPoints[POS][Y],
							0 + World.mDepthUnit*waveDepth		// default board depth + 5 depth units
									}, 
					new float[] {
							currentPoints[POS][X], 							// end X = start X
							currentPoints[POS][Y], 							// end Y = start Y
							//0 + World.mDepthUnit*6		// default board depth + 2 depth units
							0 + World.mDepthUnit*10
							}, 
					new int[] {1, 1, stageDuration*3}, 	// duration (ms)
					new int[] {flashDelay+delay, flashDelay+delay, flashDelay+delay}		// use given delay
				) // end TransformState 
			,
			
			// STAGE 2
			new TransformState(
				TransformType.SCALE, 									// Scale
				new Boolean[] {true, true, true}, 						// X, Y, Z
				TimeDomain.FINITE, 										// Timed Animation
				new MotionEquation[] {
						MotionEquation.SPRING_UNDAMP_ALT_K9_3,
						MotionEquation.SPRING_UNDAMP_ALT_K4_5,
						MotionEquation.SPRING_UNDAMP_ALT_K9_5
						}, 	
				currentPoints[SIZ].clone(),							// start point: current position
				new float[] {
						1.2f,					 						// end X 
						1.2f, 											// end Y 
						1.2f											// end Z 
							}, 
				new int[] {(int) (stageDuration*1.2), (int) (stageDuration*0.8), stageDuration*1}, 	// duration (ms)
				new int[] {flashDelay+delay, flashDelay+delay, flashDelay+delay}		// use given delay
				) // end TransformState 
			
			,
			

			
			// STAGE 3
			new TransformState(
					TransformType.SCALE, 									// Scale
					new Boolean[] {true, true, true}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
					MotionEquation.LOGISTIC, 
					MotionEquation.LOGISTIC, 
					MotionEquation.SPRING_UNDAMP_ALT_K4_5
					}, 	
					new float[] {
							1.2f,					 						// end X 
							1.2f, 											// end Y 
							1.2f											// end Z 
								}, 
					new float[] {
							0,					 						// end X 
							0, 											// end Y 
							0												// end Z 
								}, 
					new int[] {(int) (stageDuration*0.8),(int) (stageDuration*1.2),(int) (stageDuration*1.5)},	 			// duration (ms)
					new int[] {flashDelay+delay+stageDuration,flashDelay+delay+stageDuration,flashDelay+delay+stageDuration}				// trigger delay
				) // end TransformState 
			
		
		
			,
			
			
			
			// STAGE 4
			new TransformState(
					TransformType.ROTATE, 									// Scale
					new Boolean[] {true, true, true}, 						// X, Y, Z
					TimeDomain.FINITE, 										// Timed Animation
					new MotionEquation[] {
							MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
							MotionEquation.LOGISTIC, 
							MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
							}, 	
					new float[] {
							0,					 						// start X 
							0, 											// start Y 
							0												// start Z 
								}, 
					new float[] {
							45f,					 						// end X 
							45f, 											// end Y 
							45f												// end Z 
								}, 
					new int[] {(int) (stageDuration*0.8),(int) (stageDuration*1.2),stageDuration},	 			// duration (ms)
					new int[] {(int) (flashDelay+delay+stageDuration*0.5),flashDelay+delay,flashDelay+delay+stageDuration}				// trigger delay
				) // end TransformState 
			 
			
			
		};
		
	
	}
	
	
	/*
	public static TransformState[] queueBonusWaveEpicenterDestroy(  float[][] currentPoints, float[] endPos, int duration, int delay )
	{
		//Flashing: during delay
		//Stage 1: pickup (over all stages)
		//Stage 2: spin left/up 90 deg, crunch X 
		//Stage 3: spin left/up 90 deg, crunch y
		//Stage 4: crunch all, spin all (disappear)
		
		// AL IN ALL: 6 STAGE LENGTH
		
		int flashDelay = 400;
		int numFlashes = 2;
		int stageDuration = 400;
		
		int waveDepth = 5;
		
		return new TransformState[] 
		{
				
		new TransformState(
			TransformType.TRANSLATE,	 							// Translate
			new Boolean[] {true, true, true}, 					// direction
			TimeDomain.FINITE, 									// Timed Animation
			new MotionEquation[] {
				MotionEquation.LOGISTIC, 			
				MotionEquation.LOGISTIC, 
				MotionEquation.LOGISTIC, 
				}, 	
			currentPoints[POS].clone(),	
			new float[] {
				endPos[X],
				endPos[Y],
				0		// default board depth + 5 depth units
						}, 
			new int[] {duration, duration, duration}, 	// duration (ms)
			new int[] {delay, delay, delay}		// use given delay
		) // end TransformState 
		
		

			
		};
		
	
	}
	*/
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// 3D Text
	
	
	

	public static TransformState[] queueFlipUpSpawn(  int delay, int duration )
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {true, false, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,			
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC,
							},
					//currentPoints[ROT].clone(),
					new float[] {-90,0,0}, 		// start point: current position or end of current running animation
					new float[] {0,0,0}, 		// end point
					new int[] {duration, duration,  duration}, 	// duration (ms)
					new int[] {delay, delay, delay}		// use given delay
				) // end TransformState 
			
		};
			

	}
	
	public static TransformState[] queueFlipOverSpawn( int delay, int duration )
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,			
							MotionEquation.LOGISTIC,
							null,
							},
					//currentPoints[ROT].clone(),
					new float[] {0,-90,0}, 		// start point: current position or end of current running animation
					new float[] {0,0,0}, 		// end point
					new int[] {1, duration,  1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			
		};
			

	}
	

	public static TransformState[] queueSquishRockFlipOverSpawn( int delay, int duration )
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,		
							//MotionEquation.SPRING_UNDAMP_ALT_K4_5,	// bad
							MotionEquation.SPRING_UNDAMP_ALT_K9_3,		// good
							//MotionEquation.SPRING_UNDAMP_ALT_K9_5,	// good
							null,
							},
					//currentPoints[ROT].clone(),
					new float[] {0,-90,0}, 		// start point: current position or end of current running animation
					new float[] {0,0,0}, 		// end point
					new int[] {1, duration,  1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			,
			
			new TransformState(
					TransformType.SCALE, 												// Translate
					new Boolean[] {true, true, true}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.SPRING_UNDAMP_ALT_K9_5, 
							MotionEquation.SPRING_UNDAMP_ALT_K9_5, 
							MotionEquation.SPRING_UNDAMP_ALT_K9_5
					},
					new float[] {0.1f,0.4f,0.6f}, 		// start
					new float[] {1,1,1}, 
					new int[] {(int) (duration*0.9),(int) (duration*0.5),(int) (duration*0.75)}, 									// duration: 400 ms
					new int[] {delay,delay,delay}											// no trigger delay
				) // end TransformState 
		
			
		};
			

	}

	
	


	public static TransformState[] queueSquishRockFlipOverCenterSpawn( float[][] currentPoints, float[] endPoint, int delay, int duration )
	{	
		return new TransformState[] 
		{
			new TransformState(
					TransformType.TRANSLATE, 												// Translate
					new Boolean[] {true, false, false}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,
							null,
							null, 
					},
					currentPoints[POS].clone(),
					new float[] {
						endPoint[X],
						endPoint[Y],
						0
						}, 
					new int[] {duration, 1, 1}, 				
					new int[] {delay, 0, 0}	
				) // end TransformState 
			
				
			,
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,		
							//MotionEquation.SPRING_UNDAMP_ALT_K4_5,	// bad
							MotionEquation.SPRING_UNDAMP_ALT_K9_3,		// good
							//MotionEquation.SPRING_UNDAMP_ALT_K9_5,	// good
							null,
							},
					//currentPoints[ROT].clone(),
					new float[] {0,-90,0}, 		// start point: current position or end of current running animation
					new float[] {0,0,0}, 		// end point
					new int[] {1, duration,  1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			,
			
			new TransformState(
					TransformType.SCALE, 												// Translate
					new Boolean[] {true, true, true}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.SPRING_UNDAMP_ALT_K9_5, 
							MotionEquation.SPRING_UNDAMP_ALT_K9_5, 
							MotionEquation.SPRING_UNDAMP_ALT_K9_5
					},
					new float[] {0.1f,0.4f,0.6f}, 		// start
					new float[] {1,1,1}, 
					new int[] {(int) (duration*0.9),(int) (duration*0.5),(int) (duration*0.75)}, 									// duration: 400 ms
					new int[] {delay,delay,delay}											// no trigger delay
				) // end TransformState 
		
			
		};
			

	}
	
	
	
	public static TransformState[] queueRockFlipOut( float[][] currentPoints, int delay, int duration)
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,
							MotionEquation.LOGISTIC,
							//MotionEquation.SPRING_UNDAMP_ALT_K4_5,	// bad
							//MotionEquation.SPRING_UNDAMP_ALT_K9_3,		// good
							//MotionEquation.SPRING_UNDAMP_ALT_K9_5,	// good
							null,
							}, 	
					currentPoints[ROT].clone(),
					//new float[] {0,0,0}, 		// start point: current position or end of current running animation
					new float[] {0,90,0}, 		// end point
					new int[] {1, duration,  1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			
		};
			

	}
	
	
	public static TransformState[] queueSpinDeform( float[][] currentPoints, int delay, int duration)
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,
							MotionEquation.LOGISTIC,		
							null
							}, 	
					currentPoints[ROT].clone(),
					//new float[] {0,0,0}, 		// start point: current position or end of current running animation
					new float[] {0,360*3,0}, 		// end point
					new int[] {1, duration, 1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			
			,
			
			new TransformState(
					TransformType.SCALE, 												// Translate
					new Boolean[] {true, true, false}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC, 
							null
					},
					currentPoints[SIZ].clone(),
					//new float[] {0.3f,0.5f,0.7f}, 		
					new float[] {1.3f,0.5f,1}, 
					new int[] {duration/2,duration/2,1}, 				
					new int[] {delay,delay,0}	
				) // end TransformState 
			,
			new TransformState(
					TransformType.SCALE, 												// Translate
					new Boolean[] {true, true, false}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC, 
							null
					},
					new float[] {1.3f,0.5f,1}, 
					new float[] {1,1,1}, 
					new int[] {duration/2,duration/2,1}, 				
					new int[] {delay + duration/2,delay + duration/2,0}	
				) // end TransformState 
			,
			
		};
			

	}
	
	
	public static TransformState[] queueTiltFloatup( float[][] currentPoints, float z, int delay, int duration)
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {true, false, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,	
							null,
							null
							}, 	
					currentPoints[ROT].clone(),
					//new float[] {0,0,0}, 		// start point: current position or end of current running animation
					new float[] {-45,0,0}, 		// end point
					new int[] {(int) (duration*0.75), 1, 1}, 	// duration (ms)
					new int[] {delay, 0, 0}		// use given delay
				) // end TransformState 
			
			,
			
			new TransformState(
					TransformType.SCALE, 												// Translate
					new Boolean[] {true, true, false}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,
							MotionEquation.LOGISTIC, 
							null
					},
					currentPoints[SIZ].clone(),
					//new float[] {0.3f,0.5f,0.7f}, 		
					new float[] {0,0,0}, 
					new int[] {duration,duration,1}, 				
					new int[] {delay,delay,0}	
				) // end TransformState 
			
			,
			
			new TransformState(
					TransformType.TRANSLATE, 												// Translate
					new Boolean[] {false, false, true}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC, 
					},
					currentPoints[POS].clone(),
					new float[] {
						currentPoints[POS][X],
						currentPoints[POS][Y],
						z
						}, 
					new int[] {1,1,duration}, 				
					new int[] {0,0,delay}	
				) // end TransformState 
			
			
		};
			

	}
	
	
	
	
	public static TransformState[] queueTiltBubbleup( float[][] currentPoints, float z, int delay, int duration)
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {true, false, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,	
							null,
							null
							}, 	
					currentPoints[ROT].clone(),
					//new float[] {0,0,0}, 		// start point: current position or end of current running animation
					new float[] {-45,0,0}, 		// end point
					new int[] {(int) (duration*0.75), 1, 1}, 	// duration (ms)
					new int[] {delay, 0, 0}		// use given delay
				) // end TransformState 
			
			,
			
				new TransformState(
						TransformType.SCALE,	 							// Translate
						new Boolean[] {true, true, false}, 					// direction
						TimeDomain.CONTINUOUS, 								// Timed Animation
						new MotionEquation[] {
								MotionEquation.SIN, 			
								MotionEquation.SIN,
								null
								}, 	
						currentPoints[SIZ].clone(),							// start point: current position
						new double[] {
								-0.07, 				// rate X 
								0.05, 				// rate Y 
								0				// rate Z
									}, 
						new double[] {
								444, 				// unit X 
								444, 			// unit Y 
								0				// unit Z
									}, 
						new int[] {delay, 0, 0}		// use given delay
					) // end TransformState 
				
		
			
			,
			
			new TransformState(
					TransformType.TRANSLATE, 												// Translate
					new Boolean[] {false, false, true}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							null,
							null,
							MotionEquation.LOGISTIC, 
					},
					currentPoints[POS].clone(),
					new float[] {
						0,
						0,
						z
						}, 
					new int[] {1,1,duration}, 				
					new int[] {0,0,delay}	
				) // end TransformState 
			
			,
			
			new TransformState(
					TransformType.TRANSLATE, 												// Translate
					new Boolean[] {true, true, false}, 									// Z Dir only
					TimeDomain.CONTINUOUS, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.SIN,
							MotionEquation.SIN,
							null, 
					},
					currentPoints[POS].clone(),
					new double[] {
						0.01, 				// rate X 
						0.01, 				// rate Y 
						0				// rate Z
							}, 
					new double[] {
						700, 				// unit X 
						700, 			// unit Y 
						0				// unit Z
							}, 				
					new int[] {delay,delay,0}	
				) // end TransformState 
			
			
		};
			

	}
	
	
	
	
	public static TransformState[] queueGameOverFollowBoardRotation( float[][] currentPoints, int delay, int duration)
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {true, false, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,	
							null,
							null
							}, 	
					currentPoints[ROT].clone(),
					//new float[] {0,0,0}, 		// start point: current position or end of current running animation
					new float[] {90,0,0}, 		// end point
					new int[] {duration,1,1}, 	// duration (ms)
					new int[] {delay, 0, 0}		// use given delay
				) // end TransformState 
			
		};
	}
	
	
	
	public static TransformState[] queueTextWobble( float[][] currentPoints )
	{

		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.ROTATE,	 							// Translate
					new Boolean[] {false, true, false}, 					// direction
					TimeDomain.CONTINUOUS, 								// Timed Animation
					new MotionEquation[] {
							null, 			
							MotionEquation.SIN,
							null
							}, 	
					currentPoints[ROT].clone(),							// start point: current position
					new double[] {
							0, 				// rate X 
							5, 			// rate Y 
							0				// rate Z
								}, 
					new double[] {
							0, 				// unit X 
							1200, 			// unit Y 
							0				// unit Z
								}, 
					new int[] {0, 0, 0}		// use given delay
				) // end TransformState 
			/*
			,
			
			new TransformState(
					TransformType.SCALE,	 							// Translate
					new Boolean[] {true, true, false}, 					// direction
					TimeDomain.CONTINUOUS, 								// Timed Animation
					new MotionEquation[] {
							MotionEquation.SIN, 			
							MotionEquation.SIN,
							null
							}, 	
					currentPoints[SIZ].clone(),							// start point: current position
					new double[] {
							-0.01, 				// rate X 
							0.02, 				// rate Y 
							0				// rate Z
								}, 
					new double[] {
							1200, 				// unit X 
							1200, 			// unit Y 
							0				// unit Z
								}, 
					new int[] {0, 0, 0}		// use given delay
				) // end TransformState 
			*/
		};
			 
	}
	
	public static TransformState[] queueSquishRockFlipIn( float[] startPos, float[] endPos , int delay, int duration )
	{	
		return new TransformState[] 
		{
			
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {true, false, false}, 			// Y dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,			
							//MotionEquation.SPRING_UNDAMP_ALT_K9_3,
							null,
							null,
							}, 	
					new float[] {
							startPos[X], 							// end X = start X
							startPos[Y], 							// end Y = start Y
							0		// default board depth + 2 depth units
							},
					new float[] {
						endPos[X], 							// end X = start X
						endPos[Y], 							// end Y = start Y
						0		// default board depth + 2 depth units
						}, 
					new int[] {duration, 1, 1}, 	// duration (ms)
					new int[] {delay, 1, 1}		// use given delay
				) // end TransformState 
			
			,
			
			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,		
							//MotionEquation.SPRING_UNDAMP_ALT_K4_5,	// bad
							//MotionEquation.SPRING_UNDAMP_ALT_K9_3,		// good
							MotionEquation.LOGISTIC,
							//MotionEquation.SPRING_UNDAMP_ALT_K9_5,	// good
							null,
							},
					//currentPoints[ROT].clone(),
					new float[] {0,-90,0}, 		// start point: current position or end of current running animation
					new float[] {0,0,0}, 		// end point
					new int[] {1, duration,  1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			,
			/*
			new TransformState(
					TransformType.SCALE, 												// Translate
					new Boolean[] {true, true, true}, 									// Z Dir only
					TimeDomain.FINITE, 													// Timed Animation
					new MotionEquation[] {
							MotionEquation.SPRING_UNDAMP_ALT_K9_5, 
							MotionEquation.SPRING_UNDAMP_ALT_K9_5, 
							MotionEquation.SPRING_UNDAMP_ALT_K9_5
					},
					new float[] {0.1f,0.4f,0.6f}, 		// start
					new float[] {1,1,1}, 
					new int[] {(int) (duration*0.9),(int) (duration*0.5),(int) (duration*0.75)}, 									// duration: 400 ms
					new int[] {delay,delay,delay}											// no trigger delay
				) // end TransformState 
			*/
		};
			

	}
	
	
	public static TransformState[] queueSquishRockFlipOut( float[][] currentPoints, float[] endPos , int delay, int duration )
	{	
		return new TransformState[] 
		{

					
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {true, false, false}, 			// Y dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,			
							//MotionEquation.SPRING_UNDAMP_ALT_K9_3,
							null,
							null,
							}, 	
					currentPoints[POS].clone(),						// start point: current position or end of current running animation
					new float[] {
						endPos[X], 							// end X = start X
						endPos[Y], 							// end Y = start Y
						0		
						}, 
					new int[] {duration, 1, 1}, 	// duration (ms)
					new int[] {delay, 1, 1}		// use given delay
				) // end TransformState 
			
			,

			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,		
							//MotionEquation.SPRING_UNDAMP_ALT_K4_5,	// bad
							MotionEquation.LOGISTIC,		// good
							//MotionEquation.SPRING_UNDAMP_ALT_K9_5,	// good
							null,
							},
					//currentPoints[ROT].clone(),
					new float[] {0,0,0}, 		// start point: current position or end of current running animation
					new float[] {0,-90,0}, 		// end point
					new int[] {1, duration,  1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			
		};
			

	}
	
	public static TransformState[] queueSquishRockFlipThrew( float[][] currentPoints, float[] endPos , int delay, int duration )
	{	
		return new TransformState[] 
		{

					
			new TransformState(
					TransformType.TRANSLATE, 						// Translate
					new Boolean[] {true, false, false}, 			// Y dir
					TimeDomain.FINITE, 								// Timed Animation
					new MotionEquation[] {
							MotionEquation.LOGISTIC,			
							//MotionEquation.SPRING_UNDAMP_ALT_K9_3,
							null,
							null,
							}, 	
					currentPoints[POS].clone(),						// start point: current position or end of current running animation
					new float[] {
						endPos[X], 							// end X = start X
						endPos[Y], 							// end Y = start Y
						0		
						}, 
					new int[] {duration, 1, 1}, 	// duration (ms)
					new int[] {delay, 1, 1}		// use given delay
				) // end TransformState 
			
			,

			new TransformState(
					TransformType.ROTATE, 							// Translate
					new Boolean[] {false, true, false}, 				// X axis
					TimeDomain.FINITE, 									// Timed Animation
					new MotionEquation[] {
							null,		
							//MotionEquation.SPRING_UNDAMP_ALT_K4_5,	// bad
							MotionEquation.LOGISTIC,		// good
							//MotionEquation.SPRING_UNDAMP_ALT_K9_5,	// good
							null,
							},
					//currentPoints[ROT].clone(),
					new float[] {0,0,0}, 		// start point: current position or end of current running animation
					new float[] {0,90,0}, 		// end point
					new int[] {1, duration,  1}, 	// duration (ms)
					new int[] {0, delay, 0}		// use given delay
				) // end TransformState 
			
		};
			

	}
	
	
}
