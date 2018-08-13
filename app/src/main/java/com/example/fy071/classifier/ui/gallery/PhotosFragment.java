package com.example.fy071.classifier.ui.gallery;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.fy071.classifier.R;
import com.example.fy071.classifier.util.GlideApp;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotosFragment extends Fragment {
    @BindView(R.id.rv_photos)
    RecyclerView recyclerView;

    private LayoutInflater layoutInflater;

    private GridLayoutManager gridLayoutManager;

    public PhotosFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);

        layoutInflater = LayoutInflater.from(getActivity());

        gridLayoutManager = new GridLayoutManager(getActivity(), 4);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new ImageAdapter());

    }

    class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.PhotoViewHolder> {

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.item_gallery_image, parent, false);
            PhotoViewHolder viewHolder = new PhotoViewHolder(view);
            viewHolder.imageView = view.findViewById(R.id.iv_gallery_image);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.height = gridLayoutManager.getWidth() / gridLayoutManager.getSpanCount();

            GlideApp.with(getContext())
                    .asBitmap()
                    .load(GalleryFragment.imagePaths.get(position))
                    .centerCrop()
                    .error(R.drawable.ic_broken_image_black_24dp)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return GalleryFragment.imagePaths.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            PhotoViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
