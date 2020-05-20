package com.dsproject.musicstreamingservice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dsproject.musicstreamingservice.R;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button customReq = (Button) findViewById(R.id.customReq);
        customReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go to Activity2
                Log.i("DEBUG", "here");
                Intent myIntent = new Intent(view.getContext(), CustomRequestActivity.class);
                startActivity(myIntent);
            }
        });
    }
}