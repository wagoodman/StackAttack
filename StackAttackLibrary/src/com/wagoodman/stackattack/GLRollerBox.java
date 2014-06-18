package com.wagoodman.stackattack;

import java.util.concurrent.Callable;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.ColorBroker;
import com.wagoodman.stackattack.ColorState;
import com.wagoodman.stackattack.FontManager;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TextTransform;
import com.wagoodman.stackattack.TextTransformEngine;
import com.wagoodman.stackattack.World;


import android.content.Context;


public class GLRollerBox<Type> extends GLMenuItem
{

	private final Context mContext;
	private final MainActivity game;

	/*

	// EXAMPLE USAGE

	mMenuButton = new GLButton<Void>(mContext, SCOREFONT, "This is a test!", 0, 0, 
					new Callable<Void>() {
				   public Void call() {
				        return game.setHim();
				   }
				}
			);

	 */
	
	
	private Integer mSetWidth = -1;
	
	public GLRollerBox(Context context, String fontName, Color fontColor,  int x, int y, Boolean leftJust, float fontScale, int setWidth, RollableItem item, Callable<Type> lAction, Callable<Type> rAction)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mSetWidth = setWidth;
		mRollerObj = item;
		mRollerValues = mRollerObj.getAllTitles();
		mStartValueIdx = item.getIndex();
		
		mFontScaleOffset = fontScale;
		
		mLeftAction = lAction;
		mRightAction = rAction;
		
		mLabelEngine = new TextTransformEngine(mContext);
		mTransitionEngine = new TextTransformEngine(mContext);
		mRightArrowEngine = new TextTransformEngine(mContext);
		mLeftArrowEngine = new TextTransformEngine(mContext);
		
