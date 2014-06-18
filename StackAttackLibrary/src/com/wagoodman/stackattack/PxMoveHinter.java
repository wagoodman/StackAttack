package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.MainActivity;


import android.content.Context;

public class PxMoveHinter implements PxBase
{
	private final Context mContext;
	private final MainActivity game;
	
	private PxImage mMoveHint;
	private PxImage mCircle;
	
	PxMoveHinter(Context context, float circleX, float circleY,  Boolean lockXMove, Boolean lockYMove, Boolean interactable)
	{
		mContext = context;
		game = (MainActivity) mContext;
	
		
		int circleWidth = 128, circleHeight = 128;
		float circleAbsScale = (float) ( (1f/circleWidth)*(game.getWorld().mScreenBlockLength) )*1.7f;
		int hintWidth = 579, hintHeight = 644;
		float hintAbsScale = (float) ( (1f/hintWidth)*(game.getWorld().mScreenBlockLength*4) );
		
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
		
		mMoveHint = new PxImage(mContext,  
				R.drawable.movehints_w579h644, hintWidth, hintHeight, 
				hintAbsScale, 
				(-hintWidth/2f), (-hintHeight/2f),
				circleX, circleY,
				0, 0,
				lockXMove, lockYMove,
				interactable,		
				false,		// scale on interaction
				false		// crop on interaction
				);
		
	}
	
	@Override
	public void loadImage(GL10 gl) {
		mMoveHint.loadImage(gl);
		mCircle.loadImage(gl);
	}

	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount) {
		return pickup(x, y, pixYOffset, digitCount, false);
	}

	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount, Boolean force) {
		if(mCircle.pickup(x, y, pixYOffset, digitCount, force))
		{
			mMoveHint.pickup(x, y, pixYOffset, digitCount, force);
			return true;
		}
		return false;
	}

	@Override
	public Boolean interact(float x, float y, int pixYOffset, int digitCount) {
		//game.text = "\n\n\n\n\n\n\n";
		if(mCircle.interact(x, y, pixYOffset, digitCount))
		{
			mMoveHint.interact(x, y, pixYOffset, digitCount);
			//game.textviewHandler.post( game.updateTextView );
			return true;
		}
		return false;
	}

	@Override
	public void drop(int digitCount) {
		mMoveHint.drop(digitCount);
		mCircle.drop(digitCount);
	}

	@Override
	public void drop(int digitCount, Boolean toGridResolution) {
		mMoveHint.drop(digitCount, toGridResolution);
		mCircle.drop(digitCount, toGridResolution);
	}

	@Override
	public void fadeIn(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mMoveHint.fadeIn(eq, dur, delay, full);
		mCircle.fadeIn(eq, dur, delay, full);
	}

	@Override
	public void fadeOut(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mMoveHint.fadeOut(eq, dur, delay, full);
		mCircle.fadeOut(eq, dur, delay, full);
	}

	@Override
	public void moveTo(Float pXend, Float pYend, MotionEquation eq, Integer dur, Integer delay) {
		mMoveHint.moveTo(pXend, pYend, eq, dur, delay);
		mCircle.moveTo(pXend, pYend, eq, dur, delay);
	}

	@Override
	public void update(long now, Boolean primaryThread, Boolean secondaryThread) {
		mMoveHint.update(now, primaryThread, secondaryThread);
		mCircle.update(now, primaryThread, secondaryThread);
	}

	@Override
	public void draw(GL10 gl, float pixYOffset) {
		mMoveHint.draw(gl, pixYOffset);
		mCircle.draw(gl, pixYOffset);
	}

}
