package com.wagoodman.stackattack;

import java.util.ArrayList;
import java.util.Random;

import com.wagoodman.stackattack.MainActivity;

import android.content.Context;


public class GridManager extends ArrayList2D<GridElement>
{
	private static final long serialVersionUID = 8519866990061705766L;
	private static final String TAG = "GridMGR";
	private final Context mContext;
	private final MainActivity game;
	
	private static final int ROW = 0;
	private static final int COL = 1;

	private final Random randObjPool = new Random( System.currentTimeMillis() );
	
	public GridManager(Context context)
	{
		super(MainActivity.ROWCOUNT, MainActivity.COLCOUNT);
		// get the game object from context
		mContext = context;
		game = (MainActivity) (mContext);
		initializeGrid(MainActivity.ROWCOUNT, MainActivity.COLCOUNT);

	}
	
	
	public String toString()
	{
		return "GridManager: " + MainActivity.ROWCOUNT + " rows, " + MainActivity.COLCOUNT + " cols";
	}
	
	
	public String asciiBoard()
	{
		String ret = "";
		
		for (int row=0; row < MainActivity.ROWCOUNT; row++)
		{
			String line = "";
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				if (this.get(row, col).getBlockId() == null)
					line += "|   ";
				else
					line += "| X ";
				
			}
			ret = line + "\n" + ret;
		}
		
		return ret;
		
	}
	
	
	public void initializeGrid(int rows, int cols)
	{
		
		for (int row=0; row < rows; row++)
		{
			for (int col=0; col < cols; col++)
			{
				add( new GridElement(mContext, row, col ) , row);
			}
		}
		
	}

	public Boolean isEmpty(int row, int col)
	{
		if (getBlockId(row, col) == null)
			return true;
		
		return false;		
	}
	
	
	/**
	 * Return the BlockId at the given row/col
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public String getBlockId(int row, int col)
	{
		try
		{
			return get(row, col).getBlockId();
		}
		catch(Exception e)
		{
			// Log.e("GridMgr GetBlkId RC",e.toString());
		}
		
		return null;
	}
	
	
	/**
	 * Return the BlockId at the given row/col
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public String getBlockId(Coord<Integer> rowcol)
	{
		try
		{
			return get(rowcol.getRow(), rowcol.getCol()).getBlockId();
		}
		catch(Exception e)
		{
			// Log.e("GridMgr GetBlkId Crd",e.toString());
		}
		
		return null;
	}
	
	/**
	 * Set the GridElement's occupying Block ID to the given id at row/col
	 * 
	 * @param row
	 * @param col
	 * @param blockId
	 */
	public Boolean setBlockId(int row, int col, String blockId)
	{
		try
		{
			get(row, col).setBlockId(blockId);
		}
		catch(Exception e)
		{
			// Log.e("GridMgr SetBlkId RC",e.toString());
			return false;
		}
		
		return true;

	}
	
	/**
	 * Set the GridElement's occupying Block ID to the given id at row/col
	 * 
	 * @param row
	 * @param col
	 * @param blockId
	 */
	public Boolean setBlockId(Coord<Integer> rowcol, String blockId)
	{
		try
		{
			get(rowcol.getRow(), rowcol.getCol()).setBlockId(blockId);
		}
		catch(Exception e)
		{
			// Log.e("GridMgr SetBlkId Crd",e.toString());
			return false;
		}
		
		return true;

	}
	
	
	
	public Boolean deleteBlock(int row, int col)
	{
		try
		{
			get(row, col).setBlockId(null);
		}
		catch(Exception e)
		{
			// Log.e("GridMgr DeletBlk RC",e.toString());
			return false;
		}
		
		return true;

	}
	
	
	public Boolean deleteBlock(Coord<Integer> rowcol)
	{
		try
		{
			get(rowcol.getRow(), rowcol.getCol()).setBlockId(null);
		}
		catch(Exception e)
		{
			// Log.e("GridMgr DeleteBlk Crd",e.toString());
			return false;
		}
		
		return true;
	}
	
	
	public Boolean deleteBlock(String blockId)
	{
		try
		{
			for (int row=0; row < MainActivity.ROWCOUNT; row++)
			{
				for (int col=0; col < MainActivity.COLCOUNT; col++)
				{
					if (get(row, col).getBlockId() == blockId)
					{
						get(row, col).setBlockId(null);
						return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			// Log.e("GridMgr DeleteBlk ID",e.toString());
		}
		
		return false;
	}
	
	// Need false statement
	String first, second;
	public Boolean swapBlocks( Coord<Integer> rowcol1, Coord<Integer> rowcol2 )
	{
		try
		{
			first = getBlockId(rowcol1);
			second = getBlockId(rowcol2);
		}
		catch(Exception e)
		{
			// Log.e("GridMgr SwapBlocks",e.toString());
			return false;
		}
		
		try
		{
			setBlockId(
					rowcol1,  
					second
					);
			setBlockId(
					rowcol2,  
					first
					);
			
			return true;
		}
		catch(Exception e) 
		{
			// Log.e("GridMgr SwapBlocks",e.toString());
		}
		
		
		return false;
		
	}
	
	public Coord<Integer> getBlockPosition(String id)
	{
		for (int row=0; row < MainActivity.ROWCOUNT; row++)
		{
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				if (get(row, col).getBlockId() == id)
				{
					return new Coord<Integer>( row, col );
				}
			}
		}
		return null;
	}
	
	
	
	public ArrayList<Coord<Integer>> getRandomFilledBlockPositions(double percFilledPositions)
	{
		ArrayList<Coord<Integer>> ret = new ArrayList<Coord<Integer>>();
		ArrayList<Coord<Integer>> filledSet = new ArrayList<Coord<Integer>>();
		
		int filledPosCount = 0;
		
		for (int row=0; row < MainActivity.ROWCOUNT; row++)
		{
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				if (get(row, col).getBlockId() != null)
				{
					filledSet.add(new Coord<Integer>(row,col));
				}
			}
		}
		
		filledPosCount = filledSet.size();
		
		for (int idx=0; idx < (filledPosCount*percFilledPositions) ; idx++)
		{
			if (filledSet.size() > 2)
				ret.add( filledSet.remove( randObjPool.nextInt(filledSet.size()-1)) );	
			else
				// done early!
				return ret;
		}
		
		return ret;
	}
	
	public ArrayList<Coord<Integer>> getRandomFilledBlockPositions(int count)
	{
		ArrayList<Coord<Integer>> ret = new ArrayList<Coord<Integer>>();
		ArrayList<Coord<Integer>> filledSet = new ArrayList<Coord<Integer>>();
		
		for (int row=0; row < MainActivity.ROWCOUNT; row++)
		{
			for (int col=0; col < MainActivity.COLCOUNT; col++)
			{
				if (get(row, col).getBlockId() != null)
				{
					filledSet.add(new Coord<Integer>(row,col));
				}
			}
		}
		
		for (int idx=0; idx < count ; idx++)
		{
			if (filledSet.size() > 2)
				ret.add( filledSet.remove( randObjPool.nextInt(filledSet.size()-1)) );	
			else
				// done early!
				return ret;
		}
		
		return ret;
	}
	


}
