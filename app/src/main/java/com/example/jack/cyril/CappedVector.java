package com.example.jack.cyril;

/**
 * Created by jack on 08/08/2016.
 */
public class CappedVector<T> {

    private int mSize = 0;
    private final int mMaxSize;
    private final Object[] mElements;
    private int mStart = -1;

    
    public static String testString( CappedVector<String> v ) {
        String str = "";
        for (int t=0;t<v.size();t++) {
            str += v.get(t);
        }
        return str;
    }
    
    public static void test() {
        CappedVector<String> v = new CappedVector<String>(10);
        assert( v.size() == 0 );
        assert( testString(v).equals("") );
        v.add("a");
        assert( v.size() == 1 );
        assert( testString(v).equals("a") );
        v.add("b");
        assert( v.size() == 2 );
        assert( testString(v).equals("ab") );
        v.add("c");
        assert( v.size() == 3 );
        assert( testString(v).equals("abc") );
        v.add("d");
        assert( v.size() == 4 );
        assert( testString(v).equals("abcd") );
        v.add("e");
        assert( v.size() == 5 );
        assert( testString(v).equals("abcde") );
        v.add("f");
        assert( v.size() == 6 );
        assert( testString(v).equals("abcdef") );
        v.add("g");
        assert( v.size() == 7 );
        assert( testString(v).equals("abcdefg") );
        v.add("h");
        assert( v.size() == 8 );
        assert( testString(v).equals("abcdefgh") );
        assert( v.get(0).equals("a"));
        assert( v.get(7).equals("h"));
        v.add("i");
        assert( v.size() == 9 );
        assert( testString(v).equals("abcdefghi") );
        assert( v.get(0).equals("a"));
        assert( v.get(8).equals("i"));
        v.add("j");
        assert( v.size() == 10 );
        assert( testString(v).equals("abcdefghij") );
        assert( v.get(0).equals("a"));
        assert( v.get(9).equals("j"));
        v.add("k");
        assert( v.size() == 11 );
        assert( v.get(0).equals("b"));
        assert( v.get(9).equals("k"));
        assert( testString(v).equals("bcdefghijk") );
        v.add("l");
        assert( v.size() == 12 );
        assert( v.get(0).equals("c"));
        assert( v.get(9).equals("l"));
        assert( testString(v).equals("cdefghijl") );
    }

    public CappedVector( int maxSize ) {

        test();

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
            mElements[mStart] = element;
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
            i = index - (mMaxSize - mStart);
        }
        return (T)mElements[i];
    }
}

