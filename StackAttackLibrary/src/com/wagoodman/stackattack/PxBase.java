package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;


public abstract interface PxBase
{
	
	public abstract void loadImage(GL10 gl);
	//public abstract Boolean isWithinClickableArea(float xPix, float yPix, int pixYOffset);
	public abstract Boolean pickup(float x, float y, int pixYOffset, int digitCount, Boolean force);
	public abstract Boolean pickup(float x, float y, int pixYOffset, int digitCount);
	public abstract Boolean interact(float x, float y, int pixYOffset, int digitCount);
	public abstract void drop(int digitCount);
	public abstract void drop(int digitCount, Boolean toGridResolution);
	public abstract void fadeIn(MotionEquation eq, Integer dur, Integer delay, Boolean full);
	public abstract void fadeOut(MotionEquation eq, Integer dur, Integer delay, Boolean full);
	public abstract void moveTo(Float pXend, Float pYend, MotionEquation eq, Integer dur, Integer delay);
	public abstract void update(long now, Boolean primaryThread, Boolean secondaryThread);
	public abstract void draw(GL10 gl, float pixYOffset);
	
}