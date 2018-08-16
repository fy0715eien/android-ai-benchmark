package com.example.fy071.classifier.ui.gallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fy071.classifier.R;
import com.example.fy071.classifier.util.GlideApp;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_PHOTO = "DetailActivity.PHOTO";

    @BindView(R.id.iv_detail)
    ImageView imageView;

    @BindView(R.id.tv_detail)
    TextView textView;

    String photoPath;

    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        photoPath = getIntent().getStringExtra(EXTRA_PHOTO);

        String parentPath = new File(photoPath).getParent();

        category = new File(parentPath).getName();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GlideApp.with(this)
                .asBitmap()
                .load(photoPath)
                .error(R.drawable.ic_broken_image_black_24dp)
                .into(imageView);
        textView.setText(category);
    }
}
