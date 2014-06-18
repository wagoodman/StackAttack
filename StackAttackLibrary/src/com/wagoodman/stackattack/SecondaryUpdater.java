package com.wagoodman.stackattack;

import java.util.Iterator;

import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;

/**
 * This class is responsible for updating positional and animation information
 * for all objects in the world before the renderer draws the scene.
 * 
 * @author Alex Goodman
 * 
 */
public class SecondaryUpdater extends Thread
{
	private static final String TAG = "Updater";
	private static final Boolean debug = false;
	private Context mContext;
	private final MainActivity game;

	// Controls how often this thread updates information
	private double updateInterval = 100; //300			// fastest board update time (ms)
	private long sleepTime;							// per iteration sleep interval


	
	
	// state of game (Running or Paused).
	int state = 1;
	public final static int RUNNING = 1;
	public final static int PAUSED = 2;

	public SecondaryUpdater(Context context)
	{
		// get the game object from context
		game = (MainActivity) (context);

		// data about the screen
		mContext = context;

	}

	

	// The thread is started when the call to start() is made from the SurfaceView class.
	// It loops continuously until the game is finished or the application is suspended.
	@Override
	public void run()
	{
		
		long lastUpdateTimestamp = 0;

		// wait for renderer
		while (!game.getWorld().mRenderer.mStartedDrawingFrames)
		{
			
			try
			{
				sleep((long) 50.0);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
		
		// init
		// don't populate boards here!!!
		
		// run!
		while (state == RUNNING)
		{

			lastUpdateTimestamp = System.currentTimeMillis();

			if (game != null)
			{
				
				// update all visible boards only
				game.getWorld().update(lastUpdateTimestamp, false, true);
				
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

	}

}
