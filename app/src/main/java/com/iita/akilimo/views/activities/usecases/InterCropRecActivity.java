package com.iita.akilimo.views.activities.usecases;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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
import com.iita.akilimo.databinding.ActivityInterCropRecBinding;
import com.iita.akilimo.entities.AdviceStatus;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.entities.UseCases;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumUseCase;
import com.iita.akilimo.views.activities.CassavaMarketActivity;
import com.iita.akilimo.views.activities.DatesActivity;
import com.iita.akilimo.views.activities.IntercropFertilizersActivity;
import com.iita.akilimo.views.activities.MaizeMarketActivity;
import com.iita.akilimo.views.activities.MaizePerformanceActivity;
import com.iita.akilimo.views.activities.RootYieldActivity;
import com.iita.akilimo.views.activities.SweetPotatoMarketActivity;

import java.util.ArrayList;
import java.util.List;

public class InterCropRecActivity extends BaseActivity {

    RecyclerView recyclerView;
    Toolbar toolbar;
    AppCompatButton btnGetRec;
    ActivityInterCropRecBinding binding;


    String recommendations;
    String plantingString;
    String fertilizerString;
    String marketOutletString;
    String marketOutletMaizeString;
    String rootYieldString;
    String maizeHeightString;
    String sweetPotatoPricesString;

    private Activity activity;
    private RecOptionsAdapter mAdapter;
    private List<RecommendationOptions> items = new ArrayList<>();
    private UseCases useCases;
    private EnumUseCase useCase;
    private boolean icMaize;
    private boolean icPotato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInterCropRecBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        activity = this;
        recyclerView = binding.recyclerView;
        toolbar = binding.toolbarLayout.toolbar;
        btnGetRec = binding.singleButton.btnGetRecommendation;


        database = AppDatabase.getDatabase(context);


        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }

        switch (countryCode) {
            case "NG":
                recommendations = getString(R.string.title_maize_intercropping);
                useCase = EnumUseCase.CIM;
                break;
            case "TZ":
                recommendations = getString(R.string.title_sweet_potato_intercropping);
                useCase = EnumUseCase.CIS;
                break;
        }

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

        recommendations = getString(R.string.lbl_intercropping);
        plantingString = getString(R.string.lbl_planting_harvest);
        fertilizerString = getString(R.string.lbl_available_fertilizers);
        marketOutletString = getString(R.string.lbl_market_outlet);
        marketOutletMaizeString = getString(R.string.lbl_market_outlet_maize);
        rootYieldString = getString(R.string.lbl_typical_yield);
        maizeHeightString = getString(R.string.lbl_maize_performance);
        sweetPotatoPricesString = getString(R.string.lbl_sweet_potato_prices);

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
                useCases.setCIM(icMaize);
                useCases.setCIS(icPotato);
                useCases.setSPH(false);
                useCases.setSPP(false);
                useCases.setBPP(false);
                useCases.setName(useCase.name());

                database.useCaseDao().insert(useCases);
                processRecommendations(activity);
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (countryCode.equalsIgnoreCase(EnumCountry.Nigeria.countryCode())) {
            icMaize = true;
            items.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM, checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM)));
            items.add(new RecommendationOptions(maizeHeightString, EnumAdviceTasks.MAIZE_PERFORMANCE, checkStatus(EnumAdviceTasks.MAIZE_PERFORMANCE)));
            items.add(new RecommendationOptions(marketOutletMaizeString, EnumAdviceTasks.MARKET_OUTLET_MAIZE, checkStatus(EnumAdviceTasks.MARKET_OUTLET_MAIZE)));
        } else if (countryCode.equalsIgnoreCase(EnumCountry.Tanzania.countryCode())) {
            icPotato = true;
            items.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS, checkStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS)));
            items.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, checkStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA)));
            items.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, checkStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD)));
            items.add(new RecommendationOptions(sweetPotatoPricesString, EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO, checkStatus(EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO)));
        }
        mAdapter = new RecOptionsAdapter(this, items, ItemAnimation.FADE_IN);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((view, obj, position) -> {
            Intent intent = null;
            EnumAdviceTasks advice = obj.getAdviceName();
            switch (advice) {
                case PLANTING_AND_HARVEST:
                    intent = new Intent(context, DatesActivity.class);
                    break;
                case MARKET_OUTLET_CASSAVA:
                    intent = new Intent(context, CassavaMarketActivity.class);
                    intent.putExtra(CassavaMarketActivity.useCaseTag, (Parcelable) useCase);
                    break;
                case MARKET_OUTLET_SWEET_POTATO:
                    intent = new Intent(context, SweetPotatoMarketActivity.class);
                    break;
                case MARKET_OUTLET_MAIZE:
                    intent = new Intent(context, MaizeMarketActivity.class);
                    break;
                case CURRENT_CASSAVA_YIELD:
                    intent = new Intent(context, RootYieldActivity.class);
                    break;
                case AVAILABLE_FERTILIZERS_CIS:
                case AVAILABLE_FERTILIZERS_CIM:
                    intent = new Intent(context, IntercropFertilizersActivity.class);
                    intent.putExtra(IntercropFertilizersActivity.useCaseTag, (Parcelable) useCase);
                    break;
                case MAIZE_PERFORMANCE:
                    intent = new Intent(context, MaizePerformanceActivity.class);
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
}
