package com.example.fy071.classifier.ui;


import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;

import com.example.fy071.classifier.FileProvider;
import com.example.fy071.classifier.Model;
import com.example.fy071.classifier.tasks.LoadModelsTask;

import java.util.Set;

public class ModelCatalogueFragmentController extends AbstractViewController<ModelCatalogueFragment> {

    private final Context mContext;

    public ModelCatalogueFragmentController(Context context) {
        mContext = context;
    }

    @Override
    protected void onViewAttached(final ModelCatalogueFragment view) {
        view.setExtractingModelMessageVisible(true);

        final ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.registerContentObserver(
                Uri.withAppendedPath(FileProvider.getUri(), Model.INVALID_ID),
                false,
                mModelExtractionFailedObserver
        );

        contentResolver.registerContentObserver(FileProvider.getUri(), true, mModelExtractionObserver);

        startModelsExtraction();
        loadModels();
    }

    private void startModelsExtraction() {
        //ModelExtractionService.extractModel(mContext, "alexnet", R.raw.alexnet);
        //ModelExtractionService.extractModel(mContext, "inception_v3_quantized", R.raw.inception_v3_quantized);
        //ModelExtractionService.extractModel(mContext, "inception_v3", R.raw.inception_v3);

    }

    @Override
    protected void onViewDetached(final ModelCatalogueFragment view) {
        final ContentResolver contentResolver = mContext.getContentResolver();
        contentResolver.unregisterContentObserver(mModelExtractionObserver);
        contentResolver.unregisterContentObserver(mModelExtractionFailedObserver);
    }

    private final ContentObserver mModelExtractionObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (isAttached()) {
                loadModels();
            }
        }
    };

    private final ContentObserver mModelExtractionFailedObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (isAttached()) {
                getView().showExtractionFailedMessage();
            }
        }
    };

    private void loadModels() {
        final LoadModelsTask task = new LoadModelsTask(mContext, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onModelsLoaded(final Set<Model> models) {
        if (isAttached()) {
            getView().displayModels(models);
        }
    }
}
