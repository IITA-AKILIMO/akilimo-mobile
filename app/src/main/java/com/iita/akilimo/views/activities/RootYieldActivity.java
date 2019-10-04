package com.iita.akilimo.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.AdapterGridTwoLine;
import com.iita.akilimo.data.DataGenerator;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.models.CurrentFieldYield;
import com.iita.akilimo.utils.CurrencyHelper;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.widget.SpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RootYieldActivity extends BaseActivity {

    @BindString(R.string.title_activity_cassava_root_yield)
    String cassavaRootYield;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private CurrencyHelper currencyHelper;
    private AdapterGridTwoLine mAdapter;

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
        currencyHelper = new CurrencyHelper();

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(cassavaRootYield);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.blue_400);

        toolbar.setNavigationOnClickListener(v -> closeActivity(false));
    }

    @Override
    protected void initComponent() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 3), true));
        recyclerView.setHasFixedSize(true);

        String rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_acre);
        String rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_acre);
        String rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_acre);
        String rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_acre);
        String rd_more = getString(R.string.yield_more_than_12_tonnes_per_acre);


        areaUnit = "ha";
        switch (areaUnit) {
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
        items.add(yieldObject(imageIDs[0], rd_3_tonnes, 25));
        items.add(yieldObject(imageIDs[1], rd_6_tonnes, 50));
        items.add(yieldObject(imageIDs[2], rd_9_tonnes, 100));
        items.add(yieldObject(imageIDs[3], rd_12_tonnes, 150));
        items.add(yieldObject(imageIDs[4], rd_more, 200));

        items.add(yieldObject(imageIDs[0], rd_3_tonnes, 25));


        //set data and list adapter
        mAdapter = new AdapterGridTwoLine(this, items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterGridTwoLine.OnItemClickListener() {
            @Override
            public void onItemClick(View view, CurrentFieldYield fieldYield, int position) {
                mAdapter.setActiveRowIndex(position);
                CurrentFieldYield savedYield = objectBoxEntityProcessor.getCurrentFieldYield();
                toolbar.setNavigationIcon(R.drawable.ic_done);
                if (savedYield != null) {
                    //update record
                    fieldYield.setId(savedYield.getId());
                }
//                fieldYield.setYieldDesc(fieldYield.getFieldYieldLabel());
                objectBoxEntityProcessor.saveCurrentFieldYield(fieldYield);
                mAdapter.notifyDataSetChanged();
                Snackbar.make(view, fieldYield.getFieldYieldLabel() + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private CurrentFieldYield yieldObject(Integer imageID, String yieldLabel, double fieldYieldAmount) {
        double currentFieldYieldAmount = currencyHelper.computeFieldYield(fieldYieldAmount, currency);
        CurrentFieldYield cfy = new CurrentFieldYield();
        cfy.setYieldAmount(currentFieldYieldAmount);
        cfy.setImageId(imageID);
        cfy.setFieldYieldLabel(yieldLabel);

        return cfy;
    }
}
