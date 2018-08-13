package com.example.fy071.classifier.ui.gallery;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fy071.classifier.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * A simple {@link Fragment} subclass.
 */
@RuntimePermissions
public class GalleryFragment extends Fragment {
    private static final String PHOTOS_DIRECTORY = "Photos";

    private static final String NEW_PHOTOS_DIRECTORY = "New";

    private static final int CAMERA_REQUEST = 1;

    static File photosRoot;

    static List<File> categoryDirectories;

    static List<File> imagePaths;

    @BindView(R.id.view_pager_gallery)
    ViewPager viewPager;

    @BindView(R.id.tab_layout_gallery)
    TabLayout tabLayout;

    private GalleryFragmentPagerAdapter galleryFragmentPagerAdapter;

    public GalleryFragment() {

    }

    @OnClick(R.id.button_camera)
    void startCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryFragmentPagerAdapter = new GalleryFragmentPagerAdapter(getFragmentManager());

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        viewPager.setAdapter(galleryFragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        photosRoot = getPhotosRoot();
        if (photosRoot != null) {
            load();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        GalleryFragmentPermissionsDispatcher.requestCameraPermissionWithPermissionCheck(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            if (photo != null) {
                Calendar calendar = Calendar.getInstance();
                String newPhotoName = calendar.getTime().toString();
                File newPhotoFile = new File(getNewPhotosDirectory(), newPhotoName + ".jpg");
                saveNewPhoto(newPhotoFile, photo);
            }
        }
    }

    /**
     * Load directories under ${EXTERNAL_FILES_DIR}/Photos/
     */
    private void loadDirectories() {
        categoryDirectories = new ArrayList<>();
        categoryDirectories.addAll(Arrays.asList(photosRoot.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        })));
    }

    private void loadImagePaths() {
        imagePaths = new ArrayList<>();
        for (File categoryDirectory : categoryDirectories) {
            imagePaths.addAll(Arrays.asList(categoryDirectory.listFiles()));
        }
    }

    private void load() {
        loadDirectories();
        loadImagePaths();
    }

    private File getPhotosRoot() {
        File photosRoot = new File(getContext().getExternalFilesDir(null), PHOTOS_DIRECTORY);
        if (!photosRoot.exists()) {
            if (photosRoot.mkdir()) {
                return photosRoot;
            } else {
                return null;
            }
        }
        return photosRoot;
    }

    private File getNewPhotosDirectory() {
        File newPhotosDirectory = new File(getPhotosRoot(), NEW_PHOTOS_DIRECTORY);
        if (!newPhotosDirectory.exists()) {
            if (newPhotosDirectory.mkdir()) {
                return newPhotosDirectory;
            } else {
                return null;
            }
        }
        return newPhotosDirectory;
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void requestCameraPermission() {

    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showCameraRationale(final PermissionRequest request) {
        new AlertDialog.Builder(getContext())
                .setTitle("Permission required")
                .setMessage("App needs permission to capture pictures")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GalleryFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void saveNewPhoto(File newPhotoFile, Bitmap bitmap) {
        try (FileOutputStream outputStream = new FileOutputStream(newPhotoFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    enum TabTitleEnum {
        TAB_PHOTOS,
        TAB_ALBUMS
    }

    class GalleryFragmentPagerAdapter extends FragmentPagerAdapter {
        private String[] tabTitles = {
                getResources().getString(R.string.photos),
                getResources().getString(R.string.albums)
        };

        GalleryFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == TabTitleEnum.TAB_PHOTOS.ordinal()) {
                return new PhotosFragment();
            } else if (position == TabTitleEnum.TAB_ALBUMS.ordinal()) {
                return new AlbumsFragment();
            } else {
                return null;
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }
    }
}
