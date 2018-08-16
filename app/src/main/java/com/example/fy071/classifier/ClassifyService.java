package com.example.fy071.classifier;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import com.example.fy071.classifier.util.MeanImage;
import com.example.fy071.classifier.util.Model;
import com.qualcomm.qti.snpe.FloatTensor;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ClassifyService extends Service {
    private static final String TAG = "ClassifyService";

    private NeuralNetwork neuralNetwork;

    private Model model;

    public ClassifyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new LoadModelTask(getApplication(), getExternalFilesDir("models"), this).execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static class LoadModelTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<ClassifyService> classifyServiceWeakReference;

        private static final String TAG = "LoadModelTask";
        private static final String GROUND_TRUTH_FILE_NAME = "ground truths.txt";
        private static final String MODEL_DLC_FILE_NAME = "model.dlc";
        private static final String LABELS_FILE_NAME = "labels.txt";
        private static final String LAYERS_FILE_NAME = "layers.txt";

        private NeuralNetwork innerNeuralNetwork;

        private Model innerModel;

        private File modelDir;

        private final Application application;

        LoadModelTask(Application application, File file, ClassifyService context) {
            this.application = application;
            modelDir = new File(file, "inception_v3");
            classifyServiceWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (modelDir != null) {
                    innerModel = createModel();
                    innerNeuralNetwork = createNetwork();
                }
            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            classifyServiceWeakReference.get().model = innerModel;
            classifyServiceWeakReference.get().neuralNetwork = innerNeuralNetwork;
            try {
                File file = new File(classifyServiceWeakReference.get().getExternalFilesDir("Photos"), "New");
                if (!file.exists() && !file.mkdir()) {
                    throw new IOException("Can not find new photo's directory");
                }
                if (file.list().length > 0) {
                    Log.d(TAG, "onPostExecute: start ClassifyImageTask");
                    new ClassifyImageTask(classifyServiceWeakReference).execute(file.listFiles());
                }
            } catch (IOException e) {
                Log.e(TAG, "onPostExecute: ", e);
            }

        }

        private NeuralNetwork createNetwork() {
            NeuralNetwork network = null;
            try {
                final SNPE.NeuralNetworkBuilder builder = new SNPE.NeuralNetworkBuilder(application)
                        .setDebugEnabled(false)
                        .setRuntimeOrder(NeuralNetwork.Runtime.CPU)
                        .setModel(innerModel.file);
                network = builder.build();
            } catch (IllegalStateException | IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            Log.d(TAG, "createNetwork: network loaded");
            return network;
        }

        private Model createModel() throws IOException {
            final Model model = new Model();

            Log.d(TAG, "createModel: name=" + model.name);
            model.name = modelDir.getName();

            model.file = new File(modelDir, MODEL_DLC_FILE_NAME);

            model.jpgImages = null;

            model.labels = loadLabels(new File(modelDir, LABELS_FILE_NAME));
            Log.d(TAG, "createModel: labels length=" + model.labels.length);

            model.groundTruths = loadLabels(new File(modelDir, GROUND_TRUTH_FILE_NAME));
            Log.d(TAG, "createModel: ground truths length=" + model.groundTruths.length);

            String[] layers = loadLayers(new File(modelDir, LAYERS_FILE_NAME));

            model.inputLayer = layers[0];

            model.outputLayer = layers[1];

            model.mean = layers[2];

            model.isMeanImage = model.mean.equals("mean");

            return model;
        }

        private String[] loadLabels(File labelsFile) throws IOException {
            final List<String> list = new LinkedList<>();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(labelsFile)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }
            return list.toArray(new String[list.size()]);
        }

        private String[] loadLayers(File layersFile) throws IOException {
            final List<String> list = new ArrayList<>();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(layersFile)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                list.add(line);
            }

            if (list.size() != 3) {
                throw new IOException();
            } else {
                return list.toArray(new String[list.size()]);
            }
        }
    }

    private static class ClassifyImageTask extends AsyncTask<File, Void, Void> {
        private WeakReference<ClassifyService> classifyServiceWeakReference;

        private Model innerModel;

        private NeuralNetwork innerNeuralNetwork;

        ClassifyImageTask(WeakReference<ClassifyService> classifyServiceWeakReference) {
            this.classifyServiceWeakReference = classifyServiceWeakReference;
            innerNeuralNetwork = classifyServiceWeakReference.get().neuralNetwork;
            innerModel = classifyServiceWeakReference.get().model;
        }

        @Override
        protected Void doInBackground(File... files) {
            String category;

            for (File file : files) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                Log.d(TAG, "doInBackground: start classifying");
                category = classify(bitmap);
                Log.d(TAG, "doInBackground: category" + category);

                Log.d(TAG, "doInBackground: start MoveImageTask");
                new MoveImageTask(classifyServiceWeakReference, file, category).executeOnExecutor(THREAD_POOL_EXECUTOR);
            }
            return null;
        }

        private String classify(Bitmap bitmap) {
            final FloatTensor tensor = innerNeuralNetwork.createFloatTensor(
                    innerNeuralNetwork
                            .getInputTensorsShapes()
                            .get(innerModel.inputLayer)
            );

            final int[] dimensions = tensor.getShape();

            final boolean isGrayScale = (dimensions[dimensions.length - 1] == 1);
            if (!isGrayScale) {
                writeRgbBitmapAsFloat(bitmap, tensor);
            } else {
                writeGrayScaleBitmapAsFloat(bitmap, tensor);
            }

            final Map<String, FloatTensor> inputs = new HashMap<>();
            inputs.put(innerModel.inputLayer, tensor);

            final Map<String, FloatTensor> outputs = innerNeuralNetwork.execute(inputs);

            String label = "not initialized";

            for (Map.Entry<String, FloatTensor> output : outputs.entrySet()) {
                if (output.getKey().equals(innerModel.outputLayer)) {
                    for (Pair<Integer, Float> pair : topK(1, output.getValue())) {
                        label = innerModel.labels[pair.first];
                    }
                }
            }

            return label;
        }

        private void writeRgbBitmapAsFloat(Bitmap image, FloatTensor tensor) {

            final int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getPixels(pixels, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    final int rgb = pixels[y * image.getWidth() + x];

                    float[] pixelFloats = parseImage(rgb, innerModel.isMeanImage);

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
                    float grayScale = (float) (r * 0.3 + g * 0.59 + b * 0.11);
                    grayScale -= imageMean;
                    tensor.write(grayScale, y, x);
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
            int labelPosition = Integer.valueOf(innerModel.groundTruths[position]);
            return innerModel.labels[labelPosition];
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

    private static class MoveImageTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "MoveImageTask";

        private WeakReference<ClassifyService> classifyServiceWeakReference;

        private File imagePath;

        private String category;

        MoveImageTask(WeakReference<ClassifyService> classifyServiceWeakReference, File imagePath, String category) {
            this.classifyServiceWeakReference = classifyServiceWeakReference;
            this.imagePath = imagePath;
            this.category = category;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            moveImageToDirectory();
            return null;
        }

        private void moveImageToDirectory() {
            try {
                File photosDir = classifyServiceWeakReference.get().getExternalFilesDir("Photos");

                Path fromPath = Paths.get(imagePath.getAbsolutePath());
                String fileName = imagePath.getName();

                File categoryDir = new File(photosDir, category);
                if (!categoryDir.exists() && !categoryDir.mkdir()) {
                    throw new IOException("Can not find category directory");
                }

                Path toPath = Paths.get(new File(categoryDir, fileName).getAbsolutePath());


                Files.move(fromPath, toPath);
            } catch (Exception e) {
                Log.e(TAG, "moveImageToDirectory: ", e);
            }
        }
    }
}