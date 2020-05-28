package com.dsproject.musicstreamingservice.ui.fragments;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends GenericFragment
{
    private TextInputLayout connection_input_field;
    private Button connect;

    public SettingsFragment()
    {
        super(MyFragmentManager.getLayoutOf(SettingsFragment.class));
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        connection_input_field = (TextInputLayout) view.findViewById(R.id.connectionInputLayout);
        connect = (Button) view.findViewById(R.id.connectButton);

        //test create redirect notification
//        notifManager = new MyNotificationManager(context);
//        connect.setOnClickListener(view -> {
//            Intent intent = new Intent(context, MainActivity.class);
//            intent.putExtra(MainActivity.REDIRECT_TAG, MyFragmentManager.CUSTOM_REQ_FRAG_NAME);
//            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            notifManager.makeAndShowPersistentNotification("1",
//                    "REDIRECT","fefefef", null, pi);
//        });

        connect.setOnClickListener(view -> {
            String credentials = Objects.requireNonNull(connection_input_field.getEditText()).getText().toString().trim();
            if(credentials.indexOf('@')!=-1 && credentials.indexOf('@')<credentials.length()) {
                String ip = credentials.substring(0, credentials.indexOf('@'));
                int port = Integer.parseInt(credentials.substring(credentials.indexOf('@') + 1));

                Toast.makeText(context,"Connecting...", Toast.LENGTH_SHORT).show();

                try (FileOutputStream fos = context.openFileOutput("BrokerCredentials.txt",MODE_PRIVATE)){
                    fos.write(("").getBytes());
                    fos.write((ip+"@"+port).getBytes());
                    Toast.makeText(context,"Connected to broker", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                Toast.makeText(context,"Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
