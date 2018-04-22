package com.primera.amazona;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by queenlu on 2/20/18.
 */

public class TriggerService extends Service {
    private TriggerReceiver powerTriggerReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("powerButton", "TriggerService CREATED.");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        powerTriggerReceiver = new TriggerReceiver();
        registerReceiver(powerTriggerReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("powerButton", "TriggerService DESTROYED.");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        unregisterReceiver(powerTriggerReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
