package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.FieldYieldAdapter;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityRootYieldBinding;
import com.iita.akilimo.entities.FieldYield;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.entities.UseCases;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumUseCase;
import com.iita.akilimo.views.fragments.dialog.FertilizerPriceDialogFragment;
import com.iita.akilimo.views.fragments.dialog.RootYieldDialogFragment;
import com.iita.akilimo.widget.SpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

;

public class RootYieldActivity extends BaseActivity {

    String cassavaRootYield;


    Toolbar toolbar;
    RecyclerView recyclerView;
    View viewPos;
    AppCompatButton btnFinish;
    AppCompatButton btnCancel;
    TextView rootYieldTitle;

    ActivityRootYieldBinding binding;

    private FieldYield savedYield;
    private UseCases useCase;
    private FieldYieldAdapter mAdapter;

    private double selectedYieldAmount = 0.0;
    private final Integer[] yieldImages = {
            R.drawable.yield_less_than_7point5,
            R.drawable.yield_7point5_to_15,
            R.drawable.yield_15_to_22point5,
            R.drawable.yield_22_to_30,
            R.drawable.yield_more_than_30,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRootYieldBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        database = AppDatabase.getDatabase(context);
        MandatoryInfo mandatoryInfo = database.mandatoryInfoDao().findOne();
        useCase = database.useCaseDao().findOne();
        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo.getAreaUnit();
        }
        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }

        savedYield = database.fieldYieldDao().findOne();
        if (savedYield != null) {
            selectedYieldAmount = savedYield.getYieldAmount();
        }
        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.rootYieldRecycler;
        viewPos = binding.coordinatorLayout;
        btnFinish = binding.twoButtons.btnFinish;
        btnCancel = binding.twoButtons.btnCancel;
        rootYieldTitle = binding.rootYieldTitle;

        initToolbar();
        initComponent();
    }


    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_cassava_root_yield));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {
        btnFinish.setText(getString(R.string.lbl_finish));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.addItemDecoration(new SpacingItemDecoration(1, Tools.dpToPx(this, 3), true));
        recyclerView.setHasFixedSize(true);
        String tonnage = getString(R.string.lbl_acre_yield);
        if (areaUnit.equalsIgnoreCase("ha") || areaUnit.equalsIgnoreCase("hekta")) {
            tonnage = getString(R.string.lbl_ha_yield);
        }

        String title = getString(R.string.lbl_typical_yield_question, tonnage);
        if (useCase != null) {
            if (useCase.getName().equals(EnumUseCase.FR.name())) {
                title = getString(R.string.lbl_typical_yield_question_fr, tonnage);
            }
        }
        rootYieldTitle.setText(title);

        List<FieldYield> items = setYieldData(areaUnit);
        //set data and list adapter
        mAdapter = new FieldYieldAdapter(this, items, ItemAnimation.FADE_IN);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setItems(selectedYieldAmount, items);

        mAdapter.setOnItemClickListener((view, fieldYield, position) -> {
            try {
                //show a popup dialog here
                Bundle arguments = new Bundle();
                arguments.putParcelable(RootYieldDialogFragment.YIELD_DATA, fieldYield);

                RootYieldDialogFragment rootYieldDialogFragment = new RootYieldDialogFragment(context);
                rootYieldDialogFragment.setArguments(arguments);

                rootYieldDialogFragment.setOnDismissListener((yield, yieldConfirmed) -> {
                    if (yieldConfirmed) {
                        if (savedYield == null) {
                            savedYield = new FieldYield();
                        }
                        String yieldLabel = yield.getFieldYieldLabel();
                        selectedYieldAmount = yield.getYieldAmount();
                        savedYield.setYieldAmount(selectedYieldAmount);
                        savedYield.setFieldYieldLabel(yieldLabel);
                        database.fieldYieldDao().insert(savedYield);

                        mAdapter.setActiveRowIndex(position);
                    }
                    mAdapter.setItems(selectedYieldAmount, items);
                });


                FragmentTransaction fragmentTransaction;
                if (getFragmentManager() != null) {
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag(RootYieldDialogFragment.ARG_ITEM_ID);
                    if (prev != null) {
                        fragmentTransaction.remove(prev);
                    }
                    fragmentTransaction.addToBackStack(null);
                    rootYieldDialogFragment.show(getSupportFragmentManager(), RootYieldDialogFragment.ARG_ITEM_ID);
                }

//                Toast.makeText(context, yieldAmountLabel, Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }

        });

        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    public void onBackPressed() {
        validate(true);
    }

    @Override
    protected void validate(boolean backPressed) {
        if (!(selectedYieldAmount > 0)) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_yield), getString(R.string.lbl_current_field_yield_prompt), getString(R.string.lbl_ok));
            return;
        }
        closeActivity(backPressed);
    }

    private List<FieldYield> setYieldData(@NonNull String areaUnit) {
        String rd_3_tonnes;
        String rd_6_tonnes;
        String rd_9_tonnes;
        String rd_12_tonnes;
        String rd_more;
        switch (areaUnit) {
            default:
            case "acre":
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_acre);
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_acre);
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_acre);
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_acre);
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_acre);
                break;
            case "ha":
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_hectare);
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_hectare);
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_hectare);
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_hectare);
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_hectare);
                break;
            case "m2":
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_meter);
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_meter);
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_meter);
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_meter);
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_meter);
                break;

        }

        List<FieldYield> items = new ArrayList<>();
        items.add(createYieldObject(yieldImages[0], getString(R.string.fcy_lower), rd_3_tonnes, 3.75, getString(R.string.lbl_low_yield)));

        items.add(createYieldObject(yieldImages[1], getString(R.string.fcy_about_the_same), rd_6_tonnes, 11.25, getString(R.string.lbl_normal_yield)));

        items.add(createYieldObject(yieldImages[2], getString(R.string.fcy_somewhat_higher), rd_9_tonnes, 18.75, getString(R.string.lbl_high_yield)));

        items.add(createYieldObject(yieldImages[3], getString(R.string.fcy_2_3_times_higher), rd_12_tonnes, 26.25, getString(R.string.lbl_very_high_yield)));

        items.add(createYieldObject(yieldImages[4], getString(R.string.fcy_more_than_3_times_higher), rd_more, 33.75, getString(R.string.lbl_very_high_yield)));

        return items;
    }

    private FieldYield createYieldObject(Integer imageID, String yieldLabel, String fieldYieldAmountLabel, double fieldYieldAmount, String fieldYieldDesc) {
        FieldYield cfy = new FieldYield();
        cfy.setFieldYieldAmountLabel(fieldYieldAmountLabel);
        cfy.setFieldYieldDesc(fieldYieldDesc);
        cfy.setYieldAmount(fieldYieldAmount);
        cfy.setFieldYieldLabel(yieldLabel);
        cfy.setImageId(imageID);

        return cfy;
    }
}
