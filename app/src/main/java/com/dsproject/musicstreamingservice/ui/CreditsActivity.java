package com.dsproject.musicstreamingservice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.dsproject.musicstreamingservice.R;

public class CreditsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        TextView credits = (TextView) findViewById(R.id.creditsText);
        credits.setText("\n\nCredits" +
                "\n\n\nAUEB Distributed Systems Project 2020" +
                "\n\n\nCollaborators" +
                "\n\nPetros Papatheodorou 317xxxx" +
                "\n\nElena Mina 317xxxx" +
                "\n\nEleni Saxoni 3160270" +
                "\n\nIlias Manginas 3170096");
    }
}
