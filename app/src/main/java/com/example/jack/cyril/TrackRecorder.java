package com.example.jack.cyril;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;


public class TrackRecorder {

    public static class UpdateListener {
        public void onUpdate( Boolean newLocation ) {
        }
    }

    public enum Mode {
        Leg,
        Freeriding,
        Backtracking
    }

    Mode mCurrentMode;

    private int mCurrentLegNo = 1;
    private static int mNextTrackId = -1;

    private double mTravelled = 0;
    private Track mTrack;
    private SQLiteDatabase db;
    private LocationManager locationManager;

    private Location mLastLocation = null;
    private Location mOlderLocation = null;
    private Location mFirstLocation;
    private Location mStart;
    private Location mFinish;


    private WavAudioRecorder mRecorder = null;
    private TextToSpeech t1;
    private Context mContext;
    public static String mFileDirectory;
    private long mLocationCount = 0;
    private AudioRecord.OnRecordPositionUpdateListener mAudioUpdateListener;
    private Vector<Marker> mMarkers = new Vector<Marker>();
    private Vector<WayPoint> mLegs = new Vector<WayPoint>();
    private int mFocusedMarker = 1;

    private UpdateListener mExternalUpdateListener;

    //private LocationListener mListener = null;

    public void setUpdateListener(UpdateListener l) {
        mExternalUpdateListener = l;
    }

    public Marker getFocusedMarker() {
        return mMarkers.get(mFocusedMarker);
    }

    public int getFocusedMarkerNo() {
        return mFocusedMarker;
    }

    public Track getTrack() {
        return mTrack;
    }

    public TrackRecorder( LocationManager lm, Context c ) {

        mCurrentMode = TrackRecorder.Mode.Freeriding;

        LocationListener l = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mExternalUpdateListener.onUpdate(true);
            }

            @Override
            public void onProviderDisabled(String str) {

            }

            @Override
            public void onProviderEnabled(String str) {

            }

