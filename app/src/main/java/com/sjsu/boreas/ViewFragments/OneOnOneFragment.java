package com.sjsu.boreas.ViewFragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sjsu.boreas.ChatViewRelatedStuff.OneOnOneChatViewAdapter;
import com.sjsu.boreas.LandingPage;
import com.sjsu.boreas.R;
import com.sjsu.boreas.messaging.ChatActivity;
import com.sjsu.boreas.messaging.ChatMessageAdapter;

public class OneOnOneFragment extends Fragment {
    public static final String EXTRA_TAB_NAME = "tab_name";
    private String mTabName;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LandingPage mParent;
    private View rootView;
    private Context mContext;

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "---OneOnOne___Frag ";

    public OneOnOneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG+"On create");
        super.onCreate(savedInstanceState);
        mParent = (LandingPage) getActivity();
    }

    public static OneOnOneFragment newInstance(String tabName) {
        Log.e(TAG, SUB_TAG+"OneOnOneFragment");
        Bundle args = new Bundle();
        args.putString(EXTRA_TAB_NAME, tabName);
        OneOnOneFragment fragment = new OneOnOneFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        Log.e(TAG, SUB_TAG+"onAttach");
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, SUB_TAG+"onCreateView");
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_one_on_one, container, false);
        mTabName = getArguments().getString(EXTRA_TAB_NAME);
        mContext = container.getContext();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI();
    }

    private void initUI() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(mParent);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new OneOnOneChatViewAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
    }
}
