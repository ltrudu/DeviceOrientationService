package com.zebra.deviceorientationservice;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;


import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.app.job.JobInfo.PRIORITY_MIN;

public class DeviceOrientationService extends Service {
    private static final int SERVICE_ID = 1;

    private NotificationManager mNotificationManager;
    private Notification mNotification;

    private static View mView = null;
    private static WindowManager mWindowManager = null;

    public DeviceOrientationService() {
    }

    public IBinder onBind(Intent paramIntent)
    {
        return null;
    }

    public void onCreate()
    {
        logD("onCreate");
        this.mNotificationManager = ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logD("onStartCommand");
        super.onStartCommand(intent, flags, startId);
        startService();
        return Service.START_STICKY;
    }

    public void onDestroy()
    {
        logD("onDestroy");
        cleanupWindow(this);
        stopService();
    }

    @SuppressLint({"Wakelock"})
    private void startService()
    {
        logD("startService");
        try
        {
            // First check if we can draw an overlay window
            checkForOverlayPermission(this);

            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    mainActivityIntent,
                    FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);

            // Create the Foreground Service
            String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(mNotificationManager) : "";

            Notification.Builder notificationBuilder = new Notification.Builder(this, channelId);
            mNotification = notificationBuilder
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.device_orientation_service_notification_title))
                    .setContentText(getString(R.string.device_orientation_service_notification_text))
                    .setTicker(getString(R.string.device_orientation_service_notification_tickle))
                    .setPriority(Notification.PRIORITY_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();

            TaskStackBuilder localTaskStackBuilder = TaskStackBuilder.create(this);
            localTaskStackBuilder.addParentStack(MainActivity.class);
            localTaskStackBuilder.addNextIntent(mainActivityIntent);
            notificationBuilder.setContentIntent(localTaskStackBuilder.getPendingIntent(0, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE));

            // Setup overlay window before starting the service to be A15 compliant
            E_ORIENTATION targetOrientation = getOrientationSharedPreferences(this);
            Log.d(Constants.TAG, "Setting orientation to:" + targetOrientation.toString());
            setOrientation(this.getApplicationContext(), targetOrientation);

            // Start foreground service
            startForeground(SERVICE_ID, mNotification);

            logD("startService:Service started without error.");
        }
        catch(Exception e)
        {
            logD("startService:Error while starting service.");
            e.printStackTrace();
        }


    }

    private void stopService()
    {
        try
        {
            logD("stopService.");
            stopForeground(true);
            logD("stopService:Service stopped without error.");
        }
        catch(Exception e)
        {
            logD("Error while stopping service.");
            e.printStackTrace();

        }

    }

    public static boolean setOrientation(Context context, E_ORIENTATION targetOrientation) {
        if(targetOrientation == E_ORIENTATION.DISABLED)
        {
            cleanupWindow(context.getApplicationContext());
            return true;
        }
        else
        {
            return createOverlayWindowToForceOrientation(context.getApplicationContext(), targetOrientation);
        }
    }

    public static boolean checkForOverlayPermission(Context context)
    {
        if( Settings.canDrawOverlays(context) == false )
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
            return false;
        }
        return true;
    }

    private static boolean createOverlayWindowToForceOrientation(Context context, E_ORIENTATION targetOrientation) {
        // First check if we can draw an overlay window
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && checkForOverlayPermission(context) == false)
        {
            return false;
        }

        try
        {
            // We save the current state of mView
            // If a view is already existing we wants to remove it correctly.
            View saveView = mView;

            // Retrieve the window service
            if(mWindowManager == null)
                mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

            // Create a new View for our layout
            mView = new View(context);

            // We create a new layout with the following parameters
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

            // The smallest is the stealthiest
            layoutParams.width = 0;
            layoutParams.height = 0;

            // Transparency is a plus... for a zero sized layout
            layoutParams.format = PixelFormat.TRANSPARENT;
            layoutParams.alpha = 0f;

            // We force the window to be not focusable and not touchable to avoid
            // disruptions with the other apps and the launcher
            // In case of someone would manage to "touch" this zero sized sub pixel
            layoutParams.flags =
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

            // The type toast will be accepted by the system without specific permissions
            int windowType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
            layoutParams.type = windowType;

            // Let's setup the orientation we want to force
            layoutParams.screenOrientation = targetOrientation.toActivityInfoValue();

            mWindowManager.addView(mView, layoutParams);
            mView.setVisibility(View.VISIBLE);

            /*
            // Test change DPI
            DisplayMetrics metrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(metrics);

            metrics.density = 0.4f; //2.0f;
            metrics.scaledDensity = 0.4f; //2.0f;
            metrics.densityDpi = 240;
            //metrics.heightPixels = 1280;
            //metrics.widthPixels = 720;
            //metrics.xdpi = 254.0f;
            //metrics.ydpi = 254.0f;
            Configuration config = context.getResources().getConfiguration();
            config.densityDpi = 240;
            context.getResources().updateConfiguration(config, metrics);
            //context.getApplicationContext().getResources().getDisplayMetrics().setTo(metrics);
            //*/

            if(saveView != null)
            {
                saveView.setVisibility(View.GONE);
                mWindowManager.removeView(saveView);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void cleanupWindow(Context context) {
        // Revert to portray mode before cleaning up things
        createOverlayWindowToForceOrientation(context, E_ORIENTATION.SENSOR_PORTRAIT);

        if(mView != null)
        {
            mWindowManager.removeView(mView);
            mView = null;
        }
        if(mWindowManager != null)
        {
            mWindowManager = null;
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        NotificationChannel channel = new NotificationChannel(getString(R.string.deviceorientationservice_channel_id), getString(R.string.deviceorientationservice_channel_name), NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        notificationManager.createNotificationChannel(channel);
        return getString(R.string.deviceorientationservice_channel_id);
    }

    private void logD(String message)
    {
        Log.d(Constants.TAG, message);
    }

    public static void startService(Context context)
    {
        Intent myIntent = new Intent(context, DeviceOrientationService.class);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // Use start foreground service to prevent the runtime error:
            // "not allowed to start service intent app is in background"
            // to happen when running on OS >= Oreo
            context.startForegroundService(myIntent);
        }
        else
        {
            context.startService(myIntent);
        }
    }

    public static void stopService(Context context)
    {
        Intent myIntent = new Intent(context, DeviceOrientationService.class);
        context.stopService(myIntent);
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DeviceOrientationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void setOrientationSharedPreferences(Context context, E_ORIENTATION orientation)
    {
        // Update preferences
        SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(Constants.SHARED_PREFERENCES_ORIENTATION, orientation.toString());
        editor.commit();
    }

    public static E_ORIENTATION getOrientationSharedPreferences(Context context)
    {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String orientation = sharedpreferences.getString(Constants.SHARED_PREFERENCES_ORIENTATION, "auto");
        return E_ORIENTATION.fromString(orientation);
    }
}
