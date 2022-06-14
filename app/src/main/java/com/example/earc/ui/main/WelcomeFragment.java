package com.example.earc.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager.widget.ViewPager;

import com.example.earc.R;
import com.example.earc.func.adapter.ViewPagerAdapter;
import com.example.earc.func.record.SoundRecorder;
import com.lwj.widget.viewpagerindicator.ViewPagerIndicator;

import java.util.ArrayList;

public class WelcomeFragment extends Fragment {

    private View mView;

    private NavController mNavController;

    private Button mUnderstand;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.welcome_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mView = view;

        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        this.mNavController = navHostFragment.getNavController();

        this.initUnderstandButton();

        this.initViewPagerAdapter();

        checkAudioRecordPermission();
    }

    private void initUnderstandButton() {
        this.mUnderstand = this.mView.findViewById(R.id.welcome_understand);

        this.mUnderstand.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getActivity().getPreferences(Activity.MODE_PRIVATE);

            sharedPreferences.edit().putBoolean("isNeedToShowWelcomePage", false).apply();

            mNavController.navigate(R.id.action_welcome_fragment_to_main_fragment);
        });
    }

    private void initViewPagerAdapter() {
        ArrayList<Fragment> fragments = new ArrayList<>();

        for (int i = 0; i < 2; i++) fragments.add(new ViewPagerFragment(i));

        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(), fragments);

        ViewPager viewPager = this.mView.findViewById(R.id.welcome_view_pager);

        viewPager.setAdapter(adapter);

        ViewPagerIndicator viewPagerIndicator = this.mView.findViewById(R.id.welcome_indicator);

        viewPagerIndicator.setViewPager(viewPager);
    }

    private void checkAudioRecordPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), SoundRecorder.PERMISSIONS_RECORD_AUDIO, SoundRecorder.RECORD_AUDIO_REQUEST_CODE);
        }
    }
}