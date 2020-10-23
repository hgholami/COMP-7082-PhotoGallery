package com.example.photogallery;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoFile implements PhotoConverter<File, File> {

    private double longitude;
    private double latitude;

    public PhotoFile(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public File create(File source) {

        DecimalFormat format = new DecimalFormat("#.####");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "_" + timeStamp + "_caption_" + format.format(longitude) + "_" + format.format(latitude) + "_";

        File storageDir = source;
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName, //prefix
                    ".jpg", //suffix
                    storageDir //directory
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

}
