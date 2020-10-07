package com.example.photogallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
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
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int SEARCH_ACTIVITY_REQUEST_CODE = 2;
    private int gallery_index = 0;
    private ArrayList<File> files = null;
    private File appFolder;


    //Filters
    private String currentCaption;
    private String startDate = "";
    private String endDate = "";
    private Double Longitude, Latitude;

    private String topLeftLat = "";
    private String topLeftLng = "";
    private String bottomRightLat = "";
    private String bottomRightLng = "";
    private String keyword = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(5000);
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
                Longitude = location.getLongitude();
                Latitude = location.getLatitude();
                Log.d("Debug", "LONG START = " + format.format(location.getLongitude()));
                Log.d("Debug", "LAT START = " + format.format(location.getLatitude()));
            }
        });
        setContentView(R.layout.activity_main);
        try {
            loadFiles();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void loadFiles() throws ParseException {
        //appFolder is set to a file with the directory internal storage/Android/data/com.example.photogallery/files/Pictures
        //if the file doesnt exist, it creates a directory
        appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!appFolder.exists()) {
            if (appFolder.mkdir())
                Log.d("Directory Creation", "Directory: " + appFolder.getAbsolutePath());
        }

        //grabs a list of files from the appFolder directory
        if (appFolder.listFiles() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                files = new ArrayList<File>(Arrays.asList(Objects.requireNonNull(appFolder.listFiles())));
            }

            // applies filters
            // todo: do stuff for longitude and lattitude
            for (Iterator<File> it = files.iterator(); it.hasNext(); ) {
                File item = it.next();
                Date curDate = parseDate(item);
                if (!keyword.isEmpty() && !parseCaption(item).contains(keyword)) {
                    it.remove();
                    continue;
                }

                try {
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
                } catch (Exception ex) {
                    //if format is wrong, show nothing
                    Log.d("Date Parsing Error", String.valueOf(ex));
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
        }
    }

    private String parseCaption(File file) {
        String path = file.getAbsolutePath();
        String[] attr = path.split("_");
        return attr[3];
    }

    private Date parseDate(File file) throws ParseException {
        String path = file.getAbsolutePath();
        String[] attr = path.split("_");
        return new SimpleDateFormat("yyyyMMdd").parse(attr[1]);
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
            File newFile = new File(appFolder, "_" + attr[1] + "_" + attr[2] + "_" + caption + "_" + attr[4]  + "_" + attr[5]+ ".jpg");

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