package com.example.jack.cyril;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.media.MediaRecorder;
import android.media.AudioRecord;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.database.sqlite.*;
import android.speech.tts.TextToSpeech;
import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Controller extends android.app.Activity implements SensorEventListener {
    private View mContentView;

    // ===============================
    // Three button logic
    // -------------------------------
    // Back      Check previous target
    // Forward   Check next target
    // Action    Mark/Backtrack/Race
    // ===============================


    private long mTicks = 0;
    private View mFrameView;

    private TextView mDistanceView;                 // Distance to target       3 514 metres
    private TextView mTargetHeadingView;            // Target Heading           13°
    private TextView mCurrentHeadingView;           // Current Heading          15°
    private TextView mCurrentCourseView;
    private TextView mAccuracyView;                 // GPS Accuracy             4m

    private TextView mStatusView;                   // Status                   In pit
    //                          At start line
    //                          Racing
    //                          Backtracking
    private TextView mCurrentTargetView;            // Current target           25/24
    private TextView mCurrentModeView;              // Backtracking
    private TextView mTimeView;                     // Time of day              12:31
    private TextView mRaceTimeView;                 // Time racing              1:12:24.45

    private TextView mLongitudeView;                // Current position
    private TextView mLatitudeView;                 // Current position
    private TextView mOffsetLatitudeView;
    private TextView mOffsetLongitudeView;


    private TextView mTargetLongitudeView;                // Current position
    private TextView mTargetLatitudeView;                 // Current position
    private TextView mTargetOffsetLatitudeView;
    private TextView mTargetOffsetLongitudeView;

    private TextView mDistanceToGoalView;           // Distance to goal         11 514 metres

    private TextView mDistanceTravelledView;        // Distance travelled       100 413 metres
    private TextView mDistanceFromStartView;        // Distance from start      80 312 metres

    private TextView mTrackIdView;                  // Track id                 Track 1
    private TextView mLocationCountView;          // No of track entries      15 512
    private TextView mRecordingTimeView;            // Audio recording time     1:15:24
    private TextView mAudioSizeView;                 // Audio recording size     1 123 000
    private TextView mBatteryView;                  // Battery status           80%

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private TrackRecorder mTrackRecorder;

    private SensorManager mSensorManager;

    private Sensor mAccelerometer;
    private Sensor mMagneticSensor;

    float[] mGravity;
    float[] mGeomagnetic;




    //public boolean isRecording() {
    //return (mRecorder != null);
    // }








    @ Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("Cyril","========= NEW SESSION =======");

        super.onCreate(savedInstanceState);


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



        mSensorManager.registerListener(this, mMagneticSensor,SensorManager.SENSOR_DELAY_GAME); // SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_GAME); // SensorManager.SENSOR_DELAY_GAME);
        setContentView(R.layout.activity_controller);

        //Log.d("Cyril","Trying to start background recording service 1");
        //Intent intent = new Intent(this, BackgroundRecorder.class);
        //startService(intent);
        //Log.d("Cyril","Trying to start background recording service 2");



//        setContentView(R.layout.activity_controller);



        //mVisible = true;
        // mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.contentView);
        mCurrentTargetView = (TextView) findViewById(R.id.currentTargetView);
        mCurrentHeadingView = (TextView) findViewById(R.id.headingView);
        mCurrentCourseView = (TextView) findViewById(R.id.courseView);
        mRecordingTimeView = (TextView) findViewById(R.id.recordingTimeView);

        mOffsetLatitudeView = (TextView) findViewById(R.id.offsetLatitudeView);
        mOffsetLongitudeView = (TextView) findViewById(R.id.offsetLongitudeView);
        mLatitudeView = (TextView) findViewById(R.id.latitudeView);
        mLongitudeView = (TextView) findViewById(R.id.longitudeView);

        mTargetOffsetLatitudeView = (TextView) findViewById(R.id.targetOffsetLatitudeView);
        mTargetOffsetLongitudeView = (TextView) findViewById(R.id.targetOffsetLongitudeView);
        mTargetLatitudeView = (TextView) findViewById(R.id.targetLatitudeView);
        mTargetLongitudeView = (TextView) findViewById(R.id.targetLongitudeView);


        mCurrentModeView = (TextView) findViewById(R.id.modeView);
        mTimeView = (TextView) findViewById( R.id.fullscreen_time);
        mAccuracyView = (TextView) findViewById( R.id.accuracyView );
        mDistanceView = (TextView) findViewById( R.id.targetDistanceView );
        mLocationCountView = (TextView) findViewById( R.id.locationCountView );
        mDistanceToGoalView = (TextView) findViewById( R.id.goalDistanceView );
        mDistanceTravelledView = (TextView) findViewById( R.id.travelledView );
        mDistanceToGoalView = (TextView) findViewById( R.id.goalDistanceView );

        // Set up the user interaction to manually show or hide the system UI.
        //mContentView.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        //toggle();
        //        updateText();
        //    }
        //});

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


        int canDo = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (canDo != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

        canDo = checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        if (canDo != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }


        canDo = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (canDo != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }



        //updateThread.start();

        startRecording();

    }



    public void startRecording() {


        //startAudioRecording();



        mTrackRecorder = new TrackRecorder( (LocationManager) getSystemService(LOCATION_SERVICE),
                getApplicationContext() );
        mTrackRecorder.setUpdateListener(new TrackRecorder.UpdateListener() {
            @Override
            public void onUpdate(Boolean newLocation) {
                //Log.d("Cyril", "Updating");
                updateText( newLocation );
            }
        });
        mTrackRecorder.start();





        Log.i("Cyril","Started recording");

    }


    Thread updateThread = new Thread() {
        public void run() {
            try {
                while (true) {
                    Controller.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // updateText();
                        }
                    });
                    Thread.sleep(100);
                }
            } catch (InterruptedException ignore) {
                // Thread was interrupted ==> stop loop
            }
        }
    };

    private void updateText( Boolean newLocation ) {

        long now = java.lang.System.currentTimeMillis();
        mRecordingTimeView.setText(millisecondsToTimeString(now - mTrackRecorder.getTrack().BaseTime,true));
        mCurrentTargetView.setText( Integer.toString(mTrackRecorder.getFocusedMarkerNo()) + "/" + Integer.toString(mTrackRecorder.getMarkerCount() ) );
        mCurrentModeView.setText( mTrackRecorder.getCurrentModeString() );

        if (newLocation) {
            Location location = mTrackRecorder.getLastKnownLocation();
            Location first = mTrackRecorder.getFirstLocation();


            String accuracy;
            if (location == null) {
                accuracy = "n/a";
            } else {
                accuracy = Double.toString(location.getAccuracy()) + " m";
            }
            mAccuracyView.setText(accuracy);

            mCurrentCourseView.setText(Math.toDegrees(location.getBearing()) + "°");


            mLongitudeView.setText(Double.toString(location.getLongitude()));
            mLatitudeView.setText(Double.toString(location.getLatitude()));
            Location originLat = new Location(first);
            originLat.setLongitude(location.getLongitude());
            Location originLong = new Location(first);
            originLong.setLatitude(location.getLatitude());
            mOffsetLatitudeView.setText(formatDistance(location.distanceTo(originLat),false));
            mOffsetLongitudeView.setText(formatDistance(location.distanceTo(originLong),false));


            mDistanceTravelledView.setText(formatDistance(first.distanceTo(location),false));
            mDistanceView.setText(Double.toString(mTrackRecorder.getLastKnownDistance()));
            mLocationCountView.setText(Long.toString(mTrackRecorder.getLocationCount()));

            mDistanceToGoalView.setText( "n/a" );

        }


    }


    void hitAction() {
        mTrackRecorder.createMarker();
        mTrackRecorder.goForward();
    }

    void hitToggleMode() {
        mTrackRecorder.toggleMode();
    }


    void onClickModeButton(View v) {
        Log.d("Cyril","MODE TOGGLE ACTION!");
        hitToggleMode();
    }

    void onClickActionButton(View v) {
        Log.d("Cyril","CLICKED ACTION!");
        hitAction();
    }

    void onClickPreviousButton(View v) {
        Log.d("Cyril","CLICKED PREVIOUS!");
        mTrackRecorder.goBackward();
    }

    void onClickNextButton(View v) {
        Log.d("Cyril","CLICKED NEXT!");
        mTrackRecorder.goForward();
    }


    String millisecondsToTimeString( long millis, Boolean fractions ) {

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

    String formatDistance( double m, Boolean showDecimal ) {
        if (showDecimal) {
            return String.format(Locale.US, "%1$,.1f", m) + "m";
        }
        return String.format(Locale.US, "%1$,.0f", m) + "m";
    }





    @Override
    public boolean onKeyDown(int keyCode,KeyEvent e) {
        mLatitudeView.setText( Integer.toString(keyCode));

        switch (keyCode ) {
            case (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE):
            case (KeyEvent.KEYCODE_BACK):
            {
                hitAction();
                break;
            }
            case (KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD):
            case (KeyEvent.KEYCODE_VOLUME_UP):
            {
                mTrackRecorder.goBackward();
                break;
            }
            case (KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD):
            case (KeyEvent.KEYCODE_VOLUME_DOWN):
            {
                mTrackRecorder.goForward();
                break;
            }
        }

        return true;
    }


    public void stopRecording() {

        Log.i("Cyril","Stopping recording");

        //stopAudioRecording();




        if (mTrackRecorder != null) {
            mTrackRecorder.stop();
            mTrackRecorder.release();
            mTrackRecorder = null;
        }
    }

    /*
    @Override
    protected void onStop() {

        Log.i("Cyril","onStop");

        stopRecording();
        super.onStop();
    }
    */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }



    @Override
    public void onSensorChanged(SensorEvent event) {
        //mTicks++;
        //mCurrentHeadingView.setText( Long.toString(mTicks) );
        switch (event.sensor.getType()) {
            case (Sensor.TYPE_ACCELEROMETER) : {
                mGravity = event.values;
                //Log.d("Cyril", "OnSensor TYPE_ACCELEROMETER");
                break;
            }
            case ( Sensor.TYPE_MAGNETIC_FIELD) : {
                mGeomagnetic = event.values;
                //Log.d("Cyril", "OnSensor TYPE_MAGNETIC");
                break;
            }
            default:
//                Log.d("Cyril", "OnSensor ?" + event.sensor.getStringType());
        }
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                int azimut = (int) Math.round(Math.toDegrees(orientation[0]));
                if (azimut < 0) azimut += 360;
                mCurrentHeadingView.setText(azimut + "°");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

/*
new Thread(new Runnable() {
  @Override public void run() {
    FileInputStream inputStream = null;
    try {
      inputStream = new FileInputStream(path);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    while (true) {
      byte[] buffer = new byte[0];
      try {
        buffer = new byte[inputStream.available()];
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        inputStream.read(buffer);
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        mSender.getOutputStream().write(buffer);
        mSender.getOutputStream().flush();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}).start();
 */
