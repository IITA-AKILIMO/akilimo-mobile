package com.iita.akilimo.views.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecommendationAdapter;
import com.iita.akilimo.dao.AppDatabase;
import com.iita.akilimo.databinding.ActivityDstRecomendationBinding;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IRecommendationCallBack;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.mappers.ComputedResponse;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.rest.recommendation.RecommendationResponse;
import com.iita.akilimo.rest.request.RecommendationRequest;
import com.iita.akilimo.utils.BuildComputeData;
import com.iita.akilimo.utils.Tools;
import com.iita.akilimo.views.fragments.dialog.RecommendationChannelDialog;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DstRecommendationActivity extends BaseActivity implements IRecommendationCallBack {
    public static final String REC_TAG = DstRecommendationActivity.class.getSimpleName();

    Toolbar toolbar;
    RecyclerView recyclerView;
    FloatingActionButton fabRetry;
    ImageView errorImage;
    TextView errorLabel;
    LinearLayout lyt_progress;

    ActivityDstRecomendationBinding binding;
    Activity activity;
    RecommendationRequest recData;
    RecommendationAdapter recAdapter;
    List<ComputedResponse> recList;
    ProfileInfo profileInfo;
    RecommendationChannelDialog recommendationChannelDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDstRecomendationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        activity = this;
        database = AppDatabase.getDatabase(context);

        toolbar = binding.toolbarLayout.toolbar;
        recyclerView = binding.recyclerView;
        fabRetry = binding.fabRetry;
        errorImage = binding.errorImage;
        errorLabel = binding.errorLabel;
        lyt_progress = binding.lytProgress;

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.lbl_recommendations));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> {
            closeActivity(false);
        });
    }

    @Override
    protected void initComponent() {
        recyclerView.setVisibility(View.GONE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recAdapter = new RecommendationAdapter();
        profileInfo = database.profileInfoDao().findOne();

        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);
        recyclerView.setVisibility(View.GONE);
        errorLabel.setVisibility(View.GONE);
        errorImage.setVisibility(View.GONE);


        fabRetry.setOnClickListener(view -> {
            if (profileInfo != null) {
                displayDialog(profileInfo);
            }
        });

        displayDialog(profileInfo);
    }

    private void buildRecommendationData() {
        BuildComputeData buildComputeData = new BuildComputeData(activity);
        recData = buildComputeData.buildRecommendationReq();
        loadingAndDisplayContent();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    private void displayDialog(ProfileInfo profileInfo) {
        if (profileInfo != null) {
            recommendationChannelDialog = new RecommendationChannelDialog(this, profileInfo);
            recommendationChannelDialog.show(getSupportFragmentManager(), RecommendationChannelDialog.TAG);
        } else {
            //show a message
            errorLabel.setText(R.string.lbl_no_profile_info);
            lyt_progress.setVisibility(View.GONE);
            errorImage.setVisibility(View.VISIBLE);
            errorLabel.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDataReceived(@NotNull ProfileInfo profileInfo) {

        //update the profile info
        database.profileInfoDao().update(profileInfo);
        buildRecommendationData();
    }

    @Override
    public void onDismiss() {
        closeActivity(false);
    }

    private void loadingAndDisplayContent() {
        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);
        recyclerView.setVisibility(View.GONE);
        errorLabel.setVisibility(View.GONE);
        errorImage.setVisibility(View.GONE);

        final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        final RestService restService = RestService.getInstance(queue, activity);
        final RestParameters restParameters = new RestParameters(
                "v2/recommendations",
                countryCode
        );
        restParameters.setInitialTimeout(120000);
        //restParameters.setLocale(getCurrentLocale());
        restService.setParameters(restParameters);


        //print recommendation data here

        JSONObject data = Tools.prepareRecommendationJson(recData);
        restService.postJsonObject(data, new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(@NonNull String jsonStringResult) {

            }

            @Override
            public void onSuccessJsonArr(@NonNull JSONArray jsonArray) {

            }

            @Override
            public void onSuccessJsonObject(@NonNull JSONObject jsonObject) {
                lyt_progress.setVisibility(View.GONE);
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    RecommendationResponse recommendationResponse = objectMapper.readValue(jsonObject.toString(), RecommendationResponse.class);
                    recList = initializeData(recommendationResponse);
                    recAdapter.setData(recList);
                    recyclerView.setAdapter(recAdapter);
                    recyclerView.setVisibility(View.VISIBLE);

                } catch (Exception ex) {
                    lyt_progress.setVisibility(View.GONE);
                    errorImage.setVisibility(View.VISIBLE);
                    errorLabel.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    FirebaseCrashlytics.getInstance().log(ex.getMessage());
                    FirebaseCrashlytics.getInstance().recordException(ex);
                }
            }

            @Override
            public void onError(@NonNull VolleyError volleyError) {
                if (volleyError instanceof TimeoutError) {
                    // your stuf
                }else{

                }
                lyt_progress.setVisibility(View.GONE);
                errorImage.setVisibility(View.VISIBLE);
                errorLabel.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private List<ComputedResponse> initializeData(@NonNull RecommendationResponse recommendationResponse) {
        List<ComputedResponse> recList = new ArrayList<>();

        String FR = recommendationResponse.getFertilizerRecText();
        String IC = recommendationResponse.getInterCroppingRecText();
        String PP = recommendationResponse.getPlantingPracticeRecText();
        String SP = recommendationResponse.getScheduledPlantingRect();

        ComputedResponse computedResponse;

        if (!Strings.isEmptyOrWhitespace(FR)) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject(getString(R.string.lbl_fertilizer_rec), FR));
        }

        if (!Strings.isEmptyOrWhitespace(IC)) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject(getString(R.string.lbl_intercrop_rec), IC));
        }

        if (!Strings.isEmptyOrWhitespace(PP)) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject(getString(R.string.lbl_planting_practices_rec), PP));
        }

        if (!Strings.isEmptyOrWhitespace(SP)) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject(getString(R.string.lbl_scheduled_planting_rec), SP));
        }

        if (recList.size() <= 0) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject(getString(R.string.lbl_no_recommendations), getString(R.string.lbl_no_recommendations_prompt)));
        }
        return recList;
    }

}
