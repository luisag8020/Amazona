package com.primera.amazona;

/**
 * Created by queenlu on 2/20/18.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.primera.amazona.Intents;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.primera.amazona.AppConstants;
import com.primera.amazona.multimediaMessage;

import com.primera.amazona.ApplicationSettings;
import com.primera.amazona.CurrentLocationProvider;


import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

import static com.primera.amazona.Intents.locationPendingIntent;


public class PanicAlert extends AppCompatActivity{
    private int AUDIO_FLAG = 444;
    public static final int MAX_RETRIES = 20;
    public static final int LOCATION_WAIT_TIME = 3000;
    private static final String TAG = PanicAlert.class.getName();
    private LocationManager locationManager;
    private Context context;
    private AlarmManager alarmManager1, alarmManager2;
    private static final String PREFIX = "com.primera.amazona";
    public static final String LOCATION_UPDATE_ACTION = PREFIX + ".LOCATION_UPDATE_ACTION";



    public PanicAlert(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        alarmManager1 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager2 = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }


    private static void setDefaultPackage(Context context, Intent startMain) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(homeIntent, 0);
        if (activities.size() > 0) {
            String className = activities.get(0).activityInfo.packageName;
            startMain.setPackage(className);
        }
    }

    public static void close(Context context) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        setDefaultPackage(context, startMain);

        context.startActivity(startMain);
    }

    public void activate() {
        close(context);
        vibrateOnce();

        ApplicationSettings.setAlertActive(context, true);
        getExecutorService().execute(
                new Runnable() {
                    @Override
                    public void run() {
                        Log.e(">>>>>>", "Gets to activateAlert()");
                        activateAlert();
                    }
                }
        );
    }

    private void vibrateOnce() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(AppConstants.ALERT_CONFIRMATION_VIBRATION_DURATION);
    }

    private void activateAlert() {
        ApplicationSettings.setAlertActive(context, true);
        registerLocationUpdate();
        sendFirstAlert();
        scheduleFutureAlert();
        //getMultimediaMessage();
    }

    public void deActivate() {
        Log.i("deact", "Deactivating???");
        ApplicationSettings.setAlertActive(context, false);

        locationManager.removeUpdates(PendingIntent.getBroadcast(context, 0, new Intent(LOCATION_UPDATE_ACTION), FLAG_UPDATE_CURRENT));
        alarmManager2.cancel(Intents.alarmPendingIntent(context));
        ApplicationSettings.setFirstMsgWithLocationTriggered(context, false);
        ApplicationSettings.setFirstMsgSent(context, false);
        createPanicMessage().sendStopAlertMessage();
    }


    private void sendFirstAlert() {
        // Need battery, network
        // add the context awareness stuff here and choose how to send here

        CurrentLocationProvider currentLocationProvider = new CurrentLocationProvider(context);
        //Location loc = currentLocationProvider.getLocation();
        Location loc = getLocation(currentLocationProvider);


        // If location not available, schedule it for later
        if(loc != null) {
            ApplicationSettings.setFirstMsgWithLocationTriggered(context, true);
        } else {
            scheduleFirstLocationAlert();
        }


        // Context
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, String> allPreferences = (Map<String, String>) sharedPreferences.getAll();
        String networkType = allPreferences.get("networkType");
        String strengthString = allPreferences.get("strengthString");
        String mSignalStrength = allPreferences.get("mSignalStrength");
        int strLevel = Integer.parseInt(allPreferences.get("strLevel"));
        int battery = Integer.parseInt(allPreferences.get("battery"));
        Log.i("information", battery + networkType + strLevel);


       // if (battery >= 30 && networkType.equals("LTE")) {
            // Send full LTE message with latest gps location
        Log.i("passes", "passes if");

        setSystemSound();

        createPanicMessage().sendHistoryMessage();
        createPanicMessage().sendAlertMessage(loc);
    }

    private Location getLocation(CurrentLocationProvider currentLocationProvider) {
        Location location = null;
        int retryCount = 0;

        while (retryCount < MAX_RETRIES && location == null) {
            location = currentLocationProvider.getLocation();
            if (location == null) {
                try {
                    retryCount++;
                    Thread.sleep(LOCATION_WAIT_TIME);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Location wait InterruptedException", e);
                }
            }
        }
        return location;
    }


//    CurrentLocationProvider getCurrentLocationProvider() {
//        return new CurrentLocationProvider(context);
//    }



    private void registerLocationUpdate() {

//        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
//            locationManager.requestLocationUpdates(GPS_PROVIDER, AppConstants.GPS_MIN_TIME_IN_FIRST_ONE_MINUTE, AppConstants.GPS_MIN_DISTANCE, locationPendingIntent(context));
//        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
//            locationManager.requestLocationUpdates(NETWORK_PROVIDER, AppConstants.NETWORK_MIN_TIME_IN_FIRST_ONE_MINUTE, AppConstants.NETWORK_MIN_DISTANCE, locationPendingIntent(context));
//
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) +
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            int threadRunCount = 0;
            while (!ApplicationSettings.isFirstMsgWithLocationTriggered(context) && threadRunCount < 4) {
                try {
                    Thread.sleep(20000);
                    threadRunCount++;

                    if (locationManager != null && locationPendingIntent(context) != null) {
                        locationManager.removeUpdates(locationPendingIntent(context));
                    }
                    if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                        locationManager.requestLocationUpdates(GPS_PROVIDER, AppConstants.GPS_MIN_TIME_IN_FIRST_ONE_MINUTE, AppConstants.GPS_MIN_DISTANCE, locationPendingIntent(context));
                    if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                        locationManager.requestLocationUpdates(NETWORK_PROVIDER, AppConstants.NETWORK_MIN_TIME_IN_FIRST_ONE_MINUTE, AppConstants.NETWORK_MIN_DISTANCE, locationPendingIntent(context));
                    Log.e(">>>>>>>>", "threadRunCount = " + threadRunCount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (locationManager != null && locationPendingIntent(context) != null) {
                locationManager.removeUpdates(locationPendingIntent(context));
            }
            if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
                locationManager.requestLocationUpdates(GPS_PROVIDER, AppConstants.GPS_MIN_TIME, AppConstants.GPS_MIN_DISTANCE, locationPendingIntent(context));
            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
                locationManager.requestLocationUpdates(NETWORK_PROVIDER, AppConstants.NETWORK_MIN_TIME, AppConstants.NETWORK_MIN_DISTANCE, locationPendingIntent(context));

        }
    }

    @TargetApi(23)
    private void setSystemSound() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences.getBoolean("soundRadio",false) == true) {
            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AUDIO_FLAG);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        }

        else if (sharedPreferences.getBoolean("silenceRadio",false) == true) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !notificationManager.isNotificationPolicyAccessGranted()) {

                Intent intent = new Intent(
                        android.provider.Settings
                                .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

                startActivity(intent);
            }

            if (notificationManager.isNotificationPolicyAccessGranted()) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

            }
        }

        else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    PanicMessage createPanicMessage() {
        return new PanicMessage(context);
    }

    private void scheduleFirstLocationAlert() {
        PendingIntent alarmPendingIntent = Intents.singleAlarmPendingIntent(context);
        long firstTimeTriggerAt = SystemClock.elapsedRealtime() + AppConstants.ONE_MINUTE * 1;             // we schedule this alarm after 1 minute
        alarmManager1.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTimeTriggerAt, alarmPendingIntent);
//        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTimeTriggerAt, interval, alarmPendingIntent);
    }

    private void scheduleFutureAlert() {
        PendingIntent alarmPendingIntent = Intents.alarmPendingIntent(context);
        long firstTimeTriggerAt = SystemClock.elapsedRealtime() + AppConstants.ONE_MINUTE * ApplicationSettings.getAlertDelay(context);
        long interval = AppConstants.ONE_MINUTE * ApplicationSettings.getAlertDelay(context); // every 5 minutes
        alarmManager2.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTimeTriggerAt, interval, alarmPendingIntent);
    }


    //public boolean isActive() {
     //   return ApplicationSettings.isAlertActive(context);
    //}

    public void vibrate() {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(AppConstants.HAPTIC_FEEDBACK_DURATION);
    }

    public void getMultimediaMessage() {
        new multimediaMessage(context).openCamera();
    }

    ExecutorService getExecutorService() {
        return Executors.newSingleThreadExecutor();
    }
}