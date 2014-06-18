package com.wagoodman.stackattack;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TransformType;
import com.wagoodman.stackattack.World;

import android.content.Context;

public class TextTransformEngine
{
	private final Context mContext;
	private final MainActivity game;
	
	private final int 		BUFFERLEN = 50;
	
	// General
	//public Boolean			mIsVisible = false;				// activated upon intro(), diabled upon outro() conclusion
	//public Boolean			mTriggerVisible = false;		// enables isvisible 
	//public Boolean			mTriggerNotVisible = false;		// disables isvisible upon stop
	private Boolean			mFontCharacteristicsSet = false;
	
	// Font info
	public int[] 			mFontCharWidth;
	public int				mFontFirstCharOffset;
	
	// Text Transform 
	public Boolean 			isTransforming = false;
	public Boolean			mReverseTransform = false;
	public long 			mStartTime = 0;
	public int				mDuration = 900;
	public float			mStartPoint = 0;	// always 0
	public float			mEndPoint = 1;		// always 1
	public float[]			mCharOffset = new float[ BUFFERLEN ];
	public float[]			mCharWidthMod = new float[ BUFFERLEN ];
	public float[]			mCharHeightMod = new float[ BUFFERLEN ];
	public Boolean			mModifyWidth = false;
	public Boolean			mModifyHeight = false;		
	
	public Boolean			mHoldTransformEndPoint = false;
	public Boolean			mForever = false;
	
	// Delta Transforms
	public Boolean 			isTransformingDelta = false;
	public Color			mPeakColor = Color.YELLOW;
	public float			mWordPercentage = 0;
	public int				mDeltaWidth;
	public int				mYPixOffset, mXPixOffset;
	
	// Logistic Transforms
	public Boolean 		isTransformingLogistic = false;
	public int			mLogisticWidth;
	
	// deliverable mutex
	public Object mCharOffsetMutex = new Object();
	
	
	
	public TextTransformEngine(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		resetTransformVars();
	}
	
	
	// called by owning entity when font information is loaded
	public void setFontCharacteristics(int[] charWidth, int firstCharOffset)
	{
		
		mFontCharWidth = charWidth;
		mFontFirstCharOffset = firstCharOffset;
		
		// get off the screen
		resetCharPos();
		
		mFontCharacteristicsSet = true;
	}
	
	public void resetCharPos()
	{
		// get off the screen
		for (int idx=0; idx < mCharOffset.length; idx++)
		{
			mCharOffset[idx] = 10000;
			scratchCharOffset[idx] = 10000;
			mCharWidthMod[idx] = 1;
			mCharHeightMod[idx] = 1;
		}
	}
	
	public void startXTransform(TextTransform transform, int x)
	{
		startTransform(transform, null, x, 0, null);
	}
	
	public void startYTransform(TextTransform transform, int y)
	{
		startTransform(transform, null, 0, y, null);
	}
	
	public void startTransform(TextTransform transform)
	{
		startTransform(transform, null, null, null, null);
	}
	
	public void startTransform(TextTransform transform, Color color)
	{
		startTransform(transform, color, null, null, null);
	}
	
