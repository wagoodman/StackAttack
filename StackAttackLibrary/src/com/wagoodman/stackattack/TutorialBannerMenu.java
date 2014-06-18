package com.wagoodman.stackattack;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Callable;
import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.FontManager;
import com.wagoodman.stackattack.GameDifficulty;
import com.wagoodman.stackattack.GameMode;
import com.wagoodman.stackattack.MainActivity;
import com.wagoodman.stackattack.TextTransform;
import com.wagoodman.stackattack.World;

import android.content.Context;

public class TutorialBannerMenu
{

	private final Context mContext;
	private final MainActivity game;
	
	public Boolean mEnabled = false;
	
	public static final String NONE			= "none";
	public static final String RULES		= "rules";
	public static final String GAMEPLAY     = "gameplay";
	public static final String MATCHING		= "matching";
	public static final String GARBAGE		= "garbage";
	public static final String BOARD		= "board";
	public static final String ADVANCED		= "advanced";
	
	private final int mHintDelay = 9000;
	private final Color mHintColor = Color.YELLOW;
	
	public int mTopBoarder = 10;
	public int mHorizontalBoarder = 10;
	
	private float mLeftPerc = 0.07f;
	private float mRightPerc = 1f - mLeftPerc;
	private float mFontScale = 0.85f;
	
	private ConcurrentHashMap<String, Menu> mMenus = new ConcurrentHashMap<String, Menu>();
	// holy hell this is cool: http://www.onkarjoshi.com/blog/201/concurrenthashset-in-java-from-concurrenthashmap/
	private String mMostCurrentMenu = NONE;
	private Set<String> mCurrentMenus = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
	private ConcurrentHashMap<String, Long> mNonCurrentMenuTrigger = new ConcurrentHashMap<String, Long>();

	private Boolean isLocked = false;
	
	public TutorialBannerMenu(Context context)
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
	private int getVeriticalPosition(String font, float scale)
	{
		fontHeight = game.getWorld().mDropSection.mFonts.getFontHeight(font) * scale;
		
		float bottom = -game.getWorld().mDropSection.mYPixFoldedHeight;
		float middle = bottom + (-bottom/2f);
		
		// TEMP TEMP TEMP
		return (int) (middle - fontHeight/2f); //-70
		
	}
	
	private int getCenteredPosition(int width)
	{
		return (int) (game.getWorld().mScreenWidth/2 - width/2) ;
	}
	
