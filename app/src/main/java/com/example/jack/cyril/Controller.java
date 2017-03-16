package com.example.jack.cyril;

import android.Manifest;
import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

//import io.flic.lib.FlicBroadcastReceiverFlags;
//import io.flic.lib.FlicButton;
//import io.flic.lib.FlicButtonCallback;
//import io.flic.lib.FlicButtonCallbackFlags;
//import io.flic.lib.FlicManager;
//import io.flic.lib.FlicManagerInitializedCallback;

//import io.flic.lib.FlicAppNotInstalledException;
//import io.flic.lib.FlicBroadcastReceiverFlags;
//import io.flic.lib.FlicButton;
//import io.flic.lib.FlicManager;
//import io.flic.lib.FlicManagerInitializedCallback;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Controller extends android.app.Activity implements SensorEventListener, View.OnTouchListener {
    private View mContentView;

    // private FlicManager mFlicManager;
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
    private TextView mDistanceLabelView;
    private TextView mTargetHeadingView;            // Target Heading           13°
    private TextView mCurrentHeadingView;           // Current Heading          15°
    private TextView mCurrentCourseView;
    private TextView mAccuracyView;                 // GPS Accuracy             4m

      // Status                   In pit
    //                          At start line
    //                          Racing
    //                          Backtracking
    private TextView mCurrentTargetLabelView;
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

    private FrameLayout mMenuButton;

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




    public boolean onTouch(View v, MotionEvent e ) {

        final int action = e.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
//                final float x = ev.getX();
//                final float y = ev.getY();

//                mLastTouchX = x;
//                mLastTouchY = y;

                // Save the ID of this pointer
//                mActivePointerId = ev.getPointerId(0);
                if (v == mContentView ) {
                    MenuDialog m = new MenuDialog(this);
                    m.show(getFragmentManager(),"test");
                }
                break;
            }
        }
        return true;
    }




    @ Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("Cyril","========= NEW SESSION =======");


        //FlicManager.setAppCredentials("Cyril", "8d7f7a58-cf68-411d-bda5-5a10b49293d3", "393a443a-46f2-4e23-a213-8543fcfebcee");

        super.onCreate(savedInstanceState);


/* #ifdef COMPASS
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



        mSensorManager.registerListener(this, mMagneticSensor,SensorManager.SENSOR_DELAY_GAME); // SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_GAME); // SensorManager.SENSOR_DELAY_GAME);
 #endif
*/
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
        mCurrentTargetLabelView = (TextView) findViewById(R.id.targetLabelView);
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
        mDistanceLabelView = (TextView) findViewById( R.id.distanceLabelView);
        mLocationCountView = (TextView) findViewById( R.id.locationCountView );
        mDistanceToGoalView = (TextView) findViewById( R.id.goalDistanceView );
        mDistanceTravelledView = (TextView) findViewById( R.id.travelledView );
        mDistanceToGoalView = (TextView) findViewById( R.id.goalDistanceView );
        mMenuButton = (FrameLayout) findViewById( R.id.menuButton);

        mTimeView = (TextView) findViewById(R.id.timeOfDayView);

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

/*            FlicManager.getInstance(this, new FlicManagerInitializedCallback() {

                @Override
                public void onInitialized(FlicManager manager) {


                    manager.initiateGrabButton(Controller.this);
                    Log.d("Cyril", "Ready to use Flic manager");

                    Controller.this.mFlicManager = manager;

                    // Restore buttons grabbed in a previous run of the activity
                    List<FlicButton> buttons = manager.getKnownButtons();
                    for (FlicButton button : buttons) {
                        String status = null;
                        switch (button.getConnectionStatus()) {
                            case FlicButton.BUTTON_DISCONNECTED:
                                status = "disconnected";
                                break;
                            case FlicButton.BUTTON_CONNECTION_STARTED:
                                status = "connection started";
                                break;
                            case FlicButton.BUTTON_CONNECTION_COMPLETED:
                                status = "connection completed";
                                break;
                        }
                        Log.d("Cyril", "Found an existing Flic button: " + button + ", status: " + status);
                        setButtonCallback(button);
                    }
                }
            });
                FlicManager.getInstance(this, new FlicManagerInitializedCallback() {
                @Override
                public void onInitialized(FlicManager manager) {
                    manager.initiateGrabButton(Controller.this);
                }
            });
        } catch (FlicAppNotInstalledException err) {
            Toast.makeText(this, "Flic App is not installed", Toast.LENGTH_SHORT).show();
        }
        */

        //updateThread.start();

        startRecording();

        //mMenuButton.setOnTouchListener(this);
        mContentView.setOnTouchListener(this);

    }

