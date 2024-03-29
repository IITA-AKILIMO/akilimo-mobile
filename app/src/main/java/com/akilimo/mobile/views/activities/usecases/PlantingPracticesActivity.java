package com.akilimo.mobile.views.activities.usecases;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.snackbar.Snackbar;
import com.akilimo.mobile.R;
import com.akilimo.mobile.adapters.RecOptionsAdapter;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityPlantingPracticesBinding;
import com.akilimo.mobile.entities.UseCases;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.models.RecommendationOptions;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.akilimo.mobile.utils.enums.EnumUseCase;
import com.akilimo.mobile.views.activities.CassavaMarketActivity;
import com.akilimo.mobile.views.activities.DatesActivity;
import com.akilimo.mobile.views.activities.ManualTillageCostActivity;
import com.akilimo.mobile.views.activities.RootYieldActivity;
import com.akilimo.mobile.views.activities.TractorAccessActivity;
import com.akilimo.mobile.views.activities.WeedControlCostsActivity;

import java.util.ArrayList;
import java.util.List;

import io.sentry.Sentry;

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
    private UseCases useCases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlantingPracticesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        activity = this;
        database = AppDatabase.getDatabase(context);


        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.recyclerView;
        btnGetRec = binding.singleButton.btnGetRecommendation;

        useCases = database.useCaseDao().findOne();
        mAdapter = new RecOptionsAdapter();
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
        weedControlCostString = getString(R.string.lbl_cost_of_weed_control);
        rootYieldString = getString(R.string.lbl_typical_yield);
        tillageOperationsString = getString(R.string.lbl_tillage_operations);
        manualTillageCostsString = getString(R.string.lbl_cost_of_manual_tillage);
        tractorAccessString = getString(R.string.lbl_tractor_access);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        btnGetRec.setOnClickListener(view -> {
            try {
                if (useCases == null) {
                    useCases = new UseCases();
                }
                useCases.setFR(false);
                useCases.setCIM(false);
                useCases.setCIS(false);
                useCases.setSPH(false);
                useCases.setSPP(false);
                useCases.setBPP(true);
                useCases.setName(EnumUseCase.PP.name());

                database.useCaseDao().insert(useCases);
                processRecommendations(activity);
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Sentry.captureException(ex);
            }
        });


        mAdapter.setOnItemClickListener((view, obj, position) -> {
            //let us process the data
            Intent intent = null;
            EnumAdviceTasks advice = obj.getAdviceName();
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
                openActivity();
            } else {
                Snackbar.make(view, "Item " + obj.getRecName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });

        setAdapter();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter() {
        items = getRecItems();
        mAdapter.setData(items);
    }

    private List<RecommendationOptions> getRecItems() {
        List<RecommendationOptions> myItems = new ArrayList<>();
//        items.add(new RecommendationOptions(plantingString, EnumAdviceTasks.PLANTING_AND_HARVEST, 0));
        myItems.add(new RecommendationOptions(manualTillageCostsString, EnumAdviceTasks.MANUAL_TILLAGE_COST, checkStatus(EnumAdviceTasks.MANUAL_TILLAGE_COST)));
        myItems.add(new RecommendationOptions(tractorAccessString, EnumAdviceTasks.TRACTOR_ACCESS, checkStatus(EnumAdviceTasks.TRACTOR_ACCESS)));
        myItems.add(new RecommendationOptions(weedControlCostString, EnumAdviceTasks.COST_OF_WEED_CONTROL, checkStatus(EnumAdviceTasks.COST_OF_WEED_CONTROL)));
        myItems.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)));
        myItems.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)));
        return myItems;
    }
}
