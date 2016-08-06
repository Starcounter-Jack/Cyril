package com.example.jack.cyril;


import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class Marker {

    final private static int Col_TrackId = 1;
    final private static int Col_Time = 2;
    final private static int Col_MarkerId = 3;

    public int TrackId;
    public long Time;
    public int MarkerId;

    private SQLiteDatabase mDb;
    public static SQLiteStatement mInsertStmt;

    public Marker( SQLiteDatabase db ) {
        mDb = db;
        if (mInsertStmt == null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS Marker (" +
                    "Track INTEGER, " +
                    "Time INTEGER, " +
                    "MarkerId INTEGER);");
            mInsertStmt = db.compileStatement("INSERT INTO Marker VALUES(?,?,?);");
        }
    }

    public static Marker getMarker( int id ) {
        throw new RuntimeException("Not implemented");
    }

    public void Insert() {
        if (mDb.isOpen()) {
            mDb.beginTransaction();
            mInsertStmt.bindLong(Marker.Col_TrackId, TrackId);
            mInsertStmt.bindLong(Marker.Col_Time, Time);
            mInsertStmt.bindLong(Marker.Col_MarkerId, MarkerId);
            mInsertStmt.execute();
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
        }
    }

}
