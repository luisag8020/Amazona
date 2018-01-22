package com.primera.amazona;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.provider.ContactsContract;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;



/**
 * Created by queenlu on 11/28/17.
 */

public class EmergencyContacts extends AppCompatActivity {

    private static final String TAG = "Awareness";

    private static final int RESULT_PICK_CONTACT1 = 1;
    private static final int RESULT_PICK_CONTACT2 = 2;
    private static final int RESULT_PICK_CONTACT3 = 3;
    private static final int RESULT_PICK_CONTACT4 = 4;
    private static final int RESULT_PICK_CONTACT5 = 5;
    private static final int DELETE_CONTACT1 = 6;
    private static final int DELETE_CONTACT2 = 7;
    private static final int DELETE_CONTACT3 = 8;
    private static final int DELETE_CONTACT4 = 9;
    private static final int DELETE_CONTACT5 = 0;

    private TextView nameResult1;
    private TextView numberResult1;
    private TextView nameResult2;
    private TextView numberResult2;
    private TextView nameResult3;
    private TextView numberResult3;
    private TextView nameResult4;
    private TextView numberResult4;
    private TextView nameResult5;
    private TextView numberResult5;
    String phoneNo;
    String name;

    // Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        phoneNo = null;
        name = "Add Emergency Contact";

        nameResult1 = (TextView) findViewById(R.id.nameResult1);
        numberResult1 = (TextView) findViewById(R.id.numberResult1);
        nameResult2 = (TextView) findViewById(R.id.nameResult2);
        numberResult2 = (TextView) findViewById(R.id.numberResult2);
        nameResult3 = (TextView) findViewById(R.id.nameResult3);
        numberResult3 = (TextView) findViewById(R.id.numberResult3);
        nameResult4 = (TextView) findViewById(R.id.nameResult4);
        numberResult4 = (TextView) findViewById(R.id.numberResult4);
        nameResult5 = (TextView) findViewById(R.id.nameResult5);
        numberResult5 = (TextView) findViewById(R.id.numberResult5);


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

        // Read from the database
        contactName1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                nameResult1.setText(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    public void pickContact1(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT1);
    }

    public void pickContact2(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT2);
    }

    public void pickContact3(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT3);
    }

    public void pickContact4(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT4);
    }

    public void pickContact5(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT5);
    }

    public void deleteContact1(View v)
    {
        deleteContact(DELETE_CONTACT1);
    }

    public void deleteContact2(View v)
    {
        deleteContact(DELETE_CONTACT2);
    }

    public void deleteContact3(View v)
    {
        deleteContact(DELETE_CONTACT3);
    }

    public void deleteContact4(View v)
    {
        deleteContact(DELETE_CONTACT4);
    }

    public void deleteContact5(View v)
    {
        deleteContact(DELETE_CONTACT5);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be using multiple startActivityForResult
            switch (requestCode) {
                case RESULT_PICK_CONTACT1:
                    contactPicked(data, RESULT_PICK_CONTACT1);
                    break;
                case RESULT_PICK_CONTACT2:
                    contactPicked(data, RESULT_PICK_CONTACT2);
                    break;
                case RESULT_PICK_CONTACT3:
                    contactPicked(data, RESULT_PICK_CONTACT3);
                    break;
                case RESULT_PICK_CONTACT4:
                    contactPicked(data, RESULT_PICK_CONTACT4);
                    break;
                case RESULT_PICK_CONTACT5:
                    contactPicked(data, RESULT_PICK_CONTACT5);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    //Query the Uri and read contact details. Handle the picked contact data.
    private void contactPicked(Intent data, int contactNo) {

        Cursor cursor = null;
        try {
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int  nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);

            // Save to database according to case
            switch (contactNo) {
                case RESULT_PICK_CONTACT1:
                    //nameResult1.setText(name);
                    //numberResult1.setText(phoneNo);
                    contactName1.setValue(name);
                    contactNo1.setValue(phoneNo);
                    break;
                case RESULT_PICK_CONTACT2:
                    //nameResult2.setText(name);
                    //numberResult2.setText(phoneNo);
                    contactName2.setValue(name);
                    contactNo2.setValue(phoneNo);
                    break;
                case RESULT_PICK_CONTACT3:
                    //nameResult3.setText(name);
                    //numberResult3.setText(phoneNo);
                    contactName3.setValue(name);
                    contactNo3.setValue(phoneNo);
                    break;
                case RESULT_PICK_CONTACT4:
                    //nameResult4.setText(name);
                    //numberResult4.setText(phoneNo);
                    contactName4.setValue(name);
                    contactNo4.setValue(phoneNo);
                    break;
                case RESULT_PICK_CONTACT5:
                    //nameResult5.setText(name);
                    //numberResult5.setText(phoneNo);
                    contactName5.setValue(name);
                    contactNo5.setValue(phoneNo);
                    break;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Delete function
    private void deleteContact(int delete_code) {
        switch (delete_code){
            case DELETE_CONTACT1:
                phoneNo = null;
                name = "Add Emergency Contact";
                nameResult1.setText(name);
                numberResult1.setText(phoneNo);
                contactName1.setValue(name);
                contactNo1.setValue(phoneNo);
                break;
            case DELETE_CONTACT2:
                phoneNo = null;
                name = "Add Emergency Contact";
                nameResult2.setText(name);
                numberResult2.setText(phoneNo);
                contactName2.setValue(name);
                contactNo2.setValue(phoneNo);
                break;
            case DELETE_CONTACT3:
                phoneNo = null;
                name = "Add Emergency Contact";
                nameResult3.setText(name);
                numberResult3.setText(phoneNo);
                contactName3.setValue(name);
                contactNo3.setValue(phoneNo);
                break;
            case DELETE_CONTACT4:
                phoneNo = null;
                name = "Add Emergency Contact";
                nameResult4.setText(name);
                numberResult4.setText(phoneNo);
                contactName4.setValue(name);
                contactNo4.setValue(phoneNo);
                break;
            case DELETE_CONTACT5:
                phoneNo = null;
                name = "Add Emergency Contact";
                nameResult5.setText(name);
                numberResult5.setText(phoneNo);
                contactName5.setValue(name);
                contactNo5.setValue(phoneNo);
                break;
        }

    }

    // Back button
    public void back_Main (View view) {
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
    }



}
