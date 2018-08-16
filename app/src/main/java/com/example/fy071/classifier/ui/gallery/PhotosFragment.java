package com.example.fy071.classifier.ui.gallery;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.fy071.classifier.R;
import com.example.fy071.classifier.util.GlideApp;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotosFragment extends Fragment {
    public static final String EXTRA_DIRECTORY = "dir";

    @BindView(R.id.rv_photos)
    RecyclerView recyclerView;
    private static final String TAG = "PhotosFragment";

    private LayoutInflater layoutInflater;

    private GridLayoutManager gridLayoutManager;
    private List<File> imagePaths;

    public PhotosFragment() {
    }

    public static Fragment create(String directory) {
        final PhotosFragment fragment = new PhotosFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_DIRECTORY, directory);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null) {
            String imageDirectory = getArguments().getString(EXTRA_DIRECTORY);
            if (imageDirectory != null) {
                imagePaths = Arrays.asList(new File(imageDirectory).listFiles());
            }
        } else {
            imagePaths = GalleryActivity.imagePaths;
        }
        Log.d(TAG, "onActivityCreated: " + imagePaths);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        layoutInflater = LayoutInflater.from(getActivity());

        gridLayoutManager = new GridLayoutManager(getActivity(), 4);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new ImageAdapter());
    }

    class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.PhotoViewHolder> {

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: ");
            View view = layoutInflater.inflate(R.layout.item_gallery_image, parent, false);
            PhotoViewHolder viewHolder = new PhotoViewHolder(view);
            viewHolder.imageView = view.findViewById(R.id.iv_gallery_image);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final PhotoViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: ");
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.height = gridLayoutManager.getWidth() / gridLayoutManager.getSpanCount();

            position = holder.getAdapterPosition();

            if (position != RecyclerView.NO_POSITION) {
                Log.d(TAG, "onBindViewHolder: position" + position);
                final File imagePath = imagePaths.get(position);
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_PHOTO, imagePath.getAbsolutePath());
                        startActivity(intent);
                    }
                });
                GlideApp.with(getContext())
                        .asBitmap()
                        .load(imagePath)
                        .centerCrop()
                        .error(R.drawable.ic_broken_image_black_24dp)
                        .into(holder.imageView);
            }
        }

        @Override
        public int getItemCount() {
            return imagePaths.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            PhotoViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
