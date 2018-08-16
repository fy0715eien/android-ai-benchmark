package com.example.fy071.classifier.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.fy071.classifier.R;
import com.example.fy071.classifier.ui.benchmark.ModelCatalogueFragment;
import com.example.fy071.classifier.ui.benchmark.ModelOverviewFragment;
import com.example.fy071.classifier.ui.gallery.GalleryActivity;
import com.example.fy071.classifier.util.Model;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener, Drawer.OnDrawerListener {
    private static final long DRAWER_BENCHMARK = 1L;
    private static final long DRAWER_GALLERY = 2L;
    private static final long DRAWER_ABOUT = 3L;
    Drawer drawer;
    private long previousSelectedItem;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitle("Benchmark");
        setSupportActionBar(toolbar);

        drawer = new DrawerBuilder(this)
                .withHeader(R.layout.layout_drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem()
                                .withIdentifier(DRAWER_BENCHMARK)
                                .withIcon(R.drawable.ic_timeline_black_24dp)
                                .withName("Benchmark"),
                        new PrimaryDrawerItem()
                                .withIdentifier(DRAWER_GALLERY)
                                .withIcon(R.drawable.ic_photo_library_black_24dp)
                                .withName("Gallery")
                )
                .addStickyDrawerItems(
                        new PrimaryDrawerItem()
                                .withIdentifier(DRAWER_ABOUT)
                                .withIcon(R.drawable.ic_info_outline_black_24dp)
                                .withName("About")
                                .withSelectable(false)
                )
                .withOnDrawerItemClickListener(this)
                .withOnDrawerListener(this)
                .withActionBarDrawerToggle(true)
                .withCloseOnClick(true)
                .build();

        drawer.setToolbar(this, toolbar, true);

        if (savedInstanceState == null) {
            displayModelCatalogue();
        }
    }

    public void displayModelOverview(final Model model) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, ModelOverviewFragment.create(model))
                .commit();
    }

    public void displayModelCatalogue() {
        toolbar.setTitle("Benchmark");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, ModelCatalogueFragment.create())
                .commit();
    }

    public void displayGallery() {
        startActivity(new Intent(MainActivity.this, GalleryActivity.class));
    }

    public void displayAboutActivity() {
        new LibsBuilder().withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityTitle("About")
                .start(this);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        previousSelectedItem = drawerItem.getIdentifier();
        return false;
    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {
        if (previousSelectedItem == DRAWER_BENCHMARK) {
            displayModelCatalogue();
        } else if (previousSelectedItem == DRAWER_GALLERY) {
            displayGallery();
        } else if (previousSelectedItem == DRAWER_ABOUT) {
            displayAboutActivity();
        }
        previousSelectedItem = 0L;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (previousSelectedItem == 0L) {
            super.onBackPressed();
        } else if (previousSelectedItem != DRAWER_BENCHMARK) {
            drawer.setSelection(DRAWER_BENCHMARK);
            displayModelCatalogue();
            previousSelectedItem = 0L;
        }
    }
}
