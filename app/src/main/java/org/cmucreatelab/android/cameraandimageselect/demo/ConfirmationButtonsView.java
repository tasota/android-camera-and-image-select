package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ConfirmationButtonsView extends ConstraintLayout {

    private ImageButton imageButtonYes, imageButtonNo;
    private ProgressBar confirmationProgressBar;


    public ConfirmationButtonsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_confirmation_buttons, this);
        // TODO set attribtues
    }


//    public ConfirmationButtonsView(@NonNull Context context) {
//        super(context);
//        LayoutInflater.from(context).inflate(R.layout.view_confirmation_buttons, this);
//        // TODO set attribtues
//    }

}
