package com.example.photogallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static int gallery_index = 0;
    private ArrayList<File> files = null;
    private File appFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if(!appFolder.exists()){
            if(appFolder.mkdir())
                Log.d("Directory Creation", "Directory: " + appFolder.getAbsolutePath());
        }

        //gets all files in the picture directory
        //String path = Environment.getExternalStorageDirectory().toString()+"/Android/data/com.example.photogallery/files/";
        //Log.d("Files","Path: " + gallery_dir.getAbsolutePath());

        //grabs the file directory and list of image names
        //File directory = new File(appFolder.getAbsolutePath());
        if(appFolder.listFiles() != null) {
            files = new ArrayList<File>(Arrays.asList(appFolder.listFiles()));
        }
        //Log.d("Name",files[0].getName());
//        Log.d("Files", "Size: " + files.length);
//        for(int i = 0; i < files.length;i++){
//            Log.d("Files", "FileName: " + files[i].getName());
//        }

//        Log.d("FilePath", files[0].toString());
//        Log.d("# of files"," "+ files.length);
//        Log.d("exists: ", ""+ files[0].exists());

        //set first part of gallery to view
        if(!files.isEmpty()) {
            ImageView view = findViewById(R.id.photoView);
            view.setImageBitmap(BitmapFactory.decodeFile(files.get(0).getAbsolutePath()));
        }
    }

    public void navGallery(View view){
        if(files != null){
            ImageView img_view = (ImageView) findViewById(R.id.photoView);
            switch(view.getId()){
                case R.id.leftButton:
                    if(gallery_index != 0){
                        gallery_index--;
                        img_view.setImageBitmap(BitmapFactory.decodeFile(files.get(gallery_index).toString()));
                    }
                    break;
                case R.id.rightButton:
                    if(gallery_index < files.size()-1){
                        gallery_index++;
                        img_view.setImageBitmap(BitmapFactory.decodeFile(files.get(gallery_index).toString()));
                    }
                    break;
            }
        }
    }

    public void takePicture(View view){
        //creates an intent for taking a picture
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //if picture is taken create file to write to in the directory created when app is installed
        if(takePictureIntent.resolveActivity(getPackageManager())!=null){
            //create file for the photo to overwrite
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

            File imgFile = new File(appFolder.getAbsolutePath() + "/");

            if(imgFile.exists()){

                gallery_index = 0;
                Bitmap myBitmap = BitmapFactory.decodeFile(files.get(gallery_index).getAbsolutePath());
                ImageView view = findViewById(R.id.photoView);
                view.setImageBitmap(myBitmap);

                //TextView date = findViewById(R.id.textView1);
                //date.setText(new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()));
            }



        }
    }

    String currentPhotoPath;

    private File createImageFile() throws IOException {
        //create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, //prefix
                ".jpg", //suffix
                storageDir //directory
        );
        //Log.d("PATH", currentPhotoPath);
       //currentPhotoPath = image.getAbsolutePath();
        addPhoto(image);
        return image;
    }

    private void addPhoto(File file){
        files.add(0,file);
    }
}