	public void startTransform(TextTransform transform, Color frostedColor, Integer x, Integer y, Integer dur)
	{
		stop();
		
		// set duration
		if (dur == null)	mDuration = transform.getDuration();
		else				mDuration = dur;
		
		// set peak color (may be overridden)
		mPeakColor = frostedColor;
		
		// set other fields
		
		if (transform == TextTransform.Slinky)
		{
			mModifyWidth = true;
			mModifyHeight = false;
			
			
			mDeltaWidth = 5;
			if (x == null)	mXPixOffset = 24;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			isTransformingDelta = true;
		}
		if (transform == TextTransform.ColorHint)
		{
			mModifyWidth = false;
			mModifyHeight = false;
			
			
			mDeltaWidth = 5;
			if (x == null)	mXPixOffset = 0;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			isTransformingDelta = true;
		}
		else if (transform == TextTransform.Worm)
		{
			mModifyWidth = true;
			mModifyHeight = false;
			
			
			mDeltaWidth = 5;
			if (x == null)	mXPixOffset = 0;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 24;
			else			mYPixOffset = y;
			isTransformingDelta = true;
		}
		else if (transform == TextTransform.Flick)
		{
			mModifyWidth = true;
			mModifyHeight = false;
			
			
			mDeltaWidth = 5;
			if (x == null)	mXPixOffset = 24;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 24;
			else			mYPixOffset = y;
			isTransformingDelta = true;
		}
		else if (transform == TextTransform.DropFadeIn)
		{
			mModifyWidth = true;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = 0;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 50;
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mPeakColor = World.mMenuBackdropColor;
			mReverseTransform = true;
		}
		else if (transform == TextTransform.DropFadeOut)
		{
			mModifyWidth = true;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = 0;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = -50;
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mPeakColor = World.mMenuBackdropColor;
			mReverseTransform = false;
		}
		else if (transform == TextTransform.MoveInFromLeft)
		{
			mModifyWidth = false;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (-game.getWorld().mScreenWidth);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mReverseTransform = true;
		}
		
		else if (transform == TextTransform.MoveInFromRight)
		{
			mModifyWidth = false;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (game.getWorld().mScreenWidth);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mReverseTransform = true;
		}
		
		else if (transform == TextTransform.MoveOutRight)
		{
			mModifyWidth = false;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (game.getWorld().mScreenWidth);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			mLogisticWidth = 15;
			isTransformingLogistic = true;
			mReverseTransform = false;
		}
		
		else if (transform == TextTransform.MoveOutLeft)
		{
			mModifyWidth = false;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (-game.getWorld().mScreenWidth);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			mLogisticWidth = 15;
			isTransformingLogistic = true;
			mReverseTransform = false;
		}
		
		
		
/*
		else if (transform == TextTransform.ScrollOutRight)
		{
			mModifyWidth = false;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (game.getWorld().mScreenWidth);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			mLogisticWidth = 50;
			isTransformingLogistic = true;
			mReverseTransform = false;
		}
		else if (transform == TextTransform.ScrollOutLeft)
		{
			mModifyWidth = false;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (-game.getWorld().mScreenWidth);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 0;
			else			mYPixOffset = y;
			mLogisticWidth = 2;
			isTransformingLogistic = true;
			mReverseTransform = false;
		}
*/		
		else if (transform == TextTransform.MoveDiagLeftOut)
		{
			mModifyWidth = true;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (-game.getWorld().mScreenWidth*1.4);
			else			mXPixOffset = -x;
			if (y == null)	mYPixOffset = -(int) game.getWorld().mScreenHeight/3;
			else			mYPixOffset = -y;
			isTransformingLogistic = true;
			mLogisticWidth = 8;
			mReverseTransform = false;
		}
		else if (transform == TextTransform.MoveDiagLeftIn)
		{
			mModifyWidth = true;
			mModifyHeight = false;
			
			
			if (x == null)	mXPixOffset = (int) (game.getWorld().mScreenWidth*1.4);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = (int) game.getWorld().mScreenHeight/3;
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mLogisticWidth = 8;
			mReverseTransform = true;
		}

		else if (transform == TextTransform.VacuumInFromLeft)
		{
			mModifyWidth = true;
			mModifyHeight = true;
			
			
			if (x == null)	mXPixOffset = 50;//(int) (game.getWorld().mScreenWidth*1.2);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 10; //half font height
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mLogisticWidth = 8;
			mReverseTransform = true;	// do not change
		}
		else if (transform == TextTransform.VacuumOutRight)
		{
			mModifyWidth = true;
			mModifyHeight = true;
			
			
			if (x == null)	mXPixOffset = 50;//(int) (game.getWorld().mScreenWidth*1.2);
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 10; //half font height
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mLogisticWidth = 8;
			mReverseTransform = false;	// do not change
		}
		
		else if (transform == TextTransform.RollInFromLeft)
		{
			mModifyWidth = false;
			mModifyHeight = true;
			
			
			if (x == null)	mXPixOffset = 0;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 10; //half font height
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mLogisticWidth = 8;
			mReverseTransform = true;	// do not change
		}
		else if (transform == TextTransform.RollOutRight)
		{
			mModifyWidth = false;
			mModifyHeight = true;
			
			
			if (x == null)	mXPixOffset = 0;
			else			mXPixOffset = x;
			if (y == null)	mYPixOffset = 10; //half font height
			else			mYPixOffset = y;
			isTransformingLogistic = true;
			mLogisticWidth = 8;
			mReverseTransform = false;	// do not change
		}

		start();
	}

	
	public void resetTransformVars()
	{
		// Delta
		isTransformingDelta = false;
		mDeltaWidth = 5;
		
		// logistic
		isTransformingLogistic = false;
		mReverseTransform = false;
		mLogisticWidth = 16;
		
		// general
		mDuration = 900;
		mPeakColor = null;
		mStartPoint = 0;
		mEndPoint = 1;
		mYPixOffset = 0;
		mXPixOffset = 0;
		
		mModifyWidth = false;
		mModifyHeight = false;
		
		mHoldTransformEndPoint = false;
	}
	
