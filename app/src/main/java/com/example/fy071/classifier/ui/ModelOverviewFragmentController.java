package com.example.fy071.classifier.ui;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.example.fy071.classifier.Model;
import com.example.fy071.classifier.tasks.ClassifyImageTask;
import com.example.fy071.classifier.tasks.LoadNetworkTask;
import com.example.fy071.classifier.util.AccuracyCalculator;
import com.qualcomm.qti.snpe.NeuralNetwork;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ModelOverviewFragmentController extends AbstractViewController<ModelOverviewFragment> {
    private static final String TAG = ModelOverviewFragmentController.class.getSimpleName();

    private final Model mModel;

    private final Application mApplication;

    private NeuralNetwork mNeuralNetwork;

    private LoadNetworkTask mLoadTask;

    private AccuracyCalculator top1AccuracyCalculator = new AccuracyCalculator();

    private AccuracyCalculator top5AccuracyCalculator = new AccuracyCalculator();

    private List<ClassifyImageTask> taskList = new LinkedList<>();

    public ModelOverviewFragmentController(final Application application, Model model) {
        mApplication = application;
        mModel = model;
    }

    @Override
    protected void onViewAttached(ModelOverviewFragment view) {
        view.setLoadingVisible(true);
        view.setModelName(mModel.name);
        loadImageSamples(view);
        loadNetwork(NeuralNetwork.Runtime.CPU);
    }

    private void loadImageSamples(ModelOverviewFragment view) {
        for (int i = 0; i < mModel.jpgImages.length; i++) {
            final File imagePaths = mModel.jpgImages[i];
            Log.i(TAG, "loadImageSamples: " + imagePaths);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePaths.getAbsolutePath());
            onBitmapLoaded(imagePaths, bitmap);
        }
    }

    @Override
    protected void onViewDetached(ModelOverviewFragment view) {
        if (mNeuralNetwork != null) {
            mNeuralNetwork.release();
            mNeuralNetwork = null;
        }
    }

    public void onBitmapLoaded(File imageFile, Bitmap bitmap) {
        if (isAttached()) {
            getView().addSampleBitmap(bitmap);
        }
    }

    public void onNetworkLoaded(NeuralNetwork neuralNetwork) {
        if (isAttached()) {
            mNeuralNetwork = neuralNetwork;
            ModelOverviewFragment view = getView();
            view.setNetworkDimensions(neuralNetwork.getInputTensorsShapes());
            view.setModelVersion(neuralNetwork.getModelVersion());
            view.setLoadingVisible(false);
        } else {
            neuralNetwork.release();
        }
        mLoadTask = null;
    }

    public void onNetworkLoadFailed() {
        if (isAttached()) {
            ModelOverviewFragment view = getView();
            view.displayModelLoadFailed();
        }
        mLoadTask = null;
    }

    public void classify(final Bitmap bitmap, int position) {
        final NeuralNetwork neuralNetwork = mNeuralNetwork;
        if (neuralNetwork != null) {
            final ClassifyImageTask task = new ClassifyImageTask(this, mNeuralNetwork, bitmap, mModel, position);
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
            taskList.add(task);
        } else {
            getView().displayModelNotLoaded();
        }
    }

    public void onClassificationResult(String[] labels) {
        if (isAttached()) {
            getView().setClassificationResult(labels);
        }
    }

    public void onClassificationFailed() {
        if (isAttached()) {
            getView().displayClassificationFailed();
        }
    }

    public void onShowExpectedLabel(String label) {
        if (isAttached()) {
            getView().setExpectedLabel(label);
        }
    }

    public void onUpdateTop1Accuracy(Boolean result) {
        if (isAttached()) {
            top1AccuracyCalculator.addResult(result);
            getView().setTop1Accuracy(top1AccuracyCalculator.getAccuracy());
        }
    }

    public void onUpdateTop5Accuracy(Boolean result) {
        if (isAttached()) {
            top5AccuracyCalculator.addResult(result);
            getView().setTop5Accuracy(top5AccuracyCalculator.getAccuracy());
        }
    }

    public void setTargetRuntime(NeuralNetwork.Runtime targetRuntime) {
        if (isAttached()) {
            ModelOverviewFragment view = getView();
            view.setLoadingVisible(true);
            loadNetwork(targetRuntime);
        }
    }

    private void loadNetwork(NeuralNetwork.Runtime targetRuntime) {
        final NeuralNetwork neuralNetwork = mNeuralNetwork;
        if (neuralNetwork != null) {
            neuralNetwork.release();
            mNeuralNetwork = null;
        }

        if (mLoadTask != null) {
            mLoadTask.cancel(false);
        }

        mLoadTask = new LoadNetworkTask(mApplication, this, mModel, targetRuntime);
        mLoadTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void resetAccuracyCalculator() {
        top1AccuracyCalculator = new AccuracyCalculator();
        top5AccuracyCalculator = new AccuracyCalculator();
    }

    public void cancelAllClassifyTasks() {
        for (ClassifyImageTask task : taskList) {
            task.cancel(true);
        }
    }
}