/*    private void setButtonCallback(FlicButton button) {
        button.removeAllFlicButtonCallbacks();
        button.addFlicButtonCallback(buttonCallback);
        button.setFlicButtonCallbackFlags(FlicButtonCallbackFlags.UP_OR_DOWN);
        button.setActiveMode(true);
    }
    private FlicButtonCallback buttonCallback = new FlicButtonCallback() {
        @Override
        public void onButtonUpOrDown(FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
            //final String text = button + " was " + (isDown ? "pressed" : "released");
            //Log.d("Cyril", text);
            final boolean down = isDown;
            final String id  = button.getButtonId();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (down) {
                        Log.d("Cyril","Flic: " + id);
                        if (id.equals("80:e4:da:71:86:c3") || id.equals("80:e4:da:71:4c:a8") ){
                            mTrackRecorder.hitAction();
                        } else if (id.equals("80:e4:da:71:5a:7e")) {
                            mTrackRecorder.hitToggleMode();
                        } else if (id.equals("80:e4:da:71:ae:21")) {
                            mTrackRecorder.hitPrevious();
                        } else if (id.equals("80:e4:da:71:3c:23")) {
                            mTrackRecorder.hitNext();
                        }
                        else {
                            Log.d("Cyril", "UNKNOWN FLIC:" + id);
                        }
                    }
                }
            });
        }
    };
    */


    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.android.music.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.android.music.musicservicecommand.next";

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            Log.d("Cyril","mIntentReceiver.onReceive " + action + " / " + cmd);
            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                Log.d("Cyril","NEXT");
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
                Log.d("Cyril","PREV");
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
                    Log.d("Cyril","PAUSEPLAY");
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                Log.d("Cyril","PAUSE");
            } else if (CMDPLAY.equals(cmd)) {
                //play();
                Log.d("Cyril","PLAY");
            } else if (CMDSTOP.equals(cmd)) {
                Log.d("Cyril","STOP");
            }
        }
    };





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

    private boolean inTargetMode() {
        TrackRecorder.Mode mode = mTrackRecorder.getCurrentMode();
        return ( mode != TrackRecorder.Mode.Roadbook && mode != TrackRecorder.Mode.Backtracking_Free && mode != TrackRecorder.Mode.Freeriding );
    }



        private void updateText( Boolean newLocation ) {

            long now = java.lang.System.currentTimeMillis();
        mRecordingTimeView.setText(mTrackRecorder.formatTime(now - mTrackRecorder.getTrack().BaseTime,true));

        TrackRecorder.Mode mode = mTrackRecorder.getCurrentMode();
        if (!inTargetMode()) {
            mCurrentTargetLabelView.setText( "Roadbook");
            mCurrentTargetView.setText( Integer.toString(mTrackRecorder.getFocusedMarkerNo()) + "/" + Integer.toString(mTrackRecorder.getMarkerCount() ) );
        }
        else {
            mCurrentTargetLabelView.setText( "Waypoint");
            mCurrentTargetView.setText( Integer.toString(mTrackRecorder.getFocusedWayPointNo()) + "/" + Integer.toString(mTrackRecorder.getWayPointCount() ) );
        }

        mCurrentModeView.setText( mTrackRecorder.getCurrentModeString() );
        mTimeView.setText( mTrackRecorder.formatTime(mTrackRecorder.getTimeOfDay(),true) );

        Location first = mTrackRecorder.getFirstLocation();

        if (newLocation) {
            Location location = mTrackRecorder.getLastKnownLocation();


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
            mOffsetLatitudeView.setText(mTrackRecorder.formatDistance(location.distanceTo(originLat),false));
            mOffsetLongitudeView.setText(mTrackRecorder.formatDistance(location.distanceTo(originLong),false));


            mDistanceTravelledView.setText(mTrackRecorder.formatDistance(first.distanceTo(location),false));
            mLocationCountView.setText(Long.toString(mTrackRecorder.getLocationCount()));

            mDistanceToGoalView.setText( "n/a" );




        }

        WayPoint wp = null;

        if (inTargetMode() && first != null) {
            wp = mTrackRecorder.getFocusedWayPoint();
            if (wp != null) {
                Location here = mTrackRecorder.getLastKnownLocation();
                Location target = wp.getLocation(here);
                double targetDistance = target.distanceTo(here);
                mDistanceLabelView.setText("Target distance");

                mDistanceView.setText(mTrackRecorder.formatDistance(targetDistance,false));

                mTargetLatitudeView.setText(Double.toString(wp.Latitude));
                mTargetLongitudeView.setText(Double.toString(wp.Longitude));
                Location hereLat = new Location(target);
                hereLat.setLongitude(here.getLongitude());
                Location hereLong = new Location(target);
                hereLong.setLatitude(here.getLatitude());

                mTargetOffsetLatitudeView.setText(mTrackRecorder.formatDistance(target.distanceTo(hereLat), false));
                mTargetOffsetLongitudeView.setText(mTrackRecorder.formatDistance(target.distanceTo(hereLong), false));
            }
        }
        if (wp == null) {
            mDistanceView.setText(mTrackRecorder.formatDistance(mTrackRecorder.getTravelDistance(), true));
//            mDistanceView.setText("n/a");
            mDistanceLabelView.setText("Trip");
            mTargetLatitudeView.setText("n/a");
            mTargetLongitudeView.setText("n/a");
            mTargetOffsetLatitudeView.setText("n/a");
            mTargetOffsetLongitudeView.setText("n/a");
        }



    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        /*
        FlicManager.getInstance(this, new FlicManagerInitializedCallback() {
            @Override
            public void onInitialized(FlicManager manager) {
                FlicButton button = manager.completeGrabButton(requestCode, resultCode, data);
                if (button != null) {
                    button.registerListenForBroadcast(FlicBroadcastReceiverFlags.UP_OR_DOWN | FlicBroadcastReceiverFlags.REMOVED);
                    Toast.makeText(Controller.this, "Grabbed a button", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Controller.this, "Did not grab any button", Toast.LENGTH_SHORT).show();
                }
            }
        });
        */
    }



    void onClickModeButton(View v) {
        Log.d("Cyril","MODE TOGGLE ACTION!");
        mTrackRecorder.hitToggleMode();
    }

    void onClickActionButton(View v) {
        Log.d("Cyril","CLICKED ACTION!");
        mTrackRecorder.hitAction();
    }

    void onClickPreviousButton(View v) {
        mTrackRecorder.hitPrevious();
    }

    void onClickNextButton(View v) {
        mTrackRecorder.hitNext();
    }







    @Override
    public boolean onKeyDown(int keyCode,KeyEvent e) {
        mLatitudeView.setText( Integer.toString(keyCode));

        switch (keyCode ) {
            case (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE):
            case (KeyEvent.KEYCODE_BACK):
            {
                mTrackRecorder.hitAction();
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

        return false;
    }

    public void Reset() {
        mTrackRecorder.Reset();
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