	public void start()
	{
		/*
		if (mTriggerVisible)
		{
			mTriggerVisible = false;
			mIsVisible = true;
		}
		*/
		
		mStartTime = System.currentTimeMillis();
		isTransforming = true;
		
	}
	
	public void setHold()
	{
		mHoldTransformEndPoint = true;
	}
	
	public void clearHold()
	{
		mHoldTransformEndPoint = false;
	}
	
	private void hold()
	{
		if (mForever)
			start();
		else if (!mHoldTransformEndPoint)
			stop();
	}
	
	public void stop()
	{
		/*
		if (mTriggerNotVisible)
		{
			mTriggerNotVisible = false;
			mIsVisible = false;
		}
		*/
		
		isTransforming = false;
		resetTransformVars();
		
		// get off the screen
		resetCharPos();
	}
	
	
	
	
	// setup initial animation information
	// ...delta
	private float delta_width,delta_bottom,delta_top,delta_baseOffset,delta_offset,delta_xOffset,delta_charPercent;
	// ...logistic
	private float logistic_baseOffset, logistic_offset, logistic_xOffset, logistic_charPercent;
	// ...all
	private int delta_glyph, logistic_glyph, index, idx;
	private float[] scratchCharOffset = new float[ BUFFERLEN ];
	private float[] scratchCharWidthMod = new float[ BUFFERLEN ];
	private float[] scratchCharHeightMod = new float[ BUFFERLEN ];
	
