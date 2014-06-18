package com.wagoodman.stackattack;

import java.util.concurrent.CopyOnWriteArrayList;



import android.content.Context;
import android.util.SparseIntArray;

public class BoardSideStatusManager 
{
	private final Context mContext;
	private final MainActivity game;
	
	private Boolean isDisabled = false;
	
	BoardSideStatusManager(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
	}
	
	public void endGame()
	{
		isDisabled = true;
		
		// clear 
		game.getWorld().mDropSection.mLeftBoardIndicator.updateRow(0);
		game.getWorld().mDropSection.mRightBoardIndicator.updateRow(0);		
	}

	
	private int max, min, cur, leftVal, rightVal;
	public void update(SparseIntArray boardRowCounts, CopyOnWriteArrayList<Integer> currentFaces)
	{
		if (!isDisabled)
		{
			
			//game.text = "\n\n\n\n\n\n\n";
			
			// reset
			leftVal = 0;
			rightVal = 0;
			
			// are there boards?
			if (currentFaces.size()>0 && !game.getIsGamePaused())
			{
				// determine part orientation (which board heights to consider where)
				max = currentFaces.get(0);
				min = currentFaces.get(0);
				
				for (int idx=1; idx < currentFaces.size(); idx++)
				{
					// board num
					cur = currentFaces.get(idx);
					if (cur > max)
						max = cur;
					if (cur < min)
						min = cur;
				}
				

						
				//determine values
				for (int idx=0; idx < boardRowCounts.size(); idx++)
				{
					//height
					cur = boardRowCounts.get(idx);
						
					if (idx > max)
						leftVal = Math.max(cur, leftVal);
					if (idx < min)
						rightVal = Math.max(cur, rightVal);
					
				}
				
				//game.text += "Left Board : " + max + " = " + leftVal + "\n";
				//game.text += "Right Board: " + min + " = " + rightVal + "\n";
				//game.text += "\n";
				
				
			}
			
			// no boards to update with
			else
			{
				leftVal = 0;
				rightVal = 0;
			}
			
			game.getWorld().mDropSection.mLeftBoardIndicator.updateRow(leftVal);
			game.getWorld().mDropSection.mRightBoardIndicator.updateRow(rightVal);	
			
			//game.textviewHandler.post( game.updateTextView );
			
		}
	}
	
}
