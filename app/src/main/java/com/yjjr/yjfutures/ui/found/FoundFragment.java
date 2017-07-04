package com.yjjr.yjfutures.ui.found;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yjjr.yjfutures.R;
import com.yjjr.yjfutures.ui.BaseFragment;

/**
 * 发现页面
 */
public class FoundFragment extends BaseFragment implements View.OnClickListener {


    public FoundFragment() {
        // Required empty public constructor
    }


    @Override
    protected View initViews(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_found, container, false);
        v.findViewById(R.id.root_view1).setOnClickListener(this);
        v.findViewById(R.id.root_view2).setOnClickListener(this);
        v.findViewById(R.id.root_view3).setOnClickListener(this);
        v.findViewById(R.id.root_view4).setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.root_view1:
                break;
            case R.id.root_view2:
                break;
            case R.id.root_view3:
                break;
            case R.id.root_view4:
                break;
        }
    }
}