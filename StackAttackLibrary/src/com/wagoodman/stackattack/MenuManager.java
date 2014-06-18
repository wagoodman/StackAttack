package com.wagoodman.stackattack;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.AppPreferences;
import com.wagoodman.stackattack.FontManager;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.World;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;


public class MenuManager
{

	private final Context mContext;
	private final MainActivity game;
	
	public static final String NONE			 = "none";	//call outro of current menu only
	public static final String MAINMENU      = "main";
	public static final String INGAMEMENU    = "ingame";
	public static final String SINGLEPLAYERMENU  = "gameselectdiffmode";
	public static final String ENDOFGAMEMENU = "endgame";
	public static final String ABOUT = "about";
	
	public int mTopBoarder = 10;
	public int mHorizontalBoarder = 10;
	
	private final int mFlashTime = 5000;
	
	private ConcurrentHashMap<String, Menu> mMenus = new ConcurrentHashMap<String, Menu>();
	private ConcurrentHashMap<String, DropSectionState> mMenuHeight = new ConcurrentHashMap<String, DropSectionState>();
	private ConcurrentHashMap<String, Float> mMenuTopBorder = new ConcurrentHashMap<String, Float>();
	// holy hell this is cool: http://www.onkarjoshi.com/blog/201/concurrenthashset-in-java-from-concurrenthashmap/
	private String mMostCurrentMenu = NONE;
	private Set<String> mCurrentMenus = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
	private ConcurrentHashMap<String, Long> mNonCurrentMenuTrigger = new ConcurrentHashMap<String, Long>();

	private Boolean isLocked = false;
	
	public MenuManager(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;

		
	}
	
	public void lock()
	{
		isLocked = true;
	}
	
	public void unlock()
	{
		isLocked = false;
	}
	
	public void setDependencies()
	{
		for (Menu menu : mMenus.values())
			menu.setDependencies();			
	}
	
	float fontHeight;
	private int getVeriticalPosition(String menu, double pos, float fontScaleOffset)
	{
		fontHeight = game.getWorld().mDropSection.mFonts.getFontHeight(FontManager.MENUMAJORITEM_FONT)*fontScaleOffset;
		
		// width 2
		return (int) (mMenuHeight.get(menu).getHeight()*game.getWorld().mScreenHeight - mMenuTopBorder.get(menu) - 4*mTopBoarder - (pos*2*fontHeight + fontHeight));
		
	}
	
	private int getVeriticalPosition(String menu, double pos)
	{
		fontHeight = game.getWorld().mDropSection.mFonts.getFontHeight(FontManager.MENUMAJORITEM_FONT);
		
		// width 2
		return (int) (mMenuHeight.get(menu).getHeight()*game.getWorld().mScreenHeight - mMenuTopBorder.get(menu) - 4*mTopBoarder - (pos*2*fontHeight + fontHeight));
		
	}
	
	private int getCenteredPosition(int width)
	{
		return (int) (game.getWorld().mScreenWidth/2 - width/2) ;
	}
	
