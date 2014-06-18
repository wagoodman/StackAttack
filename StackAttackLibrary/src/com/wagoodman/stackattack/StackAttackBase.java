package com.wagoodman.stackattack;


import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.wagoodman.stackattack.GameDifficulty;
import com.wagoodman.stackattack.GameMode;
import com.wagoodman.stackattack.MotionEquation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;




public abstract class StackAttackBase extends Activity implements MainActivity 
{

	public static Context app;
	

	
	// Game specific
	public Boolean isGameStarted = false;
	public Boolean isGameOver = false;
	public Boolean isGamePaused = false;
	public Boolean hasShownSupportMeDialog = false;
	
	// Audio
	public AudioManager mAudioManager;
	
	// Sensor / Orientation objects
	public OrientationEventListener orientationListener;
	public Orientation mGlobalOrient = Orientation.NORMAL;
	public Vibrator mVibrator;
	
	// For handling vibrating motor
	public long[] mErrorVibratePattern = {0,50,50,50,50,50};
	final Handler vibratorHandler = new Handler();
	final Runnable errorVibratorSequence = new Runnable()
	{
		public void run()
		{
			mVibrator.vibrate(mErrorVibratePattern, -1);
		}
	};	
	public long[] mMenuSelectVibratePattern = {0,15};
	final Runnable menuSelectVibratorSequence = new Runnable()
	{
		public void run()
		{
			mVibrator.vibrate(mMenuSelectVibratePattern, -1);
		}
	};	
	
	// Dialogs
	public AlertDialog.Builder dialogBuilder;
	public AlertDialog loadingDialog;
	public AlertDialog buymeDialog;
	
	// Game data
	public static int ROWCOUNT = 9;  //8; 9
	public static int COLCOUNT = 7;  //6; 7
	public World world = null;

	public SharedPreferences mPreferences = null;
	public SharedPreferences.Editor mPreferenceEditor = null;

	
	final Handler buymeHandler = new Handler();
	final Runnable showBuyMe = new Runnable()
	{
		public void run()
		{
			buymeDialog.show();
		}
	};
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		
		app = getApplicationContext(); // don't use 'this': http://android-developers.blogspot.com/2009/01/avoiding-memory-leaks.html
		
		mPreferences = PreferenceManager.getDefaultSharedPreferences(app);
		
