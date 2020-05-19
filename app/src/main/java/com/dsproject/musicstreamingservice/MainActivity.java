package com.dsproject.musicstreamingservice;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;

import com.dsproject.musicstreamingservice.assist.Utilities;
import com.dsproject.musicstreamingservice.assist.network.ConnectionInfo;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button testBtn = (Button) findViewById(R.id.buttontest);
        testBtn.setOnClickListener((v -> {
            TestAsync t = new TestAsync();
            t.execute("Jason Shaw");
        }));
    }

    @SuppressLint("StaticFieldLeak")
    private class TestAsync extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings) {
            Consumer cons = new Consumer(ConnectionInfo.of(Utilities.getCustomIP(), 4032), MainActivity.this);

            cons.init();
            System.out.println(cons.requestSongsOfArtist(strings[0]).size());

            return "all good";
        }
    }
}
