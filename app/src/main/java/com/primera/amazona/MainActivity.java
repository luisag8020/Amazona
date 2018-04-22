package com.primera.amazona;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.primera.amazona.PanicMessage;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.awareness.state.Weather.FAHRENHEIT;

public class MainActivity extends AppCompatActivity {

    private Context mContext;

    String CONTACTS_ALLOWED = "5";

    private GoogleApiClient ApiClient;
    private static final String TAG = "Awareness";


    // Local copy
    String contactNameArray[] = new String[Integer.parseInt(CONTACTS_ALLOWED)];   // gets Name array and number from internet
    String longitudeArray[] = new String[Integer.parseInt(CONTACTS_ALLOWED)];
    String latitudeArray[] = new String[Integer.parseInt(CONTACTS_ALLOWED)];
    String longitudeHistory[] = new String[1];
    String latitudeHistory[] = new String[1];

    // Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference batteryStrRef1;
    DatabaseReference longStrRef1;
    DatabaseReference latStrRef1;
    DatabaseReference altStrRef1;
    DatabaseReference accStrRef1;
    DatabaseReference captureTimeStrRef1;

    DatabaseReference batteryStrRef2;
    DatabaseReference longStrRef2;
    DatabaseReference latStrRef2;
    DatabaseReference altStrRef2;
    DatabaseReference accStrRef2;
    DatabaseReference captureTimeStrRef2;

    DatabaseReference batteryStrRef3;
    DatabaseReference longStrRef3;
    DatabaseReference latStrRef3;
    DatabaseReference altStrRef3;
    DatabaseReference accStrRef3;
    DatabaseReference captureTimeStrRef3;

    DatabaseReference batteryStrRef4;
    DatabaseReference longStrRef4;
    DatabaseReference latStrRef4;
    DatabaseReference altStrRef4;
    DatabaseReference accStrRef4;
    DatabaseReference captureTimeStrRef4;

    DatabaseReference batteryStrRef5;
    DatabaseReference longStrRef5;
    DatabaseReference latStrRef5;
    DatabaseReference altStrRef5;
    DatabaseReference accStrRef5;
    DatabaseReference captureTimeStrRef5;

    DatabaseReference weatherStrRef;

    // Contact info
    DatabaseReference contactName1;
    DatabaseReference contactName2;
    DatabaseReference contactName3;
    DatabaseReference contactName4;
    DatabaseReference contactName5;
    DatabaseReference contactNo1;
    DatabaseReference contactNo2;
    DatabaseReference contactNo3;
    DatabaseReference contactNo4;
    DatabaseReference contactNo5;


    // Network Signal
    int mSignalStrength = 0;
    String strengthString = "";
    int strLevel = 0;
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;


    @TargetApi(17)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences historyLocation = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = historyLocation.edit();
        editor.putString("counterArray", "1");
        editor.commit();



        startService(new Intent(this, locationHistory.class));

        //SharedPreferences contactNumberList = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        //SharedPreferences.Editor editor = contactNumberList.edit();
        //editor.putString("mainAct", MainActivity.this+"");
        //editor.commit();

        // Power button protocol
        startService(new Intent(this, TriggerService.class));


        // Google Awareness API
        ApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Awareness.API)
                .build();
        ApiClient.connect();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) +
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.SEND_SMS},
                    12345
            );

        }

        // Firebase reference
        //mStorageRef = FirebaseStorage.getInstance().getReference();
        batteryStrRef1 = database.getReference("battery1");
        longStrRef1 = database.getReference("location1").child("longitude");
        latStrRef1 = database.getReference("location1").child("latitude");
        altStrRef1 = database.getReference("location1").child("altitude");
        accStrRef1 = database.getReference("location1").child("accuracy");
        captureTimeStrRef1 = database.getReference("location1").child("location Time");

        batteryStrRef2 = database.getReference("battery2");
        longStrRef2 = database.getReference("location2").child("longitude");
        latStrRef2 = database.getReference("location2").child("latitude");
        altStrRef2 = database.getReference("location2").child("altitude");
        accStrRef2 = database.getReference("location2").child("accuracy");
        captureTimeStrRef2 = database.getReference("location2").child("location Time");

        batteryStrRef3 = database.getReference("battery3");
        longStrRef3 = database.getReference("location3").child("longitude");
        latStrRef3 = database.getReference("location3").child("latitude");
        altStrRef3 = database.getReference("location3").child("altitude");
        accStrRef3 = database.getReference("location3").child("accuracy");
        captureTimeStrRef3 = database.getReference("location3").child("location Time");

        batteryStrRef4 = database.getReference("battery4");
        longStrRef4 = database.getReference("location4").child("longitude");
        latStrRef4 = database.getReference("location4").child("latitude");
        altStrRef4 = database.getReference("location4").child("altitude");
        accStrRef4 = database.getReference("location4").child("accuracy");
        captureTimeStrRef4 = database.getReference("location4").child("location Time");

        batteryStrRef5 = database.getReference("battery5");
        longStrRef5 = database.getReference("location5").child("longitude");
        latStrRef5 = database.getReference("location5").child("latitude");
        altStrRef5 = database.getReference("location5").child("altitude");
        accStrRef5 = database.getReference("location5").child("accuracy");
        captureTimeStrRef5 = database.getReference("location5").child("location Time");

        weatherStrRef = database.getReference("weather");

        contactName1 = database.getReference("allContacts").child("contactName1");
        contactName2 = database.getReference("allContacts").child("contactName2");
        contactName3 = database.getReference("allContacts").child("contactName3");
        contactName4 = database.getReference("allContacts").child("contactName4");
        contactName5 = database.getReference("allContacts").child("contactName5");
        contactNo1 = database.getReference("allContacts").child("contactNo1");
        contactNo2 = database.getReference("allContacts").child("contactNo2");
        contactNo3 = database.getReference("allContacts").child("contactNo3");
        contactNo4 = database.getReference("allContacts").child("contactNo4");
        contactNo5 = database.getReference("allContacts").child("contactNo5");

        // Because this happens every time
        populateLocalCopyContacts();

        populateLocationCopy();


        contextAware awareness = new contextAware(MainActivity.this);
        awareness.getNetwork();
        battery();
        getActivity();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        Map<String, String> allPreferences = (Map<String, String>) sharedPreferences.getAll();
        String lat = allPreferences.get("latitude");
