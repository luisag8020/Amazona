package com.primera.amazona;

import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.primera.amazona.ApplicationSettings;

import static android.location.Criteria.ACCURACY_FINE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;


/*
Created by queenlu on 4/16/18.
*/



public class CurrentLocationProvider extends LocationListenerAdapter {
    private Location currentLocation;
    public static final int MAX_RETRIES = 20;
    public static final int LOCATION_WAIT_TIME = 1000;


    public CurrentLocationProvider(Context context) {
        Log.e(">>>>>>", "in CurrentLocationProvider CONSTRUCTOR - trying to retrieve location from getLastKnownLocation()");
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(ACCURACY_FINE);
        String provider = manager.getBestProvider(criteria, true);
        Location bestLocation;

        Log.e("criteria", criteria.toString());
        Log.e("provider", provider);


        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) +
                ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (provider != null)
                bestLocation = manager.getLastKnownLocation(provider);
            else
                bestLocation = null;

            Location latestLocation = getLatest(bestLocation, manager.getLastKnownLocation(GPS_PROVIDER));
            latestLocation = getLatest(latestLocation, manager.getLastKnownLocation(NETWORK_PROVIDER));
//        latestLocation = getLatest(latestLocation, manager.getLastKnownLocation(PASSIVE_PROVIDER));
            currentLocation = latestLocation;



//            int retryCount = 0;
//
//            if (provider != null) {
//
//                while (/*retryCount < MAX_RETRIES &&*/ lastKnownLocation == null) {
//                    //if (provider.equals("GPS_PROVIDER")) provider = "GPS_PROVIDER";
//                    lastKnownLocation = manager.getLastKnownLocation(provider); //provider
//                    Log.i("provider", provider);
//                    try {
//                        retryCount++;
//                        Thread.sleep(LOCATION_WAIT_TIME);
//                    } catch (InterruptedException e) {
//                        Log.e("wait", "Location wait InterruptedException", e);
//                    }
//                }

//                lastKnownLocation = manager.getLastKnownLocation(provider);
//                if (lastKnownLocation == null) Log.e("lastLoc", "null");
//
//                Log.e("manager", lastKnownLocation.toString());
//            }
//            else
//                lastKnownLocation = null;


            //Location latestLocation = getLatest(bestLocation, manager.getLastKnownLocation(GPS_PROVIDER));
            //latestLocation = getLatest(latestLocation, manager.getLastKnownLocation(NETWORK_PROVIDER));
//        latestLocation = getLatest(latestLocation, manager.getLastKnownLocation(PASSIVE_PROVIDER));
          //  currentLocation = latestLocation;
        }

        if (currentLocation != null)
            ApplicationSettings.setCurrentBestLocation(context, currentLocation);
        return;



    }

    private static Location getLatest(final Location location1, final Location location2) {
        if (location1 == null)
            return location2;

        if (location2 == null)
            return location1;

        if (location2.getTime() > location1.getTime())
            return location2;
        else
            return location1;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    public Location getLocation() {

        if (currentLocation == null)
            Log.i("location", "null");
        else
            Log.i("location", currentLocation.toString());
        return currentLocation;
    }
}
