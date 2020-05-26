package com.dsproject.musicstreamingservice.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dsproject.musicstreamingservice.R;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment
{
    private Context fragContext;
    private View view;

    private TextInputLayout connection_input_field;
    private Button connect;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        getActivityElements();

        connection_input_field = (TextInputLayout) view.findViewById(R.id.connectionInputLayout);
        connect = (Button) view.findViewById(R.id.connectButton);

        connect.setOnClickListener(view -> {
            String credentials = Objects.requireNonNull(connection_input_field.getEditText()).getText().toString().trim();
            if(credentials.indexOf('@')!=-1 && credentials.indexOf('@')<credentials.length()) {
                String ip = credentials.substring(0, credentials.indexOf('@'));
                int port = Integer.parseInt(credentials.substring(credentials.indexOf('@') + 1));

                Toast.makeText(fragContext,"Connecting...", Toast.LENGTH_SHORT).show();

                try (FileOutputStream fos = fragContext.openFileOutput("BrokerCredentials.txt",MODE_PRIVATE)){
                    fos.write(("").getBytes());
                    fos.write((ip+"@"+port).getBytes());
                    Toast.makeText(fragContext,"Connected to broker", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(fragContext,"Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getActivityElements()
    {
        fragContext = getActivity().getApplicationContext();
        view = getView();
        if(view == null || fragContext == null){
            throw new IllegalStateException("Couldn't get view or context from fragment.");
        }
    }
}
