package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivityIntentHandler {

    // ...attributes
    public Uri imageUriFromFile = null;
    public Bitmap imageBitmapFromCamera = null;

    // i.e. "~app/cache-folder-name/images"
    private static String subdirOfAppSpecificCacheDirectory = "images";
    private static String filenameForTemporaryFile = "imagepicker_shared.png";


    // TODO you are responsible for deleting this (overwrite itself for now, limits to 1 file)
    private static File getTemporaryFileFromCache(Context context) throws Exception {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        try {
            File outputFile = File.createTempFile("imagepicker_shared", ".png", outputDir);
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static File getTemporaryFileFromCacheOld(Context context) throws Exception {
        File imagesFolder = new File(context.getCacheDir(), subdirOfAppSpecificCacheDirectory);
        boolean allDirectoriesCreated = imagesFolder.mkdirs(); // Create the folder if it doesn't exist
        if (allDirectoriesCreated) {
            return new File(imagesFolder, filenameForTemporaryFile);
        } else {
            // TODO class extends Exception
            throw new Exception("Failed to create all directories for temporary file in cache directory");
        }

//        return new File(imagesFolder, filenameForTemporaryFile);
    }


    private Uri createUriFromBitmap(Context context, Bitmap bitmap) {
        try {
            File file = getTemporaryFileFromCache(context);

            // Write the Bitmap to the file
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            return Uri.fromFile(file);

            // NOTE: providers only needed when sharing with other apps
            // ... also, if you go down this path, you must define a <provider> in AndroidManifest (nested in <application>) and a path in a resource e.g. res/xml/file_paths.xml
            // https://developer.android.com/training/secure-file-sharing/setup-sharing
//            // Get URI using FileProvider (required for Android 7.0+)
//            return FileProvider.getUriForFile(context, "org.cmucreatelab.android.cameraandimageselect.demo.fileprovider", file);

//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void updateResult(Uri uri) {
        this.imageUriFromFile = uri;
        this.imageBitmapFromCamera = null;
    }


    public void updateResult(Bitmap bitmap) {
        this.imageUriFromFile = null;
        this.imageBitmapFromCamera = bitmap;
    }


    /**
     *
     * @return
     */
    public Uri getResultUri(Context context) {
        Uri result = null;
        if (imageUriFromFile != null) {
            result = imageUriFromFile;
        } else if (imageBitmapFromCamera != null) {
            result = createUriFromBitmap(context, imageBitmapFromCamera);
        }
        return result;
    }

}
