package com.example.l33550.imageupload;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class PhotoUpload extends AppCompatActivity {

    File fileToUpload = new File("/storage/sdcard0/Pictures/Screenshots/photos.png");
    File fileToDownload = new File("/storage/sdcard0/Pictures/");
    ImageView imageToUpload, downloadedImage;
    Button btnSelect, btnGallery, btnUpload;


    AmazonS3 s3;

    TransferUtility transferUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_upload);



        // callback method to call credentialsProvider method.
        credentialsProvider();

        // callback method to call the setTransferUtility method
        setTransferUtility();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9002 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            downloadedImage.setImageURI(null);
            downloadedImage.setImageURI(selectedImage);
            //bSelectImage.setText("Change Image");
        }


    }

    public void credentialsProvider() {

        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1_dK4Jpkc6w", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        setAmazonS3Client(credentialsProvider);
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

    /**
     * This method is used to upload the file to S3 by using TransferUtility class
     * @param view
     */
    public void setFileToUpload(View view){

        TransferObserver transferObserver = transferUtility.upload(
                "test.numetric",     /* The bucket to upload to */
                "photos.png",       /* The key for the uploaded object */
                fileToUpload       /* The file where the data to upload exists */
        );
    }

    /**
     *  This method is used to Download the file to S3 by using transferUtility class
     * @param view
     **/
    public void setFileToDownload(View view) {

        TransferObserver transferObserver = transferUtility.download(
                "test.numetric",     /* The bucket to download from */
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
                Log.e("statechange", state+"");
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                int percentage = (int) (bytesCurrent/bytesTotal * 100);
                Log.e("percentage",percentage +"");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("error","error");
            }

        });
    }


}
