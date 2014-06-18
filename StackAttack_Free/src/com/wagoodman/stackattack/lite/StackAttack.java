package com.wagoodman.stackattack.lite;

import com.wagoodman.stackattack.StackAttackBase;


public class StackAttack extends StackAttackBase
{

	// LIBRARY VAR HOOKS
	public final Boolean isPaid = false;
	public final int mFreeVersionMaxPoints = 300;
	


	@Override
	public Boolean getIsPaid() { return isPaid; }
	
	@Override
	public int getFreeVersionMaxPoints() { return mFreeVersionMaxPoints; }

}