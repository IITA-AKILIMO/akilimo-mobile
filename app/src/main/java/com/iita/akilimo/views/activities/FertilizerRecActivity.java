package com.iita.akilimo.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecOptionsAdapter;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.RecommendationOptions;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FertilizerRecActivity extends BaseActivity {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.btnGetRec)
    MaterialButton btnGetRec;

    @BindString(R.string.lbl_fertilizer_recommendations)
    String recommendations;

    @BindString(R.string.lbl_planting_harvest)
    String plantingString;
    @BindString(R.string.lbl_available_fertilizers)
    String fertilizerString;
    @BindString(R.string.lbl_investment_amount)
    String investmentString;
    @BindString(R.string.lbl_typical_yield)
    String rootYieldString;

    private Activity activity;
    private RecOptionsAdapter mAdapter;
    private List<RecommendationOptions> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizer_rec);
        ButterKnife.bind(this);

        context = this;
        activity = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        initToolbar();
        initComponent();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(recommendations);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Tools.setSystemBarColor(this, R.color.blue_900);

        toolbar.setNavigationOnClickListener(v -> {
            closeActivity(false);
        });
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
            recAdvice.setFR(true);
            recAdvice.setIC(false);
            recAdvice.setSPH(false);
            recAdvice.setSPP(false);
            recAdvice.setBPP(false);

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


        items.add(new RecommendationOptions(plantingString, EnumAdviceTasks.PLANTING_AND_HARVEST, 0));
        items.add(new RecommendationOptions(fertilizerString, EnumAdviceTasks.AVAILABLE_FERTILIZERS, 0));
        items.add(new RecommendationOptions(investmentString, EnumAdviceTasks.INVESTMENT_AMOUNT, 0));
        items.add(new RecommendationOptions(rootYieldString, EnumAdviceTasks.TYPICAL_ROOT_YIELD, 0));

        mAdapter = new RecOptionsAdapter(this, items, ItemAnimation.BOTTOM_UP);
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
                case AVAILABLE_FERTILIZERS:
                    intent = new Intent(context, FertilizersActivity.class);
                    break;
                case INVESTMENT_AMOUNT:
                    intent = new Intent(context, InvestmentAmountActivity.class);
                    break;
                case TYPICAL_ROOT_YIELD:
                    intent = new Intent(context, RootYieldActivity.class);
                    break;
            }
            if (intent != null) {
                startActivity(intent);
                Animatoo.animateSlideRight(context);
            }
        });

    }
}
