package com.example.fy071.classifier.ui.gallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.fy071.classifier.R;

public class FolderPhotosActivity extends AppCompatActivity {
    private static final String TAG = "FolderPhotosActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_photos);

        String directoryString = getIntent().getStringExtra(PhotosFragment.EXTRA_DIRECTORY);
        Log.d(TAG, "onCreate: " + directoryString);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, PhotosFragment.create(directoryString))
                .commit();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
