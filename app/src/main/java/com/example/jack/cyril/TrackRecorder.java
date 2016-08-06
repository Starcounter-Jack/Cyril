package com.example.jack.cyril;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.File;
import java.util.Locale;
import java.util.Random;


public class TrackRecorder {

    private static int mNextTrackId = -1;

    private Track mTrack;
    private SQLiteDatabase db;
    private LocationManager locationManager;
    private Location mLastLocation = null;
    private Location mOlderLocation = null;
    private WavAudioRecorder mRecorder = null;
    private TextToSpeech t1;
    private Context mContext;
    public static String mFileDirectory;

    private LocationListener mListener = null;

    public TrackRecorder( LocationManager lm, LocationListener l, Context c ) {
        mListener = l;
        locationManager = lm;
        mContext = c;
        mFileDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public Location getLastKnownLocation() {
        return     locationManager.getLastKnownLocation("gps");
    }


    public double getLastKnownDistance() {
        if (mOlderLocation == null) {
            return 0;
        }
        return mLastLocation.distanceTo(mOlderLocation);
    }


    private void startAudioRecording() {

        String fileName = mFileDirectory + "/" + getFileStub() + ".wav";
        mRecorder = WavAudioRecorder.getInstance();
        //mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //mRecorder.setAudioChannels(1);
        //mRecorder.setAudioSamplingRate(48000);
        //mRecorder.setAudioEncodingBitRate(128000);
        //mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        //mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setOutputFile(fileName);

//        try {
        mRecorder.prepare();
//        } catch (IOException e) {
//            Log.e("Prepare audio recorder", e.getMessage());
//            return;
//        }
        Log.d("Cyril",fileName);

        mRecorder.start();

    }

    public void start() {

        t1 = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);

                }
            }
        });

        startAudioRecording();

        if (locationManager == null) {
            throw new IllegalStateException("Recording was stopped. Cannot restart.");
        }
        initDb();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0,locationListener);
        mTrack = new Track(db);
        mTrack.BaseTime = (java.lang.System.currentTimeMillis());
        mTrack.Insert();

        Marker marker = new Marker(db);
        marker.TrackId = mTrack.TrackId;
        marker.Insert();
    }



    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mOlderLocation = mLastLocation;
            mLastLocation = location;
                long timeOffset = (java.lang.System.currentTimeMillis()) - mTrack.BaseTime;
                TrackEntry t = new TrackEntry(db);
                t.TrackId = mTrack.TrackId;
                t.Time = timeOffset;
                t.Longitude = location.getLongitude();
                t.Latitude = location.getLatitude();
                t.Accuracy = location.getAccuracy();
                t.Insert();
            mListener.onLocationChanged(location);

        }


        @Override
        public void onProviderDisabled(String str) {
            mListener.onProviderDisabled(str);

        }

        @Override
        public void onProviderEnabled(String str) {
            mListener.onProviderEnabled(str);

        }

        @Override
        public void onStatusChanged(String str, int i, Bundle b) {
            mListener.onStatusChanged(str,i,b);
        }
    };

    private void initDb() {
        String fileName = mFileDirectory + "/" + getFileStub() + ".sqlite";
        db = SQLiteDatabase.openOrCreateDatabase(fileName, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Bookmark (" +
                "Track INTEGER, " +
                "Time INTEGER, " +
                "EventType INTEGER);");

    }

    public void goBackward() {
        Log.d("Cyril","Going backward");
    }

    public void goForward() {
        Log.d("Cyril","Going forward");

    }

    public void createMarker() {
        Log.d("Cyril","Creating marker");
        Random r = new java.util.Random();
        int distance1 = r.nextInt(99);
        int distance2 = r.nextInt(999);
        t1.speak(Integer.toString(2) + "kilometers and " + Integer.toString(530) + " metres. " +
                        Integer.toString(30 - 1) + ". " +
                        Integer.toString(30 - 2) + ". " +
                        Integer.toString(30 - 3) + ". " +
                        Integer.toString(30 - 4) + ". " +
                        Integer.toString(30 - 5) + ". " +
                        Integer.toString(30 - 6) + "... " +
                        Integer.toString(2) + "kilometers and " + Integer.toString(530 - 8) + "metres."
                , TextToSpeech.QUEUE_FLUSH, null);
    }


    public void stop() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            //Track.mNextAudioPart++;
        }

    }

    public void release() {
        if (db != null ) {
            SQLiteDatabase temp = db;
            db = null;
            temp.close();
        }

        if (t1 != null) {
            t1.stop();
            t1.shutdown();
            t1 = null;
        }

    }

    public static int calculateNextTrackId() {
        int last = 0;
        File folder = new File(mFileDirectory);

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String name = listOfFiles[i].getName();
                if (name.startsWith("Track")) { // && name.endsWith(".sqlite")) {
                    int pos = name.indexOf(".");
                    if (pos != -1) {
                        String number = name.substring(5,pos);
                        try {
                            int n = Integer.parseInt(number);
                            if (n > last) {
                                last = n;
                            }
                        }
                        catch (Exception e) {

                        }
                    }
                }
            }
        }
        Log.i( "Cyril", "Next track is " + Integer.toString(last + 1));
        return last + 1;
    }

    public static String getFileStub() {
        if (mNextTrackId==-1) {
            mNextTrackId = calculateNextTrackId();
        }

        return "Track" + Integer.toString(mNextTrackId).toString();
    }

}

