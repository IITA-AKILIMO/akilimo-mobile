package com.iita.akilimo.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecOptionsAdapter;
import com.iita.akilimo.databinding.ActivityPlantingPracticesBinding;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.RealmProcessor;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;


import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

public class PlantingPracticesActivity extends BaseActivity {


    Toolbar toolbar;
    RecyclerView recyclerView;
    AppCompatButton btnGetRec;


    String recommendations;
    String plantingString;
    String marketOutletString;
    String rootYieldString;
    String tillageOperationsString;
    String manualTillageCostsString;
    String tractorAccessString;
    String weedControlCostString;

    ActivityPlantingPracticesBinding binding;


    private Activity activity;
    private RecOptionsAdapter mAdapter;
    private List<RecommendationOptions> items = new ArrayList<>();
    private RecAdvice recAdvice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlantingPracticesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        activity = this;
        realmProcessor = new RealmProcessor();

        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.recyclerView;
        btnGetRec = binding.singleButton.btnGetRecommendation;

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
        recommendations = getString(R.string.lbl_best_planting_practices);
        plantingString = getString(R.string.lbl_planting_harvest);
        marketOutletString = getString(R.string.lbl_market_outlet);
        rootYieldString = getString(R.string.lbl_typical_yield);
        tillageOperationsString = getString(R.string.lbl_tillage_operations);
        manualTillageCostsString = getString(R.string.lbl_cost_of_manual_tillage);
        tractorAccessString = getString(R.string.lbl_tractor_access);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        btnGetRec.setOnClickListener(view -> {
            //launch the recommendation view
            recAdvice = realmProcessor.getRecAdvice();
            try (Realm myRealm = getRealmInstance()) {
                myRealm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        if (recAdvice == null) {
                            recAdvice = realm.createObject(RecAdvice.class);
                        }
                        recAdvice.setFR(false);
                        recAdvice.setCIM(false);
                        recAdvice.setCIS(false);
                        recAdvice.setSPH(false);
                        recAdvice.setSPP(false);
                        recAdvice.setBPP(true);
                    }
                });
                processRecommendations(activity);
            } catch (Exception ex) {
                Crashlytics.logException(ex);
            }
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
