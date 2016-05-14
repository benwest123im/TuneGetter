package com.domain.pmzj.singsmart;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import objects.UserResult;
import utils.Params;

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

    private Button sendButton = null;
    private Button getButton = null;
    boolean mStartRecording = true;
    boolean mStartPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";

        setContentView(R.layout.activity_main_menu);

        mRecordButton = (Button)findViewById(R.id.record);
        mPlayButton = (Button)findViewById(R.id.play);
        sendButton = (Button)findViewById(R.id.send_button);
        getButton = (Button)findViewById(R.id.get_button);

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

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float score = 0.7f;
                int challengeId = 2;
                int userId = 1;
                float[] tmp = new float[]{0.3f, 0f, 0.9f, 1.0f};
                List<Float> lst = new ArrayList<Float>();
                for (int i = 0; i < tmp.length; i++)
                    lst.add(tmp[i]);

                UserResult userResult = new UserResult(userId, challengeId, score, lst);

                boolean res = publishUserResult(userResult);
            }
        });

        getButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<UserResult> res = getResultsForUser(Params.CURR_USER_ID);
                Log.d(LOG_TAG, res.toString());

            }
        });

//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                new HttpAsyncTaskPost().execute("http://52.37.232.67/results?user_id=1");
//
//            }
//        }, 0, 1000);

    }

    private List<UserResult> getResultsForUser(int userId) {
        List<UserResult> userResults = new ArrayList<>();
        String url = Params.SERVER + Params.GET_RESULTS + "?user_id=" + userId;
        try {
            JSONObject result = new HttpAsyncTaskGet().execute(url).get();
            JSONArray results = result.getJSONArray("results");

            if (results != null) {
                for (int k = 0; k < results.length(); k++) {
                    JSONObject jo = results.getJSONObject(k);

                    String score = jo.getString("score");
                    String challengeId = jo.getString("challenge_id");
                    JSONArray scores = jo.getJSONArray("scores_by_time");

                    List<Float> listResults = new ArrayList<Float>();
                    if (scores != null) {
                        for (int i = 0; i <scores.length(); i++)
                            listResults.add(Float.parseFloat(scores.get(i).toString()));
                    }
                    userResults.add(new UserResult(userId, Integer.parseInt(challengeId), Float.parseFloat(score), listResults));
                }
            }
            return userResults;
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
            return null;
        }
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
            jsonObject.accumulate("scores_by_time", new JSONArray(s));
            Log.d(LOG_TAG, jsonObject.toString());

            new HttpAsyncTaskPost(jsonObject).execute(url);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private class HttpAsyncTaskPost extends AsyncTask<String, Void, JSONObject> {
        private JSONObject toSend;

        public HttpAsyncTaskPost(JSONObject toSend) {
            this.toSend = toSend;
        }

        @Override
        protected JSONObject doInBackground(String... urls) {

            try {
                return HttpHandler.POST(urls[0], toSend);
            } catch (JSONException e) {
                e.printStackTrace();
                return new JSONObject();
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(JSONObject result) {
            Toast.makeText(getBaseContext(), "Data Sent!", Toast.LENGTH_LONG).show();
        }
    }

    // Task for receiving
    private class HttpAsyncTaskGet extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... urls) {
            try {
                return HttpHandler.GET(urls[0]);
            } catch (JSONException e) {
                return new JSONObject();
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, result.toString());
        }
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