	private int getCenteredPosition(String phrase)
	{
		return (int) (game.getWorld().mScreenWidth/2 - (game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, phrase)*mFontScale)/2) ;
	}

	
	private int getLeftPosition(String phrase, int pos)
	{
		return pos;
	}
	
	private int getRightPosition(String phrase, int pos)
	{
		return (int) (pos - game.getWorld().mDropSection.mFonts.getStringWidth(FontManager.MENUMINORITEM_FONT, phrase)*mFontScale) ;
	}
	
	
	public void addMenu(String menuTitle, Menu menu)
	{
		mMenus.put(menuTitle, menu);
	}
	
	
	
	public Menu getMenu(String menuTitle)
	{
		return mMenus.get(menuTitle);
	}

	public void transitionToMenu(String menuTitle, Boolean doOutro )
	{
		transitionToMenu(menuTitle, doOutro, null);
	}
	
	public void transitionToMenu(String menuTitle, Boolean doOutro, Integer text_duration)
	{
		if (isLocked)
			return;
		
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
		//game.text += "TUTORIAL HIT!\n";
		//game.textviewHandler.post(game.updateTextView);
		
		if (mEnabled)
		{
			synchronized(mCurrentMenus)
			{			
				for (String menu : mCurrentMenus)
					mMenus.get(menu).interact(x, y, pixFontOffset);
			}
		}
	}
	
	public Boolean gotoBackMenu()
	{
		synchronized(mCurrentMenus)
		{
			if (mCurrentMenus.contains(mMostCurrentMenu) && mEnabled)
			{
				// only apply to the most current menu
				return mMenus.get(mMostCurrentMenu).gotoBackMenu(null, null);
			}
		}
		
		return false;
	}
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		if (!mEnabled)
			return;
		
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
		if (!mEnabled)
			return;
		
		
		synchronized(mCurrentMenus)
		{
			for (String menu : mCurrentMenus)
				mMenus.get(menu).draw(gl, pixYOffset);
		}
		

	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MENUS!
	
	private GLButton<Void> gen_menuButton;
	
	public void generateMenus()
	{

		Menu menu;
		String text;

		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// 1) Rules...
		
		menu = new Menu(mContext, NONE);
		text = "<~ MainMenu";
		String font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*mLeftPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						game.initGame( game.getWorld().mBoards.mGameState.mGameDiff, game.getWorld().mBoards.mGameState.mGameMode );
						game.setIsGameOver(false);
						game.setIsGameStarted(false);
						game.getWorld().mMenus.transitionToMenu(MenuManager.MAINMENU, true,  DropSection.DEFAULT_DURATION,  DropSection.SLOW_EQ);
						
						// Disable Tutorial on the way out!
						//game.getWorld().mTutorialMenu.mEnabled = false;	// let a new game do this (let outro be drawn)
						game.getWorld().mTutorialState.goTo(NONE, true);
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		text = "Basics ~>";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*mRightPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(GAMEPLAY, true);
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		gen_menuButton.setHint(mHintDelay, false, true, mHintColor); // hint Y color *after* X seconds, do forever
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		
		addMenu(RULES, menu);
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// 2) Game Play...
		
		menu = new Menu(mContext, NONE);

		text = "<~ Goal";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*mLeftPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(RULES, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		
		
		text = "Matching Blocks ~>";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*mRightPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(MATCHING, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		gen_menuButton.setHint(mHintDelay, false, true, mHintColor); // hint Y color *after* X seconds, do forever
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		
		addMenu(GAMEPLAY, menu);
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// 3) Matching...
		
		menu = new Menu(mContext, NONE);

		text = "<~ Basics";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*mLeftPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(GAMEPLAY, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		
		text = "Garbage Blocks ~>";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*mRightPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(GARBAGE, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;			
		gen_menuButton.setHint(mHintDelay, false, true, mHintColor); // hint Y color *after* X seconds, do forever
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		
		addMenu(MATCHING, menu);		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// 4) Garbage Blocks...
		
		menu = new Menu(mContext, NONE);

		text = "<~ Matching";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*mLeftPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(MATCHING, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;			
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		text = "Board Movement ~>";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*mRightPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(BOARD, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		gen_menuButton.setHint(mHintDelay, false, true, mHintColor); // hint Y color *after* X seconds, do forever
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		
		addMenu(GARBAGE, menu);		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// 5) Board Movement
		
		menu = new Menu(mContext, NONE);

		text = "<~ Garbage";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*mLeftPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(GARBAGE, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;			
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		text = "Advanced Boards ~>";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*mRightPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(ADVANCED, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		gen_menuButton.setHint(mHintDelay, false, true, mHintColor); // hint Y color *after* X seconds, do forever
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		addMenu(BOARD, menu);		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// 6) Advanced Boards...
		
		menu = new Menu(mContext, NONE);

		text = "<~ Board Movement";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getLeftPosition(text, (int) (game.getWorld().mScreenWidth*mLeftPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						game.getWorld().mTutorialState.goTo(BOARD, true);
						
						//TBD
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;			
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		text = "PLAY!";
		font = FontManager.MENUMINORITEM_FONT;
		gen_menuButton = new GLButton<Void>(
				mContext, FontManager.MENUMINORITEM_FONT, World.mMenuForegroundColor,
				text, getRightPosition(text, (int) (game.getWorld().mScreenWidth*mRightPerc)), getVeriticalPosition(font, mFontScale) , true, mFontScale,    //x, y, leftjust?, fontScale
				new Callable<Void>() {
					public Void call() {
						
						// Disable Tutorial on the way out!
						game.getWorld().mTutorialMenu.mEnabled = false;
						game.getWorld().mTutorialState.goTo(NONE, true);
						game.setIsGameStarted(false);
						
						// TBD
						game.initGame(GameDifficulty.EASY, GameMode.CLASSIC);
						game.startGame(null, null, false);
						
						return null;
					}
				}
			);
		gen_menuButton.mIntroTransform =  TextTransform.MoveInFromRight;
		gen_menuButton.mOutroTransform =  TextTransform.MoveOutLeft;		
		gen_menuButton.setHint(mHintDelay, false, true, mHintColor); // hint Y color *after* X seconds, do forever
		
		menu.addItem( (GLMenuItem) gen_menuButton );
		
		addMenu(ADVANCED, menu);	
		
		
	}
	
	
	
	
}
