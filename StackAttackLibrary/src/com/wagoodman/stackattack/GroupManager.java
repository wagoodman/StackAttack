package com.wagoodman.stackattack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import com.wagoodman.stackattack.Color;
import com.wagoodman.stackattack.MainActivity;


import android.content.Context;


public class GroupManager
{
	private static final String TAG = "GrpMGR";
	private static final Boolean debug = false;
	private final MainActivity game;

	// GroupId : Set<Member Ids>
	private HashMap<String, HashSet<String> > mGroups = new HashMap<String, HashSet<String> >();
	
	// GroupId : Set<Member Cols>
	private HashMap<String, HashSet<Integer> > mGroupCols = new HashMap<String, HashSet<Integer> >();
	
	// GroupTypes
	private HashMap<String, GarbageType > mGroupTypes = new HashMap<String, GarbageType >();
	
	// Groups that are melted and should be stripped
	private TreeMap< Long , HashSet<String> > mDeadGroups = new TreeMap< Long , HashSet<String> >();
	
	
	public Boolean isAnyGroupMelting()
	{
		return mDeadGroups.size() != 0;
	}
	
	public void markGroupAsDeadAt(String groupId, long timeOfDeath)
	{
		if (!mDeadGroups.containsKey(timeOfDeath))
		{
			mDeadGroups.put(timeOfDeath, new HashSet<String>() );
		}
		mDeadGroups.get(timeOfDeath).add(groupId);
	}
	
	public HashSet<String> reapDeadGroups(long now)
	{
		
		if (mDeadGroups.size() > 0)
			if (mDeadGroups.firstKey() <= now)
				return mDeadGroups.remove( mDeadGroups.firstKey() );
		
		return new HashSet<String>();
	}
	
	public Boolean hasReapableGroups(long now)
	{
		if (mDeadGroups.size() > 0)
			if (mDeadGroups.firstKey() <= now)
				return true;
		
		return false;
	}
	
	
	public GroupManager(Context context)
	{
		// get the game object from context
		game = (MainActivity) (context);
	}

	// used for unit testing
	public GroupManager()
	{
		super();
		game = null;
	}
	
	/*
	@SuppressWarnings("unused")
	private void DEBUG(String logString)
	{
		if (debug == true) Log.d(TAG, logString);
	}

	private void ERROR(String logString)
	{
		if (debug == true) Log.e(TAG, logString);
	}
	*/
	
	
	/**
	 * Add single member to the given group. If the group does not exist it will be added as well.
	 * 
	 * @param groupId		a string of the group id that the member will be added to
	 * @param memberId		a string representing the member to be added
	 * @return
	 */
	public Boolean add(String groupId, String memberId, Integer col)
	{
		return add(groupId, memberId, col, GarbageType.NORMAL);
	}
	
