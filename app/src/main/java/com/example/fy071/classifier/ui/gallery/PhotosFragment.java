package com.example.fy071.classifier.ui.gallery;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.fy071.classifier.R;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.example.fy071.classifier.ui.gallery.GalleryFragment.imagePaths;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotosFragment extends Fragment {
    @BindView(R.id.grid_view_photos)
    GridView photosGridView;

    private CompositeDisposable compositeDisposable;

    private ImagesAdapter imagesAdapter;

    public PhotosFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        super.onViewCreated(view, savedInstanceState);

        imagesAdapter = new ImagesAdapter(getContext());
        photosGridView.setAdapter(imagesAdapter);
        photosGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        loadImages();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        compositeDisposable.dispose();
    }

    private void loadImages() {
        imagesAdapter.clear();
        Disposable disposable = Observable
                .fromIterable(imagePaths)
                .map(new Function<File, Bitmap>() {
                    @Override
                    public Bitmap apply(File file) {
                        return BitmapFactory.decodeFile(file.getAbsolutePath());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) {
                        addBitmap(bitmap);
                    }
                });
        compositeDisposable.add(disposable);
    }

    private void addBitmap(Bitmap bitmap) {
        imagesAdapter.add(bitmap);
        imagesAdapter.notifyDataSetChanged();
    }

    private static class ImagesAdapter extends ArrayAdapter<Bitmap> {
        ImagesAdapter(Context context) {
            super(context, R.layout.item_image);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
            } else {
                view = convertView;
            }

            final ImageView imageView = ImageView.class.cast(view);
            imageView.setImageBitmap(getItem(position));
            return view;
        }
    }
}
