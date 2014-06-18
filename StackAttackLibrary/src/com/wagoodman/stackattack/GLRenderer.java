package com.wagoodman.stackattack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

//import wagoodman.com.R;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import android.view.Display;
import android.view.Window;
import android.view.WindowManager;

class GLRenderer implements GLSurfaceView.Renderer
{
	//GLCube cube = new GLCube(8700, 1, 1, 1, 0.5f);
	
	
	private static final String TAG = "Renderer";
	private static final Boolean debug = false;
	private final MainActivity game;
	private final Context mContext;

	public Boolean mStartedDrawingFrames = false;
	
	// Screen Information
	public int mScreenWidth = 0;
	public int mScreenHeight = 0;
	public float mScreenRatio = 0;
	public final float mFovy;
	
	// Camera Info
	public float mCameraPitch 	= 0f;
	public float mCameraHeading	= 0f;

	// Alt FPS Info
	private long mFrameStartTime = 1;
	public double mFrameDuration = 1;
	public int mFrameCount = 1;
	
	GLRenderer(Context context, float fovy)
	{
		mContext = context;
		mFovy = fovy;
		game = (MainActivity) (context);
	}
	


	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		
		
		// set background color
		gl.glClearColor(0.15686f, 0.14902f, 0.14902f, 1f);
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		
		//And there'll be light!
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, game.getWorld().mLightAmbientBuffer);		//Setup The Ambient Light 
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, game.getWorld().mLightDiffuseBuffer);		//Setup The Diffuse Light 
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, game.getWorld().mLightPositionBuffer);	//Position The Light 
		
		//gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, game.getWorld().mLightSpecular, 0);
		//gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, game.getWorld().mGlobalAmbient, 0);
		gl.glEnable(GL10.GL_LIGHT0);														//Enable Light 0
		gl.glEnable(GL10.GL_LIGHTING);

		
		/*
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, game.getWorld().mLightAmbient, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, game.getWorld().mLightDiffuse, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, game.getWorld().mLightSpecular, 0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, game.getWorld().mLightPos, 0);
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, game.getWorld().mGlobalAmbient, 0);
		*/

		// Faster
		//gl.glDisable(GL10.GL_DITHER);

		
		
		// one or the other, not both
		//gl.glEnable(GL10.GL_NORMALIZE);
		gl.glEnable(GL10.GL_RESCALE_NORMAL);
		
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping 
		
		
		gl.glEnable(GL10.GL_DEPTH_TEST); 			// Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			// The Type Of Depth Testing To Do
		
		// Hint engine to scale textures with respect to perspective
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		
	}

	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		
		mScreenWidth = width; 
		mScreenHeight = height; 
		
		//game.text =  "Given : " + width + " x " + height + "\n";
		//game.text += "WinMan: " + mScreenWidth + " x " + mScreenHeight + "\n";
		//game.textviewHandler.post( game.updateTextView );
		
		//mScreenWidth = width;
		//mScreenHeight = height;
		mScreenRatio = (float) mScreenWidth / (float) mScreenHeight;

		// Define the view frustum
		gluPerspective(gl);

		// update world distances based on new dimensions
		game.getWorld().updateWorldDistances(gl, mScreenWidth, mScreenHeight, mScreenRatio);
		
		// kickoff 
		mStartedDrawingFrames = true;
		mFrameStartTime = System.currentTimeMillis();

	}

	
	
	
	/**
	 * Sets the projection to the ortho matrix
	 */
	public void gluOrtho2D(GL10 gl)
	{
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
	    gl.glMatrixMode( GL10.GL_PROJECTION );
	    gl.glLoadIdentity();
	    GLU.gluOrtho2D( gl, 0, mScreenWidth, 0, mScreenHeight );
	    gl.glMatrixMode( GL10.GL_MODELVIEW );
	    gl.glLoadIdentity();
	}

	/**
	 * Sets the projection to the perspective matrix
	 */
	public void gluPerspective(GL10 gl)
	{
		gl.glViewport(0, 0, mScreenWidth, mScreenHeight);
	    gl.glMatrixMode( GL10.GL_PROJECTION );
	    gl.glLoadIdentity();
	    GLU.gluPerspective( gl, mFovy, mScreenRatio, World.mMinDepth, World.mMaxDepth );
	    gl.glMatrixMode( GL10.GL_MODELVIEW );
	    gl.glLoadIdentity();
	}


	/**
	 * Sets the projection to the model view matrix
	 */
	public void gluLookAt(GL10 gl, 
	        float positionX, float positionY, float positionZ,
	        float zentrumX, float zentrumY, float zentrumZ,
	        float upX, float upY, float upZ ){

	    gl.glMatrixMode( GL10.GL_MODELVIEW );
	    gl.glLoadIdentity();
	    GLU.gluLookAt( gl,positionX, positionY, positionZ, zentrumX, zentrumY, zentrumZ, upX, upY, upZ );
	}
	
	
	
	private float xrot;				//X Rotation ( NEW )
	private float yrot;				//Y Rotation ( NEW )
	private float zrot;				//Z Rotation ( NEW )
	
	
	
	
	
	
	private Boolean drawnFirstFrame = false;
	
	
	
	
	//long mLastTime = 0;
	public void onDrawFrame(GL10 gl)
	{
		if (!drawnFirstFrame)
		{
			// dismiss loading screen
			game.loadedWorld();
			drawnFirstFrame = true;
		}
		// Reset...
		//gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode( GL10.GL_MODELVIEW );
	    gl.glLoadIdentity();
		
	    /*
	    game.text  = "isGameStarted ? " + game.isGameStarted + "\n";
	    game.text += "isGameOver    ? " + game.isGameOver + "\n";
	    game.text += "isGamePaused  ? " + game.isGamePaused + "\n";
	    game.text += "Boards Paused ? " + game.getWorld().mBoards.mPauseBoardProgression + "\n";
	    game.text += "Row Iter %    ? " + game.getWorld().mBoards.mRowIterationPercentage + "\n";
	    game.text += "Row Per Min   ? " + game.getWorld().mBoards.mRowPerMin + "\n";
	    game.text += "Primary Thread: " + game.getWorld().mBlockUpdater.state + "\n";
	    game.text += "Second. Thread: " + game.getWorld().mBoardUpdater.state + "\n";
	    game.textviewHandler.post( game.updateTextView );
		*/
	    
		// draw all boards
		game.getWorld().draw(gl);

	    
		// For FPS calc
		mFrameCount++;
		
	}
	
	
	


}