	public void update(long now, String label, Boolean leftJust, int pixWordWidth)
	{
		if (isTransforming)
		{
			
			// get word percentage (% through animation)
			mWordPercentage = (float) MotionEquation.applyFinite(
					TransformType.TRANSLATE, 
					//MotionEquation.LOGISTIC,		// causes awkward delay due inherent to logistic function when used as a time dialator/controller
					MotionEquation.LINEAR,
					Math.max( 0, Math.min( now - mStartTime , mDuration ) ), 
					mDuration, 
					mStartPoint*100, 
					mEndPoint*100
					)/100;
			if (Math.max( 0, Math.min( now - mStartTime , mDuration ) ) == mDuration)
			{
				//stop();
				hold();
				mWordPercentage = mEndPoint;
			}
			
			// determine offsets
			if (mFontCharacteristicsSet)
			{
				if (isTransformingDelta )
				{
					
					// setup initial animation information
					delta_width = 2.1459660262893472396361835702f/(mDeltaWidth);
					delta_bottom = -1*(delta_width*mDeltaWidth);
					delta_top =  ((delta_width+1)*mDeltaWidth) + (delta_width*mDeltaWidth);
					delta_baseOffset = delta_bottom;
					delta_offset = delta_top*mWordPercentage;
					delta_xOffset = 0;
					delta_charPercent = 0;
					/*
					if (mWordPercentage > 0.54 && mWordPercentage < 0.59)
					{
						game.text  = "mWord%           : " + mWordPercentage + "\n";
						game.text += "mDeltaWidth      : " + mDeltaWidth + "\n";
						game.text += "delta_width      : " + delta_width + "\n";
						game.text += "delta_bottom     : " + delta_bottom + "\n";
						game.text += "delta_top        : " + delta_top + "\n";
						game.text += "delta_baseOffset : " + delta_baseOffset + "\n";
						game.text += "delta_offset     : " + delta_offset + "\n\n";
						
						//game.textviewHandler.post(game.updateTextView);
					}
					*/
					
					for(idx = 0 ; idx != label.length(); ++idx )
					{
						if (leftJust)
							index = idx;
						else
							index = label.length() - idx - 1;
						
						delta_glyph = (int)label.charAt(index) - mFontFirstCharOffset;
						
						
						/*
						if (mWordPercentage > 0.54 && mWordPercentage < 0.59)
						{
							game.text += "delta_charPercent    : " + delta_charPercent + "\n";
							//game.text += "delta_glyph          : " + delta_glyph + "\n";
							//game.text += "mFontFirstCharOffset : " + mFontFirstCharOffset + "\n";
							//game.text += "mFontCharWidth       : " + mFontCharWidth + "\n";
							//game.text += "pixWordWidth         : " + pixWordWidth + "\n";

							
							//game.textviewHandler.post(game.updateTextView);
						}
						*/
						
						
						//  figure and store percentOffset
						scratchCharOffset[index] = (float) Math.exp( -1*Math.pow( delta_baseOffset - mDeltaWidth*delta_charPercent + delta_offset, 2)   );
						

						if (mModifyWidth)
							scratchCharWidthMod[index] = scratchCharOffset[index]/2 + 1;
						else
							scratchCharWidthMod[index] = 1;
						
						if (mModifyHeight)
							scratchCharHeightMod[index] = 1 - scratchCharOffset[index];
						else
							scratchCharHeightMod[index] = 1;
						
						// get offset (for next char)
						delta_charPercent = (delta_xOffset + (mFontCharWidth[delta_glyph + mFontFirstCharOffset]))/(float)pixWordWidth;
						
						//game.text += mCharOffset[index] + "\n";//+ " " +  delta_baseOffset  + "  " + mDeltaWidth +"  " + delta_charPercent+ "  " + delta_offset + "  \n";
						
						
						// regulate width
						if (leftJust)
							delta_xOffset += mFontCharWidth[delta_glyph + mFontFirstCharOffset];
						else
							delta_xOffset -= mFontCharWidth[delta_glyph + mFontFirstCharOffset];
						
					}
					
					//game.textviewHandler.post(game.updateTextView);
	
				}
				else if (isTransformingLogistic)
				{
					
					// setup initial animation information
					logistic_baseOffset = 1;
					logistic_offset = 4f*mWordPercentage;
					logistic_xOffset = 0;
					logistic_charPercent = 0;
					
					for(idx = 0 ; idx != label.length(); ++idx )
					{
						if (leftJust)
							index = idx;
						else
							index = label.length() - idx - 1;
						
						logistic_glyph = (int)label.charAt(index) - mFontFirstCharOffset;
						
						
						
						//  figure and store percentOffset
						if (mReverseTransform)
							scratchCharOffset[index] = (float) ((2f*Math.exp(mLogisticWidth*(logistic_baseOffset + logistic_charPercent - 0.5f - logistic_offset))) / (2f + Math.exp(mLogisticWidth*(logistic_baseOffset + logistic_charPercent - 0.5f - logistic_offset))-1))*0.5f;
						else
							scratchCharOffset[index] = (float) ( 1 - ((2f*Math.exp(mLogisticWidth*(logistic_baseOffset + logistic_charPercent - 0.5f - logistic_offset))) / (2f + Math.exp(mLogisticWidth*(logistic_baseOffset + logistic_charPercent - 0.5f - logistic_offset))-1))*0.5f );
						 
						if (mModifyWidth)
							scratchCharWidthMod[index] = scratchCharOffset[index]/2 + 1;
						else
							scratchCharWidthMod[index] = 1;
						
						if (mModifyHeight)
							scratchCharHeightMod[index] = 1 - scratchCharOffset[index];
						else
							scratchCharHeightMod[index] = 1;
							
						// get char offset (for next char)
						//game.text = logistic_xOffset + "\n"+ " " +  mFontCharWidth  + "  " + logistic_glyph +"  " + mFontFirstCharOffset+ "  " + mWidth + "  \n";
						//game.textviewHandler.post(game.updateTextView);
						logistic_charPercent = (logistic_xOffset + (mFontCharWidth[logistic_glyph + mFontFirstCharOffset]))/(float)pixWordWidth;
						
						
						// regulate width
						if (leftJust)
							logistic_xOffset += mFontCharWidth[logistic_glyph + mFontFirstCharOffset];
						else
							logistic_xOffset -= mFontCharWidth[logistic_glyph + mFontFirstCharOffset];
						
					}
									
					
				}
				
				
				
			}// is dim set?

		
		
			// update char offsets
			mCharOffset = scratchCharOffset;
			mCharWidthMod = scratchCharWidthMod;
			mCharHeightMod = scratchCharHeightMod;
		
		}
		
	}
	
}
