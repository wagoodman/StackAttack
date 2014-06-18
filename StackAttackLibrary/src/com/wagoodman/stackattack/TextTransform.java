package com.wagoodman.stackattack;

public enum TextTransform
{

Slinky					(500),
ColorHint				(800),

Worm					(500),

Flick					(700),

DropFadeIn				(900),
DropFadeOut				(900),

MoveInFromLeft			(2000),
MoveInFromRight			(2000),
MoveOutRight			(2000),
MoveOutLeft				(2000),
//ScrollOutRight		(2000),
//ScrollOutLeft			(2000),

MoveDiagLeftIn			(2000),
MoveDiagLeftOut			(2000),

RollInFromLeft			(1800),
RollOutRight			(900),

VacuumInFromLeft		(900),
VacuumOutRight			(900),
;


	public int mDuration;

	TextTransform(int stdDuration)
	{
		mDuration = stdDuration;
	}
	
	public int getDuration()
	{
		return mDuration;
	}
	
	
}
