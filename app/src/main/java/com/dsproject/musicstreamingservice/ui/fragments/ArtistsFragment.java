package com.dsproject.musicstreamingservice.ui.fragments;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.managers.fragments.MyFragmentManager;

public class ArtistsFragment extends GenericFragment
{
    public ArtistsFragment()
    {
        super(MyFragmentManager.getLayoutOf(ArtistsFragment.class));
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);

        //example code
        Button change = view.findViewById(R.id.test_changeFragBtn);
        change.setOnClickListener(v -> goToFragment(new CustomRequestFragment()));

        Button edit = view.findViewById(R.id.test_editTextBtn);
        EditText txtArea = view.findViewById(R.id.test_textField);

        edit.setOnClickListener(v -> {
            //Create container of data (can send many data with different types too")
            //Here we are taking the input of the editText and passing it as argument with id songName
            Bundle bundle = new Bundle();
            bundle.putString("songName", txtArea.getText().toString().trim());
            
            goToFragmentWithData(bundle, new CustomRequestFragment());
        });
    }

}
