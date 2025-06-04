package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;

public class CameraActivityIntentHandler implements Parcelable {

    public Uri imageUriFromFile = null;
    public Bitmap imageBitmapFromCamera = null;


    public void updateResult(Uri uri) {
        this.imageUriFromFile = uri;
        this.imageBitmapFromCamera = null;
    }


    public void updateResult(Bitmap bitmap) {
        this.imageUriFromFile = null;
        this.imageBitmapFromCamera = bitmap;
    }


    public Uri getResultUri(Context context) {
        Uri result = null;
        if (imageUriFromFile != null) {
            result = imageUriFromFile;
        } else if (imageBitmapFromCamera != null) {
            // TODO Add a spinner since this takes a long time (several seconds)
            // TODO Camera Activity setVisibility on "indeterminateBar"
            result = BitmapUtil.createUriFromBitmap(context, imageBitmapFromCamera);
        }
        return result;
    }


    public CameraActivityIntentHandler() {
        this(null, null);
    }

    // Parcelable Implementation

    // CREATOR to create an instance from a Parcel
    public static final Creator<CameraActivityIntentHandler> CREATOR = new Creator<CameraActivityIntentHandler>() {
        @Override
        public CameraActivityIntentHandler createFromParcel(Parcel in) {
            return new CameraActivityIntentHandler(in);
        }

        @Override
        public CameraActivityIntentHandler[] newArray(int size) {
            return new CameraActivityIntentHandler[size];
        }
    };


    // Constructor
    public CameraActivityIntentHandler(Uri imageUri, Bitmap imageBitmap) {
        this.imageUriFromFile = imageUri;
        this.imageBitmapFromCamera = imageBitmap;
    }


    // Constructor for restoring from Parcel
    protected CameraActivityIntentHandler(Parcel in) {
        this.imageUriFromFile = in.readParcelable(Uri.class.getClassLoader());

        // Convert byte array back to Bitmap
        byte[] bitmapByteArray = in.createByteArray();
        if (bitmapByteArray != null) {
            this.imageBitmapFromCamera = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
        }
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write the Uri to Parcel
        dest.writeParcelable(imageUriFromFile, flags);

        // Convert Bitmap to byte array and write to Parcel
        if (imageBitmapFromCamera != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmapFromCamera.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream); // You can choose JPEG or PNG
            byte[] bitmapByteArray = byteArrayOutputStream.toByteArray();
            dest.writeByteArray(bitmapByteArray);
        } else {
            dest.writeByteArray(null); // If Bitmap is null, write null byte array
        }
    }


    @Override
    public int describeContents() {
        return 0;  // No special contents for this object
    }

}
