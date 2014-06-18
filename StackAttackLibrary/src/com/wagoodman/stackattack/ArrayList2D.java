package com.wagoodman.stackattack;

import java.util.ArrayList;

public class ArrayList2D<Type> extends ArrayList<ArrayList<Type>>
{
 
	private static final long serialVersionUID = 3640311489856127530L;

	public ArrayList2D()
	{
		super();
	}
	
	public ArrayList2D(int rows, int cols)
	{
		ensureCapacity(rows, cols);
	}
 

	/**
	 * Ensures that the given row has at least the given capacity. Note that
	 * this method will also ensure that getNumRows() >= row
	 * 
	 * @param row
	 * @param num
	 */
	public void ensureCapacity(int row, int num)
	{
		ensureCapacity(row);
		while (row >= getNumRows())
		{
			add(new ArrayList<Type>());
		}
		get(row).ensureCapacity(num);
	}
 
	/**
	 * Adds an item at the end of the specified row. This will guarantee that at least row rows exist.
	 */
	public void add(Type data, int row)
	{
		ensureCapacity(row);
		while(row >= getNumRows())
		{
			add(new ArrayList<Type>());
		}
		get(row).add(data);
	}
 
	public Type get(int row, int col)
	{
		return get(row).get(col);
	}
 
	public void set(Type data, int row, int col)
	{
		get(row).set(col,data);
	}
 
	public void remove(int row, int col)
	{
		get(row).remove(col);
	}

	public Boolean containsDataWithin(Type data)
	{
		for (int i = 0; i < size(); i++)
		{
			if (get(i).contains(data))
			{
				return true;
			}
		}
		
		return false;
	}
	
 
	public int getNumRows()
	{
		return size();
	}
 
	public int getNumCols(int row)
	{
		return get(row).size();
	}
}
