package com.iita.akilimo.adapters;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;

import java.util.List;

public class MyStepperAdapter extends AbstractFragmentStepAdapter {

    private final String CURRENT_STEP_POSITION_KEY = "CURRENT_STEP_POSITION_KEY";
    private Context ctx;
    private List<Fragment> fragmentArray;


    public MyStepperAdapter(FragmentManager supportFragmentManager, Context context, List<Fragment> fragmentArray) {
        super(supportFragmentManager, context);
        this.ctx = context;
        this.fragmentArray = fragmentArray;
    }

    @Override
    public Step createStep(int position) {
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
}
