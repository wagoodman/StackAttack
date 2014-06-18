package com.wagoodman.stackattack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public final class BaseConverterUtil {

    private static final String baseDigits = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Random randObjPool = new Random();
    
    
    public static String toBase62( int decimalNumber ) {
        return fromDecimalToOtherBase( 62, decimalNumber );
    }

    public static String toBase36( int decimalNumber ) {
        return fromDecimalToOtherBase( 36, decimalNumber );
    }

    public static String toBase16( int decimalNumber ) {
        return fromDecimalToOtherBase( 16, decimalNumber );
    }

    public static String toBase8( int decimalNumber ) {
        return fromDecimalToOtherBase( 8, decimalNumber );
    }

    public static String toBase2( int decimalNumber ) {
        return fromDecimalToOtherBase( 2, decimalNumber );
    }

    public static int fromBase62( String base62Number ) {
        return fromOtherBaseToDecimal( 62, base62Number );
    }

    public static int fromBase36( String base36Number ) {
        return fromOtherBaseToDecimal( 36, base36Number );
    }

    public static int fromBase16( String base16Number ) {
        return fromOtherBaseToDecimal( 16, base16Number );
    }

    public static int fromBase8( String base8Number ) {
        return fromOtherBaseToDecimal( 8, base8Number );
    }

    public static int fromBase2( String base2Number ) {
        return fromOtherBaseToDecimal( 2, base2Number );
    }

    private static String fromDecimalToOtherBase ( int base, int decimalNumber ) {
        String tempVal = decimalNumber == 0 ? "0" : "";
        int mod = 0;

        while( decimalNumber != 0 ) {
            mod = decimalNumber % base;
            tempVal = baseDigits.substring( mod, mod + 1 ) + tempVal;
            decimalNumber = decimalNumber / base;
        }

        return tempVal;
    }

    private static int fromOtherBaseToDecimal( int base, String number ) {
        int iterator = number.length();
        int returnValue = 0;
        int multiplier = 1;

        while( iterator > 0 ) {
            returnValue = returnValue + ( baseDigits.indexOf( number.substring( iterator - 1, iterator ) ) * multiplier );
            multiplier = multiplier * base;
            --iterator;
        }
        return returnValue;
    }
    
	//Used for a short unique ID
	public static String Base62Random()  
	{  
	    return BaseConverterUtil.toBase62( randObjPool.nextInt(Integer.MAX_VALUE) );  
	}  
	
	public static String lookupIntToString(int number) {
	  char[] ls = baseDigits.toCharArray();
	  String r = "";
	  while(true) {
	    r = ls[number % 26] + r;
	    if(number < 26) {
	      break;
	    }
	    number /= 26;
	  }
	  return r;
	}
	
	public static int tolkenizeStringToInt(String str) {
	  char[] ls = baseDigits.toCharArray();
	  Map<Character, Integer> m = new HashMap<Character, Integer>();
	  int j = 1;
	  for(char c: ls) {
	    m.put(c, j++);
	  }
	  int i = 0;
	  int mul = 1;
	  for(char c: new StringBuffer(str).reverse().toString().toCharArray()) {
	    i += m.get(c) * mul;
	    mul *= ls.length;
	  }
	  return i;
	}

}
