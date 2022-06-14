package com.example.earc.func.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.earc.R;
import com.example.earc.func.data.DataBaseHelper;
import com.example.earc.func.data.DataModel;
import com.example.earc.func.record.SoundRecorder;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ForeService extends Service {
    private static final String CHANNEL_NAME = "My Lovely Earc";

    private static final String CHANNEL_ID = getID();

    // TODO: 可以讓使用者自己設定
    private static final int detect_interval = 600000;

    private DataBaseHelper mDataBaseHelper;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            stopSelf();

        this.handleRecord();

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentText("正在監測周圍環境噪音")
                .setContentTitle("噪音檢測")
                .setSmallIcon(R.drawable.ic_launcher_background);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            startForeground((int) (System.currentTimeMillis() % 10000), notification.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground((int) (System.currentTimeMillis() % 10000), notification.build());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void handleRecord() {
        setIsFirstTimeRefreshPref();

        mDataBaseHelper = new DataBaseHelper(this);

        new Thread(this::handleRecordSchedule).start();
    }

    private void setIsFirstTimeRefreshPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        sharedPreferences.edit().putBoolean("isFirstTimeRefresh", false).apply();
    }

    private void handleRecordSchedule() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    SoundRecorder soundRecorder = new SoundRecorder();
                    Future<Double> future = soundRecorder.recordAndGetDecibel(5);
                    setIntervalCheckFuture(future);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, detect_interval, detect_interval);
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
        if (future.isDone()) {
            DataModel dataModel;

            try {
                Double decibel = future.get();

                if (decibel < 0) decibel = 0d;

                dataModel = new DataModel(0, decibel.intValue());
            } catch (Exception e) {
                e.printStackTrace();

                dataModel = new DataModel(0, 0);
            }

            // TODO: 如果插入失敗可以做點什麼
            final boolean isSuccess = mDataBaseHelper.addOne(dataModel);

            Log.i("SQLite", "Data insert result: " + isSuccess);

            timerTask.cancel();
        }
    }

    private static String getID() {
        String id = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(ForeService.CHANNEL_NAME.getBytes(StandardCharsets.UTF_8));
            id = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return id;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
