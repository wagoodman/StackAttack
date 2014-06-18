package com.wagoodman.stackattack;

import java.util.concurrent.CopyOnWriteArrayList;

import com.wagoodman.stackattack.BoardBackdrop;


import android.content.Context;

/**
 * Defines a shape made of multiple entities, where each entity is a face of the shape.
 * The shape is a regular polygon defined by the number of entities (numFaces)
 * 
 * @author alex
 *
 */

public class VirtualShape extends Identity
{

	protected final MainActivity game;
	protected final Context mContext;
	
	public BoardBackdrop mBackgroundBlock;	
	
	// GL World relations for virtual shape
	// ------------------------------------
	
	public 	float mVirtualShapeCenterDepth;
	public 	float mVirtualShapeHorizontalApothem;
	protected float mVirtualShapeBoarder;
	public 	static final float mBoardDepthRatio = 1f/3f;
	public 	static final float mBackdropDepthRatio = 1f/2f;
	public 	float mBoardDepth; 
	public	float mBackdropDepth;
	
	
	
	
	// Animation info used by world manipulator
	// ----------------------------------------
	protected int numFaces = 0;
	public double mCurrentFace = 0;
	//make protected
	public CopyOnWriteArrayList<Integer> mCurrentFaces = new CopyOnWriteArrayList<Integer>();
	
	public float mDelimitingDegree = 90; // 4 faces by default
	
	// The current degree applied to rotate to the correct face (facilitates transitions)... need EQ
	public float mCurrentHorizontalDegreeOffset = 0; // RL:add and subtract as manipulated by user
	public float mCurrentVerticalDegreeOffset = 0;   // UD:
	public float mCurrentVerticalOffset = 0;   // UD:
	
	// Animation Info
	public Boolean isTransformingHorizontal = false;
	public Boolean isTransformingVertical = false;
	public Boolean mHorizontalTransformingEnabled = true;
	private long mStartTime = System.currentTimeMillis();
	private float mHorizStartPoint = 0;
	private float mHorizEndPoint = 0;
	private float mVertStartPoint = 0;
	private float mVertEndPoint = 0;
	private float mStartTransPoint = 0;
	private float mEndTransPoint = 0;
	private static final int DEFAULT_HORIZ_DURATION = 300;
	private int mHorizontalDuration = DEFAULT_HORIZ_DURATION;
	private static final int DEFAULT_VERT_DURATION = 1000;
	private static final MotionEquation DEFAULT_VERT_EQ = MotionEquation.SPRING_UNDAMP_P6;
	protected int mVerticalDuration = DEFAULT_VERT_DURATION;
	protected MotionEquation mVertMotionEq = MotionEquation.SPRING_UNDAMP_P6;
	
	protected Boolean isLocked = false;
	
	public VirtualShape(Context ctxt)
	{
		// get the game object from context
		mContext = ctxt;
		game = (MainActivity) (mContext);
	}

	
	public void lock()
	{
		isLocked = true;
	}
	
	public void unlock()
	{
		isLocked = false;
	}
	


	
	protected void updateFaces(int faces)
	{
		numFaces = faces;
		
		// choose between 45 - 180 degrees
		/*
		updateFaces(
				Math.max( 45f , 
					Math.min( 180f ,
						(360f/(float) numFaces)
					)
				)
			);
			*/
		
		// choose between 45 - 180 degrees
		updateFaces(
			360f/(float) numFaces
			);
		
	}
	
	
	protected void updateFaces(float degreeDelimiter)
	{
		// set degree between each face to 360/# of faces ; allow delta degree between 30-90 only
		//mDelimitingDegree = Math.min(90, Math.max(30, degreeDelimiter ));
		
		//mDelimitingDegree = degreeDelimiter;
		
		mDelimitingDegree = 60;

		//mDelimitingDegree = 30;
		
		setDimensions();
	}
	
	
	// Virtual Shape Dimensions & Board Depth 
	public void setDimensions()
	{
		mBoardDepth = -1*((mBoardDepthRatio * (World.mMaxDepth - World.mMinDepth)) + World.mMinDepth);
		
		mBackdropDepth = -1*((mBackdropDepthRatio * (World.mMaxDepth - World.mMinDepth)) + World.mMinDepth);
		
		mVirtualShapeHorizontalApothem = (float) (( game.getWorld().getPlaneDimensions(mBoardDepth)[0] * Math.cos(Math.PI/(float)(360/mDelimitingDegree)) )/(2*Math.sin(Math.PI/(float)(360/mDelimitingDegree))));
		
		// rotate within the shape
		mVirtualShapeCenterDepth = mBoardDepth - mVirtualShapeHorizontalApothem;


		// Used to space the edges of the shape from the faces (separate boards apart)
		//mVirtualShapeBoarder = (mVirtualShapeCenterDepth/CA_Game.COLCOUNT)/8f;
		mVirtualShapeBoarder = (mVirtualShapeCenterDepth/MainActivity.COLCOUNT)/2f;
		
		mBackgroundBlock = new BoardBackdrop(mContext);
		
	}
	
	
	
