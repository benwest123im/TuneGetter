package com.domain.pmzj.singsmart;

import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class PlayActivity extends AppCompatActivity {

    MediaPlayer mPlayer;
    String mFileName;
    TextView playView;
    RelativeLayout rLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playView = (TextView)findViewById(R.id.playing_view);
        rLayout = (RelativeLayout)findViewById(R.id.rLayout);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/Download/Queen_VoiceOnly_Popravek_Refren.mid";

        startPlaying();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stopPlaying();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playView.setText("Get ready ...");
                    }
                });

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playView.setText("Steady ...");
                            }
                        });

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playView.setText("Go!");
                                        rLayout.setBackgroundColor(0xFF0000);

                                        final List<Float> lst = new ArrayList<Float>();
                                        final AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

                                        PitchDetectionHandler pdh = new PitchDetectionHandler() {
                                            @Override
                                            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                                                final float pitchInHz = result.getPitch();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        playView.setText("" + pitchInHz);
                                                        lst.add(pitchInHz);
                                                    }
                                                });
                                            }
                                        };
                                        AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
                                        dispatcher.addAudioProcessor(p);
                                        final Thread t = new Thread(dispatcher,"Audio Dispatcher");
                                        t.start();

                                        new Timer().schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                dispatcher.stop();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        playView.setText("SCORE");
                                                    }
                                                });
                                            }
                                        }, 5000);
                                    }
                                });
                            }
                        }, 1000);
                    }
                }, 1000);
            }
        }, 5000);



    }


    @Override
    protected void onPause() {
        super.onPause();
        stopPlaying();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("PLAY", "prepare() failed");
        }
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}
