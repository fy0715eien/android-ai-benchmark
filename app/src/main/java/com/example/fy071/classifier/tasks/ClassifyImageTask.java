package com.example.fy071.classifier.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.example.fy071.classifier.ui.benchmark.ModelOverviewFragmentController;
import com.example.fy071.classifier.util.MeanImage;
import com.example.fy071.classifier.util.Model;
import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClassifyImageTask extends AsyncTask<Bitmap, Void, String[]> {
    private static final String TAG = ClassifyImageTask.class.getSimpleName();

    private static final int TOP_K = 5;

    private final NeuralNetwork mNeuralNetwork;

    private final Model mModel;

    private final Bitmap mImage;

    private final ModelOverviewFragmentController mController;

    private final int mPosition;

    public ClassifyImageTask(ModelOverviewFragmentController controller, NeuralNetwork network, Bitmap image, Model model, int position) {
        mController = controller;
        mNeuralNetwork = network;
        mImage = image;
        mModel = model;
        mPosition = position;

    }

    @Override
    protected String[] doInBackground(Bitmap... params) {
        final long start = System.currentTimeMillis();

        final List<String> result = new LinkedList<>();

        final FloatTensor tensor = mNeuralNetwork.createFloatTensor(mNeuralNetwork.getInputTensorsShapes().get(mModel.inputLayer));

        final int[] dimensions = tensor.getShape();

        final boolean isGrayScale = (dimensions[dimensions.length - 1] == 1);
        if (!isGrayScale) {
            writeRgbBitmapAsFloat(mImage, tensor);
        } else {
            writeGrayScaleBitmapAsFloat(mImage, tensor);
        }

        final Map<String, FloatTensor> inputs = new HashMap<>();
        inputs.put(mModel.inputLayer, tensor);

        final Map<String, FloatTensor> outputs = mNeuralNetwork.execute(inputs);
        for (Map.Entry<String, FloatTensor> output : outputs.entrySet()) {
            if (output.getKey().equals(mModel.outputLayer)) {
                for (Pair<Integer, Float> pair : topK(TOP_K, output.getValue())) {
                    result.add(mModel.labels[pair.first]);
                    result.add(String.valueOf(pair.second));
                }
            }
        }

        final long end = System.currentTimeMillis();
        long classifyTime = end - start;
        Log.i(TAG, "doInBackground: " + mNeuralNetwork.getRuntime() + ": " + classifyTime);

        return result.toArray(new String[result.size()]);
    }

    @Override
    protected void onPostExecute(String[] labels) {
        super.onPostExecute(labels);
        if (labels.length > 0) {
            mController.onClassificationResult(labels);

            String groundTruth = getGroundTruth(mPosition);

            mController.onShowGroundTruth(groundTruth);

            boolean top1Result = labels[0].equals(groundTruth);
            mController.onUpdateTop1Accuracy(top1Result);

            boolean top5Result = false;
            for (int i = 0; i < labels.length; i++) {
                if (i % 2 == 0) {
                    if (labels[i].equals(groundTruth)) {
                        top5Result = true;
                    }
                }
            }
            mController.onUpdateTop5Accuracy(top5Result);
        } else {
            mController.onClassificationFailed();
        }
    }

    private void writeRgbBitmapAsFloat(Bitmap image, FloatTensor tensor) {

        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int rgb = pixels[y * image.getWidth() + x];

                float[] pixelFloats = parseImage(rgb, mModel.isMeanImage);

                tensor.write(pixelFloats, 0, pixelFloats.length, y, x);
            }
        }
    }

    private void writeGrayScaleBitmapAsFloat(Bitmap image, FloatTensor tensor) {
        int imageMean = 128;
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int rgb = pixels[y * image.getWidth() + x];
                final float b = ((rgb) & 0xFF);
                final float g = ((rgb >> 8) & 0xFF);
                final float r = ((rgb >> 16) & 0xFF);
                float grayscale = (float) (r * 0.3 + g * 0.59 + b * 0.11);
                grayscale -= imageMean;
                tensor.write(grayscale, y, x);
            }
        }
    }

    private Pair<Integer, Float>[] topK(int k, FloatTensor tensor) {
        final float[] array = new float[tensor.getSize()];
        tensor.read(array, 0, array.length);

        final boolean[] selected = new boolean[tensor.getSize()];
        final Pair<Integer, Float>[] topK = new Pair[k];
        int count = 0;
        while (count < k) {
            final int index = top(array, selected);
            selected[index] = true;
            topK[count] = new Pair<>(index, array[index]);
            count++;
        }
        return topK;
    }

    private int top(float[] array, boolean[] selected) {
        int index = 0;
        float max = -1.f;
        for (int i = 0; i < array.length; i++) {
            if (selected[i]) {
                continue;
            }
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    private String getGroundTruth(int position) {
        int labelPosition = Integer.valueOf(mModel.groundTruths[position]);
        return mModel.labels[labelPosition];
    }

    private float[] parseImage(int rgb, boolean isMean) {
        if (isMean) {
            float b = (((rgb) & 0xFF) - MeanImage.MEAN);
            float g = (((rgb >> 8) & 0xFF) - MeanImage.MEAN);
            float r = (((rgb >> 16) & 0xFF) - MeanImage.MEAN);
            return new float[]{b, g, r};
        } else {
            float imageStd = 128.0f;
            float b = (((rgb) & 0xFF) - MeanImage.MEAN_B) / imageStd;
            float g = (((rgb >> 8) & 0xFF) - MeanImage.MEAN_G) / imageStd;
            float r = (((rgb >> 16) & 0xFF) - MeanImage.MEAN_R) / imageStd;
            return new float[]{b, g, r};
        }
    }

}