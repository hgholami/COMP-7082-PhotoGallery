package com.example.photogallery;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;


public class PhotoManager {
    private static PhotoManager instance;
    public ArrayList<File> fileList;

    private PhotoManager(Context context){

    }

    public static PhotoManager getInstance() {
        if (instance == null)
            throw new NullPointerException("Please call initialize() before getting the instance.");
        return instance;
    }

    public synchronized static void initialize(Context context) {
        if (context == null)
            throw new NullPointerException("Provided application context is null");
        else if (instance == null) {
            instance = new PhotoManager(context);
        }
    }
    //File appFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
}
