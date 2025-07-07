package org.cmucreatelab.android.mylibrary;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static final int PERMISSION_REQUEST_CODE = 123;

    public static String[] requiredPermissions = new String[] {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
            // Add WRITE_EXTERNAL_STORAGE if targeting SDK < 29
    };

    public static boolean hasAllPermissions(Activity activity) {
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestAllPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, requiredPermissions, PERMISSION_REQUEST_CODE);
    }

    public static boolean handlePermissionResult(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(activity, "All permissions are required for this feature.", Toast.LENGTH_LONG).show();
                    Log.e("PermissionHelper", "Permission denied");
                    return false;
                }
            }
            Log.d("PermissionHelper", "All permissions granted");
            return true;
        }
        return false;
    }
}