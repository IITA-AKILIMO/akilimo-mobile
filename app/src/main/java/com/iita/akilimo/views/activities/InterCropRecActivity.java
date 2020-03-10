package com.iita.akilimo.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecOptionsAdapter;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumUseCase;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class InterCropRecActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btnGetRecommendation)
    AppCompatButton btnGetRec;

    @BindString(R.string.lbl_intercropping)
    String recommendations;

    @BindString(R.string.lbl_planting_harvest)
    String plantingString;

    @BindString(R.string.lbl_fertilizer_costs)
    String fertilizerString;

    @BindString(R.string.lbl_market_outlet)
    String marketOutletString;
    @BindString(R.string.lbl_market_outlet_maize)
    String marketOutletMaizeString;

    @BindString(R.string.lbl_typical_yield)
    String rootYieldString;

    @BindString(R.string.lbl_maize_performance)
    String maizeHeightString;

    @BindString(R.string.lbl_sweet_potato_prices)
    String sweetPotatoPricesString;

    private Activity activity;
    private RecOptionsAdapter mAdapter;
    private List<RecommendationOptions> items = new ArrayList<>();
    private EnumUseCase useCase;
    private boolean icMaize;
    private boolean icPotato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inter_crop_rec);
        ButterKnife.bind(this);
        context = this;
        activity = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this);

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            countryCode = mandatoryInfo.getCountryCode();
            currency = mandatoryInfo.getCurrency();

            switch (mandatoryInfo.getCountryEnum()) {
                case NIGERIA:
                    recommendations = getString(R.string.title_maize_intercropping);
                    useCase = EnumUseCase.CIM;
                    break;
                case TANZANIA:
                    recommendations = getString(R.string.title_sweet_potato_intercropping);
                    useCase = EnumUseCase.CIS;
                    break;
            }
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        btnGetRec.setOnClickListener(view -> {
            //launch the recommendation view
            RecAdvice recAdvice = objectBoxEntityProcessor.getRecAdvice();
            if (recAdvice == null) {
                recAdvice = new RecAdvice();
            }
            recAdvice.setFR(false);
            recAdvice.setCIM(icMaize);
            recAdvice.setCIS(icPotato);
            recAdvice.setSPH(false);
            recAdvice.setSPP(false);
            recAdvice.setBPP(false);
            recAdvice.setUseCase(useCase.name());

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
        if (countryCode.equalsIgnoreCase(EnumCountry.NIGERIA.countryCode())) {
            icMaize = true;
            items.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM, 0));
            items.add(new RecommendationOptions(maizeHeightString, EnumAdviceTasks.MAIZE_PERFORMANCE, 0));
//            items.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, 0));
            items.add(new RecommendationOptions(marketOutletMaizeString, EnumAdviceTasks.MARKET_OUTLET_MAIZE, 0));
        } else if (countryCode.equalsIgnoreCase(EnumCountry.TANZANIA.countryCode())) {
            icPotato = true;
            items.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS, 0));
            items.add(new RecommendationOptions(marketOutletString, EnumAdviceTasks.MARKET_OUTLET_CASSAVA, 0));
            items.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.CURRENT_CASSAVA_YIELD, 0));
            items.add(new RecommendationOptions(sweetPotatoPricesString, EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO, 0));
        }
        mAdapter = new RecOptionsAdapter(this, items, ItemAnimation.FADE_IN);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((view, obj, position) -> {
            Intent intent = null;
            EnumAdviceTasks advice = obj.getRecCode();
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
                Animatoo.animateSlideLeft(this);
            } else {
                Snackbar.make(view, "Item " + obj.getRecommendationName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });

    }
}
