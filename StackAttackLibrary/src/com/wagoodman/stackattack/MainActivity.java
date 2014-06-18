package com.wagoodman.stackattack;


import com.wagoodman.stackattack.GameDifficulty;
import com.wagoodman.stackattack.GameMode;
import com.wagoodman.stackattack.MotionEquation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.OrientationEventListener;


public interface MainActivity
{
	public final static int ROWCOUNT = 9;  //8; 9
	public final static int COLCOUNT = 7;  //6; 7
	
	// LIBRARY VAR HOOKS
	public Boolean getIsPaid();
	public int getFreeVersionMaxPoints();
	
	// Game specific
	public Boolean getIsGameStarted();
	public Boolean getIsGameOver();
	public Boolean getIsGamePaused();
	public Boolean getHasShownSupportMeDialog();
	public void setIsGameStarted(Boolean flag);
	public void setIsGameOver(Boolean flag);
	public void setIsGamePaused(Boolean flag);
	public void setHasShownSupportMeDialog(Boolean hasShownSupportMeDialog);
	
	// Sensor / Orientation objects
	public OrientationEventListener getOrientationListener();
	public Orientation getGlobalOrient();
	public void setGlobalOrient(Orientation ori);
	public Vibrator getVibrator();
	
	// For handling vibrating motor
	public long[] getErrorVibratePattern();
	public Handler getVibratorHandler();
	public Runnable getErrorVibratorSequence();
	public long[] getMenuSelectVibratePattern();
	public Runnable getMenuSelectVibratorSequence();	
		
	// Game data
	public World getWorld();
	public void setWorld(World world);
	
	public SharedPreferences getPreferences();
	public void setPreferences(SharedPreferences sp);
	
	public SharedPreferences.Editor getPreferenceEditor();
	public void setPreferenceEditor(Editor spe);
	
	public void onCreate(Bundle savedInstanceState);

	public void onSaveInstanceState(Bundle savedInstanceState) ;

	public void showBuyMeDialog();
	
	public void onPause();

	public void onStop();

	public void onDestroy();

	public boolean onKeyDown(int keyCode, KeyEvent event) ;
	
	public void initGame(GameDifficulty gd, GameMode gm);
	
	public void initGame();
	
	public void startGame();
	
	public void startGame(Integer dur, MotionEquation eq, Boolean doTutorial);
	
	public void endGame(int delay);
	
	public void endGame();
	
	public void finish();

	public void startActivity(Intent settingsActivity);

	public Context getContext();
	
	public Context getBaseContext();
	
	public AudioManager getAudioManager();
	
	public void loadedWorld();

}