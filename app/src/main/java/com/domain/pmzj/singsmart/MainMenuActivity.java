package com.domain.pmzj.singsmart;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.util.MidiUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

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
                        text.setText("" + pitchInHz);
                    }
                });
            }
        };
        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(p);
        new Thread(dispatcher,"Audio Dispatcher").start();


        //TEST: test parsing here

        File input = new File("assets/queen_voice_only_popravek.mid");
        OnsetPitchPair[] result = parseMIDI_file(input);
        for (int i = 0; i < result.length; i++) {
            Log.d("Polje "+ i + ": ",result[i]);
        }
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

    public double midiNoteToHz(int midiNote) {
        return Math.pow(2, (midiNote - 69) / 12)*440;
    }


    public OnsetPitchPair[] parseMIDI_file(File input) {
        ArrayList<OnsetPitchPair> parsedArray = new ArrayList<OnsetPitchPair>();
        MidiFile sourceMidiFile = null;
        try {
            sourceMidiFile = new MidiFile(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int PPQ = sourceMidiFile.getResolution();
        int bpm = 60;

        MidiTrack trackToParse = sourceMidiFile.getTracks().get(0);
        Iterator<MidiEvent> iter = trackToParse.getEvents().iterator();

        double pitchOfCurrent = 0;
        long tickOfCurrent = 0;

        long noteOnset = MidiUtil.ticksToMs(tickOfCurrent, MidiUtil.bpmToMpqn(bpm), PPQ);

        OnsetPitchPair addedPair = new OnsetPitchPair(noteOnset, pitchOfCurrent);
        parsedArray.add(addedPair);

        while(iter.hasNext()) {
            MidiEvent event = iter.next();

            if (event instanceof NoteOn || event instanceof NoteOff) {
                tickOfCurrent = event.getTick();
                noteOnset = MidiUtil.ticksToMs(tickOfCurrent, MidiUtil.bpmToMpqn(bpm), PPQ);

                if (event instanceof NoteOn) {
                    pitchOfCurrent = midiNoteToHz(((NoteOn) event).getNoteValue());
                }
                if (event instanceof NoteOff) {
                    pitchOfCurrent = 0;
                }
                addedPair = new OnsetPitchPair(noteOnset, pitchOfCurrent);
                if (addedPair.onsetTimeMs - parsedArray.get(parsedArray.size()- 1).onsetTimeMs <= 10) {
                    parsedArray.set(parsedArray.size() - 1, addedPair);
                } else {
                    parsedArray.add(addedPair);
                }
            }
        }
        OnsetPitchPair[] returnArray = new OnsetPitchPair[parsedArray.size()];
        for (int i = 0; i < parsedArray.size(); i++) {
            returnArray[i] = parsedArray.get(i);
        }
        return returnArray;
    }
}