package com.dsproject.musicstreamingservice.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dsproject.musicstreamingservice.R;
import com.dsproject.musicstreamingservice.ui.MainActivity;

public abstract class GenericFragment extends Fragment
{
    protected final int contentLayoutId;
    protected View view;
    protected Context context;

    //used for fragment-activity communication. Activity must implement it.
    public interface DataPassListener { void passData(Bundle data, GenericFragment frag);}

    protected DataPassListener mCallback;
    protected Boolean canReceiveData;


    protected GenericFragment(int contentLayoutId)
    {
        super(contentLayoutId);
        this.contentLayoutId = contentLayoutId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(this.contentLayoutId, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstance)
    {
        super.onActivityCreated(savedInstance);
        fragInit();
    }

    protected <F extends GenericFragment> void goToFragment(final F frag)
    {
        this.getActivity().getSupportFragmentManager().beginTransaction().replace(
                R.id.fragment_container, frag).commit();

        ((MainActivity)getActivity()).changeMenuCheckedItem(frag);
    }

    protected <F extends GenericFragment> void goToFragmentWithData(final Bundle data, final F frag)
    {
        if(!containerCanReceiveData()) {
            throw new IllegalArgumentException("Container activity doesn't implement DataPassListener");
        }

        mCallback.passData(data, frag);

        ((MainActivity)getActivity()).changeMenuCheckedItem(frag);
    }

    protected boolean containerCanReceiveData()
    {
        if(canReceiveData != null){
            return canReceiveData;
        }

        try {
            mCallback = (DataPassListener) getActivity();
            canReceiveData = true;
            return true;
        }catch (Exception e){
            canReceiveData = false;
            return false;
        }
    }

    private void fragInit()
    {
        context = getActivity().getApplicationContext();
        view = getView();
        if(view == null || context == null){
            throw new IllegalStateException("Couldn't get view or context from fragment.");
        }
    }
}
