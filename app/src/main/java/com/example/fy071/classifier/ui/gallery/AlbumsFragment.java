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
import android.widget.TextView;

import com.example.fy071.classifier.R;
import com.example.fy071.classifier.util.GlideApp;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumsFragment extends Fragment {
    @BindView(R.id.rv_albums)
    RecyclerView recyclerView;

    private GridLayoutManager gridLayoutManager;

    private LayoutInflater layoutInflater;

    public AlbumsFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albums, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        layoutInflater = LayoutInflater.from(getActivity());

        gridLayoutManager = new GridLayoutManager(getContext(), 2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(new AlbumAdapter());
    }

    class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
        @NonNull
        @Override
        public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = layoutInflater.inflate(R.layout.item_gallery_folder, parent, false);
            AlbumViewHolder viewHolder = new AlbumViewHolder(view);
            viewHolder.imageView = view.findViewById(R.id.iv_album);
            viewHolder.textView = view.findViewById(R.id.tv_album);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.height = gridLayoutManager.getWidth() / gridLayoutManager.getSpanCount();
            params.width = params.height;

            File directory = GalleryActivity.categoryDirectories.get(position);

            GlideApp.with(getContext())
                    .asBitmap()
                    .load(directory.listFiles()[0])
                    .centerCrop()
                    .error(R.drawable.ic_broken_image_black_24dp)
                    .into(holder.imageView);

            holder.textView.setText(directory.getName());
        }

        @Override
        public int getItemCount() {
            return GalleryActivity.categoryDirectories.size();
        }

        class AlbumViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;

            AlbumViewHolder(View itemView) {
                super(itemView);
            }
        }
    }

}
