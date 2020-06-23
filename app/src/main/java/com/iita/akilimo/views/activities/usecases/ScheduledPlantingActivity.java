package com.iita.akilimo.views.activities.usecases;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecOptionsAdapter;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityScheduledPlantingBinding;
import com.iita.akilimo.entities.UseCases;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.utils.enums.EnumUseCase;
import com.iita.akilimo.views.activities.CassavaMarketActivity;
import com.iita.akilimo.views.activities.DatesActivity;
import com.iita.akilimo.views.activities.RootYieldActivity;

import java.util.ArrayList;
import java.util.List;

;

public class ScheduledPlantingActivity extends BaseActivity {


    Toolbar toolbar;
    RecyclerView recyclerView;
    AppCompatButton btnGetRec;

    ActivityScheduledPlantingBinding binding;


    String plantingString;
    String marketOutletString;
    String rootYieldString;

    private Activity activity;
    private RecOptionsAdapter mAdapter;
    private List<RecommendationOptions> items = new ArrayList<>();
    private UseCases useCases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduledPlantingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        activity = this;
        database = AppDatabase.getDatabase(context);


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
        getSupportActionBar().setTitle(getString(R.string.lbl_scheduled_planting_and_harvest));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {
        plantingString = getString(R.string.lbl_planting_harvest);
        marketOutletString = getString(R.string.lbl_market_outlet);
        rootYieldString = getString(R.string.lbl_typical_yield);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        btnGetRec.setOnClickListener(view -> {
            //launch the recommendation view
            useCases = database.useCaseDao().findOne();
            try {
                if (useCases == null) {
                    useCases = new UseCases();
                }
                useCases.setFR(false);
                useCases.setCIM(false);
                useCases.setCIS(false);
                useCases.setSPH(true);
                useCases.setSPP(true);
                useCases.setBPP(false);
                useCases.setName(EnumUseCase.SP.name());

                database.useCaseDao().insert(useCases);
                processRecommendations(activity);
            } catch (Exception ex) {
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
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

        items.add(new RecommendationOptions(plantingString, EnumAdviceTasks.PLANTING_AND_HARVEST, 0));
        items.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, 0));
        items.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, 0));

        mAdapter = new RecOptionsAdapter(this, items, ItemAnimation.RIGHT_LEFT);
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
            }
            if (intent != null) {
                startActivity(intent);
                openActivity();
            } else {
                Snackbar.make(view, "Item " + obj.getRecommendationName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
