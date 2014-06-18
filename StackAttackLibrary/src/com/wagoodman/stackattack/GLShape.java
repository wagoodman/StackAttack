
package com.wagoodman.stackattack;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.Identity;
import com.wagoodman.stackattack.World;

abstract class GLShape extends Identity
{
	
	// Specific Delegate Members 
	public AnimationBroker	mAnimation;
	public ColorBroker 		mColor;
	//public GLRect			mShape;	// not here! high memory cost


	// ...Tangibility
	public Boolean	isInteractable = true;	// User cannot manipulate it
	public Boolean	isFrozen = false;		// User and settle cannot manipulate it
	public Boolean	isVisible = true; 		// not visible until first animation: false
	public Boolean 	isSeeThru = false;
	
	// used to trigger deletion / settling
	public Boolean isDead = false;
	public Boolean isMatching = false;
	
	// Block dep items
	protected World.BLOCKTYPES mBlockType;
	public BlockValue mBlockValue = BlockValue.NORMAL;
	
	public GLShape(/*Context context*/)
	{

	}

	//abstract public void update(long now);
	abstract public void update(long now, Boolean primaryThread, Boolean secondaryThread);
	abstract public void draw(GL10 gl, float[][] offset);
	abstract public void draw(GL10 gl);

}
