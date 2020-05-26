package com.dsproject.musicstreamingservice.ui.irrelevantActivities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
                "\n\nPetros Papatheodorou 3170133" +
                "\n\nElena Mina 3170108" +
                "\n\nEleni Saxoni 3160270" +
                "\n\nIlias Manginas 3170096");
    }
}
