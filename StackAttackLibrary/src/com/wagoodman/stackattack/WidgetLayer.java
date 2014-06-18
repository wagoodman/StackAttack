package com.wagoodman.stackattack;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.microedition.khronos.opengles.GL10;

import com.wagoodman.stackattack.Animation;
import com.wagoodman.stackattack.Coord;
import com.wagoodman.stackattack.GLShape;
import com.wagoodman.stackattack.MotionEquation;
import com.wagoodman.stackattack.PxBase;
import com.wagoodman.stackattack.PxImage;

import android.content.Context;


public class WidgetLayer
{
	private final Context mContext;
	private final MainActivity game;

	private ConcurrentHashMap<String, GLShape>	  mGLItems = new ConcurrentHashMap<String, GLShape>();
	private ConcurrentHashMap<String, Long>	mGLDeleteTimes = new ConcurrentHashMap<String, Long>();
	
	private ConcurrentHashMap<String, PxBase>	  mPxItems = new ConcurrentHashMap<String, PxBase>();
	private ConcurrentHashMap<String, Long>	mPxDeleteTimes = new ConcurrentHashMap<String, Long>();
	
	private ConcurrentHashMap<String, Long>	mOutroTimes = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<String, Integer>	mOutroDurations = new ConcurrentHashMap<String, Integer>();
	private ConcurrentHashMap<String, Coord<Integer>> mOutroCoords = new ConcurrentHashMap<String, Coord<Integer>>();
	
	public Boolean mInteractable = true;

	public WidgetLayer(Context context)
	{
		mContext = context;
		game = (MainActivity) mContext;
	}
	
	
	public void addItem(String key, PxBase item, Integer dur, Integer delay, Integer ttlDur)
	{
		if (!mPxItems.containsKey(key))
		{
			// add the item
			mPxItems.put(key, item);
			item.fadeIn(MotionEquation.LOGISTIC, dur, delay, true);
			
			if (ttlDur != null)
			{
				mOutroTimes.put(key, System.currentTimeMillis() + ttlDur + dur + delay);
				mOutroDurations.put(key, dur);
			}
		}
	}
	
	
	public void addItem(String key, Integer itemResId, int width, int height, float scaleRatio, float xRelOff, float yRelOff, float initX, float initY , float cropX, float cropY, Boolean lockX, Boolean lockY, Boolean interactable, Boolean scaleOnInteraction, Boolean cropOnInteraction, Integer dur, Integer delay)
	{
		if (!mPxItems.containsKey(key))
		{
			// add the item
			mPxItems.put(key, new PxImage(mContext, itemResId, width, height, scaleRatio, xRelOff, yRelOff, initX, initY, cropX, cropY, lockX, lockY , interactable, scaleOnInteraction, cropOnInteraction) );
			mPxItems.get(key).fadeIn(MotionEquation.LOGISTIC, dur, delay, false);
		}
	}
	
	// intro
	public void addItem(String key, GLShape item, 
			Animation in_anim, Coord<Integer> in_fromRowCol, Coord<Integer> in_toRowCol, Integer in_duration, Integer in_delay, Orientation in_orient
			)
	{
		if (!mGLItems.containsKey(key))
		{
			// add the item
			item.mAnimation.queueAnimation(in_anim, in_fromRowCol, in_toRowCol, in_duration, in_delay, in_orient );
			mGLItems.put(key, item);
			
			// keep info for outro
			mOutroCoords.put(key, in_toRowCol);
		}
	}
	
