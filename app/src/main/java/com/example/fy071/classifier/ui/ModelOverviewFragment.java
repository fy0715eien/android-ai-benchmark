package com.example.fy071.classifier.ui;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fy071.classifier.Model;
import com.example.fy071.classifier.R;
import com.qualcomm.qti.snpe.NeuralNetwork;
import com.qualcomm.qti.snpe.SNPE;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import static com.example.fy071.classifier.util.LayerNameHelper.INPUT_LAYER;

public class ModelOverviewFragment extends Fragment {
    private static final String TAG = ModelOverviewFragment.class.getSimpleName();

    public static final String EXTRA_MODEL = "model";

    enum MenuRuntimeGroup {
        SelectCpuRuntime(NeuralNetwork.Runtime.CPU),
        SelectGpuRuntime(NeuralNetwork.Runtime.GPU),
        SelectDspRuntime(NeuralNetwork.Runtime.DSP);

        public static int ID = 1;

        public NeuralNetwork.Runtime runtime;

        MenuRuntimeGroup(NeuralNetwork.Runtime runtime) {
            this.runtime = runtime;
        }
    }

    private ProgressBar mProgressBar;

    private GridView mImageGrid;

    private ModelImagesAdapter mImageGridAdapter;

    private ModelOverviewFragmentController mController;

    private TextView mDimensionsText;

    private TextView mModelNameText;

    private TextView mClassificationText;

    private TextView mGroundTruthText;

    private TextView mTop1AccuracyText;

    private TextView mTop5AccuracyText;

    private FloatingActionButton mTestButton;

    public static ModelOverviewFragment create(final Model model) {
        final ModelOverviewFragment fragment = new ModelOverviewFragment();
        final Bundle arguments = new Bundle();
        arguments.putParcelable(EXTRA_MODEL, model);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_model, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageGrid = view.findViewById(R.id.model_image_grid);
        mImageGridAdapter = new ModelImagesAdapter(getActivity());
        mImageGrid.setAdapter(mImageGridAdapter);
        mImageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Bitmap bitmap = mImageGridAdapter.getItem(position);
                mController.classify(bitmap, position);
            }
        });

        mTestButton = view.findViewById(R.id.button_test);
        mTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.resetAccuracyCalculator();
                for (int position = 0; position < mImageGridAdapter.getCount(); position++) {
                    final Bitmap bitmap = mImageGridAdapter.getItem(position);
                    mController.classify(bitmap, position);
                }
            }
        });

        mModelNameText = view.findViewById(R.id.model_overview_name_text);
        mDimensionsText = view.findViewById(R.id.model_overview_dimensions_text);
        mClassificationText = view.findViewById(R.id.model_overview_classification_text);
        mGroundTruthText = view.findViewById(R.id.model_overview_ground_truth_text);
        mTop1AccuracyText = view.findViewById(R.id.model_overview_top1_accuracy_text);
        mTop5AccuracyText = view.findViewById(R.id.model_overview_top5_accuracy_text);
        mProgressBar = view.findViewById(R.id.progressBar);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        final Model model = getArguments().getParcelable(EXTRA_MODEL);
        mController = new ModelOverviewFragmentController(getActivity().getApplication(), model);
    }

    @Override
    public void onCreateOptionsMenu(android.view.Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final SNPE.NeuralNetworkBuilder builder = new SNPE.NeuralNetworkBuilder(getActivity().getApplication());
        for (MenuRuntimeGroup item : MenuRuntimeGroup.values()) {
            if (builder.isRuntimeSupported(item.runtime)) {
                menu.add(MenuRuntimeGroup.ID, item.ordinal(), 0, item.runtime.name());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == MenuRuntimeGroup.ID) {
            final MenuRuntimeGroup option = MenuRuntimeGroup.values()[item.getItemId()];
            mController.setTargetRuntime(option.runtime);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mController.attach(this);
    }

    @Override
    public void onStop() {
        mController.detach(this);
        super.onStop();
    }

    public void addSampleBitmap(Bitmap bitmap) {
        if (mImageGridAdapter.getPosition(bitmap) == -1) {
            mImageGridAdapter.add(bitmap);
            mImageGridAdapter.notifyDataSetChanged();
        }
    }

    public void setNetworkDimensions(Map<String, int[]> inputDimensions) {
        mDimensionsText.setText(Arrays.toString(inputDimensions.get(INPUT_LAYER)));
    }

    public void displayModelLoadFailed() {
        mClassificationText.setVisibility(View.VISIBLE);
        mClassificationText.setText(R.string.model_load_failed);
        Toast.makeText(getActivity(), R.string.model_load_failed, Toast.LENGTH_SHORT).show();
    }

    public void setModelName(String modelName) {
        mModelNameText.setText(modelName);
    }

    public void setClassificationResult(String[] classificationResult) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < classificationResult.length; i += 2) {
            result.append(String.format("%s: %s", classificationResult[i], classificationResult[i + 1])).append("\n");
        }
        mClassificationText.setText(result);
        mClassificationText.setVisibility(View.VISIBLE);
    }

    public void setLoadingVisible(boolean visible) {
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setTop1Accuracy(double accuracy) {
        mTop1AccuracyText.setText(String.format(new Locale("eng"), "%.3f", accuracy));
        mTop1AccuracyText.setVisibility(View.VISIBLE);
    }

    public void setTop5Accuracy(double accuracy) {
        mTop5AccuracyText.setText(String.format(new Locale("eng"), "%.3f", accuracy));
        mTop5AccuracyText.setVisibility(View.VISIBLE);
    }

    public void setGroundTruth(String label) {
        mGroundTruthText.setText(label);
        mGroundTruthText.setVisibility(View.VISIBLE);
    }

    public void displayModelNotLoaded() {
        Toast.makeText(getActivity(), R.string.model_not_loaded, Toast.LENGTH_SHORT).show();
    }

    public void displayClassificationFailed() {
        Toast.makeText(getActivity(), R.string.classification_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        mController.cancelAllClassifyTasks();
    }

    private static class ModelImagesAdapter extends ArrayAdapter<Bitmap> {

        public ModelImagesAdapter(Context context) {
            super(context, R.layout.model_image_layout);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.model_image_layout, parent, false);
            } else {
                view = convertView;
            }

            final ImageView imageView = ImageView.class.cast(view);
            imageView.setImageBitmap(getItem(position));
            return view;
        }
    }
}
