package com.akilimo.mobile.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.akilimo.mobile.R;
import com.akilimo.mobile.adapters.FertilizerGridAdapter;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityFertilizersBinding;
import com.akilimo.mobile.entities.AdviceStatus;
import com.akilimo.mobile.entities.Fertilizer;
import com.akilimo.mobile.entities.FertilizerPrice;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.interfaces.IVolleyCallback;
import com.akilimo.mobile.rest.RestParameters;
import com.akilimo.mobile.rest.RestService;
import com.akilimo.mobile.utils.FertilizerList;
import com.akilimo.mobile.utils.Tools;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.akilimo.mobile.views.fragments.dialog.FertilizerPriceDialogFragment;
import com.akilimo.mobile.widget.SpacingItemDecoration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.sentry.Sentry;


public class FertilizersActivity extends BaseActivity {

    public static String useCaseTag = "useCase";

    Toolbar toolbar;
    RecyclerView recyclerView;
    LinearLayout lyt_progress;
    CoordinatorLayout coordinatorLayout;
    AppCompatButton btnSave;
    AppCompatButton btnCancel;
    AppCompatButton btnRetry;
    ImageView errorImage;
    TextView errorLabel;
    ActivityFertilizersBinding binding;


    private List<Fertilizer> availableFertilizersList = new ArrayList<>();
    private List<Fertilizer> selectedFertilizers = new ArrayList<>();
    private List<Fertilizer> fertilizerTypesList = new ArrayList<>();
    private List<FertilizerPrice> fertilizerPricesList = new ArrayList<>();

    private FertilizerGridAdapter mAdapter;
    int minSelection = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFertilizersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.availableFertilizers;
        lyt_progress = binding.lytProgress;
        coordinatorLayout = binding.coordinatorLayout;
        btnSave = binding.twoButtons.btnFinish;
        btnCancel = binding.twoButtons.btnCancel;
        btnRetry = binding.btnRetry;
        errorImage = binding.errorImage;
        errorLabel = binding.errorLabel;

        database = AppDatabase.getDatabase(context);
        queue = Volley.newRequestQueue(context);

