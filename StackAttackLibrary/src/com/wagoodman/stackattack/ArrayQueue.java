package com.wagoodman.stackattack;

//ArrayQueue class

public class ArrayQueue<Type> implements Queue<Type>
{

	private Type[] theArray;
	private int currentSize;
	private int front;
	private int back;

	private static final int DEFAULT_CAPACITY = 10;
	

	public ArrayQueue()
	{
		theArray = (Type[]) new Object[DEFAULT_CAPACITY];
		makeEmpty();
	}

	
	// Test if the queue is logically empty.
	public boolean isEmpty()
	{
		return currentSize == 0;
	}

	// Make the queue logically empty.
	public void makeEmpty()
	{
		currentSize = 0;
		front = 0;
		back = -1;
	}

	// Return and remove the least recently inserted item
	// from the queue.  Returns null if the queue is empty.
	public Type dequeue()
	{
		if (isEmpty()) return null;
		currentSize--;

		Type returnValue = theArray[front];
		front = increment(front);
		return returnValue;
	}


	// Get the least recently inserted item in the queue.
	// Does not alter the queue. Returns null if the queue is empty.
	public Type peek()
	{
		if (isEmpty()) return null;
		return theArray[front];
	}


	// Insert a new item into the queue.
	public void enqueue(Type x)
	{
		if (currentSize == theArray.length) doubleQueue();
		back = increment(back);
		theArray[back] = x;
		currentSize++;
	}


	// Internal method to increment with wraparound.
	private int increment(int x)
	{
		if (++x == theArray.length) x = 0;
		return x;
	}

	// Internal method to expand theArray.
	private void doubleQueue()
	{
		Type[] newArray;

		newArray = (Type[])new Object[theArray.length * 2];

		// Copy elements that are logically in the queue
		for (int i = 0; i < currentSize; i++, front = increment(front))
			newArray[i] = theArray[front];

		theArray = newArray;
		front = 0;
		back = currentSize - 1;
	}


}
