package com.example.fy071.classifier.listener;

import android.os.FileObserver;
import android.support.annotation.Nullable;

public class NewPhotoListener extends FileObserver {
    public NewPhotoListener(String path) {
        super(path);
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        switch (event) {
            case FileObserver.CREATE:

        }
    }
}
