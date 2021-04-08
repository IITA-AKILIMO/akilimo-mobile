package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.MaizePerformanceAdapter;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityMaizePerformanceActivityBinding;
import com.iita.akilimo.entities.AdviceStatus;
import com.iita.akilimo.entities.MaizePerformance;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.utils.ItemAnimation;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.enums.EnumAdviceTasks;
import com.iita.akilimo.views.fragments.dialog.MaizePerformanceDialogFragment;
import com.iita.akilimo.views.fragments.dialog.RootYieldDialogFragment;
import com.iita.akilimo.widget.SpacingItemDecoration;

;import java.util.ArrayList;
import java.util.List;


public class MaizePerformanceActivity extends BaseActivity {
    String activityTitle;
    String poorSoil;
    String richSoil;

    Toolbar toolbar;
    RecyclerView recyclerView;
    View viewPos;

    AppCompatButton btnFinish;
    AppCompatButton btnCancel;
    TextView exceptionTitle;

    private MaizePerformanceAdapter mAdapter;

    ActivityMaizePerformanceActivityBinding binding;


    private MaizePerformance savedMaizePerformance;

    private String selectedPerformanceValue;
    private String maizePerformanceValue;
    private int performanceRadioIndex;

    private final Integer[] performanceImages = {
            R.drawable.ic_maize_1,
            R.drawable.ic_maize_2,
            R.drawable.ic_maize_3,
            R.drawable.ic_maize_4,
            R.drawable.ic_maize_5,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMaizePerformanceActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        database = AppDatabase.getDatabase(context);

        toolbar = binding.toolbar;
        recyclerView = binding.rootYieldRecycler;
        viewPos = binding.coordinatorLayout;

        exceptionTitle = binding.exceptionTitle;
        btnFinish = binding.twoButtons.btnFinish;
        btnCancel = binding.twoButtons.btnCancel;

        savedMaizePerformance = database.maizePerformanceDao().findOne();
        if (savedMaizePerformance != null) {
            selectedPerformanceValue = savedMaizePerformance.getMaizePerformance();
        }
        initToolbar();
        initComponent();
    }

    @Override
    public void onBackPressed() {
        validate(true);
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_maize_performance));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> validate(false));
    }

    @Override
    protected void initComponent() {

        poorSoil = getString(R.string.lbl_maize_performance_poor);
        richSoil = getString(R.string.lbl_maize_performance_rich);


        btnFinish.setOnClickListener(view -> validate(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.addItemDecoration(new SpacingItemDecoration(1, Tools.dpToPx(this, 3), true));
        recyclerView.setHasFixedSize(true);

        List<MaizePerformance> items = new ArrayList<>();

        items.add(createPerformanceObject(performanceImages[0], poorSoil, "1", "50", getString(R.string.lbl_knee_height)));
        items.add(createPerformanceObject(performanceImages[1], null, "2", "150", getString(R.string.lbl_chest_height)));
        items.add(createPerformanceObject(performanceImages[2], null, "3", "yellow", getString(R.string.lbl_yellowish_leaves)));
        items.add(createPerformanceObject(performanceImages[3], null, "4", "green", getString(R.string.lbl_green_leaves)));
        items.add(createPerformanceObject(performanceImages[4], richSoil, "5", "dark green", getString(R.string.lbl_dark_green_leaves)));

        mAdapter = new MaizePerformanceAdapter(this, items, ItemAnimation.FADE_IN);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setItems(selectedPerformanceValue, items);


        mAdapter.setOnItemClickListener((view, clickedMaizePerformance, position) -> {
            try {
                //show a popup dialog here
                Bundle arguments = new Bundle();
                arguments.putParcelable(MaizePerformanceDialogFragment.PERFORMANCE_DATA, clickedMaizePerformance);

                MaizePerformanceDialogFragment rootYieldDialogFragment = new MaizePerformanceDialogFragment(context);
                rootYieldDialogFragment.setArguments(arguments);

                rootYieldDialogFragment.setOnDismissListener((yield, yieldConfirmed) -> {
                    if (yieldConfirmed) {
                        if (savedMaizePerformance == null) {
                            savedMaizePerformance = new MaizePerformance();
                        }
                        String maizePerformance = yield.getMaizePerformance();
                        selectedPerformanceValue = yield.getPerformanceValue();
                        savedMaizePerformance.setMaizePerformance(maizePerformance);
                        savedMaizePerformance.setPerformanceValue(selectedPerformanceValue);
                        database.maizePerformanceDao().insert(savedMaizePerformance);

                        mAdapter.setActiveRowIndex(position);
                        maizePerformanceValue = selectedPerformanceValue;
                    }
                    mAdapter.setItems(selectedPerformanceValue, items);
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

            } catch (Exception ex) {
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }

        });
    }

    private MaizePerformance createPerformanceObject(Integer yieldImage, String performanceDesc, String performanceValue, String maizePerformance, String maizePerformanceLabel) {
        MaizePerformance performance = new MaizePerformance();
        performance.setImageId(yieldImage);
        performance.setMaizePerformanceDesc(performanceDesc);
        performance.setPerformanceValue(performanceValue);
        performance.setMaizePerformance(maizePerformance);
        performance.setMaizePerformanceLabel(maizePerformanceLabel);
        return performance;
    }

    @Override
    protected void validate(boolean backPressed) {

        if (Strings.isEmptyOrWhitespace(selectedPerformanceValue)) {
            showCustomWarningDialog(getString(R.string.lbl_invalid_selection), getString(R.string.lbl_maize_performance_prompt));
            return;
        }
        database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.MAIZE_PERFORMANCE.name(), true));
        closeActivity(backPressed);
    }

}