        Intent intent = getIntent();
        if (intent != null) {
            enumUseCase = intent.getParcelableExtra(useCaseTag);
        }

        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            currency = profileInfo.getCurrency();
        }

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_fertilizer_choice));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> validateInput(false));
    }

    @Override
    public void onBackPressed() {
        validateInput(true);
    }

    private void validateInput(boolean backPressed) {
        if (isMinSelected()) {
            closeActivity(backPressed);
        }
    }


    @Override
    protected void initComponent() {
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 3), true));
        recyclerView.setHasFixedSize(true);
        mAdapter = new FertilizerGridAdapter(context);
        recyclerView.setAdapter(mAdapter);

        btnSave.setText(context.getString(R.string.lbl_finish));


        mAdapter.setOnItemClickListener((view, clickedFertilizer, position) -> {
            mAdapter.setActiveRowIndex(position);
            Fertilizer selectedType = database.fertilizerDao().findOneByTypeAndCountry(clickedFertilizer.getFertilizerType(), countryCode);
            if (selectedType == null) {
                selectedType = clickedFertilizer;
            }
            //let us open the price dialog now
            List<Fertilizer> cleanedFertilizers = selectedFertilizers;
            selectedType.setCountryCode(countryCode);

            Bundle arguments = new Bundle();
            arguments.putParcelable(FertilizerPriceDialogFragment.FERTILIZER_TYPE, selectedType);

            FertilizerPriceDialogFragment priceDialogFragment = new FertilizerPriceDialogFragment(context);
            priceDialogFragment.setArguments(arguments);

            priceDialogFragment.setOnDismissListener((priceSpecified, fertilizer, removeSelected) -> {
                if ((priceSpecified || removeSelected)) {
                    database.fertilizerDao().update(fertilizer);
                    if (removeSelected) {
                        selectedFertilizers = FertilizerList.INSTANCE.removeFertilizerByType(cleanedFertilizers, fertilizer.getFertilizerType());
                    } else {
                        selectedFertilizers.add(fertilizer);
                    }
                    //refresh the adapter and data set
                    validate(false);
                }
            });


            FragmentTransaction fragmentTransaction;
            if (getFragmentManager() != null) {
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                Fragment prev = getSupportFragmentManager().findFragmentByTag(FertilizerPriceDialogFragment.ARG_ITEM_ID);
                if (prev != null) {
                    fragmentTransaction.remove(prev);
                }
                fragmentTransaction.addToBackStack(null);
                priceDialogFragment.show(getSupportFragmentManager(), FertilizerPriceDialogFragment.ARG_ITEM_ID);
            }
        });

        initializeFertilizers();

        btnRetry.setOnClickListener(view -> initializeFertilizers());
        btnSave.setOnClickListener(view -> {
            database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS.name(), isMinSelected()));
            if (isMinSelected()) {
                closeActivity(false);
            }
        });
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    protected void validate(boolean backPressed) {
        availableFertilizersList = database.fertilizerDao().findAllByCountry(countryCode);
        if (mAdapter != null) {
            mAdapter.setItems(availableFertilizersList);
        }
    }

    private void initializeFertilizers() {

        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);
        recyclerView.setVisibility(View.GONE);
        errorLabel.setVisibility(View.GONE);
        errorImage.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        final RestParameters restParameters = new RestParameters("v2/fertilizers", countryCode);
        final RestService restService = RestService.getInstance(queue, this);
        restService.setParameters(restParameters);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {

            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Fertilizer> deletionList = new ArrayList<>();
                try {
                    availableFertilizersList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<Fertilizer>>() {
                    });
                    List<Fertilizer> savedList = database.fertilizerDao().findAllByCountry(countryCode);
                    //save fertilizers here
                    if (availableFertilizersList.size() > 0) {
                        if (savedList.size() > 0) {
                            for (Fertilizer savedFertilizer : savedList) {
                                // Loop arrayList1 items
                                boolean found = false;
                                for (Fertilizer latestFertilizer : availableFertilizersList) {
                                    Fertilizer updateFertilizer = database.fertilizerDao().findByType(latestFertilizer.getFertilizerType());
                                    if (updateFertilizer != null) {
                                        updateFertilizer.setAvailable(latestFertilizer.getAvailable());
                                        database.fertilizerDao().update(updateFertilizer);
                                    } else {
                                        database.fertilizerDao().insert(latestFertilizer);
                                    }
                                    if (latestFertilizer.getFertilizerType().equals(savedFertilizer.getFertilizerType())) {
                                        found = true;
                                    }
                                }
                                if (!found) {
                                    deletionList.add(savedFertilizer);
                                }
                            }
                        } else {
                            database.fertilizerDao().insertAll(availableFertilizersList);
                        }
                    }
                    database.fertilizerDao().deleteFertilizerByList(deletionList);
                    for (Fertilizer fertilizer : availableFertilizersList) {
                        loadFertilizerPrices(fertilizer.getFertilizerKey());
                    }
                    validate(false);
                    recyclerView.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    lyt_progress.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    errorLabel.setVisibility(View.VISIBLE);
                    errorImage.setVisibility(View.VISIBLE);
                    btnRetry.setVisibility(View.VISIBLE);
                    Sentry.captureException(ex);
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(@NotNull VolleyError ex) {
                lyt_progress.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorLabel.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);

                Toast.makeText(context, getString(R.string.lbl_fertilizer_load_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadFertilizerPrices(String fertilizerKey) {
        final RestService restService = RestService.getInstance(queue, this);
        final RestParameters restParameters = new RestParameters(
                String.format("v2/fertilizer-prices/%s", fertilizerKey), countryCode
        );
        restParameters.setInitialTimeout(5000);
        restService.setParameters(restParameters);
        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {
            }

            @Override
            public void onSuccessJsonArr(@NotNull JSONArray jsonArray) {
                lyt_progress.setVisibility(View.GONE);
                ObjectMapper objectMapper = new ObjectMapper();
                try {

                    List<FertilizerPrice> myPrices = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<FertilizerPrice>>() {
                    });

                    if (myPrices.size() > 0) {
                        database.fertilizerPriceDao().insertAll(myPrices);
                    }
                } catch (Exception ex) {
                    lyt_progress.setVisibility(View.GONE);
                    errorImage.setVisibility(View.VISIBLE);
                    errorLabel.setVisibility(View.VISIBLE);
                    btnRetry.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(VolleyError ex) {
                lyt_progress.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorLabel.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isMinSelected() {
        int count = database.fertilizerDao().findAllSelectedByCountry(countryCode).size();
        if (count < minSelection) {
            Snackbar snackbar = Snackbar.make(lyt_progress, String.format(Locale.US, context.getString(R.string.lbl_min_selection), minSelection), Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
        return count >= minSelection;
    }
}
