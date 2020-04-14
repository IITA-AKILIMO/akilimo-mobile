package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.AdapterGridTwoLine;
import com.iita.akilimo.entities.CurrentFieldYield;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.MathHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.widget.SpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RootYieldActivity extends BaseActivity {

    @BindString(R.string.title_activity_cassava_root_yield)
    String cassavaRootYield;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rootYieldRecycler)
    RecyclerView recyclerView;
    @BindView(R.id.coordinatorLayout)
    View viewPos;

    @BindView(R.id.btnFinish)
    AppCompatButton btnFinish;
    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;

    private CurrentFieldYield savedYield;
    private MathHelper mathHelper;
    private AdapterGridTwoLine mAdapter;

    private double selectedYieldAmount = 0.0;
    private Integer[] imageIDs = {
            R.drawable.yield_less_than_7point5,
            R.drawable.yield_7point5_to_15,
            R.drawable.yield_15_to_22point5,
            R.drawable.yield_22_to_30,
            R.drawable.yield_more_than_30,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_yield);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(this);
        mathHelper = new MathHelper();

        initToolbar();

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            countryCode = mandatoryInfo.getCountryCode();
            areaUnit = mandatoryInfo.getAreaUnit();
        }

        savedYield = objectBoxEntityProcessor.getCurrentFieldYield();
        if (savedYield != null) {
            selectedYieldAmount = savedYield.getYieldAmount();
        } else {
            savedYield = new CurrentFieldYield();
        }

        initComponent();
    }


    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(cassavaRootYield);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {
        btnFinish.setText(context.getString(R.string.lbl_finish));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 3), true));
        recyclerView.setHasFixedSize(true);

        List<CurrentFieldYield> items = setYieldData(areaUnit);
        //set data and list adapter
        mAdapter = new AdapterGridTwoLine(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setItems(selectedYieldAmount, items);

        // on item list clicked
        mAdapter.setOnItemClickListener((view, fieldYield, position) -> {
            mAdapter.setActiveRowIndex(position);
            selectedYieldAmount = fieldYield.getYieldAmount();
//            toolbar.setNavigationIcon(R.drawable.ic_done);

            fieldYield.setId(savedYield.getId());
            objectBoxEntityProcessor.saveCurrentFieldYield(fieldYield);
            mAdapter.setItems(selectedYieldAmount, items);
            Snackbar.make(viewPos, fieldYield.getFieldYieldLabel(), Snackbar.LENGTH_SHORT)
//                    .setAction(R.string.lbl_close, showListener)
                    .show();
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
            showCustomWarningDialog("Invalid yield", "Please specify your current field yield", "OK");
            return;
        }
        closeActivity(backPressed);
    }

    private List<CurrentFieldYield> setYieldData(@Nonnull String areaUnit) {
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

        List<CurrentFieldYield> items = new ArrayList<>();
        items.add(yieldObject(imageIDs[0], rd_3_tonnes, 3.75));
        items.add(yieldObject(imageIDs[1], rd_6_tonnes, 11.25));
        items.add(yieldObject(imageIDs[2], rd_9_tonnes, 18.75));
        items.add(yieldObject(imageIDs[3], rd_12_tonnes, 26.25));
        items.add(yieldObject(imageIDs[4], rd_more, 33.75));

        return items;
    }

    private CurrentFieldYield yieldObject(Integer imageID, String yieldLabel, double fieldYieldAmount) {
        double currentFieldYieldAmount = mathHelper.computeFieldYield(fieldYieldAmount, currency);
        CurrentFieldYield cfy = new CurrentFieldYield();
        cfy.setYieldAmount(currentFieldYieldAmount);
        cfy.setImageId(imageID);
        cfy.setFieldYieldLabel(yieldLabel);

        return cfy;
    }


}
