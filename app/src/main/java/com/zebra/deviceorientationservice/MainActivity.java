package com.zebra.deviceorientationservice;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

// The service can be launched using the graphical user interface, intent actions or adb.
//
// If the option "Start on boot" is enabled, the service will be automatically launched when the boot is complete.
//
// Power events occur when the device is connected to a power source (AC/USB/Wireless).
// If the option "Start when charging / Stop when charging" is enabled, the power events will be monitored.
// The DeviceOrientationService will be launched when the device is connected to a power source
//
//
// The service respond to two intent actions (both uses the category: android.intent.category.DEFAULT)
// - "com.zebra.deviceorientationservice.startservice" sent on the component "com.zebra.deviceorientationservice/com.zebra.deviceorientationservice.StartServiceBroadcastReceiver":
//   Start the service.
//   If the device get rebooted the service will start automatically once the reboot is completed.
// - "com.zebra.deviceorientationservice.stopservice" sent on the component "com.zebra.deviceorientationservice/com.zebra.deviceorientationservice.StopServiceBroadcastReceiver":
//   Stop the service.
//   If the device is rebooted, the service will not be started.
//
// The service can be started and stopped manually using the following adb commands:
//  - Start service:
//      adb shell am broadcast -a com.zebra.deviceorientationservice.startservice -n com.zebra.deviceorientationservice/com.zebra.deviceorientationservice.StartServiceBroadcastReceiver
//  - Stop service:
//      adb shell am broadcast -a com.zebra.deviceorientationservice.stopservice -n com.zebra.deviceorientationservice/com.zebra.deviceorientationservice.StopServiceBroadcastReceiver
//  - Setup service
//          The service can be configured using the following intent:
//          adb shell am broadcast -a com.zebra.deviceorientationservice.setupservice -n com.zebra.deviceorientationservice/com.zebra.deviceorientationservice.SetupServiceBroadcastReceiver --es startonboot "true" --es startoncharging "true" " --es orientation “portrait”
//          The command must contain at least one of the extras:
//          - Configure autostart on boot:
//          --es startonboot "true"
//          - Configure autostart on power connection (AC/USB/Wireless)
//          --es startoncharging "true"
//          The extras value can be set to "true" or "1" to enable the option and "false" or "0" to disable the option (with double quotes).
//          - Configure layout orientation
//          --es orientation "disabled"
//          The extras value can be set to : "disabled","landscape","portrait" or "reverse_landscape" (with double quotes)
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Switch mStartStopServiceSwitch = null;
    private Switch mAutoStartServiceOnBootSwitch = null;
    private Switch mAutoStartServiceOnCraddleSwitch = null;
    private Spinner mOrientationSpinner = null;
    private ArrayAdapter<CharSequence> mOrientationAdapter = null;
    private E_ORIENTATION mOrientation = E_ORIENTATION.DISABLED;
    public static MainActivity mMainActivity;

    public static int OVERLAY_PERMISSION_CODE = 1242;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button)findViewById(R.id.btLicense)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, LicenceActivity.class);
                startActivity(myIntent);
            }
        });

        mStartStopServiceSwitch = (Switch)findViewById(R.id.startStopServiceSwitch);
        mStartStopServiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mStartStopServiceSwitch.setText(getString(R.string.serviceStarted));
                    if(!DeviceOrientationService.isRunning(MainActivity.this))
                        DeviceOrientationService.startService(MainActivity.this);
                }
                else
                {
                    mStartStopServiceSwitch.setText(getString(R.string.serviceStopped));
                    if(DeviceOrientationService.isRunning(MainActivity.this))
                        DeviceOrientationService.stopService(MainActivity.this);
                }
            }
        });

        mAutoStartServiceOnBootSwitch = (Switch)findViewById(R.id.startOnBootSwitch);
        mAutoStartServiceOnBootSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mAutoStartServiceOnBootSwitch.setText(getString(R.string.startOnBoot));
                }
                else
                {
                    mAutoStartServiceOnBootSwitch.setText(getString(R.string.doNothingOnBoot));
                }
                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_BOOT, isChecked);
                editor.commit();
            }
        });

        mAutoStartServiceOnCraddleSwitch = (Switch)findViewById(R.id.startOnCraddle);
        mAutoStartServiceOnCraddleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mAutoStartServiceOnCraddleSwitch.setText(getString(R.string.startOnCharging));
                    // Launch the watcher service
                    if(!PowerEventsWatcherService.isRunning(MainActivity.this))
                        PowerEventsWatcherService.startService(MainActivity.this);
                    // Let's check if we are already connected on power to launch DeviceOrientationService if necessary
                    BatteryManager myBatteryManager = (BatteryManager) MainActivity.this.getSystemService(Context.BATTERY_SERVICE);
                    if(myBatteryManager.isCharging() && !DeviceOrientationService.isRunning(MainActivity.this))
                        DeviceOrientationService.startService(MainActivity.this);
                }
                else
                {
                    mAutoStartServiceOnCraddleSwitch.setText(getString(R.string.doNothingOnCharging));
                    // Stop the watcher service
                    if(PowerEventsWatcherService.isRunning(MainActivity.this))
                        PowerEventsWatcherService.stopService(MainActivity.this);
                }
                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, isChecked);
                editor.commit();
            }
        });

        mOrientationSpinner = findViewById(R.id.orientation_spinner);
        mOrientationAdapter = ArrayAdapter.createFromResource(this, R.array.orientation_modes, android.R.layout.simple_spinner_item);
        mOrientationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOrientationSpinner.setAdapter(mOrientationAdapter);
        mOrientationSpinner.setOnItemSelectedListener(this);

        updateSwitches();
        launchPowerEventsWatcherServiceIfNecessary();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            checkOverlayPermission();
    }

    @Override
    protected void onResume() {
        mMainActivity = this;
        super.onResume();
        updateSwitches();
        launchPowerEventsWatcherServiceIfNecessary();
    }

    public void checkOverlayPermission()
    {
        if (Settings.canDrawOverlays(this) == false) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == OVERLAY_PERMISSION_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please grant overlay permissions.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_CODE);
            }
        }
    }

    public void updateSwitches()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(DeviceOrientationService.isRunning(MainActivity.this))
                {
                    setServiceStartedSwitchValues(true, getString(R.string.serviceStarted));
                }
                else
                {
                    setServiceStartedSwitchValues(false, getString(R.string.serviceStopped));
                }

                SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                boolean startServiceOnBoot = sharedpreferences.getBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_BOOT, false);
                setAutoStartServiceOnBootSwitch(startServiceOnBoot, startServiceOnBoot ? getString(R.string.startOnBoot) : getString(R.string.doNothingOnBoot));

                boolean startServiceOnCharging = sharedpreferences.getBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, false);
                setAutoStartServiceOnChargingSwitch(startServiceOnCharging, startServiceOnCharging ? getString(R.string.startOnCharging) : getString(R.string.doNothingOnCharging));

                // Set spinner position
                String orientation = sharedpreferences.getString(Constants.SHARED_PREFERENCES_ORIENTATION, E_ORIENTATION.DISABLED.toString());
                int spinnerPosition = mOrientationAdapter.getPosition(orientation);
                mOrientationSpinner.setSelection(spinnerPosition);
            }
        });

    }

    private void launchPowerEventsWatcherServiceIfNecessary()
    {
        // We need to launch the PowerEventsWatcher Service if necessary
        SharedPreferences sharedpreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean startServiceOnCharging = sharedpreferences.getBoolean(Constants.SHARED_PREFERENCES_START_SERVICE_ON_CHARGING, false);
        if(startServiceOnCharging)
        {
            // Launch the service if it was not running
            if(!PowerEventsWatcherService.isRunning(this))
                PowerEventsWatcherService.startService(this);

            // Let's check if we are already connected on power to launch DeviceOrientationService if necessary
            BatteryManager myBatteryManager = (BatteryManager) MainActivity.this.getSystemService(Context.BATTERY_SERVICE);
            if(myBatteryManager.isCharging() && !DeviceOrientationService.isRunning(MainActivity.this))
                DeviceOrientationService.startService(MainActivity.this);
        }
    }

    @Override
    protected void onPause() {
        mMainActivity = null;
        super.onPause();
    }

    private void setServiceStartedSwitchValues(final boolean checked, final String text)
    {
        mStartStopServiceSwitch.setChecked(checked);
        mStartStopServiceSwitch.setText(text);
    }

    private void setAutoStartServiceOnBootSwitch(final boolean checked, final String text)
    {
        mAutoStartServiceOnBootSwitch.setChecked(checked);
        mAutoStartServiceOnBootSwitch.setText(text);
    }

    private void setAutoStartServiceOnChargingSwitch(final boolean checked, final String text)
    {
        mAutoStartServiceOnCraddleSwitch.setChecked(checked);
        mAutoStartServiceOnCraddleSwitch.setText(text);
    }


    public static void updateGUISwitchesIfNecessary()
    {
        // Update GUI if necessary
        if(MainActivity.mMainActivity != null) // The application default activity has been opened
        {
            MainActivity.mMainActivity.updateSwitches();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        E_ORIENTATION selectedOrientation = E_ORIENTATION.fromString(parent.getItemAtPosition(position).toString());
        if(selectedOrientation != mOrientation)
        {
            mOrientation = selectedOrientation;
            DeviceOrientationService.setOrientationSharedPreferences(this, mOrientation);

            if(DeviceOrientationService.isRunning(this))
            {
                DeviceOrientationService.setOrientation(this, selectedOrientation);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
