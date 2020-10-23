package com.example.photogallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class PhotoManager {
    private static PhotoManager instance;
    private File appFolder;
    public List<File> files;
    public Context context;
    private int gallery_index = 0;
    //Filters
    private String startDate = "";
    private String endDate = "";

    private String topLeftLat = "";
    private String topLeftLng = "";
    private String bottomRightLat = "";
    private String bottomRightLng = "";
    private String keyword = "";

    private PhotoFile phFile;

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

    @RequiresApi(api = Build.VERSION_CODES.N)
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
            files = filterFiles(files,  keyword,  startDate,  endDate, topLeftLat,  topLeftLng, bottomRightLat,  bottomRightLng);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<File> filterFiles(List<File> files, String keyword,
                                  String startDate, String endDate,
                                  String topLeftLat, String topLeftLng,
                                  String bottomRightLat, String bottomRightLng) {

        return files.stream().filter((item) -> {
            try {
                Date curDate = parseDate(item);
                double lat = parseLat(item);
                double lng = parseLng(item);

                if (!keyword.isEmpty() && !parseCaption(item).contains(keyword)) {
                    return false;
                }
                if (!startDate.isEmpty() && new SimpleDateFormat("yyyy-MM-dd").parse(startDate).compareTo(curDate) > 0) {
                    return false;
                }
                if (!endDate.isEmpty() && new SimpleDateFormat("yyyy-MM-dd").parse(endDate).compareTo(curDate) < 0) {
                    return false;
                }
                if (!topLeftLat.isEmpty() && Double.parseDouble(topLeftLat) > lat ) {
                    return false;
                }
                if (!bottomRightLat.isEmpty() && Double.parseDouble(bottomRightLat) < lat ) {
                    return false;
                }
                if (!topLeftLng.isEmpty() && Double.parseDouble(topLeftLng) > lng ) {
                    return false;
                }
                if (!bottomRightLng.isEmpty() && Double.parseDouble(bottomRightLng) > lng ) {
                    return false;
                }
            }
            catch (Exception ex) {
                // If parsing errors occur, just filter out the image
                return false;
            }
            return true;

        }).collect(Collectors.toList());
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

    private void addPhoto(File file) {
        files.add(0, file);
    }

    public void updatePhoto(String caption) {
        String[] attr = files.get(gallery_index).toString().split("_");
        Log.d("Attr Length", " = " + attr.length);
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

    /**
     * called after the photo is taken: creates the file and sets the name
     *
     * @return
     * @throws IOException
     */
    @SuppressLint("MissingPermission")
    public File createImageFile(double Longitude, double Latitude) throws IOException {
        //create an image file name
        phFile = new PhotoFile(Longitude, Latitude);
        File image = phFile.create(appFolder);

        addPhoto(image);
        return image;
    }

    public int getGallery_index() {
        return gallery_index;
    }
    public void setGallery_index(int i){
        gallery_index = i;
    }
    public File getAppFolder(){
        return appFolder;
    }
    public String getPhoto(){
        if (files.size() > 0) {
            return files.get(gallery_index).toString();
        } else {
            return "";
        }

    }
    public int getSize(){return files.size();}
    public void increment(){gallery_index++;}
    public void decrement(){gallery_index--;}
    public boolean isEmpty(){return files.isEmpty();}

    public void setFilter(String sDate, String eDate, String tlLat, String tlLong, String brLat, String brLong,String k){
        startDate = sDate;
        endDate = eDate;
        topLeftLat = tlLat;
        topLeftLng = tlLong;
        bottomRightLat = brLat;
        bottomRightLng = brLong;
        keyword = k;
    }
}
