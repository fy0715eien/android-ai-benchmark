package com.example.fy071.classifier.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fy071.classifier.Model;
import com.example.fy071.classifier.R;

import java.util.Set;

public class ModelCatalogueFragment extends Fragment {

    private ModelCatalogueFragmentController mController;

    private ListView mModelsList;

    private ProgressBar mLoadStatusProgressBar;

    public static ModelCatalogueFragment create() {
        return new ModelCatalogueFragment();
    }

    private ModelsAdapter mModelsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.models_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mModelsList = view.findViewById(R.id.models_list);
        mLoadStatusProgressBar = view.findViewById(R.id.models_load_status);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mController = new ModelCatalogueFragmentController(getActivity());

        mModelsAdapter = new ModelsAdapter(getActivity());
        mModelsList.setAdapter(mModelsAdapter);
        mModelsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.class.cast(getActivity()).displayModelOverview(mModelsAdapter.getItem(position));
            }
        });
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

    public void setExtractingModelMessageVisible(final boolean isVisible) {
        mLoadStatusProgressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void displayModels(Set<Model> models) {
        setExtractingModelMessageVisible(models.isEmpty());
        mModelsAdapter.clear();
        mModelsAdapter.addAll(models);
        mModelsAdapter.notifyDataSetChanged();
    }

    public void showExtractionFailedMessage() {
        Toast.makeText(getActivity(), R.string.model_extraction_failed, Toast.LENGTH_SHORT).show();
    }

    private static final class ModelsAdapter extends ArrayAdapter<Model> {
        public ModelsAdapter(Context context) {
            super(context, R.layout.models_list_item, R.id.model_name);
        }
    }
}
