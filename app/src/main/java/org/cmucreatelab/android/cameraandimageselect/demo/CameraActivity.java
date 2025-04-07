package org.cmucreatelab.android.cameraandimageselect.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.View;
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

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraActivity extends AppCompatActivity {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private static String logTag = "camera-activity";
    private ImageCapture imageCapture;
    //private PreviewView previewView;


    //private void setupCamera() throws ExecutionException, InterruptedException {
    private void setupCamera() {
        // TODO find target rotation
        this.imageCapture =
                new ImageCapture.Builder()
                        //.setTargetRotation(view.getDisplay().getRotation())
                        .setTargetRotation(Surface.ROTATION_0)
                        // NOTE: "If not set, the capture mode will default to CAPTURE_MODE_MINIMIZE_LATENCY."
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        //.setCaptureMode(ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG)
                        .build();
        try {
            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
            LifecycleOwner lifecycleOwner = this;
            // TODO control camera selection (front vs back)
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
            Preview preview = new Preview.Builder().build();
            PreviewView previewView = findViewById(R.id.previewView);
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

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


    // TODO old method delete me (saving to file/IO takes too long)
    private void takePictureAndSaveToFile() {
        PreviewView previewView = findViewById(R.id.previewView);
        if (imageCapture == null) {
            Toast.makeText(this, "ImageCapture not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the image to a file
        File photoFile = new File(
                getExternalMediaDirs()[0],
                System.currentTimeMillis() + "_photo.jpg"
        );

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // NOTE: getMainExecutor() (requires API Level 28)
        ////imageCapture.takePicture(outputFileOptions, getMainExecutor(),
        // NOTE: Handler instead of Executor
        //imageCapture.takePicture(outputFileOptions, new Handler(Looper.getMainLooper()),
        // NOTE: Create an Executor using a Handler to work on the main thread
        Executor executor = command -> new Handler(Looper.getMainLooper()).post(command);

        // Use a Handler to post the callback on the main thread
        imageCapture.takePicture(outputFileOptions, executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        //Log.d(logTag, "Photo captured successfully: " + savedUri);
                        Log.d(logTag, "Photo SAVED successfully: " + savedUri);

                        // You can display the image in an ImageView or handle it further
                        ImageView previewImage = findViewById(R.id.previewImageView);
                        previewImage.setImageURI(savedUri);

                        Toast.makeText(CameraActivity.this, "Picture saved: " + savedUri, Toast.LENGTH_SHORT).show();
                        updateViewToDisplayPreview();
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        Log.e(logTag, "Photo capture failed: " + exception.getMessage(), exception);
                        Toast.makeText(CameraActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // TODO handle takePicture
//        ImageCapture.OutputFileOptions outputFileOptions =
//                new ImageCapture.OutputFileOptions.Builder(new File(...)).build();
//        imageCapture.takePicture(outputFileOptions, cameraExecutor,
//                new ImageCapture.OnImageSavedCallback() {
//                    @Override
//                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
//                        // insert your code here.
//                    }
//                    @Override
//                    public void onError(ImageCaptureException error) {
//                        // insert your code here.
//                    }
//                }
//        );
    }


    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



    private void takePictureAndAccessFromMemory() {
        PreviewView previewView = findViewById(R.id.previewView);
        if (imageCapture == null) {
            Toast.makeText(this, "ImageCapture not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the image to a file
        File photoFile = new File(
                getExternalMediaDirs()[0],
                System.currentTimeMillis() + "_photo.jpg"
        );

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        Executor executor = command -> new Handler(Looper.getMainLooper()).post(command);

        // Use a Handler to post the callback on the main thread
        imageCapture.takePicture(executor,
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        Log.d(logTag, "Photo captured successfully");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ImageView previewImage = findViewById(R.id.previewImageView);
                                previewImage.setImageBitmap(imageProxyToBitmap(image));
                            }
                        });
                        updateViewToDisplayPreview();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // TODO handle error
                        super.onError(exception);
                    }
                }
        );
    }


    private void updateViewToDisplayPreview() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.previewView).setVisibility(View.GONE);
                findViewById(R.id.captureButton).setVisibility(View.GONE);
                findViewById(R.id.previewImageView).setVisibility(View.VISIBLE);
                findViewById(R.id.confirmButton).setVisibility(View.VISIBLE);
                findViewById(R.id.retakeButton).setVisibility(View.VISIBLE);

                findViewById(R.id.imageButtonNo).setVisibility(View.VISIBLE);
                findViewById(R.id.imageButtonYes).setVisibility(View.VISIBLE);
            }
        });
    }


    private void updateViewToDisplayLiveCamera() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.previewView).setVisibility(View.VISIBLE);
                findViewById(R.id.captureButton).setVisibility(View.VISIBLE);
                findViewById(R.id.previewImageView).setVisibility(View.GONE);
                findViewById(R.id.confirmButton).setVisibility(View.GONE);
                findViewById(R.id.retakeButton).setVisibility(View.GONE);

                findViewById(R.id.imageButtonNo).setVisibility(View.GONE);
                findViewById(R.id.imageButtonYes).setVisibility(View.GONE);
            }
        });
    }

    private void photoPreviewRetake() {
        // TODO not necessary to call again?
        //setupCamera();
        updateViewToDisplayLiveCamera();
    }

    private void photoPreviewConfirm() {
        // TODO finish activity (with image as result?)
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);

        // TODO handle run-time permissions (here, or onResume?)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }


    @Override
    protected void onResume() {
        super.onResume();
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
            }
        });
        findViewById(R.id.imageButtonSwitchCamera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "imageButtonSwitchCamera onClick");
            }
        });
        findViewById(R.id.imageButtonNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "imageButtonNo onClick");
                photoPreviewRetake();
            }
        });
        findViewById(R.id.imageButtonYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "imageButtonYes onClick");
                photoPreviewConfirm();
            }
        });

        // TODO demo buttons, remove later
        findViewById(R.id.retakeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "retakeButton onClick");
                photoPreviewRetake();
            }
        });
        findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(logTag, "confirmButton onClick");
                photoPreviewConfirm();
            }
        });

        setupCamera();
    }

}