package com.iita.akilimo.views.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.AdapterListAnimation;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.Recommendations;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumAdvice;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RecommendationsActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindString(R.string.lbl_recommendations)
    String recommendations;

    @BindString(R.string.lbl_fertilizer_recommendations)
    String frString;
    @BindString(R.string.lbl_intercropping)
    String icString;
    @BindString(R.string.lbl_scheduled_planting_and_harvest)
    String sphString;
    @BindString(R.string.lbl_best_planting_practices)
    String bppString;


    private AdapterListAnimation mAdapter;
    private List<Recommendations> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations_activity);
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

        toolbar.setNavigationOnClickListener(v -> {
            // back button pressed
            finish();
        });
    }


    @Override
    protected void initComponent() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //set data and list adapter
        mAdapter = new AdapterListAnimation(this);
        recyclerView.setAdapter(mAdapter);
        items = new ArrayList<>();

        Recommendations FR = new Recommendations();
        FR.setRecCode(EnumAdvice.FR);
        FR.setRecommendationName(frString);
        items.add(FR);

        Recommendations IC = new Recommendations();
        IC.setRecCode(EnumAdvice.IC);
        IC.setRecommendationName(icString);
        items.add(IC);

        Recommendations SPH = new Recommendations();
        SPH.setRecCode(EnumAdvice.SPH);
        SPH.setRecommendationName(sphString);
        items.add(SPH);

        Recommendations BPP = new Recommendations();
        BPP.setRecCode(EnumAdvice.BPP);
        BPP.setRecommendationName(bppString);
        items.add(BPP);

        setAdapter();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setAdapter() {
        mAdapter.setItems(items, ItemAnimation.FADE_IN);
        // on item list clicked
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            //let us process the data
            Intent intent = null;
            EnumAdvice advice = obj.getRecCode();
            switch (advice) {
                case FR:
                    intent = new Intent(this, FertilizerRecActivity.class);
                    break;
                case BPP:
                    intent = new Intent(this, PlantingPracticesActivity.class);
                    break;
                case IC:
                    intent = new Intent(this, InterCropRecActivity.class);
                    break;
                case SPH:
                    intent = new Intent(this, ScheduledPlantingActivity.class);
                    break;
                case WM:
                    break;
            }
            if (intent != null) {
                startActivity(intent);
                Animatoo.animateSlideRight(this);
            } else {
                Snackbar.make(view, "Item " + obj.getRecommendationName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });

    }
}
