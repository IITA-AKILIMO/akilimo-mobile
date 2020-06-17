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
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecOptionsAdapter;
import com.iita.akilimo.databinding.ActivityFertilizerRecBinding;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.RealmProcessor;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

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
    private RecAdvice recAdvice;

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


        realmProcessor = new RealmProcessor();
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

        btnGetRec.setOnClickListener(view -> {
            //launch the recommendation view
            recAdvice = realmProcessor.getRecAdvice();
            try (Realm myRealm = getRealmInstance()) {
                myRealm.executeTransaction(realm -> {
                    if (recAdvice == null) {
                        recAdvice = myRealm.createObject(RecAdvice.class);
                    }
                    recAdvice.setFR(true);
                    recAdvice.setCIM(false);
                    recAdvice.setCIS(false);
                    recAdvice.setSPH(false);
                    recAdvice.setSPP(false);
                    recAdvice.setBPP(false);
                    processRecommendations(activity);
                });
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

        items.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, 0));
        items.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS, 0));
        items.add(new RecommendationOptions(investmentString, EnumAdviceTasks.INVESTMENT_AMOUNT, 0));
        items.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, 0));

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
                case AVAILABLE_FERTILIZERS_CIS:
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
                Animatoo.animateSlideRight(context);
            }
        });

    }
}
