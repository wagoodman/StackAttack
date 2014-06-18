package com.wagoodman.stackattack;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;

import com.wagoodman.stackattack.Animation;
import com.wagoodman.stackattack.Block;
import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.ColorState;
import com.wagoodman.stackattack.Coord;
import com.wagoodman.stackattack.DropSectionState;
import com.wagoodman.stackattack.FontManager;
import com.wagoodman.stackattack.GL3DText;
import com.wagoodman.stackattack.GLShape;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.PxCropSwipeLeft;
import com.wagoodman.stackattack.PxDoubleStaticSwipeSideways;
import com.wagoodman.stackattack.PxDoubleStaticSwipeUp;
import com.wagoodman.stackattack.PxImage;
import com.wagoodman.stackattack.PxMoveHinter;
import com.wagoodman.stackattack.TutorialBannerMenu;

import android.content.Context;

public class TutorialState 
{
	private final MainActivity game;
	private final Context mContext;
	
	public Boolean isInTutorial = false;
	
	private HashSet<String> mRedMatchBlocks;
	private HashSet<Coord<Integer>> mRedMatchBlock_Coords;
	private HashSet<String> mYellowMatchBlocks;
	private HashSet<Coord<Integer>> mYellowMatchBlock_Coords;
	private String mBlueHintBlock;
	private Coord<Integer> hintCoord;
	Color[][] mStockBoard = new Color[MainActivity.ROWCOUNT][MainActivity.COLCOUNT];
	
	TutorialState(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
	}
	
	// Rules
	private PxImage mRuleDangerBanner;
	
	private GL3DText mRuleInstructions1;
	private Coord<Integer> mRuleCoords1;
	private PxImage mRuleArrow1;

	private GL3DText mRuleInstructions2;
	private Coord<Integer> mRuleCoords2;
	private PxImage mRuleArrow2;
	
	// Gameplay
	private GL3DText mGameplayInstructions1;
	private Coord<Integer> mGameplayCoords1;
	
	// Matching
	private GL3DText mMatchingInstructions1;
	private Coord<Integer> mMatchingCoords1 = new Coord<Integer>(MainActivity.ROWCOUNT - 1, (MainActivity.COLCOUNT/2) - 3);
	
	private PxCropSwipeLeft mMatchingSwipe;
	
	// Garbage
	private GL3DText mGarbageInstructions1;
	private Coord<Integer> mGarbageCoords1;
	
	// Boards
	private GL3DText mBoardsInstructions1;
	private Coord<Integer> mBoardsCoords1;
	
	private PxDoubleStaticSwipeUp mBoardsDoubleSwipe;
	
	// Advanced
	private GL3DText mAdvancedInstructions1;
	private Coord<Integer> mAdvancedCoords1;
	
	private PxDoubleStaticSwipeSideways mAdvancedDoubleSwipe;
	
