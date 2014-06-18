package com.wagoodman.stackattack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.BlockValue;
import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.Coord;
import com.wagoodman.stackattack.DropSection;
import com.wagoodman.stackattack.DropSectionState;
import com.wagoodman.stackattack.FinalStarImageSet;
import com.wagoodman.stackattack.GLCube;
import com.wagoodman.stackattack.GameDifficulty;
import com.wagoodman.stackattack.GameMode;
import com.wagoodman.stackattack.MenuManager;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.StarImageSet;
import com.wagoodman.stackattack.TutorialBannerMenu;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import android.widget.Toast;

class World extends GLSurfaceView implements SurfaceHolder.Callback
{
	private static final String TAG = "World";
	private static final Boolean debug = false;
	
	private final Context mContext;
	private final MainActivity game;
	
	public static enum BLOCKTYPES {REGULAR, GROUPWRAPPER};
	
	// 9x7: 8300
	// 8x6: 9000
	// 3x3: 15000
	// 1x1: 30000
	public float mGLDist = 1;
	
	public static final float GLEX11HUD_Z_ORDER = -1f;
	
	//public static final TDModel mBlock = new OBJParser(0.4f).parseOBJ("/sdcard/obj/alex/buldge64Sharp.obj");	
	//public static final TDModel mBlock = new OBJParser(0.06f).parseOBJ("/sdcard/obj/alex/buldge64Smooth.obj");	
	//public static final TDModel mBlock = new OBJParser(0.15f).parseOBJ("/sdcard/obj/jiun/design1_1_nomtl.obj");	
	//public static final TDModel mBlock = new OBJParser(0.15f).parseOBJ("/sdcard/obj/jiun/design1_1.obj");	
	
	
	public GLCube mBackgroundBlock;
	
	public GLCube mBlock;	
	//public GLCubeBuldge mBlock;
	
	//public static final TDModel mWrapperBlock = new OBJParser(0.06f).parseOBJ("/sdcard/obj/polyCubeQuads.obj");	
	//public static final TDModel mWrapperBlock = new OBJParser(0.06f).parseOBJ("/sdcard/obj/polyCubeTris.obj");	
	//public static final TDModel mWrapperBlock = new OBJParser(0.06f).parseOBJ("/sdcard/obj/cube.obj");	
	//public static final TDModel mWrapperBlock = new OBJParser(0.06f).parseOBJ("/sdcard/obj/cube2.obj");	
	public GLCube mWrapperBlock;
	
	public StarImageSet mStarSet;
	public FinalStarImageSet mFinalStarSet;
	
	public SecondaryUpdater mBoardUpdater;
	public PrimaryUpdater mBlockUpdater;
	public final GLRenderer mRenderer;
	private final SurfaceHolder surfaceHolder;

	// Sounds
	private Boolean mSoundsEnabled = true;
	private SoundPool mSoundPool;
	public HashMap<Integer, Integer> mSoundMap = new HashMap<Integer, Integer>();
	public static int SOUND_PICKUP = 1;
	public static int SOUND_DROP = 2;
	public static int SOUND_MENUCLICK = 3;
	

	// game elements (boards)
	public BoardManager mBoards;
	
	public TutorialState mTutorialState;
	
	// Score Board
	public DropSection mDropSection;
	public MenuManager mMenus;
	public TutorialBannerMenu mTutorialMenu;
	
	//Extra Elements (floating stuff)
	public WidgetLayer mWidgetLayer;
	
	// Screen Information
	public float mScreenWidth = 0;		//pixels
	public float mScreenHeight = 0;		//pixels
	public float mScreenRatio = 0;
	public final float mFovy = 45;
	
	// Pixel Lengths
	public float mScreenBlockLength = 0;
	
	// GL Lengths
	public float mGLBlockLength = 0; 	// @ board depth
	
	// GL World Depth Information
	public static final float mMinDepth = 0.2f; //1f
	public static final float m2ndMinDepth = mMinDepth+0.00001f; //1f
	public static final float mMaxDepth = 6f;	//5f
	public static final float mDepthUnit = (mMaxDepth - mMinDepth)/100f;
	

	// Colors
	public static final Color	mMenuBackdropColor = Color.BLACKGREY;
	public static final Color	mMenuForegroundColor = Color.WHITE;
	public static final Color	mMenuSecondaryForegroundColor = Color.TAN;
	
	// Define the lighting
	
	float[] mLightAmbient 	= new float[] { 0.6f, 0.6f, 0.6f, 1 };
	float[] mLightDiffuse 	= new float[] { 0.6f, 0.6f, 0.6f, 1 };
	float[] mLightPos 		= new float[] { 1, 1, -1, 0.8f }; // { 1, 1, -1, 0.8f };	
	float[] mLightSpecular	= new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
	float[] mGlobalAmbient 	= new float[] {0.2f, 0.2f, 0.2f, 1f};

	
	public FloatBuffer mLightAmbientBuffer;
	public FloatBuffer mLightDiffuseBuffer;
	public FloatBuffer mLightPositionBuffer;
	
	
	// For posting small messages to screen
	final Handler toastHandler = new Handler();

	void postToast(String msg)
	{
		Toast t = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
		t.show();
	}

