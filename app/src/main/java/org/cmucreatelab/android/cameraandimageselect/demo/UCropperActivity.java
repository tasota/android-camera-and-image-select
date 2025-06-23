package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class UCropperActivity extends AppCompatActivity {

    private String sourceUri, destinationUri;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ucropper);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent result = getIntent();
        Uri imageUri= result.getParcelableExtra(CameraActivity.RESULT_INTENT_EXTRA_IMAGE_URI);
        sourceUri = imageUri.getPath();
        destinationUri = new StringBuilder(UUID.randomUUID().toString()).append(".jpeg").toString();

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), destinationUri)))
                .withOptions(options)
//                .withAspectRatio(16,16)
//                .withMaxResultSize(1080,1080)
                .start(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Intent intent = new Intent(this, CircleSelectorActivity.class);
            intent.putExtra(CameraActivity.RESULT_INTENT_EXTRA_IMAGE_URI, resultUri);
            intent.putExtra("source_activity", "uCropActivity");

            setResult(RESULT_OK, intent);
            startActivity(intent);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }
}