	public void setupElements()
	{		
		// Rules
		int	bannerWidth = 506,
			bannerHeight = 92;
		float bannerAbsScale = (float) ( (1f/bannerWidth)*(game.getWorld().mScreenWidth) );
		
		/*
		mRuleDangerBanner = new PxImage(mContext,  
				R.drawable.dangerbanner_w506h92, bannerWidth, bannerHeight, 
				bannerAbsScale, 
				(-bannerWidth/2f), -bannerHeight,
				game.getWorld().mScreenWidth/2, (float) (game.getWorld().mScreenHeight - game.getWorld().mScreenHeight*DropSectionState.FOLDED.getHeight()/2f) ,
				0,0,
				true, true, 
				false,		
				false,		// scale on interaction
				false,		// crop on interaction
				false		// sticky interaction
				);
		*/
		
		
		
		
		
		mRuleCoords2 = new Coord<Integer>(MainActivity.ROWCOUNT/2 +1, (MainActivity.COLCOUNT/2) - 2);
		mRuleInstructions2 = new GL3DText(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"Match blocks for points.",
						true,
						FontManager.BANNER_VALUE_COLOR,
						mRuleCoords2.getRow(),
						mRuleCoords2.getCol(),
						0.8f, //scale
						false,
						false
					);
		
		int	arrowWidth = 100,
			arrowHeight = 161;
		float arrowAbsScale = (float) ( (1f/arrowWidth)*(game.getWorld().mScreenBlockLength) )*1.5f;
		
		mRuleArrow2 = new PxImage(mContext,  
				R.drawable.downarrow_w100h161, arrowWidth, arrowHeight, 
				arrowAbsScale, 
				0, -arrowHeight,
				(mRuleCoords2.getCol()-1)*game.getWorld().mScreenBlockLength, (mRuleCoords2.getRow()+1)*game.getWorld().mScreenBlockLength + game.getWorld().mScreenBlockLength*0.5f,
				0,0,
				true, true, 
				false,		
				false,		// scale on interaction
				false,		// crop on interaction
				false		// sticky interaction
				);
		
		
		
		
		
		
		
		mRuleCoords1 = new Coord<Integer>(MainActivity.ROWCOUNT - 1, (MainActivity.COLCOUNT/2) - 3);
		mRuleInstructions1 = new GL3DText(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"Don't hit the top.",
						true,
						FontManager.BANNER_VALUE_COLOR,
						mRuleCoords1.getRow(),
						mRuleCoords1.getCol(),
						0.8f, //scale
						false,
						false
					);
		
		arrowWidth = 124; 
		arrowHeight = 145;
		//arrowAbsScale = (float) ( (1f/arrowWidth)*(game.getWorld().mScreenBlockLength) )*1.3f;
		
		mRuleArrow1 = new PxImage(mContext,  
				R.drawable.uparrow_w124h145, arrowWidth, arrowHeight, 
				arrowAbsScale, 
				0, -arrowHeight*0.3f,
				(MainActivity.COLCOUNT-2.4f)*game.getWorld().mScreenBlockLength, mRuleCoords1.getRow()*game.getWorld().mScreenBlockLength + game.getWorld().mScreenBlockLength*0.5f,
				0,0,
				true, true, 
				false,		
				false,		// scale on interaction
				false,		// crop on interaction
				false		// sticky interaction
				);
		
		
		

		
		
		// Gameplay
		mGameplayCoords1 = new Coord<Integer>(MainActivity.ROWCOUNT -1, (MainActivity.COLCOUNT/2) - 3);
		mGameplayInstructions1 = new GL3DText(
						mContext,
						FontManager.MENUMINORITEM_FONT,
						"Blocks only move sideways",
						true,
						FontManager.BANNER_VALUE_COLOR,
						mGameplayCoords1.getRow(),
						mGameplayCoords1.getCol(),
						0.8f, //scale
						false,
						false
					);
		
		
		// Matching
		mMatchingCoords1 = new Coord<Integer>(MainActivity.ROWCOUNT -1, (MainActivity.COLCOUNT/2) - 3);
		mMatchingInstructions1 = new GL3DText(
				mContext,
				FontManager.MENUMINORITEM_FONT,
				"Match the Red Blocks",
				true,
				FontManager.BANNER_VALUE_COLOR,
				mMatchingCoords1.getRow(),
				mMatchingCoords1.getCol(),
				1f, //scale
				false,
				false
			);
		
		int circleX = (int) (game.getWorld().mScreenBlockLength*((int)(MainActivity.COLCOUNT/2 +2) +0.5  )),
			circleY = (int) (game.getWorld().mScreenBlockLength*((int)(2)    +0.5  ));
		
		int arrowHeadX = (int) (game.getWorld().mScreenBlockLength*((int)(MainActivity.COLCOUNT/2 -1)  )),
			arrowHeadY = (int) (game.getWorld().mScreenBlockLength*((int)(2)    +0.5  ));
			
		mMatchingSwipe = new PxCropSwipeLeft(mContext, 
				circleX, circleY, 
				arrowHeadX, arrowHeadY, 
				false, true,  // lock x, y
				true
				);
		
		// Garbage		
		mGarbageCoords1 = new Coord<Integer>((int)(MainActivity.ROWCOUNT), MainActivity.COLCOUNT/2 - 3);
		mGarbageInstructions1 = new GL3DText(
				mContext,
				FontManager.MENUMINORITEM_FONT,
				"Match next to the Garbage Blocks",
				true,
				FontManager.BANNER_VALUE_COLOR,
				mGarbageCoords1.getRow(),
				mGarbageCoords1.getCol(),
				0.65f, //scale
				false,
				false
			);

				
		// Boards
		
		mBoardsCoords1 = new Coord<Integer>((int)(MainActivity.ROWCOUNT -1), MainActivity.COLCOUNT/2 - 2);
		mBoardsInstructions1 = new GL3DText(
				mContext,
				FontManager.MENUMINORITEM_FONT,
				 "Want More Blocks?",
				true,
				FontManager.BANNER_VALUE_COLOR,
				mBoardsCoords1.getRow(),
				mBoardsCoords1.getCol(),
				0.85f, //scale
				false,
				false
			);
		
		circleY = (int) (game.getWorld().mScreenBlockLength*((int)(3)    +0.5  ));
		
		mBoardsDoubleSwipe = new PxDoubleStaticSwipeUp(
				mContext, 
				circleY
				);
		
		// Advanced
		mAdvancedCoords1 = new Coord<Integer>((int)(MainActivity.ROWCOUNT - 1), MainActivity.COLCOUNT/2 - 3);
		mAdvancedInstructions1 = new GL3DText(
				mContext,
				FontManager.MENUMINORITEM_FONT,
				"Harder Difficulties = More Boards",
				true,
				FontManager.BANNER_VALUE_COLOR,
				mAdvancedCoords1.getRow(),
				mAdvancedCoords1.getCol(),
				0.65f, //scale
				false,
				false
			);
		
		circleY = (int) (game.getWorld().mScreenBlockLength*((int)(3)    +0.5  ));
		
		mAdvancedDoubleSwipe = new PxDoubleStaticSwipeSideways(
				mContext
				);
		
	}
	
