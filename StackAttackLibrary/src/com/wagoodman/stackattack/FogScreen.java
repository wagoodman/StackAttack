package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.ColorBroker;
import com.wagoodman.stackattack.ColorState;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;


public class FogScreen extends GLMenuItem
{
	
	private final Context mContext;
	private final MainActivity game;
	
	public ColorBroker mColor;
	
	
	private Driver mHeightDriver;	
	private Driver mPointDriver;
	
	private int texID;
	private int [] UVarray = new int[4];
	private int cropHeight, scaledWidth;
	
	FogScreen(Context context)
	{
		// get the game object from context
		game = (MainActivity) (context);
		mContext = context;
		
		mColor = new ColorBroker(Color.TRANSPARENT);
		
		mHeightDriver = new Driver(MotionEquation.LOGISTIC, DropSection.DEFAULT_DURATION, 0f ); 
		mPointDriver  = new Driver(MotionEquation.LOGISTIC, DropSection.DEFAULT_DURATION, 0f ); 

		hideNow();
	}
	
	public void loadImage(GL10 gl)
	{

		// select a bitmap based on dpi

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.white);
		setDimensions(8, 8, (int) game.getWorld().mScreenHeight);

		
		scaledWidth = (int) (game.getWorld().mScreenWidth);
		cropHeight = mHeight;
				
		// set position based off of scaled info
		
		setPos(0, (int) game.getWorld().mScreenHeight, true);

		
		
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
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
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


	    gl.glDisable(GL10.GL_BLEND);
	    
	    bitmap.recycle();
		
	}
	
	public void hideNow()
	{
		// stop any previous transforms
		stop();
		
		mHeightDriver.mCurrent = 0;
		mPointDriver.mCurrent = 0;
		
	}
	
	public void set(float point, float height)
	{
		// stop any previous transforms
		stop();
		
		mHeightDriver.mCurrent = height;
		mPointDriver.mCurrent = point;
		
	}
	
	public void hide(int duration)
	{
		// stop any previous transforms
		stop();
		
		start(mColor.mCurrentColor, Color.TRANSPARENT, duration);
		start(0f, 0f, duration);
	}
	
	public void start(Color end, int duration)
	{
		start(mColor.mCurrentColor, end, duration);
	}
	
	public void start(Color start, Color end, int duration)
	{

		mColor.enqueue( 
			new ColorState( 
					new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
					start, 
					end,
					new int[] {duration, duration, duration, duration},
					new int[] {0,0,0,0}	// relative to FIFO q
					));	
	}
	
	public void start(float topPercentage, float bottomPercentage, int duration)
	{
		// point, height
		start(game.getWorld().mScreenHeight*bottomPercentage, game.getWorld().mScreenHeight*Math.abs(topPercentage-bottomPercentage), MotionEquation.LOGISTIC, duration);
	}
	
	private void start(float endTopY, float endHeight, MotionEquation eq, int dur)
	{
		
		// stop any previous transforms
		stop();
		
		// start both drivers
		mPointDriver.start(mPointDriver.mCurrent, endTopY, eq, dur, null);
		mHeightDriver.start(mHeightDriver.mCurrent, endHeight, eq, dur, null);
	}
	
	private void stop()
	{
		mPointDriver.stop();
		mHeightDriver.stop();
	}
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		mPointDriver.update(now, primaryThread, secondaryThread);	// start - finish
		mHeightDriver.update(now, primaryThread, secondaryThread);	// start - finish
		
		if (primaryThread)
		{
			
			// Process color
			if (!mColor.isEmpty())
				mColor.processColorElements(now);
			
		}
		else if (secondaryThread)
		{
			//queueing
		}

		
	}
	
	public void draw(GL10 gl)
	{
		
		// rely on z-ordering
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		// GL11ETX wont work with lighting on some devices!
		// Disable lighting
		gl.glDisable(GL10.GL_LIGHT0);	
		gl.glDisable(GL10.GL_LIGHTING);
		
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glColor4f(
				mColor.mCurrentColorAmbient[0], 
				mColor.mCurrentColorAmbient[1], 
				mColor.mCurrentColorAmbient[2], 
				mColor.mCurrentColorAmbient[3]
						);
		
		// Set up GL for rendering the text
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, texID);
		
		// Update the crop rect
		
		UVarray[0] = 0;
		UVarray[1] = (int) cropHeight;
		UVarray[2] = (int) mWidth;
		UVarray[3] = (int) -cropHeight;
		
		// bottom half
		
		/*
		UVarray[0] = 0;
		UVarray[1] =  mHeight;
		UVarray[2] = mWidth;
		UVarray[3] = -(int)(mHeight*0.5);
		*/
		
		/*
		// top half
		UVarray[0] = 0;
		UVarray[1] =  (int)(mHeight*0.5);
		UVarray[2] = mWidth;
		UVarray[3] = -(int)(mHeight*0.5);
		*/
		
		// no bluring texture on scale
		//((GL11) gl).glTexParameteri(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		
		// Set crop area
		((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,UVarray,0);
		
		// Draw texture (scaled)	
		
		((GL11Ext) gl).glDrawTexfOES(
				xPos ,	// x
				game.getWorld().mScreenHeight - mPointDriver.mCurrent,	// y
				World.GLEX11HUD_Z_ORDER,
				scaledWidth,
				mHeightDriver.mCurrent );
		
		/*
		gl.glScalef( 
				3, 
				3, 
				3
						);
		
		game.getWorld().mBlock.draw(gl);
		*/
		
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glDisable(GL10.GL_BLEND);
		
		// Enable Lighting!
		gl.glEnable(GL10.GL_LIGHT0);
		gl.glEnable(GL10.GL_LIGHTING);
		
		
		// re enable
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		//game.text  = "Stats Backdrop Position : " + mPointDriver.mCurrent + "\n";
		//game.text += "Stats Backdrop Height   : " + mHeightDriver.mCurrent + "\n";
		//game.textviewHandler.post( game.updateTextView );
	}

	@Override
	public void triggerIntro(int delay){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void triggerOutro(int delay){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	void intro() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void intro(Integer dur) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void outro() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void outro(Integer dur) {
		// TODO Auto-generated method stub
		
	}

	@Override
	void setFontDimensions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void setLabelDimensions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	int getOutroDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	Boolean interact(int x, int y, int pixYOffset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void draw(GL10 gl, float pixYOffset) {
		// TODO Auto-generated method stub
		
	}


	
}
