package com.primera.amazona;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
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
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;

import static com.google.android.gms.awareness.state.Weather.FAHRENHEIT;

public class MainActivity extends AppCompatActivity {

    private GoogleApiClient ApiClient;
    private static final String TAG = "Awareness";
    //public final static int REQUEST_CODE = 10101;

    // Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference batteryStrRef;
    DatabaseReference longStrRef;
    DatabaseReference latStrRef;
    DatabaseReference altStrRef;
    DatabaseReference accStrRef;
    DatabaseReference captureTimeStrRef;
    DatabaseReference weatherStrRef;




    // Network Signal
    int mSignalStrength = 0;
    String strengthString = "";
    int strLevel = 0;
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;



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
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    12345
            );
        }

        // Firebase reference
        //mStorageRef = FirebaseStorage.getInstance().getReference();
        batteryStrRef = database.getReference("battery");
        longStrRef = database.getReference("location").child("longitude");
        latStrRef = database.getReference("location").child("latitude");
        altStrRef = database.getReference("location").child("altitude");
        accStrRef = database.getReference("location").child("accuracy");
        captureTimeStrRef = database.getReference("location").child("location Time");
        weatherStrRef = database.getReference("weather");



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

        // Network strength
        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }

    // Obtains and prints all background information about the user
    @TargetApi(23)
    private void initSnapshots() {

        battery();
        getActivity();
        getLocation();     // Get location and weather
        getNetwork();

    }

    // Goes to record page upon button press
    public void recordVideo (View view) {
        Intent intent = new Intent (this, Recorder.class);
        startActivity(intent);
    }

    // Goes to emergency contact page upon button press
    public void emergencyContacts (View view) {
        Intent intent = new Intent (this, EmergencyContacts.class);
        startActivity(intent);
    }

    // Goes to settings page upon button press
    public void settings (View view) {
        Intent intent = new Intent (this, SettingsActivity.class);
        startActivity(intent);
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



    // Battery level
    private void battery() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int)((level / (float)scale) * 100);
        TextView batteryText=(TextView)findViewById(R.id.batteryText);
        String battery = String.valueOf(batteryPct);
        Log.i(TAG, "Battery: " + batteryPct);
        batteryText.setText("Battery: " + battery);
        batteryStrRef.setValue(battery);
    }

    // Detect user's activity
    private void getActivity() {
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

    }

    // Get location and weather
    private void getLocation() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getLocation(ApiClient).setResultCallback(new ResultCallback<LocationResult>() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onResult(@NonNull LocationResult locationResult) {
                    TextView locationText = (TextView) findViewById(R.id.locationText);

                    if (!locationResult.getStatus().isSuccess()) {
                        String location = "Could not get location.";
                        Log.e(TAG, "Could not get location.");
                        locationText.setText(location);
                        longStrRef.setValue(location);
                        latStrRef.setValue(location);

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
                    latStrRef.setValue(location.getLatitude());
                    longStrRef.setValue(location.getLongitude());
                    altStrRef.setValue(location.getAltitude());
                    accStrRef.setValue(location.getAccuracy());
                    captureTimeStrRef.setValue(location.getTime());


                    Log.i(TAG, "Time:" + location.getTime()); // UTC time
                    Log.i(TAG, "Accuracy:" + location.getAccuracy());
                    Log.i(TAG, "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude() + ", Alt: " + location.getAltitude());
                }
            });
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    12345
            );
        }

        Awareness.SnapshotApi.getWeather(ApiClient).setResultCallback(new ResultCallback<WeatherResult>() {
            @Override
            public void onResult(@NonNull WeatherResult weatherResult) {
                TextView weatherText = (TextView) findViewById(R.id.weatherText);
                if (!weatherResult.getStatus().isSuccess()) {
                    String weatherString = "Could not get weather.";
                    Log.e(TAG, "Could not get weather.");
                    weatherText.setText(weatherString);
                    weatherStrRef.setValue(weatherString);
                    return;
                }

                Weather weather = weatherResult.getWeather();
                String weatherString = "Weather: " + weather.getTemperature(FAHRENHEIT);
                weatherText.setText(weatherString);
                weatherStrRef.setValue(weatherString);
                Log.i(TAG, "Weather Conditions:" + Arrays.toString(weather.getConditions()));
                Log.i(TAG, "Weather Temperature:" + weather.getTemperature(FAHRENHEIT));
                Log.i(TAG, "Weather: " + weather);
            }
        });

    }

    // Obtains type and strength of network and prints it
    private void getNetwork() {
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
            String networkType = "Network Type: " + networkType() +
                    "\n Strength Level: " + strLevel +
                    "\n Strength: " + mSignalStrength +
                    "\n String:  " + strengthString;
            networkText.setText(networkType);
            Log.i(TAG, "network type: " + networkType);
        }

    }

    // Detect if headphones are pluged in
    private void getHeadphones() {
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
    }


    // Helper method for signal strength of network
    @TargetApi(23)
    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            strengthString = signalStrength.toString();
            mSignalStrength = signalStrength.getGsmSignalStrength();
            //mSignalStrength = (2 * mSignalStrength) - 113; // -> dBm
            strLevel = signalStrength.getLevel(); // 0 -> lowest
        }
    }

    // Helper method for network
    private String networkType() {
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = tManager.getNetworkType();

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

}
