package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ConfirmationButtonsView extends ConstraintLayout {

    private static final String logTag = "camera-activity";

    private final ImageButton imageButtonYes, imageButtonNo;
    private final ProgressBar confirmationProgressBar;


    public ConfirmationButtonsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_confirmation_buttons, this);

        this.imageButtonYes = findViewById(R.id.imageButtonYes);
        this.imageButtonNo = findViewById(R.id.imageButtonNo);
        this.confirmationProgressBar = findViewById(R.id.confirmationProgressBar);
    }


    @UiThread
    public void showSpinner() {
        Log.v(logTag, "ConfirmationButtonsView.showSpinner");
        confirmationProgressBar.setVisibility(VISIBLE);
    }


    @UiThread
    public void hideSpinner() {
        Log.v(logTag, "ConfirmationButtonsView.hideSpinner");
        confirmationProgressBar.setVisibility(GONE);
    }


    public void setOnClickListenerForImageButtonYes(OnClickListener listener) {
        imageButtonYes.setOnClickListener(listener);
    }


    public void setOnClickListenerForImageButtonNo(OnClickListener listener) {
        imageButtonNo.setOnClickListener(listener);
    }

}
