package com.example.earc.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.earc.R;

public class ViewPagerFragment extends Fragment {
    private int position;
    private int layoutID;

    public ViewPagerFragment(int position) {
        this.position = position;
        setLayoutID();
    }

    public void setLayoutID() {
        switch (this.position) {
            case 0:
                layoutID = R.layout.pager_fragment_one;
                break;
            case 1:
                layoutID = R.layout.pager_fragment_two;
                break;
            default:
                layoutID = 0;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutID, container, false);
    }
}
