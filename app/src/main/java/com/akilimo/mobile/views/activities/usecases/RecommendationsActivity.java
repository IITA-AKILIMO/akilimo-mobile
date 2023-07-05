package com.akilimo.mobile.views.activities.usecases;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akilimo.mobile.R;
import com.akilimo.mobile.adapters.AdapterListAnimation;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityRecommendationsActivityBinding;
import com.akilimo.mobile.entities.Currency;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.entities.UseCases;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.interfaces.IVolleyCallback;
import com.akilimo.mobile.models.Recommendations;
import com.akilimo.mobile.rest.RestParameters;
import com.akilimo.mobile.rest.RestService;
import com.akilimo.mobile.utils.SessionManager;
import com.akilimo.mobile.utils.TheItemAnimation;
import com.akilimo.mobile.utils.enums.EnumAdvice;
import com.akilimo.mobile.utils.enums.EnumCountry;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.sentry.Sentry;

public class RecommendationsActivity extends BaseActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;

    ActivityRecommendationsActivityBinding binding;


    String frString;
    String icMaizeString;
    String icSweetPotatoString;
    String sphString;
    String bppString;

    private MandatoryInfo mandatoryInfo;
    private UseCases useCase;
    private AdapterListAnimation mAdapter;
    private List<Recommendations> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecommendationsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);
        queue = Volley.newRequestQueue(context);

        if (sessionManager == null) {
            sessionManager = new SessionManager(context);
        }
        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        useCase = database.useCaseDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }

        toolbar = binding.toolbar;
        recyclerView = binding.recyclerView;
        initToolbar();
        initComponent();

        updateCurrencyList();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_home);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.lbl_recommendations));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }


    @Override
    protected void initComponent() {
        frString = getString(R.string.lbl_fertilizer_recommendations);
        icMaizeString = getString(R.string.lbl_intercropping_maize);
        icSweetPotatoString = getString(R.string.lbl_intercropping_sweet_potato);
        sphString = getString(R.string.lbl_scheduled_planting_and_harvest);
        bppString = getString(R.string.lbl_best_planting_practices);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        //set data and list adapter
        mAdapter = new AdapterListAnimation(this, R.layout.item_card_recommendation_no_arrow);
        recyclerView.setAdapter(mAdapter);
        items = new ArrayList<>();

        Recommendations FR = new Recommendations();
        FR.setRecCode(EnumAdvice.FR);
        FR.setRecommendationName(frString);
        FR.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
        items.add(FR);

        Recommendations SPH = new Recommendations();
        SPH.setRecCode(EnumAdvice.SPH);
        SPH.setRecommendationName(sphString);
        SPH.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
        items.add(SPH);

        if (!countryCode.equals(EnumCountry.Ghana.countryCode())) {
            Recommendations BPP = new Recommendations();
            BPP.setRecCode(EnumAdvice.BPP);
            BPP.setRecommendationName(bppString);
            BPP.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
            items.add(BPP);
        }

        if (countryCode.equals(EnumCountry.Nigeria.countryCode())) {
            Recommendations IC_MAIZE = new Recommendations();
            IC_MAIZE.setRecCode(EnumAdvice.IC_MAIZE);
            IC_MAIZE.setRecommendationName(icMaizeString);
            IC_MAIZE.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
            items.add(IC_MAIZE);
        } else if (countryCode.equals(EnumCountry.Tanzania.countryCode())) {
            Recommendations IC_SWEET_POTATO = new Recommendations();
            IC_SWEET_POTATO.setRecCode(EnumAdvice.IC_SWEET_POTATO);
            IC_SWEET_POTATO.setRecommendationName(icSweetPotatoString);
            IC_SWEET_POTATO.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
            items.add(IC_SWEET_POTATO);
        }


        setAdapter();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(context, R.string.lbl_back_instructions, Toast.LENGTH_SHORT)
                .show();
    }

    private void setAdapter() {
        mAdapter.setItems(items, TheItemAnimation.BOTTOM_UP);
        // on item list clicked
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            //let us process the data
            Intent intent = null;
            EnumAdvice advice = obj.getRecCode();
            if (advice == null) {
                advice = EnumAdvice.WM;
            }
            switch (advice) {
                case FR:
                    intent = new Intent(this, FertilizerRecActivity.class);
                    break;
                case BPP:
                    intent = new Intent(this, PlantingPracticesActivity.class);
                    break;
                case IC_MAIZE:
                case IC_SWEET_POTATO:
                    intent = new Intent(this, InterCropRecActivity.class);
                    break;
                case SPH:
                    intent = new Intent(this, ScheduledPlantingActivity.class);
                    break;
                case WM:
                    break;
            }
            if (intent != null) {
                if (useCase == null) {
                    useCase = new UseCases();
                }
                useCase.setName(advice.name());
                database.useCaseDao().insert(useCase);
                startActivity(intent);
                openActivity();
            } else {
                Snackbar.make(view, "Item " + obj.getRecommendationName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });

    }


    private void updateCurrencyList() {
        final RestService restService = RestService.getInstance(queue, this);
        final RestParameters restParameters = new RestParameters("v1/currency", countryCode);
        restParameters.setInitialTimeout(5000);
        restService.setParameters(restParameters);
        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(@NotNull String jsonStringResult) {
            }

            @Override
            public void onSuccessJsonArr(@NotNull JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {

                    List<Currency> currencyList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<Currency>>() {
                    });

                    if (currencyList.size() > 0) {
                        database.currencyDao().insertAll(currencyList);
                    }
                } catch (Exception ex) {
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    Sentry.captureException(ex);
                }
            }

            @Override
            public void onSuccessJsonObject(@NonNull JSONObject jsonObject) {
            }

            @Override
            public void onError(@NonNull VolleyError ex) {
            }
        });
    }
}
