package com.example.jack.cyril;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Bundle;
/*
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
*/
import android.util.Log;

public class WavAudioRecorder { // implements RecognitionListener {
    private final static int[] sampleRates = {48000, 44100, 22050, 11025, 16000, 8000};
    //private Yin mPitchDetector;

    /*
    private SpeechRecognizer mSpeech = null;
    private Intent recognizerIntent;


    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i("Cyril", "onEvent");
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";

        Log.d("Cyril","Got speech " + text);
    }

    @Override
    public void onReadyForSpeech( Bundle b ) {
        Log.d("Cyril","Speech onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("Cyril","Speech onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float f) {
        Log.d("Cyril","Speech onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("Cyril","Speech onBufferReceived " );
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Cyril","onEndOfSpeech");
    }

    @Override
    public void onError(int i) {
        Log.d("Cyril","Speech error " + i);
    }

    @Override
    public void onPartialResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";

        Log.d("Cyril","Got partial speech " + text);
    }
    */

    public static WavAudioRecorder getInstance(Context context) {

        WavAudioRecorder result = null;
        int i=0;
        do {
            result = new WavAudioRecorder(context,AudioSource.MIC,
                    sampleRates[i],
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
        } while((++i<sampleRates.length) & !(result.getState() == WavAudioRecorder.State.INITIALIZING));
        Log.d("Cyril","Sample rate is " + result.audioRecorder.getSampleRate());


        return result;
    }

    /**
     * INITIALIZING : recorder is initializing;
     * READY : recorder has been initialized, recorder not yet started
     * RECORDING : recording
     * ERROR : reconstruction needed
     * STOPPED: reset needed
     */
    public enum State {INITIALIZING, READY, RECORDING, ERROR, STOPPED};

   // public static final boolean RECORDING_UNCOMPRESSED = true;
  //  public static final boolean RECORDING_COMPRESSED = false;

    // The interval in which the recorded samples are output to the file
    // Used only in uncompressed mode
    private static final int TIMER_INTERVAL = 120;

    // Recorder used for uncompressed recording
    private AudioRecord     audioRecorder = null;

    // Output file path
    private String          filePath = null;

    // Recorder state; see State
    private State          	state;

    // File writer (only in uncompressed mode)
    private RandomAccessFile randomAccessWriter;

    // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
    private short                    nChannels;
    private int                      sRate;
    private short                    mBitsPerSample;
    private int                      mBufferSize;
    private int                      mAudioSource;
    private int                      aFormat;

    // Number of frames/samples written to file on each output(only in uncompressed mode)
    private int                      mPeriodInFrames;

    // Buffer for output(only in uncompressed mode)
    private byte[]                   buffer;

    // Number of bytes written to file after header(only in uncompressed mode)
    // after stop() is called, this size is written to the header/data chunk in the wave file
    private int                      payloadSize;

    /**
     *
     * Returns the state of the recorder in a WavAudioRecorder.State typed object.
     * Useful, as no exceptions are thrown.
     *
     * @return recorder state
     */
    public State getState() {
        return state;
    }

    private AudioRecord.OnRecordPositionUpdateListener externalUpdateListener;

    public void setUpdateListener(AudioRecord.OnRecordPositionUpdateListener l) {
        externalUpdateListener = l;
    }

    //private boolean mLastOneWasLoud = false;

    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        //	periodic updates on the progress of the record head
        public void onPeriodicNotification(AudioRecord recorder) {
            if (State.STOPPED == state) {
                Log.d("Cyril", "recorder stopped");
                return;
            }
            if (externalUpdateListener != null ) {
                externalUpdateListener.onPeriodicNotification(recorder);
            }
            int numOfBytes = audioRecorder.read(buffer, 0, buffer.length); // read audio data to buffer
            //short[] copy = new short[numOfBytes];
            //for (int i=0;i<1000;i++) {
            //    copy[i] = (short)buffer[i];
            //}

            long cnt = 0;
            for (int i=0;i<(numOfBytes-2);i+=2) {
                ByteBuffer bb = ByteBuffer.allocate(2);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                bb.put(buffer[i]);
                bb.put(buffer[i+1]);
                short shortVal = bb.getShort(0);
                if (shortVal >= 30000 ) {
                    cnt += 1;
                }
            }
            if (cnt >= 50) {
                Log.d("Cyril","Loud! " + Long.toString(cnt));
            }



			//Log.d("Cyril", state + ":" + numOfBytes);
            try {
                randomAccessWriter.write(buffer); 		  // write audio data to file
                payloadSize += buffer.length;
            } catch (IOException e) {
                Log.e("Cyril", "Error occured in updateListener, recording is aborted");
                e.printStackTrace();
            }
            //Log.d("Cyril", Float.toString( mPitchDetector.getPitch(copy)));
        }


        //	reached a notification marker set by setNotificationMarkerPosition(int)
        public void onMarkerReached(AudioRecord recorder) {
            if (externalUpdateListener != null) {
                externalUpdateListener.onMarkerReached(recorder);
            }
        }
    };

