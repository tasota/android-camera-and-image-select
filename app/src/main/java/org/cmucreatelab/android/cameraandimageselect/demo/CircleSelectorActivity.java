package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zigis.segmentedarcview.SegmentedArcView;
import com.zigis.segmentedarcview.custom.ArcSegment;

import java.util.ArrayList;
import java.util.List;

public class CircleSelectorActivity extends AppCompatActivity {
    private ImageView imageView;

    private static String logTag = "selector-activity";

     private void buildSegmentedBorder(){
         SegmentedArcView sa = findViewById(R.id.segmented_arc_view);

         List<ArcSegment> segments = new ArrayList<>();
         segments.add(new ArcSegment(Color.RED, Color.RED,false, 45f));    // 45 degrees
         segments.add(new ArcSegment(Color.GREEN, Color.GREEN,false, 90f)); // 90 degrees
         segments.add(new ArcSegment(Color.BLUE, Color.BLUE,false, 225f));  // 225 degrees

    // Set the segments (custom sweep angles are taken from constructor)
         sa.setSegments(segments);
     }


    // Launcher to receive result from ImagePickerActivity
//    private final ActivityResultLauncher<Intent> pickerActivityLauncher =
//            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//                Log.v(logTag, "ActivityResultLauncher");
//                if (result.getResultCode() == RESULT_OK) {
//                    Log.v(logTag, "ActivityResultLauncher RESULT_OK");
//                    if (result.getData() != null) {
//                        Uri imageUri = result.getData().getParcelableExtra(CameraActivity.RESULT_INTENT_EXTRA_IMAGE_URI);
//                        if (imageUri != null) {
//                            //imageView.setImageURI(imageUri);
//                            onActivityResultImage(imageUri);
//                        }
//                    } else {
//                        Log.w(logTag, "ActivityResultLauncher received result OK but data was null.");
//                        // TODO handle OK but no result?
//                    }
//                }
//                if (result.getResultCode() == RESULT_CANCELED) {
//                    Log.v(logTag, "ActivityResultLauncher RESULT_CANCELED");
//                    // TODO cancel actions?
//                }
//            });

    private void loadImageFromResult(){
        Log.v(logTag, "loading Image");
        Intent result = getIntent();
        Uri imageUri= result.getParcelableExtra(CameraActivity.RESULT_INTENT_EXTRA_IMAGE_URI);
        //imageView.setImageURI(imageUri);
        Glide.with(this)
                .load(imageUri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .dontAnimate()
                .into(imageView);
    }


    private void onActivityResultImage(Uri uri) {
        Log.v(logTag, String.format("Got image Uri %s", uri.toString()));
        //imageView.setImageURI(uri);
        Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .dontAnimate()
                .into(imageView);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(logTag, "onCreate");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_selector);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.selector), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        this.imageView = findViewById(R.id.imageSelectorView);
//        loadImageFromResult();
//        buildSegmentedBorder();
        if(getIntent().getStringExtra("source_activity").equals("cameraActivity")){

            Log.v(logTag, "source_activity is cameraActivity");
            imageView.setVisibility(View.GONE);
            this.imageView = findViewById(R.id.imageRectView);
            loadImageFromResult();


        } else {
            Log.v(logTag, "source_activity is not cameraActivity");
            imageView.setVisibility(View.VISIBLE);
            findViewById(R.id.imageRectView).setVisibility(View.GONE);
            loadImageFromResult();
            buildSegmentedBorder();
        }


    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        findViewById(R.id.buttonActivity).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.v("demo-app", "buttonActivity onClick");
//                Intent intent = new Intent(CircleSelectorActivity.this, CameraActivity.class);
//                //startActivity(intent);
//                pickerActivityLauncher.launch(intent);
//            }
//        });
//    }
}
