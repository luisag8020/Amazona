package com.primera.amazona;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by queenlu on 2/26/18.
 */

public class contextAware {
    private Context context;
    int mSignalStrength = 0;
    String strengthString = "";
    int strLevel = 0;
    TelephonyManager mTelephonyManager;
    MyPhoneStateListener mPhoneStatelistener;

    public contextAware(Context context) {
        this.context = context;
        mPhoneStatelistener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(mPhoneStatelistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void getNetwork() {
        // get network status
        boolean connected = false;
        //TextView connectText=(TextView)findViewById(R.id.connectText);
        String connectString = "let's check";
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
            connectString = "connected";
            Log.i("getNetwork", "connected");
            //connectText.setText(connectString);

        }
        else {
            connected = false;
            connectString = "not connected";
            Log.i("getNetwork", "not connected");
            //connectText.setText(connectString);
        }

        // Specific network type
        if (connected == true) {
            //TextView networkText=(TextView)findViewById(R.id.networkText);
            strLevel = getStrengthLevel();
            mSignalStrength = getSignalStrength();
            strengthString = getStrengthString();
            String networkType = "Network Type: " +
                    "\n Strength Level: " + strLevel +
                    "\n Strength: " + mSignalStrength +
                    "\n String:  " + strengthString;
            //networkText.setText(networkType);
            Log.i("getNetwork", "network type: " + networkType);
        }

        SharedPreferences saveLocation = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = saveLocation.edit();
        editor.putString("strengthString", strengthString);
        editor.putString("mSignalStrength", mSignalStrength+"");
        editor.putString("strLevel", strLevel +"");
        editor.commit();

    }

    public int getStrengthLevel() {
        return strLevel;
    }

    public int getSignalStrength() {
        return mSignalStrength;
    }

    private String getStrengthString() {
        return strengthString;
    }

    public String getNeworkType() {
        return networkType();
    }

    private String networkType() {
        TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
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



}