//        longitude = allPreferences.get("longitude");
//        accuracy = allPreferences.get("accuracy");
//        timeCapture = allPreferences.get("timeCapture");
//        Log.i("got?", "lat" + lat);


        // Update button
        Button updateBtn = (Button)findViewById(R.id.UpdateButton);
        updateBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                initSnapshots();
                //getMultimediaMessage();  to test if when presses, it will take a photo in the background
            }
        });

        Button recordButton = (Button) findViewById(R.id.RecordButton);
        recordButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                recordVideo(v);
            }
        });



        // Network strength
        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        // Deactivate message
        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PanicAlert panicAlert = new PanicAlert(getApplicationContext());
                panicAlert.deActivate();

               // PanicMessage panicMessage = new PanicMessage(getApplicationContext());
               // panicMessage.sendAlertMessage();
            }
        });

    }

    // Obtains and prints all background information about the user
    @TargetApi(23)
    private void initSnapshots() {

        battery();
        getActivity();
        //getLocation();     // Get location and weather
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
        batteryStrRef1.setValue(battery);
        SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = saveLocation.edit();
        editor.putString("battery", battery);
        editor.commit();
    }

    public void getMultimediaMessage() {
        new multimediaMessage(MainActivity.this).openCamera();
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

                        int activityDetect = probableActivity.getType();
                        String activityUser = "";
                        if (activityDetect == 0) {activityUser = "IN A VEHICLE";}
                        else if (activityDetect == 1) {activityUser = "ON FOOT";}
                        else if (activityDetect == 2) {activityUser = "ON A BICYCLE";}
                        else if (activityDetect == 3) {activityUser = "STATIONARY";}
                        else if (activityDetect == 4) {activityUser = "UNKOWN ACTIVITY";}
                        else if (activityDetect == 5) {activityUser = "TILTING";}
                        else if (activityDetect == 7) {activityUser = "WALKING";}
                        else if (activityDetect == 8) {activityUser = "RUNNING";}


                        //toString() + "Type: " + String.valueOf(probableActivity.getType());
                        Log.i(TAG, probableActivity.toString() + " " + String.valueOf(probableActivity.getType()));
                        activityText.setText(activityUser);

                        SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = saveLocation.edit();
                        editor.putString("detectedActivity", activityUser);
                        editor.commit();
                    }
                });

    }

    // Get location and weather
