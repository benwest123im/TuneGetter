package com.domain.pmzj.singsmart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


    }

    public void onClickLogin(View view) {
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }
}