	public void reset()
	{
		isInTutorial = false;
	}
	
	public void goTo(String tutMenu)
	{
		goTo(tutMenu, true, null);
	}
	
	public void goTo(String tutMenu, Boolean doOutro)
	{
		goTo(tutMenu, doOutro, null);
	}
	
	public void goTo(String tutMenu, Boolean doOutro, Integer duration)
	{	
		
		// transition banner
		game.getWorld().mTutorialMenu.transitionToMenu(tutMenu, true);
		
		// reset general board info
		normalizeFrame();
		
		// transition board
		
			// outro...
			if (doOutro)
			{
				game.getWorld().mWidgetLayer.outro(
						Animation.SQUISHROCKFLIPTHREW, 
						1000, 
						0  , 
						null
					);
				
				//game.text += "outro\n";
				//game.textviewHandler.post(game.updateTextView);
			}
		
			// intro...
		if (tutMenu == TutorialBannerMenu.RULES)
		{
			loadFirstTutorialBoard();
		}
		else if (tutMenu == TutorialBannerMenu.GAMEPLAY)
		{
			load_Gameplay();
		}
		else if (tutMenu == TutorialBannerMenu.MATCHING)
		{
			load_Matching();
		}
		else if (tutMenu == TutorialBannerMenu.GARBAGE)
		{
			load_Garbage();
		}
		else if (tutMenu == TutorialBannerMenu.BOARD)
		{
			load_Board();
		}
		else if (tutMenu == TutorialBannerMenu.ADVANCED)
		{
			load_Advanced();
		}
		
		// Check if the tutorial is still active...
		if (tutMenu == TutorialBannerMenu.NONE)
			isInTutorial = false;
		else
			isInTutorial = true;
	}
	



	
	


