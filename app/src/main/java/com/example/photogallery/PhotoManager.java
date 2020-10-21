package com.example.photogallery;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;


public class PhotoManager {
    private static PhotoManager instance;
    private File appFolder;
    public ArrayList<File> files;
    public Context context;
    
    //Filters
    private String startDate = "";
    private String endDate = "";

    private String topLeftLat = "";
    private String topLeftLng = "";
    private String bottomRightLat = "";
    private String bottomRightLng = "";
    private String keyword = "";

    private PhotoManager(Context context){
        this.context = context;
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

    public void loadFiles() throws ParseException {
        //appFolder is set to a file with the directory internal storage/Android/data/com.example.photogallery/files/Pictures
        //if the file doesnt exist, it creates a directory
        appFolder = this.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!appFolder.exists()) {
            if (appFolder.mkdir())
                Log.d("Directory Creation", "Directory: " + appFolder.getAbsolutePath());
        }
        if (appFolder.listFiles() == null) {
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
                    if (!topLeftLat.isEmpty() && Double.parseDouble(topLeftLat) > lat) {
                        it.remove();
                        continue;
                    }
                    if (!bottomRightLat.isEmpty() && Double.parseDouble(bottomRightLat) < lat) {
                        it.remove();
                        continue;
                    }
                    if (!topLeftLng.isEmpty() && Double.parseDouble(topLeftLng) > lng) {
                        it.remove();
                        continue;
                    }
                    if (!bottomRightLng.isEmpty() && Double.parseDouble(bottomRightLng) > lng) {
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
}
