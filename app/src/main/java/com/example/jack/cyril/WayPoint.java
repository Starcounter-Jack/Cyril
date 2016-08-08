package com.example.jack.cyril;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

enum WayPointStatus {
    NotVisited(1),
    Calculated(2),
    Confirmed(3);

    private long value;

    private WayPointStatus(long value) {
        this.value = value;
    }
    public long getValue() {
        return value;
    }
}

public class WayPoint {


    final private static int Col_TrackId = 1;
    final private static int Col_Time = 2;
    final private static int Col_WayPointId = 3;
    final private static int Col_SegmentDistance= 4;
    final private static int Col_TotalDistance= 5;
    final private static int Col_Longitude=6;
    final private static int Col_Latitude=7;
    final private static int Col_Status=8;

    public int TrackId;
    public long Time;
    public int WayPointId;
    public double SegmentDistance = -1;
    public double TotalDistance = -1;
    public double Longitude = -1;
    public double Latitude = -1;
    public WayPointStatus Status = WayPointStatus.NotVisited;

    private SQLiteDatabase mDb;
    public static SQLiteStatement mInsertStmt;

    public WayPoint(SQLiteDatabase db ) {
        mDb = db;
        if (mInsertStmt == null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS WayPoint (" +
                    "Track INTEGER, " +
                    "Time INTEGER, " +
                    "WayPointId INTEGER, " +
                    "SegmentDistance DOUBLE, " +
                    "TotalDistance DOUBLE, " +
                    "Longitude DOUBLE, " +
                    "Latitude DOUBLE, " +
                    "Status INTEGER);");
            mInsertStmt = db.compileStatement("INSERT INTO WayPoint VALUES(?,?,?,?,?,?,?,?);");
        }
    }

    public static WayPoint getWayPoint(int id ) {
        throw new RuntimeException("Not implemented");
    }

    public void Insert() {
        if (mDb.isOpen()) {
            mDb.beginTransaction();
            mInsertStmt.bindLong(WayPoint.Col_TrackId, TrackId);
            mInsertStmt.bindLong(WayPoint.Col_Time, Time);
            mInsertStmt.bindLong(WayPoint.Col_WayPointId, WayPointId);
            mInsertStmt.bindDouble(WayPoint.Col_SegmentDistance, SegmentDistance);
            mInsertStmt.bindDouble(WayPoint.Col_TotalDistance, TotalDistance);
            mInsertStmt.bindDouble(WayPoint.Col_Longitude,Longitude);
            mInsertStmt.bindDouble(WayPoint.Col_Latitude,Latitude);
            mInsertStmt.bindLong(WayPoint.Col_Status,Status.getValue());
            mInsertStmt.execute();
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
        }
    }

}
