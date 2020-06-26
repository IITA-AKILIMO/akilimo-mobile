package com.iita.akilimo.views.activities.usecases;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.AdapterListAnimation;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityRecommendationsActivityBinding;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.Recommendations;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.EnumAdvice;
import com.iita.akilimo.utils.enums.EnumCountry;

import java.util.ArrayList;
import java.util.List;

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
    private AdapterListAnimation mAdapter;
    private List<Recommendations> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecommendationsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        database = AppDatabase.getDatabase(context);

        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }

        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.recyclerView;
        initToolbar();
        initComponent();
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

        if (countryCode.equals(EnumCountry.NIGERIA.countryCode())) {
            Recommendations IC_MAIZE = new Recommendations();
            IC_MAIZE.setRecCode(EnumAdvice.IC_MAIZE);
            IC_MAIZE.setRecommendationName(icMaizeString);
            IC_MAIZE.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
            items.add(IC_MAIZE);
        } else if (countryCode.equals(EnumCountry.TANZANIA.countryCode())) {

            Recommendations IC_SWEET_POTATO = new Recommendations();
            IC_SWEET_POTATO.setRecCode(EnumAdvice.IC_SWEET_POTATO);
            IC_SWEET_POTATO.setRecommendationName(icSweetPotatoString);
            IC_SWEET_POTATO.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
            items.add(IC_SWEET_POTATO);
        }

        Recommendations SPH = new Recommendations();
        SPH.setRecCode(EnumAdvice.SPH);
        SPH.setRecommendationName(sphString);
        SPH.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
        items.add(SPH);

        Recommendations BPP = new Recommendations();
        BPP.setRecCode(EnumAdvice.BPP);
        BPP.setRecommendationName(bppString);
        BPP.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_gradient_very_soft));
        items.add(BPP);


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
        mAdapter.setItems(items, ItemAnimation.BOTTOM_UP);
        // on item list clicked
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            //let us process the data
            Intent intent = null;
            EnumAdvice advice = obj.getRecCode();
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
                startActivity(intent);
                openActivity();
            } else {
                Snackbar.make(view, "Item " + obj.getRecommendationName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });

    }
}
