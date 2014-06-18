package com.wagoodman.stackattack;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.wagoodman.stackattack.Block;
import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.GLShape;
import com.wagoodman.stackattack.MainActivity;

import android.content.Context;


public class ShapeManager extends ConcurrentHashMap<String, GLShape>
{

	private static final long serialVersionUID = -432751726541036817L;
	private static final String TAG = "BlkMGR";
	private static final Boolean debug = false;
	private final MainActivity game;
	
	// blocks the user is actively interacting with
	public Block mActiveBlock;
	public long	 mActiveBlockStartTime = -1;
	public static final int mActiveBlockExpirationDuration = 2500;
	public Coord<Integer> mActiveBlockStartCoord;
	//public final int mActiveBlockExpirationDuration = 60000;	//TEMP TEMP TEMP
	
	public ShapeManager(Context context)
	{
		super();
		// get the game object from context
		game = (MainActivity) (context);

	}
	/*
	@SuppressWarnings("unused")
	private void DEBUG(String logString)
	{
		if (debug == true) Log.d(TAG, logString);
	}

	private void ERROR(String logString)
	{
		if (debug == true) Log.e(TAG, logString);
	}
	*/
	
	/**
	 * Call when the user begins to interact with the block. 
	 * 
	 * @param id
	 * @return
	 */
	public Boolean hasActiveBlock()
	{
		if (mActiveBlock == null)
			return false;
		
		return true;
	}
	
	
	/**
	 * Call when the user begins to interact with the block. 
	 * 
	 * @param id
	 * @return
	 */
	public Boolean setActive(String id, Coord<Integer> start)
	{
		GLShape shape = get(id);
		
		if (shape != null)
		{
			if (shape instanceof Block)
			{
				mActiveBlockStartCoord = start.clone();
				
				mActiveBlock = (Block) shape;
				mActiveBlockStartTime = System.currentTimeMillis();
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Call when the user is done interacting with the block.
	 * 
	 * @param id
	 * @return
	 */
	public Boolean setInactive(String id)
	{
		GLShape block = get(id);
		if (block != null && mActiveBlock != null)
		{
			if (mActiveBlock.getId() == id)
			{
				mActiveBlockStartTime = -1;
				mActiveBlock = null;
				return true;
			}
		}
		
		return false;
	}
	

	public Boolean put(GLShape block)
	{
		try
		{
			put(block.getId(), block);
			return true;
		}
		catch(Exception e)
		{
			//ERROR(e.toString());
		}
		
		return false;
	}
	
	public Boolean kill(String blockId)
	{
		try
		{
			GLShape block = get(blockId);
			block.isDead = true;
			block.isVisible = true;
			
			return true;
		}
		catch(Exception e)
		{
			//ERROR(e.toString());
		}
		
		return false;
	}

	public Boolean remove(GLShape block)
	{
		try
		{
			remove(block.getId());
			return true;
		}
		catch(Exception e)
		{
			//ERROR(e.toString());
		}
		
		return false;
	}
	
	
	public ArrayList<String> getAllBlockIdsWithColor(Color color)
	{
		ArrayList<String> ret = new ArrayList<String>();
		
		for (GLShape block : values())
		{
			if (block != null)
			{
				if (block.mColor.mCurrentColor == color)
				{
					if (block.isInteractable)
					{
						ret.add(block.getId());
					}
				}
			}
		}
		
		return ret;
	}


}
