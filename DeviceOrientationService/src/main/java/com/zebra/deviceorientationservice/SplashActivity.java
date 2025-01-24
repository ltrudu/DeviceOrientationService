package com.zebra.deviceorientationservice;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashActivity extends AppCompatActivity {

    TextView tvStatus = null;
    TextView tvLoading = null;
    private Handler title_animation_handler;
    private Runnable title_animation_runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        tvStatus = findViewById(R.id.tvStatus);
        tvLoading = findViewById(R.id.tvLoading);

        // Retrieve the Intent that started this activity
        Intent apkIntent = getIntent();
        processArgumentsIfApplicable(apkIntent);

        if (MainApplication.permissionGranted == false) {
            setTitle(R.string.app_title);
            startPointsAnimations(getString(R.string.app_title), getString(R.string.loading_status));
            MainApplication.iMainApplicationCallback = new MainApplication.iMainApplicationCallback() {
                @Override
                public void onPermissionSuccess(String message) {
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopPointsAnimations();
                            tvStatus.setText("Success Granting Permissions.");
                            // Start MainActivity
                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

                @Override
                public void onPermissionError(String message) {
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopPointsAnimations();
                            tvStatus.setText(message);
                        }
                    });
                }

                @Override
                public void onPermissionDebug(String message) {
                    SplashActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvStatus.setText(message);
                        }
                    });

                }
            };

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
        else
        {
            stopPointsAnimations();
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void processArgumentsIfApplicable(Intent intent)
    {
        String sOrientation = intent.getStringExtra(Constants.EXTRA_CONFIGURATION_ORIENTATION);
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
                DeviceOrientationService.setOrientationSharedPreferences(this, eOrientation);
                if(DeviceOrientationService.isRunning(this))
                {
                    DeviceOrientationService.setOrientation(this, eOrientation);
                }
            }
        }
        else
        {
            Log.d(Constants.TAG, "SplashActivity::onCreate:No orientation extra found.");
        }

        String sStartOnBoot = intent.getStringExtra(Constants.EXTRA_CONFIGURATION_START_ON_BOOT);
        if(sStartOnBoot != null)
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Start on boot extra found with value:" + sStartOnBoot);
            boolean bStartOnBoot = sStartOnBoot.equalsIgnoreCase("true") || sStartOnBoot.equalsIgnoreCase("1");
            setSharedPreference(this, Constants.SHARED_PREFERENCES_START_SERVICE_ON_BOOT, bStartOnBoot);
            // Update GUI if necessary
            MainActivity.updateGUISwitchesIfNecessary();
        }
        else
        {
            Log.d(Constants.TAG, "SplashActivity::onCreate:No start on boot extra found.");
        }

        String sStartOnCharging = intent.getStringExtra(Constants.EXTRA_CONFIGURATION_START_ON_CHARGING);
        if(sStartOnCharging != null)
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Start on charging extra found with value:" + sStartOnCharging);
            boolean bStartOnCharging = sStartOnCharging.equalsIgnoreCase("true") || sStartOnCharging.equalsIgnoreCase("1");
            setSharedPreference(this, Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, bStartOnCharging);
            // Launch service if necessary
            if(bStartOnCharging)
            {
                if(!PowerEventsWatcherService.isRunning(this))
                    PowerEventsWatcherService.startService(this);

                // Let's check if we are already connected on power to launch DeviceOrientationService if necessary
                BatteryManager myBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                if(myBatteryManager.isCharging() && !DeviceOrientationService.isRunning(this))
                    DeviceOrientationService.startService(this);
            }
            // Update GUI if necessary
            MainActivity.updateGUISwitchesIfNecessary();
        }
        else
        {
            Log.d(Constants.TAG, "SplashActivity::onCreate:No start on charging extra found.");
        }

        String autoStart = intent.getStringExtra(Constants.EXTRA_CONFIGURATION_AUTOSTART);
        if(autoStart != null)
        {
            Log.d(Constants.TAG, "SetupServiceBroadcastReceiver::onReceive:Start on charging extra found with value:" + autoStart);
            boolean bAutoStart = autoStart.equalsIgnoreCase("true") || autoStart.equalsIgnoreCase("1");
            setSharedPreference(this, Constants.SHARED_PREFERENCES_AUTOSTART, bAutoStart);
            // Launch service if necessary
            if(bAutoStart)
            {
                if(!DeviceOrientationService.isRunning(this))
                    DeviceOrientationService.startService(this);
            }
            // Update GUI if necessary
            MainActivity.updateGUISwitchesIfNecessary();
        }
        else
        {
            Log.d(Constants.TAG, "SplashActivity::onCreate:No autostart extra found.");
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

    private void startPointsAnimations(String baseTitle, String baseLoadingStatus) {
        final int maxDots = 5;
        title_animation_handler = new Handler(Looper.getMainLooper());
        title_animation_runnable = new Runnable() {
            int dotCount = 0;

            @Override
            public void run() {
                StringBuilder title = new StringBuilder(baseTitle);
                StringBuilder loadingStatus = new StringBuilder(baseLoadingStatus);
                for (int i = 0; i < dotCount; i++) {
                    title.append(".");
                    loadingStatus.append(".");
                }
                setTitle(title.toString());
                tvLoading.setText(loadingStatus.toString());
                dotCount = (dotCount + 1) % (maxDots + 1);
                title_animation_handler.postDelayed(this, 500); // Update every 500 milliseconds
            }
        };
        title_animation_handler.post(title_animation_runnable);
    }

    private void stopPointsAnimations() {
        if (title_animation_handler != null && title_animation_runnable != null) {
            title_animation_handler.removeCallbacks(title_animation_runnable);
            title_animation_handler = null;
            title_animation_runnable = null;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setTitle(R.string.app_title);
                tvLoading.setText(R.string.loading_status);
            }
        });
    }
}