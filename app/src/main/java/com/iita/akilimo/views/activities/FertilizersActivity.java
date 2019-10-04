package com.iita.akilimo.views.activities;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.AvailableFertilizersAdapter;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.Fertilizer;
import com.iita.akilimo.models.FertilizerPrices;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.FertilizerList;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;
import com.iita.akilimo.views.fragments.dialog.FertilizerPriceDialogFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FertilizersActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.availableFertilizers)
    RecyclerView recyclerView;

    @BindView(R.id.swipeRefreshFertilizers)
    SwipeRefreshLayout mSwipeRefreshLayout;

    //    @BindString(R.string.title_activity_fertilizer_choice)
    String headerTitleText = "Available fertilizers";
    //    @BindString(R.string.lbl_choose_fertilizers)
    String headerSubTitleText = "Select fertilizers that are available at your local agro-dealer";


    private boolean skipRecommendation;
    private List<Fertilizer> availableFertilizersList = new ArrayList<>();
    private List<Fertilizer> selectedFertilizers = new ArrayList<>();
    private List<Fertilizer> fertilizerTypesList = new ArrayList<>();
    private List<FertilizerPrices> fertilizerPricesList = new ArrayList<>();

    private RecyclerView.LayoutManager linearLayoutManager;
    private ProgressBar progressDialog;
    private AvailableFertilizersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fertilizers);
        ButterKnife.bind(this);
        context = this;
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
        queue = Volley.newRequestQueue(context);

        initToolbar();
        initComponent();
    }


    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(headerTitleText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Tools.setSystemBarColor(this, R.color.deep_orange_900);
        toolbar.setNavigationOnClickListener(v -> {
            // back button pressed
            closeActivity(false);
        });
    }

    @Override
    protected void initComponent() {
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new AvailableFertilizersAdapter(context);
        fetchAvailableFertilizers();
        fetchFertilizerPriceList();

        adapter.setClickListener((view, position, isLongClick) -> {
            Fertilizer unsavedType = availableFertilizersList.get(position);
            Fertilizer selectedType = objectBoxEntityProcessor.getSavedFertilizer(unsavedType.getType(), countryCode);
            if (selectedType == null) {
                selectedType = unsavedType;
            }
            if (!isLongClick) {
                List<Fertilizer> cleanedFertilizers = selectedFertilizers;
                selectedType.setCountryCode(countryCode);
                Bundle arguments = new Bundle();
                arguments.putParcelable(FertilizerPriceDialogFragment.FERTILIZER_TYPE, selectedType);
                FertilizerPriceDialogFragment customDialogFragment = new FertilizerPriceDialogFragment();
                customDialogFragment.setArguments(arguments);

                customDialogFragment.setOnDismissListener((priceSpecified, fertilizer, removeSelected) -> {
                    if (fertilizer != null && (priceSpecified || removeSelected)) {
                        long id = objectBoxEntityProcessor.saveUpdateFertilizer(fertilizer);
                        if (id > 0) {
                            if (removeSelected) {
                                selectedFertilizers = FertilizerList.removeFertilizerByType(cleanedFertilizers, fertilizer.getType());
                            } else {
                                selectedFertilizers.add(fertilizer);
                            }
                            //refresh the adapter and data set
                            fertilizerTypesList = objectBoxEntityProcessor.getAvailableFertilizersByCountry(countryCode);
                            adapter.setItems(fertilizerTypesList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyItemChanged(position);
                            adapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(position);
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
                    customDialogFragment.show(getSupportFragmentManager(), FertilizerPriceDialogFragment.ARG_ITEM_ID);
                }
            }
        });
    }

    private void fetchAvailableFertilizers() {
        final RestService restService = RestService.getInstance(queue, this);
        restService.setEndpoint("v2/fertilizers");
        restService.setCountryCode(countryCode);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {

            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    availableFertilizersList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<Fertilizer>>() {
                    });
                    adapter.setItems(availableFertilizersList);
                    recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
                    recyclerView.setAdapter(adapter);
                    objectBoxEntityProcessor.saveFertilizerList(availableFertilizersList);

                } catch (Exception ex) {
                    Crashlytics.log(ex.getMessage());
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {

            }

            @Override
            public void onError(VolleyError volleyError) {

            }
        });
    }

    private void fetchFertilizerPriceList() {
        final RestService restService = RestService.getInstance(queue, this);

        restService.setEndpoint("v2/fertilizer-prices");
        restService.setCountryCode(countryCode);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {
            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    fertilizerPricesList = objectMapper.readValue(jsonArray.toString(), new TypeReference<List<FertilizerPrices>>() {
                    });
                    objectBoxEntityProcessor.saveFertilizerPrices(fertilizerPricesList);
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(VolleyError volleyError) {
            }
        });
    }

}
