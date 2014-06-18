package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.FontManager;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TransformType;
import com.wagoodman.stackattack.World;

import android.content.Context;
import android.graphics.BitmapFactory;


public class DropSection
{
	private final MainActivity game;
	private final Context mContext;
	
	
	// Current state
	public DropSectionState	mDropState = DropSectionState.FOLDED;
	
	
	// Supporting objects
	private StaticMenuBackground mMenuBackdrop;
	//private GLBottomRect 	mMenuBackdrop;
	private GLBottomRect 	mScoreBackdrop;
	private GLBottomRect 	mScoreBorder;
	public FontManager		mFonts;
	public ScoreBanner		mScoreBanner;
	public FogScreen mStatsFogScreen;
	
	// Finger Interaction
	public Boolean  mActiveSection = false;
	
	// Transform
	
		// Indexes
		public static final int MENU_DRIVER = 0;
		public static final int SCOREBOARD_DRIVER = 1;
		public static final int[] animationDriverIndexes = {MENU_DRIVER, SCOREBOARD_DRIVER};
		
		// Defaults
		public static final int DEFAULT_DURATION = 1000;
		public static final MotionEquation DEFAULT_MOTION = MotionEquation.SPRING_UNDAMP_P6;
		public static final int ENDGAME_DURATION = 3500;
		public static final int SLOW_DURATION = 2000;
		public static final MotionEquation SLOW_EQ = MotionEquation.LOGISTIC;
		
		// Indexed, Two Animation Drivers
		private Boolean[] isTransforming = {false, false};
		private long[] 	mStartTime = {0,0};
		private int[]	mDuration = {DEFAULT_DURATION, DEFAULT_DURATION};
		private float[]	mStartPoint = {0,0};
		private float[]	mEndPoint = {0,0};
		private float	mPickupPercentage = 0;
		private MotionEquation[] mMotionEq = {DEFAULT_MOTION,DEFAULT_MOTION};
		public float[]	mBottomPercentage = {0,0};
		
		
	// Drawing Bases

	private float mGLYBase;
	public int   mYBase;
	public float mYFontBaseOffset;
	public float mXBorderOffset = 17;
	private float mBackdropGLPlaneHeight;
	public float mYPixFoldedHeight;
	
	// Boarder
	private float mBorderHeightProportion = 1f;
	private float mBorderHeight = 1f;
	
	// Drawing Offsets
	private float[] mGLYOffset = {0,0};
	private float[] mPixFontOffset = {0,0};
	public int   mPixBorderHeight = 0;
	
	public Boolean isLocked = false;
	
	public BoardSideStatusIndicator mLeftBoardIndicator;
	public BoardSideStatusIndicator mRightBoardIndicator;
	
	public DropSection(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;

		
		// add asset fonts to manager (dont load)
		mFonts = new FontManager(mContext);

		mScoreBanner = new ScoreBanner(mContext);
		mStatsFogScreen = new FogScreen(mContext);
		
	}
	
	public void lock()
	{
		isLocked = true;
	}
	
	public void unlock()
	{
		isLocked = false;
	}
	
	public void loadFonts(GL10 gl)
	{
		mFonts.loadFonts(gl);
	}
	
