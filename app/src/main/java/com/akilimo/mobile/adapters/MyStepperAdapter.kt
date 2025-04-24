package com.akilimo.mobile.adapters;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.akilimo.mobile.R;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;

import java.util.List;

public class MyStepperAdapter extends AbstractFragmentStepAdapter {

    private final Context ctx;
    private final List<Fragment> fragmentArray;


    public MyStepperAdapter(FragmentManager supportFragmentManager, Context context, List<Fragment> fragmentArray) {
        super(supportFragmentManager, context);
        this.ctx = context;
        this.fragmentArray = fragmentArray;
    }

    @Override
    public Step createStep(int position) {
        String CURRENT_STEP_POSITION_KEY = "CURRENT_STEP_POSITION_KEY";
        final Fragment step = fragmentArray.get(position);
        Bundle bundleParams = new Bundle();
        bundleParams.putInt(CURRENT_STEP_POSITION_KEY, position);
        step.setArguments(bundleParams);

        return (Step) step;
    }

    @Override
    public int getCount() {
        return fragmentArray.size();
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {
        StepViewModel.Builder builder = new StepViewModel.Builder(ctx);
        if (position == 0) {
            builder.setBackButtonLabel(R.string.lbl_cancel);
        }
        return builder.create();
    }
}
