package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.views.fragments.ComputeRecommendationsFragment;
import com.iita.akilimo.views.fragments.DstOptionsFragment;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DstOptionsActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.navigation)
    BottomNavigationView navigation;

    @BindString(R.string.lbl_recommendations)
    String recommendations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dst_options);

        ButterKnife.bind(this);

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(recommendations);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Tools.setSystemBarColor(this, R.color.blue_900);

        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        loadFragment(DstOptionsFragment.newInstance());
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.dst_options:
                    toolbar.setTitle("Options");
                    loadFragment(DstOptionsFragment.newInstance());
                    return true;
                case R.id.get_recommendations:
                    toolbar.setTitle("Compute recommendations");
                    loadFragment(ComputeRecommendationsFragment.newInstance());
                    return true;
            }
            return false;
        }
    };
}
