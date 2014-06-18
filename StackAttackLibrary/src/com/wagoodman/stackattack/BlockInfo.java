package com.wagoodman.stackattack;

import com.wagoodman.stackattack.BlockValue;

public class BlockInfo 
{
	public final String mBlockId;
	public final Coord<Integer>	mBlockCoord;
	public final BlockValue mBlockValue;
	
	BlockInfo(String id, Coord<Integer> coord, BlockValue bv)
	{
		mBlockId = id;
		mBlockCoord = coord.clone();
		mBlockValue = bv;
	}
	
    public boolean equals(Object other) {
        if (other instanceof BlockInfo) 
        { 
        	return mBlockId.equals(((BlockInfo) other).mBlockId); 
        }

        return false;
    }
    
}
