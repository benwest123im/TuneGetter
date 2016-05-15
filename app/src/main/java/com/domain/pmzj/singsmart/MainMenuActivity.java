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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import objects.Challenge;
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

    private Button lessonsButton = null;
    private ListView challengeList = null;
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
        lessonsButton = (Button)findViewById(R.id.lessons_button);
        challengeList = (ListView)findViewById(R.id.challengeList);


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


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final List<Challenge> challenges = getChallengesForUser(Params.CURR_USER_ID);
//                final List<Challenge> challenges = new ArrayList<Challenge>();
//
//                float[] tmp = new float[]{0.3f, 0f, 0.9f, 1.0f};
//                List<Float> lst = new ArrayList<Float>();
//                for (int i = 0; i < tmp.length; i++)
//                    lst.add(tmp[i]);
//
//                Challenge c = new Challenge(0, Params.CURR_USER_ID, 1, lst);
//                challenges.add(c);
//
//                if (Math.random() < 0.5) {
//                    Challenge c2 = new Challenge(3, Params.CURR_USER_ID, 5, lst);
//                    challenges.add(c2);
//
//                    if(Math.random() < 0.5)
//                        challenges.add(c2);
//
//                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fillChallengeList(challenges);
                    }
                });
            }
        }, 0, 1000);

    }

    private void fillChallengeList(List<Challenge> challenges) {
        List<String> scores = new ArrayList<String>();

        for(Challenge c : challenges) {
            scores.add("Challenge " + c.getChallengeId() + " from " + c.getFromUser());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                scores);

        // Bind to our new adapter.
        challengeList.setAdapter(arrayAdapter);
    }

    private List<Challenge> getChallengesForUser(int userId) {
        List<Challenge> challengeList = new ArrayList<>();
        String url = Params.SERVER + Params.GET_CHALLENGES + "?to_user_id=" + userId;
        try {
            JSONObject result = new HttpHandler.HttpAsyncTaskGet().execute(url).get();
            JSONArray challenges = result.getJSONArray("challenges");

            if (challenges != null) {
                for (int k = 0; k < challenges.length(); k++) {
                    JSONObject jo = challenges.getJSONObject(k);

                    String from = jo.getString("from_user_id");
                    String to = jo.getString("to_user_id");
                    String challengeId = jo.getString("id");
                    JSONArray scores = jo.getJSONArray("pitches_by_time");

                    List<Float> listResults = new ArrayList<Float>();
                    if (scores != null) {
                        for (int i = 0; i <scores.length(); i++)
                            listResults.add(Float.parseFloat(scores.get(i).toString()));
                    }
                    challengeList.add(new Challenge(Integer.parseInt(from), Integer.parseInt(to), challengeId, listResults));
                }
            }
            return challengeList;
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

            new HttpHandler.HttpAsyncTaskPost(jsonObject).execute(url);
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

    // ON CLICK CALLBACKS
    public void onClickLessons(View view) {
        Intent intent = new Intent(this, LessonsActivity.class);
        startActivity(intent);
    }

    public void onClickSendChallenge(View view) {
        Intent intent = new Intent(this, SendChallengeActivity.class);
        startActivity(intent);

    }
}