		initLabel(fontName, fontColor, mRollerValues[0], x, y, leftJust);	
		
	}

	public GLRollerBox(Context context, String fontName, Color fontColor,  int x, int y, Boolean leftJust, float fontScale, int setWidth, RollableItem item, Callable<Type> lAction, Callable<Type> rAction, Callable<Type> action)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mSetWidth = setWidth;
		mRollerObj = item;
		mRollerValues = mRollerObj.getAllTitles();
		mStartValueIdx = item.getIndex();
		
		mFontScaleOffset = fontScale;
		
		mLeftAction = lAction;
		mRightAction = rAction;
		
		mLabelEngine = new TextTransformEngine(mContext);
		mTransitionEngine = new TextTransformEngine(mContext);
		mRightArrowEngine = new TextTransformEngine(mContext);
		mLeftArrowEngine = new TextTransformEngine(mContext);
		
		initLabel(fontName, fontColor, mRollerValues[0], x, y, leftJust);
		initButton(action);
		
	}
	
	public GLRollerBox(Context context, String fontName, Color fontColor,  int x, int y, Boolean leftJust, float fontScale, int setWidth, RollableItem item, Callable<Type> lAction, Callable<Type> rAction, Callable<Type> action, Callable<Type> postAction, int delay)
	{
		mContext = context;
		game = (MainActivity) mContext;
	
		mSetWidth = setWidth;
		mRollerObj = item;
		mRollerValues = mRollerObj.getAllTitles();
		mStartValueIdx = item.getIndex();
		
		mFontScaleOffset = fontScale;
		
		mLeftAction = lAction;
		mRightAction = rAction;
		
		mLabelEngine = new TextTransformEngine(mContext);
		mTransitionEngine = new TextTransformEngine(mContext);
		mRightArrowEngine = new TextTransformEngine(mContext);
		mLeftArrowEngine = new TextTransformEngine(mContext);
		
		initLabel(fontName, fontColor, mRollerValues[0], x, y, leftJust);
		initButton(action, postAction, delay);
		

	}
	

	
	
	// Button functionality
	
	private TextTransform mClickAnimation = TextTransform.Slinky;
	private Callable<Type> mAction;
	private int mPostActionDelay = 0;
	private Callable<Type> mPostAction;
	public Boolean	mClicked = false;
	private long mTriggerAction = -1;
	private long mTriggerPostAction = -1;
	
	
	// Roller Functionality
	private int mRollerManipulationLength = 0;
	public Boolean	mLeftClicked = false;
	public Boolean	mRightClicked = false;
	private TextTransform mRollInAnimation = TextTransform.RollInFromLeft; //TextTransform.RollInFromLeft;
	private TextTransform mRollOutAnimation = TextTransform.RollOutRight ; //TextTransform.RollOutRight;
	private String[] mRollerValues;
	private RollableItem mRollerObj;
	private Callable<Type> mLeftAction;
	private Callable<Type> mRightAction;
	private int mCurrentValueIdx = 0;
	private int mStartValueIdx = 0;

	
	private void initButton(Callable<Type> action)
	{
		setAction(action);
	}
	
	private void initButton(Callable<Type> action, Callable<Type> postAction, int delay)
	{
		setAction(action, postAction, delay);
	}
	
	public void setAction(Callable<Type> action)
	{
		mAction = action;
	}
	
	public void setAction(Callable<Type> action, Callable<Type> postAction, int delay)
	{
		mAction = action;
		mPostAction = postAction;
		mPostActionDelay = delay;
	}
	
	
	private Boolean isWithinLeftRollerManipulatorArea(int x, int y, int pixYOffset)
	{
		if (
				x >= (mLeftPos-mRollerManipulationLength) && 
				x <= mLeftPos &&
				(mScreenHeight - y) <= mTopPos + (mScreenHeight - pixYOffset) &&
				(mScreenHeight - y) >= mBottomPos + (mScreenHeight - pixYOffset)
			)
			
			return true;
		
		return false;
	}
	
	private Boolean isWithinRightRollerManipulatorArea(int x, int y, int pixYOffset)
	{
		if (
				x >= mRightPos && 
				x <= (mRightPos+mRollerManipulationLength) &&
				(mScreenHeight - y) <= mTopPos + (mScreenHeight - pixYOffset) &&
				(mScreenHeight - y) >= mBottomPos + (mScreenHeight - pixYOffset)
			)
			
			return true;
		
		return false;
	}
	
	// return true if there was an interaction with this object
	@Override
	public Boolean interact(int x, int y, int pixYOffset)
	{
		if (isWithinClickableArea(x, y, pixYOffset))
		{
			// do something
			mClicked = true;
			
			return true;
		}
		else if ( isWithinRightRollerManipulatorArea( x, y, pixYOffset) )
		{
			mRightClicked = true;
			
			return true;
		}
		else if ( isWithinLeftRollerManipulatorArea( x, y, pixYOffset) )
		{
			mLeftClicked = true;
			
			return true;
		}
		
		return false;
	}
	
	
	private void executeLeftClick()
	{
		try
		{
			game.getVibratorHandler().post( game.getMenuSelectVibratorSequence() );	
		}
		catch (Exception e)
		{
			//Log.e("Vibrator", "Could not vibrate! " + e.toString());
		}
		
		// show
		execute(mLeftAction);
		mCurrentValueIdx = mRollerObj.getPrev(mCurrentValueIdx);
		transitionToLabel(mRollerValues[mCurrentValueIdx]);
		mLabelEngine.startTransform(mRollInAnimation);
		mTransitionEngine.startTransform(mRollOutAnimation);
		// do
		//...
		
		// play menu click sound
		game.getWorld().playSound(World.SOUND_MENUCLICK);
		
		//game.text = "Left";
		//game.textviewHandler.post(game.updateTextView);
		
		mLeftClicked = false;
	}

	
	
	private void executeRightClick()
	{
		try
		{
			game.getVibratorHandler().post( game.getMenuSelectVibratorSequence() );	
		}
		catch (Exception e)
		{
			//Log.e("Vibrator", "Could not vibrate! " + e.toString());
		}
		
		// show...
		execute(mRightAction);
		mCurrentValueIdx = mRollerObj.getNext(mCurrentValueIdx);
		transitionToLabel(mRollerValues[mCurrentValueIdx]);
		mLabelEngine.startTransform(mRollInAnimation);
		mTransitionEngine.startTransform(mRollOutAnimation);
		// do
		//...
	
		// play menu click sound
		game.getWorld().playSound(World.SOUND_MENUCLICK);
		
		//game.text = "Right";
		//game.textviewHandler.post(game.updateTextView);
		
		mRightClicked = false;
	}
	
	
	public void mainClick()
	{
		// FOR NOW THIS IS NOT NEEDED, JUST RESET
		
		/*
		// Animate click (flash)
		//flashColor(Color.YELLOW, 200);
	
		// Animate click (Worm Animation)
		mLabelEngine.startTransform(mClickAnimation, Color.YELLOW);
		
		// trigger a service click for later
		mTriggerAction = System.currentTimeMillis() + mClickAnimation.getDuration();
		mTriggerPostAction = mTriggerAction + mPostActionDelay;
		*/
		
		// do next
		executeRightClick();
		
		// reset trigger
		mClicked = false;
		
		//game.text = "Main";
		//game.textviewHandler.post(game.updateTextView);
		
	}
	
	public Type execute(Callable<Type> action /*, Boolean print*/)
	{
	    
		try
		{
			return action.call();
		}
		catch (Exception e)
		{
			/*
			if (print)
			{
				game.text = "";
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
			}
			*/
			
			//Log.e("GLButton", e.toString());
		}
		
		return null;
	}

	
	
	
	
	
	
	
	// Label functionality
	
	private ColorBroker	mColor;
	public String mFontName = "";
	public String mLabel = "";
	public String mTransitionLabel = "";
	private Integer mTransitionWidth = 0;
	private Boolean mLeftJust = true;
	private Boolean isDimensionsSet = false;
	private Double mAugmentHeight = 1.5;
	private float mFontScaleOffset = 1f;
	
	public TextTransform mIntroTransform = TextTransform.MoveDiagLeftIn;
	public TextTransform mOutroTransform = TextTransform.MoveDiagLeftOut;
	
	public TextTransformEngine	mLabelEngine;
	
	public TextTransformEngine	mTransitionEngine;
	
	
	// Roller Arrows
	private String mArrowFontName = FontManager.ARROW_FONT;
	private TextTransformEngine	mRightArrowEngine;
	private TextTransformEngine	mLeftArrowEngine;
	private String mRightArrowChar = " d";
	private String mLeftArrowChar = "c ";
	private Boolean mRightArrowLeftJust = true;
	private Boolean mLeftArrowLeftJust = false;
	
	private Integer mRightArrowLeftXPos = 0;
	private Integer mLeftArrowRightXPos = 0;
	
	// Trigger Animations
	private long mTriggerIntro = -1;
	private long mTriggerOutro = -1;
	
	
	private void initLabel(String fontName, Color fontColor, String label, int x, int y, Boolean leftJust)
	{

		setPos(x, y, leftJust);
		
		mFontName = fontName;
		mLabel = label;	
		
		mLeftJust = leftJust;
		
		mColor = new ColorBroker(fontColor);
		
	}

	
	public void setLabel(String label)
	{
		this.mLabel = label;
		
		// reset length to new value
		isDimensionsSet = false;
	}
	
	public void transitionToLabel(String label)
	{
		mTransitionLabel = mLabel;	// transition away from current label
		mTransitionWidth = mWidth;
		mLabel = label;				// transition to a new label
		
		// reset length to new value
		isDimensionsSet = false;
	}

	
	
	@Override
	public void setLabelDimensions()
	{

		// (square)
		mRollerManipulationLength = (int)(game.getWorld().mDropSection.mFonts.getFontHeight(mFontName)*mAugmentHeight*1.5*mFontScaleOffset);
		
		// override width
		if (mSetWidth == -1)
			mSetWidth = (int)(game.getWorld().mDropSection.mFonts.getStringWidth(mFontName, mLabel)*mFontScaleOffset) ;
		
		setDimensions( 
				mSetWidth, 
				(int)(game.getWorld().mDropSection.mFonts.getFontHeight(mFontName)*mAugmentHeight*mFontScaleOffset),	// make it just a little taller
				(int) game.getWorld().mScreenHeight
				);
		
		if (mLeftJust)
		{
			mRightArrowLeftXPos = xPos+mWidth;
			mLeftArrowRightXPos = xPos;
		}
		else
		{
			mRightArrowLeftXPos = xPos;
			mLeftArrowRightXPos = xPos-mWidth;			
		}
		
		
		// dont re init this
		isDimensionsSet = true;
	}
	
	
	@Override
	public void setFontDimensions()
	{	
		
		mLabelEngine.setFontCharacteristics(
				game.getWorld().mDropSection.mFonts.getCharWidthArray(mFontName),
				game.getWorld().mDropSection.mFonts.getFirstCharOffset(mFontName)
				);

		mTransitionEngine.setFontCharacteristics(
				game.getWorld().mDropSection.mFonts.getCharWidthArray(mFontName),
				game.getWorld().mDropSection.mFonts.getFirstCharOffset(mFontName)
				);
		
		mRightArrowEngine.setFontCharacteristics(
				game.getWorld().mDropSection.mFonts.getCharWidthArray(mArrowFontName),
				game.getWorld().mDropSection.mFonts.getFirstCharOffset(mArrowFontName)
				);
		
		mLeftArrowEngine.setFontCharacteristics(
				game.getWorld().mDropSection.mFonts.getCharWidthArray(mArrowFontName),
				game.getWorld().mDropSection.mFonts.getFirstCharOffset(mArrowFontName)
				);
	}
	

	private void changeColor(Color color, int dur)
	{
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LOGISTIC, MotionEquation.LOGISTIC, MotionEquation.LOGISTIC, MotionEquation.LINEAR}, 
						mColor.mCurrentColor,
						color, 
						new int[] {dur, dur, dur, dur}, 
						new int[] {0,0,0,0}
						));	
	}

	private void flashColor(Color color, int dur)
	{
		mColor.cancelColorTransitionsAndSet(World.mMenuForegroundColor);
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
						mColor.mCurrentColor,
						color, 
						new int[] {(int) (dur/1.5),(int) (dur/1.5),(int) (dur/1.5),(int) (dur/1.5)}, 
						new int[] {0,0,0,0}
						));	
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LOGISTIC, MotionEquation.LOGISTIC, MotionEquation.LOGISTIC, MotionEquation.LINEAR}, 
						color,
						mColor.mCurrentColor,
						new int[] {dur, dur, dur, dur}, 
						new int[] {0,0,0,0}
						));	
	}
	
	
	
	@Override
	public void triggerIntro(int delay)
	{
		mTriggerIntro = System.currentTimeMillis() + delay;
	}
	
	@Override
	public void triggerOutro(int delay)
	{
		mTriggerOutro = System.currentTimeMillis() + delay;
	}
	
	
	// starts stage entrance animation
	@Override
	public void intro()
	{
		mRightArrowEngine.clearHold();
		mLeftArrowEngine.clearHold();
		mLabelEngine.clearHold();
		
		// reset roller value
		mCurrentValueIdx=mStartValueIdx;
		transitionToLabel(mRollerValues[mCurrentValueIdx]);
		
		//mLabelEngine.mTriggerVisible = true;
		mLabelEngine.startTransform(mIntroTransform);
		mLeftArrowEngine.startTransform( TextTransform.MoveInFromLeft );
		mRightArrowEngine.startTransform( TextTransform.MoveInFromRight );
		
	}
	
	@Override
	public void intro(Integer dur)
	{	
		mRightArrowEngine.clearHold();
		mLeftArrowEngine.clearHold();
		mLabelEngine.clearHold();
		
		// reset roller value
		mCurrentValueIdx=mStartValueIdx;
		transitionToLabel(mRollerValues[mCurrentValueIdx]);
		
		//mLabelEngine.mTriggerVisible = true;
		mLabelEngine.startTransform(mIntroTransform, null, null, null, dur);
		mLeftArrowEngine.startTransform( TextTransform.MoveInFromLeft, null, null, null, dur );
		mRightArrowEngine.startTransform( TextTransform.MoveInFromRight, null, null, null, dur );
		
	}
	
	// starts stage exit animation
	@Override
	public void outro()
	{
		mLabelEngine.startTransform(mOutroTransform);
		mLeftArrowEngine.startTransform( TextTransform.MoveOutLeft );
		mRightArrowEngine.startTransform( TextTransform.MoveOutRight );
		//mLabelEngine.mTriggerNotVisible = true;
		
		mRightArrowEngine.setHold();
		mLeftArrowEngine.setHold();
		mLabelEngine.setHold();
	}

	@Override
	public void outro(Integer dur)
	{
		mLabelEngine.startTransform(mOutroTransform, null, null, null, dur);
		mLeftArrowEngine.startTransform( TextTransform.MoveOutLeft, null, null, null, dur );
		mRightArrowEngine.startTransform( TextTransform.MoveOutRight, null, null, null, dur );
		//mLabelEngine.mTriggerNotVisible = true;
		
		mRightArrowEngine.setHold();
		mLeftArrowEngine.setHold();
		mLabelEngine.setHold();
	}
	



	/*
	//immediately hide menu item
	@Override
	public void hide()
	{
		mLabelEngine.mIsVisible = false;
	}
	*/
	
	@Override
	int getOutroDuration()
	{
		return mOutroTransform.getDuration();
	}
	
	@Override
	void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		if (secondaryThread)
		{
			
			if (!isDimensionsSet)
			{
				setLabelDimensions();
			}
			
			
			/*
			game.text  = now + "\n";
			game.text += "Transforming: " +  mLabelEngine.isTransforming + "\n";
			game.text += "Pos	      : " +  mLabelEngine.mCharOffset[0] + "\n";
			game.text += "\nRIGHT:\nTransforming: " +  mRightArrowEngine.isTransforming + "\n";
			game.text += "Pos	      : " +  mRightArrowEngine.mCharOffset[0] + "\n";
			game.text += "\nLEFT:\nTransforming: " +  mLeftArrowEngine.isTransforming + "\n";
			game.text += "Pos	      : " +  mLeftArrowEngine.mCharOffset[0] + "\n";
			game.textviewHandler.post(game.updateTextView);
			*/
			// Update button
	
			if (mLeftClicked)
				executeLeftClick();
			
			if (mRightClicked)
				executeRightClick();
			
			if (mClicked)
				mainClick();
	
			
			
			// main action
			if (mTriggerAction != -1)
			{
				if (mTriggerAction <= now)
				{
					// service click
					execute(mAction/*, true*/);
					
					// reset trigger
					mTriggerAction = -1;
				}
			}
			
			// post action
			if (mTriggerPostAction != -1)
			{
				if (mTriggerPostAction <= now)
				{
					// service click
					execute(mPostAction/*, false*/);
					
					// reset trigger
					mTriggerPostAction = -1;
				}
			}

			if (mTriggerIntro != -1)
			{
				if (mTriggerIntro <= now)
				{
					intro();
					
					// reset trigger
					mTriggerIntro = -1;
				}
			}
			
			if (mTriggerOutro != -1)
			{
				if (mTriggerOutro <= now)
				{
					outro();
					
					// reset trigger
					mTriggerOutro = -1;
				}
			}
			
		}
		
		if (primaryThread)
		{
			// Update Label
			
			// Process color
			if (!mColor.isEmpty())
				mColor.processColorElements(now);
		
			// Text Transform
			if (mLabelEngine.isTransforming)
			{
				
				// update char offsets
				mLabelEngine.update(now, mLabel, mLeftJust, mWidth);
	
			}
	
			// Text Transform
			if (mTransitionEngine.isTransforming)
			{
				
				// update char offsets
				mTransitionEngine.update(now, mTransitionLabel, mLeftJust, mTransitionWidth);
	
			}
			
			if (mRightArrowEngine.isTransforming)
			{
				
				// update char offsets
				mRightArrowEngine.update(now, mRightArrowChar, mRightArrowLeftJust, mRollerManipulationLength);
	
			}
			
			if (mLeftArrowEngine.isTransforming)
			{
				
				// update char offsets
				mLeftArrowEngine.update(now, mLeftArrowChar, mLeftArrowLeftJust, mRollerManipulationLength);
	
			}
		}
		
	}

	@Override
	void draw(GL10 gl, float pixYOffset)
	{
		
	
		// Draw Label
		
		// if not visible, dont draw!
		//if (!mLabelEngine.mIsVisible)
		//	return;
		
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		//game.text = System.currentTimeMillis() + "\n";
		//game.text = pixYOffset + "\n";
		//game.textviewHandler.post(game.updateTextView);
		

		if (mTransitionEngine.isTransforming)
		{
			
			// update char offsets
			synchronized(mTransitionEngine.mCharOffsetMutex)
			{

				game.getWorld().mDropSection.mFonts.printOffsetAt(mFontName, gl, mTransitionLabel , 
						xPos + 0f, 			// X
						yPos + pixYOffset,	// Y
						mColor.mCurrentColorAmbient,
						null,
						mLeftJust,
						mTransitionEngine.mCharOffset,
						mTransitionEngine.mXPixOffset,
						mTransitionEngine.mYPixOffset,
						mTransitionEngine.mCharWidthMod,
						mTransitionEngine.mCharHeightMod,
						mFontScaleOffset
						);
			}
			
		}
		
		
		if (mLabelEngine.isTransforming)
		{
			
			// update char offsets
			synchronized(mLabelEngine.mCharOffsetMutex)
			{
				if (mLabelEngine.mPeakColor != null)
					game.getWorld().mDropSection.mFonts.printOffsetAt(mFontName, gl, mLabel , 
							xPos + 0f, 			// X
							yPos + pixYOffset,	// Y
							mColor.mCurrentColorAmbient,
							mLabelEngine.mPeakColor.ambient(),
							mLeftJust,
							mLabelEngine.mCharOffset,
							mLabelEngine.mXPixOffset,
							mLabelEngine.mYPixOffset,
							mLabelEngine.mCharWidthMod,
							mLabelEngine.mCharHeightMod,
							mFontScaleOffset
							);
				else
					game.getWorld().mDropSection.mFonts.printOffsetAt(mFontName, gl, mLabel , 
							xPos + 0f, 			// X
							yPos + pixYOffset,	// Y
							mColor.mCurrentColorAmbient,
							null,
							mLeftJust,
							mLabelEngine.mCharOffset,
							mLabelEngine.mXPixOffset,
							mLabelEngine.mYPixOffset,
							mLabelEngine.mCharWidthMod,
							mLabelEngine.mCharHeightMod,
							mFontScaleOffset
							);
			}
			
		}
		else
		{
			game.getWorld().mDropSection.mFonts.printAt(mFontName, gl, mLabel , 
					xPos, 				// X
					yPos + pixYOffset,	// Y
					mColor.mCurrentColorAmbient,
					mLeftJust,
					mFontScaleOffset
					);

			
		}
		
		if (mRightArrowEngine.isTransforming)
		{
			
			synchronized(mRightArrowEngine.mCharOffsetMutex)
			{
				game.getWorld().mDropSection.mFonts.printOffsetAt(mArrowFontName, gl, mRightArrowChar , 
							mRightArrowLeftXPos, // X
							yPos + pixYOffset,	// Y
							mColor.mCurrentColorAmbient,
							null,
							mRightArrowLeftJust,
							mRightArrowEngine.mCharOffset,
							mRightArrowEngine.mXPixOffset,
							mRightArrowEngine.mYPixOffset,
							mRightArrowEngine.mCharWidthMod,
							mRightArrowEngine.mCharHeightMod,
							mFontScaleOffset*1.3f
							);
			}
		}
		else
		{
			game.getWorld().mDropSection.mFonts.printAt(mArrowFontName, gl, mRightArrowChar , 
					mRightArrowLeftXPos, // X
					yPos + pixYOffset,	// Y
					mColor.mCurrentColorAmbient,
					mRightArrowLeftJust,
					mFontScaleOffset*1.3f
					);
		}
		
		
		if (mLeftArrowEngine.isTransforming)
		{
			synchronized(mLeftArrowEngine.mCharOffsetMutex)
			{
				game.getWorld().mDropSection.mFonts.printOffsetAt(mArrowFontName, gl, mLeftArrowChar , 
							mLeftArrowRightXPos, // X
							yPos + pixYOffset,	// Y
							mColor.mCurrentColorAmbient,
							null,
							mLeftArrowLeftJust,
							mLeftArrowEngine.mCharOffset,
							mLeftArrowEngine.mXPixOffset,
							mLeftArrowEngine.mYPixOffset,
							mLeftArrowEngine.mCharWidthMod,
							mLeftArrowEngine.mCharHeightMod,
							mFontScaleOffset*1.3f
							);
			}
			
		}
		else
		{
			game.getWorld().mDropSection.mFonts.printAt(mArrowFontName, gl, mLeftArrowChar , 
					mLeftArrowRightXPos, // X
					yPos + pixYOffset,	// Y
					mColor.mCurrentColorAmbient,
					mLeftArrowLeftJust,
					mFontScaleOffset*1.3f
					);	
		}
	
		
	
		gl.glDisable(GL10.GL_BLEND);
		
	}


	
}
