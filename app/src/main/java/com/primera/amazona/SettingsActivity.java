package com.primera.amazona;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by queenlu on 11/30/17.
 */


public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    AudioManager audioManager;
    RadioGroup audioRadioGroup;
    RadioButton soundButton, vibrationButton, silentButton;
    Spinner locationSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences switches = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // initiate a Switch
        Switch locationSwitch = (Switch) findViewById(R.id.locationSwitch);
        Switch batterySwitch = (Switch) findViewById(R.id.batterySwitch);
        Switch activitySwitch = (Switch) findViewById(R.id.activitySwitch);

        locationSpinner = (Spinner) findViewById(R.id.locationInterval);
        audioRadioGroup = (RadioGroup) findViewById(R.id.groupAudio);
        soundButton = (RadioButton) findViewById(R.id.soundButton);
        vibrationButton = (RadioButton) findViewById(R.id.vibrateButton);
        silentButton = (RadioButton) findViewById(R.id.silenceButton);


        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Select time interval");
        categories.add("10 min");
        categories.add("15 min");
        categories.add("30 min");
        categories.add("1 hr");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        locationSpinner.setAdapter(dataAdapter);
        locationSpinner.setOnItemSelectedListener(this);




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


        //set the current state of activitySwitch
        if (switches.getBoolean("activitySwitch", false))
            activitySwitch.setChecked(true);
        else
            activitySwitch.setChecked(false);

        activitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences switches = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor editor = switches.edit();

                if (isChecked) {
                    editor.putBoolean("activitySwitch", true).commit();
                }
                else
                    editor.putBoolean("activitySwitch", false).commit();
            }
        });

        // set the current state of radio group
        SharedPreferences radios = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);

        if (radios.getBoolean("soundRadio", false) == true){
            soundButton.setChecked(true);
        }
        else if (radios.getBoolean("silenceRadio", false) == true) {
            silentButton.setChecked(true);
        }

        else if (radios.getBoolean("vibrateRadio", false) == true) {
            vibrationButton.setChecked(true);
        }


        audioRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                SharedPreferences radioGroup = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                SharedPreferences.Editor editor = radioGroup.edit();

                if(checkedId == R.id.soundButton) {
                    editor.putBoolean("soundRadio", true).commit();
                    editor.putBoolean("silenceRadio", false).commit();
                    editor.putBoolean("vibrateRadio", false).commit();
                }

                else if(checkedId == R.id.silenceButton) {
                    editor.putBoolean("soundRadio", false).commit();
                    editor.putBoolean("silenceRadio", true).commit();
                    editor.putBoolean("vibrateRadio", false).commit();
                }
                else {
                    editor.putBoolean("soundRadio", false).commit();
                    editor.putBoolean("silenceRadio", false).commit();
                    editor.putBoolean("vibrateRadio", true).commit();
                }
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        Log.i("itemTime", item);

        SharedPreferences radioGroup = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        SharedPreferences.Editor editor = radioGroup.edit();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference timeInterval = database.getReference("locTimeInterval");

        Log.i("compare", item.equals("10 min") + "");
        if (item.equals("10 min")) {
            editor.putString("locationInterval", "10").commit();
            timeInterval.setValue("10");
        }

        else if (item.equals("15 min")) {
            editor.putString("locationInterval", "15").commit();
            timeInterval.setValue("15");
        }

        else if (item.equals("30 min")) {
            editor.putString("locationInterval", "30").commit();
            timeInterval.setValue("30");

        }

        else if (item.equals("1 hr")) {
            editor.putString("locationInterval", "60").commit();
            timeInterval.setValue("60");
        }

        else
            editor.putString("locationInterval", "none").commit();
            timeInterval.setValue("none");


    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    // Back button
    public void back_to_Main (View view) {
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
    }

}
