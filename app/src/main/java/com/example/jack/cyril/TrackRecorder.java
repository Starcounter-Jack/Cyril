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
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


public class TrackRecorder {

    public static class UpdateListener {
        public void onUpdate( Boolean newLocation ) {
        }
    }

    public enum Mode {
        Leg,
        Freeriding,
        Backtracking_Free,
        Backtracking_Leg,
        Error
    }

    Mode mCurrentMode;

    //private int mCurrentWaypoint = 1;
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
    private Vector<WayPoint> mWayPoints = new Vector<WayPoint>();
    private int mFocusedMarker = 1;
    private int mFocusedWayPoint = 0;
    private CappedVector<TrackEntry> mLastEntries = new CappedVector<TrackEntry>(1000);

    private UpdateListener mExternalUpdateListener;

    //private LocationListener mListener = null;

    public void setUpdateListener(UpdateListener l) {
        mExternalUpdateListener = l;
    }

    public Marker getFocusedMarker() {
        return mMarkers.get(mFocusedMarker);
    }
    public WayPoint getFocusedWayPoint() {
        if (mFocusedWayPoint >= mWayPoints.size()) {
            return null;
        }
        return mWayPoints.get(mFocusedWayPoint);
    }

    public WayPoint getNextWayPoint() {
        int i = mFocusedWayPoint+1;
        if (i >= mWayPoints.size()) {
            return null;
        }
        return mWayPoints.get(i);
    }

    public int getFocusedMarkerNo() {
        return mFocusedMarker;
    }
    public int getFocusedWayPointNo() {
        return mFocusedWayPoint;
    }

    public Track getTrack() {
        return mTrack;
    }

    public TrackRecorder( LocationManager lm, Context c ) {

       // CappedVector.test();

        mCurrentMode = TrackRecorder.Mode.Freeriding;


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
        mRecorder = WavAudioRecorder.getInstance(mContext);
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

    public void hitAction() {
        switch (mCurrentMode) {
            case Leg:
                //long now = getTimeOfDay();
                long now = getNowTimeOffset() - 300;
                Log.d( "Cyril", "Pressed at "  + formatTime(now,true) );
                mFocusedWayPoint++;
                say("At " + mFocusedWayPoint+"! " + getApproachingString(true));
                break;
            case Freeriding:
                createMarker();
                goForward();
                break;
            case Backtracking_Free:
                say("Tracking");
                break;
            case Backtracking_Leg:
                say("Tracking " + mFocusedWayPoint + "!");
                break;

        }
    }

    public void hitPrevious() {
        goBackward();
        switch (mCurrentMode) {
            case Leg:
                say( getApproachingString(true) );
                break;
        }
    }

    public void goBackward() {
        switch (mCurrentMode) {
            case Leg:
                if (mFocusedWayPoint>0) {
                    mFocusedWayPoint--;
                    mExternalUpdateListener.onUpdate(false);
                }
                break;
            case Freeriding:
                mFocusedMarker--;
                mExternalUpdateListener.onUpdate(false);
                break;
        }

    }

    public void hitNext() {
        goForward();
        switch (mCurrentMode) {
            case Leg:
                say( getApproachingString(true) );
                break;
        }

    }

    public void goForward() {
        switch (mCurrentMode) {
            case Leg:
                mFocusedWayPoint++;
                mExternalUpdateListener.onUpdate(false);
                break;
            case Freeriding:
                mFocusedMarker++;
                mExternalUpdateListener.onUpdate(false);
                break;
        }

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
        //t1.setEngineByPackageName("com.google.tts");




        mNextTrackId = calculateNextTrackId();

        long baseTime = startAudioRecording();

        if (locationManager == null) {
            throw new IllegalStateException("Recording was stopped. Cannot restart.");
        }
        initDb();

        readJson();


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0,locationListener);
        mTrack = new Track(db);
        mTrack.BaseTime = (baseTime);
        mTrack.Insert();

        sayMode();

        //Marker marker = createMarker();
        //goForward();

        //mExternalUpdateListener.onUpdate(false);
    }

    public void hitToggleMode() {
        switch (mCurrentMode) {
            case Leg:
                mCurrentMode = Mode.Freeriding;
                break;
            case Freeriding:
                mCurrentMode = Mode.Backtracking_Free;
                break;
            case Backtracking_Free:
                mCurrentMode = Mode.Backtracking_Leg;
                break;
            case Backtracking_Leg:
                mCurrentMode = Mode.Leg;
                break;
        }
        sayMode();
        mExternalUpdateListener.onUpdate(false);
    }


