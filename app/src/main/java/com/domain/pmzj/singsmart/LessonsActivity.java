package com.domain.pmzj.singsmart;

import android.app.ListActivity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import objects.UserResult;
import utils.Params;

public class LessonsActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons);

        List<UserResult> listResult = getResultsForUser(Params.CURR_USER_ID);
        List<String> scores = new ArrayList<String>();
        for(int i = 0; i < Params.NUM_LESSONS; i++)
            scores.add("Lesson " + i);

        for(UserResult ur : listResult) {
            if (ur.getChallengeId() < Params.NUM_LESSONS) {
                int ind = ur.getChallengeId();
                scores.set(ind, scores.get(ind) + ": " + ur.getScore());
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                scores);

        // Bind to our new adapter.
        setListAdapter(arrayAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(this, PlayActivity.class);
        startActivity(intent);
    }

    private List<UserResult> getResultsForUser(int userId) {
        List<UserResult> userResults = new ArrayList<>();
        String url = Params.SERVER + Params.GET_RESULTS + "?user_id=" + userId;
        try {
            JSONObject result = new HttpHandler.HttpAsyncTaskGet().execute(url).get();
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
}