	public void tour()
	{
		//game.text = "Start Tour";
		//game.textviewHandler.post( game.updateTextView );
		
		// start at last board, go to the first one
		mHorizStartPoint = mDelimitingDegree * (numFaces - 1);
		mHorizEndPoint = 0;

		// set upright
		mCurrentVerticalDegreeOffset = 0;
		mCurrentVerticalOffset = 0;
			
		// every face takes 700 ms
		mHorizontalDuration = numFaces * 700;
		
		mStartTime = System.currentTimeMillis();
		isTransformingHorizontal = true;
		
	}
	
	
	
	
	
	protected void augmentHorizontalOffsetDegree(float deltaDegree)
	{
		
		//mCurrentHorizontalDegreeOffset = (mCurrentHorizontalDegreeOffset + deltaDegree)%360;
		
		
		if (!mHorizontalTransformingEnabled)
			deltaDegree /= 360;	// dont reset (since you dont know the direction)
		
		if (deltaDegree + mCurrentHorizontalDegreeOffset < 0)
			mCurrentHorizontalDegreeOffset += (deltaDegree + 360);
		else
			mCurrentHorizontalDegreeOffset += (deltaDegree);
		
	}
	

	
	public void transitionLeft()
	{
		if (isLocked)
			return;
		
		if (numFaces > 1)
			startHorizontal(-1, false);	// commit
		else
			startHorizontal(1, true);	// go back to center
	}
	
	public void transitionRight()
	{
		if (isLocked)
			return;
		
		if (numFaces > 1)
			startHorizontal(1, false);	// commit
		else
			startHorizontal(-1, true);	// go back to center
	}
	
	public void transitionCenter()
	{	
		startHorizontal(0, true);
	}
		
	protected void startHorizontal(int dir, boolean force)
	{
		// dont transition when paused
		if (!mHorizontalTransformingEnabled && !force)
			return;
		
		if (isTransformingHorizontal)
			stopHorizontal();
		
		mHorizStartPoint = mCurrentHorizontalDegreeOffset;

		if (dir > 0)
		{
			mHorizEndPoint = mCurrentHorizontalDegreeOffset + ( mDelimitingDegree - (mCurrentHorizontalDegreeOffset%mDelimitingDegree));

		}
		else if (dir < 0)
		{
			mHorizEndPoint = mCurrentHorizontalDegreeOffset - (mCurrentHorizontalDegreeOffset%mDelimitingDegree);
		}
		else // dir==0
		{
			if ( Math.abs(mCurrentHorizontalDegreeOffset%mDelimitingDegree - mDelimitingDegree) < mCurrentHorizontalDegreeOffset%mDelimitingDegree)
				mHorizEndPoint = mCurrentHorizontalDegreeOffset - ( mCurrentHorizontalDegreeOffset%mDelimitingDegree - mDelimitingDegree);
			else
				mHorizEndPoint = mCurrentHorizontalDegreeOffset - (mCurrentHorizontalDegreeOffset%mDelimitingDegree);
		}
		
		// if the change in distance is 0, don't animate
		if (mHorizStartPoint != mHorizEndPoint)
		{			
			// choose time between 1-DEFAULT_DURATION
			//mDuration = (int) ( Math.min(Math.abs(mStartPoint - mEndPoint)/mDelimitingDegree, 1 ) *DEFAULT_DURATION);
			
			// every 45 degrees takes DEFAULT_DURATIONms
			mHorizontalDuration = (int) ( Math.min(Math.abs(mHorizStartPoint - mHorizEndPoint)/45, 1 ) * DEFAULT_HORIZ_DURATION);
			
			mStartTime = System.currentTimeMillis();
			isTransformingHorizontal = true;
		}
	}
	
	
	public void stopHorizontal()
	{
		isTransformingHorizontal = false;
	}
	
	/*
	public Boolean transitionDown()
	{
		mVerticalDuration = DEFAULT_VERT_DURATION;
		mVertMotionEq = DEFAULT_VERT_EQ;
		
		return startVertical(-1);	// commit
	}
	
	public Boolean transitionUp()
	{
		mVerticalDuration = DEFAULT_VERT_DURATION;
		mVertMotionEq = DEFAULT_VERT_EQ;
		
		return startVertical(1);	// commit
	}
	*/
	
	
	public Boolean transitionDown(MotionEquation eq, int duration)
	{
		mVerticalDuration = duration;
		mVertMotionEq = eq;
		
		return startVertical(-1);	// commit
	}
	
