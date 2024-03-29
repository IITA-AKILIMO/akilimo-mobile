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
import com.akilimo.mobile.adapters.IntercropFertilizerGridAdapter;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityFertilizersBinding;
import com.akilimo.mobile.entities.AdviceStatus;
import com.akilimo.mobile.entities.FertilizerPrice;
import com.akilimo.mobile.entities.InterCropFertilizer;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.interfaces.IVolleyCallback;
import com.akilimo.mobile.rest.RestParameters;
import com.akilimo.mobile.rest.RestService;
import com.akilimo.mobile.utils.FertilizerList;
import com.akilimo.mobile.utils.Tools;
import com.akilimo.mobile.utils.enums.EnumAdviceTasks;
import com.akilimo.mobile.views.fragments.dialog.FertilizerPriceDialogFragment;
import com.akilimo.mobile.views.fragments.dialog.IntercropFertilizerPriceDialogFragment;
import com.akilimo.mobile.widget.SpacingItemDecoration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.sentry.Sentry;

public class IntercropFertilizersActivity extends BaseActivity {

    public static String useCaseTag = "useCase";
    public static String interCropTag = "interCrop";
    private String TAG = BaseActivity.class.getSimpleName();


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


    private List<InterCropFertilizer> availableFertilizersList = new ArrayList<>();
    private List<InterCropFertilizer> selectedFertilizers = new ArrayList<>();
    private List<InterCropFertilizer> fertilizerTypesList = new ArrayList<>();
    private List<FertilizerPrice> fertilizerPricesList = new ArrayList<>();

    private IntercropFertilizerGridAdapter mAdapter;
    private int minSelection = 1;
    private ModelMapper modelMapper;

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
        modelMapper = new ModelMapper();

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
        btnSave.setText(context.getString(R.string.lbl_finish));
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 3), true));
        recyclerView.setHasFixedSize(true);
        mAdapter = new IntercropFertilizerGridAdapter(context);
        recyclerView.setAdapter(mAdapter);


        mAdapter.setOnItemClickListener((view, clickedFertilizer, position) -> {
            mAdapter.setActiveRowIndex(position);
            InterCropFertilizer selectedType = database.interCropFertilizerDao().findByTypeCountryAndUseCase(clickedFertilizer.getFertilizerType(), countryCode, enumUseCase.name());
            if (selectedType == null) {
                selectedType = clickedFertilizer;
            }
            //let us open the price dialog now
            List<InterCropFertilizer> cleanedFertilizers = selectedFertilizers;

            selectedType.setCountryCode(countryCode);

            Bundle arguments = new Bundle();
            arguments.putParcelable(FertilizerPriceDialogFragment.FERTILIZER_TYPE, selectedType);

            IntercropFertilizerPriceDialogFragment priceDialogFragment = new IntercropFertilizerPriceDialogFragment(context);
            priceDialogFragment.setArguments(arguments);

            priceDialogFragment.setOnDismissListener((priceSpecified, fertilizer, removeSelected) -> {
                InterCropFertilizer interCropFertilizer = modelMapper.map(fertilizer, InterCropFertilizer.class);
                database.interCropFertilizerDao().update(fertilizer);
                if ((priceSpecified || removeSelected)) {
                    if (removeSelected) {
                        selectedFertilizers = FertilizerList.INSTANCE.removeIntercropFertilizerByType(cleanedFertilizers, fertilizer.getFertilizerType());
                    } else {
                        selectedFertilizers.add(interCropFertilizer);
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
            database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIM.name(), isMinSelected()));
            database.adviceStatusDao().insert(new AdviceStatus(EnumAdviceTasks.AVAILABLE_FERTILIZERS_CIS.name(), isMinSelected()));
            if (isMinSelected()) {
                closeActivity(false);
            }
        });
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    protected void validate(boolean backPressed) {
        if (mAdapter != null) {
            availableFertilizersList = database.interCropFertilizerDao().findAllByCountryAndUseCase(countryCode, enumUseCase.name());
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

        final RestParameters restParameters = new RestParameters("v2/fertilizers",countryCode );
        final RestService restService = RestService.getInstance(queue, this);
        restService.setParameters(restParameters);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(@NotNull String jsonStringResult) {

            }

            @Override
            public void onSuccessJsonArr(@NotNull JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                List<InterCropFertilizer> deletionList = new ArrayList<>();
                try {
                    availableFertilizersList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<InterCropFertilizer>>() {
                    });

                    List<InterCropFertilizer> savedList = database.interCropFertilizerDao().findAllByCountry(countryCode);
                    //save fertilizers here
                    if (availableFertilizersList.size() > 0) {
                        if (savedList.size() > 0) {
                            for (InterCropFertilizer savedFertilizer : savedList) {
                                // Loop arrayList1 items
                                boolean found = false;
                                for (InterCropFertilizer latestFertilizer : availableFertilizersList) {
                                    InterCropFertilizer updateFertilizer = database.interCropFertilizerDao().findByType(latestFertilizer.getFertilizerType());
                                    if (updateFertilizer != null) {
                                        updateFertilizer.setAvailable(latestFertilizer.getAvailable());
                                        database.interCropFertilizerDao().update(updateFertilizer);
                                    } else {
                                        database.interCropFertilizerDao().insert(latestFertilizer);
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
                            database.interCropFertilizerDao().insertAll(availableFertilizersList);
                        }
                    }
                    database.interCropFertilizerDao().deleteFertilizerByList(deletionList);
                    for (InterCropFertilizer fertilizer : availableFertilizersList) {
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
                    recyclerView.setVisibility(View.GONE);

                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    Sentry.captureException(ex);
                }
            }

            @Override
            public void onSuccessJsonObject(@NotNull JSONObject jsonObject) {
            }

            @Override
            public void onError(@NotNull VolleyError ex) {
                lyt_progress.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorLabel.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);

                Toast.makeText(context, getString(R.string.lbl_fertilizer_load_error), Toast.LENGTH_LONG).show();
                Sentry.captureException(ex);
            }
        });
    }

    private void loadFertilizerPrices(String fertilizerKey) {
        final RestParameters restParameters = new RestParameters(
                String.format("v2/fertilizer-prices/%s", fertilizerKey), countryCode
        );
        final RestService restService = RestService.getInstance(queue, this);
        restParameters.setInitialTimeout(5000);
        restService.setParameters(restParameters);
        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {
            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                lyt_progress.setVisibility(View.GONE);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    //check if fertilizer price list is already populated
                    fertilizerPricesList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<FertilizerPrice>>() {
                    });

                    if (fertilizerPricesList.size() > 0) {
                        database.fertilizerPriceDao().insertAll(fertilizerPricesList);
                    }
                    validate(false);
                    recyclerView.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    lyt_progress.setVisibility(View.GONE);
                    errorImage.setVisibility(View.VISIBLE);
                    errorLabel.setVisibility(View.VISIBLE);
                    btnRetry.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    Sentry.captureException(ex);
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
        int count = database.interCropFertilizerDao().findAllSelectedByCountryAndUseCase(countryCode, enumUseCase.name()).size();
        if (count < minSelection) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, String.format(Locale.US, context.getString(R.string.lbl_min_selection), minSelection), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        return count >= minSelection;
    }
}
