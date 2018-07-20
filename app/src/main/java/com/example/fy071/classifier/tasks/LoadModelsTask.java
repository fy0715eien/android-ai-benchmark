package com.example.fy071.classifier.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.fy071.classifier.ui.benchmark.ModelCatalogueFragmentController;
import com.example.fy071.classifier.util.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * AsyncTask that loads all valid models in external file directory
 */
public class LoadModelsTask extends AsyncTask<Void, Void, Set<Model>> {
    private static final String TAG = LoadModelsTask.class.getSimpleName();

    private static final String GROUND_TRUTH_FILE_NAME = "ground truths.txt";
    private static final String MODEL_DLC_FILE_NAME = "model.dlc";
    private static final String LABELS_FILE_NAME = "labels.txt";
    private static final String LAYERS_FILE_NAME = "layers.txt";
    private static final String IMAGES_FOLDER_NAME = "images";
    private static final String JPG_EXT = ".jpg";

    private final ModelCatalogueFragmentController mController;

    private final Context mContext;

    public LoadModelsTask(Context context, ModelCatalogueFragmentController controller) {
        mContext = context.getApplicationContext();
        mController = controller;
    }

    @Override
    protected Set<Model> doInBackground(Void... params) {
        final Set<Model> result = new LinkedHashSet<>();
        final File modelsRoot = mContext.getExternalFilesDir("models");
        if (modelsRoot != null) {
            result.addAll(createModels(modelsRoot));
        }
        return result;
    }

    @Override
    protected void onPostExecute(Set<Model> models) {
        mController.onModelsLoaded(models);
    }

    private Set<Model> createModels(File modelsRoot) {
        final Set<Model> models = new LinkedHashSet<>();
        for (File child : modelsRoot.listFiles()) {
            if (!child.isDirectory()) {
                continue;
            }
            try {
                models.add(createModel(child));
            } catch (IOException e) {
                Log.e(TAG, "Failed to load model from model directory.", e);
            }
        }
        return models;
    }

    private Model createModel(File modelDir) throws IOException {
        final Model model = new Model();

        model.name = modelDir.getName();

        model.file = new File(modelDir, MODEL_DLC_FILE_NAME);

        final File images = new File(modelDir, IMAGES_FOLDER_NAME);
        if (images.isDirectory()) {
            File[] files = images.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().endsWith(JPG_EXT);
                }
            });
            ArrayList<File> fileArrayList = new ArrayList<>(Arrays.asList(files));
            fileArrayList.sort(new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            model.jpgImages = fileArrayList.toArray(new File[fileArrayList.size()]);
        }

        model.labels = loadLabels(new File(modelDir, LABELS_FILE_NAME));

        model.groundTruths = loadLabels(new File(modelDir, GROUND_TRUTH_FILE_NAME));

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
