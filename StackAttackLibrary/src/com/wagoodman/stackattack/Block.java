package com.wagoodman.stackattack;

import java.text.DecimalFormat;
import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.World;

import android.content.Context;
//import android.util.Log;

public class Block extends GLShape
{
	// General
	private static final String TAG = "Block";
	private static final Boolean debug = false;
	private final MainActivity game;	
	
	public Block(Context context)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, World.BLOCKTYPES.REGULAR, null, 0, 0, isVisible, isInteractable);
	}

	
	public Block(Context context, Color color, int row, int col, Boolean visible, Boolean interact)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, World.BLOCKTYPES.REGULAR, color, row, col, visible, interact);

	}
	
	public Block(Context context, World.BLOCKTYPES type, Color color, int row, int col, Boolean visible, Boolean interact)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, type, color, row, col, visible, interact);

	}
	
	public Block(Context context, Color color, int row, int col)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, World.BLOCKTYPES.REGULAR, color, row, col, isVisible, isInteractable);

	}
	
	public Block(Context context, Color color)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, World.BLOCKTYPES.REGULAR, color, 0, 0, true, false);
	}
	
	public Block(Context context, Color color, Boolean visible, Boolean interact)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		init(context, World.BLOCKTYPES.REGULAR, color, 0, 0, visible, interact);
	}
	
	private void init(Context context, World.BLOCKTYPES type, Color color, int row, int col, Boolean visible, Boolean interact)
	{
		isVisible = visible;
		isInteractable = interact;
		
		mAnimation = new AnimationBroker(context, row, col);
		if (color != null)
			mColor = new ColorBroker(color);
		else
			mColor = new ColorBroker();
		
		mBlockType = type;
		
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
			//if (!isVisible)
			//	isVisible = true;
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
	
	private final float[][] nullFloatArray = new float[][]{new float[]{0,0,0},new float[]{0,0,0},new float[]{0,0,0}};
	
	public void draw(GL10 gl)
	{
		draw(gl, nullFloatArray );
	}
	
	public void draw(GL10 gl, float[][] offset)
	{
		if (isVisible)
		{
			// Translate
			gl.glTranslatef( 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.X] + offset[AnimationBroker.POS][AnimationBroker.X], 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.Y] + offset[AnimationBroker.POS][AnimationBroker.Y], 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.Z] + offset[AnimationBroker.POS][AnimationBroker.Z] 
							);
			
			
			// Rotate
			gl.glRotatef( mAnimation.mCurrentPoints[AnimationBroker.ROT][AnimationBroker.X] + offset[AnimationBroker.ROT][AnimationBroker.X], 1, 0, 0);
			gl.glRotatef( mAnimation.mCurrentPoints[AnimationBroker.ROT][AnimationBroker.Y] + offset[AnimationBroker.ROT][AnimationBroker.Y], 0, 1, 0);
			gl.glRotatef( mAnimation.mCurrentPoints[AnimationBroker.ROT][AnimationBroker.Z] + offset[AnimationBroker.ROT][AnimationBroker.Z], 0, 0, 1);
			
			// Scale
			gl.glScalef( 
					mAnimation.mCurrentPoints[AnimationBroker.SIZ][AnimationBroker.X] + offset[AnimationBroker.SIZ][AnimationBroker.X], 
					mAnimation.mCurrentPoints[AnimationBroker.SIZ][AnimationBroker.Y] + offset[AnimationBroker.SIZ][AnimationBroker.Y], 
					mAnimation.mCurrentPoints[AnimationBroker.SIZ][AnimationBroker.Z] + offset[AnimationBroker.SIZ][AnimationBroker.Z]
							);
			
			// color
		
			
			gl.glEnable(GL10.GL_DEPTH_TEST);
			
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
			
			// Set color/lighting
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, mColor.mCurrentColorAmbient, 0);
			gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, mColor.mCurrentColorDiffuse, 0);
			
			
			if (mBlockType == World.BLOCKTYPES.REGULAR)
			{
				// eventually
				//game.world.mBlock.draw(gl, mBlockValue.ordinal());
				
				if(mBlockValue == BlockValue.NORMAL)
					game.getWorld().mBlock.draw(gl, null );
				else
					game.getWorld().mBlock.draw(gl, mBlockValue.ordinal() );
				
			}
			else if (mBlockType == World.BLOCKTYPES.GROUPWRAPPER)
			{
				game.getWorld().mWrapperBlock.draw(gl, null);
			}
			
		}
	}
	
}
