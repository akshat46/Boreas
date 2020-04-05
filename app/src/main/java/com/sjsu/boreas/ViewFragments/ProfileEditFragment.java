package com.sjsu.boreas.ViewFragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sjsu.boreas.ChatNewActivity;
import com.sjsu.boreas.ProfileEditActivity;
import com.sjsu.boreas.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileEditFragment extends Fragment {

    private ProfileEditActivity mParent;
    private View rootView;
    private FloatingActionButton mFabButton;
    private EditText mEditText;

    public static ProfileEditFragment newInstance(Bundle bundle) {
        ProfileEditFragment fragment = new ProfileEditFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParent = (ProfileEditActivity) getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        //loadChats();
    }

    private void initViews() {

    }


}
