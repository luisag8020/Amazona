package com.primera.amazona;

/**
 * Created by queenlu on 2/20/18.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.content.SharedPreferences;
import static android.telephony.SmsMessage.MAX_USER_DATA_SEPTETS;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PanicMessage {
    public static final String GOOGLE_MAP_URL = "https://maps.google.com/maps?q=";

    private Context context;
    private Location location;

    // private MainActivity mainActivity;
    private String[] contactNumbers;
    String latitude;
    String longitude;
    String accuracy;
    String speed;
    String time;
    String detectedActivity;


    public PanicMessage(Context context) {
        this.context = context;
    }

    public String createMessageString1() {
        String IAM;
        String message;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, String> allPreferences = (Map<String, String>) sharedPreferences.getAll();
        sharedPreferences.getBoolean("locationSwitch", false);

        // CHANGE SO THAT IT IS NOT IN THE MAIN ACTIVITY. MOve it here
        detectedActivity = allPreferences.get("detectedActivity");


        if (sharedPreferences.getBoolean("batterySwitch", false) &&
                sharedPreferences.getBoolean("activitySwitch", false)) {
            IAM = "This is an emergency." + " I was " + detectedActivity +  " at the moment of the incident with " +
                    sharedPreferences.getString("battery", "unknown") + "% battery. ";
        }
        else if (sharedPreferences.getBoolean("activitySwitch", false) &&
                !sharedPreferences.getBoolean("batterySwitch", false)){
            IAM = "This is an emergency." + " I was " + detectedActivity + " at the moment of the incident. ";
        }
        else if (!sharedPreferences.getBoolean("activitySwitch", false) &&
                sharedPreferences.getBoolean("batterySwitch", false)) {
            IAM = "This is an emergency. I had " +
                    sharedPreferences.getString("battery", "unknown") + "% battery. ";
        }
        else {
            IAM = "This is an emergency.";
        }

        //String address = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + ","+ longitude + "&sensor=true";

        String address = getAddress();
            //String mapMessage = "\n Map: https://maps.google.com/?q=loc:" + latitude + "," + longitude;
        message = IAM + "I am at: " + address;

        return message;
    }

    public String createMessageString2() {
        String locationString;
        if (location != null) {
            this.longitude = Double.toString(location.getLongitude());
            this.latitude = Double.toString(location.getLatitude());
            this.accuracy = Double.toString(location.getAccuracy());
            this.speed = Double.toString(location.getSpeed());
            this.time = Double.toString(location.getTime());
            locationString = GOOGLE_MAP_URL + latitude + "," + longitude; // + " Accuracy: " + accuracy;
        }
        else
            locationString = "Could not get location";

        return locationString;

    }

    public void sendAlertMessage(Location location)  {
        this.location = location;

        String message1 = createMessageString1();
        String message2 = createMessageString2();

        sendMessage(message1, message2);
    }

    private String getAddress() {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            //String city = addresses.get(0).getLocality();
            //String state = addresses.get(0).getAdminArea();
//            String country = addresses.get(0).getCountryName();
//            String postalCode = addresses.get(0).getPostalCode();
//            String knownName = addresses.get(0).getFeatureName();
            Log.i("address", address);
            return address;
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
        return "Could not get address";
    }

    // Sends message to all contacts
    private void sendMessage(String message1, String message2){
        Log.e(">>>>>>", "here1");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, String> allPreferences = (Map<String, String>) sharedPreferences.getAll();
        int contactNoAllowed = Integer.parseInt(allPreferences.get("No_Contacts"));

        contactNumbers = new String[contactNoAllowed];
        List<String> sortedKeys = new ArrayList<String>(allPreferences.keySet());
        Collections.sort(sortedKeys);
        int i = 0;
        for (String preferenceKey : sortedKeys) {
            if (preferenceKey.startsWith("contactNo")) {
                Log.i("key", preferenceKey);
                Log.i("i", i + "");
                contactNumbers[i] = allPreferences.get(preferenceKey);
                i++;
            }
        }

        Log.i("all contacts: ", contactNumbers[0] + contactNumbers[1] + contactNumbers[2]);
        for (String phoneNumber : contactNumbers) {
            String mess1 = message1;
            String mess2 = message2;
            if (phoneNumber != null) {
                Log.i("phoneNo", phoneNumber);
                sendSMS(context, phoneNumber, mess1, mess2);
            }
            else
                Log.i("phoneNo", "null");
        }
    }

    // All of SMSAdapter
    public void sendSMS(Context context, String phoneNumber, String message1, String message2) {
        if(!ApplicationSettings.isFirstMsgSent(context)){
            ApplicationSettings.setFirstMsgSent(context, true);
        }
        SmsManager smsManager = SmsManager.getDefault();
        try {
            if (message2 != null) {
                smsManager.sendTextMessage(phoneNumber, null, message1, null, null);
                smsManager.sendTextMessage(phoneNumber, null, message2, null, null);
            }
            else
                smsManager.sendTextMessage(phoneNumber, null, message1, null, null);

        } catch (Exception exception) {
            Log.e("messageTag", "Sending SMS failed " + exception.getMessage());
        }
    }


    // Will send history message only the first time
    public void sendHistoryMessage() {

        Log.i("inHistory", "here");
        SharedPreferences locInterval = PreferenceManager.getDefaultSharedPreferences(context);
        String lat1 = locInterval.getString("Lat1", "none");
        String lon1 = locInterval.getString("Lon1", "none");
        String lat2 = locInterval.getString("Lat2", "none");
        String lon2 = locInterval.getString("Lon2", "none");
        String lat3 = locInterval.getString("Lat3", "none");
        String lon3 = locInterval.getString("Lon3", "none");
        String lat4 = locInterval.getString("Lat4", "none");
        String lon4 = locInterval.getString("Lon4", "none");
        String lat5 = locInterval.getString("Lat5", "none");
        String lon5 = locInterval.getString("Lon5", "none");


        if (lat1.equals("none") || lat1.equals("null"))
            lat1 = "";
        else if (lat2.equals("none") || lat2.equals("null"))
            lat2 = "";
        else if (lat3.equals("none") || lat3.equals("null"))
            lat3 = "";
        else if (lat4.equals("none") || lat4.equals("null"))
            lat4 = "";
        else if (lat5.equals("none") || lat5.equals("null"))
            lat5 = "";

        if (lon1.equals("none") || lon1.equals("null"))
            lon1 = "";
        else if (lon2.equals("none") || lon2.equals("null"))
            lon2 = "";
        else if (lon3.equals("none") || lon3.equals("null"))
            lon3 = "";
        else if (lon4.equals("none") || lon4.equals("null"))
            lon4 = "";
        else if (lon5.equals("none") || lon5.equals("null"))
            lon5 = "";

        String locationString = "www.google.com/maps/dir/" + lat1 + "," + lon1 + "/" +
                lat2 + "," + lon2 + "/" + lat3 + "," + lon3 + "/" + lat4 + "," + lon4 + "/" +
                lat5 + "," + lon5 + "/";

        //https://www.google.com/maps/dir/33.93729,-106.85761/33.91629,-106.866761/33.98729,-106.85861/@33.9598637,-106.905489,13z/data=!4m2!4m1!3e2
        sendMessage("Location history: ", locationString);

    }

    public void sendStopAlertMessage() {
        String message = "The alert has been stopped";
        sendMessage(message, "");
    }
}
