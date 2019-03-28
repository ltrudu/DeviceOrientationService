package com.zebra.deviceorientationservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

public class SetupServiceBroadcastReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive");

        String sOrientation = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_ORIENTATION, E_ORIENTATION.DISABLED.toString());
        if(sOrientation != null)
        {
            // Check if this extra is recognized or fall back to disabled
            E_ORIENTATION eOrientation = E_ORIENTATION.fromString(sOrientation);
            if(sOrientation.equals(eOrientation.toString()) == false)
            {
                Log.d(Constants.TAG, "Extra orientation not recognized (" + sOrientation + "), please choose one of this options: disabled,landscape,portrait or reverse_landscape");
            }
            else
            {
                DeviceOrientationService.setOrientationSharedPreferences(context, eOrientation);
                if(DeviceOrientationService.isRunning(context))
                {
                    DeviceOrientationService.setOrientation(context, eOrientation);
                }
            }
        }

        String sStartOnBoot = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_START_ON_BOOT, null);
        if(sStartOnBoot != null)
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Start on boot extra found with value:" + sStartOnBoot);
            boolean bStartOnBoot = sStartOnBoot.equalsIgnoreCase("true") || sStartOnBoot.equalsIgnoreCase("1");
            setSharedPreference(context, Constants.SHARED_PREFERENCES_START_SERVICE_ON_BOOT, bStartOnBoot);
            // Update GUI if necessary
            MainActivity.updateGUISwitchesIfNecessary();
        }
        else
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:No start on boot extra found.");
        }

        String sStartOnCharging = intent.getExtras().getString(Constants.EXTRA_CONFIGURATION_START_ON_CHARGING, null);
        if(sStartOnCharging != null)
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Start on charging extra found with value:" + sStartOnCharging);
            boolean bStartOnCharging = sStartOnCharging.equalsIgnoreCase("true") || sStartOnBoot.equalsIgnoreCase("1");
            setSharedPreference(context, Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, bStartOnCharging);
            // Launch service if necessary
            if(bStartOnCharging)
            {
                if(!PowerEventsWatcherService.isRunning(context))
                    PowerEventsWatcherService.startService(context);

                // Let's check if we are already connected on power to launch DeviceOrientationService if necessary
                BatteryManager myBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
                if(myBatteryManager.isCharging() && !DeviceOrientationService.isRunning(context))
                    DeviceOrientationService.startService(context);
            }
            // Update GUI if necessary
            MainActivity.updateGUISwitchesIfNecessary();
        }
        else
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:No start on charging extra found.");
        }
    }

    private void setSharedPreference(Context context, String key, boolean value)
    {
        Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::setSharedPreference: Key=" + key + " | Value=" + value);
        // Setup shared preferences for next reboot
        SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