//    private void getLocation() {
//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            Awareness.SnapshotApi.getLocation(ApiClient).setResultCallback(new ResultCallback<LocationResult>() {
//                @RequiresApi(api = Build.VERSION_CODES.M)
//                @Override
//                public void onResult(@NonNull LocationResult locationResult) {
//                    TextView locationText = (TextView) findViewById(R.id.locationText);
//
//                    if (!locationResult.getStatus().isSuccess()) {
//                        String location = "Could not get location.";
//                        Log.e(TAG, "Could not get location.");
//                        locationText.setText(location);
//                        latitudeHistory[0] = location;
//                        longitudeHistory[0] = location;
//                        longStrRef1.setValue(location);
//                        latStrRef1.setValue(location);
//
//                        return;
//                    }
//
//                    Location location = locationResult.getLocation();
//                    latitudeHistory[0] = location.getLatitude() + "";
//                    longitudeHistory[0] = location.getLongitude() + "";
//                    String locString = "Location: " +
//                            " \nCapture Time: " + location.getTime() +
//                            " \nLat: " + location.getLatitude() +
//                            " \nLon: " + location.getLongitude() +
//                            " \nAlt: " + location.getAltitude() +
//                            " \nAccuracy: " + location.getAccuracy();
//                    locationText.setText(locString);
//                    latStrRef1.setValue(location.getLatitude());
//                    longStrRef1.setValue(location.getLongitude());
//                    altStrRef1.setValue(location.getAltitude());
//                    accStrRef1.setValue(location.getAccuracy());
//                    captureTimeStrRef1.setValue(location.getTime());
//
//
//                    Log.i(TAG, "Time:" + location.getTime()); // UTC time
//                    Log.i(TAG, "Accuracy:" + location.getAccuracy());
//                    Log.i(TAG, "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude() + ", Alt: " + location.getAltitude());
//                }
//            });
//        }
//        else {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    12345
//            );
//        }
//
//        Awareness.SnapshotApi.getWeather(ApiClient).setResultCallback(new ResultCallback<WeatherResult>() {
//            @Override
//            public void onResult(@NonNull WeatherResult weatherResult) {
//                TextView weatherText = (TextView) findViewById(R.id.weatherText);
//                if (!weatherResult.getStatus().isSuccess()) {
//                    String weatherString = "Could not get weather.";
//                    Log.e(TAG, "Could not get weather.");
//                    weatherText.setText(weatherString);
//                    weatherStrRef.setValue(weatherString);
//                    return;
//                }
//
//                Weather weather = weatherResult.getWeather();
//                String weatherString = "Weather: " + weather.getTemperature(FAHRENHEIT);
//                weatherText.setText(weatherString);
//                weatherStrRef.setValue(weatherString);
//                Log.i(TAG, "Weather Conditions:" + Arrays.toString(weather.getConditions()));
//                Log.i(TAG, "Weather Temperature:" + weather.getTemperature(FAHRENHEIT));
//                Log.i(TAG, "Weather: " + weather);
//            }
//        });
//
//    }


//    public int getLocation() {
//        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            Awareness.SnapshotApi.getLocation(ApiClient).setResultCallback(new ResultCallback<LocationResult>() {
//                @RequiresApi(api = Build.VERSION_CODES.M)
//                @Override
//                public void onResult(@NonNull LocationResult locationResult) {
//
//                    if (!locationResult.getStatus().isSuccess()) {
//                        String location = "Could not get location.";
//                        Log.e("loc", "Could not get location.");
//                        return;
//                    }
//
//                    // FOR LOC HISTORY, MAKE FOR LOOP USING A VARIABLE TO BE SET BY CLIENT IN SETTINGS
//
//                    SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//                    SharedPreferences.Editor editor = saveLocation.edit();
//
//                    Location location = locationResult.getLocation();
//                    editor.putString("longitude", location.getLongitude()+"");
//                    editor.putString("latitude", location.getLatitude()+"");
//                    editor.putString("accuracy", location.getAccuracy()+"");
//                    editor.putString("timeCapture", location.getTime()+"");
//                    editor.commit();
//                }
//            });
//        } else {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    12345
//            );
//        }
//
//        return 1;
//    }


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

            SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = saveLocation.edit();
            editor.putString("networkType", networkType);
            editor.commit();
        }

    }

    // Detect if headphones are plugged in
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

            SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = saveLocation.edit();
            editor.putString("strengthString", strengthString);
            editor.putString("mSignalStrength", mSignalStrength+"");
            editor.putString("strLevel", strLevel +"");
            editor.commit();
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

    private void populateLocationCopy() {
        longStrRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                longitudeArray[0] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        latStrRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                latitudeArray[0] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        longStrRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                longitudeArray[1] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        latStrRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                latitudeArray[1] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        longStrRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                longitudeArray[2] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        latStrRef3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                latitudeArray[2] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        longStrRef4.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                longitudeArray[3] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        latStrRef4.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                latitudeArray[3] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        longStrRef5.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                longitudeArray[4] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        latStrRef5.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                latitudeArray[4] = value;
                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void populateLocalCopyContacts() {


        contactName1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                contactNameArray[0] = value;

                //Log.i("contact Name1", contactNameArray[0]);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactName2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                contactNameArray[1] = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactName3.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                contactNameArray[2] = value;

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactName4.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                contactNameArray[3] = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactName5.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                contactNameArray[4] = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


        contactNo1.addValueEventListener(new ValueEventListener() {

            SharedPreferences contactNumberList = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = contactNumberList.edit();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                String value = dataSnapshot.getValue(String.class);
                editor.putString("contactNo0", value);
                editor.commit();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactNo2.addValueEventListener(new ValueEventListener() {
            SharedPreferences contactNumberList = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = contactNumberList.edit();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                editor.putString("contactNo1", value);
                editor.commit();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactNo3.addValueEventListener(new ValueEventListener() {
            SharedPreferences contactNumberList = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = contactNumberList.edit();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                editor.putString("contactNo2", value);
                editor.commit();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactNo4.addValueEventListener(new ValueEventListener() {
            SharedPreferences contactNumberList = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = contactNumberList.edit();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                editor.putString("contactNo3", value);
                editor.commit();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        contactNo5.addValueEventListener(new ValueEventListener() {
            SharedPreferences contactNumberList = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = contactNumberList.edit();

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                editor.putString("contactNo4", value);
                editor.commit();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }



}
