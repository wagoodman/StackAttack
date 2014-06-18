package com.wagoodman.stackattack;

import java.util.Iterator;

import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.view.SurfaceHolder;

/**
 * This class is responsible for updating positional and animation information
 * for all objects in the world before the renderer draws the scene.
 * 
 * @author Alex Goodman
 * 
 */
public class PrimaryUpdater extends Thread
{
	private static final String TAG = "Updater";
	private static final Boolean debug = false;
	private Context mContext;
	private final MainActivity game;

	// Controls how often this thread updates information
	private Boolean measureFPS = true;
	public double measuredFPS = 45;							// the current fps the device is processing
	private double updateInterval = 1000 / measuredFPS;		// based on fps, the fastest that the block data should be updated (ms)
	private int frameCount = 0;
	private long sleepTime;									// per iteration sleep interval

	public Integer mFps = 0;
	
	public String mFPSReadout = "";
	
	// update the sleep time dynamically to match the current fps read
	
	public final Handler  fpsHandler = new Handler();
	public final Runnable updateMeasuredFPS = new Runnable()
	{
		public void run()
		{
			updateBlockSleepInterval();
		}
	};
	
	
	
	// state of game (Running or Paused).
	int state = 1;
	public final static int RUNNING = 1;
	public final static int PAUSED = 2;

	public PrimaryUpdater(Context context)
	{
		// get the game object from context
		game = (MainActivity) (context);

		// data about the screen
		mContext = context;

		// Commit FPS Preference
		if (game.getPreferences().getBoolean("showFPS", false))
		{
			measureFPS = true;
		}
		else
		{
			measureFPS = false;
		}
		
	}


	
	/**
	 * updates the sleep interval based on the current measured fps value.
	 * 
	 */
	
	private void updateBlockSleepInterval()
	{
		if (measuredFPS >= 30 && measuredFPS <= 58)
			updateInterval = 1000 / measuredFPS;
	}
	

	// The thread is started when the call to start() is made from the SurfaceView class.
	// It loops continuously until the game is finished or the application is suspended.
	
	@Override
	public void run()
	{
		//Debug.startMethodTracing("CAGAME");
		
		long lastUpdateTimestamp = 0;
		long lastPost = 0;
		long lastIntervalTimestamp = 0;
		double currentFrameDuration = 0;
		
		
		// wait for renderer
		while (!game.getWorld().mRenderer.mStartedDrawingFrames)
		{
			
			try
			{
				sleep((long) 30.0);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		// init
		game.getWorld().mBoards.update(System.currentTimeMillis(), true, false);
		
		// run!
		while (state == RUNNING)
		{

			lastUpdateTimestamp = System.currentTimeMillis();

			if (game != null)
			{
				
				// update all visible block elements & score board
				game.getWorld().update(lastUpdateTimestamp, true, false);
				
				if (measureFPS)
				{
				
					// update the interval to sleep at
					if ( (lastUpdateTimestamp - lastIntervalTimestamp) > 1500)
					{
						/// METHOD 1; instant fps (less accurate)
						/*
						currentFrameDuration = Math.abs(game.getWorld().mRenderer.mFrameDuration);	
						
						if ( currentFrameDuration > 35 && currentFrameDuration < 58 )
							updateInterval = currentFrameDuration;
						
						// FPS
						fps =  "Frame MS  : " + currentFrameDuration+"\n";
						fps += "FPS       : " + (int)( 1000.0/currentFrameDuration ) +"\n";
						//game.text = fps;
						//game.textviewHandler.post( game.updateTextView );
						
						// SCORE
						game.text = fps+  "Score: "+game.getWorld().mBoards.mScoreValue+"\n";
						game.text += "Mult : x"+game.getWorld().mBoards.mScoreComboMultiplier + "\n";
						game.textviewHandler.post( game.updateTextView );
						
	
						lastIntervalTimestamp =	lastUpdateTimestamp;
						*/
						
						
						/// METHOD 2 ; avg fps
						
						frameCount = game.getWorld().mRenderer.mFrameCount;
						game.getWorld().mRenderer.mFrameCount = 1;
						currentFrameDuration = (lastUpdateTimestamp - lastIntervalTimestamp)/frameCount;
						
						if ( currentFrameDuration > 35 && currentFrameDuration < 58 )
							updateInterval = currentFrameDuration;
						
						
						// FPS
						mFps = (int)( 1000.0/currentFrameDuration );
						
						//mFPSReadout  = "FPS       : " + mFps +"\n";
						//mFPSReadout += "Frame MS  : " + currentFrameDuration+"\n";
						
						// post a toast!
						
						if ( (lastUpdateTimestamp - lastPost) > 3000)
						{
							game.getWorld().toastHandler.post(new Runnable()
							{
								public void run()
								{
									game.getWorld().postToast("FPS = " + (int)(mFps));
		
								}
							});
							lastPost = lastUpdateTimestamp;
						}
						
						
						
						
						
						
						
						//game.text = fps;
						//game.textviewHandler.post( game.updateTextView );
						
						/*
						// SCORE
						game.text = fps+  "Score: "+game.getWorld().mBoards.mScoreValue+"\n";
						game.text += "Mult : x"+game.getWorld().mBoards.mScoreComboMultiplier + "\n";
						game.text += game.getWorld().mBoards.getCurrentBoard().mGenerator.report();
						game.textviewHandler.post( game.updateTextView );
						*/
						
						lastIntervalTimestamp =	lastUpdateTimestamp;					
						
					}
				}
				

			}

			// Sleep ... :)
			try
			{
				// The amount of time to sleep before updating the game elemnts again
				sleepTime = (long) (updateInterval - (System.currentTimeMillis() - lastUpdateTimestamp));

				// actual sleep code
				if (sleepTime > 0)
				{
					sleep(sleepTime);
				}
			}
			catch (InterruptedException ex)
			{
				// Log
			}

		}
		
		//Debug.stopMethodTracing();

	}

}