    public Mode getCurrentMode() {
        return mCurrentMode;
    }


    public String getApproachingString(Boolean full) {
        String str = "Approaching " + Integer.toString(mFocusedWayPoint + 1);
        if (full) {
            WayPoint wp = getNextWayPoint();
            str += ", " + wp.Description + "," + wp.Direction;
        }
        return str;
    }

    public String getCurrentModeString() {
        String str;
        switch (mCurrentMode) {
            case Backtracking_Free:
                str = "Backtracking Free";
                break;
            case Backtracking_Leg:
                str = "Backtracking Leg";
                break;
            case Freeriding:
                str = "Freeriding";
                break;
            case Leg:
                str = getApproachingString(false);
                break;
            case Error:
                str = "Error";
                break;
            default:
                str = "Unknown";
        }
        return str;
    }

    public void sayMode() {
        switch (mCurrentMode) {
            case Backtracking_Leg:
                say("Backtracking leg");
                break;
            case Backtracking_Free:
                say("Backtracking free");
                break;
            case Freeriding:
                say("Free riding");
                break;
            case Leg:
                say(getApproachingString(true));
                break;
            case Error:
                say("Error");
                break;
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
            mRecorder.checkPoint();
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
            File file = new File(mFileDirectory + "/cyril.json");

            byte[] fileBytes = FileHelper.readBytes(file);

            str = new String(fileBytes, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            Log.e("Cyril",e.getMessage());
            mCurrentMode = Mode.Error;
            return;
        }

        //str = "{ \"roadBook\": [ { \"lon\":102.123456 } ]}";
        try {
            JSONObject obj = new JSONObject(str);
            //JSONArray arr = new JSONArray(str);
            JSONArray arr = obj.getJSONArray("roadBook");
            for (int t = 0; t < arr.length(); t++) {
                JSONObject elem = arr.getJSONObject(t);
                int no = elem.getInt("no");
                double lon = elem.getDouble("lon");
                double lat = elem.getDouble("lat");
                WayPoint wp = new WayPoint(db);
                wp.Latitude = lat;
                wp.Longitude = lon;
                wp.Status = WayPointStatus.NotVisited;
                wp.WayPointId = no;
                wp.Description = elem.getString("descr");
                wp.Direction = elem.getString("dir");
                wp.TotalDistance = elem.getDouble("odo");
                mWayPoints.add( wp );
//                Log.d("Cyril","Parsing longitude " + Double.toString());
//                Log.d("Cyril","Parsing latitude " + elem.getDouble("lat"));
            }
        }
        catch (JSONException e ) {
            Log.e("Cyril",e.getMessage());
            mCurrentMode = Mode.Error;
        }
    }


    private void initDb() {

        String fileName = mFileDirectory + "/" + getFileStub() + ".sqlite";
        Log.d("Cyril.Db","Creating database " + fileName);
        db = SQLiteDatabase.openOrCreateDatabase(fileName, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Bookmark (" +
                "Track INTEGER, " +
                "Time INTEGER, " +
                "EventType INTEGER);");
        db = SQLiteDatabase.openOrCreateDatabase(fileName, null);
    }


    public double getTravellDistance() {
        return mTravelled;
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

    public int getWayPointCount() {
        return mWayPoints.size();
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



    static String formatTime( long millis, Boolean fractions ) {

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        if (fractions ) {
            return String.format("%02d:%02d:%02d:%02d",
                    hours,
                    minutes - TimeUnit.HOURS.toMinutes(hours),
                    seconds - TimeUnit.MINUTES.toSeconds(minutes),
                    millis - TimeUnit.SECONDS.toMillis(seconds)
            );
        }
        return String.format("%02d:%02d:%02d:%02d",
                hours,
                minutes - TimeUnit.HOURS.toMinutes(hours),
                seconds - TimeUnit.MINUTES.toSeconds(minutes));
    }

    static String formatDistance( double m, Boolean showDecimal ) {
        if (showDecimal) {
            return String.format(Locale.US, "%1$,.1f", m) + "m";
        }
        return String.format(Locale.US, "%1$,.0f", m) + "m";
    }

    long getTimeOfDay() {
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long passed = now - c.getTimeInMillis();
        return passed;
    }

}

