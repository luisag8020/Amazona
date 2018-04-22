package com.primera.amazona;

import android.*;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import static android.content.ContentValues.TAG;

public class locationHistory extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("locationHistory", "locationHistory CREATED.");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        //powerTriggerReceiver = new TriggerReceiver();
        registerReceiver(new GpsLocationReceiver(), filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("powerButton", "locationHistory DESTROYED.");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        unregisterReceiver(new GpsLocationReceiver());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
