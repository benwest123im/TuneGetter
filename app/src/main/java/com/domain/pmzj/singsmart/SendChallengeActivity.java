package com.domain.pmzj.singsmart;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import objects.Challenge;
import utils.Params;

public class SendChallengeActivity extends ListActivity {

    public static final String LOG_TAG ="SendChallengeActivity";
    private List<Integer> userIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_challenge);

        userIds = new ArrayList<>();
        List<String> users = new ArrayList<String>();
        for(Integer i : Params.LIST_USERS) {
            if (i != Params.CURR_USER_ID) {
                users.add("User " + i);
                userIds.add(i);
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                users);

        // Bind to our new adapter.
        setListAdapter(arrayAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(LOG_TAG, userIds.get(position) + " ");

        String challengeId = "2";
        int userId = userIds.get(position);
        float[] tmp = new float[]{0.3f, 0f, 0.9f, 1.0f};
        List<Float> lst = new ArrayList<Float>();
        for (int i = 0; i < tmp.length; i++)
            lst.add(tmp[i]);

        Challenge c = new Challenge(Params.CURR_USER_ID, userId, challengeId, lst);

        sendChallenge(c);
    }

    private boolean sendChallenge(Challenge challenge) {
        String url = Params.SERVER + Params.POST_CHALLENGES;

        // 3. build jsonObject
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.accumulate("from_user_id", challenge.getFromUser() + "");
            jsonObject.accumulate("to_user_id", challenge.getToUser() + "");

            String s = "[";
            JSONArray array = new JSONArray();

            for (Float f : challenge.getPitchesByTime())
                s += f + ",";
            s = s.substring(0, s.length()-1) + "]";
            jsonObject.accumulate("pitches_by_time", new JSONArray(s));
            Log.d(LOG_TAG, jsonObject.toString());

            new HttpHandler.HttpAsyncTaskPost(jsonObject).execute(url);

            Toast.makeText(this, "Challenge sent. ", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
