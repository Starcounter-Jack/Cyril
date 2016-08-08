package com.example.jack.cyril;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;

/**
 * Created by jack on 03/08/2016.
 */
public class Track {

    final private static int Col_TrackId = 1;
    final private static int Col_BaseTime = 2;
    final private static int Col_MarkerCount = 3;
    final private static int Col_CurrentTarget = 4;

    //public static int mNextAudioPart = 1;


    public int TrackId;
    public long BaseTime;
    public int MarkerCount;
    public int CurrentTarget;


    //public static String getAudioFileStub() {
    //    if (mNextTrackId==-1) {
    //        mNextTrackId = calculateNextTrackId();
//        }
//
//        return "Track" + Integer.toString(mNextTrackId).toString() + "-" + Integer.toString(mNextAudioPart);
//    }




    private SQLiteDatabase mDb;
    public static SQLiteStatement mInsertStmt;


    public Track( SQLiteDatabase db ) {
        mDb = db;
        if (mInsertStmt == null) {
            db.execSQL("CREATE TABLE IF NOT EXISTS Track (" +
                    "Track INTEGER, " +
                    "BaseTime INTEGER, " +
                    "MarkerCount INTEGER, " +
                    "CurrentTarget INTEGER);");
            mInsertStmt = db.compileStatement("INSERT INTO Track VALUES(?,?,?,?);");
        }
    }

    public void Insert() {
        Log.d("Cyril.Db","Inserting Track");
        mDb.beginTransaction();
        mInsertStmt.bindLong(Col_TrackId, TrackId);
        mInsertStmt.bindLong(Col_BaseTime, BaseTime );
        mInsertStmt.bindLong(Col_MarkerCount, MarkerCount );
        mInsertStmt.bindLong(Col_CurrentTarget, CurrentTarget );
        mInsertStmt.execute();
        mDb.setTransactionSuccessful();
        mDb.endTransaction();

    }
}