	public Boolean add(String groupId, String memberId, Integer col, GarbageType groupType)
	{
		try
		{

			if (!mGroups.containsKey(groupId))
			{
				mGroups.put(groupId, new HashSet<String>() );
				mGroupCols.put(groupId, new HashSet<Integer>() );
			}

			mGroups.get(groupId).add(memberId);
			mGroupCols.get(groupId).add(col);
			mGroupTypes.put(groupId, groupType);

			return true;

		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return false;
	}

	/**
	 * Add a set of members to the given group. If the group does not exist it will be added as well.
	 * 
	 * @param groupId		a string of the group id that the members will be added to
	 * @param memberIds		a set of strings representing the members to be added
	 * @return
	 */
	public Boolean add(String groupId, HashSet<String> memberIds, HashSet<Integer> memberCols)
	{
		try
		{
			if (mGroups.containsKey(groupId))
			{
				memberIds   = (HashSet<String>) (unionString( mGroups.get(groupId), memberIds ));
				memberCols = (HashSet<Integer>) (unionInteger( mGroupCols.get(groupId), memberCols ));
			}

			mGroups.put(groupId, memberIds);
			mGroupCols.put(groupId, memberCols);

			return true;

		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return false;
	}
	
	/**
	 * Add a list of members to the given group. If the group does not exist it will be added as well.
	 * 
	 * @param groupId		a string of the group id that the members will be added to
	 * @param memberIds		a list of strings representing the members to be added
	 * @return
	 */	
	public Boolean add(String groupId, ArrayList<String> memberIds, ArrayList<Integer> memberCols)
	{
		try
		{
			return add(groupId, new HashSet<String>(memberIds), new HashSet<Integer>(memberCols) );
		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return false;
	}
	
	
	public HashSet<String> keys()
	{
		return new HashSet<String>(mGroups.keySet());
	}
	
	public Collection<HashSet<String>> memberValues()
	{
		return mGroups.values();
	}
	
	public Collection<HashSet<Integer>> colValues()
	{
		return mGroupCols.values();
	}
	
	
	/**
	 * Returns weather or not the given group exists.
	 * 
	 * @param groupId
	 * @return
	 */
	public Boolean hasGroup(String groupId)
	{
		return mGroups.containsKey(groupId);
	}
	

	/**
	 * Returns weather or not the given block id is a member of any groups.
	 * 
	 * @param blockId
	 * @return
	 */
	public Boolean isInAnyGroup(String blockId)
	{
		for (String groupId : mGroups.keySet()) 
		{
			if ( mGroups.get(groupId).contains(blockId) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns weather or not the given block id is a member of any groups.
	 * If so, the groupId is returned, otherwise null is returned
	 * 
	 * @param blockId
	 * @return
	 */
	public String getGroup(String blockId)
	{
		if (blockId == null)
			return null;
		
		for (String groupId : mGroups.keySet()) 
		{
			if ( mGroups.get(groupId).contains(blockId) )
			{
				return groupId;
			}
		}
		
		return null;
	}
	
	
	/**
	 * Returns weather or not the given block id is a member of the given group.
	 * 
	 * @param blockId
	 * @return
	 */
	public Boolean isInGroup(String groupId, String blockId)
	{
		 return mGroups.get(groupId).contains(blockId);
	}
	
	
	/**
	 * Returns a list of group members from the given group
	 * 
	 * @param groupId		a string of the group id for which to get all members of
	 * @return
	 */
	public ArrayList<String> getGroupMembers(String groupId)
	{
		try
		{
			if (mGroups.containsKey(groupId))
			{
				return new ArrayList<String>( mGroups.get(groupId) );
			}
		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return null;
	}
	
	
	
	
	
	
	
	
	public int getMiddleCol(String groupId)
	{
		try
		{
			if (mGroups.containsKey(groupId))
			{
				int maxCol = -1;
				for (Integer col : mGroupCols.get(groupId) )
					maxCol = Math.max(maxCol, col);
				
				return maxCol - (getGroupMemberCols(groupId).size()-1)/2;
			}
		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return -1;
	}
	
	
	/**
	 * Returns a list of group member columns from the given group
	 * 
	 * @param groupId		a string of the group id for which to get all members of
	 * @return
	 */
	public ArrayList<Integer> getGroupMemberCols(String groupId)
	{
		try
		{
			if (mGroups.containsKey(groupId))
			{
				return new ArrayList<Integer>( mGroupCols.get(groupId) );
			}
		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return null;
	}
	
	
	public HashSet<Coord<Integer>> getCoords(String groupId, int row)
	{
		HashSet<Coord<Integer>> ret = new HashSet<Coord<Integer>>();
		for (Integer col : getGroupMemberCols(groupId))
		{
			ret.add(new Coord<Integer>(row, col) );
		}
		return ret;
	}
	
	
	/**
	 * Returns a list of group Ids that have the given ID as a member
	 * 
	 * @param memberId		a string of the member id to search for
	 * @return
	 */
	public ArrayList<String> getGroupsWithMember(String memberId)
	{
		ArrayList<String> ret = new ArrayList<String>();
		
		try
		{
			for (String groupId : mGroups.keySet()) 
			{
				if ( mGroups.get(groupId).contains(memberId) )
				{
					ret.add(groupId);
				}
			}
			
			return ret;
		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return null;
	}
	
	
	

	public Color getColor(String groupId)
	{
		return getGroupType(groupId).getColor();
	}
	
	
	public void setGroupType(String groupId, GarbageType type)
	{
		mGroupTypes.put(groupId, type);
	}
	
	
	public void pickGroupType(String groupId)
	{
		mGroupTypes.put(groupId, GarbageType.pickGarbageType() );
	}
	
	
	public GarbageType getGroupType(String groupId)
	{
		return mGroupTypes.get(groupId);
	}
	
	
	
	/**
	 * Delete the groupId from the manifest.
	 * 
	 * @param groupId
	 * @return
	 */
	public Boolean removeGroup(String groupId)
	{
		try
		{
			if (mGroups.containsKey(groupId))
			{
				mGroups.remove(groupId);
				mGroupCols.remove(groupId);
				mGroupTypes.remove(groupId);
				return true;
			}
		}
		catch (Exception e)
		{
			//ERROR(e.toString());
		}

		return false;
	}
	
	
	// Supporting Java Methods
	
	public String toString()
	{
		String ret = "";
		
		for (String groupId : mGroups.keySet()) 
			 ret += groupId + ":" + mGroups.get(groupId).toString() + "\n";
		
		return ret;
	}

	// Supporting Set Operators
	
	private HashSet<String> unionString(HashSet<String> x, HashSet<String> y)
	{
		HashSet<String> t = new HashSet<String>(x);
		t.addAll(y);
		return t;
	}
	
	private HashSet<Integer> unionInteger(HashSet<Integer> x, HashSet<Integer> y)
	{
		HashSet<Integer> t = new HashSet<Integer>(x);
		t.addAll(y);
		return t;
	}
	
	@SuppressWarnings("unused")
	private HashSet<String> intersection(HashSet<String> x, HashSet<String> y)
	{
		HashSet<String> t = new HashSet<String>(x);
		t.retainAll(y);
		return t;
	}

	// the elements in x but not in y: "set-theoretic difference"
	@SuppressWarnings("unused")
	private HashSet<String> difference(HashSet<String> x, HashSet<String> y)
	{
		HashSet<String> t = new HashSet<String>();
		for (String o : x)
			if (!y.contains(o)) t.add(o);
		return t;
	}

}
