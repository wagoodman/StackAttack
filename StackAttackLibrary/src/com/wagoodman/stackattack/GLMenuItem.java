package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;

public abstract class GLMenuItem
{

	// Position functionality
	
	// in pixels
	public int mWidth = 100;
	public int mHeight = 35;
	
	public int mLeftPos=0, mRightPos=0, mTopPos=0, mBottomPos=0;
	
	public int xPos = 0;
	public int yPos = 0;
	
	protected int mScreenHeight = 0;
	
	protected Boolean mLeftJust = true;

	
	// Clickable 
	
	public void setPos(int xSide, int bottom, Boolean leftJust)
	{
		xPos = xSide;	// left or right
		yPos = bottom;
		
		mLeftJust = leftJust;
	}

	

	public void setDimensions(int width, int height, int screenheight)
	{
		mWidth = width;
		mHeight = height;
		
		mScreenHeight = screenheight;
		
		// set comparisons
		if (mLeftJust)
		{
			// left just
			mLeftPos	= xPos;
			mRightPos	= xPos + width;
		}
		else
		{
			// right just
			mLeftPos	= xPos - width;
			mRightPos	= xPos;
		}
		
		mTopPos		= yPos + height;
		mBottomPos	= yPos;
		
	}
	
	
	public Boolean isWithinClickableArea(int xPix, int yPix, int pixYOffset)
	{
		
		if (
				xPix >= mLeftPos && 
				xPix <= mRightPos &&
				(mScreenHeight - yPix) <= mTopPos + (mScreenHeight - pixYOffset) &&
				(mScreenHeight - yPix) >= mBottomPos + (mScreenHeight - pixYOffset)
			)
			
			return true;
		
		return false;
	}
	
	abstract void triggerIntro(int delay);
	abstract void triggerOutro(int delay);
	abstract void intro();
	abstract void intro(Integer dur);
	abstract void outro();
	abstract void outro(Integer dur);
	abstract void setFontDimensions();
	abstract void setLabelDimensions();
	//abstract void hide();
	abstract int getOutroDuration();
	abstract Boolean interact(int x, int y, int pixYOffset);
	abstract void update(long now, Boolean primaryThread, Boolean secondaryThread);
	abstract void draw(GL10 gl, float pixYOffset);
	
}
