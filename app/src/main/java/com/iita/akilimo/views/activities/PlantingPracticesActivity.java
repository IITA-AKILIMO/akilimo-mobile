package com.iita.akilimo.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecOptionsAdapter;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PlantingPracticesActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btnGetRecommendation)
    AppCompatButton btnGetRec;

    @BindString(R.string.lbl_best_planting_practices)
    String recommendations;
    @BindString(R.string.lbl_planting_harvest)
    String plantingString;
    @BindString(R.string.lbl_market_outlet)
    String marketOutletString;
    @BindString(R.string.lbl_typical_yield)
    String rootYieldString;

    @BindString(R.string.lbl_tillage_operations)
    String tillageOperationsString;

    @BindString(R.string.lbl_cost_of_manual_tillage)
    String manualTillageCostsString;
    @BindString(R.string.lbl_tractor_access)
    String tractorAccessString;

    @BindString(R.string.lbl_cost_of_weed_control)
    String weedControlCostString;


    private Activity activity;
    private RecOptionsAdapter mAdapter;
    private List<RecommendationOptions> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planting_practices);
        ButterKnife.bind(this);
        context = this;
        activity = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);

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

        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        btnGetRec.setOnClickListener(view -> {
            //launch the recommendation view
            RecAdvice recAdvice = objectBoxEntityProcessor.getRecAdvice();
            if (recAdvice == null) {
                recAdvice = new RecAdvice();
            }
            recAdvice.setFR(false);
            recAdvice.setCIM(false);
            recAdvice.setCIS(false);
            recAdvice.setSPH(false);
            recAdvice.setSPP(false);
            recAdvice.setBPP(true);

            objectBoxEntityProcessor.saveRecAdvice(recAdvice);
            processRecommendations(activity);
        });

        setAdapter();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    private void setAdapter() {
        //set data and list adapter
        items = new ArrayList<>();

//        items.add(new RecommendationOptions(plantingString, EnumAdviceTasks.PLANTING_AND_HARVEST, 0));
        items.add(new RecommendationOptions(manualTillageCostsString, EnumAdviceTasks.MANUAL_TILLAGE_COST, 0));
        items.add(new RecommendationOptions(tractorAccessString, EnumAdviceTasks.TRACTOR_ACCESS, 0));
        items.add(new RecommendationOptions(weedControlCostString, EnumAdviceTasks.COST_OF_WEED_CONTROL, 0));
        items.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, 0));
        items.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, 0));
        mAdapter = new RecOptionsAdapter(this, items, ItemAnimation.FADE_IN);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            //let us process the data
            Intent intent = null;
            EnumAdviceTasks advice = obj.getRecCode();
            switch (advice) {
                case PLANTING_AND_HARVEST:
                    intent = new Intent(context, DatesActivity.class);
                    break;
                case MARKET_OUTLET_CASSAVA:
                    intent = new Intent(context, CassavaMarketActivity.class);
                    break;
                case CURRENT_CASSAVA_YIELD:
                    intent = new Intent(context, RootYieldActivity.class);
                    break;
                case MANUAL_TILLAGE_COST:
                    intent = new Intent(context, ManualTillageCostActivity.class);
                    break;
                case TRACTOR_ACCESS:
                    intent = new Intent(context, TractorAccessActivity.class);
                    break;
                case COST_OF_WEED_CONTROL:
                    intent = new Intent(context, WeedControlCostsActivity.class);
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
