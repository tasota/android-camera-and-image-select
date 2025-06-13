package org.cmucreatelab.android.cameraandimageselect.demo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.drawable.Drawable;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {

    public static final String RESULT_INTENT_EXTRA_IMAGE_URI = "image_uri";

    public enum CameraActivityState {
        VIEW_CAMERA,
        VIEW_IMAGE_CHOOSER,
        VIEW_PREVIEW_FROM_CAMERA,
        VIEW_PREVIEW_FROM_FILE
    }

    private static final String logTag = "camera-activity";

    // Persistent class attributes (need reconstructed on configuration changes)
    private int cameraSelectorLensFacing = CameraSelector.LENS_FACING_BACK;
    private CameraActivityIntentHandler intentHandler = null;
    private CameraActivityState cameraActivityState = CameraActivityState.VIEW_CAMERA;

    // Transient attrs
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ImageCapture imageCapture;
    private int ogConfiguration;
    private boolean isImageCaptured = false;

    private void setupCamera() {
        // finds target rotation
        isImageCaptured = false;
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



    private void takePictureAndAccessFromMemory() {
        if (imageCapture == null) {
            Toast.makeText(this, "ImageCapture not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use a Handler to post the callback on the main thread
        Executor executor = command -> new Handler(Looper.getMainLooper()).post(command);

        //save camera image to file then convert to uri
        File cameraImageFile = new File(getCacheDir(), "image.jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(cameraImageFile).build();
        imageCapture.takePicture(outputOptions, executor, new ImageCapture.OnImageSavedCallback(){
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                //intentHandler.imageUriFromCamera = outputFileResults.getSavedUri();
                Uri savedCameraUri = outputFileResults.getSavedUri();
                if(savedCameraUri == null){
                    Log.v(logTag, "savedCameraUri is null creatingURI from file");
                    savedCameraUri = FileProvider.getUriForFile(
                            CameraActivity.this, getApplicationContext().getPackageName() + ".provider", cameraImageFile
                    );
                }
                if(intentHandler.imageUriFromCamera == null)
                {
                    Log.e(logTag, "imageUriFromCamera is null");
                    // ToDo handle error
                }
                intentHandler.imageUriFromCamera = savedCameraUri;
                intentHandler.updateCameraResult(intentHandler.imageUriFromCamera);
                CameraActivity.this.cameraActivityState = CameraActivityState.VIEW_PREVIEW_FROM_CAMERA;
                ogConfiguration = getResources().getConfiguration().orientation;

                ImageView previewImage = findViewById(R.id.previewImageView);
                previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                doViewPreviewFromCamera();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                // TODO handle error
                Log.e(logTag, "onImageSaved onError");
            }

        });
    }


    private void cameraFlip() {
        this.cameraSelectorLensFacing = (cameraSelectorLensFacing == CameraSelector.LENS_FACING_BACK) ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;
        setupCamera();
        doViewCamera();
    }


    private void    updateViewToDisplayPreview() {
        runOnUiThread(() -> {
            //---RESOLVED----
            // TODO we want to hide/show previewView when using the image/file picker
            //---RESOLVED----
            // NOTE: do not hide PreviewView (avoid black scrren)
            //findViewById(R.id.previewView).setVisibility(View.GONE);
            findViewById(R.id.captureButton).setVisibility(View.INVISIBLE);
            findViewById(R.id.imageButtonSwitchCamera).setVisibility(View.INVISIBLE);
            findViewById(R.id.previewImageView).setVisibility(View.VISIBLE);
            findViewById(R.id.imageButtonNo).setVisibility(View.VISIBLE);
            findViewById(R.id.imageButtonYes).setVisibility(View.VISIBLE);
            hideSpinner();

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
        if (imageCapture == null) {
            Log.v(logTag, "doViewCamera found imageCapture is null, calling setupCamera()");
            //---RESOLVED-----
            // TODO we want to call this when camera flip button is clicked, but we don't want to call this when hitting the "no" button
            //----RESOLVED-----
            setupCamera();
        } else {
            Log.v(logTag, "doViewCamera skipping setupCamera");
        }
        updateViewToDisplayLiveCamera();
    }


    private void doViewImageChooser() {
        this.cameraActivityState = CameraActivityState.VIEW_IMAGE_CHOOSER;
        findViewById(R.id.previewView).setVisibility(View.GONE);
        ImageChooser.launch(this);
    }


    private void doViewPreviewFromCamera() {


        isImageCaptured = true;
        Log.v(logTag, "doViewPreviewFromCamera");
        this.cameraActivityState = CameraActivityState.VIEW_PREVIEW_FROM_CAMERA;
        ImageView previewImage = findViewById(R.id.previewImageView);
        Glide.with(CameraActivity.this)
                .load(intentHandler.imageUriFromCamera)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(previewImage);
        updateViewToDisplayPreview();
        //hideSpinner();
    }


    private void doViewPreviewFromFile() {
        isImageCaptured = true;
        this.cameraActivityState = CameraActivityState.VIEW_PREVIEW_FROM_FILE;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView previewImage = findViewById(R.id.previewImageView);
                updateScaleTypeAfterRotation();
                //previewImage.setImageURI(intentHandler.imageUriFromFile);
                Glide.with(CameraActivity.this)
                        .load(intentHandler.imageUriFromFile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(previewImage);
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

    //rotates the image based on changing orientation
    private void updateScaleTypeAfterRotation() {
        int newConfiguration = getResources().getConfiguration().orientation;
        ImageView previewImage = findViewById(R.id.previewImageView);


        if (newConfiguration != ogConfiguration) {
          if(newConfiguration == Configuration.ORIENTATION_LANDSCAPE){
              previewImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
          } else {
              previewImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
          }

        } else {
            previewImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    //Sets up the camera after a rotation during image select to prevent black screen delay
    private void handleCameraRotationSetup(){
        int config = getResources().getConfiguration().orientation;
        setupCamera();
        if(config != ogConfiguration) {
            findViewById(R.id.previewView).setVisibility(View.INVISIBLE);
        }

    }

    private void showSpinner() {
        findViewById(R.id.indeterminateBar).setVisibility(View.VISIBLE);
    }

    private void hideSpinner() {
        findViewById(R.id.indeterminateBar).setVisibility(View.GONE);
    }


    private void initializeViewOnClickListeners() {
        findViewById(R.id.captureButton).setOnClickListener(v -> {
            Log.v(logTag, "captureButton onClick");
            showSpinner();
            takePictureAndAccessFromMemory();
        });
        findViewById(R.id.imageButtonFolder).setOnClickListener(v -> {
            Log.v(logTag, "imageButtonFolder onClick");
            doViewImageChooser();
        });
        findViewById(R.id.imageButtonSwitchCamera).setOnClickListener(v -> {
            Log.v(logTag, "imageButtonSwitchCamera onClick");
            cameraFlip();
        });
        findViewById(R.id.imageButtonNo).setOnClickListener(v -> {
            Log.v(logTag, "imageButtonNo onClick");
            doViewCamera();
        });
        findViewById(R.id.imageButtonYes).setOnClickListener(v -> {
            Log.v(logTag, "imageButtonYes onClick");
            Uri resultUri = intentHandler.getResultUri(getApplicationContext());
            finishActivityWithResult(Activity.RESULT_OK, resultUri);
        });
        findViewById(R.id.imageButtonCancel).setOnClickListener(v -> {
            Log.v(logTag, "imageButtonCancel onClick");
            finishActivityWithResult(Activity.RESULT_CANCELED, null);
        });
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v(logTag, "onSaveInstanceState");

        // save on rotation changes only; do not save instance state when leaving the activity (or else the Parcel is too large?)]
        //----RESOLVED--------
        // TODO previewImageView should switch between centerCrop and centerInside when captured image is displayed
        //------RESOLVED--------
        if (isChangingConfigurations()) {
            outState.putString("camera_state", cameraActivityState.name());
            outState.putInt("camera_lens", cameraSelectorLensFacing);
            outState.putParcelable("intent_handler", intentHandler);
            outState.putInt("configuration", ogConfiguration);
            outState.putBoolean("isImageCaptured", isImageCaptured);
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
        this.ogConfiguration = savedInstanceState.getInt("configuration");
        this.isImageCaptured = savedInstanceState.getBoolean("isImageCaptured");

        if(isImageCaptured){
            updateScaleTypeAfterRotation();
           handleCameraRotationSetup();
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(logTag, "onCreate");
        setContentView(R.layout.activity_camera);

        // TODO handle run-time permissions (here, or onResume?) (note: this is already handled in StudentUpdateAbstractActivity)
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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // requestCode to match our intent
            if (requestCode == ImageChooser.SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    Log.i(logTag, "onActivityResult got result with selectedImageUri");
                    intentHandler.updateFileResult(selectedImageUri);
                    isImageCaptured=true;
                    doViewPreviewFromFile();
                }
            }

        }
    }

}