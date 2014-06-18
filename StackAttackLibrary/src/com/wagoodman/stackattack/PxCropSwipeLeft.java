package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;


import com.wagoodman.stackattack.Coord;
import com.wagoodman.stackattack.MainActivity;

import android.content.Context;

public class PxCropSwipeLeft implements PxBase
{
	private final Context mContext;
	private final MainActivity game;
	
	private PxImage mArrow;
	private PxImage mCircle;
	
	PxCropSwipeLeft(Context context, float circleX, float circleY, float arrowHeadX, float arrowHeadY, Boolean lockXMove, Boolean lockYMove, Boolean interactable)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		int circleWidth = 128, circleHeight = 128;
		float circleAbsScale = (float) ( (1f/circleWidth)*(game.getWorld().mScreenBlockLength) )*1.7f;
		
		int arrowWidth = 709, arrowHeight = 74;
		float arrowAbsScale = (1f/arrowWidth)*game.getWorld().mScreenWidth;
		
		mCircle = new PxImage(mContext,  
				R.drawable.fingercircle_w128h128, circleWidth, circleHeight, 
				circleAbsScale, 
				(-circleWidth/2f), (-circleHeight/2f),
				circleX, circleY,
				0,0,
				lockXMove, lockYMove, 
				interactable,		
				true,		// scale on interaction
				false		// crop on interaction
				);
		
		mArrow = new PxImage(mContext,  
				R.drawable.dotarrowleft_w700h74, arrowWidth, arrowHeight, 
				arrowAbsScale,
				0, (-arrowHeight/2f),
				arrowHeadX, arrowHeadY,
				circleX, circleY,
				lockXMove, lockYMove,
				interactable,		
				false,		// scale on interaction
				true		// crop on interaction
				);
		
	}
	
	@Override
	public void loadImage(GL10 gl) {
		mArrow.loadImage(gl);
		mCircle.loadImage(gl);
	}

	public void setFingerPos(Coord<Integer> coord)
	{
		float	x = (coord.getCol() + 0.5f)*game.getWorld().mScreenBlockLength,
				y = game.getWorld().mScreenHeight - (coord.getRow() + 0.5f)*game.getWorld().mScreenBlockLength;
		
		// move circle
		if (mCircle.mLockXDir == false)
		{
			if (mCircle.mXDriver.isTransforming)
				mCircle.mXDriver.stop();
			mCircle.mXDriver.mCurrent = x;
		}
		
		if (mCircle.mLockYDir == false)
		{
			if (mCircle.mYDriver.isTransforming)
				mCircle.mYDriver.stop();
			mCircle.mYDriver.mCurrent = y;
		}
		
		// move arrow
		mArrow.interact(x, y, 0, 1);
		
	}
	
	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount) {
		return pickup(x, y, pixYOffset, digitCount, false);
	}

	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount, Boolean force) {
		if(mCircle.pickup(x, y, pixYOffset, digitCount, force))
		{
			mArrow.pickup(x, y, pixYOffset, digitCount, force);
			return true;
		}
		return false;
	}

	@Override
	public Boolean interact(float x, float y, int pixYOffset, int digitCount) {
		if(mCircle.interact(x, y, pixYOffset, digitCount))
		{
			mArrow.interact(x, y, pixYOffset, digitCount);
			return true;
		}
		return false;
	}

	@Override
	public void drop(int digitCount) {
		mArrow.drop(digitCount);
		mCircle.drop(digitCount);
	}

	@Override
	public void drop(int digitCount, Boolean toGridResolution) {
		mArrow.drop(digitCount, toGridResolution);
		mCircle.drop(digitCount, toGridResolution);
	}

	@Override
	public void fadeIn(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mArrow.fadeIn(eq, dur, delay, full);
		mCircle.fadeIn(eq, dur, delay, full);
	}

	@Override
	public void fadeOut(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mArrow.fadeOut(eq, dur, delay, full);
		mCircle.fadeOut(eq, dur, delay, full);
	}

	@Override
	public void moveTo(Float pXend, Float pYend, MotionEquation eq, Integer dur, Integer delay) {
		mArrow.moveTo(pXend, pYend, eq, dur, delay);
		mCircle.moveTo(pXend, pYend, eq, dur, delay);
	}

	@Override
	public void update(long now, Boolean primaryThread, Boolean secondaryThread) {
		mArrow.update(now, primaryThread, secondaryThread);
		mCircle.update(now, primaryThread, secondaryThread);
	}

	@Override
	public void draw(GL10 gl, float pixYOffset) {
		mArrow.draw(gl, pixYOffset);
		mCircle.draw(gl, pixYOffset);
	}

}