	public void loadFirstTutorialBoard()
	{
		game.getWorld().mBoards.mBoardMembers.clear();
		game.getWorld().createBoards(1);
		
		buildStockBoard();
		
		
		Board temp;

		
		
		for (int idx=0; idx < game.getWorld().mBoards.mBoardMembers.size(); idx++)
		{
			temp = new Board(mContext);
			
			// Populate Board
			normalizeBoard(temp, 
					false, // all grey
					false, // show red
					true,  // allow add
					false, // show blue hint
					false  // show yellow
					);

			temp.isInteractable = false;
			
			// add new board
			game.getWorld().mBoards.mBoardMembers.set(idx, temp);
			
		}
		
		/*
		// banner
		game.getWorld().mWidgetLayer.addItem("RuleBanner",
				mRuleDangerBanner,
				700,
				0,
				null		// dont trigger outro
			);
		*/
		
		
		// show text 1
		game.getWorld().mWidgetLayer.addItem(
				"RuleText1",
				mRuleInstructions1,
				Animation.SQUISHROCKFLIPIN , null, mRuleCoords1 , 1000, 0  , null
			);
		
		// arrow 1
		game.getWorld().mWidgetLayer.addItem("RuleArrow1",
				mRuleArrow1,
				700,
				0,
				null		// dont trigger outro
			);
		
		
		// show text 2
		game.getWorld().mWidgetLayer.addItem(
				"RuleText2",
				mRuleInstructions2,
				Animation.SQUISHROCKFLIPIN , null, mRuleCoords2 , 1000, 0  , null
			);
		
		// arrow2
		game.getWorld().mWidgetLayer.addItem("RuleArrow2",
				mRuleArrow2,
				700,
				0,
				null		// dont trigger outro
			);

		
		
		
	}
	
	


	private void load_Gameplay() 
	{

		for (Board board : game.getWorld().mBoards.mBoardMembers)
		{
			
			// Populate Board
			normalizeBoard(board, 
					true,  // all grey
					false, // show red
					false, // allow add
					true, // show blue hint
					false  // show yellow
					);
			
			// find coords
			hintCoord = board.mGrid.getBlockPosition(mBlueHintBlock);
			
			
			for (Map.Entry<String, GLShape> entry : board.mBlocks.entrySet())
			{
				// turn everything grey except for the red match blocks
				if ( entry.getKey() == mBlueHintBlock )
				{
					// ensure they are red!
					entry.getValue().mColor.dequeue();
					entry.getValue().mColor.enqueue( 
							new ColorState( 
									new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
									entry.getValue().mColor.mCurrentColor, 
									Color.BLUE,
									new int[] {1000, 1000, 1000, 1000},
									new int[] {0,0,0,0}	// relative to FIFO q
									));	
				}
			}
		}
		
		// show text
		game.getWorld().mWidgetLayer.addItem(
				"GameplayText1",
				mGameplayInstructions1,
				Animation.SQUISHROCKFLIPIN , null, mGameplayCoords1 , 1000, 0  , null
			);

		
		// show hint!
		game.getWorld().mTutorialState.moveHint( hintCoord.getRow() , hintCoord.getCol() , 500 , null );
		
		
	}
	
	
	

