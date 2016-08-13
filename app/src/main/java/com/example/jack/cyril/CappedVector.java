package com.example.jack.cyril;

import android.util.Log;

/**
 * Created by jack on 08/08/2016.
 */
public class CappedVector<T> {

    private int mSize = 0;
    private final int mMaxSize;
    private final Object[] mElements;
    private int mStart = 0;

    
    public static String testString( CappedVector<String> v ) {
        String str = "";
        for (int t=0;t<v.size();t++) {
            str += v.get(t);
        }
        return str;
    }

    private static void ass( Boolean b ) {
        if (!b) {
            throw new RuntimeException("Assert failed");
        }
    }
    
    public static void test() {
        CappedVector<String> v = new CappedVector<String>(10);
        ass( v.size() == 0 );
        ass( testString(v).equals("") );
        v.add("a");
        ass( v.size() == 1 );
        ass( testString(v).equals("a") );
        v.add("b");
        ass( v.size() == 2 );
        ass( testString(v).equals("ab") );
        v.add("c");
        ass( v.size() == 3 );
        ass( testString(v).equals("abc") );
        v.add("d");
        ass( v.size() == 4 );
        ass( testString(v).equals("abcd") );
        v.add("e");
        ass( v.size() == 5 );
        ass( testString(v).equals("abcde") );
        v.add("f");
        ass( v.size() == 6 );
        ass( testString(v).equals("abcdef") );
        v.add("g");
        ass( v.size() == 7 );
        ass( testString(v).equals("abcdefg") );
        v.add("h");
        ass( v.size() == 8 );
        ass( testString(v).equals("abcdefgh") );
        ass( v.get(0).equals("a"));
        ass( v.get(7).equals("h"));
        v.add("i");
        ass( v.size() == 9 );
        ass( testString(v).equals("abcdefghi") );
        ass( v.get(0).equals("a"));
        ass( v.get(8).equals("i"));
        v.add("j");
        ass( v.size() == 10 );
        ass( testString(v).equals("abcdefghij") );
        ass( v.get(0).equals("a"));
        ass( v.get(9).equals("j"));
        v.add("k");
        ass( v.size() == 10 );
        ass( v.get(0).equals("b"));
        ass( v.get(9).equals("k"));
        ass( testString(v).equals("bcdefghijk") );
        v.add("l");
        ass( v.size() == 10 );
        ass( v.get(0).equals("c"));
        ass( v.get(9).equals("l"));
        ass( testString(v).equals("cdefghijkl") );
        Log.d("Cyril","Capped vector " + testString(v));
    }

    public CappedVector( int maxSize ) {
        mMaxSize = maxSize;
        mElements = new Object[maxSize];
    }

    public void add( T element ) {
        // 0   1   2
        // a   b   c
        if (mSize < mMaxSize ) {
            mElements[mSize] = element;
            mSize++;
        }
        else {
            mStart++;
            if (mStart == mMaxSize) {
                mStart = 0;
            }
            if (mStart>0) {
                mElements[mStart-1] = element;
            }
            else {
                mElements[mMaxSize-1] = element;
            }
        }
    }

    public int size() {
        return mSize;
    }

    public T get( int index ) {
        // 0 1 2 3
        // b c d a
        //       !
        int i = mStart + index; // 3+2
        if (i >= mMaxSize) {
            i -= (mMaxSize);
        }
        return (T)mElements[i];
    }
}

