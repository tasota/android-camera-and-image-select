package org.cmucreatelab.android.mylibrary;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static final int PERMISSION_REQUEST_CODE = 123;

    public static String[] getRequiredPermissions(){
//
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            return new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        };
        } else {
            return new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }

    };

    public static boolean hasAllPermissions(Activity activity) {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.w("camera-activity", "Missing permission: " + permission);
                return false;
            }
        }
        return true;
    }

    public static void requestAllPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, getRequiredPermissions(), PERMISSION_REQUEST_CODE);
    }

    public static boolean handlePermissionResult(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity, "Permissions are required to continue.", Toast.LENGTH_LONG).show();
                    Log.e("camera-activity", "One or more permissions denied");
                    return false;
                }
            }
            Log.i("camera-activity", "All permissions granted");
            return true;
        }
        return false;
    }
}