package com.wagoodman.stackattack;

import java.util.Random;


public enum Color {
	//Color  	MaterialAmbience			   			Material Diffusion							Enabled?
	NONE		(new float[] { 0, 0, 0, 0 }	,  			new float[] { 0, 0, 0, 0 }, 				false	), // NONE must be first!

	
	// Alli 5
	
	TAN (new float[] {0.82353f, 0.78824f, 0.6f, 1f}, new float[] {1, 1, 1, 1}, true),
	GREEN (new float[] {0.23137f, 0.45882f, 0.11765f, 1f}, new float[] {1, 1, 1, 1}, true),
	BLUE (new float[] {0.05098f, 0.3451f, 0.65098f, 1f}, new float[] {1, 1, 1, 1}, true),
	YELLOW (new float[] {1f, 0.6f, 0f, 1f}, new float[] {1, 1, 1, 1}, true),
	RED (new float[] {0.68235f, 0f, 0f, 1f}, new float[] {1, 1, 1, 1}, true),
	PLUM (new float[] {0.27843f, 0.0902f, 0.12549f, 1f}, new float[] {1, 1, 1, 1}, true),
	
	// Original picks
	/*
	RED			(new float[] { 1f, 0, 0, 1 }, 			new float[] { 1, 1, 1, 1 }, 			true	), 
	GREEN		(new float[] { 0, 1f, 0, 1 }, 			new float[] { 1, 1, 1, 1 }, 				true	),  
	BLUE		(new float[] { 0, 0, 1f, 1 }, 			new float[] { 1, 1, 1, 1 }, 				true	), 
	ORANGE      (new float[] { 0.8f, 0.5f, 0f, 1 }, 	new float[] { 1, 1, 1, 1 }, 				true	),
	PURPLE	 	(new float[] { 0.78f, 0.08f, 0.52f, 1 }, new float[] { 1, 1, 1, 1 }, 			false	),
	DARKGREY	(new float[] { 0.2f, 0.2f, 0.2f, 1 }, 	new float[] { 1, 1, 1, 1 }, 				true	), 
	*/
	
	// Logo
	
	LOGO_TAN 	(new float[] {0.91373f, 0.88235f, 0.72549f, 1f},	new float[] {1, 1, 1, 1},	false),
	
	LOGO_BLUE 	(new float[] {0.40784f, 0.61176f, 0.82745f, 1f},	new float[] {1, 1, 1, 1},	false),
	LOGO_GREEN 	(new float[] {0.52941f, 0.72941f, 0.43137f, 1f},	new float[] {1, 1, 1, 1},	false),
	LOGO_RED	(new float[] {0.84314f, 0.38039f, 0.38039f, 1f},	new float[] {1, 1, 1, 1},	false),
	LOGO_YELLOW	(new float[] {1f, 0.78039f, 0.45098f, 1f},			new float[] {1, 1, 1, 1},	false),
	
	// Continue!
	
	WHITE		(new float[] { 1f, 1f, 1f, 1 }, 		new float[] { 1, 1, 1, 1 }, 				false	), 

	RED_LIGHT	(new float[] { 0.65f, 0.16f, 0.16f, 1 }, new float[] { 1, 1, 1, 1 }, 				false	), 
	
	PITCHBLACK	(new float[] { 0f, 0f, 0f, 1 }, 		new float[] { 0, 0, 0, 1 }, 				false	),
	BLACK		(new float[] { 0f, 0f, 0f, 1 }, 		new float[] { 1, 1, 1, 1 }, 				false	), 
	BLACKGREY	(new float[] { 0.05f, 0.05f, 0.05f, 1 }, new float[] { 1, 1, 1, 1 }, 				false	), 

	DARKGREY	(new float[] { 0.2f, 0.2f, 0.2f, 1 }, 	new float[] { 1, 1, 1, 1 }, 				false	), 
	GREY		(new float[] { 0.4f, 0.4f, 0.4f, 1 }, 	new float[] { 1, 1, 1, 1 }, 				false	),
 