	public Boolean transitionUp(MotionEquation eq, int duration)
	{
		mVerticalDuration = duration;
		mVertMotionEq = eq;
		
		return startVertical(1);	// commit
	}
	
	protected Boolean startVertical(int dir)
	{
		
		if (isTransformingVertical)
		{
			//return false;
			// no! not relative/cancelable. Only absolute/complete
			// ...
			// just kidding!
			stopVertical();
		}
		
		mVertStartPoint = mCurrentVerticalDegreeOffset;
		mStartTransPoint = mCurrentVerticalOffset;

		if (dir > 0)
		{
			//mEndPoint = mCurrentVerticalDegreeOffset + mDelimitingDegree;
			mVertEndPoint = 0;
			mEndTransPoint = 0;
		}
		else if (dir < 0)
		{
			//mEndPoint = mCurrentVerticalDegreeOffset - mDelimitingDegree;
			
			//mVertEndPoint = -90;
			//mEndTransPoint = -game.getWorld().mGLBlockLength;
			
			mVertEndPoint = -75;
			mEndTransPoint = game.getWorld().mGLBlockLength*0.1f;
		}

		// if the change in distance is 0, don't animate
		if (mVertStartPoint != mVertEndPoint)
		{						
			mStartTime = System.currentTimeMillis();
			isTransformingVertical = true;
			
			return true;
		}
		
		return false;
	}
	
	
	public void stopVertical()
	{
		isTransformingVertical = false;
	}
	
	
	
	
	
	
	
	protected void updateVS(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		
		
		if (isTransformingHorizontal)
		{
			mCurrentHorizontalDegreeOffset = (float) MotionEquation.applyFinite(
					TransformType.TRANSLATE, 
					MotionEquation.LOGISTIC, 
					Math.max( 0, Math.min( now - mStartTime , mHorizontalDuration ) ), 
					mHorizontalDuration, 
					mHorizStartPoint, 
					mHorizEndPoint
					)%360;
			if (Math.max( 0, Math.min( now - mStartTime , mHorizontalDuration ) ) == mHorizontalDuration)
			{
				stopHorizontal();
				// prevent settling in the wrong place
				mCurrentHorizontalDegreeOffset = mHorizEndPoint%360;
			}
		}
		else if (isTransformingVertical)
		{
			mCurrentVerticalDegreeOffset = (float) MotionEquation.applyFinite(
					TransformType.ROTATE, 
					mVertMotionEq,
					//MotionEquation.SPRING_UNDAMP_P4,
					//MotionEquation.SPRING_UNDAMP_ALT_K4_5, 
					Math.max( 0, Math.min( now - mStartTime , mVerticalDuration ) ), 
					mVerticalDuration, 
					mVertStartPoint, 
					mVertEndPoint
					)%360;
			mCurrentVerticalOffset = (float) MotionEquation.applyFinite(
					TransformType.TRANSLATE, 
					mVertMotionEq,
					Math.max( 0, Math.min( now - mStartTime , mVerticalDuration ) ), 
					mVerticalDuration, 
					mStartTransPoint, 
					mEndTransPoint
					);
			if (Math.max( 0, Math.min( now - mStartTime , mVerticalDuration ) ) == mVerticalDuration)
			{
				stopVertical();
				// prevent settling in the wrong place
				mCurrentVerticalDegreeOffset = mVertEndPoint%360;
				mCurrentVerticalOffset = mEndTransPoint;
			}
		}
		
		//game.text = String.valueOf(mCurrentDegreeOffset) + " ---> S " + String.valueOf(mStartPoint) + ", E "+ String.valueOf(mEndPoint) + ", D "+ String.valueOf(mDuration) + ", ";
		//game.textviewHandler.post( game.updateTextView );
		
		
		// select current visible face
		mCurrentFace =  (mCurrentHorizontalDegreeOffset/mDelimitingDegree) == numFaces ? 0 :  (mCurrentHorizontalDegreeOffset/mDelimitingDegree)  ;
		
		//game.text = "Face: "+String.valueOf(mCurrentFace) +"\nDegree: "+ String.valueOf(mCurrentDegreeOffset) ;
		//game.textviewHandler.post( game.updateTextView );
		
		/*
		game.text = "";
		game.text += "Horizontal : " + mCurrentHorizontalDegreeOffset + "\n";
		game.text += "VerticalDeg: " + mCurrentVerticalDegreeOffset + "\n";
		game.text += "Vertical   : " + mCurrentVerticalOffset + "\n";
		game.textviewHandler.post( game.updateTextView );
		*/
		
		if (mBackgroundBlock != null)
			mBackgroundBlock.update(now, primaryThread, secondaryThread);
		
	}
	
	
	
}