	public void load_Matching()
	{
		Coord<Integer> redBlock = null;
		for (Board board :  game.getWorld().mBoards.mBoardMembers)
		{
			
			// Populate Board
			normalizeBoard(board, 
					true,  // all grey
					true,  // show red
					true,  // allow add
					false, // show blue hint
					false  // show yellow
					);
			
			try{
				for (String redBlockId : mRedMatchBlocks)
				{
					Coord<Integer> crd = board.mGrid.getBlockPosition( redBlockId );
					
					//game.text +=  crd.getRow() + " == " + primaryRedBlockRow + "\n";
					//game.textviewHandler.post( game.updateTextView );
					
					if (crd.getRow() == primaryRedBlockRow)
					{
						//game.text += "Found: " + crd + "\n";
						//game.textviewHandler.post( game.updateTextView );
						
						redBlock = crd.clone();
						break;
					}
				}
			}
			catch(Exception e){}
			
			//game.text += redBlock + "\n";
			//game.textviewHandler.post( game.updateTextView );
			
			for (Map.Entry<String, GLShape> entry : board.mBlocks.entrySet())
			{
				// turn everything grey except for the red match blocks
				if ( mRedMatchBlocks.contains( entry.getKey() ) )
				{
					// ensure they are red!
					entry.getValue().mColor.dequeue();
					entry.getValue().mColor.enqueue( 
							new ColorState( 
									new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
									entry.getValue().mColor.mCurrentColor, 
									Color.RED,
									new int[] {1000, 1000, 1000, 1000},
									new int[] {0,0,0,0}	// relative to FIFO q
									));	
				}
			}
		}
		

		game.getWorld().mWidgetLayer.addItem(
				"MatchInstructions1",
				mMatchingInstructions1,
				Animation.SQUISHROCKFLIPIN , null,mMatchingCoords1 , 1000, 0   , null
			);

		if (redBlock != null)
			mMatchingSwipe.setFingerPos(redBlock);
		
		game.getWorld().mWidgetLayer.addItem("MatchingSwipeLeft",
				mMatchingSwipe,
				1000,
				0,
				null		// dont trigger outro
			);
		
	}

	
	



	
	private void load_Garbage() 
	{
		
		for (Board board :  game.getWorld().mBoards.mBoardMembers)
		{
			
			// Populate Board
			normalizeBoard(board, 
					true,   // all grey
					false,  // show red
					true,   // allow add
					false,  // show blue hint
					true    // show yellow
					);
			
			board.mGarbageGenerator.isEnabled = true;
			
			board.generateGarbage( 3 , true);
			
			board.mGarbageGenerator.isEnabled = false;
			

		}

		
		game.getWorld().mWidgetLayer.addItem(
				"GarbageInstructions",
				mGarbageInstructions1,
				Animation.SQUISHROCKFLIPIN , null, mGarbageCoords1, 1000, 0   , null
			);
		
		
	}

	
	private void load_Board() 
	{
		if (game.getWorld().mBoards.mBoardMembers.size() > 1)
		{
			game.getWorld().mBoards.mBoardMembers.clear();
			game.getWorld().createBoards(1);	
		}
		
		for (Board board :  game.getWorld().mBoards.mBoardMembers)
		{
			normalizeBoard(board, 
					false,  // all grey
					false,  // show red
					true,  // allow add
					false, // show blue hint
					false  // show yellow
					);
			
			board.isInteractable = false;
			
			board.generateRowOnDeck();
			
		}
		
		// attempt to get back to 0
		game.getWorld().mBoards.tour();
		
		game.getWorld().mBoards.setIterationSpeed(1);
		game.getWorld().mBoards.resetBoardProgression();
		game.getWorld().mBoards.mEnableBoardProgression = true;
		

		game.getWorld().mWidgetLayer.addItem(
				"BoardsInstructions1",
				mBoardsInstructions1,
				Animation.SQUISHROCKFLIPIN , null, mBoardsCoords1, 1000, 0   , null
			);
		
		
		game.getWorld().mWidgetLayer.addItem(
				"BoardsDoubleSwipe",
				mBoardsDoubleSwipe,
				1000,
				0,
				null		// dont trigger outro
			);
		
	}


	private void load_Advanced() 
	{
		
		game.getWorld().mBoards.mBoardMembers.clear();
		game.getWorld().createBoards(2);
	
		Board temp;
		for (int idx=0; idx < game.getWorld().mBoards.mBoardMembers.size(); idx++)
		{
			temp = new Board(mContext);
			
			int   minFillLevel = 2;
			float maxFillRatio = 0.8f;		//Only fill up to xx% of rows in a col (at most)!
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
							Block leftBlock = temp.getBlock(row, col-1);
							
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
					temp.spawnBlock( 
							new Block(mContext, colorPick, row, col),  // not visible, is interactable 
							row, col, Animation.GROWFLIPSPAWN, 600
							);

					// remember last colorPick; don't colorPick again this color consecutively!
					lastcolorPick = colorPick;
					
				} // end for rows...
				
			} // end for cols...
		
