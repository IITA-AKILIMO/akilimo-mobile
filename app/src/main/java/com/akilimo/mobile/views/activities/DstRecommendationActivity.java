package com.akilimo.mobile.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.akilimo.mobile.R;
import com.akilimo.mobile.adapters.RecommendationAdapter;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.databinding.ActivityDstRecomendationBinding;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseActivity;
import com.akilimo.mobile.interfaces.IRecommendationCallBack;
import com.akilimo.mobile.interfaces.IVolleyCallback;
import com.akilimo.mobile.mappers.ComputedResponse;
import com.akilimo.mobile.rest.RestParameters;
import com.akilimo.mobile.rest.RestService;
import com.akilimo.mobile.rest.recommendation.RecommendationResponse;
import com.akilimo.mobile.rest.request.RecommendationRequest;
import com.akilimo.mobile.utils.BuildComputeData;
import com.akilimo.mobile.utils.Tools;
import com.akilimo.mobile.views.fragments.dialog.RecommendationChannelDialog;

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
    AppCompatButton btnFeedback;
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
        btnFeedback = binding.feedbackButton.btnGetRecommendation;
        errorImage = binding.errorImage;
        errorLabel = binding.errorLabel;
        lyt_progress = binding.lytProgress;

        btnFeedback.setText(R.string.lbl_provide_feedback);
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

        btnFeedback.setOnClickListener(view -> {
            //launch the feedback dialog
            Intent intent = new Intent(this, MySurveyActivity.class);
            startActivityForResult(intent, 2);// Activity is started with requestCode 2
        });

        displayDialog(profileInfo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            String message = data.getStringExtra("MESSAGE");
        }
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
            recommendationChannelDialog.setCancelable(false);
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

        JSONObject data = Tools.prepareJsonObject(recData);
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
                    Crashlytics.log(Log.ERROR, REC_TAG, ex.getMessage());
                    Crashlytics.logException(ex);
                }
            }

            @Override
            public void onError(@NonNull VolleyError volleyError) {
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
