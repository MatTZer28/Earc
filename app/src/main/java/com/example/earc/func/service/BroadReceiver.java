package com.example.earc.func.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, ForeService.class);
            context.startForegroundService(serviceIntent);
        }
    }
}