			// the row that will rise from below the screen
			temp.generateRowOnDeck();
			
			// set the board state as populated
			temp.isPopulated = true;
			
			// add new board
			game.getWorld().mBoards.mBoardMembers.set(idx, temp);
			
		}
		/*
		// move board
		for (Board board :  game.getWorld().mBoards.mBoardMembers)
		{
			board.generateRowOnDeck();
		}
		
		game.getWorld().mBoards.setIterationSpeed(0);
		game.getWorld().mBoards.mEnableBoardProgression = true;
		*/
	
		game.getWorld().mWidgetLayer.addItem(
				"AdvancedInstructions1",
				mAdvancedInstructions1,
				Animation.SQUISHROCKFLIPIN , null, mAdvancedCoords1, 1500, 0   , null
			);
		
		
		game.getWorld().mWidgetLayer.addItem(
				"AdvancedDoubleSwipe",
				mAdvancedDoubleSwipe,
				1000,
				0,
				null		// dont trigger outro
			);
		
		
		// show off the boards
		game.getWorld().mBoards.tour();
		
	}

	
	
	
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	// SUPPORTING METHODS
	
	
	
	
	
	

	
	public void moveHint(int row, int col, int transitionDur, Integer stableDur)
	{
		
		int circleX = (int) (game.getWorld().mScreenBlockLength*(col +0.5 )),
			circleY = (int) (game.getWorld().mScreenBlockLength*(row +0.5 ));
		
		game.getWorld().mWidgetLayer.addItem("HowToMoveHint",
				new PxMoveHinter(mContext, 
					circleX, circleY, 
					false, true,  // lock x, y
					true
					),
					transitionDur,
					0, 
					stableDur // trigger outro (pass in null for no trigger)
				);
		
	}
	
	
	private int primaryRedBlockRow;
	private void buildStockBoard()
	{

		mRedMatchBlocks = new HashSet<String>();
		mRedMatchBlock_Coords = new HashSet<Coord<Integer>>();
		
		mYellowMatchBlocks = new HashSet<String>();
		mYellowMatchBlock_Coords = new HashSet<Coord<Integer>>();
		
		int curFillLevel = MainActivity.ROWCOUNT/2;
		
		for (int col=0; col < MainActivity.COLCOUNT; col++)
		{

			Color lastcolorPick = Color.NONE;
			
			for (int row=0; row < MainActivity.ROWCOUNT; row++)
			{
				
				if (row <= curFillLevel)
				{
					Boolean goodcolorPick = false;
					Color colorPick = Color.NONE;
					
					// colorPick a color! avoid two consecutive blocks with same color (row & col)
					do
					{
						colorPick = Color.pickColorExcept(lastcolorPick);
						
						if (col!=0)
						{
							Color beneath = mStockBoard[row][col-1];
							
							// dont generate red
							if (beneath != colorPick && colorPick != Color.RED && colorPick != Color.YELLOW)
								goodcolorPick = true;
							
						}
						else
						{
							if (colorPick != Color.RED && colorPick != Color.YELLOW)
								goodcolorPick = true;
						}
						
					}
					while (!goodcolorPick);
					
					// Store color
					mStockBoard[row][col] = colorPick;

					// remember last colorPick; don't colorPick again this color consecutively!
					lastcolorPick = colorPick;
				}
				else
				{
					// Store no block
					mStockBoard[row][col] = Color.NONE;
				}
				
			} // end for rows...
			
		} // end for cols...
		

		// Misdirection ;)
		mStockBoard[0][0] = Color.GREY;
		mStockBoard[1][MainActivity.COLCOUNT - 1] = Color.GREY;
		mStockBoard[MainActivity.ROWCOUNT/2][0] = Color.NONE;
		mStockBoard[MainActivity.ROWCOUNT/2][1] = Color.NONE;
		mStockBoard[MainActivity.ROWCOUNT/2-1][0] = Color.NONE;
		mStockBoard[MainActivity.ROWCOUNT/2][MainActivity.COLCOUNT -1] = Color.NONE;
		mStockBoard[MainActivity.ROWCOUNT/2][MainActivity.COLCOUNT -3] = Color.NONE;
		
		// Match Coords
		int mrow1 =0, 
			mcol1 = MainActivity.COLCOUNT/2 -2,
			
			mrow2 = 1,
			mcol2 = MainActivity.COLCOUNT/2 -2,
			
			mrow3 = 2,
			mcol3 = MainActivity.COLCOUNT/2 +2;
		
		primaryRedBlockRow = mrow3;
		
		mStockBoard[mrow1][mcol1] = Color.RED;
		mStockBoard[mrow2][mcol2] = Color.RED;
		mStockBoard[mrow3][mcol3] = Color.RED;
		
		mRedMatchBlock_Coords.add(new Coord<Integer>(mrow1, mcol1));
		mRedMatchBlock_Coords.add(new Coord<Integer>(mrow2, mcol2));
		mRedMatchBlock_Coords.add(new Coord<Integer>(mrow3, mcol3));
		
		// Hint Coord
		hintCoord = new Coord<Integer>(3, MainActivity.COLCOUNT/2 );
		mStockBoard[hintCoord.getRow()][hintCoord.getCol()] = Color.BLUE;

		
		// Garbage Coords
		int grow = MainActivity.ROWCOUNT/2, 
				
			gcol1 = MainActivity.COLCOUNT/2 -1,
			gcol2 = MainActivity.COLCOUNT/2 ,
			gcol3 = MainActivity.COLCOUNT/2 +2;
		
		mStockBoard[grow][gcol1] = Color.YELLOW;
		mStockBoard[grow][gcol2] = Color.YELLOW;
		mStockBoard[grow][gcol3] = Color.YELLOW;
		
		mYellowMatchBlock_Coords.add(new Coord<Integer>(grow, gcol1));
		mYellowMatchBlock_Coords.add(new Coord<Integer>(grow, gcol2));
		mYellowMatchBlock_Coords.add(new Coord<Integer>(grow, gcol3));
		
	}
	
	
	private void normalizeFrame()
	{
		if (game.getWorld().mBoards.mCurrentGlobalRowIndex != 0)
		{
			game.getWorld().mBoards.mGameState.resetMultiplier();
			game.getWorld().mBoards.mEnableBoardProgression = false;
			game.getWorld().mBoards.resetBoardProgression();
			
			// reset all boards
			
			Board temp;

			for (int idx=0; idx < game.getWorld().mBoards.mBoardMembers.size(); idx++)
			{
				temp = new Board(mContext);

				// Populate Board
				normalizeBoard(temp, 
						false, // all grey
						false, // show red
						true,  // allow add
						false, // show blue hint
						false  // show yellow
						);

				// add new board
				game.getWorld().mBoards.mBoardMembers.set(idx, temp);

			}
		}
		else
		{
			game.getWorld().mBoards.mGameState.resetMultiplier();
			game.getWorld().mBoards.mEnableBoardProgression = false;
			game.getWorld().mBoards.resetBoardProgression();
		}
	}

	private void normalizeBoard(Board temp, Boolean allBlocksGrey, Boolean showRed, Boolean allowAddBlocks, Boolean showHintBlock, Boolean showYellow)
	{
		Block block;
		Color color;
		
		// make interactable
		temp.isInteractable = true;
		
		// ensure there is no board multiplier
		temp.mGarbageGenerator.isEnabled = false;
		
		// ensure there is no row on deck
		temp.deleteRowOnDeck();
		
		for (int col=0; col < MainActivity.COLCOUNT; col++)
		{
			
			for (int row=0; row <  MainActivity.ROWCOUNT; row++)
			{
				
				//game.text += "   "+row+" , " +col+"\n";
				//game.textviewHandler.post( game.updateTextView );
				
				try
				{
					if (allBlocksGrey)
					{
						if (showRed && mStockBoard[row][col] == Color.RED )
							color = mStockBoard[row][col];
						else if (showYellow && mStockBoard[row][col] == Color.YELLOW )
							color = mStockBoard[row][col];
						else
						{
							if ( mStockBoard[row][col] != Color.NONE)
								color = Color.GREY;
							else
								color = Color.NONE;
						}
					}
					else
						color = mStockBoard[row][col];
					
					block = temp.getBlock(row, col);
					
					if (color != Color.NONE)
					{
						
						// add block
						if (block == null)
						{
							if (allowAddBlocks)
							{
								// not visible, is interactable 
								block = new Block(mContext, color, row, col);
								
								// remember red blocks
								if (color == Color.RED)
									mRedMatchBlocks.add(block.getId());
								
								// remember yellow blocks
								if (color == Color.YELLOW)
									mYellowMatchBlocks.add(block.getId());
								
								// remember blue hint block
								if (row == hintCoord.getRow() && col == hintCoord.getCol())
									mBlueHintBlock = block.getId();
								
								// Create the new block!
								temp.spawnBlock( 
										block, 
										row, col, Animation.GROWFLIPSPAWN, 600
										);
							}
						}
						else
						{
							if (showHintBlock && block.getId() == mBlueHintBlock)
							{
								// change block
								block.mColor.enqueue( 
										new ColorState( 
												new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
												block.mColor.mCurrentColor,
												Color.BLUE, 
												new int[] {1000, 1000, 1000, 1000},
												new int[] {0,0,0,0}	// relative to FIFO q
												));	
								continue;
							}
						
							
							else if (color != block.mColor.mCurrentColor)
							{
								if (block.mColor.mCurrentColor == Color.RED)
								{
									mRedMatchBlocks.remove(block.getId());
								}
								
								if (color == Color.RED)
								{
									mRedMatchBlocks.add(block.getId());
								}
								
								// change block
								block.mColor.enqueue( 
										new ColorState( 
												new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
												block.mColor.mCurrentColor,
												color, 
												new int[] {1000, 1000, 1000, 1000},
												new int[] {0,0,0,0}	// relative to FIFO q
												));	
								
								
								
							}
						}
					}
					else
					{
						// color is NONE
						
						//game.text += "Deleting..." +block.getId()+ "\n";
						//game.textviewHandler.post( game.updateTextView );
						
						// delete block
						if (block != null)
						{
							temp.destroyBlock(block.getId());
						}
					}
					
					
					
				}
				catch (Exception e)
				{
					//Log.e("TutStNormBd", e.toString());
					
					
					/*
					String content = "";
					//Log.e("GLBtn Action", e.getMessage());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos);
					e.printStackTrace(ps);
					try {
					content	= baos.toString("ISO-8859-1");
					} catch (UnsupportedEncodingException e1) {
						game.text += e1.toString() + "\n===========================\n";
						game.textviewHandler.post(game.updateTextView);
					}  
					game.text += e.toString() + "\n" + content;
					game.textviewHandler.post(game.updateTextView);
					*/
				}
				
			} 
		}
		 
		// melt any garbage
		for (String groupId : temp.mGroups.keys())
		{
			try {
				temp.mBlocks.get(groupId).mAnimation.queueAnimation(Animation.MELTWRAPPER, null, null, 500,  0, null);
			}
			catch (Exception e){}
			
			for (String memberId : temp.mGroups.getGroupMembers(groupId))
			{
				temp.destroyBlock( memberId );
			}
			
			temp.mGroups.removeGroup(groupId);
			
		}
		
		
		temp.isPopulated = true;

	}
	
	
	
	
	
}
