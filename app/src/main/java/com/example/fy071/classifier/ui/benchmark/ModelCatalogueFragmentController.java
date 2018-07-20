package com.example.fy071.classifier.ui.benchmark;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.fy071.classifier.tasks.LoadModelsTask;
import com.example.fy071.classifier.ui.AbstractViewController;
import com.example.fy071.classifier.util.Model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ir.mahdi.mzip.zip.ZipArchive;

public class ModelCatalogueFragmentController extends AbstractViewController<ModelCatalogueFragment> {
    private static final String MODELS_ROOT_DIR = "models";

    private static final String TAG = ModelCatalogueFragment.class.getSimpleName();

    private final Context mContext;

    ModelCatalogueFragmentController(Context context) {
        mContext = context;
    }

    @Override
    protected void onViewAttached(final ModelCatalogueFragment view) {
        view.setExtractingProgressVisible(true);
    }

    public void startModelsExtraction() {
        List<File> files = new ArrayList<>();
        File root = Environment.getExternalStorageDirectory();
        files.add(new File(root, "alexnet.zip"));
        files.add(new File(root, "inception_v3.zip"));
        files.add(new File(root, "inception_v3_quantized.zip"));

        Observable.fromIterable(files)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<File>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(File file) {
                        extractSingle(file);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        loadModels();
                    }
                });
    }


    @Override
    protected void onViewDetached(final ModelCatalogueFragment view) {

    }

    private void loadModels() {
        final LoadModelsTask task = new LoadModelsTask(mContext, this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void onModelsLoaded(final Set<Model> models) {
        if (isAttached()) {
            getView().displayModels(models);
        }
    }

    private File getExternalModelsRootDirectory() throws IOException {
        final File modelsRoot = mContext.getExternalFilesDir(MODELS_ROOT_DIR);
        if (modelsRoot == null) {
            throw new IOException("Unable to access application external storage.");
        }

        if (!modelsRoot.isDirectory() && !modelsRoot.mkdir()) {
            throw new IOException("Unable to create model root directory: " + modelsRoot.getAbsolutePath());
        }
        return modelsRoot;
    }

    private File createModelDirectory(File directory) throws IOException {
        if (!directory.isDirectory() && !directory.mkdir()) {
            throw new IOException("Unable to create model root directory: " + directory.getAbsolutePath());
        }
        return directory;
    }

    private void extractSingle(File zipFile) {
        try {
            String targetPath = zipFile.getAbsolutePath();
            String zipFileWithoutExt = zipFile.getName().split("\\.")[0];
            File destFile = new File(getExternalModelsRootDirectory(), zipFileWithoutExt);
            if (destFile.exists()) {
                return;
            }
            String destinationPath = createModelDirectory(destFile).getAbsolutePath();
            ZipArchive.unzip(targetPath, destinationPath, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
