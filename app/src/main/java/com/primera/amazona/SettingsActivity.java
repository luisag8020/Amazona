package com.primera.amazona;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by queenlu on 11/30/17.
 */

public class SettingsActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences switches = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);

        // initiate a Switch
        Switch locationSwitch = (Switch) findViewById(R.id.locationSwitch);
        Switch batterySwitch = (Switch) findViewById(R.id.batterySwitch);
        Switch messageSwitch = (Switch) findViewById(R.id.messageSwitch);

        //set the current state of locationSwitch
        if (switches.getBoolean("locationSwitch", false))
            locationSwitch.setChecked(true);
        else
            locationSwitch.setChecked(false);

        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences switches = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor editor = switches.edit();

                if (isChecked) {
                    editor.putBoolean("locationSwitch", true).commit();
                }
                else
                    editor.putBoolean("locationSwitch", false).commit();
            }
        });


        //set the current state of batterySwitch
        if (switches.getBoolean("batterySwitch", false))
            batterySwitch.setChecked(true);
        else
            batterySwitch.setChecked(false);

        batterySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences switches = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor editor = switches.edit();

                if (isChecked) {
                    editor.putBoolean("batterySwitch", true).commit();
                }
                else
                    editor.putBoolean("batterySwitch", false).commit();
            }
        });


        //set the current state of messageSwitch
        if (switches.getBoolean("messageSwitch", false))
            messageSwitch.setChecked(true);
        else
            messageSwitch.setChecked(false);

        messageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences switches = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor editor = switches.edit();

                if (isChecked) {
                    editor.putBoolean("messageSwitch", true).commit();
                }
                else
                    editor.putBoolean("messageSwitch", false).commit();
            }
        });
    }

}
