package com.wagoodman.stackattack;

//Queue interface

public interface Queue<Type>
{
	//Insert a new item into the queue.
	void enqueue(Type x);

	//Get the least recently inserted item in the queue.
	//Does not alter the queue.
	Type peek();

	//Return and remove the least recently inserted item
	//from the queue.
	Type dequeue();

	//Test if the queue is logically empty.
	boolean isEmpty();

	//Make the queue logically empty.
	void makeEmpty();
}