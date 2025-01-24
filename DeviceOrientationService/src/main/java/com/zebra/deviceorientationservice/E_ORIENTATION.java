package com.zebra.deviceorientationservice;

import android.content.pm.ActivityInfo;

public enum E_ORIENTATION {
    DISABLED("disabled"),
    LANDSCAPE("landscape"),
    PORTRAIT("portrait"),
    USER("user"),
    BEHIND("behind"),
    SENSOR("sensor"),
    NOSENSOR("nosensor"),
    SENSOR_LANDSCAPE("sensor_landscape"),
    SENSOR_PORTRAIT("sensor_portrait"),
    REVERSE_LANDSCAPE("reverse_landscape"),
    REVERSE_PORTRAIT("reverse_portrait"),
    FULL_SENSOR("full_sensor"),
    USER_LANDSCAPE("user_landscape"),
    USER_PORTRAIT("user_portrait"),
    FULL_USER("full_user"),
    LOCKED("locked");

    private String enumString;

    E_ORIENTATION(String confName)
    {
        this.enumString = confName;
    }

    public String toString()
    {
        return this.enumString;
    }

    public static E_ORIENTATION fromString(String orientation)
    {
        switch (orientation)
        {
            case "landscape":
                return  LANDSCAPE;
            case "portrait":
                return  PORTRAIT;
            case "user":
                return  USER;
            case "behind":
                return  BEHIND;
            case "sensor":
                return  SENSOR;
            case "nosensor":
                return  NOSENSOR;
            case "sensor_landscape":
                return  SENSOR_LANDSCAPE;
            case "sensor_portrait":
                return  SENSOR_PORTRAIT;
            case "reverse_landscape":
                return  REVERSE_LANDSCAPE;
            case "reverse_portrait":
                return  REVERSE_PORTRAIT;
            case "full_sensor":
                return  FULL_SENSOR;
            case "user_landscape":
                return  USER_LANDSCAPE;
            case "user_portrait":
                return  USER_PORTRAIT;
            case "full_user":
                return  FULL_USER;
            case "locked":
                return  LOCKED;
        }
        return DISABLED;
    }
    
    public int toActivityInfoValue()
    {
        switch (this) {
            case DISABLED:
                return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            case LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            case PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            case USER:
                return ActivityInfo.SCREEN_ORIENTATION_USER;
            case BEHIND:
                return ActivityInfo.SCREEN_ORIENTATION_BEHIND;
            case SENSOR:
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR;
            case NOSENSOR:
                return ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
            case SENSOR_LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            case SENSOR_PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
            case REVERSE_LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            case REVERSE_PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            case FULL_SENSOR:
                return ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
            case USER_LANDSCAPE:
                return ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
            case USER_PORTRAIT:
                return ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
            case FULL_USER:
                return ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
            case LOCKED:
                return ActivityInfo.SCREEN_ORIENTATION_LOCKED;
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }
}
