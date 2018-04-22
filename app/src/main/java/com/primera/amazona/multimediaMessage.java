package com.primera.amazona;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * Created by queenlu on 4/17/18.
 */

public class multimediaMessage {
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    private File photoCaptured;
    private Context context;
    private static final int CAMERA_REQUEST = 1888;

    public multimediaMessage(Context context) {
        this.context = context;
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = System.currentTimeMillis() + "";
        String fileName = "JPEG_" + timeStamp;

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            photoCaptured = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES),
                    fileName + ".jpg");
        } else {
            String dir = Environment.getExternalStorageDirectory() + File.separator + "myDirectory";
            //create folder
            File folder = new File(dir); //folder name
            if (!folder.exists()) {
                folder.mkdirs();
            }

            //create file
            photoCaptured = new File(dir, fileName + ".jpg");
        }


        if (intent.resolveActivity(context.getPackageManager()) != null) {
            ((AppCompatActivity) context).startActivityForResult(intent, REQUEST_TAKE_PHOTO);
        }
    }


    public File getAlbumDir() {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Amazona/"
        );

        // Create directories if needed
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        return storageDir;
    }


//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = getAlbumDir().toString() + timeStamp + ".jpg";
//        //     String imageFileName = "JPEG_" + timeStamp + "_";
//        File image = new File(imageFileName);
////
////        File image = File.createTempFile(
////                imageFileName,  /* prefix */
////                ".jpg",         /* suffix */
////                storageDir      /* directory */
////        );
//
//        // Save a file: path for use with ACTION_VIEW intents
//       // mCurrentPhotoPath = image.getAbsolutePath();
//        return image;
//    }
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(photo);
//
//            Intent i = new Intent(Intent.ACTION_SEND);
//            i.setType("message/rfc822");
//            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"first.last@gmail.com"});
//            i.putExtra(Intent.EXTRA_SUBJECT, "first picture");
//            i.putExtra(Intent.EXTRA_TEXT   , "body of email");
//
//            Uri uri = Uri.fromFile(f);
//            i.putExtra(Intent.EXTRA_STREAM, uri);
//            try {
//                startActivity(Intent.createChooser(i, "Send mail..."));
//            } catch (android.content.ActivityNotFoundException ex) {
//                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
}
