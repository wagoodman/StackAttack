package com.wagoodman.stackattack;

import java.util.ArrayList;



public class Coord<Type> extends Object{
	private final static int ROW = 0;
	private final static int COL = 1;
	private final static int X = 0;
	private final static int Y = 1;
	private final static int Z = 2;
	
    private ArrayList<Type> coord= new ArrayList<Type>();

    public Coord(Type rx, Type cy) {
        super();
        coord.add(ROW, rx);
        coord.add(COL, cy);
    }

    public Coord(Type x, Type y, Type z) {
        super();
        coord.add(X, x);
        coord.add(Y, y);
        coord.add(Z, z);
    }
    
    public ArrayList<Type> getRawCoord(){ return coord; }
    public Type getRow()	{ return coord.get(ROW); 	}
    public Type getCol()	{ return coord.get(COL);	}
    public Type getX()		{ return coord.get(X); 		}
    public Type getY()		{ return coord.get(Y); 		}
    public Type getZ()		{ return coord.get(Z); 		}
    
    public void setRow(Type row)	{ coord.set(ROW, row); 	}
    public void setCol(Type col)	{ coord.set(COL, col);	}
    public void setX(Type x)		{ coord.set(X, x); 		}
    public void setY(Type y)		{ coord.set(Y, y); 		}
    public void setZ(Type z)		{ coord.set(Z, z); 		}
    
    /*
    public float[] getRowCol()	{ return new float[] {(Float) coord.get(ROW), (Float) coord.get(COL)}; }
    public float[] getXYZ()		{ return new float[] {(Float) coord.get(X), (Float) coord.get(Y), (Float) coord.get(Z)}; }
    */
    
    @Override
    public boolean equals(Object other) 
    {
    	if (other instanceof Coord) 
        { 
        	return getRawCoord().equals(((Coord<?>) other).getRawCoord()); 
        }

        return false;
    }
    
    @Override
    public int hashCode()
    {
	    return getRawCoord().hashCode();
    }
    
    public String toString()
    { 
    	if (coord.size() == 2)
    		return "(" + coord.get(ROW) + ", " + coord.get(COL) + ")"; 
    	
    	return "[" + coord.get(X) + ", " + coord.get(Y)+ ", " + coord.get(Z) + "]";
    }

    public Coord<Type> clone()
    {
    	if (coord.size() == 2)
    		return new Coord<Type>(getRow(), getCol());
    	
    	return new Coord<Type>(getX(), getY(), getZ());
    }
    
    @SuppressWarnings("unchecked")
	public Type[] toArray()
    {
    	return (Type[]) coord.toArray();
    }
    
}
