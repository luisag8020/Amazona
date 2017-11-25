package com.primera.amazona;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.util.Arrays;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
import static com.google.android.gms.awareness.state.Weather.FAHRENHEIT;

public class MainActivity extends AppCompatActivity {

    private GoogleApiClient ApiClient;
    private static final String TAG = "Awareness";
    //public final static int REQUEST_CODE = 10101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Google Awareness API
        ApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Awareness.API)
                .build();
        ApiClient.connect();


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    12345
            );
        }


        // Update button
        Button updateBtn = (Button)findViewById(R.id.UpdateButton);
        updateBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                initSnapshots();
            }
        });

        // For power button
        Receiver powerBroadCastReceiver = new Receiver();
        IntentFilter screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF); // TO-DO: Add multiple presses
        registerReceiver(powerBroadCastReceiver, screenStateFilter);
    }

    public void recordVideo (View view) {
        Intent intent = new Intent (this, Recorder.class);
        startActivity(intent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregisters BroadcastReceiver when app is destroyed.
    }

    // For power button
    public class Receiver extends BroadcastReceiver {
        private boolean screenOff;

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                screenOff = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                screenOff = false;
            }
            Intent i = new Intent(context, MainActivity.class);
            i.putExtra("screen_state", screenOff);
            context.startService(i);
        }
    }


    private void initSnapshots() {

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int)((level / (float)scale) * 100);
        TextView batteryText=(TextView)findViewById(R.id.batteryText);
        String battery = String.valueOf(batteryPct);
        Log.i(TAG, "Battery: " + batteryPct);
        batteryText.setText("Battery: " + battery);


        // Detect user's activity
        Awareness.SnapshotApi.getDetectedActivity(ApiClient)
        .setResultCallback(new ResultCallback<DetectedActivityResult>() {
            @Override
            public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
                TextView activityText=(TextView)findViewById(R.id.activityText);

            if (!detectedActivityResult.getStatus().isSuccess()) {
                String activity = "Could not get the current activity.";
                Log.e(TAG, "Could not get the current activity.");
                activityText.setText(activity);
                return;
            }
            ActivityRecognitionResult aResult = detectedActivityResult.getActivityRecognitionResult();
            DetectedActivity probableActivity = aResult.getMostProbableActivity();

            String testing = probableActivity.toString();
                    //toString() + "Type: " + String.valueOf(probableActivity.getType());
            Log.i(TAG, probableActivity.toString() + " " + String.valueOf(probableActivity.getType()));
            activityText.setText(testing);
            }
        });

        // Check if headphones are plugged in
        Awareness.SnapshotApi.getHeadphoneState(ApiClient)
            .setResultCallback(new ResultCallback<HeadphoneStateResult>() {
                @Override
                public void onResult(@NonNull HeadphoneStateResult headphoneStateResult) {
                    TextView headphoneText=(TextView)findViewById(R.id.headphoneText);

                    if (!headphoneStateResult.getStatus().isSuccess()) {
                        String headphoneString = "Could not get headphone state.";
                        Log.e(TAG, "Could not get headphone state.");
                        headphoneText.setText(headphoneString);

                        return;
                    }
                    HeadphoneState headphoneState = headphoneStateResult.getHeadphoneState();
                    if (headphoneState.getState() == HeadphoneState.PLUGGED_IN) {
                        String headphoneString = "Headphones are plugged in";
                        Log.i(TAG, "Headphones are plugged in.\n");
                        headphoneText.setText(headphoneString);
                    } else {
                        // HeadphoneState.UNPLUGGED
                        String headphoneString = "Headphones are NOT plugged in";
                        Log.i(TAG, "Headphones are NOT plugged in.\n");
                        headphoneText.setText(headphoneString);
                    }
                }
            });


