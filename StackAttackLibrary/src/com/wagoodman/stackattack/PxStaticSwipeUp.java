package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.MainActivity;


import android.content.Context;

public class PxStaticSwipeUp implements PxBase
{
	private final Context mContext;
	private final MainActivity game;
	
	private PxImage mCircle;
	
	PxStaticSwipeUp(Context context, float circleX, float circleY, Boolean lockXMove, Boolean lockYMove, Boolean interactable)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		int circleWidth = 128, circleHeight = 191;
		float circleAbsScale = (float) ( (1f/circleWidth)*(game.getWorld().mScreenBlockLength) )*1.7f;
		
		mCircle = new PxImage(mContext,  
				R.drawable.fingercircleup_w128h191, circleWidth, circleHeight, 
				circleAbsScale, 
				(-circleWidth/2f), (-circleWidth/2f),
				circleX, circleY,
				0,0,
				lockXMove, lockYMove, 
				interactable,		
				true,		// scale on interaction
				false		// crop on interaction
				);
		
	}
	
	@Override
	public void loadImage(GL10 gl) {
		mCircle.loadImage(gl);
	}

	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount) {
		return pickup(x, y, pixYOffset, digitCount, false);
	}
	
	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount, Boolean force) {
		return mCircle.pickup(x, y, pixYOffset, digitCount, force);
	}

	@Override
	public Boolean interact(float x, float y, int pixYOffset, int digitCount) {
		return mCircle.interact(x, y, pixYOffset, digitCount);
	}

	@Override
	public void drop(int digitCount) {
		mCircle.drop(digitCount);
	}

	@Override
	public void drop(int digitCount, Boolean toGridResolution) {
		mCircle.drop(digitCount, toGridResolution);
	}

	@Override
	public void fadeIn(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mCircle.fadeIn(eq, dur, delay, full);
	}

	@Override
	public void fadeOut(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mCircle.fadeOut(eq, dur, delay, full);
	}

	@Override
	public void moveTo(Float pXend, Float pYend, MotionEquation eq, Integer dur, Integer delay) {
		mCircle.moveTo(pXend, pYend, eq, dur, delay);
	}

	@Override
	public void update(long now, Boolean primaryThread, Boolean secondaryThread) {
		mCircle.update(now, primaryThread, secondaryThread);
	}

	@Override
	public void draw(GL10 gl, float pixYOffset) {
		mCircle.draw(gl, pixYOffset);
	}

}
