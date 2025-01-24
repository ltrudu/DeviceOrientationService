package com.zebra.deviceorientationservice;

public class Constants {
    public static final String TAG  ="DeviceOrientationSvc";

    // Shared preference keys
    public static final String SHARED_PREFERENCES_NAME = "DeviceOrientationService";
    public static final String SHARED_PREFERENCES_AUTOSTART = "autostart";
    public static final String SHARED_PREFERENCES_START_SERVICE_ON_BOOT = "startonboot";
    public static final String SHARED_PREFERENCES_START_SERVICE_ON_CHARGING = "startoncharging";
    public static final String SHARED_PREFERENCES_ORIENTATION = "orientation";

    public static final String EXTRA_CONFIGURATION_AUTOSTART = "autostart";
    public static final String EXTRA_CONFIGURATION_START_ON_BOOT = "startonboot";
    public static final String EXTRA_CONFIGURATION_START_ON_CHARGING = "startoncharging";
    public static final String EXTRA_CONFIGURATION_ORIENTATION = "orientation";
}
