package com.wagoodman.stackattack;

public enum DropSectionState
{
	// these are set int DropSection.java
	FOLDED   (1),
	PEEK     (1),
	PEEK_UNFOLDEDSCORE (1),
	UNFOLDED (1),
	FULL     (1);
	
	private double heightPercentage;

	DropSectionState(double height)
	{
		heightPercentage = height;
	}
	
	public double getHeight()
	{
		return heightPercentage;
	}
	
	public void setHeight(double height)
	{
		heightPercentage = height;
	}
}
