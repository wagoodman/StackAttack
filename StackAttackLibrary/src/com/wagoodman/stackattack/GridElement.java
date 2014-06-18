package com.wagoodman.stackattack;

import com.wagoodman.stackattack.Identity;
import com.wagoodman.stackattack.MainActivity;

import android.content.Context;


public class GridElement extends Identity
{
	private static final String TAG = "BoardElement";
	private static final Boolean debug = false;
	private final Context context;
	private final MainActivity game;
	
	//private int startRef;
	private int startRow;			// Row of instantiation.
	private int finalCol;			// Col of instantiation.

	private String blockId = null;

	/**
	 * @param context	context of the game
	 * @param startRow	the row this BlockElement was instantiated in.
	 * @param finalCol	the column this BlockElement was instantiated in and will stay in.
	 */
	public GridElement(Context context, int startRow, int finalCol /*, int ref*/ ) {
		//DEBUG("Creating GridElement...");

		this.context	= context;
		game 			= (MainActivity) context;
		this.startRow	= startRow;
		this.finalCol	= finalCol;
		//this.startRef	= ref;
	}
	
	public GridElement(Context context, int startRow, int finalCol, String blockId /*, int ref*/) {
		//DEBUG("Creating GridElement...");

		this.context	= context;
		game 			= (MainActivity) context;
		this.startRow	= startRow;
		this.finalCol	= finalCol;
		this.blockId	= blockId;
		//this.startRef	= ref;
	}
	
	/*
	@SuppressWarnings("unused")
	private void DEBUG(String logString)
	{
		if (debug == true) Log.d(TAG, logString);
	}
	@SuppressWarnings("unused")
	private void ERROR(String logString)
	{
		if (debug == true) Log.e(TAG, logString);
	}
	*/
	
	/**
	 * Set the occupying block to the given id
	 * 
	 * @param id	set the current block id to the given string
	 */
	public void setBlockId(String givenId)
	{
		blockId = givenId;
	}
	
	/**
	 * Returns a block id currently occupying this grid element
	 * 
	 * @return
	 */
	public String getBlockId()
	{
		return blockId;
	}
	
	
	/**
	 * Determines the current row this block is in. As the board gets new rows
	 * appended to the bottom, this number should increase due to the curRowIndex
	 * stored in the game board structure.
	 * 
	 * @return	Integer representing row
	 */
	public int getRow(int ref)
	{
		//return startRow + game.board.curRowIndex;
		return startRow + ref;
	}
	

	/**
	 * Retrieves the initial/final column this block is in. In theory this should
	 * never change (The BlockElements change row/col position as the game progresses)
	 * 
	 * @return	Integer representing column
	 */
	public int getCol()
	{
		return finalCol;
	}
	
	
} 

