package com.example.earc.ui.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.earc.R;
import com.example.earc.func.data.DataBarChart;
import com.example.earc.func.data.DataBaseHelper;
import com.example.earc.func.data.DataModel;
import com.example.earc.func.record.SoundRecorder;
import com.example.earc.func.service.ForeService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.MultiplePulseRing;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.john.waveview.WaveView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainFragment extends Fragment {
    private final int FADE_OUT_DURATION = 1000;

    private final int FADE_IN_DURATION = 1000;

    private final int WAVE_LEVEL = 30;

    private MainViewModel mViewModel;

    private View mView;

    private ConstraintLayout mBottomSheet;

    private Button mSetting;

    private Button mHelping;

    private BottomSheetBehavior mBottomSheetBehavior;

    private SwipeRefreshLayout mSwipeRefresh;

    private ProgressBar mSpinKit;

    private ImageView mCircle;

    private WaveView mWaveView;

    private TextView mText;

    private BarChart mBarChart;

    private LineChart mLineChart;

    private boolean mIsAnalyzing = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mView = view;

        this.checkIsNeedToShowWelcomePage();

        this.checkIsServiceRunning();

        this.initText();

        this.initButtons();

        this.initBottomSheet();

        this.initSpinKit();

        this.initSwipeRefresh();

        this.initBarChart();

        this.initLineChart();

        this.mCircle = view.findViewById(R.id.main_circle);

        this.mWaveView = view.findViewById(R.id.main_wave_view);

        this.mWaveView.setProgress(WAVE_LEVEL);
    }

    private void checkIsNeedToShowWelcomePage() {
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        SharedPreferences sharedPreferences = getActivity().getPreferences(Activity.MODE_PRIVATE);

        boolean isNeedToShowWelcomePage = sharedPreferences.getBoolean("isNeedToShowWelcomePage", true);

        if (isNeedToShowWelcomePage)
            navController.navigate(R.id.action_main_fragment_to_welcome_fragment);
    }

    private void checkIsServiceRunning() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean isFirstTimeRefresh = sharedPreferences.getBoolean("isFirstTimeRefresh", true);

        if (!isFirstTimeRefresh && !isMyServiceRunning()) {
            Intent serviceIntent = new Intent(getContext(), ForeService.class);
            getActivity().startForegroundService(serviceIntent);
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForeService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initText() {
        mText = this.mView.findViewById(R.id.main_text);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean isFirstTimeRefresh = sharedPreferences.getBoolean("isFirstTimeRefresh", true);

        if (!isFirstTimeRefresh) mText.setText(getResources().getString(R.string.pull_to_detect));
    }

    private void initButtons() {
        mSetting = this.mView.findViewById(R.id.main_setting);
        mSetting.setVisibility(View.INVISIBLE);
        mHelping = this.mView.findViewById(R.id.main_helping);
        mHelping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

                NavController navController = navHostFragment.getNavController();

                navController.navigate(R.id.action_main_fragment_to_welcome_fragment);
            }
        });
    }

    private void initBottomSheet() {
        mBottomSheet = this.mView.findViewById(R.id.main_bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean isFirstTimeRefresh = sharedPreferences.getBoolean("isFirstTimeRefresh", true);

        if (isFirstTimeRefresh) {
            mBottomSheetBehavior.setHideable(true);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void initSpinKit() {
        mSpinKit = this.mView.findViewById(R.id.main_spin_kit);
        Sprite MultiplePulseRing = new MultiplePulseRing();
        mSpinKit.setIndeterminateDrawable(MultiplePulseRing);
    }

    private void initSwipeRefresh() {
        mSwipeRefresh = this.mView.findViewById(R.id.main_swipe_refresh);

        mSwipeRefresh.setOnRefreshListener(() -> {
            try {
                if (checkAudioRecordPermission(getActivity())) {
                    handleFirstTimeRefresh();

                    SoundRecorder soundRecorder = new SoundRecorder();

                    handleOpeningUI();

                    mIsAnalyzing = true;

                    Future<Double> future = soundRecorder.recordAndGetDecibel(5);
                    setIntervalCheckFuture(future);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleFirstTimeRefresh() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean isFirstTimeRefresh = sharedPreferences.getBoolean("isFirstTimeRefresh", true);

        if (isFirstTimeRefresh) {
            Intent serviceIntent = new Intent(getContext(), ForeService.class);
            getActivity().startForegroundService(serviceIntent);
        }
    }

    private boolean checkAudioRecordPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, SoundRecorder.PERMISSIONS_RECORD_AUDIO, SoundRecorder.RECORD_AUDIO_REQUEST_CODE);
            return false;
        } else return true;
    }

    private void setIntervalCheckFuture(Future<Double> future) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handleFuture(this, future);
            }
        }, 0, 100);
    }

    private void handleFuture(TimerTask timerTask, Future<Double> future) {
        try {
            if (future.isDone()) {
                Double decibel = future.get();

                if (decibel < 0) decibel = 0d;

                handleData(decibel);

                mIsAnalyzing = false;

                GradientDrawable circleShapeDrawable = (GradientDrawable) mCircle.getBackground();

                circleShapeDrawable.mutate();

                if (decibel < 80) {
                    Double finalDecibel = decibel;
                    requireActivity().runOnUiThread(() -> {
                        mText.setText(String.format(Locale.TAIWAN, "偵測到最大分貝值：%d dB\n\n環境中沒有噪音危害", finalDecibel.intValue()));
                        mCircle.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_hearing, null));
                    });

                    circleShapeDrawable.setStroke(25, ContextCompat.getColor(this.mView.getContext(), R.color.good_green));
                } else if (decibel >= 80 && decibel < 120) {
                    Double finalDecibel = decibel;
                    requireActivity().runOnUiThread(() -> {
                        mText.setText(String.format(Locale.TAIWAN, "偵測到最大分貝值：%d dB\n\n環境中存在些許噪音", finalDecibel.intValue()));
                        mCircle.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_outline_priority_high, null));
                    });

                    circleShapeDrawable.setStroke(25, ContextCompat.getColor(this.mView.getContext(), R.color.warning_orange));
                } else if (decibel >= 120) {
                    Double finalDecibel = decibel;
                    requireActivity().runOnUiThread(() -> {
                        mText.setText(String.format(Locale.TAIWAN, "偵測到最大分貝值：%d dB\n\n環境中噪音危害嚴重", finalDecibel.intValue()));
                        mCircle.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_baseline_hearing_disabled, null));
                    });

                    circleShapeDrawable.setStroke(25, ContextCompat.getColor(this.mView.getContext(), R.color.bad_red));
                }

                requireActivity().runOnUiThread(() -> {
                    mSpinKit.setVisibility(View.INVISIBLE);

                    //fadeIn(mSetting);
                    fadeIn(mHelping);
                    fadeIn(mCircle);

                    mWaveView.setProgress(WAVE_LEVEL);

                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    mBottomSheetBehavior.setHideable(false);

                    mSwipeRefresh.setEnabled(true);
                });

                timerTask.cancel();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleData(Double decibel) {
        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());

        DataModel dataModel;

        try {
            dataModel = new DataModel(0, decibel.intValue());
        } catch (Exception e) {
            e.printStackTrace();

            dataModel = new DataModel(0, 0);
        }

        final boolean isSuccess = dataBaseHelper.addOne(dataModel);

        Log.i("SQLite", "Data insert result: " + isSuccess);
    }

    private void fadeOut(View view) {
        view.animate()
                .alpha(0f)
                .setDuration(FADE_OUT_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void handleOpeningUI() {
        mSwipeRefresh.setRefreshing(false);
        mSwipeRefresh.setEnabled(false);

        mText.setText("開始分析環境噪音");

        mSpinKit.setVisibility(View.VISIBLE);

        //fadeOut(mSetting);
        fadeOut(mHelping);
        fadeOut(mCircle);

        new Thread(() -> {
            try {
                for (int i = WAVE_LEVEL; i <= 100; i++) {
                    int progress = i;

                    requireActivity().runOnUiThread(() -> mWaveView.setProgress(progress));

                    if (i < 55) Thread.sleep(25);

                    else if (i < 75) Thread.sleep(17);

                    else if (i < 100) Thread.sleep(10);
                }

                setIntervalUpdateMainText();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void setIntervalUpdateMainText() {
        final int[] count = {0};
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mIsAnalyzing) {
                    switch (count[0]) {
                        case 0:
                            requireActivity().runOnUiThread(() -> mText.setText("分析中.  "));
                            count[0] = (count[0] + 1) % 3;
                            break;
                        case 1:
                            requireActivity().runOnUiThread(() -> mText.setText("分析中.. "));
                            count[0] = (count[0] + 1) % 3;
                            break;
                        case 2:
                            requireActivity().runOnUiThread(() -> mText.setText("分析中..."));
                            count[0] = (count[0] + 1) % 3;
                            break;
                    }
                } else this.cancel();
            }
        }, 0, 750);
    }

    private void fadeIn(View view) {
        view.setVisibility(View.VISIBLE);

        view.animate()
                .alpha(1f)
                .setDuration(FADE_IN_DURATION)
                .setListener(null);
    }

    private void initBarChart() {
        this.mBarChart = this.mView.findViewById(R.id.bottom_bar_chart);

        List<DataBarChart> dataBarChartList = new ArrayList<>();

        List<BarEntry> entries = new ArrayList<>();

        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());

        Date dateToday = new Date();

        String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return weekdays[(int) value - 1];
            }
        };

        int weekdayToday = getDayNumberOld(dateToday);

        for (int i = 1; i <= 7; i++) {
            if (i < weekdayToday) {
                LocalDateTime localDateTime = LocalDateTime.ofInstant(dateToday.toInstant(), ZoneId.systemDefault());

                localDateTime = localDateTime.minusDays(Math.abs(weekdayToday - i));

                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

                List<DataModel> data = dataBaseHelper.getDataInSpecificTime(atStartOfDay(date), atEndOfDay(date));

                dataBarChartList.add(new DataBarChart(i, data));
            } else if (i == weekdayToday) {
                List<DataModel> data = dataBaseHelper.getDataInSpecificTime(atStartOfDay(dateToday), atEndOfDay(dateToday));

                dataBarChartList.add(new DataBarChart(i, data));
            } else {
                List<DataModel> data = new ArrayList<>();

                dataBarChartList.add(new DataBarChart(i, data));
            }
        }

        for (DataBarChart dataBarChart : dataBarChartList) {
            entries.add(new BarEntry((float) dataBarChart.getValueX(), (float) dataBarChart.getValueY()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "平均噪音分貝值");

        XAxis xAxis = this.mBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis left = this.mBarChart.getAxisLeft();
        left.setDrawLabels(false);
        left.setDrawGridLines(false);
        left.setDrawAxisLine(false);
        left.setAxisMinimum(0f);

        YAxis right = this.mBarChart.getAxisRight();
        right.setDrawGridLines(false);
        right.setDrawAxisLine(false);
        right.setAxisMinimum(0f);

        this.mBarChart.setData(new BarData(dataSet));
        this.mBarChart.setDescription(null);
        this.mBarChart.setDrawBorders(false);
        this.mBarChart.setFitBars(true);
        this.mBarChart.invalidate();
    }

    private static int getDayNumberOld(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    private static Date atStartOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return localDateTimeToDate(startOfDay);
    }

    private static Date atEndOfDay(Date date) {
        LocalDateTime localDateTime = dateToLocalDateTime(date);
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return localDateTimeToDate(endOfDay);
    }

    private static LocalDateTime dateToLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private void initLineChart() {
        this.mLineChart = this.mView.findViewById(R.id.bottom_line_chart);

        List<Entry> entries = new ArrayList<>();

        DataBaseHelper dataBaseHelper = new DataBaseHelper(getContext());

        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.TAIWAN);

        dateFormat.setTimeZone(TimeZone.getDefault());

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                long xValue = (long) value;
                long todayStartTime = atStartOfDay(date).getTime() / 1000;
                long pen = xValue + todayStartTime;
                return dateFormat.format(new Date(pen * 1000));
            }
        };

        List<DataModel> dataList = dataBaseHelper.getDataInSpecificTime(atStartOfDay(date), atEndOfDay(date));

        for (DataModel data : dataList) {
            long timestamp = data.getTimestamp();
            long todayStartTime = atStartOfDay(date).getTime() / 1000;
            long diff = timestamp - todayStartTime;
            entries.add(new Entry((float) diff, (float) data.getDecibel()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "噪音分貝值");

        XAxis xAxis = this.mLineChart.getXAxis();
        if (dataList.size() != 0) {
            long timestampMax = dataList.get(dataList.size() - 1).getTimestamp();
            long todayStartTime = atStartOfDay(date).getTime() / 1000;
            long max = timestampMax - todayStartTime;
            xAxis.setAxisMaximum((float ) max);
        }
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis left = this.mLineChart.getAxisLeft();
        left.setDrawLabels(false);
        left.setDrawGridLines(false);
        left.setDrawAxisLine(false);
        left.setAxisMinimum(0f);

        YAxis right = this.mLineChart.getAxisRight();
        right.setDrawGridLines(false);
        right.setDrawAxisLine(false);
        right.setAxisMinimum(0f);

        this.mLineChart.setData(new LineData(dataSet));
        this.mLineChart.setDescription(null);
        this.mLineChart.setDrawBorders(false);
        this.mLineChart.invalidate();
    }
}