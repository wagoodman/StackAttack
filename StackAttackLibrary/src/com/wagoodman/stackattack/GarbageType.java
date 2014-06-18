package com.wagoodman.stackattack;

import java.util.EnumSet;

import com.wagoodman.stackattack.Color;


public enum GarbageType
{
	//Type		Probability of selecting type
	NORMAL 		(7,	Color.DARKGREY),	 
	BARRIER 	(1,	Color.RED_LIGHT)
	;
	
	private double	prob;
	private Color	color;
	
	private static final RandomCollection<GarbageType> rnd = new RandomCollection<GarbageType>();
	
	static 
	{
		for(GarbageType s : EnumSet.allOf(GarbageType.class))
			rnd.add(s.prob, s);
	}
	
	GarbageType(double p, Color c)
	{
		prob = p;
		color = c;
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public static GarbageType pickGarbageType()
	{
		return rnd.next();
	}
}