//        if (ContextCompat.checkSelfPermission(MainActivity.this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    12345
//            );
//        }

        // hasAccuracy()      hasAltitude()    getProvider()
        // set(Location l)

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                Awareness.SnapshotApi.getLocation(ApiClient)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onResult(@NonNull LocationResult locationResult) {
                            TextView locationText = (TextView) findViewById(R.id.locationText);


                            if (!locationResult.getStatus().isSuccess()) {
                                String location = "Could not get location.";
                                Log.e(TAG, "Could not get location.");
                                locationText.setText(location);
                                return;
                            }
                            Location location = locationResult.getLocation();
                            String locString = "Location: " +
                                    " \nCapture Time: " + location.getTime() +
                                    " \nLat: " + location.getLatitude() +
                                    " \nLon: " + location.getLongitude() +
                                    " \nAlt: " + location.getAltitude() +
                                    " \nAccuracy: " + location.getAccuracy();
                            locationText.setText(locString);
                            Log.i(TAG, "Time:" + location.getTime()); // UTC time
                            Log.i(TAG, "Accuracy:" + location.getAccuracy());
                            Log.i(TAG, "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude() + ", Alt: " + location.getAltitude());
                        }
                    });


                Awareness.SnapshotApi.getWeather(ApiClient)
                    .setResultCallback(new ResultCallback<WeatherResult>() {
                        @Override
                        public void onResult(@NonNull WeatherResult weatherResult) {
                            TextView weatherText = (TextView) findViewById(R.id.weatherText);
                            if (!weatherResult.getStatus().isSuccess()) {
                                String weatherString = "Could not get weather.";
                                Log.e(TAG, "Could not get weather.");
                                weatherText.setText(weatherString);
                                return;
                            }
                            Weather weather = weatherResult.getWeather();
                            String weatherString = "Weather: " + weather.getTemperature(FAHRENHEIT);
                            weatherText.setText(weatherString);
                            Log.i(TAG, "Weather Conditions:" + Arrays.toString(weather.getConditions()));
                            Log.i(TAG, "Weather Temperature:" + weather.getTemperature(FAHRENHEIT));
                            Log.i(TAG, "Weather: " + weather);
                        }
                    });
        }
        else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        12345
                );
        }


        // get access to camera

        // get network status
        boolean connected = false;
        TextView connectText=(TextView)findViewById(R.id.connectText);
        String connectString = "let's check";
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            connectString = "connected";
            Log.i(TAG, "connected");
            connectText.setText(connectString);

        }
        else {
            connected = false;
            connectString = "not connected";
            Log.i(TAG, "not connected");
            connectText.setText(connectString);
        }

        // Specific network type
        if (connected == true) {
            TextView networkText=(TextView)findViewById(R.id.networkText);
            String networkType = "Network Type: " + networkType();
            networkText.setText(networkType);
            Log.i(TAG, "network type: " + networkType);
        }

    }

    private String networkType() {
        TelephonyManager teleMan = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = teleMan.getNetworkType();
        switch (networkType) {
            // 2G
            case TelephonyManager.NETWORK_TYPE_GPRS: return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE: return "EDGE"; // E 2G: 50-100kbps down/up
            case TelephonyManager.NETWORK_TYPE_CDMA: return "CDMA";
            case TelephonyManager.NETWORK_TYPE_1xRTT: return "1xRTT"; // 2G: 50-100Kbps Single-Carrier Radio Transmission Technology 1.25MHz channel for data transfer
            case TelephonyManager.NETWORK_TYPE_IDEN: return "iDen";

            // 3G
            case TelephonyManager.NETWORK_TYPE_UMTS: return "UMTS"; //3G
            case TelephonyManager.NETWORK_TYPE_EVDO_0: return "EVDO rev. 0"; // 3G Down: 400-1000kbps (w/ bursts 2.0mbps). Up: 50-100kbps (burst 144mbps)
            case TelephonyManager.NETWORK_TYPE_EVDO_A: return "EVDO rev. A"; //3G Download:600-1400kbps (bursts to 3.1mbps). Up: 500kbps-800kbps (burst 1.8mbps)
            case TelephonyManager.NETWORK_TYPE_HSDPA: return "HSDPA"; //3G
            case TelephonyManager.NETWORK_TYPE_HSUPA: return "HSUPA"; //3G 28-40mbps
            case TelephonyManager.NETWORK_TYPE_HSPA: return "HSPA"; //3G < 14,4mbps
            case TelephonyManager.NETWORK_TYPE_EVDO_B: return "EVDO rev. B";
            case TelephonyManager.NETWORK_TYPE_EHRPD: return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP: return "HSPA+"; //3G 21-42mbps

            // 4G
            case TelephonyManager.NETWORK_TYPE_LTE: return "LTE"; //4G

            case TelephonyManager.NETWORK_TYPE_GSM: return "GSM";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA: return "TD SCDMA";
            case TelephonyManager.NETWORK_TYPE_IWLAN: return "IWLAN";

            case TelephonyManager.NETWORK_TYPE_UNKNOWN: return "Unknown";
        }
        throw new RuntimeException("New type of network");
    }



    /*public class ActivityRecognizedService extends IntentService {

        public ActivityRecognizedService() {
            super("ActivityRecognizedService");
        }

        public ActivityRecognizedService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
        }
    }*/
}
