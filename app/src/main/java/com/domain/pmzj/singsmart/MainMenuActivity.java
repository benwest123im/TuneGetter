package com.domain.pmzj.singsmart;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

class OnsetPitchPair {
    public long onsetTimeMs;
    public double pitchInHz;
}

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainMenuActivity extends AppCompatActivity {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;

    private Button mRecordButton = null;
    private MediaRecorder mRecorder = null;

    private Button   mPlayButton = null;
    private MediaPlayer mPlayer = null;

    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    RelativeLayout fLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        setContentView(R.layout.activity_main_menu);

        mRecordButton = (Button)findViewById(R.id.record);
        mPlayButton = (Button)findViewById(R.id.play);

        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    mRecordButton.setText("Stop recording");
                } else {
                    mRecordButton.setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        });

        mRecordButton.setText("Start recording");
        mPlayButton.setText("Start playing");

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    mPlayButton.setText("Stop playing");
                } else {
                    mPlayButton.setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        });

        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView text = (TextView) findViewById(R.id.textView);
                        text.setText(" "+pitchInHz+" ");
                    }
                });
                timePitch.add(((double)pitchInHz));
            }
        };

        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();
    }







    ArrayList<Double> timePitch = new ArrayList<>();
    double relativnaPravilnost = 0;

    //poveda v centih koliko si dalec
    private double tuningCent( double refPitch, double measPitch ){ //reference pitch and measured pitch
        return 3986*Math.log10(measPitch/refPitch);
    }

    //gre cez seynam in porihta
    private double[] evaluate(OnsetPitchPair[] opp){

        double[] measure = new double[timePitch.size()];
        double tms = 0;    //time in miliseconds
        relativnaPravilnost = 0;
        int st = 0;
        int stPravilnih = 0;
        for(int i = 0; i<timePitch.size();i++){
            tms += (1000 * 1024/22050);    //time in miliseconds
            int j = 0; //j bo index za nas trenutni refrencePitch
            while( opp[j].onsetTimeMs < tms )j++;
            //measure that shit
            measure[i] = tuningCent(opp[j].pitchInHz, timePitch.get(i));
            if( Math.abs(measure[i]) < 5 ) stPravilnih++;
        }

        relativnaPravilnost = stPravilnih / timePitch.size();

        return measure;
    }





    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

}
