package com.example.photogallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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


public class PhotoManager {
    private static PhotoManager instance;
    private File appFolder;
    public ArrayList<File> files;
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