	private int getCenteredPosition(String phrase)
	{
		return (int) (game.getWorld().mScreenWidth/2 - game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMAJORITEM_FONT, phrase)/2) ;
	}

	private int getCenteredPosition(String phrase, float fontScaleOffset)
	{
		return (int) (game.getWorld().mScreenWidth/2 - (game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMAJORITEM_FONT, phrase)*fontScaleOffset)/2f) ;
	}
	
	private int getCenteredPosition(String phrase, String font, float fontScaleOffset)
	{
		return (int) (game.getWorld().mScreenWidth/2 - (game.getWorld().mDropSection.mFonts.getStringWidth(font, phrase)*fontScaleOffset)/2) ;
	}
	
	private int getLeftPosition(String phrase, int pos)
	{
		return pos;
	}
	
	private int getRightPosition(String phrase, int pos)
	{
		return (int) (pos - game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMAJORITEM_FONT, phrase)) ;
	}
	
	
	public void addMenu(String menuTitle, Menu menu)
	{
		mMenus.put(menuTitle, menu);
	}
	
	
	
	public Menu getMenu(String menuTitle)
	{
		return mMenus.get(menuTitle);
	}

	public void transitionToMenu(String menuTitle, Boolean doOutro, Integer duration, MotionEquation eq)
	{
		transitionToMenu(menuTitle, doOutro, duration, eq, null);
	}
	
	public void transitionToMenu(String menuTitle, Boolean doOutro, Integer drop_duration, MotionEquation drop_eq, Integer text_duration)
	{
		if (isLocked)
			return;
		
		//Log.e("TRANSITION", menuTitle);
		
		// if the current menu is the given title and is not scheduled to be removed...
		if (mCurrentMenus.contains(menuTitle) && !mNonCurrentMenuTrigger.containsKey(menuTitle))
		{
			// set the height
			if (drop_duration == null || drop_eq == null)
				game.getWorld().mDropSection.changeStateTo(mMenuHeight.get(menuTitle));
			else
				game.getWorld().mDropSection.changeStateTo(mMenuHeight.get(menuTitle), drop_duration, drop_eq);
			
			// dont do anything else!
			return;
		}
			
			
		// cancel any menus on outro, prepare for intro
		mNonCurrentMenuTrigger.remove(menuTitle);
		
		synchronized(mCurrentMenus)
		{
			for (String menu : mCurrentMenus)
			{
				if (menuTitle != menu)
				{
					// only commit outro if it has not recently been committed...
					if (!mNonCurrentMenuTrigger.containsKey(menu))
					{
						// instructed to do outro?
						if (doOutro)
						{
							// start outro
							mMenus.get(menu).outro();
							
							// trigger menu to not be updated
							mNonCurrentMenuTrigger.put(menu, mMenus.get(menu).getOutroDuration() + System.currentTimeMillis());
						}
						else
						{
							mNonCurrentMenuTrigger.put(menu, System.currentTimeMillis());
						}
					}
				}
			}
		}


		if (menuTitle != NONE)
		{
			// set the height
			if (drop_duration == null || drop_eq == null)
				game.getWorld().mDropSection.changeStateTo(mMenuHeight.get(menuTitle));
			else
				game.getWorld().mDropSection.changeStateTo(mMenuHeight.get(menuTitle), drop_duration, drop_eq);
			
			// start intro
			if (text_duration == null)
				mMenus.get(menuTitle).intro();
			else
				mMenus.get(menuTitle).intro(text_duration);
			
			synchronized(mCurrentMenus)
			{
				// add new menu to current menus
				mCurrentMenus.add(menuTitle);
				mMostCurrentMenu = menuTitle;
			}
		}
		
	}
	
	public void outro()
	{
		synchronized(mCurrentMenus)
		{
			for (String menu : mCurrentMenus)
			{

				// start outro
				mMenus.get(menu).outro();
				
				// trigger menu to not be upated
				mNonCurrentMenuTrigger.put(menu, mMenus.get(menu).getOutroDuration() + System.currentTimeMillis());
			
			}
		}
	}
	
	/*
	public void hide()
	{
		for (String menu : mCurrentMenus)
			mMenus.get(menu).hide();
		
	}
	*/
	
	public void interact(int x, int y, int pixFontOffset)
	{
		synchronized(mCurrentMenus)
		{
			for (String menu : mCurrentMenus)
				mMenus.get(menu).interact(x, y, pixFontOffset);
		}
	}
	
	public Boolean gotoBackMenu()
	{
		synchronized(mCurrentMenus)
		{
			if (mCurrentMenus.contains(mMostCurrentMenu))
			{
				// only apply to the most current menu
				return mMenus.get(mMostCurrentMenu).gotoBackMenu(null, null);
			}
		}
		
		return false;
	}
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		/*
		game.text = "MENUS:\n";
		for (String menu : mMenus.keySet())
		{
			game.text += "   "+menu + "\n";
			for (GLMenuItem item : mMenus.get(menu).mItems)
			{
				game.text += "      "+((GLButton<Void>)(item)).mLabel + " : " + ((GLButton<Void>)(item)).mAction.toString() + "\n";
			}
		}
		*/
		
		/*
		game.text += "\n\nCURRENT:\n";
		for (String menu : mCurrentMenus)
			game.text += "   "+menu + "\n";
		*/
		
		//game.textviewHandler.post(game.updateTextView);
		
		

		// remove any menus from the currentMenu array (if needed)
		if (secondaryThread)
		{
			if (mNonCurrentMenuTrigger.size() > 0)
			{
				for (Map.Entry<String, Long> entry : mNonCurrentMenuTrigger.entrySet())
				{
					if (entry.getValue() < now)
					{
						synchronized(mCurrentMenus)
						{
							// remove from the list
							mCurrentMenus.remove(entry.getKey());
						}
						// remove trigger
						mNonCurrentMenuTrigger.remove(entry.getKey());
					}
				}
			}
		}
	
		synchronized(mCurrentMenus)
		{
			// do updates
			for (String menu : mCurrentMenus)
				mMenus.get(menu).update(now, primaryThread, secondaryThread);
		}
	}

	public void draw(GL10 gl, float pixYOffset)
	{

		
		synchronized(mCurrentMenus)
		{
			for (String menu : mCurrentMenus)
				mMenus.get(menu).draw(gl, pixYOffset);
		}
		

		
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MENUS!
	
	private GLButton gen_menuButton;
	
	public void generateMenus()
	{

		Menu menu;
		String text;
		String[] soundText;
		
		if (game.getWorld().isSoundEnabled())
		{
			soundText = new String[] {"Turn Sound Off", "Turn Sound  On" };
		}
		else
		{
			soundText = new String[] {"Turn Sound  On", "Turn Sound Off" };
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// ABOUT MENU
		
		menu = new Menu(mContext, MAINMENU);
		// set height first
		mMenuHeight.put(ABOUT, DropSectionState.FULL);
		mMenuTopBorder.put(ABOUT, game.getWorld().mScreenHeight*0.27f);

		float scale = 0.87f;
		
		
		text = "Developed By";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuSecondaryForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(ABOUT, 0, scale) , true, scale,    //x, y, leftjust?, fontScale
						null,
						false
					)
				);
		
		text = "Alex Goodman";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
						text, (int) (game.getWorld().mScreenWidth*0.1), getVeriticalPosition(ABOUT, 0.8, scale) , true, scale,    //x, y, leftjust?, fontScale
						null,
						false
					)		
				);
		
		text = "Graphics By";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuSecondaryForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(ABOUT,  1.8, scale) , true, scale,    //x, y, leftjust?, fontScale
						null,
						false
					)
				);
		
		text = "Alli Daniello & Alex Goodman";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(ABOUT,  2.6, scale) , true, scale,    //x, y, leftjust?, fontScale
						null,
						false
					)
				);

		text = "Fonts Used";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuSecondaryForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(ABOUT,  3.6, scale) , true, scale,    //x, y, leftjust?, fontScale
						null,
						false
					)
				);
		
		text = "Cube 02, Telegrafico, Eyecicles,";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(ABOUT,  4.35, scale) , true, scale*0.85f,    //x, y, leftjust?, fontScale
						null,
						false
					)
				);
		
		text = "ForTheLoveOfHate, BlueHighway";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(ABOUT,  5, scale) , true, scale*0.85f,    //x, y, leftjust?, fontScale
						null,
						false
					)
				);

		String app_ver_code = "?";
		String app_ver_name = "?";
		try
		{
			app_ver_code = String.valueOf( mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode );    
		}
		catch (Exception e)
		{
			app_ver_code = "?" ;
		}
		
		try
		{
			app_ver_name = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
		}
		catch (Exception e)
		{
			app_ver_name = "?" ;
		}
		
		text = "Version: code " + app_ver_code + " name " + app_ver_name;
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
						text, (int) (game.getWorld().mScreenWidth*0.1), getVeriticalPosition(ABOUT, 6, scale) , true, scale*0.7f,    //x, y, leftjust?, fontScale
						null,
						false
					)		
				);
		
		
		text = "Based on 'CrackAttack!' by Daniel Nelson";
		menu.addItem( (GLMenuItem)
				new GLButton<Void>(
						mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
						text, getCenteredPosition(text, FontManager.MENUMINORITEM_FONT, scale*0.85f), getVeriticalPosition(ABOUT,  7, scale) , true, scale*0.85f,    //x, y, leftjust?, fontScale
						null,
						false
					)		
				);
		
		if (game.getIsPaid())
		{
			text = "Thanks for purchasing!";
			menu.addItem( (GLMenuItem)
					new GLButton<Void>(
							mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
							text, getCenteredPosition(text, FontManager.MENUMINORITEM_FONT, scale*0.85f), getVeriticalPosition(ABOUT,  8, scale) , true, scale*0.85f,    //x, y, leftjust?, fontScale
							null,
							false
						)		
					);
		}
		
		addMenu(ABOUT, menu);
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// END OF GAME MENU
		
		menu = new Menu(mContext, NONE);
		// set height first
		mMenuHeight.put(ENDOFGAMEMENU, DropSectionState.PEEK_UNFOLDEDSCORE);
		mMenuTopBorder.put(ENDOFGAMEMENU, game.getWorld().mScreenHeight*0.03f);

		
		text = "Play Again";
		menu.addItem( (GLMenuItem)
				
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						// right
						//text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(ENDOFGAMEMENU, 0) , true, 1f,    //x, y, leftjust?, fontScale
						// center
						text, getCenteredPosition(text), getVeriticalPosition(ENDOFGAMEMENU, 0) , true, 1f,    //x, y, leftjust?, fontScale
						new Callable<Void>() {
							public Void call() {
								game.initGame( game.getWorld().mBoards.mGameState.mGameDiff, game.getWorld().mBoards.mGameState.mGameMode );
								game.startGame(DropSection.DEFAULT_DURATION, DropSection.DEFAULT_MOTION, false);
								return null;
							}
						}
					)
				
				);
		
		text = "MainMenu";
		menu.addItem( (GLMenuItem)
				
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						// right
						//text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(ENDOFGAMEMENU, 1) , true, 1f,    //x, y, leftjust?, fontScale
						// center
						text, getCenteredPosition(text), getVeriticalPosition(ENDOFGAMEMENU, 1) , true, 1f,    //x, y, leftjust?, fontScale
						new Callable<Void>() {
							public Void call() {
								game.initGame( game.getWorld().mBoards.mGameState.mGameDiff, game.getWorld().mBoards.mGameState.mGameMode );
								game.setIsGameOver(false);
								game.setIsGameStarted(false);
								game.getWorld().mMenus.transitionToMenu(MAINMENU, true,  DropSection.SLOW_DURATION,  DropSection.SLOW_EQ);
								return null;
							}
						}
					)
				
				);

		
		addMenu(ENDOFGAMEMENU, menu);
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// MAINMENU
		
		menu = new Menu(mContext, NONE);
		// set height first
		mMenuHeight.put(MAINMENU, DropSectionState.FULL);
		mMenuTopBorder.put(MAINMENU, game.getWorld().mScreenHeight*0.3f);
		
		text = "Single Player";		
		gen_menuButton = new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(MAINMENU, 0) , true, 1f,    //x, y, leftjust?, fontScale
						
							new Callable<Void>() {
							public Void call() {
								// must reset diff & mode upon going to single player menu
								game.getWorld().mBoards.mGameState.mGameMode = GameState.DEFAULT_MODE;
								game.getWorld().mBoards.mGameState.mGameDiff = GameState.DEFAULT_DIFFICULTY;
								game.getWorld().mMenus.transitionToMenu(SINGLEPLAYERMENU, true, null, null);
								return null;
							}
						}
					);
		
		gen_menuButton.setHint(mFlashTime, true, false, Color.LOGO_BLUE); // hint Y color *every* X seconds
		menu.addItem( gen_menuButton );
		

		text = "Tutorial";
		gen_menuButton = new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(MAINMENU, 1) , true, 1f,    //x, y, leftjust?, fontScale
						
							new Callable<Void>() {
							public Void call() {
								// single player menu takes care of this
								//game.initGame();
								game.startGame(null, null, true);
								game.getWorld().mTutorialState.loadFirstTutorialBoard();
								return null;
							}
						}
					);
		
		gen_menuButton.setHint(mFlashTime, true, false, Color.LOGO_GREEN); // hint Y color *every* X seconds
		menu.addItem( gen_menuButton );
		
		// SOUND 
		text = soundText[0] ;
		gen_menuButton = new GLButton<Boolean>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						soundText, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(MAINMENU, 2) , true, 1f,    //x, y, leftjust?, fontScale
						
						new Callable<Boolean>() {
							public Boolean call() {
								game.getWorld().toggleSoundEnable();
								return game.getWorld().isSoundEnabled();
								}
							}
					);

		gen_menuButton.setHint(mFlashTime, true, false, Color.LOGO_RED); // hint Y color *every* X seconds
		menu.addItem( gen_menuButton );

		text = "Options";
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(MAINMENU, 3) , true, 1f,    //x, y, leftjust?, fontScale
				
					new Callable<Void>() {
					public Void call() {
						
						if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
				          game.startActivity(new Intent(game.getBaseContext(), AppPreferences.class));
				        }
				        else {
				          game.startActivity(new Intent(game.getBaseContext(), AppPreferencesFragment.class));
				        }
						
						return null;
					}
				}
			);

		gen_menuButton.setHint(mFlashTime, true, false, Color.RED_LIGHT); // hint Y color *every* X seconds
		menu.addItem( gen_menuButton );

		text = "About";
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(MAINMENU, 4) , true, 1f,    //x, y, leftjust?, fontScale
				
					new Callable<Void>() {
					public Void call() {
						
						//try
						//{
							game.getWorld().mMenus.transitionToMenu(ABOUT, true,  DropSection.SLOW_DURATION,  DropSection.SLOW_EQ);
						//}
						//catch (Exception e)
						//{
							
							/*
							String content = "";
							//Log.e("GLBtn Action", e.getMessage());
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							PrintStream ps = new PrintStream(baos);
							e.printStackTrace(ps);
							try {
							content	= baos.toString("ISO-8859-1");
							} catch (UnsupportedEncodingException e1) {
								content += e1.toString() + "\n===========================\n";
							}  
							game.debug( e.toString() + "\n" + content , true);
							
							*/
							
							
							//Log.e("GLButton", e.toString());
						//}
							
						return null;
					}
				}
			);
		
		gen_menuButton.setHint(mFlashTime, true, false, Color.YELLOW); // hint Y color *every* X seconds
		menu.addItem( gen_menuButton );

		text = "Exit";
		gen_menuButton = new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*0.9)), getVeriticalPosition(MAINMENU, 5) , true, 1f,    //x, y, leftjust?, fontScale
						
							new Callable<Void>() {
							public Void call() {
								game.finish();
								return null;
							}
						}
					);

		gen_menuButton.setHint(mFlashTime, true, false, Color.GREY); // hint Y color *every* X seconds
		menu.addItem( gen_menuButton );
		
		addMenu(MAINMENU, menu);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// SINGLEPLAYERMENU MENU
		
		menu = new Menu(mContext, MAINMENU);
		// set height first
		mMenuHeight.put(SINGLEPLAYERMENU, DropSectionState.FULL);
		mMenuTopBorder.put(SINGLEPLAYERMENU, game.getWorld().mScreenHeight*0.3f);
		
		text = "Game Mode";
		menu.addItem( (GLMenuItem)
				
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuSecondaryForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(SINGLEPLAYERMENU, 0) , true, 1f,    //x, y, leftjust?, fontScale
						
						null,
						false
					)
				
				);
		
		// centered
		menu.addItem( (GLMenuItem)
			new GLRollerBox<Void>(
					mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
					getCenteredPosition((int)(game.getWorld().mScreenWidth*0.66)), getVeriticalPosition(MAINMENU, 1) , true, 1f,    //x, y, leftjust?, fontScale
					(int)(game.getWorld().mScreenWidth*0.66),
					GameState.DEFAULT_MODE,
					new Callable<Void>() {
						public Void call() {
							game.getWorld().mBoards.mGameState.prevMode();
							return null;
						}
					}
					,
					new Callable<Void>() {
						public Void call() {
							game.getWorld().mBoards.mGameState.nextMode();
							return null;
						}
					}
				)
			
		);
		
		text = "Game Difficulty";
		menu.addItem( (GLMenuItem)
				
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuSecondaryForegroundColor,
						text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*0.1)), getVeriticalPosition(SINGLEPLAYERMENU, 3) , true, 1f,    //x, y, leftjust?, fontScale
						
						null,
						false
					)
				
				);
		
		// centered
		menu.addItem( (GLMenuItem)
			new GLRollerBox<Void>(
					mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
					getCenteredPosition((int)(game.getWorld().mScreenWidth*0.66)), getVeriticalPosition(MAINMENU, 4) , true, 1f,    //x, y, leftjust?, fontScale
					(int)(game.getWorld().mScreenWidth*0.66),
					GameState.DEFAULT_DIFFICULTY,
					new Callable<Void>() {
						public Void call() {
							game.getWorld().mBoards.mGameState.prevDifficulty();
							return null;
						}
					}
					,
					new Callable<Void>() {
						public Void call() {
							game.getWorld().mBoards.mGameState.nextDifficulty();
							return null;
						}
					}
				)
			
		);
		
		text = "Start Game";
		scale = 1.5f;
		gen_menuButton = new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						text, getCenteredPosition(text, scale), getVeriticalPosition(MAINMENU, 6) , true, scale,    //x, y, leftjust?, fontScale
						
						new Callable<Void>() {
							public Void call() {
								game.initGame( game.getWorld().mBoards.mGameState.mGameDiff, game.getWorld().mBoards.mGameState.mGameMode );
								game.startGame();
								//game.getWorld().resumeWorld(true);
								return null;
							}
						}
					);
		
		gen_menuButton.setHint(3000, true, false, Color.YELLOW); // hint Y color *every* X seconds
		
		menu.addItem(gen_menuButton);
		
		
		addMenu(SINGLEPLAYERMENU, menu);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// INGAME MENU
		
		menu = new Menu(mContext, INGAMEMENU);
		// set height first
		mMenuHeight.put(INGAMEMENU, DropSectionState.UNFOLDED);
		mMenuTopBorder.put(INGAMEMENU, game.getWorld().mScreenHeight*0.03f);
		
		text = "Resume Game";
		menu.addItem( (GLMenuItem)
				
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						text, getCenteredPosition(text), getVeriticalPosition(INGAMEMENU, 0) , true, 1f,    //x, y, leftjust?, fontScale
						
							new Callable<Void>() {
							public Void call() {
								game.getWorld().resumeWorld(true);
								return null;
							}
						}
					)
				
				);
		
		text = soundText[0] ;
		menu.addItem( (GLMenuItem)
				
				new GLButton<Boolean>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						// Callable True Entry, Callable False Entry
						soundText, getCenteredPosition(text), getVeriticalPosition(INGAMEMENU, 1) , true, 1f,    //x, y, leftjust?, fontScale
						
						new Callable<Boolean>() {
						public Boolean call() {
							game.getWorld().toggleSoundEnable();
							return game.getWorld().isSoundEnabled();
							}
						}
					)
				
				);
		
		text = "Quit Game";
		menu.addItem( (GLMenuItem)
				
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						text, getCenteredPosition(text), getVeriticalPosition(INGAMEMENU, 2) , true, 1f,    //x, y, leftjust?, fontScale
						
						// action
						new Callable<Void>() {
							public Void call() {
								game.endGame(0);
								return null;
							}
						}
						
						
						,
						
						
						// post action
						new Callable<Void>() {
							public Void call() {
								// end game is already dong this
								//game.getWorld().mMenus.transitionToMenu(ENDOFGAMEMENU, true, DropSection.ENDGAME_DURATION, DropSection.SLOW_EQ);
								return null;
							}
						}
						
						,
						
						100	// in ms to delay before committing do post action
						
					)
				
				);
		
		text = "Exit";
		menu.addItem( (GLMenuItem)
				
				new GLButton<Void>(
						mContext, FontManager.MENUMAJORITEM_FONT, World.mMenuForegroundColor,
						text, getCenteredPosition(text), getVeriticalPosition(INGAMEMENU, 3) , true, 1f,    //x, y, leftjust?, fontScale
						
							new Callable<Void>() {
							public Void call() {
								game.finish();
								return null;
							}
						}
					)
				
				);
		

		
		addMenu(INGAMEMENU, menu);
		
		
	}
	
	
	
	
}
