package com.iita.akilimo.views.activities;

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
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.IntercropFertilizerGridAdapter;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.Fertilizer;
import com.iita.akilimo.models.FertilizerPrices;
import com.iita.akilimo.models.InterCropFertilizer;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.FertilizerList;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.FertilizerPriceDialogFragment;
import com.iita.akilimo.widget.SpacingItemDecoration;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class IntercropFertilizersActivity extends BaseActivity {

    public static String useCaseTag = "useCase";
    public static String interCropTag = "interCrop";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.availableFertilizers)
    RecyclerView recyclerView;

    @BindString(R.string.title_activity_fertilizer_choice)
    String headerTitleText;

    @BindView(R.id.lyt_progress)
    LinearLayout lyt_progress;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.btnFinish)
    AppCompatButton btnSave;

    @BindView(R.id.btnCancel)
    AppCompatButton btnCancel;

    @BindView(R.id.btnRetry)
    AppCompatButton btnRetry;

    @BindView(R.id.errorImage)
    ImageView errorImage;
    @BindView(R.id.errorLabel)
    TextView errorLabel;

    private List<InterCropFertilizer> availableFertilizersList = new ArrayList<>();
    private List<InterCropFertilizer> selectedFertilizers = new ArrayList<>();
    private List<InterCropFertilizer> fertilizerTypesList = new ArrayList<>();
    private List<FertilizerPrices> fertilizerPricesList = new ArrayList<>();

    private IntercropFertilizerGridAdapter mAdapter;
    private int minSelection = 1;
    private ModelMapper modelMapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizers);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(context);
        modelMapper = new ModelMapper();

        Intent intent = getIntent();
        if (intent != null) {
            useCase = intent.getParcelableExtra(useCaseTag);
        }

        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo != null) {
            countryCode = mandatoryInfo.getCountryCode();
        }
        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(headerTitleText);
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
        btnSave.setEnabled(false);
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new SpacingItemDecoration(2, Tools.dpToPx(this, 3), true));
        recyclerView.setHasFixedSize(true);
        mAdapter = new IntercropFertilizerGridAdapter(context);
        recyclerView.setAdapter(mAdapter);


        mAdapter.setOnItemClickListener((view, clickedFertilizer, position) -> {
            mAdapter.setActiveRowIndex(position);
            Fertilizer selectedType = objectBoxEntityProcessor.getSavedInterCropFertilizer(clickedFertilizer.getType(), countryCode, useCase);
            if (selectedType == null) {
                selectedType = clickedFertilizer;
            }
            //let us open the price dialog now
            List<InterCropFertilizer> cleanedFertilizers = selectedFertilizers;
            selectedType.setCountryCode(countryCode);
            Bundle arguments = new Bundle();
            arguments.putParcelable(FertilizerPriceDialogFragment.FERTILIZER_TYPE, selectedType);

            FertilizerPriceDialogFragment priceDialogFragment = new FertilizerPriceDialogFragment();
            priceDialogFragment.setArguments(arguments);

            priceDialogFragment.setOnDismissListener((priceSpecified, fertilizer, removeSelected) -> {
                InterCropFertilizer interCropFertilizer = modelMapper.map(fertilizer, InterCropFertilizer.class);
                if ((priceSpecified || removeSelected)) {
                    long id = objectBoxEntityProcessor.saveSelectedIntercropFertilizer(interCropFertilizer);
                    if (id > 0) {
                        if (removeSelected) {
                            selectedFertilizers = FertilizerList.INSTANCE.removeIntercropFertilizerByType(cleanedFertilizers, fertilizer.getType());
                        } else {
                            selectedFertilizers.add(interCropFertilizer);
                        }
                        //refresh the adapter and data set
                        validate(false);
                    }
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
        btnSave.setOnClickListener(view -> closeActivity(false));
        btnCancel.setOnClickListener(view -> closeActivity(false));
    }

    @Override
    protected void validate(boolean backPressed) {
        if (mAdapter != null) {
            availableFertilizersList = objectBoxEntityProcessor.getAvailableIntercropFertilizersByCountryUseCase(countryCode, useCase);
            mAdapter.setItems(availableFertilizersList);
            btnSave.setEnabled(isMinSelected());
        }
    }

    private void initializeFertilizers() {

        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);
        recyclerView.setVisibility(View.GONE);
        errorLabel.setVisibility(View.GONE);
        errorImage.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        final RestParameters restParameters = new RestParameters(
                "v2/fertilizers",
                countryCode
        );

        final RestService restService = RestService.getInstance(queue, this);
        restService.setParameters(restParameters);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {

            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    availableFertilizersList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<InterCropFertilizer>>() {
                    });
                    objectBoxEntityProcessor.saveIntercropFertilizerList(availableFertilizersList);
                    initializeFertilizerPriceList();
                } catch (Exception ex) {
                    lyt_progress.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    errorLabel.setVisibility(View.VISIBLE);
                    errorImage.setVisibility(View.VISIBLE);
                    btnRetry.setVisibility(View.VISIBLE);

                    Crashlytics.log(Log.ERROR, LOG_TAG, "Error saving price list");
                    Crashlytics.logException(ex);
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(@NotNull VolleyError volleyError) {
                lyt_progress.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorLabel.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);

                Toast.makeText(context, "Unable to load fertilizer list", Toast.LENGTH_LONG).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, "Fertilizer list not able to load");
                Crashlytics.logException(volleyError);
            }
        });
    }

    private void initializeFertilizerPriceList() {
        final RestParameters restParameters = new RestParameters(
                "v2/fertilizer-prices",
                countryCode
        );
        final RestService restService = RestService.getInstance(queue, this);

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
                    fertilizerPricesList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<FertilizerPrices>>() {
                    });
                    objectBoxEntityProcessor.saveFertilizerPrices(fertilizerPricesList);
                    validate(false);
                    recyclerView.setVisibility(View.VISIBLE);
                } catch (Exception ex) {
                    lyt_progress.setVisibility(View.GONE);
                    errorImage.setVisibility(View.VISIBLE);
                    errorLabel.setVisibility(View.VISIBLE);
                    btnRetry.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Crashlytics.log(Log.ERROR, LOG_TAG, "Error saving fertilizer price list");
                    Crashlytics.logException(ex);
                }

            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(VolleyError volleyError) {
                lyt_progress.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                errorLabel.setVisibility(View.VISIBLE);
                errorImage.setVisibility(View.VISIBLE);
                btnRetry.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean isMinSelected() {
        int count = objectBoxEntityProcessor.getSelectedIntercropFertilizers(countryCode, useCase).size();
        if (count < minSelection) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, String.format(Locale.US, context.getString(R.string.lbl_min_selection), minSelection), Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        return count >= minSelection;
    }
}