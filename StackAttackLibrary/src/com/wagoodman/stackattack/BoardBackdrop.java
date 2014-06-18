package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;



import android.content.Context;


public class BoardBackdrop extends GLShape
{
	// General
	private static final String TAG = "BoardBackdrop";
	private static final Boolean debug = false;
	private final MainActivity game;
	
	private Color DEFAULT_COLOR = Color.BACKGROUND_DEFAULT;
	
	public Color mCurrentSteadyStateColor = DEFAULT_COLOR;
	
	public BoardBackdrop(Context context)
	{
		// get the game object from context
		game = (MainActivity) (context);
		
		// set block attributes
		isVisible = true;
		isInteractable = false;
		
		mAnimation = new AnimationBroker(context, 0, 0, false);
		
		// TEMP TEMP TEMP 
		//mAnimation.queueAnimation(Animation.WOBBLE, null, null, null, null, null);
		
		mColor = new ColorBroker(mCurrentSteadyStateColor);
		
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
	
	public void changeBackgroundColor(Color color, int duration)
	{
		mCurrentSteadyStateColor = color;
		
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
						mColor.mCurrentColor, 
						mCurrentSteadyStateColor,
						new int[] {duration, duration, duration, duration},
						new int[] {0,0,0,0}	// relative to FIFO q
						));	
	}
	
	
	public void flash(Color color, int flashDelim)
	{
		// cancel other color transforms
		mColor.cancelColorTransitionsAndSet(mCurrentSteadyStateColor);
		
		
		// Flash
		
		// comment out to start as red!
		/*
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
						mCurrentSteadyStateColor,  
						color,
						new int[] {flashDelim, flashDelim, flashDelim, flashDelim},
						new int[] {0,0,0,0}	// relative to FIFO q
						));	
		 */
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
						color,
						mCurrentSteadyStateColor, 
						new int[] {flashDelim, flashDelim, flashDelim, flashDelim},
						new int[] {0,0,0,0}	// relative to FIFO q
						));	
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
						mCurrentSteadyStateColor, 
						color,
						new int[] {flashDelim, flashDelim, flashDelim, flashDelim},
						new int[] {0,0,0,0}	// relative to FIFO q
						));	
		mColor.enqueue( 
				new ColorState( 
						new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
						color,
						mCurrentSteadyStateColor, 
						new int[] {flashDelim, flashDelim, flashDelim, flashDelim},
						new int[] {0,0,0,0}	// relative to FIFO q
						));
	}
	
	
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
	
	// do not use
	public void draw(GL10 gl, float[][] offset) {}
	
	
	public void draw(GL10 gl)
	{
		if (isVisible)
		{


			//gl.glLoadIdentity();
			//gl.glTranslatef(0, 0, -World.mMaxDepth);
			
			// Translate
			/*
			gl.glTranslatef( 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.X], 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.Y], 
					mAnimation.mCurrentPoints[AnimationBroker.POS][AnimationBroker.Z] 
							);
			*/
			
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
			
			// draw it!
			game.getWorld().mBackgroundBlock.draw(gl, null);

			
		}
	}
}