	// outro
	public void addItem(String key, GLShape item, 
			Animation in_anim, Coord<Integer> in_fromRowCol, Coord<Integer> in_toRowCol, Integer in_duration, Integer in_delay, Orientation in_orient,
			Animation out_anim, Coord<Integer> out_fromRowCol, Coord<Integer> out_toRowCol, Integer out_duration, Integer out_delay, Orientation out_orient
			)
	{
		if (!mGLItems.containsKey(key))
		{
			// add the item
			item.mAnimation.queueAnimation(in_anim, in_fromRowCol, in_toRowCol, in_duration, in_delay, in_orient );
			item.mAnimation.queueAnimation(out_anim, out_fromRowCol, out_toRowCol, out_duration, out_delay, out_orient );
			mGLItems.put(key, item);
			mGLDeleteTimes.put(key, System.currentTimeMillis() + out_duration + out_delay);
			
			// keep info for outro
			mOutroCoords.put(key, in_toRowCol);
		}
	}
	
	
	public GLShape getGLItem(String key)
	{
		return mGLItems.get(key);
	}
	
	public PxBase getPxItem(String key)
	{
		return mPxItems.get(key);
	}
	
	public void clearItems()
	{
		mGLItems.clear();
		mGLDeleteTimes.clear();
		mPxItems.clear();
		mPxDeleteTimes.clear();
		mOutroCoords.clear();
		mOutroTimes.clear();
		mOutroDurations.clear();
	}
	
	public void outro(String key, Animation out_anim, Coord<Integer> out_fromRowCol, Coord<Integer> out_toRowCol, Integer out_duration, Integer out_delay, Orientation out_orient)
	{
		if (mGLItems.containsKey(key))
		{
			getGLItem(key).mAnimation.queueAnimation(out_anim, out_fromRowCol, out_toRowCol, out_duration, out_delay, out_orient );
			mGLDeleteTimes.put(key, System.currentTimeMillis() + out_duration + out_delay);
		}
		else if (mPxItems.containsKey(key))
		{
			getPxItem(key).fadeOut( MotionEquation.LOGISTIC, out_duration, out_delay , false);
			mPxDeleteTimes.put(key, System.currentTimeMillis() + out_duration + out_delay);
		}
	}
	
	public void outro(Animation out_anim, Integer out_duration, Integer out_delay, Orientation out_orient)
	{
		glOutro(out_anim, out_duration, out_delay, out_orient);
		pxOutro(out_duration, out_delay);
	}
	
	public void glOutro(Animation out_anim, Integer out_duration, Integer out_delay, Orientation out_orient)
	{
		// outro 3D objects
		for (Map.Entry<String, GLShape> entry : mGLItems.entrySet())
		{
			entry.getValue().mAnimation.queueAnimation(out_anim, mOutroCoords.get(entry.getKey()), mOutroCoords.get(entry.getKey()), out_duration, out_delay, out_orient );
			mGLDeleteTimes.put(entry.getKey(), System.currentTimeMillis() + out_duration + out_delay);
		}
	}
	
	public void pxOutro(Integer out_duration, Integer out_delay)
	{
		// outro 2D objects
		for (Map.Entry<String, PxBase> entry : mPxItems.entrySet())
		{
			entry.getValue().fadeOut( MotionEquation.LOGISTIC, out_duration, out_delay , false);
			mPxDeleteTimes.put(entry.getKey(), System.currentTimeMillis() + out_duration + out_delay);
		}
	}
	
	public void pxIntro(Integer out_duration, Integer out_delay)
	{
		// outro 2D objects
		for (Map.Entry<String, PxBase> entry : mPxItems.entrySet())
		{
			entry.getValue().fadeIn( MotionEquation.LOGISTIC, out_duration, out_delay , false);
		}
	}
	
	public Boolean pickup(int x, int y, int pixFontOffset, int digitCount)
	{
		//game.text = "PICKUP " +x+  ", " +y + "\n" + game.text;
		//game.textviewHandler.post(game.updateTextView);
		
		if (mInteractable)
		{
			//game.text = "PICKUP " +x+  ", " +y + "\n" ;
			for (PxBase item : mPxItems.values())
			{
				//game.text += "   " + item.interact(x, y, pixFontOffset) + "\n" ;

				if (item.pickup(x, y, pixFontOffset, digitCount))
					return true;
			}
			//game.textviewHandler.post(game.updateTextView);
		}
	
		return false;
	}
	
