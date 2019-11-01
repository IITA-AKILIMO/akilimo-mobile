package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.AdapterListAnimation;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.models.Recommendations;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.enums.EnumAdvice;
import com.iita.akilimo.views.activities.FertilizerRecActivity;
import com.iita.akilimo.views.activities.InterCropRecActivity;
import com.iita.akilimo.views.activities.PlantingPracticesActivity;
import com.iita.akilimo.views.activities.ScheduledPlantingActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DstOptionsFragment extends BaseFragment {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindString(R.string.lbl_fertilizer_recommendations)
    String frString;
    @BindString(R.string.lbl_intercropping)
    String icString;
    @BindString(R.string.lbl_scheduled_planting_and_harvest)
    String sphString;
    @BindString(R.string.lbl_best_planting_practices)
    String bppString;

    private AdapterListAnimation mAdapter;
    private List<Recommendations> items = new ArrayList<>();

    public DstOptionsFragment() {
        // Required empty public constructor
    }

    public static DstOptionsFragment newInstance() {
        return new DstOptionsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dst_options, container, false);
    }

    @Override
    public void refreshData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        mAdapter = new AdapterListAnimation(context);
        recyclerView.setAdapter(mAdapter);

        items = new ArrayList<>();
        Recommendations FR = new Recommendations();
        FR.setRecCode(EnumAdvice.FR);
        FR.setRecommendationName(frString);
        items.add(FR);

        Recommendations IC = new Recommendations();
        IC.setRecCode(EnumAdvice.IC);
        IC.setRecommendationName(icString);
        items.add(IC);

        Recommendations SPH = new Recommendations();
        SPH.setRecCode(EnumAdvice.SPH);
        SPH.setRecommendationName(sphString);
        items.add(SPH);

        Recommendations BPP = new Recommendations();
        BPP.setRecCode(EnumAdvice.BPP);
        BPP.setRecommendationName(bppString);
        items.add(BPP);

        initComponent();
    }


    private void initComponent() {
        //set data and list adapter
        mAdapter.setItems(items, ItemAnimation.FADE_IN);
        // on item list clicked
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            //let us process the data
            Intent intent = null;
            EnumAdvice advice = obj.getRecCode();
            switch (advice) {
                case FR:
                    intent = new Intent(context, FertilizerRecActivity.class);
                    break;
                case BPP:
                    intent = new Intent(context, PlantingPracticesActivity.class);
                    break;
                case IC:
                    intent = new Intent(context, InterCropRecActivity.class);
                    break;
                case SPH:
                    intent = new Intent(context, ScheduledPlantingActivity.class);
                    break;
                case WM:
                    break;
            }
            if (intent != null) {
                startActivity(intent);
                Animatoo.animateSlideRight(context);
            } else {
                Snackbar.make(view, "Item " + obj.getRecommendationName() + " clicked but not launched", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

}
