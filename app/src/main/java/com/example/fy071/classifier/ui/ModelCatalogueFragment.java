package com.example.fy071.classifier.ui;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.example.fy071.classifier.R;
import com.example.fy071.classifier.util.Model;

import java.util.Set;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
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
    public void onResume() {
        super.onResume();
        ModelCatalogueFragmentPermissionsDispatcher.handlePermissionsWithPermissionCheck(this);
    }

    @Override
    public void onStop() {
        mController.detach(this);
        super.onStop();
    }

    public void setExtractingProgressVisible(final boolean isVisible) {
        mLoadStatusProgressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void displayModels(Set<Model> models) {
        setExtractingProgressVisible(models.isEmpty());
        mModelsAdapter.clear();
        mModelsAdapter.addAll(models);
        mModelsAdapter.notifyDataSetChanged();
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void handlePermissions() {
        mController.extractAndLoad();
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void showRationale(final PermissionRequest permissionRequest) {
        new AlertDialog.Builder(getContext())
                .setTitle("Permission required")
                .setMessage("App needs permission to load models")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionRequest.proceed();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionRequest.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ModelCatalogueFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private static final class ModelsAdapter extends ArrayAdapter<Model> {
        ModelsAdapter(Context context) {
            super(context, R.layout.models_list_item, R.id.model_name);
        }
    }
}
