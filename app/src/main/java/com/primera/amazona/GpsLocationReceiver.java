package com.primera.amazona;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by queenlu on 4/20/18.
 */

public class GpsLocationReceiver extends BroadcastReceiver{


    Context context;
    DatabaseReference Lat1;
    DatabaseReference Lon1;
    DatabaseReference time1;

    DatabaseReference Lat2;
    DatabaseReference Lon2;
    DatabaseReference time2;

    DatabaseReference Lat3;
    DatabaseReference Lon3;
    DatabaseReference time3;

    DatabaseReference Lat4;
    DatabaseReference Lon4;
    DatabaseReference time4;

    DatabaseReference Lat5;
    DatabaseReference Lon5;
    DatabaseReference time5;
    DatabaseReference error;


    int counterArray = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference counter;
        counter = database.getReference("LocationHistory").child("counter");
        counter.setValue(1);

        SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = saveLocation.edit();

        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {

            //FirebaseDatabase database = FirebaseDatabase.getInstance();
            error = database.getReference("Error");
            Lat1 = database.getReference("LocationHistory").child("Lat1");
            Lon1 = database.getReference("LocationHistory").child("Lon1");
            time1 = database.getReference("LocationHistory").child("time1");

            Lat2 = database.getReference("LocationHistory").child("Lat2");
            Lon2 = database.getReference("LocationHistory").child("Lon2");
            time2 = database.getReference("LocationHistory").child("time2");

            Lat3 = database.getReference("LocationHistory").child("Lat3");
            Lon3 = database.getReference("LocationHistory").child("Lon3");
            time3 = database.getReference("LocationHistory").child("time3");

            Lat4 = database.getReference("LocationHistory").child("Lat4");
            Lon4 = database.getReference("LocationHistory").child("Lon4");
            time4 = database.getReference("LocationHistory").child("time4");

            Lat5 = database.getReference("LocationHistory").child("Lat5");
            Lon5 = database.getReference("LocationHistory").child("Lon5");
            time5 = database.getReference("LocationHistory").child("time5");


            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );
            //boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            Handler handler = new Handler();

            while (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                counterArray = Integer.parseInt(saveLocation.getString("counterArray", "null"));

                updateInfo(counterArray);
                counterArray++;

                if (counterArray == 6) {
                    counterArray = 1;
                }

                counter.setValue(counterArray); // Firebase
                editor.putString("counterArray" , Integer.toString(counterArray)).commit(); // local copy

                long tempTime;
                try {
                    SharedPreferences locInterval = PreferenceManager.getDefaultSharedPreferences(context);
                    String locationInterval  = locInterval.getString("locationInterval", "none");
                    if (locationInterval.equals("10"))
                        tempTime = AppConstants.TEN_MINUTES;
                    else if (locationInterval.equals("15"))
                        tempTime = AppConstants.FIFTEEN_MINUTES;
                    else if (locationInterval.equals("30"))
                        tempTime = AppConstants.THIRTY_MINUTES;
                    else if (locationInterval.equals("60"))
                        tempTime = AppConstants.ONE_HOUR;
                    else // default
                        tempTime = AppConstants.FIVE_MINUTES;

                    Thread.sleep(tempTime);}
                catch(InterruptedException e) {}

            }
        }
    }

    // Captures and stores location history
    private void updateInfo(int counter) {
        CurrentLocationProvider currentLocationProvider = new CurrentLocationProvider(context);
        Location loc = currentLocationProvider.getLocation();

        SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = saveLocation.edit();


        if (counter == 1) {
            //Lat1.setValue("its getting to 1");

            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            long time = loc.getTime();

            Lat1.setValue(lat);
            Lon1.setValue(lon);
            time1.setValue(time);

            editor.putString("Lat1", Double.toString(lat));
            editor.putString("Lon1", Double.toString(lon));
            editor.putString("time1", Double.toString(time));
            editor.commit();
        }

        else if (counter == 2) {
            //Lat2.setValue("its getting to 2");

            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            long time = loc.getTime();

            Lat2.setValue(loc.getLatitude());
            Lon2.setValue(loc.getLongitude());
            time2.setValue(loc.getTime());

            editor.putString("Lat2", Double.toString(lat));
            editor.putString("Lon2", Double.toString(lon));
            editor.putString("time2", Double.toString(time));
            editor.commit();
        }

        else if (counter == 3) {
            //Lat3.setValue("its getting to 3");
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            long time = loc.getTime();

            Lat3.setValue(loc.getLatitude());
            Lon3.setValue(loc.getLongitude());
            time3.setValue(loc.getTime());

            editor.putString("Lat3", Double.toString(lat));
            editor.putString("Lon3", Double.toString(lon));
            editor.putString("time3", Double.toString(time));
            editor.commit();
        }

        else if (counter == 4) {
            //Lat4.setValue("its getting to 4");
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            long time = loc.getTime();

            Lat4.setValue(loc.getLatitude());
            Lon4.setValue(loc.getLongitude());
            time4.setValue(loc.getTime());

            editor.putString("Lat4", Double.toString(lat));
            editor.putString("Lon4", Double.toString(lon));
            editor.putString("time4", Double.toString(time));
            editor.commit();
        }

        else if (counter == 5) {
            //Lat5.setValue("its getting to 5");
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            long time = loc.getTime();

            Lat5.setValue(loc.getLatitude());
            Lon5.setValue(loc.getLongitude());
            time5.setValue(loc.getTime());

            editor.putString("Lat5", Double.toString(lat));
            editor.putString("Lon5", Double.toString(lon));
            editor.putString("time5", Double.toString(time));
            editor.commit();
        }

        else {
            error.setValue("Error in updateInfo");
        }

    }
}
