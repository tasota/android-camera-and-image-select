package org.cmucreatelab.android.cameraandimageselect.demo;

import android.app.Activity;
import android.content.Intent;

public class ImageChooser {

    // image picker stuff
    // ... (taken from https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/)
    // other options
    // https://developer.android.com/training/data-storage/shared/photopicker#java

    public static final int SELECT_PICTURE = 200;


    public static void launch(Activity activity) {
        launch(activity, SELECT_PICTURE);
    }


    public static void launch(Activity activity, int requestCode) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(
                Intent.createChooser(i, "Select Picture"),
                requestCode);
    }

}