    /**
     *
     *
     * Default constructor
     *
     * Instantiates a new recorder
     * In case of errors, no exception is thrown, but the state is set to ERROR
     *
     */
    public WavAudioRecorder(Context context, int audioSource, int sampleRate, int channelConfig, int audioFormat) {

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.startBluetoothSco();

        try {
            if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                mBitsPerSample = 16;
            } else {
                mBitsPerSample = 8;
            }

            if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
                nChannels = 1;
            } else {
                nChannels = 2;
            }

            mAudioSource = audioSource;
            sRate   = sampleRate;
            aFormat = audioFormat;

            mPeriodInFrames = sampleRate * TIMER_INTERVAL / 1000;		//?
            mBufferSize = mPeriodInFrames * 2  * nChannels * mBitsPerSample / 8;		//?
            if (mBufferSize < AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)) {
                // Check to make sure buffer size is not smaller than the smallest allowed one
                mBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
                // Set frame period and timer interval accordingly
                mPeriodInFrames = mBufferSize / ( 2 * mBitsPerSample * nChannels / 8 );
                Log.w("Cyril", "Increasing buffer size to " + Integer.toString(mBufferSize));
            }
            Log.d("Cyril","Buffer size:" + Integer.toString(mBufferSize));

            audioRecorder = new AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, mBufferSize);

            if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new Exception("AudioRecord initialization failed");
            }
            audioRecorder.setRecordPositionUpdateListener(updateListener);
            audioRecorder.setPositionNotificationPeriod(mPeriodInFrames);
            filePath = null;
            state = State.INITIALIZING;