		// Make the windows into full screen mode.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
    	// Loading...
		dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.loading, (ViewGroup)findViewById(R.id.root));
		dialogBuilder.setView(layout);
		loadingDialog = dialogBuilder.create();
		showLoadingDialog();
		
		// make buy me dialog
		dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Support the Developer");
		dialogBuilder.setMessage("I hope you've enjoyed the game so far! Please consider supporting the developer by buying the app on Google Play!\n\nDon't worry, you can keep playing the free one as long as you would like :)");
		dialogBuilder.setPositiveButton("Buy", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent updateIntent = null;

			        updateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.wagoodman.stackattack.full"));

			        startActivity(updateIntent); 
				}
			}
		);
		dialogBuilder.setNegativeButton("No Thanks", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					
				}
			}
		);
		buymeDialog = dialogBuilder.create();
		
		// set volume control
		mAudioManager =  (AudioManager)app.getSystemService(Context.AUDIO_SERVICE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (  (double) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*0.6 ), 0);
	
		// create gl world
		world = new World(this);
		setContentView(world);
		
		// Set Default (and perminent) orientation
		world.mBoards.setNextOrientation(Orientation.NORMAL);
		mGlobalOrient = Orientation.NORMAL;

		// set vibration motor control
		mVibrator = (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);

	}
	
	public void showLoadingDialog()
	{
		loadingDialog.show();
	}
	

	
	@Override
	public void showBuyMeDialog()
	{
		buymeHandler.post(showBuyMe);
	}

	
	@Override
	public void loadedWorld()
	{
		loadingDialog.dismiss();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
		if (world.mTutorialState.isInTutorial)
		{
			// get out of the tutorial...
			
			initGame( world.mBoards.mGameState.mGameDiff, world.mBoards.mGameState.mGameMode );
			setIsGameOver(false);
			setIsGameStarted(false);
			world.mMenus.transitionToMenu(MenuManager.MAINMENU, true,  DropSection.DEFAULT_DURATION,  DropSection.SLOW_EQ);
			
			// Disable Tutorial on the way out!
			world.mTutorialState.goTo(TutorialBannerMenu.NONE, true);
		}
		else
		{
			// pause...
			world.pauseWorld(true);
		}
		
		super.onSaveInstanceState(savedInstanceState);    
	}  
	
	
	@Override
	public void onPause()
	{
		super.onPause();
		//Debug.stopMethodTracing();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		//Debug.stopMethodTracing();
	}
	
	@Override
	public void onDestroy() {        
	    super.onDestroy();
	    //Debug.stopMethodTracing();
	}

	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	    if (keyCode == KeyEvent.KEYCODE_MENU) 
	    {
	    	// only functional during game & not in tutorial
	    	if (isGameStarted && !isGameOver && world.mBoards.mGameState.hasGameStarted && !world.mTutorialState.isInTutorial)
	    	{  		
		    	// pause game & show menu
		    	if (!isGamePaused)
		    	{
		    		world.pauseWorld(true);
		    	}
		    	else
		    	{
		    		world.resumeWorld(true);
		    	}
	    	}
	    	
	    	// let the OS continue with typical behavior
		    return super.onKeyDown(keyCode, event);
	    	
	    }
	    else if (keyCode == KeyEvent.KEYCODE_BACK)
	    {
	    	if (!world.mMenus.gotoBackMenu())
	    	{
	    		// let the OS continue with typical behavior
	    	    return super.onKeyDown(keyCode, event);
	    	}
	    }  
	    else
	    {
	    	// let the OS continue with typical behavior
		    return super.onKeyDown(keyCode, event);
	    }
	    
	    return true;
	    
	}
	
	
	public void initGame(GameDifficulty gd, GameMode gm)
	{
		world.resetWorld(gd, gm);
		world.pauseWorld(false);
	}
	
	public void initGame()
	{
		world.resetWorld();
		world.pauseWorld(false);
	}
	
	public void startGame()
	{
		startGame(null, null, false);
	}
	
	public void startGame(Integer dur, MotionEquation eq, Boolean doTutorial)
	{
		if (!isGameStarted)
		{
			isGameOver = false;
			isGameStarted = true;
			
			if (doTutorial)
				world.startTutorial(dur, eq);
			else
				world.startGame(dur, eq);
		}
	}
	
	public void endGame(int delay)
	{
		if (!world.mTutorialState.isInTutorial)
		{
			world.endGame(delay);
			isGameStarted = false;
		}
	}
	
	public void endGame()
	{
		if (!world.mTutorialState.isInTutorial)
		{
			world.endGame();
			isGameStarted = false;
		}
	}


	@Override
	public Context getContext() {
		return app;
	}

	@Override
	public long[] getErrorVibratePattern() {
		return this.mErrorVibratePattern;
	}

	@Override
	public Runnable getErrorVibratorSequence() {
		return this.errorVibratorSequence;
	}

	@Override
	public Orientation getGlobalOrient() {
		return this.mGlobalOrient;
	}

	@Override
	public Boolean getIsGameOver() {
		return this.isGameOver;
	}

	@Override
	public Boolean getIsGamePaused() {
		return this.isGamePaused;
	}

	@Override
	public Boolean getIsGameStarted() {
		return this.isGameStarted;
	}

	@Override
	public Boolean getHasShownSupportMeDialog() {
		return this.hasShownSupportMeDialog;
	}

	@Override
	public long[] getMenuSelectVibratePattern() {
		return this.mMenuSelectVibratePattern;
	}

	@Override
	public Runnable getMenuSelectVibratorSequence() {
		return this.menuSelectVibratorSequence;
	}

	@Override
	public OrientationEventListener getOrientationListener() {
		return this.orientationListener;
	}

	@Override
	public Editor getPreferenceEditor() {
		return this.mPreferenceEditor;
	}

	@Override
	public SharedPreferences getPreferences() {
		return this.mPreferences;
	}

	@Override
	public Vibrator getVibrator() {
		return this.mVibrator;
	}

	@Override
	public Handler getVibratorHandler() {
		return this.vibratorHandler;
	}

	@Override
	public World getWorld() {
		return this.world;
	}

	@Override
	public void setGlobalOrient(Orientation ori) {
		this.mGlobalOrient = ori;
	}

	@Override
	public void setIsGameOver(Boolean isGameOver) {
		this.isGameOver = isGameOver;
	}

	@Override
	public void setIsGamePaused(Boolean isGamePaused) {
		this.isGamePaused = isGamePaused;
	}

	@Override
	public void setIsGameStarted(Boolean isGameStarted) {
		this.isGameStarted = isGameStarted;
	}

	@Override
	public void setHasShownSupportMeDialog(Boolean hasShownSupportMeDialog) {
		this.hasShownSupportMeDialog = hasShownSupportMeDialog;
	}
	
	@Override
	public void setPreferenceEditor(Editor pe) {
		this.mPreferenceEditor = pe;
	}

	@Override
	public void setPreferences(SharedPreferences p) {
		this.mPreferences = p;
	}

	@Override
	public void setWorld(World world) {
		this.world = world;
	}

	
	@Override
	public AudioManager getAudioManager() {
		return this.mAudioManager;
	}
	
		
	

}