	// determines where the bottom of the score board is
	public void setDistances(GL10 gl)
	{

		DropSectionState.FOLDED.setHeight( (game.getWorld().mScreenHeight - game.getWorld().mScreenBlockLength*(MainActivity.ROWCOUNT))/game.getWorld().mScreenHeight );	
		DropSectionState.UNFOLDED.setHeight(0.55 - DropSectionState.FOLDED.getHeight()/2f);
		DropSectionState.PEEK.setHeight(0.4 - DropSectionState.FOLDED.getHeight()/2f);
		DropSectionState.PEEK_UNFOLDEDSCORE.setHeight( DropSectionState.PEEK.getHeight() );
		
		mXBorderOffset = game.getWorld().mScreenWidth*0.04f;
		
		mBackdropGLPlaneHeight = game.getWorld().getPlaneDimensions(World.mMinDepth)[1];
		
		// INVERSED: This is the bottom of the score board (on Y axis)
		mGLYBase = (float) ( mBackdropGLPlaneHeight/2.0f - (mBackdropGLPlaneHeight * DropSectionState.FOLDED.getHeight())/2.0f );
		mYBase = (int) (game.getWorld().mScreenHeight/2.0 - (game.getWorld().mScreenHeight* DropSectionState.FOLDED.getHeight())/2.0);
		// NOT INVERSED: This is the bottom of the score text (Y Axis)
		
		
		mYFontBaseOffset = (float) (game.getWorld().mScreenHeight * (1 - DropSectionState.FOLDED.getHeight()/4) );
		
		
		
		mBorderHeightProportion = 30;
		mBorderHeight = (mBackdropGLPlaneHeight/2.0f - mGLYBase)/mBorderHeightProportion;
		mPixBorderHeight = (int) ((game.getWorld().mScreenHeight/2.0 - mYBase)/mBorderHeightProportion);
		//game.text = "Height: " + mBorderHeight + "  of  " + mBackdropGLPlaneHeight + "\nBase: " + mGLYBase;
		//game.textviewHandler.post(game.updateTextView);
		
		
		//mYPixFoldedHeight = (float) (  (game.getWorld().mScreenHeight - game.getWorld().mScreenBlockLength*(CA_Game.ROWCOUNT) )/2 + 4*mPixBorderHeight );
		mYPixFoldedHeight = (float) (  (game.getWorld().mScreenHeight - game.getWorld().mScreenBlockLength*(MainActivity.ROWCOUNT) )/2f + 4f*mPixBorderHeight );
		
		DropSectionState.FULL.setHeight(1 + mPixBorderHeight/game.getWorld().mScreenHeight);
		
		
		/*
		game.text =  DropSectionState.FOLDED + " : " + DropSectionState.FOLDED.getHeight() + "\n";
		game.text += DropSectionState.UNFOLDED + " : " + DropSectionState.UNFOLDED.getHeight() + "\n";
		game.text += DropSectionState.FULL + " : " + DropSectionState.FULL.getHeight() + "\n";
		game.textviewHandler.post(game.updateTextView);
		*/
		// Menu Backdrop
		//mMenuBackdrop = new GLBottomRect(game.getWorld().mGLDist, CA_Game.COLCOUNT, CA_Game.ROWCOUNT, 0, 7f);
		mMenuBackdrop = new StaticMenuBackground(mContext);
		mMenuBackdrop.loadBackground(gl);
		
		// Board Indicators
		mLeftBoardIndicator = new BoardSideStatusIndicator(mContext, true);
		mLeftBoardIndicator.loadImage(gl);
		
		mRightBoardIndicator = new BoardSideStatusIndicator(mContext, false);
		mRightBoardIndicator.loadImage(gl);
		
		// Scoreboard backdrop
		mScoreBackdrop = new GLBottomRect(game.getWorld().mGLDist, MainActivity.COLCOUNT, mBackdropGLPlaneHeight*20, 0f, 35.5f);
		mScoreBorder = new GLBottomRect(game.getWorld().mGLDist, game.getWorld().mGLDist*mBorderHeightProportion, game.getWorld().mGLDist, MainActivity.COLCOUNT, mBackdropGLPlaneHeight, 0);
		// This will tell the BitmapFactory to not scale based on the device's pixel density:
	    BitmapFactory.Options opts = new BitmapFactory.Options();
	    //opts.inScaled = false;
		mScoreBackdrop.loadGLTexture(gl, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.stripe12to36) );
	    
		// stats backdrop load the texture
		mStatsFogScreen.loadImage(gl);
		