/*
            mSpeech = SpeechRecognizer.createSpeechRecognizer(context);
            mSpeech.setRecognitionListener(this);
            recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mSpeech.startListening(recognizerIntent);
            //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            //        "en");
            //recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
            //        this.getPackageName());
            //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            //        RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
           // RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
           */


        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e("Cyril", e.getMessage());
            } else {
                Log.e("Cyril", "Unknown error occured while initializing recording");
            }
            state = State.ERROR;
        }

        //mPitchDetector = new Yin(audioRecorder.getSampleRate());
    }

    /**
     * Sets output file path, call directly after construction/reset.
     *
     * @param output file path
     *
     */
    public void setOutputFile(String argPath) {
        try {
            if (state == State.INITIALIZING) {
                filePath = argPath;
            }
        } catch (Exception e) {
            if (e.getMessage() != null) {
                Log.e("Cyril", e.getMessage());
            } else {
                Log.e("Cyril", "Unknown error occured while setting output path");
            }
            state = State.ERROR;
        }
    }


    /**
     *
     * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
     * the recorder is set to the ERROR state, which makes a reconstruction necessary.
     * In case uncompressed recording is toggled, the header of the wave file is written.
     * In case of an exception, the state is changed to ERROR
     *
     */
    public void prepare() {
        try {
            if (state == State.INITIALIZING) {
                if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) & (filePath != null)) {
                    // write file header
                    randomAccessWriter = new RandomAccessFile(filePath, "rw");
                    randomAccessWriter.setLength(0); // Set file length to 0, to prevent unexpected behavior in case the file already existed
                    randomAccessWriter.writeBytes("RIFF");
                    randomAccessWriter.writeInt(0); // Final file size not known yet, write 0
                    randomAccessWriter.writeBytes("WAVE");
                    randomAccessWriter.writeBytes("fmt ");
                    randomAccessWriter.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
                    randomAccessWriter.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
                    randomAccessWriter.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
                    randomAccessWriter.writeInt(Integer.reverseBytes(sRate)); // Sample rate
                    randomAccessWriter.writeInt(Integer.reverseBytes(sRate*nChannels*mBitsPerSample/8)); // Byte rate, SampleRate*NumberOfChannels*mBitsPersample/8
                    randomAccessWriter.writeShort(Short.reverseBytes((short)(nChannels*mBitsPerSample/8))); // Block align, NumberOfChannels*mBitsPersample/8
                    randomAccessWriter.writeShort(Short.reverseBytes(mBitsPerSample)); // Bits per sample
                    randomAccessWriter.writeBytes("data");
                    randomAccessWriter.writeInt(0); // Data chunk size not known yet, write 0
                    buffer = new byte[mPeriodInFrames*mBitsPerSample/8*nChannels];
                    state = State.READY;
                } else {
                    Log.e("Cyril", "prepare() method called on uninitialized recorder");
                    state = State.ERROR;
                }
            } else {
                Log.e("Cyril", "prepare() method called on illegal state");
                release();
                state = State.ERROR;
            }
        } catch(Exception e) {
            if (e.getMessage() != null) {
                Log.e("Cyril", e.getMessage());
            } else {
                Log.e("Cyril", "Unknown error occurred in prepare()");
            }
            state = State.ERROR;
        }
    }

    /**
     *
     *
     *  Releases the resources associated with this class, and removes the unnecessary files, when necessary
     *
     */
    public void release() {
        if (state == State.RECORDING) {
            stop();
        } else {
            if (state == State.READY){
                try {
                    randomAccessWriter.close(); // Remove prepared file
                } catch (IOException e) {
                    Log.e("Cyril", "I/O exception occured while closing output file");
                }
                (new File(filePath)).delete();
            }
        }

        if (audioRecorder != null) {
            audioRecorder.release();
        }
    }

    /**
     *
     *
     * Resets the recorder to the INITIALIZING state, as if it was just created.
     * In case the class was in RECORDING state, the recording is stopped.
     * In case of exceptions the class is set to the ERROR state.
     *
     */
    public void reset() {
        try {
            if (state != State.ERROR) {
                release();
                filePath = null; // Reset file path
                audioRecorder = new AudioRecord(mAudioSource, sRate, nChannels, aFormat, mBufferSize);
                if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                    throw new Exception("AudioRecord initialization failed");
                }
                audioRecorder.setRecordPositionUpdateListener(updateListener);
                audioRecorder.setPositionNotificationPeriod(mPeriodInFrames);
                state = State.INITIALIZING;
            }
        } catch (Exception e) {
            Log.e("Cyril", e.getMessage());
            state = State.ERROR;
        }
    }

    /**
     *
     *
     * Starts the recording, and sets the state to RECORDING.
     * Call after prepare().
     *
     */
    public void start() {
        if (state == State.READY) {
            payloadSize = 0;
            audioRecorder.startRecording();
            audioRecorder.read(buffer, 0, buffer.length);	//[TODO: is this necessary]read the existing data in audio hardware, but don't do anything
            state = State.RECORDING;
        } else {
            Log.e("Cyril", "start() called on illegal state");
            state = State.ERROR;
        }
    }

    public void checkPoint() {
        try {
            randomAccessWriter.seek(4); // Write size to RIFF header
            randomAccessWriter.writeInt(Integer.reverseBytes(36+payloadSize));

            randomAccessWriter.seek(40); // Write size to Subchunk2Size field
            randomAccessWriter.writeInt(Integer.reverseBytes(payloadSize));
            randomAccessWriter.seek(randomAccessWriter.length());
        } catch(IOException e) {
            Log.e("Cyril", "I/O exception occured while writing to output file");
            state = State.ERROR;
        }
        Log.d("Cyril","Audio checkpoint");
    }
    /**
     *
     *
     *  Stops the recording, and sets the state to STOPPED.
     * In case of further usage, a reset is needed.
     * Also finalizes the wave file in case of uncompressed recording.
     *
     */
    public void stop() {
        if (state == State.RECORDING) {
            Log.d("Cyril","stop()");
            audioRecorder.stop();
            try {
                checkPoint();

                randomAccessWriter.close();
            } catch(IOException e) {
                Log.e("Cyril", "I/O exception occured while closing output file");
                state = State.ERROR;
            }
            state = State.STOPPED;
        } else {
            Log.e("Cyril", "stop() called on illegal state");
            state = State.ERROR;
        }
    }
}