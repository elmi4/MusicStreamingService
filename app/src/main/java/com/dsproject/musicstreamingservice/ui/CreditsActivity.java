package com.dsproject.musicstreamingservice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.dsproject.musicstreamingservice.R;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);
        Button backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            //go to Activity2
            Log.i("DEBUG", "here");
            Intent myIntent = new Intent(view.getContext(), MainActivity.class);
            startActivity(myIntent);
        });
    }
}
