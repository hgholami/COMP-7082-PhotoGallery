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
import android.os.Debug;
import android.os.Environment;
import android.os.FileUtils;
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
import java.util.Locale;
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

    //Filters
    private String currentCaption;
    private String startDate = "";
    private String endDate = "";
    private Double Longitude = 0.0, Latitude = 0.0;

    private String topLeftLat = "";
    private String topLeftLng = "";
    private String bottomRightLat = "";
    private String bottomRightLng = "";
    private String keyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Begin Method Tracing for onCreate
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.getDefault());
        String logDate = dateFormat.format(new Date());
        Debug.startMethodTracing("onCreate_performance_profile_" + logDate);

        super.onCreate(savedInstanceState);
        checkPermission();
        setContentView(R.layout.activity_main);
        //moved load files to after checking if location is enabled
        try {
            loadFiles();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Stop Method Tracing for onCreate
        Debug.stopMethodTracing();
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

    public void loadFiles() throws ParseException {
        //appFolder is set to a file with the directory internal storage/Android/data/com.example.photogallery/files/Pictures
        //if the file doesn't exist, it creates a directory
        appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!appFolder.exists()) {
            if (appFolder.mkdir())
                Log.d("Directory Creation", "Directory: " + appFolder.getAbsolutePath());
        }
        if(appFolder.listFiles() == null){
            files = new ArrayList<File>();
        }

        if (appFolder.listFiles() != null) {

            //grabs a list of files from the appFolder directory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                files = new ArrayList<File>(Arrays.asList(Objects.requireNonNull(appFolder.listFiles())));
            }

            // applies filters
            for (Iterator<File> it = files.iterator(); it.hasNext(); ) {
                File item = it.next();
                Date curDate = parseDate(item);
                Double lat = parseLat(item);
                Double lng = parseLng(item);
                if (!keyword.isEmpty() && !parseCaption(item).contains(keyword)) {
                    it.remove();
                    continue;
                }

                try {
                    Log.d("bottomRight", bottomRightLat);
                    Log.d("lat", lat.toString());
                    if (!startDate.isEmpty()) {
                        if (new SimpleDateFormat("yyyy-MM-dd").parse(startDate).compareTo(curDate) > 0) {
                            it.remove();
                            continue;
                        }
                    }
                    if (!endDate.isEmpty()) {
                        if (new SimpleDateFormat("yyyy-MM-dd").parse(endDate).compareTo(curDate) < 0) {
                            it.remove();
                            continue;
                        }
                    }
                    if (!topLeftLat.isEmpty() && Double.parseDouble(topLeftLat) > lat ) {
                        it.remove();
                        continue;
                    }
                    if (!bottomRightLat.isEmpty() && Double.parseDouble(bottomRightLat) < lat ) {
                        it.remove();
                        continue;
                    }
                    if (!topLeftLng.isEmpty() && Double.parseDouble(topLeftLng) > lng ) {
                        it.remove();
                        continue;
                    }
                    if (!bottomRightLng.isEmpty() && Double.parseDouble(bottomRightLng) > lng ) {
                        it.remove();
                        continue;
                    }

                } catch (Exception ex) {
                    //if format is wrong, show nothing
                    Log.d("Parsing Error", String.valueOf(ex));
                    it.remove();
                    continue;
                }




            }
        }

        // display default image if no image files exist,
        // otherwise display the current image
        if (files.size() == 0) {
            displayPhoto(null);
        } else {
            displayPhoto(files.get(gallery_index).toString());
        }
    }

    /**
     * called by the left and right buttons from the MainActivity
     * checks if the button that called the function has the id leftButton or rightButton
     *
     * @param view
     */
    public void navGallery(View view) {
        if (!files.isEmpty()) {
            EditText caption = (EditText) findViewById(R.id.editCaption);

            if (!currentCaption.equals(caption.getText().toString())) {
                updatePhoto((caption.getText().toString()));
//                Log.d("Caption changed from", "" + currentCaption);
//                Log.d("Caption changed to", "" + caption.getText().toString());
            } else {
//                Log.d("Caption has not changed", "" + currentCaption);
//                Log.d("Caption has not changed", "" + caption.getText().toString());
            }
            switch (view.getId()) {
                case R.id.leftButton:
                    if (gallery_index != 0) {
                        gallery_index--;
                    }
                    break;
                case R.id.rightButton:
                    if (gallery_index < files.size() - 1) {
                        gallery_index++;
                    }
                    break;
                default:
                    break;
            }
            displayPhoto(files.get(gallery_index).toString());
        }
    }

    /**
     * called by the Snap Button in the MainActivity and opens the devices built in camera application
     *
     * @param view
     */
    @SuppressLint("MissingPermission")
    public void takePicture(View view) {
        //Begin Method Tracing for TakePicture
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("dd_MM_yyyy_hh_mm_ss", Locale.getDefault());
        String logDate = dateFormat.format(new Date());
        Debug.startMethodTracing("takePicture_performance_profile_" + logDate);

        //creates an intent for taking a picture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //if picture is taken create file to write to in the directory created when app is installed
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            //create file for the photo to write to
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }

            //check if image file is created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

        //Stop Method Tracing for TakePicture
        Debug.stopMethodTracing();
    }


    /**
     * called by the Share Button in the MainActivity and opens up the menu for image sharing
     *
     * @param view
     */
    public void shareToMedia(View view) {
        ImageView photoView = (ImageView) findViewById(R.id.photoView);

        if (tempFile != null) {
            tempFile.delete();
        }

        Uri bmpUri = getLocalBitmapUri(photoView);
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

    /**
     * Gets the bitmap available currently from the photoView
     *
     * @param imageView
     * @return bmpUri the current bitmap from the image view
     */
    public Uri getLocalBitmapUri(ImageView imageView) {

        // gets the image from the imageview as a bitmap
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }

        Uri bmpUri = null;
        try {
            
            // saves the bitmap as a new file before sharing
            tempFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(tempFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(MainActivity.this, "com.example.android.fileprovider", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public void navigateSearch(View view) throws ParseException {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, SEARCH_ACTIVITY_REQUEST_CODE);
        loadFiles();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //gets folder where the pictures taken in the app are stored
            File imgFile = new File(appFolder.getAbsolutePath() + "/");

            //checks if the image file was created successfully
            if (imgFile.exists()) {
                //sets the index to 0 and grabs the image at that index which is the latest taken picture
                gallery_index = 0;
                displayPhoto(files.get(gallery_index).getAbsolutePath());
            }

        }

        if (requestCode == SEARCH_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            startDate = data.getStringExtra("startDate");
            endDate = data.getStringExtra("endDate");
            topLeftLat = data.getStringExtra("topLeftLat");
            topLeftLng = data.getStringExtra("topLeftLng");
            bottomRightLat = data.getStringExtra("bottomRightLat");
            bottomRightLng = data.getStringExtra("bottomRightLng");
            keyword = data.getStringExtra("keyword");
            try {
                loadFiles();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //Stop Method Tracing for Search feature
            Debug.stopMethodTracing();
        }

        if(requestCode == REQUEST_CHECK_SETTINGS){
            checkPermission();
            getLocation();
        }

        if (tempFile != null) {
            tempFile.delete();
        }
    }

    private static String parseCaption(File file) {
        String path = file.getAbsolutePath();
        String[] attr = path.split("_");
        return attr[3];
    }

    private static Date parseDate(File file) throws ParseException {
        String path = file.getAbsolutePath();
        String[] attr = path.split("_");
        return new SimpleDateFormat("yyyyMMdd").parse(attr[1]);
    }

    private static Double parseLat(File file) throws ParseException {
        String path = file.getAbsolutePath();
        String[] attr = path.split("_");
        return Double.parseDouble(attr[5]);
    }

    private static Double parseLng(File file) throws ParseException {
        String path = file.getAbsolutePath();
        String[] attr = path.split("_");
        Log.d("Debug", path);
        return Double.parseDouble(attr[4]);
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

    /**
     * called after the photo is taken: creates the file and sets the name
     *
     * @return
     * @throws IOException
     */
    @SuppressLint("MissingPermission")
    private File createImageFile() throws IOException {
        //create an image file name
        DecimalFormat format = new DecimalFormat("#.####");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_" + timeStamp + "_caption_" + format.format(Longitude) + "_" + format.format(Latitude) + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, //prefix
                ".jpg", //suffix
                storageDir //directory
        );

        addPhoto(image);
        return image;
    }

    /**
     * adds the photo to the beginning of the list
     *
     * @param file
     */
    private void addPhoto(File file) {
        files.add(0, file);
    }

    private void log(String string) {
        Log.d("Debug", string);
    }

    /**
     * Updates the photo's file path with a newly updated caption
     *
     * @param caption
     */
    private void updatePhoto(String caption) {
        String[] attr = files.get(gallery_index).toString().split("_");
        Log.d("Attr Lenght", " = " + attr.length);
        if (attr.length >= 5) {
            // get the old file name and rename it with new caption included
            File oldFile = files.get(gallery_index);
            File newFile = new File(appFolder, "_" + attr[1] + "_" + attr[2] + "_" + caption + "_" + attr[4]  + "_" + attr[5] + "_.jpg");

            if (oldFile.renameTo(newFile)) {
                files = new ArrayList<File>(Arrays.asList(appFolder.listFiles()));
//                Log.d("File rename","Successfully renamed file to " + files.get(gallery_index).getAbsolutePath());
//                Log.d("File rename","Successfully renamed file to " + files.get(gallery_index).getPath());
            } else {
//                Log.d("File rename","Could not rename file " + newFile.getName());
//                Log.d("File 1 Exists: ", "" + files.get(gallery_index).exists());
//                Log.d("File 2 Exists: ", "" + newFile.exists());
            }
        }
    }
}