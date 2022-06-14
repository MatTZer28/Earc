package com.example.earc.func.record;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SoundRecorder {
    private File mRecorderFile;

    private MediaRecorder mMediaRecorder;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private boolean mIsRecording = false;

    private double mEMA = 0.0;

    public static final String[] PERMISSIONS_RECORD_AUDIO = {"android.permission.RECORD_AUDIO"};

    public static final int RECORD_AUDIO_REQUEST_CODE = 1;

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    private static final int OUTPUT_FORMAT = MediaRecorder.OutputFormat.THREE_GPP;

    private static final int AUDIO_ENCODER = MediaRecorder.AudioEncoder.AMR_NB;

    private static final int SAMPLE_RATE = 44100;

    private static final double EMA_FILTER = 0.6;

    public SoundRecorder() {
        try {
            this.initRecorder();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initRecorder() throws IOException {
        this.mRecorderFile = File.createTempFile("temp", ".amr");

        this.mMediaRecorder = new MediaRecorder();

        this.mMediaRecorder.setAudioSource(AUDIO_SOURCE);

        this.mMediaRecorder.setOutputFormat(OUTPUT_FORMAT);

        this.mMediaRecorder.setAudioEncoder(AUDIO_ENCODER);

        this.mMediaRecorder.setAudioSamplingRate(SAMPLE_RATE);

        this.mMediaRecorder.setOutputFile(this.mRecorderFile.getAbsolutePath());
    }

    public Future<Double> recordAndGetDecibel(int durationSec) throws IOException {
        this.mMediaRecorder.prepare();

        this.mMediaRecorder.start();

        this.mIsRecording = true;

        this.mMediaRecorder.getMaxAmplitude();

        Future<Double> future = mExecutor.submit(this::recordAndReturnGetDecibel);

        this.setStopRecordTimer(durationSec);

        return future;
    }

    // Cellphones can catch up to 90 db + -
    // getMaxAmplitude returns a value between 0-32767 (in most phones). that means that if the maximum db is 90, the pressure
    // at the microphone is 0.6325 Pascal.
    // it does a comparison with the previous value of getMaxAmplitude.
    // we need to divide maxAmplitude with (32767/0.6325)
    //51805.5336 or if 100db so 46676.6381

    //Assuming that the minimum reference pressure is 0.000085 Pascal (on most phones) is equal to 0 db

    private double recordAndReturnGetDecibel() throws InterruptedException {
        while (this.mIsRecording) {
            Thread.sleep(100);
        }

        int maxAmplitude = this.mMediaRecorder.getMaxAmplitude();

        mEMA = EMA_FILTER * maxAmplitude + (1.0 - EMA_FILTER) * mEMA;

        double decibel = 20 * Math.log10((mEMA / (32767 / 0.6325d)) / 0.000085d);

        this.mMediaRecorder.stop();

        this.mMediaRecorder.release();

        this.mRecorderFile.delete();

        return decibel;
    }

    private void setStopRecordTimer(int durationSec) {
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            public void run() {
                mIsRecording = false;
            }
        };

        timer.schedule(task, durationSec * 1000L);
    }

    private boolean checkAudioRecordPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_RECORD_AUDIO, RECORD_AUDIO_REQUEST_CODE);
            return false;
        } else return true;
    }
}
