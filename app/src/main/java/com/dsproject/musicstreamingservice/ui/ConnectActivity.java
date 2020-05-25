package com.dsproject.musicstreamingservice.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.managers.setup.ApplicationSetup;

import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import java.util.Objects;

public class ConnectActivity extends AppCompatActivity {

    private TextInputLayout connection_input_field;
    private Button connect;
    private int timeout = 4000;     //time in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        connection_input_field = (TextInputLayout) findViewById(R.id.connectionInputLayout);

        connect = (Button) findViewById(R.id.connectButton);

    }

    @Override
    public void onStart()
    {
        super.onStart();

        connect.setOnClickListener(view -> {

            String credentials = Objects.requireNonNull(connection_input_field.getEditText()).getText().toString().trim();

            if(credentials.indexOf('@')!=-1 && credentials.indexOf('@')<credentials.length()) {
                String ip = credentials.substring(0, credentials.indexOf('@'));
                int port = Integer.parseInt(credentials.substring(credentials.indexOf('@') + 1));



                Toast.makeText(this,"Connecting...", Toast.LENGTH_SHORT).show();


                FileOutputStream fos = null;

                try {
                    fos = openFileOutput("BrokerCredentials.txt",MODE_PRIVATE);
                    fos.write(("").getBytes());
                    fos.write((ip+"@"+port).getBytes());
                    Toast.makeText(this,"Connected to broker", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
            else{
                Toast.makeText(this,"Invalid credentials", Toast.LENGTH_SHORT).show();
            }


        });


    }


}
