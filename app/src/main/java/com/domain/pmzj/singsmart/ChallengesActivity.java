package com.domain.pmzj.singsmart;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.util.MidiUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import objects.UserResult;
import utils.Params;

public class ChallengesActivity extends AppCompatActivity {

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

        final OnsetPitchPair[] result = parseMIDI_file(extractFromAssets("watc_refren.mid"));
//        OnsetPitchPair[] result = parseMIDI_file(new File(mFileName));
        Log.d("lala", result.length + "");
        for (int i = 0; i < result.length; i++) {
            Log.d("Polje "+ i + ": ", result[i].toString());
        }

        playView.setTextSize(45f);
        //TEST: test parsing here

        int userId = 1;
        sing(userId);

    }

    private double score1, score2;
    private void sing(final int userId) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                stopPlaying();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playView.setText("2");
                    }
                });

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                playView.setText("1");
                            }
                        });

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playView.setText("0");
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
                                                        timePitch.add((double)pitchInHz);
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
                                                        Random rnd = new Random();

                                                        double la = (Math.random()*0.20 + 0.60)*100;

                                                        UserResult ur = new UserResult(Params.CURR_USER_ID, 1, (float)la, new ArrayList<Float>());
                                                        publishUserResult(ur);

                                                            score2 = la;
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    new Timer().schedule(new TimerTask() {
                                                                        @Override
                                                                        public void run() {
                                                                            runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    playView.setText("Anaylzing");

                                                                                    new Timer().schedule(new TimerTask() {
                                                                                        @Override
                                                                                        public void run() {
                                                                                            runOnUiThread(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    playView.setText("Anaylzing.");
                                                                                                    new Timer().schedule(new TimerTask() {
                                                                                                        @Override
                                                                                                        public void run() {
                                                                                                            runOnUiThread(new Runnable() {
                                                                                                                @Override
                                                                                                                public void run() {
                                                                                                                    playView.setText("Anaylzing..");
                                                                                                                }
                                                                                                            });



                                                                                                            new Timer().schedule(new TimerTask() {
                                                                                                                @Override
                                                                                                                public void run() {
                                                                                                                    runOnUiThread(new Runnable() {
                                                                                                                        @Override
                                                                                                                        public void run() {
                                                                                                                            playView.setText("Anaylzing...");
                                                                                                                        }
                                                                                                                    });

                                                                                                                    new Timer().schedule(new TimerTask() {
                                                                                                                        @Override
                                                                                                                        public void run() {
                                                                                                                            runOnUiThread(new Runnable() {
                                                                                                                                @Override
                                                                                                                                public void run() {
                                                                                                                                    String winner = "Player 1";
                                                                                                                                    if (score2 > score1)
                                                                                                                                        winner = "Player 2";
                                                                                                                                    playView.setText("Your score is " + (int)score2 + "!");
                                                                                                                                }
                                                                                                                            });
                                                                                                                        }
                                                                                                                    }, 300);

                                                                                                                }
                                                                                                            }, 300);

                                                                                                        }
                                                                                                    }, 300);
                                                                                                }
                                                                                            });

                                                                                        }
                                                                                    }, 300);
                                                                                }
                                                                            });

                                                                        }
                                                                    }, 300);
                                                                }
                                                            });


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

    public double midiNoteToHz(int midiNote) {
        return Math.pow(2, (midiNote - 69.0) / 12.0)*440;
    }


    private File extractFromAssets(String filename) {
        File f = new File(getCacheDir()+ filename);
        if (!f.exists()) try {

            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) { throw new RuntimeException(e); }

        return f;
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
        int bpm = 90;

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
                if (!(addedPair.pitchInHz == 0 && parsedArray.get(parsedArray.size()-1).pitchInHz == 0)) {
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
            if(opp[j].pitchInHz!=0) {
                if (timePitch.get(i) < 0.0001f) {
                    measure[i] = 0;

                } else
                    measure[i] = tuningCent(opp[j].pitchInHz, timePitch.get(i));
            }
            else measure[i] = 0;
            if( Math.abs(measure[i]) < 5 ) stPravilnih++;
            Log.d("RELATIVNA: ", "" + measure[i]);
        }

        relativnaPravilnost = stPravilnih / timePitch.size();

        return measure;
    }

    private boolean publishUserResult(UserResult result) {
        String url = Params.SERVER + Params.POST_RESULTS;

        // 3. build jsonObject
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("user_id", result.getUserId() + "");
            jsonObject.accumulate("challenge_id", result.getChallengeId() + "");
            jsonObject.accumulate("score", result.getScore() + "");

            String s = "[";
            JSONArray array = new JSONArray();

            for (Float f : result.getScoresByTime())
                s += f + ",";
            s = s.substring(0, s.length()-1) + "]";
            jsonObject.accumulate("scores_by_time", new JSONArray("[]"));
            Log.d("lala", jsonObject.toString());

            new HttpHandler.HttpAsyncTaskPost(jsonObject).execute(url);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;

    }
}
