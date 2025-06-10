package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


public class MainActivity extends AppCompatActivity {

    private static String logTag = "main-activity";

    private ImageView imageView;

    // Launcher to receive result from ImagePickerActivity
    private final ActivityResultLauncher<Intent> pickerActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.v(logTag, "ActivityResultLauncher RESULT_OK");
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getParcelableExtra(CameraActivity.RESULT_INTENT_EXTRA_IMAGE_URI);
                        if (imageUri != null) {
                            //imageView.setImageURI(imageUri);
                            onActivityResultImage(imageUri);
                        }
                    } else {
                        Log.w(logTag, "ActivityResultLauncher received result OK but data was null.");
                        // TODO handle OK but no result?
                    }
                }
                if (result.getResultCode() == RESULT_CANCELED) {
                    Log.v(logTag, "ActivityResultLauncher RESULT_CANCELED");
                    // TODO cancel actions?
                }
            });


    private void onActivityResultImage(Uri uri) {
        Log.v(logTag, String.format("Got image Uri %s", uri.toString()));
        //imageView.setImageURI(uri);
        Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imageView);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.imageView = findViewById(R.id.imageView);
    }


    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.buttonActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("demo-app", "buttonActivity onClick");
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                //startActivity(intent);
                pickerActivityLauncher.launch(intent);
            }
        });
    }

}