	GHOSTWHITE	(new float[] { 1f, 1f, 1f, 0.5f }, 		new float[] { 0.5f, 0.5f, 0.5f, 0.5f }, 	false	), 
	GHOSTBLACK	(new float[] { 0f, 0f, 0f, 0.5f }, 		new float[] { 0.8f, 0.8f, 0.8f, 0.8f }, 	false	), 
	GHOSTDARKBLACK	(new float[] { 0f, 0f, 0f, 0.8f }, 	new float[] { 0.8f, 0.8f, 0.8f, 0.8f }, 	false	), 
	
	TRANSPARENT	(new float[] { 0f, 0f, 0f, 0f }, 		new float[] { 0f, 0f, 0f, 0f }, 	false	), 
	
	// Backgrounds
	BACKGROUND_DEFAULT		(Color.BLACK.ambient(),							Color.BLACK.diffuse(),			false	), // DEFAULT must be last!
	BACKGROUND_RED			(new float[] {0.05f, 0.0f, 0.0f, 1f }, 			new float[] { 1, 1, 1, 1 }, 	false	), 
	BACKGROUND_LIGHT_RED	(new float[] {0.2f, 0.0f, 0.0f, 1f }, 			new float[] { 1, 1, 1, 1 }, 	false	), 
	
	// Default Color
	DEFAULT		(Color.GREY.ambient(),					Color.GREY.diffuse(),						false	), // DEFAULT must be last!
	
	; 
	
	private static final Random randObjPool = new Random( System.currentTimeMillis() );
	
	//private static final String TAG = "Color";
	private final float[] ambient;
	private final float[] diffuse;
	public final boolean isUsable;
	
	Color(float[] matAmbient, float[] matDiffuse, boolean enabled) 
	{
		
		//Log.d(TAG,"Constructing Color...");
		this.ambient = matAmbient;
		this.diffuse = matDiffuse;
		this.isUsable = enabled;
	}
	
	public float[] ambient() { return ambient; }
	public float[] diffuse() { return diffuse; }

	
	private static Color getRandomColor() 
	{
		//Log.d(TAG,"gettingRandomNumberInRange...");
		int minPick = 1; // Don't Include "NONE" color
		int maxPick = Color.values().length - 2; // Don't include "DEFAULT" color
		return Color.values()[minPick + randObjPool.nextInt(maxPick) ];
	}

	public static Color pickColorExcept(Color exceptedColor) {
		//Log.d(TAG,"PickingColor(Except)...");
		
		Color pick = getRandomColor();
		
		//keep picking!
		while (pick.isUsable == false || pick == exceptedColor)
		{
			pick = getRandomColor();
		}
		
		//Log.d(TAG,"   Except="+String.valueOf(exceptedColor)+ "   Pick=" +String.valueOf(pick) );
		return pick;
	}

	public static Color pickColor() {
		//Log.d(TAG,"pickingColor...");

		Color pick = getRandomColor();
		
		//keep picking!
		while (pick.isUsable == false)
		{
			pick = getRandomColor();
		}
		
		//Log.d(TAG,"   Pick=" +String.valueOf(pick) );
		return pick;
	}
	
	
	
	public static Color pickLogoColor() {
		//Log.d(TAG,"pickingColor...");

		int minPick = LOGO_BLUE.ordinal();
		int maxPick = LOGO_YELLOW.ordinal(); 
		return Color.values()[minPick + randObjPool.nextInt(maxPick - minPick + 1) ];

	}
	
	public static Color getNextLogoColor(Color c) {
		//Log.d(TAG,"pickingColor...");

		if (c.ordinal() >= LOGO_YELLOW.ordinal())
			return LOGO_BLUE;
		else
			return Color.values()[ c.ordinal() + 1 ];

	}
	
	
}
