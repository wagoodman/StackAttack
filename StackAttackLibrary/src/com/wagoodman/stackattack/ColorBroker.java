package com.wagoodman.stackattack;

public class ColorBroker extends ArrayQueue<ColorState>
{

	public	Color 	mCurrentColor;
	public	float[] mCurrentColorAmbient;
	public	float[] mCurrentColorDiffuse;
	
	public ColorBroker()
	{
		setColor(Color.DEFAULT);
	}
	
	public ColorBroker(Color color)
	{
		setColor(color);
	}
	
	public void setColor(Color color)
	{
		mCurrentColor = color;
		// I dare you to remove the '.clone()' from these lines.... go ahead, fuckin do it!
		mCurrentColorAmbient = color.ambient().clone();
		mCurrentColorDiffuse = color.diffuse().clone();
	}
	
	public void cancelColorTransitionsAndSet(Color newColor)
	{
		makeEmpty();
		try
		{
			onDeck.stop();
		}
		catch (Exception e)
		{
			
		}
		
		setColor(newColor);
	}
	
	private ColorState onDeck;
	
	public void processColorElements(long now)
	{
		onDeck = peek();
		
		// dont process null indexes
		if (onDeck != null)
		{
		
			// check if complete, if so remove it
			if (onDeck.isCompleted)
			{
				// set color
				setColor(dequeue().mEndColor);
			}
			else 
			{
				
				// start animation if needed
				if (!onDeck.isTransforming)
				{
					onDeck.start(now);
				}
					
				// apply color transform
				
				// ambient; cycle R,G,B,A
				for (int idx=0; idx < 4; idx++)
					mCurrentColorAmbient[idx] = 
					(float) MotionEquation.applyFinite(
							TransformType.COLOR,
							onDeck.mEquation[idx], 
							onDeck.getRunningTime(now, idx), 
							onDeck.mDuration[idx], 
							onDeck.mStartColorAmbient[idx], 
							onDeck.mEndColorAmbient[idx]
								);
				
				// diffuse; cycle R,G,B,A
				for (int idx=0; idx < 4; idx++)
					mCurrentColorDiffuse[idx] = 
					(float) MotionEquation.applyFinite(
							TransformType.COLOR,
							onDeck.mEquation[idx], 
							onDeck.getRunningTime(now, idx), 
							onDeck.mDuration[idx], 
							onDeck.mStartColorDiffuse[idx], 
							onDeck.mEndColorDiffuse[idx]
								);
			}
		}	
	
		
		
		
	}


}
