package com.example.photogallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoUri implements PhotoConverter<Uri, ImageView>{

    //private ImageView imageView;
    private Context context;
    private File tempFile = null;

    public PhotoUri(Context context) {
        this.context = context;
    }

    private void deleteFile() {
        if (tempFile != null) {
            tempFile.delete();
        }
    }

    @Override
    public Uri create(ImageView source) {
        // get drawable of image view
        deleteFile();

        Drawable drawable = source.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) source.getDrawable()).getBitmap();
        } else {
            return null;
        }

        Uri bmpUri = null;
        try {
            // saves the bitmap as a new file before sharing
            tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image" + System.currentTimeMillis() + ".jpg");

            FileOutputStream out = new FileOutputStream(tempFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            bmpUri = FileProvider.getUriForFile(context, "com.example.android.fileprovider", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
