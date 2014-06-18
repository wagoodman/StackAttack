package com.wagoodman.stackattack;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.MainActivity;

import android.content.Context;


public class Menu
{
	private final Context mContext;
	private final MainActivity game;

	private ArrayList<GLMenuItem>	mItems = new ArrayList<GLMenuItem>();
	private int mOutroDuration = 0;
	
	private Boolean mInteractable = true;
	
	public String mBackMenu = MenuManager.NONE;
	
	public Menu(Context context, String backMenu)
	{
		mContext = context;
		game = (MainActivity) mContext;
		
		mBackMenu = backMenu;
	}
	
	public void setDependencies()
	{
		for (GLMenuItem item : mItems)
		{	
			item.setLabelDimensions();
			item.setFontDimensions();
		}
	}
	
	public void addItem(GLMenuItem item)
	{
		// increment the outro duration time
		mOutroDuration = Math.max(mOutroDuration, item.getOutroDuration());
		
		// add the item
		mItems.add(item);
	}
	
	
	public Boolean interact(int x, int y, int pixFontOffset)
	{
		if (mInteractable)
		{
			for (GLMenuItem item : mItems)
				if (item.interact(x, y, pixFontOffset))
					return true;
		}
	
		return false;
	}
	
	public Boolean gotoBackMenu(Integer dur, MotionEquation eq)
	{
		if (mBackMenu != MenuManager.NONE)
		{
			// go to a menu...
			if (!game.getIsGameStarted())
			{
				if (eq == null)
					eq = DropSection.SLOW_EQ;
				if (dur == null)
					dur = DropSection.SLOW_DURATION;
			}
			else
			{
				if (eq == null)
					eq = DropSection.DEFAULT_MOTION;
				if (dur == null)
					dur = DropSection.DEFAULT_DURATION;
			}
			
			game.getWorld().mMenus.transitionToMenu(mBackMenu, true, dur, eq);
			
			return true;
		}
		
		return false;
	}
	
	
	
	public void intro()
	{
		mInteractable = true;
		
		for (GLMenuItem item : mItems)
			item.intro();

	}
	
	public void intro(Integer text_duration)
	{
		mInteractable = true;
		
		for (GLMenuItem item : mItems)
			item.intro(text_duration);

	}
	
	public void outro()
	{
		mInteractable = false;
		
		for (GLMenuItem item : mItems)
			item.outro();

	}
	
	/*
	public void hide()
	{
		for (GLMenuItem item : mItems)
			item.hide();
	}
	*/
	
	public int getOutroDuration()
	{
		return mOutroDuration;
	}
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
		for (GLMenuItem item : mItems)
			item.update(now, primaryThread, secondaryThread);
		
	}
	
	
	public void draw(GL10 gl, float pixYOffset)
	{
		
		for (GLMenuItem item : mItems)
			item.draw(gl, pixYOffset);

	}
	
}
