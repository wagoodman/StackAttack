package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;


import com.wagoodman.stackattack.Coord;
import com.wagoodman.stackattack.Driver;
import com.wagoodman.stackattack.ShapeManager;
import com.wagoodman.stackattack.MainActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class PxImage implements PxBase
{
	private final Context mContext;
	private final MainActivity game;
	
	private Boolean isLoaded = false;
	
	private Boolean	mInteractable = false;
	private Boolean mScaleOnInteraction = false;
	private Boolean mCropOnInteraction = false;
	private Boolean isPickedUp = false;
	
	private Long mPickupStartTime = -1l;
	
	public Boolean mLockXDir = false,
					mLockYDir = false;
	
	private Boolean mStickyInteraction = false;	// always drop in original coords
	private Coord<Float> mStickyPixCoord;
	
	public float absx=0, absy=0;
	
	public float 	mLeftPos=0, 
					mRightPos=0, 
					mTopPos=0, 
					mBottomPos=0,
					mXRelOffset,
					mYRelOffset,
					mXOffset,
					mYOffset,
					mXScaledOffset,
					mYScaledOffset,
					mAbsoluteScale,
					mWidth,
					mHeight,
					mAbsScaledWidth,
					mAbsScaledHeight,
					mCropWidth,
					mCropHeight,
					mScaledHeight, 
					mScaledWidth,
					mXCrop,
					mYCrop;
	
	protected Boolean mLeftJust = true;
	
	private int texID;
	private int [] UVarray = new int[4];
	
	public Driver mXDriver, mYDriver, mAlphaDriver, mScaleDriver;
	
	private Integer mImageResId;
	
	private Bitmap mBitmap;
	
	public PxImage(Context context, int resId, int width, int height, float scaleRatio, float xRelOff, float yRelOff, float initX, float initY, float xCrop, float yCrop, Boolean lockXMove, Boolean lockYMove, Boolean interactable, Boolean scaleOnInteraction, Boolean cropOnInteraction)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		init(resId, width, height, scaleRatio, xRelOff, yRelOff, initX, initY, xCrop, yCrop, lockXMove, lockYMove, interactable, scaleOnInteraction, cropOnInteraction, false);
	}
	
	public PxImage(Context context, int resId, int width, int height, float scaleRatio, float xRelOff, float yRelOff, float initX, float initY, float xCrop, float yCrop, Boolean lockXMove, Boolean lockYMove, Boolean interactable, Boolean scaleOnInteraction, Boolean cropOnInteraction, Boolean stickyInteraction)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		init(resId, width, height, scaleRatio, xRelOff, yRelOff, initX, initY, xCrop, yCrop, lockXMove, lockYMove, interactable, scaleOnInteraction, cropOnInteraction, stickyInteraction);
	}
	
	private void init(int resId, int width, int height, float scaleRatio, float xRelOff, float yRelOff, float initX, float initY, float xCrop, float yCrop, Boolean lockXMove, Boolean lockYMove, Boolean interactable, Boolean scaleOnInteraction, Boolean cropOnInteraction, Boolean stickyInteraction)
	{
		mImageResId = resId;
		
		mInteractable = interactable;
		mScaleOnInteraction = scaleOnInteraction;
		mCropOnInteraction = cropOnInteraction;
		
		mLockXDir = lockXMove;
		mLockYDir = lockYMove;
		
		mXCrop = (int) xCrop;
		mYCrop = (int) yCrop;
		
		// new Driver, init position
		mXDriver = new Driver(MotionEquation.LOGISTIC, 500, initX);
		mYDriver = new Driver(MotionEquation.LOGISTIC, 500, initY);
		mAlphaDriver = new Driver(MotionEquation.LOGISTIC, 500, 0f);
		mScaleDriver = new Driver(MotionEquation.LOGISTIC, 500, 1f);
		
		// select a bitmap based on dpi
		mBitmap = BitmapFactory.decodeResource(mContext.getResources(), mImageResId);
		mWidth = width;
		mHeight = height;
		mCropWidth = width;
		mCropHeight = height;

		
		// get crop & scale dim
		mAbsoluteScale = scaleRatio;
		mXRelOffset = xRelOff*mAbsoluteScale;
		mYRelOffset = yRelOff*mAbsoluteScale;
		mAbsScaledWidth = (int) (mWidth*mAbsoluteScale);
		mAbsScaledHeight = (int) (mHeight*mAbsoluteScale);
		mScaledWidth = (int) (mAbsScaledWidth*mScaleDriver.mCurrent);
		mScaledHeight = (int) (mAbsScaledHeight*mScaleDriver.mCurrent);
	
		mXOffset = mXRelOffset;
		mYOffset = mYRelOffset;
		
		mXScaledOffset = mXOffset*mScaleDriver.mCurrent;
		mYScaledOffset = mYOffset*mScaleDriver.mCurrent;
		
		mStickyInteraction = stickyInteraction;
		if (mStickyInteraction)
		{
			mStickyPixCoord = new Coord<Float>(initX, initY);
		}
	}

	@Override
	public void loadImage(GL10 gl)
	{
		
		int texid[] = new int[1];
		gl.glGenTextures(texid.length, texid, 0);
		texID = texid[0];
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
	    
	    // Set texture parameters
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
	    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, mBitmap, 0);
	    
	    mBitmap.recycle();
		
	    isLoaded = true;
	}

	
	public Boolean isWithinClickableArea(float xPix, float yPix, int pixYOffset)
	{
		//game.text += "   " + ((game.getWorld().mScreenHeight - yPix)) + " <= " + (mTopPos /*+ (game.getWorld().mScreenHeight - pixYOffset)*/) + "      " + ((game.getWorld().mScreenHeight - yPix)) + " >= " + (mBottomPos /*+ (game.getWorld().mScreenHeight - pixYOffset)*/) + "\n" ;
		if (
				xPix >= mLeftPos && 
				xPix <= mRightPos &&
				(game.getWorld().mScreenHeight - yPix) <= mTopPos /*+ (game.getWorld().mScreenHeight - pixYOffset)*/ &&
				(game.getWorld().mScreenHeight - yPix) >= mBottomPos /*+ (game.getWorld().mScreenHeight - pixYOffset) */
			)
			return true;
			
		return false;
	}
	
	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount)
	{
		return pickup(x, y, pixYOffset, digitCount, false);
	}
	
	@Override
	public Boolean pickup(float x, float y, int pixYOffset, int digitCount, Boolean force)
	{
		if (mInteractable)
		{

			if (!mCropOnInteraction)
			{
				// move
				if (!isPickedUp && ( isWithinClickableArea(x, y, pixYOffset)  ||  force )) 
				{
					//game.text = "      Pickup indeed!" + "\n" + game.text;
					//game.textviewHandler.post(game.updateTextView);
					
					if (mLockXDir == false)
					{
						if (mXDriver.isTransforming)
							mXDriver.stop();
						mXDriver.mCurrent = x;
						isPickedUp = true;
					}
					if (mLockYDir == false)
					{
						if (mYDriver.isTransforming)
							mYDriver.stop();
						mYDriver.mCurrent = game.getWorld().mScreenHeight - y;
						isPickedUp = true;
					}
					
					if (isPickedUp)
					{
						mPickupStartTime = System.currentTimeMillis();
						if (mScaleOnInteraction)
							mScaleDriver.start(mScaleDriver.mCurrent, 1.2f, MotionEquation.LOGISTIC, 350, 0);
					}
					
					
					return true;
				}
			}
			else
			{
				// crop

				//if (isWithinClickableArea(x, y, pixYOffset))
				{
					if (mLockXDir == false)
					{
						mXCrop = (int) x;
					}
					if (mLockYDir == false)
					{
						mYCrop = (int) y;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public Boolean interact(float x, float y, int pixYOffset, int digitCount)
	{
		absx = x;
		absy = y;
		
		if (mInteractable)
		{
			//game.text += x+" , " + y + "  --->  ";	
			
			
			//game.text += x+" , " + y + "\n";
			
			if (!mCropOnInteraction)
			{
				// move
				if (isPickedUp /*&& isWithinClickableArea(x, y, pixYOffset)*/) 
				{
					//game.text = "      Drag indeed!" + "\n" + game.text;
					//game.textviewHandler.post(game.updateTextView);
					
					if (mLockXDir == false)
					{
						if (mXDriver.isTransforming)
							mXDriver.stop();
						mXDriver.mCurrent = x;
					}
					if (mLockYDir == false)
					{
						if (mYDriver.isTransforming)
							mYDriver.stop();
						mYDriver.mCurrent = game.getWorld().mScreenHeight - y;
					}
					
					return true;
				}
			}
			else
			{
				// crop
				
				//if (isWithinClickableArea(x, y, pixYOffset))
				{
					if (mLockXDir == false)
					{
						mXCrop = (int) x;
					}
					if (mLockYDir == false)
					{
						mYCrop = (int) y;
					}
				}
					
			}
		}
		return false;
	}
	
	@Override
	public void drop(int digitCount)
	{
		drop(digitCount, true);
	}
	
	@Override
	public void drop(int digitCount, Boolean toGridResolution)
	{
		if (mInteractable)
		{
			if (isPickedUp)
			{
				isPickedUp = false;
				mPickupStartTime = -1l;
				
				if (mScaleOnInteraction)
					mScaleDriver.start(mScaleDriver.mCurrent, 1f, MotionEquation.LOGISTIC, 350, 0);
				
				if (mStickyInteraction)
				{
					// drop where it was picked up
					
					if (mLockXDir == false)
					{
						float x = mStickyPixCoord.getX();
						mXDriver.start(mXDriver.mCurrent, x, MotionEquation.LOGISTIC, 400, 0);
					}
					if (mLockYDir == false)
					{
						float y = mStickyPixCoord.getY();
						mYDriver.start(mYDriver.mCurrent, y, MotionEquation.LOGISTIC, 400, 0);
					}
					
				}
				else if (toGridResolution)
				{
					// drop in nearest grid location
					
					Coord<Integer> coord = game.getWorld().convertPixToRC(mXDriver.mCurrent, mYDriver.mCurrent);
					
					//x += mXOffset;
					//y += mYOffset;
					
					if (mLockXDir == false)
					{
						float x = game.getWorld().mScreenBlockLength*(coord.getCol()) + game.getWorld().mScreenBlockLength/2;
						mXDriver.start(mXDriver.mCurrent, x, MotionEquation.LOGISTIC, 400, 0);
					}
					if (mLockYDir == false)
					{
						float y = game.getWorld().mScreenHeight - game.getWorld().mScreenBlockLength*(coord.getRow())  + game.getWorld().mScreenBlockLength/2;
						mYDriver.start(mYDriver.mCurrent, y, MotionEquation.LOGISTIC, 400, 0);
					}
					
					
				}
			}
		}
	}

	@Override
	public void fadeIn(MotionEquation eq, Integer dur, Integer delay, Boolean full)
	{
		if (full)
			mAlphaDriver.start(0f, 1f, eq, dur, delay);
		else
			mAlphaDriver.start(mAlphaDriver.mCurrent, 1f, eq, dur, delay);
	}
	
	@Override
	public void fadeOut(MotionEquation eq, Integer dur, Integer delay, Boolean full)
	{
		if (full)
			mAlphaDriver.start(1f, 0f, eq, dur, delay);
		else
			mAlphaDriver.start(mAlphaDriver.mCurrent, 0f, eq, dur, delay);
			
	}
	
	@Override
	public void moveTo(Float pXend, Float pYend, MotionEquation eq, Integer dur, Integer delay)
	{
		mXDriver.start(mXDriver.mCurrent, pXend, eq, dur, delay);
		mYDriver.start(mYDriver.mCurrent, pYend, eq, dur, delay);
	}
	
	@Override
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		mXDriver.update(now, primaryThread, secondaryThread);
		mYDriver.update(now, primaryThread, secondaryThread);
		mAlphaDriver.update(now, primaryThread, secondaryThread);
		mScaleDriver.update(now, primaryThread, secondaryThread);
		
		if (primaryThread)
		{	
			
			//game.text  = "Start  : " + mAlphaDriver.mStartPoint+"\n";
			//game.text += "End    : " + mAlphaDriver.mEndPoint+"\n";
			//game.text += "Current: " + mAlphaDriver.mCurrent+"\n";
			//game.textviewHandler.post(game.updateTextView);
			
			
			// adject width & height based on scale
			mScaledWidth = (int) (mAbsScaledWidth*mScaleDriver.mCurrent);
			mScaledHeight = (int) (mAbsScaledHeight*mScaleDriver.mCurrent);
			
			mXOffset = mXRelOffset*mScaleDriver.mCurrent;
			mYOffset = mYRelOffset*mScaleDriver.mCurrent;
			
			// Adjust Offset based on scale (center on dimensional differences)
			mXScaledOffset = mXOffset - (mScaledWidth - mAbsScaledWidth)/2f;
			mYScaledOffset = mYOffset - (mScaledHeight - mAbsScaledHeight)/2f;		
			
			// set relative positions (for interaction reference)
			if (mLeftJust)
			{
				// left just
				mLeftPos	= mXScaledOffset + mXDriver.mCurrent;
				mRightPos	= mXScaledOffset + mXDriver.mCurrent + mScaledWidth;
			}
			else
			{
				// right just
				mLeftPos	= mXScaledOffset + mXDriver.mCurrent - mScaledWidth;
				mRightPos	= mXScaledOffset + mXDriver.mCurrent;
			}
			// vertical 
			mTopPos		= mYScaledOffset + mYDriver.mCurrent + mScaledHeight;
			mBottomPos	= mYScaledOffset + mYDriver.mCurrent;
			
			
			// Crop
			if (mCropOnInteraction)
			{
				if (mLockXDir == false)
				{
					// anchored left
					if (mLeftJust)
						mCropWidth = (mWidth/mScaledWidth)*(mScaledWidth + Math.min(0, mLeftPos + ( mXCrop - mRightPos) ) - mLeftPos );
					// anchored right
					else
					{
						// todo
						mCropHeight = (mHeight/mScaledHeight)*(mScaledHeight + Math.min(0, mTopPos + ( mYCrop - mBottomPos) ) - mTopPos );
					}
				}
				if (mLockYDir == false)
				{
					mCropHeight = (mHeight/mScaledHeight)*(mScaledHeight + Math.min(0, mBottomPos + (mYCrop - mTopPos) ) - mBottomPos);
				}
			}
			
			/*
			game.text += "========================\n";
			game.text += "scaW / 2      : " + (mScaledWidth/2) + "\n";
			game.text += "mXScaledOffset: " + mXScaledOffset + "\n";
			game.text += "mLeftPos      : " + mLeftPos + "\n";
			game.text += "Center Coords : " + ((int)(mLeftPos + mScaledWidth/2)) + " == " + ((int)absx) + "\n";
			game.text += "scaW - absScaW: " + (mScaledWidth - mAbsScaledWidth) + "\n";
			game.text += "relX          : " + (mXDriver.mCurrent - mLeftPos) + "\n";
			game.text += "curX          : " + mXDriver.mCurrent + "\n";
			*/
			
			/*
			game.text += "========================\n";
			game.text += "mXOffset        : " + mXOffset + "\n";
			game.text += "mXScaledOffset  : " + mXScaledOffset + "\n";
			game.text += "scaW - absScaW  : " + (mScaledWidth - mAbsScaledWidth) + "\n";
			game.text += "xPos            : " + mXDriver.mCurrent + "\n";
			game.text += "mLeftPos        : " + mLeftPos + "\n";	
			*/
			
			/*
			game.text += "========================\n";
			game.text += "mXCrop    : " + mXCrop + "\n";
			game.text += "mYCrop    : " + mYCrop + "\n";
			game.text += "cropWidth : " + mCropWidth + "\n";
			game.text += "cropHeight: " + mCropHeight + "\n";
			*/
			
			/*
			game.text += "Left  : " + mLeftPos + "\n";
			game.text += "Right : " + mRightPos + "\n";
			game.text += "Top   : " + mTopPos + "\n";
			game.text += "Bottom: " + mBottomPos + "\n";
			game.text += "--------------------------\n";
			game.text += "ScaWidth : " + mScaledWidth + "\n";
			game.text += "ScaHeight: " + mScaledHeight + "\n";
			game.text += "--------------------------\n";
			game.text += "ScaXOff: " + mXScaledOffset + "\n";
			game.text += "ScaYOff: " + mYScaledOffset + "\n";
			*/
			//game.textviewHandler.post(game.updateTextView);
			
		}
		else if (secondaryThread)
		{	
			// check if should drop
			if (isPickedUp)
			{
				if (((mPickupStartTime+ShapeManager.mActiveBlockExpirationDuration) < now) && mPickupStartTime != -1 )
				{
					drop(0);
				}
			}
		}
		
	}

	@Override
	public void draw(GL10 gl, float pixYOffset)
	{
		// LOAD...
		if (!isLoaded)
		{
			loadImage(gl);
		}
		
		// DRAW...
		
		// rely on z-ordering
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		// GL11ETX wont work with lighting on some devices!
		// Disable lighting
		gl.glDisable(GL10.GL_LIGHT0);	
		gl.glDisable(GL10.GL_LIGHTING);
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glColor4f(1f, 1f, 1f, mAlphaDriver.mCurrent);
		
		// Set up GL for rendering the text
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		
		// Update the crop rect
		UVarray[0] = 0;
		UVarray[1] = (int) mCropHeight;
		UVarray[2] = (int) mCropWidth;
		UVarray[3] = (int) -mCropHeight;
		
		// Set crop area
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,UVarray,0);
		
		// Draw texture (scaled)			
		((GL11Ext) gl).glDrawTexfOES(
				mXScaledOffset + mXDriver.mCurrent ,	// x
				mYScaledOffset + mYDriver.mCurrent + pixYOffset,	// y
				World.GLEX11HUD_Z_ORDER,
				(mCropWidth/mWidth)*mScaledWidth*mScaleDriver.mCurrent,
				(mCropHeight/mHeight)*mScaledHeight*mScaleDriver.mCurrent );

		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
		
		// Enable Lighting!
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHTING);
		
		// re enable depth
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
	}


	
}