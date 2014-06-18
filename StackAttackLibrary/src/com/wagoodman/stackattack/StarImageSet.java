package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.wagoodman.stackattack.MainActivity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;


public class StarImageSet
{

	private final MainActivity game;
	private final Context mContext;
	
	private int[] mImageResIds = new int[]{ R.drawable.star0_w107h105, R.drawable.star1_w107h105, R.drawable.star2_w107h105, R.drawable.star3_w107h105 }; // stars: 0,1,2,3
	private int[] mTexID = new int[4]; // stars: 0,1,2,3
	private int[] mWidth = new int[4]; // stars: 0,1,2,3
	private int[] mHeight = new int[4]; // stars: 0,1,2,3
	private int[][] UVarray = new int[4][4];
	public int[] mScaledWidth = new int[4];
	public int[] mScaledHeight = new int[4];
	
	public StarImageSet(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
	}
	
	public void setDistances(GL10 gl)
	{

		gl.glGenTextures(mTexID.length, mTexID, 0);

		// select a bitmap based on dpi
		for (int starIdx=0; starIdx < mImageResIds.length; starIdx++)
		{
			Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mImageResIds[starIdx]);	
			
			// hard coded
			mWidth[starIdx] = 107;
			mHeight[starIdx] = 105;
			
			// get crop & scale dim
			mScaledHeight[starIdx] = (int) (game.getWorld().mDropSection.mYPixFoldedHeight * 0.75) ;
			double scaleRatio =  mScaledHeight[starIdx] / (double) mHeight[starIdx]  ;
			mScaledWidth[starIdx] = (int) ( scaleRatio * mWidth[starIdx]);
		
			
			/*
			game.text =  "Picture       : " + mWidth + " x " + mHeight + "\n";
			game.text += "Screen        : " + game.getWorld().mScreenWidth + " x " + game.getWorld().mScreenHeight + "\n";
			game.text += "Scaled        : " + scaledWidth + " x " + scaledHeight + "\n";
			game.text += "ScaledCropOff : " + scaledCropHeightOffset + "\n";
			game.text += "Scaled Crop Ht: " + scaledCropHeight + "\n";
			game.text += "Crop Ht       : " + cropHeight + "\n";
			game.textviewHandler.post(game.updateTextView);
			*/
			
			
			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexID[starIdx]);
		    
		    // Set texture parameters
		    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
			
		    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		    bitmap.recycle();
			
		}
		

	}
	
	public void draw(GL10 gl, int starCount, float xPixPos, float yPixPos)
	{
		// rely on z-ordering
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		// GL11ETX wont work with lighting on some devices!
		// Disable lighting
		gl.glDisable(GL10.GL_LIGHT0);	
		gl.glDisable(GL10.GL_LIGHTING);
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		
		gl.glColor4f(1f, 1f, 1f, 1.0f);
		
		// Set up GL for rendering the text
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexID[starCount]);
		
		// Update the crop rect
		
		UVarray[starCount][0] = 0;
		UVarray[starCount][1] = mHeight[starCount] ;
		UVarray[starCount][2] = mWidth[starCount];
		UVarray[starCount][3] = -mHeight[starCount] ;
		
		
		// no bluring texture on scale
		//((GL11) gl).glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		
		// Set crop area
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,UVarray[starCount],0);
		
		// Draw texture (scaled)	
		
		((GL11Ext) gl).glDrawTexfOES(
				xPixPos ,	// x
				yPixPos, //y
				World.GLEX11HUD_Z_ORDER,
				mScaledWidth[starCount],
				mScaledHeight[starCount] );
		
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
		
		// Enable Lighting!
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHTING);
		
		// re enable depth
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
	}


	
}
