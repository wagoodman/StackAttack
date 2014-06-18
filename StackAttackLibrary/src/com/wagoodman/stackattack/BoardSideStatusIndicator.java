package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;


import com.wagoodman.stackattack.R;
import com.wagoodman.stackattack.R.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLUtils;
import android.view.Window;

public class BoardSideStatusIndicator extends GLMenuItem
{
	
	private final Context mContext;
	private final MainActivity game;
	
	public ColorBroker mColor;
	
	private Boolean isLeft;
	
	private int mLastEventRow = 0;
	private float mCurrentRow = 0;
	
	public Color IMMINENT = Color.RED;
	public Color WARNING = Color.YELLOW;
	public Color NEUTRAL = Color.GREEN;
	
	private Boolean isTransforming = false;
	private long mStartTime;
	private int mDuration = 500;
	private float mStartPoint;
	private float mEndPoint;	
	
	private int texID, level=0;
	private int [] UVarray = new int[4];
	private int cropHeight, scaledWidth, scaledHeight, scaledCropHeight, scaledCropHeightOffset;
	
	private float totalPixHeight = 200;
	
	BoardSideStatusIndicator(Context context, Boolean left)
	{
		// get the game object from context
		game = (MainActivity) (context);
		mContext = context;
		
		mColor = new ColorBroker(NEUTRAL);
		
		isLeft = left;
	}
	
	public void loadImage(GL10 gl)
	{

		// select a bitmap based on dpi

		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.white);
		setDimensions(8, 8, (int) game.getWorld().mScreenHeight);

		
		// fix for the extra notification bar space (when taking away the bar the usable space does not increase as it should... so add it manually.
		//Rect rectgle= new Rect();
		//Window window= game.getWindow();
		//window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
		//int statusBarHeight = rectgle.top;

		
		// get crop & scale dim
		/*
		scaledWidth = (int) game.getWorld().mScreenWidth/CA_Game.COLCOUNT;
		double scaleRatio =  scaledWidth / (double) mWidth  ;
		scaledHeight = (int) ( scaleRatio * mHeight );		
		scaledCropHeightOffset = (int) (game.getWorld().mScreenHeight - scaledHeight) + statusBarHeight;
		scaledCropHeight = (int) (scaledHeight - Math.abs(scaledCropHeightOffset));
		cropHeight = (int) (scaledCropHeight/scaleRatio);
		*/
		
		
		// TEMP TEMP TEMP
		/*
		scaledWidth = mWidth;
		scaledHeight = mHeight;
		cropHeight = mHeight;
		scaledCropHeight = mHeight;
		*/
		scaledWidth = (int) (game.getWorld().mScreenWidth/(float)MainActivity.COLCOUNT/7f);
		scaledHeight = mHeight;
		cropHeight = mHeight;
		scaledCropHeight = 0;//(int)(game.getWorld().mScreenHeight*0.75);
		
		totalPixHeight = (int) (game.getWorld().mScreenHeight - (game.getWorld().mScreenHeight* DropSectionState.FOLDED.getHeight()/2 ) );
		
		// set position based off of scaled info
		
		if (isLeft)
			setPos(0, 0, true);
		else
			setPos((int)(game.getWorld().mScreenWidth - scaledWidth), 0, true);
		
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
		
		//gl.glEnable(GL10.GL_BLEND);
		//gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
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


	    //gl.glDisable(GL10.GL_BLEND);
	    
	    bitmap.recycle();
		
	}
	
	public synchronized void updateRow(int newRow)
	{
		if (newRow != mLastEventRow)
		{
			// set transform
			mLastEventRow = newRow;
			start((float)mLastEventRow);
			
			Color nextColor = Color.NONE;
			
			// set color
			if (mLastEventRow >= MainActivity.ROWCOUNT*0.7)
			{
				// Imminent
				nextColor = IMMINENT;
			}
			else if ( mLastEventRow >= MainActivity.ROWCOUNT*0.5 && mLastEventRow < MainActivity.ROWCOUNT*0.7)
			{
				// warning
				nextColor = WARNING;
			}
			else
			{
				// neutral
				nextColor = NEUTRAL;
			}
			
			// change color
			if (mColor.mCurrentColor != nextColor)
			{
				mColor.enqueue( 
					new ColorState( 
							new MotionEquation[] {MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR, MotionEquation.LINEAR}, 
							mColor.mCurrentColor, 
							nextColor,
							new int[] {mDuration, mDuration, mDuration, mDuration},
							new int[] {0,0,0,0}	// relative to FIFO q
							));	
			}
			
		}
	}
	
	private void start(float endRow)
	{
		// stop any previous transforms
		stop();
		
		mStartPoint = mCurrentRow; //where i am now
		mEndPoint = endRow;	// trigger point
		mStartTime = System.currentTimeMillis();
		isTransforming = true;
	}
	
	private void stop()
	{
		isTransforming = false;
	}
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		if (primaryThread)
		{
			
			
			
			// Process color
			if (!mColor.isEmpty())
				mColor.processColorElements(now);
			
			
			if (isTransforming)
			{
				mCurrentRow = (float) MotionEquation.applyFinite(
						TransformType.TRANSLATE, 
						MotionEquation.LOGISTIC, 
						Math.max( 0, Math.min( now - mStartTime , mDuration ) ), 
						mDuration, 
						mStartPoint, 
						mEndPoint
						);
				if (Math.max( 0, Math.min( now - mStartTime , mDuration ) ) == mDuration)
				{
					stop();
					// prevent settling in the wrong place
					mCurrentRow = mEndPoint;
				}
			}
			
			scaledCropHeight = (int) (totalPixHeight*((float)mCurrentRow/(MainActivity.ROWCOUNT-1)));
			
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
				yPos,	// y
				World.GLEX11HUD_Z_ORDER,
				scaledWidth,
				scaledCropHeight );
		
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
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		//game.text = mCurrentRow + " of " + (CA_Game.ROWCOUNT-1) + "\n";
		//game.text += (mCurrentRow/(CA_Game.ROWCOUNT-1)) + "\n";
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
