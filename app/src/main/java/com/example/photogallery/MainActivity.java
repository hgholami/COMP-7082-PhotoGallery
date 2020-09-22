package com.example.photogallery;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private int gallery_index = 0;
    private ArrayList<File> files = null;
    private File appFolder;
    private String currentCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //appFolder is set to a file with the directory internal storage/Android/data/com.example.photogallery/files/Pictures
        //if the file doesnt exist, it creates a directory
        appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(!appFolder.exists()){
            if(appFolder.mkdir())
                Log.d("Directory Creation", "Directory: " + appFolder.getAbsolutePath());
        }

        //grabs a list of files from the appFolder directory
        if(appFolder.listFiles() != null) {
            files = new ArrayList<File>(Arrays.asList(appFolder.listFiles()));
        }

        //set first part of gallery to view
//        if(!files.isEmpty()) {
//            ImageView view = findViewById(R.id.photoView);
//            view.setImageBitmap(BitmapFactory.decodeFile(files.get(0).getAbsolutePath()));
//        }
        if (files.size() == 0) {
            displayPhoto(null);
        } else {
            displayPhoto(files.get(gallery_index).toString());
        }
    }

    /**
     * called by the left and right buttons from the MainActivity
     * checks if the button that called the function has the id leftButton or rightButton
     * @param view
     */
    public void navGallery(View view){
        if(!files.isEmpty()){

            EditText caption = (EditText) findViewById(R.id.editCaption);
            Log.d("Caption ", "" + caption.getText().toString());
            if(!currentCaption.equals(caption.getText().toString())) {
                updatePhoto((caption.getText().toString()));
                Log.d("Caption changed from", "" + currentCaption);
                Log.d("Caption changed to", "" + caption.toString());
            } else {
                Log.d("Caption has not changed", "" + currentCaption);
                Log.d("Caption has not changed", "" + caption.getText().toString());
            }
            switch(view.getId()){
                case R.id.leftButton:
                    if(gallery_index != 0){
                        gallery_index--;
                    }
                    break;
                case R.id.rightButton:
                    if(gallery_index < files.size()-1){
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
     * @param view
     */
    public void takePicture(View view){
        //creates an intent for taking a picture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //if picture is taken create file to write to in the directory created when app is installed
        if(takePictureIntent.resolveActivity(getPackageManager())!=null){

            //create file for the photo to write to
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch(IOException ex){

            }

            //check if image file is created
            if(photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.fileprovider",photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //gets folder where the pictures taken in the app are stored
            File imgFile = new File(appFolder.getAbsolutePath() + "/");

            //checks if the image file was created successfully
            if(imgFile.exists()) {

                //sets the index to 0 and grabs the image at that index which is the latest taken picture
                gallery_index = 0;
                displayPhoto(files.get(gallery_index).getAbsolutePath());
            }

        }
    }

    /**
     * Displays the photo retrieve by the current file path
     * @param path
     */
    private void displayPhoto(String path) {
        ImageView img_view = (ImageView) findViewById(R.id.photoView);
        TextView text_view = (TextView) findViewById(R.id.timestamp);
        EditText edit_text = (EditText) findViewById(R.id.editCaption);
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
                Date calDate = new SimpleDateFormat("yyyyMMdd").parse(attr[1]);
                String calDateFormat = new SimpleDateFormat("yyyy-MM-dd").format(calDate);

                Date timeDate = new SimpleDateFormat("HHmmss").parse(attr[2]);
                String timeDateFormat = new SimpleDateFormat("HH:mm:ss").format(timeDate);

                text_view.setText("Timestamp: " + calDateFormat + " " + timeDateFormat);
            } catch (ParseException pe) {}
        }
    }

    /**
     * called after the photo is taken: creates the file and sets the name
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        //create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + "_";
        String imageFileName =  "_"+timeStamp + "_caption_";
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
     * @param file
     */
    private void addPhoto(File file){
        files.add(0,file);
    }

    /**
     * Updates the photo's file path with a newly updated caption
     * @param caption
     */
    private void updatePhoto(String caption) {
        String[] attr = files.get(gallery_index).toString().split("_");
        Log.d("Attr Lenght", " = " + attr.length);
        if (attr.length >= 3) {
            File oldFile = files.get(gallery_index);
            File newFile = new File(appFolder,"_" + attr[1] + "_" + attr[2] + "_" + caption + "_" + ".jpg");

            if (oldFile.renameTo(newFile)) {
                files.set(gallery_index,oldFile);
                //newFile.delete();
                Log.d("File rename","Successfully renamed file to " + newFile.getName());
            } else {
                Log.d("File rename","Could not rename file " + newFile.getName());
                Log.d("File 1 Exists: ", "" + files.get(gallery_index).exists());
                Log.d("File 2 Exists: ", "" + newFile.exists());
            }
        }
    }
}