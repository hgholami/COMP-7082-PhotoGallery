package com.example.photogallery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 3;
    private static final int REQUEST_CODE_PERMISSIONS = 123;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 2;
    private int gallery_index = 0;
    private ArrayList<File> files = null;
    private File appFolder;
    private File tempFile = null;
    private PhotoManager phManager;
    private PhotoUri phUri;

    //Filters
    private String currentCaption;
    private Double Longitude = 0.0, Latitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_main);
        PhotoManager.initialize(this);
        phManager = PhotoManager.getInstance();
        phUri = new PhotoUri(this);
        //moved load files to after checking if location is enabled
        try {
            phManager.loadFiles();
            if (phManager.getSize() == 0) {
                displayPhoto(null);
            } else {
                displayPhoto(phManager.getPhoto().toString());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void getLocation(){
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationCallback mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                if(locationResult == null)
                {
                    return;
                }
                for(Location location: locationResult.getLocations())
                {
                    if(location != null){
                        Longitude = location.getLongitude();
                        Latitude = location.getLatitude();
                        //TODO: UI updates.
                    }
                }
            }
        };
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest,mLocationCallback,null);
        LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                DecimalFormat format = new DecimalFormat("#.####");
                if(location != null) {
                    Longitude = location.getLongitude();
                    Latitude = location.getLatitude();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if(isLocationEnabled(this)){
                getLocation();
            } else {
                createLocationRequest();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private boolean isLocationEnabled(Context context){
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isLocationEnabled();
    }

    protected void createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                checkPermission();
                getLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    //do code here to show user dialog to turn on Location Settings
                    try{
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,REQUEST_CHECK_SETTINGS);
                    } catch(IntentSender.SendIntentException sendEx) {

                    }
                }
            }
        });
    }

    /**
     * called by the left and right buttons from the MainActivity
     * checks if the button that called the function has the id leftButton or rightButton
     *
     * @param view
     */
    public void navGallery(View view) {
        if (!phManager.isEmpty()) {
            EditText caption = (EditText) findViewById(R.id.editCaption);

            if (!currentCaption.equals(caption.getText().toString())) {
                phManager.updatePhoto((caption.getText().toString()));
//                Log.d("Caption changed from", "" + currentCaption);
//                Log.d("Caption changed to", "" + caption.getText().toString());
            } else {
//                Log.d("Caption has not changed", "" + currentCaption);
//                Log.d("Caption has not changed", "" + caption.getText().toString());
            }
            switch (view.getId()) {
                case R.id.leftButton:
                    if (phManager.getGallery_index() != 0) {
                        phManager.decrement();
                    }
                    break;
                case R.id.rightButton:
                    if (phManager.getGallery_index() < phManager.getSize() - 1) {
                        phManager.increment();
                    }
                    break;
                default:
                    break;
            }
            displayPhoto(phManager.getPhoto().toString());
        }
    }

    /**
     * called by the Snap Button in the MainActivity and opens the devices built in camera application
     *
     * @param view
     */
    @SuppressLint("MissingPermission")
    public void takePicture(View view) {
        //creates an intent for taking a picture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //if picture is taken create file to write to in the directory created when app is installed
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            //create file for the photo to write to
            File photoFile = null;
            try {
                photoFile = phManager.createImageFile(Longitude,Latitude);
            } catch (IOException ex) {

            }

            //check if image file is created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    /**
     * called by the Share Button in the MainActivity and opens up the menu for image sharing
     *
     * @param view
     */
    public void shareToMedia(View view) {
        ImageView photoView = (ImageView) findViewById(R.id.photoView);

        Uri bmpUri = phUri.create(photoView);

        if (bmpUri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share image using"));

        } else {
            log("Sharing failed");
        }

    }

    public void navigateSearch(View view) throws ParseException {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
        phManager.loadFiles();
        displayPhoto(phManager.getPhoto().toString());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //gets folder where the pictures taken in the app are stored
            File imgFile = new File(phManager.getAppFolder().getAbsolutePath() + "/");

            //checks if the image file was created successfully
            if (imgFile.exists()) {
                //sets the index to 0 and grabs the image at that index which is the latest taken picture
                phManager.setGallery_index(0);
                displayPhoto(phManager.getPhoto().getAbsolutePath());
            }

        }

        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            String startDate = data.getStringExtra("startDate");
            String endDate = data.getStringExtra("endDate");
            String topLeftLat = data.getStringExtra("topLeftLat");
            String topLeftLng = data.getStringExtra("topLeftLng");
            String bottomRightLat = data.getStringExtra("bottomRightLat");
            String bottomRightLng = data.getStringExtra("bottomRightLng");
            String keyword = data.getStringExtra("keyword");
            try {
                phManager.setFilter(startDate, endDate, topLeftLat, topLeftLng,
                        bottomRightLat, bottomRightLng,keyword);
                phManager.loadFiles();
                displayPhoto(phManager.getPhoto().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(requestCode == REQUEST_CHECK_SETTINGS){
            checkPermission();
            getLocation();
        }

    }

    /**
     * Displays the photo retrieve by the current file path
     *
     * @param path
     */
    private void displayPhoto(String path) {
        ImageView img_view = (ImageView) findViewById(R.id.photoView);
        TextView text_view = (TextView) findViewById(R.id.timestamp);
        EditText edit_text = (EditText) findViewById(R.id.editCaption);
        final TextView Longitude_text = (TextView) findViewById(R.id.Long_text);
        final TextView Latitude_text = (TextView) findViewById(R.id.Lat_text);

        if (path == null || path == "") {
            img_view.setImageResource(R.mipmap.ic_launcher);
            edit_text.setText("");
            text_view.setText("Timestamp: ");
        } else {
            img_view.setImageBitmap(BitmapFactory.decodeFile(path));
            String[] attr = path.split("_");
            currentCaption = attr[3];
            edit_text.setText(attr[3]);
            try {
                // take the timestamp and format it to a more "human readable" date
                Date calDate = new SimpleDateFormat("yyyyMMdd").parse(attr[1]);
                String calDateFormat = new SimpleDateFormat("yyyy-MM-dd").format(calDate);

                Date timeDate = new SimpleDateFormat("HHmmss").parse(attr[2]);
                String timeDateFormat = new SimpleDateFormat("HH:mm:ss").format(timeDate);

                text_view.setText("Timestamp: " + calDateFormat + " " + timeDateFormat);
                Longitude_text.setText("Longitude: "+attr[4]);
                Latitude_text.setText("Latitude: " + attr[5]);

            } catch (ParseException pe) {
                pe.printStackTrace();
            }
        }
    }

    private void log(String string) {
        Log.d("Debug", string);
    }
}