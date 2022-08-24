package com.isb.webplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class NamedBitmap {

    Bitmap nbitmap;
    private String name;
    int scaleFactor = 0;

    public NamedBitmap(String name)
    {
        this.name = name;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(name, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;


        // 1920x1080
        scaleFactor = Math.min(photoW/1920, photoH/1080);

        if (scaleFactor>2)
        {
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true; //Deprecated API 21

            nbitmap= BitmapFactory.decodeFile(name, bmOptions);
        }
    }
}
