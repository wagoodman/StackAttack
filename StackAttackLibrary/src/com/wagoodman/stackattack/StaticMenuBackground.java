package com.wagoodman.stackattack;


import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.wagoodman.stackattack.MainActivity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.opengl.GLUtils;


public class StaticMenuBackground extends GLMenuItem
{
	private final Context mContext;
	private final MainActivity game;
	
	private int texID;
	private int [] UVarray = new int[4];
	private int cropHeight, scaledWidth, scaledHeight, scaledCropHeight, scaledCropHeightOffset;
	public StaticMenuBackground(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		
	}
	
	public void loadBackground(GL10 gl)
	{

		// select a bitmap based on dpi

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.menubase_w940h1843);
		setDimensions(940, 1843, (int) game.getWorld().mScreenHeight);

		
		// fix for the extra notification bar space (when taking away the bar the usable space does not increase as it should... so add it manually.
		/*
		Rect rectgle= new Rect();
		Window window= game.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
		int statusBarHeight = rectgle.top;
		*/
		
		// get crop & scale dim
		scaledWidth = (int) game.getWorld().mScreenWidth;
		double scaleRatio =  game.getWorld().mScreenWidth / (double) mWidth  ;
		scaledHeight = (int) ( scaleRatio * mHeight );		
		scaledCropHeightOffset = (int) (game.getWorld().mScreenHeight - scaledHeight) /*+ statusBarHeight*/;
		scaledCropHeight = (int) (scaledHeight - Math.abs(scaledCropHeightOffset));
		cropHeight = (int) (scaledCropHeight/scaleRatio);
		
		// set position based off of scaled info
		setPos((int) (game.getWorld().mScreenWidth/2 - scaledWidth/2), 0, true);
		
		
		/*
		String extensions = gl.glGetString(GL10.GL_EXTENSIONS);
		boolean drawTexture = extensions.contains("draw_texture");
		game.text = "OpenGL Support - ver.:" + gl.glGetString(GL10.GL_VERSION) + "renderer:" + gl.glGetString(GL10.GL_RENDERER) + " : " + (drawTexture ? "good to go!" : "forget it!!");
		game.textviewHandler.post(game.updateTextView);
		*/
		
		/*
		game.text =  "Picture       : " + mWidth + " x " + mHeight + "\n";
		game.text += "Screen        : " + game.getWorld().mScreenWidth + " x " + game.getWorld().mScreenHeight + "\n";
		game.text += "Scaled        : " + scaledWidth + " x " + scaledHeight + "\n";
		game.text += "ScaledCropOff : " + scaledCropHeightOffset + "\n";
		game.text += "Scaled Crop Ht: " + scaledCropHeight + "\n";
		game.text += "Crop Ht       : " + cropHeight + "\n";
		game.textviewHandler.post(game.updateTextView);
		*/
		
		
		/*
		cropHeightOffset = (int) (game.getWorld().mScreenHeight - mHeight) + statusBarHeight;
		cropHeight = mHeight - Math.abs(cropHeightOffset);
		setPos((int) (game.getWorld().mScreenWidth/2 - mWidth/2), 0, true);
		*/
		
		/*
		game.text  = "Height         : " + mHeight + "\n";
		game.text += "Crop Height    : " + cropHeight + "\n";
		game.text += "Crop Height Off: " + cropHeightOffset + "\n";
		game.textviewHandler.post( game.updateTextView );
		*/
		
		int texid[] = new int[1];
		gl.glGenTextures(texid.length, texid, 0);
		texID = texid[0];
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
	    
	    // Set texture parameters
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
	    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);


	    
	    bitmap.recycle();
		
	}

	
	@Override
	public void triggerIntro(int delay)
	{
	}
	
	@Override
	public void triggerOutro(int delay)
	{
	}
	
	@Override
	public void intro()
	{	
	}
	
	@Override
	public void intro(Integer dur)
	{	
	}
	
	@Override
	public void outro()
	{
	}

	@Override
	public void outro(Integer dur)
	{
	}

	@Override
	void setLabelDimensions()
	{
	}
	
	@Override
	void setFontDimensions()
	{
	}
	
	@Override
	int getOutroDuration()
	{
		return 0;
	}
	
	
	@Override
	Boolean interact(int x, int y, int pixYOffset)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{

	}

	public void draw(GL10 gl, float pixYOffset)
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
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		
		// Update the crop rect
		
		UVarray[0] = 0;
		UVarray[1] = (int) cropHeight;
		UVarray[2] = (int) mWidth;
		UVarray[3] = (int) -cropHeight;
		
		// Set crop area
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,UVarray,0);
		
		
		// Draw texture (scaled)			
		((GL11Ext) gl).glDrawTexfOES(
				xPos ,	// x
				yPos + pixYOffset,	// y
				World.GLEX11HUD_Z_ORDER,
				scaledWidth,
				scaledCropHeight );

		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
		
		// Enable Lighting!
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHTING);
		
		// re enable depth
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		
	}


	
}
