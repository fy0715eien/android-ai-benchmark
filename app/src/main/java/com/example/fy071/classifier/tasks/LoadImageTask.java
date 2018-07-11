package com.example.fy071.classifier.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;


import com.example.fy071.classifier.ui.ModelOverviewFragmentController;

import java.io.File;

public class LoadImageTask extends AsyncTask<File, Void, Bitmap> {

    private final ModelOverviewFragmentController mController;

    private final File mImageFile;

    public LoadImageTask(ModelOverviewFragmentController controller, final File imageFile) {
        mController = controller;
        mImageFile = imageFile;
    }

    @Override
    protected Bitmap doInBackground(File... params) {
        return BitmapFactory.decodeFile(mImageFile.getAbsolutePath());
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        mController.onBitmapLoaded(mImageFile, bitmap);
    }
}

