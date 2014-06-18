package com.wagoodman.stackattack;


public interface RollableItem 
{
	public String[] getAllTitles();
	
	public String getNext(String curValue);
	public int getNext(int curIdx);

	public String getPrev(String curValue);
	public int getPrev(int curIdx);
	
	public String getTitle();
	public int getIndex();
	
}
