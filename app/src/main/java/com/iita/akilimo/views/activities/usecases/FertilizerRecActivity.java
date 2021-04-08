package com.iita.akilimo.views.activities.usecases;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecOptionsAdapter;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityFertilizerRecBinding;
import com.iita.akilimo.entities.AdviceStatus;
import com.iita.akilimo.entities.UseCases;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.utils.enums.EnumUseCase;
import com.iita.akilimo.views.activities.CassavaMarketActivity;
import com.iita.akilimo.views.activities.DatesActivity;
import com.iita.akilimo.views.activities.FertilizersActivity;
import com.iita.akilimo.views.activities.InvestmentAmountActivity;
import com.iita.akilimo.views.activities.RootYieldActivity;

import java.util.ArrayList;
import java.util.List;

;

public class FertilizerRecActivity extends BaseActivity {

    RecyclerView recyclerView;
    Toolbar toolbar;
    AppCompatButton btnGetRec;

    ActivityFertilizerRecBinding binding;


    String plantingString;
    String fertilizerString;
    String investmentString;
    String rootYieldString;
    String marketOutletString;

    private Activity activity;
    private RecOptionsAdapter mAdapter;
    private List<RecommendationOptions> items = new ArrayList<>();
    private UseCases useCases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFertilizerRecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        activity = this;
        recyclerView = binding.recyclerView;
        toolbar = binding.toolbarLayout.toolbar;
        btnGetRec = binding.singleButton.btnGetRecommendation;


        database = AppDatabase.getDatabase(context);
        initToolbar();
        initComponent();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.lbl_fertilizer_recommendations));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {
        plantingString = getString(R.string.lbl_planting_harvest);
        fertilizerString = getString(R.string.lbl_available_fertilizers);
        investmentString = getString(R.string.lbl_investment_amount);
        rootYieldString = getString(R.string.lbl_typical_yield);
        marketOutletString = getString(R.string.lbl_market_outlet);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //set data and list adapter
        items = getRecItems();
        mAdapter = new RecOptionsAdapter(this, items, ItemAnimation.RIGHT_LEFT);
        recyclerView.setAdapter(mAdapter);

        btnGetRec.setOnClickListener(view -> {
            //launch the recommendation view
            useCases = database.useCaseDao().findOne();
            try {
                if (useCases == null) {
                    useCases = new UseCases();
                }
                useCases.setFR(true);
                useCases.setCIM(false);
                useCases.setCIS(false);
                useCases.setSPH(false);
                useCases.setSPP(false);
                useCases.setBPP(false);
                useCases.setName(EnumUseCase.FR.name());

                database.useCaseDao().insert(useCases);
                processRecommendations(activity);

            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }
        });

        mAdapter.setOnItemClickListener((view, obj, position) -> {
            Intent intent = null;
            EnumAdviceTasks advice = obj.getAdviceName();
            if (advice == null) {
                advice = EnumAdviceTasks.NOT_SELECTED;
            }
            switch (advice) {
                case PLANTING_AND_HARVEST:
                    intent = new Intent(context, DatesActivity.class);
                    break;
                case AVAILABLE_FERTILIZERS:
                    intent = new Intent(context, FertilizersActivity.class);
                    break;
                case INVESTMENT_AMOUNT:
                    intent = new Intent(context, InvestmentAmountActivity.class);
                    break;
                case CURRENT_CASSAVA_YIELD:
                    intent = new Intent(context, RootYieldActivity.class);
                    break;
                case MARKET_OUTLET_CASSAVA:
                    intent = new Intent(context, CassavaMarketActivity.class);
                    break;
            }
            if (intent != null) {
                startActivity(intent);
                openActivity();
            } else {
                Snackbar.make(view, "Item " + obj.getRecName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private List<RecommendationOptions> getRecItems() {
        List<RecommendationOptions> myItems = new ArrayList<>();
        myItems.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)));
        myItems.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS, checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS)));
        myItems.add(new RecommendationOptions(investmentString, EnumAdviceTasks.INVESTMENT_AMOUNT, checkStatus(EnumAdviceTasks.INVESTMENT_AMOUNT)));
        myItems.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)));

        return myItems;
    }

    private AdviceStatus checkStatus(EnumAdviceTasks taskName) {
        AdviceStatus adviceStatus = database.adviceStatusDao().findOne(taskName.name());
        if (adviceStatus != null) {
            return adviceStatus;
        }
        return new AdviceStatus(taskName.name(), false);
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

}