            @Override
            public void onStatusChanged(String str, int i, Bundle b) {

            }
        };
        //mListener = l;
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


    private long startAudioRecording() {

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

        mRecorder.setUpdateListener( new AudioRecord.OnRecordPositionUpdateListener() {
            //	periodic updates on the progress of the record head
            public void onPeriodicNotification(AudioRecord recorder) {
                if (mExternalUpdateListener != null)
                    mExternalUpdateListener.onUpdate(false);
            }
            //	reached a notification marker set by setNotificationMarkerPosition(int)
            public void onMarkerReached(AudioRecord recorder) {
            }
        });

//        try {
        mRecorder.prepare();
//        } catch (IOException e) {
//            Log.e("Prepare audio recorder", e.getMessage());
//            return;
//        }
        Log.d("Cyril",fileName);

        mRecorder.start();

        return java.lang.System.currentTimeMillis();

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

        readJson();

        long baseTime = startAudioRecording();

        if (locationManager == null) {
            throw new IllegalStateException("Recording was stopped. Cannot restart.");
        }
        initDb();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0,locationListener);
        mTrack = new Track(db);
        mTrack.BaseTime = (baseTime);
        mTrack.Insert();

        sayMode();

        //Marker marker = createMarker();
        //goForward();

        //mExternalUpdateListener.onUpdate(false);
    }

    public void toggleMode() {
        switch (mCurrentMode) {
            case Leg:
                mCurrentMode = Mode.Freeriding;
                break;
            case Freeriding:
                mCurrentMode = Mode.Backtracking;
                break;
            case Backtracking:
                mCurrentMode = Mode.Leg;
                break;
        }
        sayMode();
    }

    public String getCurrentModeString() {
        String str;
        switch (mCurrentMode) {
            case Backtracking:
                str = "Backtracking";
                break;
            case Freeriding:
                str = "Freeride";
            case Leg:
                str = "Approaching 14"; // TODO!
            default:
                str = "Unknown";
        }
        return str;
    }

    public void sayMode() {
        switch (mCurrentMode) {
            case Backtracking:
                say("Backtracking");
                break;
            case Freeriding:
                say("Freeriding");
            case Leg:
                say("Approaching 14"); // TODO!
        }
    }

    public Marker createMarker() {
        Marker marker = new Marker(db);
        marker.TrackId = mTrack.TrackId;
        marker.Insert();
        mMarkers.add(marker);
        mExternalUpdateListener.onUpdate(false);
        return marker;
    }


    public long getLocationCount() {
        return mLocationCount;
    }

    public Location getFirstLocation() {
        return mFirstLocation;
    }

    public long getNowTimeOffset() {
        return (java.lang.System.currentTimeMillis()) - mTrack.BaseTime;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mOlderLocation = mLastLocation;
            mLastLocation = location;
            mLocationCount++;
                long timeOffset = getNowTimeOffset();
                TrackEntry t = new TrackEntry(db);
                t.TrackId = mTrack.TrackId;
                t.Time = timeOffset;
                t.Longitude = location.getLongitude();
                t.Latitude = location.getLatitude();
                t.Accuracy = location.getAccuracy();
                t.Insert();
            if (mOlderLocation == null) {
                mFirstLocation = location;
            }
           // mListener.onLocationChanged(location);
            mExternalUpdateListener.onUpdate(true);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String str, int i, Bundle b) {

        }
    };


    private void readJson() {
        String str;

        try {
            File file = new File(mFileDirectory = "/cyril.json");

            byte[] fileBytes = FileHelper.readBytes(file);

            str = new String(fileBytes, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            Log.e("Cyril",e.getMessage());
        }

        str = "{ \"roadBook\": [ { \"lon\":102.123456 } ]}";
        try {
            JSONObject obj = new JSONObject(str);
            //JSONArray arr = new JSONArray(str);
            JSONArray arr = obj.getJSONArray("roadBook");
            for (int t = 0; t < arr.length(); t++) {
                JSONObject elem = arr.getJSONObject(t);
                Log.d("Cyril","Parsing longitude " + Double.toString(elem.getDouble("lon")));
                Log.d("Cyril","Parsing latitude " + elem.getDouble("lat"));
            }
        }
        catch (JSONException e ) {
            Log.e("Cyril",e.getMessage());
        }
    }


    private void initDb() {
        mNextTrackId = calculateNextTrackId();

        String fileName = mFileDirectory + "/" + getFileStub() + ".sqlite";
        Log.d("Cyril.Db","Creating database " + fileName);
        db = SQLiteDatabase.openOrCreateDatabase(fileName, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Bookmark (" +
                "Track INTEGER, " +
                "Time INTEGER, " +
                "EventType INTEGER);");
        db = SQLiteDatabase.openOrCreateDatabase(fileName, null);
    }

    public void goBackward() {

        Log.d("Cyil","Going backward");
        mFocusedMarker--;
        mExternalUpdateListener.onUpdate(false);
    }

    public double getTravellDistance() {
        return mTravelled;
    }

    public void goForward() {
        Log.d("Cyril","Going forward");
        mFocusedMarker++;
        mExternalUpdateListener.onUpdate(false);
    }

    public void foobar() {


        Log.d("Cyril","Creating marker");
        Random r = new java.util.Random();
        int distance1 = r.nextInt(99);
        int distance2 = r.nextInt(999);
        say(Integer.toString(2) + "kilometers and " + Integer.toString(530) + " metres. " +
                        Integer.toString(30 - 1) + ". " +
                        Integer.toString(30 - 2) + ". " +
                        Integer.toString(30 - 3) + ". " +
                        Integer.toString(30 - 4) + ". " +
                        Integer.toString(30 - 5) + ". " +
                        Integer.toString(30 - 6) + "... " +
                        Integer.toString(2) + "kilometers and " + Integer.toString(530 - 8) + "metres.");
    }

    public void say( String text ) {
        t1.speak( text, TextToSpeech.QUEUE_FLUSH, null);
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

    public int getMarkerCount() {
        return mMarkers.size();
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

        return "Track" + Integer.toString(mNextTrackId).toString();
    }

}

