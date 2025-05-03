package org.cmucreatelab.android.cameraandimageselect.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {

    public static String RESULT_INTENT_EXTRA_IMAGE_URI = "image_uri";

    public enum CameraActivityState {
        VIEW_CAMERA,
        VIEW_IMAGE_CHOOSER,
        VIEW_PREVIEW_FROM_CAMERA,
        VIEW_PREVIEW_FROM_FILE
    }

    private static String logTag = "camera-activity";

    // Persistent class attributes (need reconstructed on configuration changes)
    private int cameraSelectorLensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraActivityIntentHandler intentHandler = null;
    private CameraActivityState cameraActivityState = CameraActivityState.VIEW_CAMERA;

    // Transient attrs
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;


    private void setupCamera() {
        // finds target rotation
        int rotation = ((WindowManager) getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay()
                .getRotation();

        this.imageCapture =
                new ImageCapture.Builder()
                        //.setTargetRotation(view.getDisplay().getRotation())
                        //.setTargetRotation(Surface.ROTATION_0)
                        .setTargetRotation(rotation)
                        // NOTE: "If not set, the capture mode will default to CAPTURE_MODE_MINIMIZE_LATENCY."
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        //.setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
                        .build();
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            LifecycleOwner lifecycleOwner = this;
            // controls front vs back camera
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorLensFacing)
                    .build();
            Preview preview = new Preview.Builder().build();
            PreviewView previewView = findViewById(R.id.previewView);
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            // unbind previous (required for camera flip)
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, preview);
        } catch (ExecutionException e) {
            // TODO do something with the exception?
            Log.e(logTag, "setupCamera has thrown ExecutionException.");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            // TODO do something with the exception?
            Log.e(logTag, "setupCamera has thrown InterruptedException.");
            throw new RuntimeException(e);
        }
    }


    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Log.d(logTag, String.format("rotating bitmap by %d degrees", rotationDegrees));
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        int rotationDegrees = image.getImageInfo().getRotationDegrees();
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap unrotatedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return rotateBitmap(unrotatedBitmap, rotationDegrees);
    }



    private void takePictureAndAccessFromMemory() {
        if (imageCapture == null) {
            Toast.makeText(this, "ImageCapture not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use a Handler to post the callback on the main thread
        Executor executor = command -> new Handler(Looper.getMainLooper()).post(command);

        imageCapture.takePicture(executor,
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        Log.d(logTag, "Photo captured successfully");
                        Bitmap bitmap = imageProxyToBitmap(image);
                        intentHandler.updateResult(bitmap);
                        CameraActivity.this.cameraActivityState = CameraActivityState.VIEW_PREVIEW_FROM_CAMERA;
                        doViewPreviewFromCamera();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // TODO handle error
                        super.onError(exception);
                    }
                }
        );
    }


    private void cameraFlip() {
        this.cameraSelectorLensFacing = (cameraSelectorLensFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        doViewCamera();
    }


    private void updateViewToDisplayPreview() {
        runOnUiThread(() -> {
            findViewById(R.id.previewView).setVisibility(View.GONE);
            findViewById(R.id.captureButton).setVisibility(View.GONE);
            findViewById(R.id.imageButtonSwitchCamera).setVisibility(View.GONE);
            findViewById(R.id.previewImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.imageButtonNo).setVisibility(View.VISIBLE);
            findViewById(R.id.imageButtonYes).setVisibility(View.VISIBLE);
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.previewView).setVisibility(View.GONE);
                findViewById(R.id.captureButton).setVisibility(View.GONE);
                findViewById(R.id.imageButtonSwitchCamera).setVisibility(View.GONE);
                findViewById(R.id.previewImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.imageButtonNo).setVisibility(View.VISIBLE);
                findViewById(R.id.imageButtonYes).setVisibility(View.VISIBLE);

            }
        });
    }


    private void updateViewToDisplayLiveCamera() {
        runOnUiThread(() -> {
            findViewById(R.id.previewView).setVisibility(View.VISIBLE);
            findViewById(R.id.captureButton).setVisibility(View.VISIBLE);
            findViewById(R.id.imageButtonSwitchCamera).setVisibility(View.VISIBLE);
            findViewById(R.id.previewImageView).setVisibility(View.GONE);
            findViewById(R.id.imageButtonNo).setVisibility(View.GONE);
            findViewById(R.id.imageButtonYes).setVisibility(View.GONE);
        });
    }


    private void doViewCamera() {
        this.cameraActivityState = CameraActivityState.VIEW_CAMERA;
        setupCamera();
        updateViewToDisplayLiveCamera();
    }


    private void doViewImageChooser() {
        this.cameraActivityState = CameraActivityState.VIEW_IMAGE_CHOOSER;
        imageChooser();

    }


    private void doViewPreviewFromCamera() {
        this.cameraActivityState = CameraActivityState.VIEW_PREVIEW_FROM_CAMERA;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView previewImage = findViewById(R.id.previewImageView);
                previewImage.setImageBitmap(intentHandler.imageBitmapFromCamera);
            }
        });
        updateViewToDisplayPreview();
    }


    private void doViewPreviewFromFile() {
        this.cameraActivityState = CameraActivityState.VIEW_PREVIEW_FROM_FILE;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView previewImage = findViewById(R.id.previewImageView);
                previewImage.setImageURI(intentHandler.imageUriFromFile);
            }
        });
        updateViewToDisplayPreview();
    }


    private void finishActivityWithResult(int resultCode, Uri resultUri) {
        Intent resultIntent = new Intent();
        if (resultUri != null) {
            resultIntent.putExtra(RESULT_INTENT_EXTRA_IMAGE_URI, resultUri);
        }
        setResult(resultCode, resultIntent);
        finish();
    }


    private void updateActivityWithCameraState() {
        switch (cameraActivityState) {
            case VIEW_CAMERA:
                doViewCamera();
                break;
            case VIEW_PREVIEW_FROM_CAMERA:
                doViewPreviewFromCamera();
                break;
            case VIEW_PREVIEW_FROM_FILE:
                doViewPreviewFromFile();
                break;
            default:
                Log.w(logTag, "could not determine cameraActivityState; default to VIEW_CAMERA.");
                doViewCamera();
        }
    }


    private void initializeViewOnClickListeners() {
        findViewById(R.id.captureButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "captureButton onClick");
                takePictureAndAccessFromMemory();
            }
        });
        findViewById(R.id.imageButtonFolder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "imageButtonFolder onClick");
                doViewImageChooser();
            }
        });
        findViewById(R.id.imageButtonSwitchCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "imageButtonSwitchCamera onClick");
                cameraFlip();
            }
        });
        findViewById(R.id.imageButtonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "imageButtonNo onClick");
                doViewCamera();
            }
        });
        findViewById(R.id.imageButtonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "imageButtonYes onClick");
                Uri resultUri = intentHandler.getResultUri(getApplicationContext());
                finishActivityWithResult(Activity.RESULT_OK, resultUri);
            }
        });
        findViewById(R.id.imageButtonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishActivityWithResult(Activity.RESULT_CANCELED, null);
            }
        });
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(logTag, "onSaveInstanceState");

        // save on rotation changes only; do not save instance state when leaving the activity (or else the Parcel is too large?)
        if (isChangingConfigurations()) {
            outState.putString("camera_state", cameraActivityState.name());
            outState.putInt("camera_lens", cameraSelectorLensFacing);
            outState.putParcelable("intent_handler", intentHandler);
        }
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.v(logTag, "onRestoreInstanceState");

        String stateName = savedInstanceState.getString("camera_state");
        Log.d(logTag, String.format("restoring cameraActivityState name=%s", stateName));
        this.cameraActivityState = CameraActivityState.valueOf(stateName);

        this.cameraSelectorLensFacing = savedInstanceState.getInt("camera_lens");

        // NOTE: requires API level 33+, but who needs type safety at compile time anyways?
        //this.intentHandler = savedInstanceState.getParcelable("intent_handler", CameraActivityIntentHandler.class);
        this.intentHandler = savedInstanceState.getParcelable("intent_handler");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(logTag, "onCreate");
        setContentView(R.layout.activity_camera);

        // TODO handle run-time permissions (here, or onResume?)
        this.cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        this.intentHandler = new CameraActivityIntentHandler();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.v(logTag, "onResume");
        initializeViewOnClickListeners();
        updateActivityWithCameraState();
    }


    // image picker stuff
    // ... (taken from https://www.geeksforgeeks.org/how-to-select-an-image-from-gallery-in-android/)
    // other options
    // https://developer.android.com/training/data-storage/shared/photopicker#java

    public int SELECT_PICTURE = 200;


    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(i, "Select Picture"),
                SELECT_PICTURE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // requestCode to match our intent
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    Log.i(logTag, "onActivityResult got result with selectedImageUri");
                    intentHandler.updateResult(selectedImageUri);
                    doViewPreviewFromFile();
                }
            }

        }
    }

}