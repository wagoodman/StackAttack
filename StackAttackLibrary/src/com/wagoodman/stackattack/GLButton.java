package com.wagoodman.stackattack;

import java.util.concurrent.Callable;
import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.ColorBroker;
import com.wagoodman.stackattack.ColorState;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TextTransform;
import com.wagoodman.stackattack.TextTransformEngine;
import com.wagoodman.stackattack.World;

import android.content.Context;


public class GLButton<Type> extends GLMenuItem
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
	
	private Boolean canDraw = true;

	public GLButton(Boolean draw, Context context, String fontName, Color fontColor, String[] labels, int x, int y, Boolean leftJust, float fontScale)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, labels[0], x, y, leftJust);	
		mLabels = labels;
		
		canDraw = draw;
	}
	
	public GLButton(Boolean draw, Context context, String fontName, Color fontColor, String label, int x, int y, Boolean leftJust, float fontScale)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, label, x, y, leftJust);	
		
		canDraw = draw;
	}
	
	public GLButton(Context context, String fontName, Color fontColor, String label, int x, int y, Boolean leftJust, float fontScale)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, label, x, y, leftJust);		
	}
	
	public GLButton(Context context, String fontName, Color fontColor, String[] labels, int x, int y, Boolean leftJust, float fontScale)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, labels[0], x, y, leftJust);		
		mLabels = labels;
	}
	
	public GLButton(Context context, String fontName, Color fontColor, String label, int x, int y, Boolean leftJust, float fontScale, Callable<Type> action, Boolean clickable)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, label, x, y, leftJust);
		initButton(action);
		
		isClickable = clickable;
	}
	
	public GLButton(Context context, String fontName, Color fontColor, String[] labels, int x, int y, Boolean leftJust, float fontScale, Callable<Type> action, Boolean clickable)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, labels[0], x, y, leftJust);
		initButton(action);
		mLabels = labels;
		
		isClickable = clickable;
	}

	public GLButton(Context context, String fontName, Color fontColor, String label, int x, int y, Boolean leftJust, float fontScale, Callable<Type> action)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, label, x, y, leftJust);
		initButton(action);
		
	}
	
	public GLButton(Context context, String fontName, Color fontColor, String[] labels, int x, int y, Boolean leftJust, float fontScale, Callable<Type> action)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, labels[0], x, y, leftJust);
		initButton(action);
		mLabels = labels;
		
	}
	
	public GLButton(Context context, String fontName, Color fontColor, String label, int x, int y, Boolean leftJust, float fontScale, Callable<Type> action, Callable<Type> postAction, int delay)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, label, x, y, leftJust);
		initButton(action, postAction, delay);
		
	}
	
	public GLButton(Context context, String fontName, Color fontColor, String[] labels, int x, int y, Boolean leftJust, float fontScale, Callable<Type> action, Callable<Type> postAction, int delay)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mLabelEngine = new TextTransformEngine(mContext);
		
		mFontScaleOffset = fontScale;
		
		initLabel(fontName, fontColor, labels[0], x, y, leftJust);
		initButton(action, postAction, delay);
		mLabels = labels;
	}
	
	private Boolean mHint = false;
	private Boolean mOnPeriod = false;
	private Boolean mForever = false;
	private long mStartHint = -1;
	private int mHintWait = -1;
	private Color mHintColor = Color.YELLOW;
	
	public void setHint(int waitTime, Boolean onPeriod, Boolean forever, Color hintColor)
	{
		mHint = true;
		
		mOnPeriod = onPeriod;
		mHintWait = waitTime;
		mHintColor = hintColor;
		mForever = forever;
		resetHintTime();
	}
	
	public void startHinting()
	{	if (mHint)
		{
			mLabelEngine.mForever = mForever;
			mLabelEngine.startTransform(mHintAnimation, mHintColor);
			if (mOnPeriod)
				resetHintTime();
			else
				mStartHint = -1;
		}
	}
	
	public void resetHintTime()
	{
		if (mHint)
			mStartHint = System.currentTimeMillis() + mHintWait;
	}
	
	// Button functionality
	
	private Boolean isClickable = true;
	private TextTransform mClickAnimation = TextTransform.Slinky;
	private TextTransform mHintAnimation = TextTransform.ColorHint;
	private Callable<Type> mAction;
	private int mPostActionDelay = 0;
	private Callable<Type> mPostAction;
	public Boolean	mClicked = false;
	private long mTriggerAction = -1;
	private long mTriggerPostAction = -1;
	
	
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
	
	
	// return true if there was an interaction with this object
	@Override
	public Boolean interact(int x, int y, int pixYOffset)
	{
		if (isWithinClickableArea(x, y, pixYOffset))
		{
			if (isClickable)
			{
				// do something
				mClicked = true;
				
				return true;
			}
		}
		
		return false;
	}
	
	public void click()
	{
		if (isClickable)
		{
			try
			{
				game.getVibratorHandler().post( game.getMenuSelectVibratorSequence() );	
			}
			catch (Exception e)
			{
				//Log.e("Vibrator", "Could not vibrate! " + e.toString());
			}
			
			// Animate click (flash)
			//flashColor(Color.YELLOW, 200);
		
			// stop hinting (for now)
			mStartHint = -1;
			
			// Animate click (Worm Animation)
			mLabelEngine.mForever = false;
			mLabelEngine.startTransform(mClickAnimation, Color.YELLOW);
			
			// trigger a service click for later
			mTriggerAction = System.currentTimeMillis() + mClickAnimation.getDuration();
			mTriggerPostAction = mTriggerAction + mPostActionDelay;
			
			// play menu click sound
			game.getWorld().playSound(World.SOUND_MENUCLICK);
		}
		// reset trigger
		mClicked = false;
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
	
	private Boolean mLabelLocked = false;
	
	private ColorBroker	mColor;
	public String mFontName = "";
	public String mLabel = "";
	private int labelIndex = 0;
	private String[] mLabels;
	private Boolean mLeftJust = true;
	private Boolean isDimensionsSet = false;
	private Double mAugmentHeight = 1.5;
	public float mFontScaleOffset = 1f;
	
	public TextTransform mIntroTransform = TextTransform.MoveDiagLeftIn;
	public TextTransform mOutroTransform = TextTransform.MoveDiagLeftOut;
	
	public TextTransformEngine	mLabelEngine;
	
	// Trigger Animations
	private long mTriggerIntro = -1;
	private long mTriggerOutro = -1;
	private long mTriggerClickAnimation = -1;
	
	private void initLabel(String fontName, Color fontColor, String label, int x, int y, Boolean leftJust)
	{

		setPos(x, y, leftJust);
		
		mFontName = fontName;
		mLabel = label;	
		
		mLeftJust = leftJust;
		
		mColor = new ColorBroker(fontColor);
		
	}

	
	public void lockLabel()
	{
		mLabelLocked = true;
	}
	
	public void unlockLabel()
	{
		mLabelLocked = false;
	}
	
	public Boolean isLabelLocked()
	{
		return mLabelLocked;
	}
	
	public void setLabel(String label)
	{
		setLabel(label, false);
	}
	
	public void setLabel(String label, Boolean force)
	{
		if (!mLabelLocked || force)
		{
			if (label != mLabel)
			{
				mLabel = label;
				
				// reset length to new value
				isDimensionsSet = false;
			}
		}
	}
	
	@Override
	public void setLabelDimensions()
	{

		
		setDimensions( 
				(int)((game.getWorld().mDropSection.mFonts.getStringWidth(mFontName, mLabel)*mFontScaleOffset)) , 
				(int)(game.getWorld().mDropSection.mFonts.getFontHeight(mFontName)*mAugmentHeight*mFontScaleOffset),	// make it just a little taller
				(int) game.getWorld().mScreenHeight
				);	
		
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

	public void triggerClickAnimation(int delay)
	{
		mTriggerClickAnimation = System.currentTimeMillis() + delay;
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
		//mLabelEngine.mTriggerVisible = true;
		mLabelEngine.clearHold(); // clear any holds
		mLabelEngine.startTransform(mIntroTransform);
		canDraw = true;
		
		resetHintTime();
		
	}
	
	@Override
	public void intro(Integer dur)
	{	
		//mLabelEngine.mTriggerVisible = true;
		mLabelEngine.clearHold(); // clear any holds
		mLabelEngine.startTransform(mIntroTransform, null, null, null, dur);
		canDraw = true;
		
		resetHintTime();
	}
	
	// starts stage exit animation
	@Override
	public void outro()
	{
		mLabelEngine.startTransform(mOutroTransform);
		mLabelEngine.setHold();	// to avoid menu flashes at the end of an outro 
		//mLabelEngine.mTriggerNotVisible = true;
	}

	@Override
	public void outro(Integer dur)
	{
		
		mLabelEngine.startTransform(mOutroTransform, null, null, null, dur);
		mLabelEngine.setHold();	// to avoid menu flashes at the end of an outro 
		//mLabelEngine.mTriggerNotVisible = true;
		
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
			game.text += "IsVisible   : " +  mLabelEngine.mIsVisible + "\n";
			game.text += "Pos	      : " +  mLabelEngine.mCharOffset[0] + "\n";
			game.textviewHandler.post(game.updateTextView);
			*/
			// Update button
			
			if (mClicked)
				click();
			
			
			
			// main action
			if (mTriggerAction != -1)
			{
				if (mTriggerAction <= now)
				{
					// service click
					Type ret = execute(mAction/*, true*/);
					
					// check if change in label is needed:
					if (mLabels != null && ret instanceof Boolean)
					{
						// toggle
						if ( (Boolean)ret == true)
							labelIndex = 0;
						else
							labelIndex = 1;
						
						setLabel(mLabels[labelIndex], true);
						
					}
					
					// reset trigger
					mTriggerAction = -1;
				}
			}
			
			// Animation Only
			if (mTriggerClickAnimation != -1)
			{
				if (mTriggerClickAnimation <= now)
				{
					// service click
					mLabelEngine.mForever = false;
					mLabelEngine.startTransform(mClickAnimation, Color.YELLOW);
					
					// reset trigger
					mTriggerClickAnimation = -1;
				}
			}
			
			
			
			// Hint animation
			if (mHint)
			{
				if (mStartHint != -1)
				{
					if (mStartHint <= now)
					{
						if (!mLabelEngine.isTransforming)
							startHinting();
					}
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
		}
		
		
	}
	
	private String getIntArrayValues(int[] Array)
	{
		String ret = "[ ";
		for(int i = 0; i < 1; i++) {
		   ret += String.valueOf(Array[i]) + " ";
		}
		
		return ret + " ]";
	}

	private String getFloatArrayValues(float[] Array)
	{
		String ret = "[ ";
		for(int i = 0; i < Array.length; i++) {
		   ret += String.valueOf(Array[i]) + " ";
		}
		
		return ret + " ]";
	}
	
	@Override
	void draw(GL10 gl, float pixYOffset)
	{
		//game.text += mLabel + "\n";
		//game.textviewHandler.post( game.updateTextView );
	

		// Draw Label
		
		
		// if not visible, dont draw!
		//if (!mLabelEngine.mIsVisible)
		//	return;
		
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		//game.text = System.currentTimeMillis() + "\n";
		//game.text = pixYOffset + "\n";
		//game.textviewHandler.post(game.updateTextView);
		

		// only draw if there has been an intro
		if (canDraw)
		{
			if (mLabelEngine.isTransforming)
			{
				/*
				game.text += mLabel + "\n";
				game.text += "mFontCharWidth: " + getIntArrayValues(mLabelEngine.mFontCharWidth)+ "\n";
				game.text += "DimSet: " + isDimensionsSet + "\n";
				game.text += xPos + ", " + yPos + "\n";
				game.text += "yoff: " + pixYOffset + "\n";
				game.text += "mCharOffset: " + getFloatArrayValues(mLabelEngine.mCharOffset) + "\n";
				game.text += "mXPixOffset: " + mLabelEngine.mXPixOffset + "\n";
				game.text += "mYPixOffset: " + mLabelEngine.mYPixOffset + "\n\n";
				game.textviewHandler.post( game.updateTextView );
				*/
				
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
				/*
				game.text += mLabel + "\n";
				game.text += "FONT: " + mFontName + "\n";
				game.text += "mFontCharWidth: " + getIntArrayValues(mLabelEngine.mFontCharWidth)+ "\n";
				game.text += "DimSet: " + isDimensionsSet + "\n"; 
				game.text += xPos + ", " + yPos + "\n";
				game.text += "yoff: " + pixYOffset + "\n\n";
				game.text += "color: " + getFloatArrayValues(mColor.mCurrentColorAmbient) + "\n";
				game.text += "mFontScaleOffset: " + mFontScaleOffset + "\n";
				
				game.textviewHandler.post( game.updateTextView );
				*/
				
				game.getWorld().mDropSection.mFonts.printAt(mFontName, gl, mLabel , 
						xPos, 				// X
						yPos + pixYOffset,	// Y
						mColor.mCurrentColorAmbient,
						mLeftJust,
						mFontScaleOffset
						);
			}
		}

		//game.text += mLabel + " :  yPos>" + yPos + " pixYOffset>" + pixYOffset + " += " + (yPos + pixYOffset) + "\n";
	
		gl.glDisable(GL10.GL_BLEND);
		
	}


	
}
