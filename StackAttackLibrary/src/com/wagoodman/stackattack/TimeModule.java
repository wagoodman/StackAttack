package com.wagoodman.stackattack;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;

public class TimeModule 
{
	private long absoluteStartTime;
	private long relativeStartTime;
	private long pauseStartTime;
	private long pauseAtStartElapsedTime;
	public boolean isPaused = false;
	public boolean isStarted = false;
	
	private HashSet<String> mLocks = new HashSet<String>();
	
	TimeModule()
	{

	}

	TimeModule(boolean start)
	{
		if (start == true)
			init();
	}
	
	private void init()
	{
		absoluteStartTime = System.currentTimeMillis();
		relativeStartTime = System.currentTimeMillis();
		pauseStartTime = 0;
		isPaused = false;
		isStarted = false;
		
		// break all locks
		mLocks.clear();
	}
	
	public synchronized void start()
	{
		init();
		isStarted = true;
	}
	
	public synchronized void restart()
	{
		init();
		isStarted = true;
	}
	
	public synchronized void reset()
	{
		init();
		pause();
	}
	
	public synchronized void toggle(String lockId)
	{
		if (isPaused)
			resume(lockId);
		else
			pause(lockId);
	}
	
	public synchronized void toggle()
	{
		if (isPaused)
			resume();
		else
			pause();
	}
	
	public synchronized void pause(String lockId)
	{
		attachLock(lockId);
		pause();
	}
	
	public synchronized boolean pause()
	{
		if (isStarted)
		{
			if (isPaused)
				return false;
			
			isPaused = true;
			pauseStartTime = System.currentTimeMillis();
			pauseAtStartElapsedTime = pauseStartTime - relativeStartTime;
			return isPaused;
		}
		
		return false;
	}
	
	public synchronized boolean resume(String lockId)
	{
		releaseLock(lockId);
		return resume();
	}
	
	public synchronized boolean resume()
	{
		if (isStarted)
		{
			if (!isPaused)
				return false;
			
			if (isLocked())
				return false;
			
			relativeStartTime += System.currentTimeMillis() - pauseStartTime;
			pauseStartTime = 0;
			pauseAtStartElapsedTime = 0;
			isPaused = false;
			
			return !isPaused;
		}
		
		return false;
	}
	
	public synchronized boolean startOrResume(String lockId)
	{
		releaseLock(lockId);
		return startOrResume();
	}
	
	public synchronized boolean startOrResume()
	{
		if (isStarted)
		{
			if (!isPaused)
				return false;
			
			relativeStartTime += System.currentTimeMillis() - pauseStartTime;
			pauseStartTime = 0;
			pauseAtStartElapsedTime = 0;
			isPaused = false;
			return !isPaused;
		}
		else
		{
			start();
			pauseStartTime = 0;
			pauseAtStartElapsedTime = 0;
			isPaused = false;
			return !isPaused;
		}

	}

	// increase timer, act as if it has started later (add more time to a count down clock)
	public synchronized void addSeconds(int seconds)
	{
		if (isStarted)
		{
			relativeStartTime += seconds;
			
			if (isPaused)
			{
				pauseAtStartElapsedTime += seconds;
			}

		}
	}
	
	// LOCKERS
	
	public synchronized void attachLock(String lockId)
	{
		mLocks.add(lockId);
	}
	
	public synchronized void clearAllLocks()
	{
		mLocks.clear();
	}
	
	public synchronized Boolean releaseLock(String lockId)
	{
		return mLocks.remove(lockId);
	}
	
	public synchronized Boolean isLocked()
	{
		return mLocks.size() != 0;
	}
	
	// GETTERS
	
	
	public synchronized boolean hasElapsed(int mills)
	{
		return hasElapsed( System.currentTimeMillis(), mills );
	}
	
	public synchronized boolean hasElapsed(long now, int mills)
	{
		if (isStarted)
		{
			return getElapsedTime(now) >= mills;
		}
		
		return false;
	}
	
	public synchronized long getElapsedTime(long now)
	{
		if (isStarted)
		{
			if (isPaused)
				return pauseAtStartElapsedTime;
			else
				return now - relativeStartTime;
		}
		
		return 0;
	}
	/*
	public synchronized long getRealElapsedTime(long now)
	{
		return now - absoluteStartTime;
	}
	*/
	public synchronized long getElapsedTime()
	{
		return getElapsedTime(System.currentTimeMillis());
	}
	
	public synchronized String getFormattedElapsedTime()
	{
		return formatInterval(getElapsedTime(System.currentTimeMillis()));
	}
	/*
	public synchronized long getRealElapsedTime()
	{
		return getRealElapsedTime(System.currentTimeMillis());
	}
	*/
	public static String formatInterval(final long l)
    {
		final long hr = Math.max(0, TimeUnit.MILLISECONDS.toHours(l));
        final long min = Math.max(0, TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr)));
        final long sec = Math.max(0, TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)));
        return String.format("%02d:%02d:%02d", hr, min, sec);
    }
	
	public static String formatFineInterval(final long l)
    {
		final long hr  = Math.max(0, TimeUnit.MILLISECONDS.toHours(l));
        final long min = Math.max(0, TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr)));
        final long sec = Math.max(0, TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)));
        final long mil = Math.max(0, TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec)));
        return String.format("%d.%03d", sec, mil);
    }
	
}
