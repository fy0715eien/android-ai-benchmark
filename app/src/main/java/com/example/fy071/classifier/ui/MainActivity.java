package com.example.fy071.classifier.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.fy071.classifier.R;
import com.example.fy071.classifier.util.Model;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_content, ModelCatalogueFragment.create())
                    .commit();
        }
    }

    public void displayModelOverview(final Model model) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, ModelOverviewFragment.create(model))
                .addToBackStack(null)
                .commit();
    }
}
