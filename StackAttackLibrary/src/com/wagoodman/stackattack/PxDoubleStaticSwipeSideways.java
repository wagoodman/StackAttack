package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.MainActivity;


import android.content.Context;

public class PxDoubleStaticSwipeSideways implements PxBase
{
	private final Context mContext;
	private final MainActivity game;
	
	private PxImage mRightCircle;
	private PxImage mLeftCircle;
	
	PxDoubleStaticSwipeSideways(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
	
		
		int circleWidth = 249, circleHeight = 128;
		float circleAbsScale = (float) ( (1f/circleHeight)*(game.getWorld().mScreenBlockLength) )*2f;
		float centerOffset = circleHeight*circleAbsScale*0.75f;
		mLeftCircle = new PxImage(mContext,  
				R.drawable.fingercirclesideways_w249h128, circleWidth, circleHeight, 
				circleAbsScale, 
				(-circleWidth/2f), (-circleHeight/2f),
				(game.getWorld().mScreenWidth/2), (game.getWorld().mScreenHeight/2) - centerOffset,
				0,0,
				false, true, 
				true,		
				true,		// scale on interaction
				false,		// crop on interaction
				true		// sticky interaction
				);
		
		mRightCircle = new PxImage(mContext,  
				R.drawable.fingercirclesideways_w249h128, circleWidth, circleHeight, 
				circleAbsScale, 
				(-circleWidth/2f), (-circleHeight/2f),
				(game.getWorld().mScreenWidth/2), (game.getWorld().mScreenHeight/2) + centerOffset,
				0,0,
				false, true,  
				true,		
				true,		// scale on interaction
				false,		// crop on interaction
				true		// sticky interaction
				);
		
	}
	
	@Override
	public void loadImage(GL10 gl) {
		mRightCircle.loadImage(gl);
		mLeftCircle.loadImage(gl);
	}

	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount) {
		return pickup(x, y, pixYOffset, digitCount, false);
	}

	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount, Boolean force) {
		if (digitCount == 2)
		{
			if(mLeftCircle.pickup(x, y, pixYOffset, digitCount, force))
			{
				mRightCircle.pickup(x, y, pixYOffset, digitCount, true);
				return true;
			}
			else if(mRightCircle.pickup(x, y, pixYOffset, digitCount, force))
			{
				mLeftCircle.pickup(x, y, pixYOffset, digitCount, true);
				return true;
			}
		}
		return false;
	}

	@Override
	public Boolean interact(float x, float y, int pixYOffset, int digitCount) {
		//game.text = "\n\n\n\n\n\n\n";
		
		if (digitCount == 2)
		{
			
			if(mLeftCircle.interact(x, y, pixYOffset, digitCount))
			{
				mRightCircle.interact(x, y, pixYOffset, digitCount);
				//game.textviewHandler.post( game.updateTextView );
				return true;
			}
			else if(mRightCircle.interact(x, y, pixYOffset, digitCount))
			{
				mLeftCircle.interact(x, y, pixYOffset, digitCount);
				//game.textviewHandler.post( game.updateTextView );
				return true;
			}
		}
		return false;
	}

	@Override
	public void drop(int digitCount) {
		mRightCircle.drop(digitCount, false);
		mLeftCircle.drop(digitCount, false);
	}

	@Override
	public void drop(int digitCount, Boolean toGridResolution) {
		// ignore resolution
		mRightCircle.drop(digitCount, false);
		mLeftCircle.drop(digitCount, false);
	}

	@Override
	public void fadeIn(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mRightCircle.fadeIn(eq, dur, delay, full);
		mLeftCircle.fadeIn(eq, dur, delay, full);
	}

	@Override
	public void fadeOut(MotionEquation eq, Integer dur, Integer delay, Boolean full) {
		mRightCircle.fadeOut(eq, dur, delay, full);
		mLeftCircle.fadeOut(eq, dur, delay, full);
	}

	@Override
	public void moveTo(Float pXend, Float pYend, MotionEquation eq, Integer dur, Integer delay) {
		mRightCircle.moveTo(pXend, pYend, eq, dur, delay);
		mLeftCircle.moveTo(pXend, pYend, eq, dur, delay);
	}

	@Override
	public void update(long now, Boolean primaryThread, Boolean secondaryThread) {
		mRightCircle.update(now, primaryThread, secondaryThread);
		mLeftCircle.update(now, primaryThread, secondaryThread);
	}

	@Override
	public void draw(GL10 gl, float pixYOffset) {
		mRightCircle.draw(gl, pixYOffset);
		mLeftCircle.draw(gl, pixYOffset);
	}

}