	World(Context context)
	{
		super(context);
		// DEBUG("Creating GLView...");

		// Uncomment this to turn on error-checking and logging for open gl
		// setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);

		// get the game object from context
		game = (MainActivity) (context);
		mContext = context;

		//mBlock =  new OBJParser(context).parseOBJ("/sdcard/obj/polyCubeTris.obj");
		
		// add callback for surface events (renderer gets these currently)
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		
		
		// Create Boards Data Structure
		mBoards = new BoardManager(context);
		
		// Create Scoreboard (Dont do this here, need gl for fonts)
		mDropSection = new DropSection(context);
		
		// Create Menus
		mMenus = new MenuManager(mContext);
		mTutorialMenu = new TutorialBannerMenu(mContext);
		
		mTutorialState = new TutorialState(mContext);
		
		// create widget layer
		mWidgetLayer = new WidgetLayer(mContext);
		
		// Create new block/board updater thread
		mBoardUpdater = new SecondaryUpdater(context);
		mBlockUpdater = new PrimaryUpdater(context);
		
		// create and set the renderer thread
		mRenderer = new GLRenderer(context, mFovy);
		setRenderer(mRenderer);
		
		// Load lighting
		
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(mLightAmbient.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mLightAmbientBuffer = byteBuf.asFloatBuffer();
		mLightAmbientBuffer.put(mLightAmbient);
		mLightAmbientBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(mLightDiffuse.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mLightDiffuseBuffer = byteBuf.asFloatBuffer();
		mLightDiffuseBuffer.put(mLightDiffuse);
		mLightDiffuseBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(mLightPos.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		mLightPositionBuffer = byteBuf.asFloatBuffer();
		mLightPositionBuffer.put(mLightPos);
		mLightPositionBuffer.position(0);
		
		
		// Load Sound
		initSounds();

		
	}
	
	public void initSounds() 
	{
		// Commit Sound Preference
		mSoundsEnabled = game.getPreferences().getBoolean("soundEffects", true);
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC,0);
		mSoundMap = new HashMap<Integer, Integer>();
		addSound(SOUND_PICKUP, R.raw.pickup);
		addSound(SOUND_DROP, R.raw.drop);
		addSound(SOUND_MENUCLICK, R.raw.menuclick);
	}
	
	public void addSound(int index, int SoundID)
	{
		mSoundMap.put(index, mSoundPool.load(mContext, SoundID, 1));
	}
	
	public Boolean isSoundEnabled()
	{
		return mSoundsEnabled;
	}
	
	/*
	// This could take a while.... do it in it's own thread
	private class CommitSoundPreference extends AsyncTask< Void, Void, Void> 
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			game.setPreferenceEditor( game.getPreferences().edit() );
			game.getPreferenceEditor().putBoolean("soundEffects", mSoundsEnabled);
			game.getPreferenceEditor().commit();
			return null;
		}
	}
	*/
	public void toggleSoundEnable()
	{
		if (mSoundsEnabled)
			mSoundsEnabled = false;
		else
			mSoundsEnabled = true;
		
		try{
			//new CommitSoundPreference().execute();
			game.setPreferenceEditor( game.getPreferences().edit() );
			game.getPreferenceEditor().putBoolean("soundEffects", mSoundsEnabled);
			game.getPreferenceEditor().commit();
		}
		catch (Exception e){}
	}
	
	public void playSound(int index)
	{
		if (mSoundsEnabled)
		{
			float streamVolume = game.getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
			streamVolume = streamVolume / game.getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		    mSoundPool.play(mSoundMap.get(index), streamVolume, streamVolume, 1, 0, 1f);
		}
	}
	 
	public void playLoopedSound(int index)
	{
		if (mSoundsEnabled)
		{
		    float streamVolume = game.getAudioManager().getStreamVolume(AudioManager.STREAM_MUSIC);
		    streamVolume = streamVolume / game.getAudioManager().getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		    mSoundPool.play(mSoundMap.get(index), streamVolume, streamVolume, 1, -1, 1f);
		}
	}

	private boolean mHasLoadedEntities = false;
	public void updateWorldDistances(GL10 gl, int screenWidth, int screenHeight, float screenRatio)
	{
		
		// Pixel Coordinates
		mScreenWidth = screenWidth;
		mScreenHeight = screenHeight;
		mScreenRatio = (float) mScreenWidth / (float) mScreenHeight;
		
		// Pixel Lengths; row+1 because the top row should *always* be on the board
		mScreenBlockLength = (int) Math.min(
				mScreenWidth  / (double) MainActivity.COLCOUNT, 
				mScreenHeight / (double) MainActivity.ROWCOUNT+1
				);
		
		// GL Lengths; row+1 because the top row should *always* be on the board
		mGLBlockLength = (float) Math.min(
				getPlaneDimensions(-mBoards.mBoardDepth)[X] / (double) (MainActivity.COLCOUNT),
				getPlaneDimensions(-mBoards.mBoardDepth)[Y] / (double) (MainActivity.ROWCOUNT+1) 
				);
				
		mBoards.updateIterationSpeed(); // correct iteration speed
		
		// slim factor * exact proportion
		mGLDist =   1.2f   * (float) ((getPlaneDimensions(-mBoards.mBoardDepth)[X] / mGLBlockLength) * 2) ;
		
		
		if (!mHasLoadedEntities)
		{
			
			mBackgroundBlock = new GLCube( 
					mGLDist,
					
					// TEMP TEMP TEMP
					//CA_Game.COLCOUNT,CA_Game.ROWCOUNT,1
					
					(int) (MainActivity.COLCOUNT*(1.0/(VirtualShape.mBackdropDepthRatio-VirtualShape.mBoardDepthRatio))), 
					(int) (MainActivity.ROWCOUNT*(1.0/(VirtualShape.mBackdropDepthRatio-VirtualShape.mBoardDepthRatio))), 
					4
					);
			
			mBlock = new GLCube(mGLDist, 1, 1, 1);
			// make texture pointers
			mBlock.initGLTextures(gl, BlockValue.values().length-1);
			//mBlock.initGLTextures(gl, 2);
			
			//for texture bitmaps:
			//	mBlock.loadGLTexture(gl, bitmap);
			//mBlock.loadGLTexture(gl, ((BitmapDrawable)game.getResources().getDrawable(R.drawable.lock)).getBitmap() );
			
			
			for (int idx=0; idx < BlockValue.values().length; idx++)
				mBlock.loadGLTexture(
						gl, 
						((BitmapDrawable)mContext.getResources().getDrawable( BlockValue.values()[idx].mDrawableId )).getBitmap(), 
						BlockValue.values()[idx].ordinal() 
						);
			
			
			
			mWrapperBlock = new GLCube(mGLDist, 3.8f, 1.1f, 1);		
	
			// update menu distances
			mDropSection.setDistances(gl);
	
			// make the menus first!
			mMenus.generateMenus();
			mTutorialMenu.generateMenus();
			// set any gl deps for menus (fonts)
			mMenus.setDependencies();
			mTutorialMenu.setDependencies();
			
			// update virtual shape distances
			mBoards.setDimensions();
			
			// setup images for stars
			mStarSet = new StarImageSet(mContext);
			mStarSet.setDistances(gl);
			
			mFinalStarSet = new FinalStarImageSet(mContext);
			mFinalStarSet.setDistances(gl);
			
			// tutorial elements
			mTutorialState.setupElements();
			
			mHasLoadedEntities = true;
		}
		
		// TEMP TEMP TEMP
		// pause world on the start of a game (or re-entering the app)
		//game.initGame();
		//game.startGame();
		
		pauseWorld(true, 0, null);
		
	}
	
	
	public void resetWorld()
	{
		resetWorld(null, null);
	}
	
	public void resetWorld(GameDifficulty gd, GameMode gm)
	{
		mTutorialState.reset();
		
		cancelGameTriggers();
		
		//stopUpdaterThreads();
		
		if (gd != null && gm != null)
			mBoards = new BoardManager(mContext, gd, gm);
		else
			mBoards = new BoardManager(mContext);
		
		mBoards.updateIterationSpeed(); // correct iteration speed
		
		// update virtual shape distances
		mBoards.setDimensions();
		
		createBoards(1);
		
		//startUpdaterThreads();
	}
	
	
	/**
	 * Returns the w,h dimensions of the plane given the reference depth. 
	 * Must be used AFTER the surface has called 'surfaceChanged' to get 
	 * the screen width and height.
	 * 
	 * @param refDepth		depth at which to calculate the pane distances from
	 * @return [float x plane width, float y plane height]
	 */
	public float[] getPlaneDimensions(float refDepth) 
	{
	
		float planeHeight = (float) (2f * Math.tan((float)(Math.toRadians(mFovy / 2f))) * refDepth);
		
		// Thank you wolfram alpha! (I know, I know, more digits than needed [or possible to calculate] but THIS fixes the problem )
		//float planeHeight = (float) ( 0.82842712474619009760337744841939615713934375075389614635335947598146495692421407770077506865528314547002769246182459404984967211170147442528824299419987166282644533185501118551159990100230556412114294021911994321194054906919372402f * refDepth);
		
		return new float[] { mScreenRatio * planeHeight , planeHeight};
	
	}
	


	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		super.surfaceDestroyed(holder);
		
		stopUpdaterThreads();
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		super.surfaceCreated(holder);
		
		startUpdaterThreads();

	}
	
	public void startUpdaterThreads()
	{
		
		if (mBoardUpdater.state == SecondaryUpdater.PAUSED || mBlockUpdater.state == PrimaryUpdater.PAUSED)
		{
			// When game is opened again in the Android OS
			mBoardUpdater = new SecondaryUpdater(mContext);
			mBoardUpdater.start();

			mBlockUpdater = new PrimaryUpdater(mContext);
			mBlockUpdater.start();
			
		}
		else
		{	
			// create the main board
			createBoards(1);
			
			// start the game Thread for the first time
			mBoardUpdater.start();
			mBlockUpdater.start();
		}

		
	}
	
	public void stopUpdaterThreads()
	{
	
		boolean retry = true;
		
		// code to end gameloop
		mBoardUpdater.state = SecondaryUpdater.PAUSED;
		mBlockUpdater.state = PrimaryUpdater.PAUSED;
		
		while (retry)
		{
			try
			{
				// Kill Thread
				mBlockUpdater.join();
				retry = false;
			}
			catch (InterruptedException e)
			{

			}
		}
		
		retry = true;
		while (retry)
		{
			try
			{
				// Kill Thread
				mBoardUpdater.join();
				retry = false;
			}
			catch (InterruptedException e)
			{

			}
		}
		
	}
	
	
	public void createBoards(int numBoards)
	{
		mBoards.clearAllBoards();
		for (int i=0; i < numBoards; i++)
			mBoards.addBoard(new Board(mContext));
	}
	
	public void pauseWorld(Boolean anim)
	{
		pauseWorld(anim, null, null);
	}
	
	public void pauseWorld(Boolean anim, Integer dur, MotionEquation eq)
	{

		//game.text = "\n\n\n\n\n\n\nPaused!";
		//game.textviewHandler.post(game.updateTextView);
		
		game.setIsGamePaused( true );
		mBoards.pauseGame();
		
		for (Board board : mBoards)
			board.lockBoard();
				
		if (anim)
		{
			
			String menu;
			
			// go to a menu...
			if (game.getIsGameStarted() == false && game.getIsGameOver() == false)
			{
				menu = MenuManager.MAINMENU;
				if (eq == null)
					eq = DropSection.SLOW_EQ;
				if (dur == null)
					dur = DropSection.SLOW_DURATION;
			}
			else if (game.getIsGameStarted() == false && game.getIsGameOver() == true)
			{
				menu = MenuManager.ENDOFGAMEMENU;
				if (eq == null)
					eq = DropSection.DEFAULT_MOTION;
				if (dur == null)
					dur = DropSection.DEFAULT_DURATION;
				
				mDropSection.mScoreBanner.introEndOfGameStats();
			}
			else
			{
				menu = MenuManager.INGAMEMENU;
				if (eq == null)
					eq = DropSection.DEFAULT_MOTION;
				if (dur == null)
					dur = DropSection.DEFAULT_DURATION;
				
				mDropSection.mScoreBanner.introInGameStats(dur);
			}
			
			
			mBoards.transitionDown(eq, dur);
			
			//mMenus.transitionToMenu(menu, false, dur, eq, (int) (dur*1.5) );
			mMenus.transitionToMenu(menu, false, dur, eq );
		
		}

	}
	
	public void resumeWorld(Boolean anim)
	{	
		resumeWorld(anim, null, null);
	}
	
	public void resumeWorld(Boolean anim, Integer dur, MotionEquation eq)
	{	
		//game.text = "\n\n\n\n\n\n\nResume!";
		//game.textviewHandler.post(game.updateTextView);
		
		//if (!game.isGameStarted)
		//	return;
		
		game.setIsGamePaused( false );
		for (Board board : mBoards)
			board.unlockBoard();
		mBoards.resumeGame();
				
		if (anim)
		{
			//outro the current menu
			mMenus.transitionToMenu(MenuManager.NONE, false, dur, eq);
			
			// make any menu go away!...
			mBoards.transitionUp(DropSection.DEFAULT_MOTION, DropSection.DEFAULT_DURATION);
			mDropSection.changeStateTo(DropSectionState.FOLDED, dur, eq); 
			
			mDropSection.mScoreBanner.outroInGameStats(dur);
			
		}

		
	}
	
	
	
	private void dumpEvent(MotionEvent event)
	{
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
		{
			sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++)
		{
			// sb.append("#" ).append(i);
			// sb.append("(pid " ).append(event.getPointerId(i));
			sb.append("#").append(event.getPointerId(i));
			// sb.append(")=" ).append((int) event.getX(i));
			sb.append("=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount()) sb.append(";");
		}
		sb.append("]");

		// Log.d(TAG, sb.toString());

		/*
		game.text = sb.toString() + "\n" + game.text;
		game.textviewHandler.post(game.updateTextView);
		*/
	}

	
	private static final int SWIPE_MIN_HORIZ_DISTANCE = 110;
    private static final int SWIPE_MIN_VERTI_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 300;
	boolean[] hasFinger = {false,false};
	
	// var [PRI or SEC][X or Y]
	int[][] start	= {{-1,-1},{-1,-1}};
	int[][] delta	= {{ 0, 0},{ 0, 0}};
	int[][] end 	= {{-1,-1},{-1,-1}};
	// keys
	private static final int PRI = 0;
	private static final int SEC = 1;
	private static final int X = 0;
	private static final int Y = 1;
	
	private int eventPointerCount = 0;
	private int eventaction = 0;
	
	float deltaDegree;
	Boolean hintAccepted = true;
	
	// keep board coordinate history
	Coord<Integer> curPrimaryBoardCoords = new Coord<Integer> (0,0);
	Coord<Integer> prePrimaryBoardCoords = new Coord<Integer> (0,0);
    

    @SuppressWarnings("unchecked")
	@Override
    public boolean onTouchEvent(MotionEvent event) 
    {
    	
    	//dumpEvent(event);
    	
    	
    	// get event
		eventaction = event.getAction();
		eventPointerCount = event.getPointerCount();
		
		if (eventaction == MotionEvent.ACTION_DOWN || (eventaction & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN)
		{
			
			// Has a new primary finger
			if (!hasFinger[PRI] && !hasFinger[SEC] && eventPointerCount == 1)
			{
				hasFinger[PRI] = true;
				start[PRI][X] = (int) event.getX(PRI);
				start[PRI][Y] = (int) event.getY(PRI);
				
				// pickup position
				curPrimaryBoardCoords = convertPixToRC(start[PRI][X], start[PRI][Y]);
				prePrimaryBoardCoords = curPrimaryBoardCoords.clone();
				
				// This operation happens too often, too quickly to allow the information to not be available to the next operator (swap).
				// Allow the UI thread (with the most up to date info) process the pickup from here (dont use AsyncPickup)

				
				if (!game.getIsGamePaused())
				{
					if ( mDropSection.isXYOnScoreBoard(start[PRI][X], start[PRI][Y]) )
					{
						// pickup menu
						if (!mDropSection.isLocked)
							mDropSection.pickupSection(start[PRI][X], start[PRI][Y]);
						else
							mDropSection.interact(start[PRI][X], start[PRI][Y], MotionEvent.ACTION_DOWN);
					}
					else 
					{
						mBoards.pickupBlock( curPrimaryBoardCoords );
						
						// pickup block
						//if (mBoards.pickupBlock( curPrimaryBoardCoords ))
						
						// If in tutorial, interact with widgets... only if you picked up a single block
						if (mTutorialState.isInTutorial)
							mWidgetLayer.pickup(start[PRI][X], start[PRI][Y], 0, 1);
					}
				}
				else
				{
					if ( mDropSection.isXYOnScoreBoard(start[PRI][X], start[PRI][Y] ) )
					{
						// pickup menu
						if (!mDropSection.isLocked)
							mDropSection.pickupSection(start[PRI][X], start[PRI][Y]);
						else
							mDropSection.interact(start[PRI][X], start[PRI][Y], MotionEvent.ACTION_DOWN);
					}
					else 
					{
						mDropSection.interact(start[PRI][X], start[PRI][Y], MotionEvent.ACTION_DOWN);
					}
					
				}
				
			}
			// Simultaneous primary & secondary
			else if (!hasFinger[PRI] && !hasFinger[SEC] && eventPointerCount == 2)
			{
				hasFinger[PRI] = true;
				hasFinger[SEC] = true;
				start[PRI][X] = (int) event.getX(PRI);
				start[SEC][X] = (int) event.getX(SEC);
				start[PRI][Y] = (int) event.getY(PRI);
				start[SEC][Y] = (int) event.getY(SEC);
			}
			// Had the primary, now has a secondary finger
			else if (hasFinger[PRI] && !hasFinger[SEC]  && eventPointerCount == 2)
			{
				hasFinger[SEC] = true;
				start[SEC][X] = (int) event.getX(SEC);
				start[SEC][Y] = (int) event.getY(SEC);
				
				// drop current
				mBoards.dropActiveBlock(null);
				
				// If in tutorial, interact with widgets... got another finger, follow the primary
				if (mTutorialState.isInTutorial)
					mWidgetLayer.pickup(start[PRI][X], start[PRI][Y], 0, 2);
				
				// drop menu
				mDropSection.dropSection();

			}
			// More than two fingers
			else
			{
				mBoards.transitionCenter();	
				
				// invalidate
				hasFinger[PRI]	= false;
				start[PRI][X]	= -1;
				end[PRI][X]		= -1;
				start[PRI][Y]	= -1;
				end[PRI][Y]		= -1;

				hasFinger[SEC]	= false;
				start[SEC][X]	= -1;
				end[SEC][X]		= -1;
				start[SEC][Y]	= -1;
				end[SEC][Y]		= -1;
				
				// drop position
				mBoards.dropActiveBlock(null);
				
				// If in tutorial, interact with widgets... dont support more than 2 fingers
				if (mTutorialState.isInTutorial)
					mWidgetLayer.drop();
				
				// drop menu
				mDropSection.dropSection();
			}
			
		}

		if (eventaction == MotionEvent.ACTION_MOVE)
		{
			
			// Two finger swipe event (horizontal)
			if (hasFinger[PRI] && hasFinger[SEC] && eventPointerCount == 2)
			{
				// no need for two finger swipe unless there is a game! (or if there is a tutorial)
				if ( (game.getIsGameStarted() && !game.getIsGameOver() && !game.getIsGamePaused()) || mTutorialState.isInTutorial )
				{
					// If in tutorial, interact with widgets... follow the primary only
					if (mTutorialState.isInTutorial)
						mWidgetLayer.interact(event.getX(PRI), event.getY(PRI), 0, 2);
					
					// dont fight against a current transform
					if (mBoards.isTransformingHorizontal)
						mBoards.stopHorizontal();
					
					delta[PRI][X] = (int) (event.getX(PRI)-delta[PRI][X]);
					delta[SEC][X] = (int) (event.getX(SEC)-delta[SEC][X]);
					deltaDegree = (float) ((((delta[PRI][X]+delta[SEC][X])/2.0)/(float)mScreenWidth)*mBoards.mDelimitingDegree);
					hintAccepted = mBoards.hintHorizontalDirection(deltaDegree);
				}
			}
			// Single, primary finger drag
			if (hasFinger[PRI] && !hasFinger[SEC] && eventPointerCount == 1)
			{
				// keep previous for reference
				prePrimaryBoardCoords = curPrimaryBoardCoords.clone();
				
				// get new coords
				curPrimaryBoardCoords = convertPixToRC(event.getX(PRI), event.getY(PRI));
				
				// If in tutorial, interact with widgets... 
				if (mTutorialState.isInTutorial)
					mWidgetLayer.interact(event.getX(PRI), event.getY(PRI), 0, 1);
				
				// swap blocks on drop row path only
				Board board = mBoards.getCurrentBoard();
								
				if (board != null)
				{
					// drag active block...
					if (board.mBlocks.mActiveBlock != null)
					{

						// swap, if necessary
						if ( !curPrimaryBoardCoords.equals(prePrimaryBoardCoords) /* && dragWorked */ )
						{
							// TODO:null pointer exception???
							Coord<Integer> block1 = new Coord<Integer> ( board.mActiveDropRow.get(prePrimaryBoardCoords.getCol()) , prePrimaryBoardCoords.getCol() );
							Coord<Integer> block2 = new Coord<Integer> ( board.mActiveDropRow.get(curPrimaryBoardCoords.getCol()) , curPrimaryBoardCoords.getCol() );
							
							// one of the blocks must be the active block
							if (board.mGrid.getBlockId(block1) == board.mBlocks.mActiveBlock.getId() || board.mGrid.getBlockId(block2) == board.mBlocks.mActiveBlock.getId() )
								mBoards.swapBlocks(
									block1, 
									block2
									);
							
						}
					}
					// no active block, was the menu dragging?
					else if (!game.getIsGamePaused())
					{
						mDropSection.dragSection( event.getY(PRI) );
					}
					else
					{
						// if on score board or a drag is already happening...
						if ( mDropSection.isXYOnScoreBoard(event.getX(PRI) , event.getY(PRI) ) || mDropSection.mActiveSection )
						{
							// drag menu
							mDropSection.dragSection( event.getY(PRI)  );
						}
						else 
						{
							// interact with the menu items
							mDropSection.interact((int)event.getX(PRI),(int) event.getY(PRI), MotionEvent.ACTION_MOVE);
						}
						
					}

				}

				
			}
		}

		if (eventaction == MotionEvent.ACTION_UP || (eventaction & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP)
		{
			
			
			// Lifted primary
			if (hasFinger[PRI] && !hasFinger[SEC]  && eventPointerCount == 1)
			{
				hasFinger[PRI] = false;
				end[PRI][X] = (int) event.getX(PRI);
				end[PRI][Y] = (int) event.getY(PRI);
				
				if (!game.getIsGamePaused())
				{
					// drop position
					mBoards.dropActiveBlock(convertPixToRC(end[PRI][X], end[PRI][Y]));
					
					// If in tutorial, interact with widgets... 
					if (mTutorialState.isInTutorial)
						mWidgetLayer.drop();
					
					// drop menu
					mDropSection.dropSection();
				}
				else
				{
					if ( !mDropSection.dropSection() )
					{
						mDropSection.interact((int)event.getX(PRI), (int)event.getY(PRI),MotionEvent.ACTION_UP);
					}
					
					// If in tutorial, interact with widgets... 
					if (mTutorialState.isInTutorial)
						mWidgetLayer.drop();
					
				}
				
				
			}
			// Simultaneously lifted primary & secondary 
			else if (hasFinger[PRI] && hasFinger[SEC]  && eventPointerCount == 2)
			{
				hasFinger[PRI] = false;
				hasFinger[SEC] = false;
				end[PRI][X] = (int) event.getX(PRI);
				end[SEC][X] = (int) event.getX(SEC);	
				end[PRI][Y] = (int) event.getY(PRI);
				end[SEC][Y] = (int) event.getY(SEC);
			}
			// Still have primary, lifted secondary
			else if (!hasFinger[PRI] && hasFinger[SEC]  && eventPointerCount == 2)
			{
				hasFinger[SEC] = false;
				end[SEC][X] = (int) event.getX(SEC);
				end[SEC][Y] = (int) event.getY(SEC);
				
			}
			else
			{	
				
				
				// invalidate
				hasFinger[PRI]	= false;
				start[PRI][X]	= -1;
				end[PRI][X]		= -1;
				start[PRI][Y]	= -1;
				end[PRI][Y]		= -1;

				hasFinger[SEC]	= false;
				start[SEC][X]	= -1;
				end[SEC][X]		= -1;
				start[SEC][Y]	= -1;
				end[SEC][Y]		= -1;
				

				
				if (!game.getIsGamePaused())
				{
					// drop position
					mBoards.dropActiveBlock(convertPixToRC((int)event.getX(PRI), (int) event.getY(PRI)));
					
					// If in tutorial, interact with widgets... 
					if (mTutorialState.isInTutorial)
						mWidgetLayer.drop();
					
					// drop menu
					mDropSection.dropSection();
				}
				else
				{
					if ( !mDropSection.dropSection() )
					{
						mDropSection.interact((int)event.getX(PRI), (int) event.getY(PRI), MotionEvent.ACTION_UP);
					}
					
					// If in tutorial, interact with widgets... 
					if (mTutorialState.isInTutorial)
						mWidgetLayer.drop();
					
				}
				
				
				
			}

		}


		// Keep history
		if (hasFinger[PRI] && hasFinger[SEC] && eventPointerCount > 1)
		{
			 delta[PRI][X] = (int) event.getX(PRI);
			 delta[SEC][X] = (int) event.getX(SEC);
			 delta[PRI][Y] = (int) event.getY(PRI);
			 delta[SEC][Y] = (int) event.getY(SEC);
		}
		
		
		// Is a two finger swipe complete?
		if (end[SEC][X] != -1 && end[PRI][X] != -1 || end[SEC][Y] != -1 && end[PRI][Y] != -1)
		{
			boolean validHorizontal = Math.abs(start[PRI][X] - end[PRI][X]) > SWIPE_MIN_HORIZ_DISTANCE && Math.abs(start[SEC][X] - end[SEC][X]) > SWIPE_MIN_HORIZ_DISTANCE;
			boolean validVertical = Math.abs(start[PRI][Y] - end[PRI][Y]) > SWIPE_MIN_VERTI_DISTANCE && Math.abs(start[SEC][Y] - end[SEC][Y]) > SWIPE_MIN_VERTI_DISTANCE;
			int horizontalDist = Math.max(Math.abs(start[PRI][X] - end[PRI][X]), Math.abs(start[SEC][X] - end[SEC][X]));
			int verticalDist = Math.max(Math.abs(start[PRI][Y] - end[PRI][Y]), Math.abs(start[SEC][Y] - end[SEC][Y]));
			
			// Check for valid horizontal swipe
			if (validHorizontal && horizontalDist > verticalDist /*&& verticalDist < SWIPE_MAX_OFF_PATH*/ ) // correction: allow centering to fall on the correct side
			{
				if ( (start[PRI][X] - end[PRI][X]) < 0 && (start[SEC][X] - end[SEC][X]) < 0)
				{
					// swipe right
					if (hintAccepted)
					{
						// TEMP TEMP TEMP?
						// IS this right???
						//mWidgetLayer.clearItems();
						
						//new AsyncTransitionRight().execute();
						mBoards.transitionRight();
						
						//game.text = "RIGHT";
						//game.textviewHandler.post( game.updateTextView );
					}
					else
					{
						// TEMP TEMP TEMP?
						// IS this right???
						//mWidgetLayer.clearItems();
						
						//new AsyncTransitionCenter().execute();
						mBoards.transitionCenter();
						
						// vibrate 3x...
						try
						{
							game.getVibratorHandler().post( game.getErrorVibratorSequence() );	
						}
						catch (Exception e)
						{
							//Log.e("Vibrator", "Could not vibrate! " + e.toString());
						}
						
						//game.text = "RIGHT DENIED";
						//game.textviewHandler.post( game.updateTextView );
						
					}

				}
				else if ( (start[PRI][X] - end[PRI][X]) > 0 && (start[SEC][X] - end[SEC][X]) > 0)
				{
					// swipe left
					if (hintAccepted)
					{
						//new AsyncTransitionLeft().execute();
						mBoards.transitionLeft();
						
						//game.text = "LEFT";
						//game.textviewHandler.post( game.updateTextView );
					}
					else
					{
						//new AsyncTransitionCenter().execute();
						mBoards.transitionCenter();
						
						// vibrate 3x...
						try
						{
							game.getVibratorHandler().post( game.getErrorVibratorSequence() );	
						}
						catch (Exception e)
						{
							//Log.e("Vibrator", "Could not vibrate! " + e.toString());
						}
						
						//game.text = "LEFT DENIED";
						//game.textviewHandler.post( game.updateTextView );
					}

				}
				else
				{
					//new AsyncTransitionCenter().execute();
					mBoards.transitionCenter();
				}
			}
			// Check for valid vertical swipe
			else if (validVertical && horizontalDist < verticalDist && horizontalDist < SWIPE_MAX_OFF_PATH)
			{
				if ( (start[PRI][Y] - end[PRI][Y]) < 0 && (start[SEC][Y] - end[SEC][Y]) < 0)
				{
					
					if (game.getGlobalOrient() == Orientation.NORMAL)
					{
						// swipe down
						//new AsyncTransitionCenter().execute();
						mBoards.transitionCenter();
					}
					else
					{
						// swipe up
						//new AsyncTransitionCenter().execute();
						mBoards.transitionCenter();
						//new AsyncQuickenBoardProgression().execute();
						mBoards.quickenBoardProgression();
					}

				}
				else if ( (start[PRI][Y] - end[PRI][Y]) > 0 && (start[SEC][Y] - end[SEC][Y]) > 0)
				{
					
					if (game.getGlobalOrient() == Orientation.NORMAL)
					{
						// swipe up
						//new AsyncTransitionCenter().execute();
						mBoards.transitionCenter();
						//new AsyncQuickenBoardProgression().execute();
						mBoards.quickenBoardProgression();
					}
					else
					{
						// swipe down
						//new AsyncTransitionCenter().execute();
						mBoards.transitionCenter();
					}

				}
				else
				{	
					//new AsyncTransitionCenter().execute();
					mBoards.transitionCenter();
				}
			}
			else
			{	
				//new AsyncTransitionCenter().execute();
				mBoards.transitionCenter();
			}
			
			// invalidate
			hasFinger[PRI]	= false;
			start[PRI][X]	= -1;
			end[PRI][X]		= -1;
			start[PRI][Y]	= -1;
			end[PRI][Y]		= -1;

			hasFinger[SEC]	= false;
			start[SEC][X]	= -1;
			end[SEC][X]		= -1;
			start[SEC][Y]	= -1;
			end[SEC][Y]		= -1;
			
			hintAccepted = true;
			
		}
		
		
		return true;
	    	
	    
    }
    
    
    /**
     * Convert pixel coordinates to Row/Column coordinates
     * 
     * @param x pix coord
     * @param y pix coord
     * @return	Row/Col
     */
    public double mCurrentPixYOffset = 0;
    
    
	public Coord<Integer> convertPixToRC(float pixX, float pixY)
	{
		
		// keep up with the moving board
		if (game.getGlobalOrient() == Orientation.NORMAL)
			mCurrentPixYOffset = (float) (mBoards.mRowIterationPercentage*mScreenBlockLength);
		else
			mCurrentPixYOffset = (float) (mScreenBlockLength - (mBoards.mRowIterationPercentage*mScreenBlockLength));
		
		
		
		/*
		game.text = String.valueOf((int) (((mScreenHeight-pixY)-mCurrentPixYOffset)/mScreenBlockLength)) + ", " +
					String.valueOf((int) (pixX/mScreenBlockLength) ) + "\n" + game.text;
		game.textviewHandler.post( game.updateTextView );
		*/
		
		
		// assumes origin @ bottom left; only returns within defined row/col count of board (no more or less)
		return new Coord<Integer> ( 
				Math.min( MainActivity.ROWCOUNT-1,
					Math.max( 0 , 
							(int) (((mScreenHeight-pixY)-mCurrentPixYOffset)/mScreenBlockLength)
							)
						)
				,
				
				Math.min( MainActivity.COLCOUNT-1,
					Math.max( 0 , 
							(int) (pixX/mScreenBlockLength) 
							)
						)
				
				);
		
	}
	
	
	
	

	/*
	private class AsyncTransitionLeft extends AsyncTask< Void, Void, Void> 
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			mBoards.transitionLeft();
			return null;
		}
	}
	
	private class AsyncTransitionRight extends AsyncTask< Void, Void, Void> 
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			mBoards.transitionRight();
			return null;
		}
	}
	
	private class AsyncTransitionCenter extends AsyncTask< Void, Void, Void > 
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			mBoards.transitionCenter();
			return null;
		}
	}
	
	
	private class AsyncQuickenBoardProgression extends AsyncTask< Void, Void, Void > 
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			mBoards.quickenBoardProgression();
			return null;
		}
	}
	*/
	
	
	// TEMP TMEP TEMP
	//public static final int STARTOFGAMEDELAY = 3600;
	public static final int STARTOFGAMEDELAY = 3000;
	public static final int ENDOFGAMEDELAY = 2000;
	public static final int POSTENDOFGAMEDELAY = DropSection.ENDGAME_DURATION/2;
	public long endOfGameTrigger = -1;
	public long postEndOfGameTrigger = -1;
	public long startOfGameTrigger = -1;
	
	public void cancelGameTriggers()
	{
		startOfGameTrigger = -1;
		endOfGameTrigger = -1;
	}
	
	// point of no return
	public void endGame()
	{
		endGame(ENDOFGAMEDELAY);
	}
	
	public void endGame(int delay)
	{
		//game.text += "End of Game\n";
		//game.textviewHandler.post( game.updateTextView );
		
		// center the board if the game ended in mid-drag of boards
		//new AsyncTransitionCenter().execute();
		mBoards.transitionCenter();
		
		// do now stuff
		pauseWorld(false);
		game.setIsGameOver( true );
		mBoards.pauseBoardProgression();
		if (delay == 0)
			mBoards.mGameState.endGame(false);
		else
			mBoards.mGameState.endGame();
		
		// show delayed stuff
		endOfGameTrigger = System.currentTimeMillis() + delay;
	}
	
	private void doEndGame()
	{
		//game.text += "**Do** End of Game\n";
		//game.textviewHandler.post( game.updateTextView );
		
		mBoards.endGame();
		mBoards.mGameState.doEndGame();
		
		// show delayed stuff
		postEndOfGameTrigger = System.currentTimeMillis() + POSTENDOFGAMEDELAY;
	}
	
	private void doPostEndGame()
	{
		//game.text += "Do **POST** End of Game\n";
		//game.textviewHandler.post( game.updateTextView );
		
		mDropSection.mScoreBanner.doPostEndGame();
	}
	
	public void startTutorial(Integer dur, MotionEquation eq)
	{
		mTutorialState.isInTutorial = true;
		
		startOfGameTrigger = -1;
		postEndOfGameTrigger = -1;
		endOfGameTrigger = -1;
		
		// clear widget layer
		mWidgetLayer.clearItems();
		
		//outro the current menu
		mMenus.transitionToMenu(MenuManager.NONE, false, dur, eq);
		
		// make any menu go away!...
		mBoards.transitionUp(DropSection.DEFAULT_MOTION, DropSection.DEFAULT_DURATION);
		mDropSection.changeStateTo(DropSectionState.FOLDED, dur, eq); 
		
		mDropSection.mStatsFogScreen.hideNow();
		
		mBoards.startGame(true);
		
		mDropSection.mScoreBanner.outroInGameStats(1);
		
		mDropSection.mScoreBanner.outro(true);
		
		mDropSection.lock();
		
		//mMenus.lock();
		
		mBoards.mEnableBoardProgression = false;
		resumeWorld(false);
		mBoards.doStartGame();
		
		mBoards.disableGarbage();
		mBoards.disableBonusBlocks();
		
		mTutorialMenu.unlock();
		mTutorialMenu.mEnabled = true;
		mTutorialMenu.transitionToMenu(TutorialBannerMenu.RULES, true);
		
	}
	
	public void startGame(Integer dur, MotionEquation eq)
	{
		mTutorialState.isInTutorial = false;
		
		// assume initGame() has been called!
		//resumeWorld(true, dur, eq);
		
		postEndOfGameTrigger = -1;
		endOfGameTrigger = -1;
		
		// clear widget layer
		mWidgetLayer.clearItems();
		
		//outro the current menu
		mMenus.transitionToMenu(MenuManager.NONE, false, dur, eq);
		
		// make any menu go away!...
		mBoards.transitionUp(DropSection.DEFAULT_MOTION, DropSection.DEFAULT_DURATION);
		mDropSection.changeStateTo(DropSectionState.FOLDED, dur, eq); 
		
		mDropSection.mStatsFogScreen.hideNow();
		
		mDropSection.lock();
		mMenus.lock();
		
		mBoards.startGame();

		mBoards.enableGarbage();
		mBoards.enableBonusBlocks();
		
		mTutorialMenu.lock();
		mTutorialMenu.mEnabled = false;
		
		startOfGameTrigger = System.currentTimeMillis() + STARTOFGAMEDELAY;

	}
	
	public void doStartGame()
	{
		mDropSection.unlock();
		mMenus.unlock();
		
		// assume initGame() has been called!
		resumeWorld(false);
		
		mBoards.doStartGame();
		
	}
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{	
		
		// update score board
		mDropSection.update(now, primaryThread, secondaryThread);
		
		// update board
		mBoards.update(now, primaryThread, secondaryThread);	
		
		// update widget layer
		mWidgetLayer.update(now, primaryThread, secondaryThread);
		
		if (secondaryThread)
		{
			/*
			game.text = "";
			game.text += "Over    : " + game.isGameOver + "\n";
			game.text += "Paused  : " + game.isGamePaused + "\n";
			game.text += "Started : " + game.isGameStarted + "\n";
			game.textviewHandler.post( game.updateTextView );
			*/
			
			if (endOfGameTrigger > 0 && endOfGameTrigger < now)
			{
				endOfGameTrigger = -1;
				doEndGame();
			}
			
			if (postEndOfGameTrigger > 0 && postEndOfGameTrigger < now)
			{
				postEndOfGameTrigger = -1;
				doPostEndGame();
			}
			
			if (startOfGameTrigger > 0 && startOfGameTrigger < now)
			{
				startOfGameTrigger = -1;
				doStartGame();
			}
		}
		
	}
	
	
	float x=0, y=0, z=0;
	long startTemp = System.currentTimeMillis();
	public void draw(GL10 gl)
	{
		//game.text = "";
		
		if (
				mBoardUpdater.state == SecondaryUpdater.RUNNING
				
				&&
				
				mBlockUpdater.state == PrimaryUpdater.RUNNING
			)
		{	
			
			// draw all boards
			mBoards.draw(gl);
			
			// draw scoreboard / Menu
			mDropSection.draw(gl);
			
			// this used to be in the mBoards.draw...
			
			/*
			// draw widgets
			gl.glPushMatrix();
			
			mWidgetLayer.draw(gl);
			
			gl.glPopMatrix();
			*/

		}
	}

	
	
}
