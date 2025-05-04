package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class BitmapUtil {

    private static final String logTag = "bitmap-util";


    public static Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Log.d(logTag, String.format("rotating bitmap by %d degrees", rotationDegrees));
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    public static Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap unrotatedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return rotateBitmap(unrotatedBitmap, rotationDegrees);
    }


    public static Uri createUriFromBitmap(Context context, Bitmap bitmap) {
        try {
            File file = TemporaryFileHandler.getTemporaryFileFromCache(context);

            // Write the Bitmap to the file
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
