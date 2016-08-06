package com.example.jack.cyril;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by jack on 03/08/2016.
 */
public class TrackEntry {

    public int TrackId;
    public long Time;
    public double Longitude;
    public double Latitude;
    public double Accuracy;


    final private static int Col_TrackId = 1;
    final private static int Col_Time = 2;
    final private static int Col_Longitude = 3;
    final private static int Col_Latitude = 4;
    final private static int Col_Accuracy = 5;

    private SQLiteDatabase mDb;
    public static SQLiteStatement mInsertStmt;


    public TrackEntry( SQLiteDatabase db ) {
        mDb = db;
        if (mInsertStmt == null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS TrackEntry (" +
                    "TrackId INTEGER, " +
                    "Time INTEGER, " +
                    "Longitude REAL, " +
                    "Latitude REAL, " +
                    "Accuracy REAL);");
            mInsertStmt = db.compileStatement("INSERT INTO TrackEntry VALUES(?,?,?,?,?);");
        }
    }

    public void Insert() {
        if (mDb.isOpen()) {
            mDb.beginTransaction();
            mInsertStmt.bindLong(TrackEntry.Col_TrackId, TrackId);
            mInsertStmt.bindLong(TrackEntry.Col_Time, Time);
            mInsertStmt.bindDouble(TrackEntry.Col_Longitude, Longitude);
            mInsertStmt.bindDouble(TrackEntry.Col_Latitude, Latitude);
            mInsertStmt.bindDouble(TrackEntry.Col_Accuracy, Accuracy);
            mInsertStmt.execute();
            mDb.setTransactionSuccessful();
            mDb.endTransaction();
        }
    }
}
