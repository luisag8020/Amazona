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
import android.widget.TextView;
import android.provider.ContactsContract;



/**
 * Created by queenlu on 11/28/17.
 */

public class EmergencyContacts extends AppCompatActivity {

    private static final int RESULT_PICK_CONTACT = 1;
    private TextView nameResult;
    private TextView numberResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);


        nameResult = (TextView) findViewById(R.id.nameResult);
        numberResult = (TextView) findViewById(R.id.numberResult);

    }

    public void pickContact(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    //Query the Uri and read contact details. Handle the picked contact data.
    private void contactPicked(Intent data) {

        Cursor cursor = null;
        try {
            String phoneNo = null ;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);

            // Set the value to the textviews
            nameResult.setText(name);
            numberResult.setText(phoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Back button
    public void back_Main (View view) {
        Intent intent = new Intent (this, MainActivity.class);
        startActivity(intent);
    }

}
