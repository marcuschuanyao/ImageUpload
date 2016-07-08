package com.example.l33550.imageupload;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    ImageView imageView;

    File fileToUpload;
    File fileToDownload = new File("/storage/sdcard0/Pictures/MY");
    AmazonS3 s3;
    TransferUtility transferUtility;

    private CoordinatorLayout coordinatorLayout;

    private static final int SELECT_PHOTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Instantiate the iamge view
        imageView = (ImageView)findViewById(R.id.imageView);

        // Instantiate coordinator layout
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);

        // callback method to call credentialsProvider method.
        credentialsProvider();

        // callback method to call the setTransferUtility method
        setTransferUtility();



    }


    /**
     *  Create a AmazonS3Client constructor and pass the credentialsProvider.
     * @param credentialsProvider
     */
    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider){

        // Create an S3 client
        s3 = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));

    }

    public void setTransferUtility(){

        transferUtility = new TransferUtility(s3, getApplicationContext());
    }

    public void credentialsProvider(){

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:b8ab0f4a-865b-45d0-a9df-16cf02f25173", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        setAmazonS3Client(credentialsProvider);
    }


    public void browseImageFile(View v){

        /*Log.d("hello world","test");

        TransferObserver transferObserver = transferUtility.upload(
                "imageuploadnypsit",      The bucket to upload to
                "7.png",       /* The key for the uploaded object
                fileToUpload       /* The file where the data to upload exists
        );
        transferObserverListener(transferObserver);*/

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);

    }

    public void uploadImage(File uploadFile) {

        // Generate random file string to prevent
        String uuid = UUID.randomUUID().toString();

        TransferObserver transferObserver = transferUtility.upload(
                "imageuploadnypsit",     /* The bucket to upload to */
                uuid+".jpg",       /* The key for the uploaded object */
                uploadFile       /* The file where the data to upload exists */
        );
        transferObserverListener(transferObserver);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){

                    // Get the selected image file
                    Uri selectedImage = imageReturnedIntent.getData();

                    // Get the file URL
                    fileToUpload = new File(getRealPathFromURI(selectedImage));

                    InputStream imageStream = null;
                    try {
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);

                    imageView.setImageBitmap(yourSelectedImage);

                    // Upload image into server
                    uploadImage(fileToUpload);

                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Image has been uploaded.", Snackbar.LENGTH_SHORT);

                    snackbar.show();


                }
        }



    }


    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    public void setFileToDownload(View v){

        TransferObserver transferObserver = transferUtility.download(
                "imageuploadnypsit",     /* The bucket to download from */
                "MY",    /* The key for the object to download */
                fileToDownload        /* The file to download the object to */
        );

        transferObserverListener(transferObserver);

    }


    /**
     * This is listener method of the TransferObserver
     * Within this listener method, we get the status of uploading and downloading the file,
     * it displays percentage part of the  file to be uploaded or downloaded to S3
     * It also displays an error, when there is problem to upload and download file to S3.
     * @param transferObserver
     */

    public void transferObserverListener(TransferObserver transferObserver){

        transferObserver.setTransferListener(new TransferListener(){

            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("statechange", state+"");
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                Log.d("percentage",percentage +"");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("error","error");

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Please allow the app's permission to read the file.", Snackbar.LENGTH_SHORT);

                snackbar.show();
            }

        });
    }



}
