package com.akilimo.mobile.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.android.material.snackbar.Snackbar;
import com.akilimo.mobile.R;
import com.akilimo.mobile.adapters.AdapterListAnimation;
import com.akilimo.mobile.databinding.FragmentDstOptionsBinding;
import com.akilimo.mobile.inherit.BaseFragment;
import com.akilimo.mobile.models.Recommendations;
import com.akilimo.mobile.utils.ItemAnimation;
import com.akilimo.mobile.utils.enums.EnumAdvice;
import com.akilimo.mobile.views.activities.usecases.FertilizerRecActivity;
import com.akilimo.mobile.views.activities.usecases.InterCropRecActivity;
import com.akilimo.mobile.views.activities.usecases.PlantingPracticesActivity;
import com.akilimo.mobile.views.activities.usecases.ScheduledPlantingActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DstOptionsFragment extends BaseFragment {


    RecyclerView recyclerView;
    FragmentDstOptionsBinding binding;


    String frString;
    String icString;
    String sphString;
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
        binding = FragmentDstOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void refreshData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        frString = context.getString(R.string.lbl_fertilizer_recommendations);
        icString = context.getString(R.string.lbl_intercropping);
        sphString = context.getString(R.string.lbl_scheduled_planting_and_harvest);
        bppString = context.getString(R.string.lbl_best_planting_practices);


        recyclerView = binding.recyclerView;

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
        IC.setRecCode(EnumAdvice.IC_MAIZE);
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
            if (advice == null) {
                return;
            }
            switch (advice) {
                case FR:
                    intent = new Intent(context, FertilizerRecActivity.class);
                    break;
                case BPP:
                    intent = new Intent(context, PlantingPracticesActivity.class);
                    break;
                case IC_MAIZE:
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
