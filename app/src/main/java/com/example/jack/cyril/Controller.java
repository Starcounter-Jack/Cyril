package com.example.jack.cyril;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.media.MediaRecorder;
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

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.Random;




/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Controller extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    // ===============================
    // Three button logic
    // -------------------------------
    // Back      Check previous target
    // Forward   Check next target
    // Action    Mark/Backtrack/Race
    // ===============================



    private TextView mDistanceView;                 // Distance to target       3 514 metres
    private TextView mTargetHeadingView;     // Target Heading           13°
    private TextView mCurrentHeadingView;           // Current Heading          15°
    private TextView mAccuracyView;                 // GPS Accuracy             4m

    private TextView mStatusView;                   // Status                   In pit
                                                    //                          At start line
                                                    //                          Racing
                                                    //                          Backtracking
    private TextView mCurrentTargetView;            // Current target           25/24
    private TextView mTimeView;                     // Time of day              12:31
    private TextView mRaceTimeView;                 // Time racing              1:12:24.45

    private TextView mLongitudeView;                // Current position
    private TextView mLatitudeView;                 // Current position
    private TextView mDistanceToGoalView;           // Distance to goal         11 514 metres

    private TextView mDistanceTravelledView;        // Distance travelled       100 413 metres
    private TextView mDistanceFromStartView;        // Distance from start      80 312 metres

    private TextView mTrackIdView;                  // Track id                 Track 1
    private TextView mTrackEntryCountView;          // No of track entries      15 512
    private TextView mRecordingTimeView;            // Audio recording time     1:15:24
    private TextView mFileSizeView;                 // Audio recording size     1 123 000
    private TextView mBatteryView;                  // Battery status           80%

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private TrackRecorder mTrackRecorder;






    //public boolean isRecording() {
        //return (mRecorder != null);
   // }










    @ Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("Cyril","========= NEW SESSION =======");

        super.onCreate(savedInstanceState);



        //Log.d("Cyril","Trying to start background recording service 1");
        //Intent intent = new Intent(this, BackgroundRecorder.class);
        //startService(intent);
        //Log.d("Cyril","Trying to start background recording service 2");



        setContentView(R.layout.activity_controller);


        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.contentView);

        mLongitudeView = (TextView) findViewById(R.id.longitudeView);
        mLatitudeView = (TextView) findViewById(R.id.latitudeView);
        mTimeView = (TextView) findViewById( R.id.fullscreen_time);
        mAccuracyView = (TextView) findViewById( R.id.accuracyView );
        mDistanceView = (TextView) findViewById( R.id.targetDistanceView );

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

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
               updateText();
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


        mTrackRecorder = new TrackRecorder( (LocationManager) getSystemService(LOCATION_SERVICE),
                locationListener, getApplicationContext() );
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

    private void updateText() {
        Location location = mTrackRecorder.getLastKnownLocation();

        Date date = new Date();
        double tempLong = location.getLongitude();
        double tempLat = location.getLatitude();
        //double tempDist =
        //mDistanceView.setText( Double.toString(tempDist));
        mLongitudeView.setText(Double.toString( tempLong ));
        mLatitudeView.setText(Double.toString(tempLat));
        String accuracy;
        if (location == null ) {
            accuracy = "n/a";
        }
        else {
            accuracy = Double.toString(location.getAccuracy()) + " m";
        }
        mAccuracyView.setText( accuracy );
        //mLatitudeView.setText("La: " + Double.toString(tempLat));
//        mTimeView.setText( Long.toString( date.getTime() ) );
        if (mTrackRecorder != null) {
            mDistanceView.setText(Double.toString(mTrackRecorder.getLastKnownDistance()));
        }
        Date now = new Date();
        mTimeView.setText( now.toString() );
    }







    @Override
    public boolean onKeyDown(int keyCode,KeyEvent e) {
        mLatitudeView.setText( Integer.toString(keyCode));

        switch (keyCode ) {
            case (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE):
            case (KeyEvent.KEYCODE_BACK):
            {
                mTrackRecorder.createMarker();
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

    @Override
    protected void onDestroy() {

        Log.i("Cyril","OnDestroy");

        stopRecording();
        super.onDestroy();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
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
