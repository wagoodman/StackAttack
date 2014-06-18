package com.wagoodman.stackattack;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.MainActivity;

import android.content.Context;
import android.util.Log;


public class FontManager 
{
	private final Context mContext; 
	private final MainActivity game;
	private HashSet<String>	mFontNames = new HashSet<String>();
	private ConcurrentHashMap<String, TexFont>  mTexFont = new ConcurrentHashMap<String, TexFont>();
	private ConcurrentHashMap<String, Integer>  mFontId  = new ConcurrentHashMap<String, Integer>();
	private ConcurrentHashMap<String, Float>  mFontHorizontalCount  = new ConcurrentHashMap<String, Float>();
	
	public static final String SCORE_FONT = "BlueHighwayRg40.bff";//"Fortheloveofhate.bff";
	
	//public static final String MENUITEM_FONT = "operatinginstructions60.bff";
	public static final String MENUMAJORITEM_FONT = "telegrafico30.bff";
	public static final String MENUMINORITEM_FONT = "BlueHighwayRg40.bff";
	public static final String ARROW_FONT = "Eyecicles40.bff";
	
	/*
	public static final float[] BANNER_TITLE_COLOR = Color.WHITE.ambient().clone();
	public static final float[] BANNER_VALUE_COLOR = Color.TAN.ambient().clone();
	public static final float[] BANNER_SCORE_COLOR = Color.WHITE.ambient().clone();
	*/
	public static final Color BANNER_TITLE_COLOR = Color.TAN;
	public static final Color BANNER_VALUE_COLOR = Color.WHITE;
	public static final Color BANNER_SCORE_COLOR = Color.WHITE;
	public static final Color ERROR_COLOR = Color.RED_LIGHT;
	
	public FontManager(Context context)
	{
		mContext = context;
		game = (MainActivity) context;
	
		// font name & # of "1"s that fits on the screen horizontally
		addFont("telegrafico30.bff", 48f );			// menu major
		addFont("BlueHighwayRg40.bff", 58f );		// menu minor / score
		//addFont("Fortheloveofhate.bff", 28.236f);	// score?
		addFont("Eyecicles40.bff", 20f);			// arrows
	}
	
	public void loadFonts(GL10 gl)
	{
		// Generate GL texture ID
		int texId[] = new int[mFontNames.size()];
		gl.glGenTextures(texId.length, texId, 0);
		
		int idx=0;
		for (String font : mFontNames)
			mFontId.put(font, texId[idx++]);
		
		for (Entry<String, Integer> fontEntry : mFontId.entrySet())
		{
			//Log.d("LOADFONT", fontEntry.getKey());
			
			// Add new texFont
			mTexFont.put(fontEntry.getKey(), new TexFont(mContext, gl, mFontId.get(fontEntry.getKey())));
			
			// Load font
			try 
			{
				mTexFont.get(fontEntry.getKey()).LoadFont(fontEntry.getKey(), gl, mFontHorizontalCount.get(fontEntry.getKey()));
				//Log.d("LOADED", fontEntry.getKey());
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				//Log.e("FontManager.loadFonts()", e.toString());
			}
		}
		
	}
	
	
	public void addFont(String fontAssetName, float horizontalCount)
	{
		mFontNames.add(fontAssetName);
		mFontHorizontalCount.put(fontAssetName, horizontalCount);
	}
	
	public TexFont getFont(String fontAssetName)
	{
		return mTexFont.get(fontAssetName);
	}
	
	public float getFontHeight(String fontAssetName)
	{
		tempFont = getFont(fontAssetName);
		return tempFont.fntCellHeight*tempFont.charScale;	
	}

	
	public int getStringWidth(String fontAssetName, String text)
	{
		return mTexFont.get(fontAssetName).getStringWidth(text);	
	}
	
	public int[] getCharWidthArray(String fontAssetName)
	{
		tempFont = getFont(fontAssetName);
		int[] ret = tempFont.charWidth.clone();
		if (tempFont != null)
		{
			// scale font for usage
			for (int idx=0; idx < tempFont.charWidth.length; idx++)
			{
				ret[idx] = (int) (tempFont.charWidth[idx] * tempFont.charScale);
			}
			return ret;
				
		}
			
		
		return null;
	}
	
	public int getFirstCharOffset(String fontAssetName)
	{
		tempFont = getFont(fontAssetName);
		if (tempFont != null)
			return tempFont.firstCharOffset;
		
		return -1;
	}
	
	private TexFont tempFont;
	

	
	
	public void printAt(String fontAssetName, GL10 gl, String text, float x, float y, float[] rgba, Boolean leftJust, float fontScaleOffset)
	{
		tempFont = getFont(fontAssetName);
		if (tempFont != null)
			tempFont.printAt(gl, text, x, y, rgba, leftJust, fontScaleOffset);
	}

	
	public void printOffsetAt(String fontAssetName, GL10 gl, String text, float x, float y, float[] rgba, float[] peakrgba, Boolean leftJust, float[] charOffset, int pixXOffset, int pixYOffset, float[] charWidthMod, float[] charHeightMod, float fontScaleOffset)
	{
		tempFont = getFont(fontAssetName);
		if (tempFont != null)
			tempFont.printOffsetAt(gl, text, x, y, rgba, peakrgba, leftJust, charOffset, pixXOffset, pixYOffset, charWidthMod, charHeightMod, fontScaleOffset);
	}

	public void print3D(String fontAssetName, GL10 gl, String text,float[] rgba, float xScale, float yScale)
	{
		tempFont = getFont(fontAssetName);
		if (tempFont != null)
			tempFont.print3D(gl, text, rgba, xScale, yScale);
	}
	
	/*
	public void printDropFadeInAt(String fontAssetName, GL10 gl, String text, float x, float y, float[] rgba, Boolean leftJust, float percentOfPhrase, int pixOffset, int wordWidth)
	{
		tempFont = getFont(fontAssetName);
		if (tempFont != null)
			tempFont.printLogisticOffsetAt(gl, text, x, y, rgba, World.mMenuBackdropColor.ambient(), leftJust, percentOfPhrase, 0, pixOffset,  wordWidth, true);
	}
	
	public void printSlideRightFadeInAt(String fontAssetName, GL10 gl, String text, float x, float y, float[] rgba, Boolean leftJust, float percentOfPhrase, int pixOffset, int wordWidth)
	{
		tempFont = getFont(fontAssetName);
		if (tempFont != null)
			tempFont.printLogisticOffsetAt(gl, text, x, y, rgba, World.mMenuBackdropColor.ambient(), leftJust, percentOfPhrase, -pixOffset, 0,  wordWidth, true);
	}
	*/
	
}
