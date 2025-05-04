package org.cmucreatelab.android.cameraandimageselect.demo;

import android.content.Context;

import java.io.File;
import java.io.IOException;

public class TemporaryFileHandler {

    private static final String temporaryFilePrefix = "imagepicker_shared";

    private static final String temporaryFileSuffix = ".png";


    // TODO you are responsible for deleting this (overwrite itself for now, limits to 1 file)
    public static File getTemporaryFileFromCache(Context context) throws Exception {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        try {
            File outputFile = File.createTempFile(temporaryFilePrefix, temporaryFileSuffix, outputDir);
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
