package com.wagoodman.stackattack;

import java.text.DecimalFormat;
import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.AnimationBroker;
import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.ColorBroker;
import com.wagoodman.stackattack.GLShape;
import com.wagoodman.stackattack.MainActivity;

import android.content.Context;


public class GL3DText extends GLShape
{
	// General
	private static final String TAG = "Block";
	private static final Boolean debug = false;
	private final MainActivity game;
	
	
	public String mFontName = "";
	public String mLabel = "";
	private Boolean mLeftJust = true;
	private Boolean isDimensionsSet = false;
	private Double mAugmentHeight = 1.5;
	private float mFontScaleOffset = 1f;
	
	
	public GL3DText(Context context)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context,  null, 0, 0, isVisible, isInteractable, mFontScaleOffset);
	}
	
	public GL3DText(Context context,String fontName, String labelText, Boolean leftJust, Color color, int row, int col, float scale, Boolean visible, Boolean interact)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, color, row, col, visible, interact, scale);
		setLabel(fontName, labelText, leftJust);
	}
	
	public GL3DText(Context context,String fontName, String labelText, Boolean leftJust, Color color, int row, int col, float scale)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, color, row, col, isVisible, isInteractable, scale);
		setLabel(fontName, labelText, leftJust);
	}
	
	/*
	public GL3DText(Context context,String fontName, String labelText, Boolean leftJust, Color color)
	{
		// get the game object from context
		game = (StackAttack) (context);
		
		// set block attributes
		init(context, color, 0, 0, true, false);
		setLabel(fontName, labelText, leftJust);
	}
	
	public GL3DText(Context context,String fontName, String labelText, Boolean leftJust, Color color, Boolean visible, Boolean interact)
	{
		// get the game object from context
		game = (StackAttack) (context);
		
		// set block attributes
		init(context,color, 0, 0, visible, interact);
		setLabel(fontName, labelText, leftJust);
	}
	*/
	private void init(Context context, Color color, int row, int col, Boolean visible, Boolean interact, float scale)
	{
		isVisible = visible;
		isInteractable = interact;
		mFontScaleOffset = scale;
		
		//mAnimation = new AnimationBroker(context, row, col, true, true); // move & compensate for y offset upon addition
		mAnimation = new AnimationBroker(context, row, col, true);
		if (color != null)
			mColor = new ColorBroker(color);
		else
			mColor = new ColorBroker();
		
		
	}
	
	public void setLabel(String fontName, String labelText, Boolean leftJust, Double augHeight, float fontScaleOffset)
	{
		mFontName = fontName;
		mLabel = labelText;
		mLeftJust = leftJust;
		mAugmentHeight = augHeight;
		mFontScaleOffset = fontScaleOffset;	
	}
	
	public void setLabel(String fontName, String labelText, Boolean leftJust)
	{
		mFontName = fontName;
		mLabel = labelText;
		mLeftJust = leftJust;
	}
	
	public void setLabel(String labelText)
	{
		mLabel = labelText;	
	}
	
	/*
	@SuppressWarnings("unused")
	private void DEBUG(String logString)
	{
		if (debug == true) Log.d(TAG, logString);
	}
	
	@SuppressWarnings("unused")
	private void ERROR(String logString)
	{
		if (debug == true) Log.e(TAG, logString);
	}
	*/
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	// Thread Pool Here?
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		// Process color
		if (!mColor.isEmpty())
			mColor.processColorElements(now);
		
		/*
		float[] temp;
		DecimalFormat f = new DecimalFormat("###.00"); 
		temp = mColor.mCurrentColorAmbient;
		game.text  = String.format("AMBIENT:   R: %6s  G: %6s  B: %6s\n", f.format(temp[0]), f.format(temp[1]), f.format(temp[2]) );
		temp = mColor.mCurrentColorDiffuse;
		game.text += String.format("DIFFUSE:   R: %6s  G: %6s  B: %6s\n", f.format(temp[0]), f.format(temp[1]), f.format(temp[2]) );
		game.textviewHandler.post( game.updateTextView );
		*/

		
		// Process position & motion  (if there is an animation)
		if ( mAnimation.mAvailableIndexes.size() != AnimationBroker.QUEUESIZE )
		{
			mAnimation.processAnimationElements(now);
			
			// if there is an animation queued, and it is not visible, make visible after first update iteration
			if (!isVisible)
				isVisible = true;
		}

		/*
		float[] temp;
		DecimalFormat f = new DecimalFormat("###.00"); 
		temp = mAnimation.mCurrentPoints[0];
		game.text  = String.format("POS: %6s   %6s   %6s\n", f.format(temp[0]), f.format(temp[1]), f.format(temp[2]) );
		temp = mAnimation.mCurrentPoints[1];
		game.text  += String.format("ROT: %6s   %6s   %6s\n", f.format(temp[0]), f.format(temp[1]), f.format(temp[2]));
		temp = mAnimation.mCurrentPoints[2];
		game.text  += String.format("SCL: %6s   %6s   %6s\n", f.format(temp[0]), f.format(temp[1]), f.format(temp[2]));
		game.textviewHandler.post( game.updateTextView );
		*/
		
		// check for death
		if (mAnimation.mTimeOfDeath != -1 && !isDead)
		{
			// dying...
			if (isInteractable)
				isInteractable = false;
			
			if (mAnimation.mTimeOfDeath < System.currentTimeMillis())
			{
				isVisible = false;
				isDead = true;
			}
		
		}
		
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	// do not use
	public void draw(GL10 gl, float[][] offset) {}
	
	public void draw(GL10 gl)
	{
		if (isVisible)
		{
			// Translate
			gl.glTranslatef( 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.X], 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.Y], 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.Z] 
							);
			
			// Rotate
			gl.glRotatef( mAnimation.mCurrentPoints[AnimationBroker.ROT][AnimationBroker.X] , 1, 0, 0);
			gl.glRotatef( mAnimation.mCurrentPoints[AnimationBroker.ROT][AnimationBroker.Y] , 0, 1, 0);
			gl.glRotatef( mAnimation.mCurrentPoints[AnimationBroker.ROT][AnimationBroker.Z] , 0, 0, 1);
			
			// Scale
			gl.glScalef( 
					mAnimation.mCurrentPoints[AnimationBroker.SIZ][AnimationBroker.X], 
					mAnimation.mCurrentPoints[AnimationBroker.SIZ][AnimationBroker.Y], 
					mAnimation.mCurrentPoints[AnimationBroker.SIZ][AnimationBroker.Z] 
							);
			
			// color
		
			
			/*
			if (isSeeThru)
			{
				// transparent!
				
				gl.glEnable(GL10.GL_BLEND);
				gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
				
			}
			else
			{
				
				gl.glDisable(GL10.GL_BLEND);
			}
			*/
			// Set color/lighting
			//gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mColor.mCurrentColorAmbient, 0);
			//gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mColor.mCurrentColorDiffuse, 0);
			
			
			// DRAW FONT STRING
			game.getWorld().mDropSection.mFonts.print3D(mFontName, gl, mLabel, mColor.mCurrentColorAmbient, 0.003f*mFontScaleOffset, 0.003f*mFontScaleOffset);
		}
	}
	
}