	public Boolean interact(float x, float y, int pixFontOffset, int digitCount)
	{
		
		//game.text = "DRAG " +x+  ", " +y + "\n" + game.text;
		//game.textviewHandler.post(game.updateTextView);
		
		if (mInteractable)
		{
			for (PxBase item : mPxItems.values())
				if (item.interact(x, y, pixFontOffset, digitCount))
					return true;
		}
	
		return false;
	}
	
	public void drop()
	{
		drop(0);
	}
	
	public void drop(int digitCount)
	{
		if (mInteractable)
		{
			for (PxBase item : mPxItems.values())
				item.drop(digitCount);
		}
	}
	
	/*
	
	
	*/
	/*
	public void hide()
	{
		for (GLMenuItem item : mItems)
			item.hide();
	}
	*/
	/*
	public int getOutroDuration()
	{
		return mOutroDuration;
	}
	*/
	
	
	public void update(long now, Boolean primaryThread, Boolean secondaryThread)
	{
				
		//game.text = "";
		for (GLShape item : mGLItems.values())
		{
			//game.text += item + "\n";
			try
			{
				item.update(now, primaryThread, secondaryThread);	
			}
			catch(Exception e)
			{
				// item may have been deleted
				//Log.e("WidgetLayer Update",e.toString());
			}
		}

		//game.text = "\n\n\n\n\n\n\n";
		for (Map.Entry<String, PxBase> entry : mPxItems.entrySet())
		{
			//game.text += item + "\n";
			try
			{
				mPxItems.get(entry.getKey()).update(now, primaryThread, secondaryThread);
			}
			catch(Exception e)
			{
				// item may have been deleted
				//Log.e("WidgetLayer Update",e.toString());
			}
		}
		
		//game.textviewHandler.post( game.updateTextView );
		
		// delete old items!
		if (secondaryThread)
		{
			for (Map.Entry<String, Long> entry : mGLDeleteTimes.entrySet()) 
			{
			    if (now >= entry.getValue())
			    {
			    	mGLDeleteTimes.remove(entry.getKey());
			    	mGLItems.remove(entry.getKey());
			    }
			    
			}
			
			for (Map.Entry<String, Long> entry : mOutroTimes.entrySet()) 
			{
			    if (now >= entry.getValue())
			    {
			    	
			    	mOutroTimes.remove(entry.getKey());
			    	if (mPxItems.containsKey(entry.getKey()))
			    		mPxItems.get(entry.getKey()).fadeOut(MotionEquation.LOGISTIC, mOutroDurations.get(entry.getKey()), 0, false);
			    	// GL not supported
			    	//if (mGLItems.containsKey(entry.getKey()))
			    	//	mGLItems.get(entry.getKey()).fadeOut(MotionEquation.LOGISTIC, mPxOutroDurations.get(entry.getKey()), 0, false);
			    	mPxDeleteTimes.put(entry.getKey(), now + mOutroDurations.get(entry.getKey()) );
			    	mOutroDurations.remove(entry.getKey());
			    }
			    
			}
			
			for (Map.Entry<String, Long> entry : mPxDeleteTimes.entrySet()) 
			{
			    if (now >= entry.getValue())
			    {
			    	mPxDeleteTimes.remove(entry.getKey());
			    	mPxItems.remove(entry.getKey());
			    }
			    
			}
			
		}
		
			
	}
	
	public void draw(GL10 gl, Boolean draw2D)
	{
		//game.text = "";
		
		// Draw 3D shapes
		for (GLShape item : mGLItems.values())
		{
			try
			{
				gl.glPushMatrix();
				//game.text += item + "\n";
				item.draw(gl);
				gl.glPopMatrix();
			}
			catch(Exception e)
			{
				//Log.e("StackAttack", "WidgetLayerDraw: "+e.toString());
			}
		}
		

		// draw 2D images
		if (draw2D)
		{
			if (!game.getIsGamePaused())
			{
				for (PxBase image : mPxItems.values())
				{
					image.draw(gl, 0);
				}
			}
		}

		
		//game.textviewHandler.post( game.updateTextView );
	}


	
}
