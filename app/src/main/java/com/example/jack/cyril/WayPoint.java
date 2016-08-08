package com.example.jack.cyril;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class WayPoint {

    final private static int Col_TrackId = 1;
    final private static int Col_Time = 2;
    final private static int Col_LegId = 3;
    final private static int Col_Length= 4;

    public int TrackId;
    public long Time;
    public int LegId;
    public double Length;

    private SQLiteDatabase mDb;
    public static SQLiteStatement mInsertStmt;

    public WayPoint(SQLiteDatabase db ) {
        mDb = db;
        if (mInsertStmt == null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS WayPoint (" +
                    "Track INTEGER, " +
                    "Time INTEGER, " +
                    "LegId INTEGER, " +
                    "Length INTEGER);");
            mInsertStmt = db.compileStatement("INSERT INTO WayPoint VALUES(?,?,?,?);");
        }
    }

    public static WayPoint getLeg(int id ) {
        throw new RuntimeException("Not implemented");
    }

    public void Insert() {
        if (mDb.isOpen()) {
            mDb.beginTransaction();
            mInsertStmt.bindLong(WayPoint.Col_TrackId, TrackId);
            mInsertStmt.bindLong(WayPoint.Col_Time, Time);
            mInsertStmt.bindLong(WayPoint.Col_LegId, LegId);
            mInsertStmt.bindDouble(WayPoint.Col_Length, Length);
            mInsertStmt.execute();
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
        }
    }

}
