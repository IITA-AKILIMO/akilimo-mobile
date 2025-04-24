package com.akilimo.mobile.views.activities.usecases;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akilimo.mobile.R;
import com.akilimo.mobile.adapters.RecOptionsAdapter;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityFertilizerRecBinding;
import com.akilimo.mobile.entities.UseCases;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.models.RecommendationOptions;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.akilimo.mobile.utils.enums.EnumUseCase;
import com.akilimo.mobile.views.activities.CassavaMarketActivity;
import com.akilimo.mobile.views.activities.DatesActivity;
import com.akilimo.mobile.views.activities.FertilizersActivity;
import com.akilimo.mobile.views.activities.InvestmentAmountActivity;
import com.akilimo.mobile.views.activities.RootYieldActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import io.sentry.Sentry;

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
        mAdapter = new RecOptionsAdapter();
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
                Sentry.captureException(ex);
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

        setAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
    }

    private void setAdapter() {
        //set data and list adapter
        items = getRecItems();
        mAdapter.setData(items);
        recyclerView.setAdapter(mAdapter);
    }

    private List<RecommendationOptions> getRecItems() {
        List<RecommendationOptions> myItems = new ArrayList<>();
        myItems.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)));
        myItems.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS, checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS)));
        myItems.add(new RecommendationOptions(investmentString, EnumAdviceTasks.INVESTMENT_AMOUNT, checkStatus(EnumAdviceTasks.INVESTMENT_AMOUNT)));
        myItems.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)));

        return myItems;
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

}
