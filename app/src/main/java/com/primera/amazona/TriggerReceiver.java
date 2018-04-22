package com.primera.amazona;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.primera.amazona.MainActivity;
import com.primera.amazona.PanicAlert;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;

/**
 * Created by queenlu on 2/20/18.
 */

public class TriggerReceiver extends BroadcastReceiver {
    private static final String TAG = TriggerReceiver.class.getName();
    protected MultiClickEvent multiClickEvent;

    public TriggerReceiver() {
        resetEvent();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(">>>>>>>", "in onReceive of TriggerReceiver");
        if (intent.getAction().equals(ACTION_SCREEN_OFF) || intent.getAction().equals(ACTION_SCREEN_ON)) {
            // captures the first power button press and passes time of press
            multiClickEvent.registerClick(System.currentTimeMillis());

            if(multiClickEvent.skipCurrentClick()){
                Log.e("*****", "skipped click");
                multiClickEvent.resetSkipCurrentClickFlag();
            }

            else if(multiClickEvent.canStartVibration()){
                Log.e("*****", "vibration started");
                PanicAlert panicAlert = new PanicAlert(context);
                panicAlert.vibrate();
            }

            else if (multiClickEvent.isActivated()) {
                Log.e("*****", "alerts activated");
                onActivation(context);
                resetEvent();
            }
        }
    }

    protected void onActivation(Context context) {
        activateAlert(new PanicAlert(context));
        Log.e(">>>>>>>", "we are in onActivation of TriggerReceiver");

    }

    void activateAlert(PanicAlert panicAlert) {

        panicAlert.activate();
    }

    protected void resetEvent() {
        multiClickEvent = new MultiClickEvent();
    }

}





