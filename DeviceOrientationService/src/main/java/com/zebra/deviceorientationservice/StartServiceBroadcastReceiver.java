package com.zebra.deviceorientationservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.TAG, "StartServiceBroadcastReceiver::onReceive");
        // Start service
        DeviceOrientationService.startService(context);
        MainActivity.updateGUISwitchesIfNecessary();
    }
}