		mFonts.loadFonts(gl);
		
	}

	
	public void interact(int x, int y, int motion)
	{
		
		//game.text = mMenuItem.isWithinClickableArea(x, y) + "\n" + game.text;
		//game.textviewHandler.post(game.updateTextView);
		
		//game.text += "DROP INTERACT\n";
		//game.textviewHandler.post(game.updateTextView);
		
		/*
		if (motion == 0)
			interact = mMenuButton.interact(x, y, (int) mPixFontOffset);
		*/
		
		// only on down!
		if (motion == 0)
		{
			game.getWorld().mMenus.interact(x, y, (int) mPixFontOffset[MENU_DRIVER]);
			game.getWorld().mTutorialMenu.interact(x, y, (int) mPixFontOffset[MENU_DRIVER]);
		}
		
		
		
		/*
		game.text = game.setme+"\n";
		game.text += x+", "+y+" : "+interact + "\n";
		game.text += mMenuButton.mWidth + ", " + mMenuButton.mHeight + "\n";
		game.textviewHandler.post(game.updateTextView);
		*/
		
		/*
		game.text = "Set? " + game.setme + "\n";
		game.text += x + " >= " + mMenuButton.mLeftPos  + "\n";
		game.text += x + " <= " + mMenuButton.mRightPos + "\n";
		game.text += (game.getWorld().mScreenHeight - y) + " <= " + (mMenuButton.mTopPos + (game.getWorld().mScreenHeight-(int)mPixFontOffset))    +" ("+ mMenuButton.mTopPos       + " + " + (game.getWorld().mScreenHeight-(int)mPixFontOffset) + ")\n";
		game.text += (game.getWorld().mScreenHeight - y) + " >= " + (mMenuButton.mBottomPos + (game.getWorld().mScreenHeight-(int)mPixFontOffset)) +" ("+ mMenuButton.mBottomPos + " + " + (game.getWorld().mScreenHeight-(int)mPixFontOffset) + ")\n";
		game.textviewHandler.post(game.updateTextView);
		 */
		
	}
	
	
	public Boolean isXYOnScoreBoard(float x, float y)
	{
		
		//game.text =  y +  " <= " +( game.getWorld().mScreenHeight*mYPercentRealestate/2.0) + "\n" + game.text;
		//game.textviewHandler.post(game.updateTextView);
		
		if (y <= (game.getWorld().mScreenHeight*DropSectionState.FOLDED.getHeight()/2.0) + mPixFontOffset[MENU_DRIVER] && y >= mPixFontOffset[MENU_DRIVER] )
			return true;
	
		return false;
	}
	
	public Boolean pickupSection(float x, float y)
	{
		if (isLocked)
			return false;
		
		// dont allow interaction if there is no game to play!
		if (!game.getIsGameOver())
		{
			if (!mActiveSection)
			{
				//game.text = "Pickup\n" + game.text;
				//game.textviewHandler.post(game.updateTextView);
				
				mPickupPercentage = mBottomPercentage[MENU_DRIVER];
				mActiveSection = true;
				dragSection(y);
				return true;
			}
		}
		
		return false;
	}
	
	public void dragSection(float y)
	{
		//game.text = "Called Drag\n";
		
		// dont allow interaction if there is no game to play!
		if (!game.getIsGameOver())
		{
			if (mActiveSection)
			{
				// if re-dragging, ignore previous transform queue item (follow the user!)
				if (isTransforming[MENU_DRIVER])
					stop();
				
				/*
				mBottomPercentage = (float) (
						Math.min(
							Math.max(
									y/game.getWorld().mScreenHeight, 
									DropSectionState.FOLDED.getHeight()/2
									),
									DropSectionState.UNFOLDED.getHeight()
								) -DropSectionState.FOLDED.getHeight()/2 );
				*/
				
				mBottomPercentage[MENU_DRIVER] = (float) (
							Math.max(
									y/game.getWorld().mScreenHeight, 
									DropSectionState.FOLDED.getHeight()/2
									) - DropSectionState.FOLDED.getHeight()/2 );
				
				//game.text += "Drag: "  + mBottomPercentage;// + "\n" + game.text;
				
			}
		}
		//game.textviewHandler.post(game.updateTextView);
	}
	
	public Boolean dropSection()
	{
		//game.text = "Drop...";
		
		if (mActiveSection)
		{
			
			if ( (mPickupPercentage-mBottomPercentage[MENU_DRIVER]) > 0 && Math.abs(mPickupPercentage-mBottomPercentage[MENU_DRIVER]) > DropSectionState.FOLDED.getHeight()/2f )
			{
				//game.text += "Fold/Resume";
				//game.textviewHandler.post(game.updateTextView);
				// fold/Resume
				mActiveSection = false;
				game.getWorld().resumeWorld(true);
				return true;
			}
			else if ( (mPickupPercentage-mBottomPercentage[MENU_DRIVER]) < 0 && Math.abs(mPickupPercentage-mBottomPercentage[MENU_DRIVER]) > DropSectionState.FOLDED.getHeight()/2f )
			{
				//game.text += "Unfold/Pause";
				//game.textviewHandler.post(game.updateTextView);
				// Unfold/Pause
				mActiveSection = false;
				game.getWorld().pauseWorld(true);
				return true;
			}
			else
			{
				//game.text += "Else...";
				
				// go back to previous position (closest)
				if (Math.abs(mBottomPercentage[MENU_DRIVER]-DropSectionState.UNFOLDED.getHeight()) < Math.abs(mBottomPercentage[MENU_DRIVER]-DropSectionState.FOLDED.getHeight()/2))
				{
					//game.text += "Unfold";
					// unfold
					changeStateTo(DropSectionState.UNFOLDED);
				}
				else
				{
					//game.text += "Fold";
					// fold
					changeStateTo(DropSectionState.FOLDED);
				}
				
				//game.textviewHandler.post(game.updateTextView);
				
				mActiveSection = false;
				return true;
			}

		}
		
		//game.textviewHandler.post(game.updateTextView);
		
		return false;
	}
	
	
	public Boolean isTransforming()
	{
		return isTransforming[MENU_DRIVER];
	}
	
	public Boolean changeStateTo(DropSectionState nextState)
	{
		return changeStateTo(nextState, null, null);
	}
	
	public Boolean changeStateTo(DropSectionState nextState, Integer duration, MotionEquation eq)
	{
		if (duration == null)
			duration = mDuration[MENU_DRIVER];
		if (eq == null)
			eq = mMotionEq[MENU_DRIVER];
			
		if (nextState == DropSectionState.FOLDED)
			foldMenu(duration, eq);
		else if (nextState == DropSectionState.PEEK )
			peekMenu(duration, eq, false);
		else if (nextState == DropSectionState.PEEK_UNFOLDEDSCORE)
			peekMenu(duration, eq, true);
		else if (nextState == DropSectionState.UNFOLDED)
			unfoldMenu(duration, eq);
		else if (nextState == DropSectionState.FULL)
			unfoldFullMenu(duration, eq);
		else
			return false;
		
		mDropState = nextState;
		
		return true;
	}
	

	
	private void unfoldFullMenu(int duration, MotionEquation eq)
	{
		//game.text = "Unfold Full";
		//game.textviewHandler.post(game.updateTextView);
		
		start( mBottomPercentage[MENU_DRIVER] , (float)DropSectionState.FULL.getHeight(), duration, eq );

		if (mBottomPercentage[SCOREBOARD_DRIVER] != 0)
		{
			//game.text += "...Reset";
			//game.textviewHandler.post(game.updateTextView);
			start( mBottomPercentage[SCOREBOARD_DRIVER] , 0, duration, eq, SCOREBOARD_DRIVER );
		}
	}
	
	private void unfoldMenu(int duration, MotionEquation eq)
	{
		//game.text = "Unfold";
		//game.textviewHandler.post(game.updateTextView);
		
		start( mBottomPercentage[MENU_DRIVER] , (float) DropSectionState.UNFOLDED.getHeight(), duration, eq  );

		if (mBottomPercentage[SCOREBOARD_DRIVER] != 0)
		{
			//game.text += "...Reset";
			//game.textviewHandler.post(game.updateTextView);
			start( mBottomPercentage[SCOREBOARD_DRIVER] , 0, duration, eq, SCOREBOARD_DRIVER );
		}
	}
	
	private void peekMenu(int duration, MotionEquation eq, Boolean unfoldScoreBoard)
	{
		//game.text = "Peek";
		//game.textviewHandler.post(game.updateTextView);
		
		start( mBottomPercentage[MENU_DRIVER] , (float) DropSectionState.PEEK.getHeight(), duration, eq  );
		
		if (unfoldScoreBoard)
		{
			//game.text += "///Unfold";
			//game.textviewHandler.post(game.updateTextView);
			start( mBottomPercentage[SCOREBOARD_DRIVER] , (float)( DropSectionState.FULL.getHeight() -  DropSectionState.PEEK.getHeight() /*-  DropSectionState.FOLDED.getHeight()/2*/  ), /*(int)(duration * 1.25)*/ (int)(duration * 1.85), eq, SCOREBOARD_DRIVER );
		}
		else
		{
			if (mBottomPercentage[SCOREBOARD_DRIVER] != 0)
			{
				//game.text += "...Reset";
				//game.textviewHandler.post(game.updateTextView);
				start( mBottomPercentage[SCOREBOARD_DRIVER] , 0, duration, eq, SCOREBOARD_DRIVER );
			}
		}
		
	}
	
	private void foldMenu(int duration, MotionEquation eq)
	{
		//game.text = "Fold";
		//game.textviewHandler.post(game.updateTextView);
		start(mBottomPercentage[MENU_DRIVER] , 0, duration, eq  );
		
		if (mBottomPercentage[SCOREBOARD_DRIVER] != 0)
		{
			//game.text += "...Reset";
			//game.textviewHandler.post(game.updateTextView);
			start( mBottomPercentage[SCOREBOARD_DRIVER] , 0, duration, eq, SCOREBOARD_DRIVER );
		}
	}
	
	public void start(float start, float end, int duration, MotionEquation eq)
	{
		mDuration[MENU_DRIVER] = duration;
		mMotionEq[MENU_DRIVER] = eq;
		
		mStartPoint[MENU_DRIVER] = start;
		mEndPoint[MENU_DRIVER] = end;
		mStartTime[MENU_DRIVER] = System.currentTimeMillis();
		isTransforming[MENU_DRIVER] = true;
	}
	
	public void start(float start, float end, int duration, MotionEquation eq, int idx)
	{
		mDuration[idx] = duration;
		mMotionEq[idx] = eq;
		
		mStartPoint[idx] = start;
		mEndPoint[idx] = end;
		mStartTime[idx] = System.currentTimeMillis();
		isTransforming[idx] = true;
	}

	
	public void stop()
	{
		isTransforming[MENU_DRIVER] = false;
		// reset vars
		mMotionEq[MENU_DRIVER] = DEFAULT_MOTION;
		mDuration[MENU_DRIVER] = DEFAULT_DURATION;
	}
	

	public void stop(int idx)
	{
		isTransforming[idx] = false;
		// reset vars
		mMotionEq[idx] = DEFAULT_MOTION;
		mDuration[idx] = DEFAULT_DURATION;
	}
	
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		//game.text += String.valueOf(now) + "\n";
		//game.text += isTransforming + "  " + mActiveBoard +"  "+mBottomPercentage + "  " +mStartPoint + "  " + mEndPoint+ "\n";
		
		/*
		game.text  = "Bottom   : " + mBottomPercentage + "\n";
		game.text += "   FOLD  : " + DropSectionState.FOLDED.getHeight() + "\n";
		game.text += "   UNFOLD: " + DropSectionState.UNFOLDED.getHeight() + "\n";
		game.text += "   FULL  : " + DropSectionState.FULL.getHeight() + "\n";
		game.textviewHandler.post(game.updateTextView);
		*/
		

		
		// Process position & motion  (if there is an animation)

		if (primaryThread)
		{
			for (int idx : animationDriverIndexes)
			{
				if (isTransforming[idx])
				{
					
					mBottomPercentage[idx] = (float) MotionEquation.applyFinite(
							TransformType.TRANSLATE, 
							//MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
							mMotionEq[idx],
							Math.max( 0, Math.min( now - mStartTime[idx] , mDuration[idx] ) ), 
							mDuration[idx], 
							mStartPoint[idx]*100, 
							mEndPoint[idx]*100
							)/100;
					if (Math.max( 0, Math.min( now - mStartTime[idx] , mDuration[idx] ) ) == mDuration[idx])
					{
						stop(idx);
						mBottomPercentage[idx] = mEndPoint[idx];
					}
					
				}
			
				mGLYOffset[idx] = mBottomPercentage[idx]*mBackdropGLPlaneHeight;
				mPixFontOffset[idx] = mBottomPercentage[idx]*game.getWorld().mScreenHeight;
			}
		}
		
		if (mBottomPercentage[MENU_DRIVER] != 0 || mBottomPercentage[SCOREBOARD_DRIVER] != 0 )
			game.getWorld().mMenus.update(now, primaryThread, secondaryThread);
		
		game.getWorld().mTutorialMenu.update(now, primaryThread, secondaryThread);
			
		// score banner
		mScoreBanner.update(now, primaryThread, secondaryThread);
		
		// side indicators
		mLeftBoardIndicator.update(now, primaryThread, secondaryThread);
		mRightBoardIndicator.update(now, primaryThread, secondaryThread);
		
		// stats backdrop
		mStatsFogScreen.update(now, primaryThread, secondaryThread);
		
		/*
		mMenuLabel.update(now);
		mMenuButton.update(now);
		*/
	}
	
	
	/*
	private void drawScoreboard(GL10 gl, float pixYOffset)
	{
		// Draw Scoreboard
	
		// dont move up with board!
		//gl.glLoadIdentity();
		//gl.glTranslatef(0, mGLYBase - mGLYOffset, -World.mMinDepth);
		
		// white needed for textures
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.WHITE.ambient(), 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.WHITE.diffuse(), 0);
		mScoreBackdrop.draw(gl);

		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glTranslatef(0, mGLYBase - mGLYOffset - mBorderHeight, -World.mMinDepth);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.RED_LIGHT.ambient(), 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.RED_LIGHT.diffuse(), 0);
		mScoreBorder.draw(gl);
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glTranslatef(0, (mBackdropGLPlaneHeight/2f) - mGLYOffset, -World.mMinDepth);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.RED_LIGHT.ambient(), 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.RED_LIGHT.diffuse(), 0);
		mScoreBorder.draw(gl);
		
		gl.glPopMatrix();
		
		// for text transparency
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// draw the score banner
		//game.getWorld().mBoards.mGameState.draw(gl, mXBorderOffset, mYFontBaseOffset, mPixFontOffset);
		mScoreBanner.draw(gl, pixYOffset);
		
		// don't enable transparency for everything
		gl.glDisable(GL10.GL_BLEND);
	}
	*/
	
	
	private void drawScoreboard(GL10 gl, float pixYOffset)
	{
		// Draw Scoreboard
	
		// dont move up with board!
		//gl.glLoadIdentity();
		//gl.glTranslatef(0, mGLYBase - mGLYOffset, -World.mMinDepth);
		
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
			// Backdrop
			gl.glPushMatrix();
				gl.glTranslatef(0, mGLYBase - (mGLYOffset[MENU_DRIVER] + mGLYOffset[SCOREBOARD_DRIVER]), -World.m2ndMinDepth);
				// white needed for textures
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.WHITE.ambient(), 0);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.WHITE.diffuse(), 0);
				mScoreBackdrop.draw(gl);
			gl.glPopMatrix();
			
			// Bottom-Bottom Border
			gl.glPushMatrix();
				gl.glTranslatef(0, mGLYBase - (mGLYOffset[MENU_DRIVER] + mGLYOffset[SCOREBOARD_DRIVER]) - mBorderHeight, -World.m2ndMinDepth);
				// white needed for textures
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.RED_LIGHT.ambient(), 0);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.RED_LIGHT.diffuse(), 0);
				mScoreBorder.draw(gl);
			gl.glPopMatrix();
			
			/*
			// Top-Bottom Border
			gl.glPushMatrix();
				gl.glTranslatef(0, mGLYBase - mGLYOffset[MENU_DRIVER] - mBorderHeight, -World.mMinDepth-World.m2ndMinDepth);
				// white needed for textures
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.RED_LIGHT.ambient(), 0);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.RED_LIGHT.diffuse(), 0);
				mScoreBorder.draw(gl);
			gl.glPopMatrix();
			*/
			
			// Top-Top Border
			gl.glPushMatrix();
				gl.glTranslatef(0, (mBackdropGLPlaneHeight/2f) - mGLYOffset[MENU_DRIVER] , -World.m2ndMinDepth);
				// white needed for textures
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.RED_LIGHT.ambient(), 0);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.RED_LIGHT.diffuse(), 0);
				mScoreBorder.draw(gl);
			gl.glPopMatrix();
			
			/*
			// Bottom-Top Border
			gl.glPushMatrix();
				gl.glTranslatef(0, (mBackdropGLPlaneHeight/2f) - (mGLYOffset[MENU_DRIVER] + mGLYOffset[SCOREBOARD_DRIVER]) , -World.mMinDepth-World.m2ndMinDepth);
				// white needed for textures
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.RED_LIGHT.ambient(), 0);
				gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.RED_LIGHT.diffuse(), 0);
				mScoreBorder.draw(gl);
			gl.glPopMatrix();
			*/
			// for text transparency
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			
			
			// Draw Stats Backdrop
			mStatsFogScreen.draw(gl);
			
			// draw the score banner
			//game.getWorld().mBoards.mGameState.draw(gl, mXBorderOffset, mYFontBaseOffset, mPixFontOffset);
			mScoreBanner.draw(gl, pixYOffset);
			
			// Tutorial Menu
			game.getWorld().mTutorialMenu.draw(gl, pixYOffset);
			
			// don't enable transparency for everything
			gl.glDisable(GL10.GL_BLEND);
		gl.glPopMatrix();
	}
	
	
	private void drawMenu(GL10 gl, float pixYOffset)
	{
		
		// dont move up with board!
		//gl.glLoadIdentity();
		//gl.glTranslatef(0, mGLYBase - mGLYOffset, -World.mMinDepth);
		
		// Draw Backdrop
		
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glTranslatef(0, ((mBackdropGLPlaneHeight/2f) - mGLYOffset[MENU_DRIVER]) + mBorderHeight, -World.mMinDepth);

		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Color.BLACKGREY.ambient(), 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Color.BLACKGREY.diffuse(), 0);
		//mMenuBackdrop.draw(gl);
		mMenuBackdrop.draw(gl, pixYOffset);
		
		game.getWorld().mMenus.draw(gl, pixYOffset);
		
		
		/*
		mMenuLabel.draw(gl, game.getWorld().mScreenHeight - mPixFontOffset);
		mMenuButton.draw(gl, game.getWorld().mScreenHeight - mPixFontOffset);
		*/
		gl.glPopMatrix();
		
		
	}
	
	public void draw(GL10 gl)
	{
		gl.glPushMatrix();
		
		// dont move up with board!
		gl.glLoadIdentity();
		gl.glTranslatef(0, mGLYBase - mGLYOffset[MENU_DRIVER], -World.m2ndMinDepth);

		//Board Indicators
		mLeftBoardIndicator.draw(gl);
		mRightBoardIndicator.draw(gl);
		
		// score board
		drawScoreboard(gl, game.getWorld().mScreenHeight - mPixFontOffset[MENU_DRIVER] + mPixBorderHeight);
		
		// menu
		if (mBottomPercentage[MENU_DRIVER] != 0)
			drawMenu(gl, game.getWorld().mScreenHeight - mPixFontOffset[MENU_DRIVER] + mPixBorderHeight);
		
		gl.glPopMatrix();
		 
	}

}
