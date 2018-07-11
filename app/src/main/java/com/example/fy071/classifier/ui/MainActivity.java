package com.example.fy071.classifier.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;

import com.example.fy071.classifier.Model;
import com.example.fy071.classifier.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.main_content, ModelCatalogueFragment.create());
            transaction.commit();
        }
    }

    public void displayModelOverview(final Model model) {
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, ModelOverviewFragment.create(model));
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
