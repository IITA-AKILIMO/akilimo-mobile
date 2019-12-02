package com.iita.akilimo.views.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.RecommendationAdapter;
import com.iita.akilimo.inherit.BaseActivity;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.mappers.ComputedResponse;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.rest.recommendation.RecommendationResponse;
import com.iita.akilimo.rest.request.RecommendationRequest;
import com.iita.akilimo.utils.BuildComputeData;
import com.iita.akilimo.utils.Tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class DstRecommendationActivity extends BaseActivity {
    public static final String REC_TAG = "REC";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.fabRetry)
    FloatingActionButton fabRetry;

    @BindView(R.id.errorImage)
    ImageView errorImage;

    @BindView(R.id.errorLabel)
    TextView errorLabel;

    @BindView(R.id.lyt_progress)
    LinearLayout lyt_progress;

    @BindString(R.string.lbl_recommendations)
    String activityTitle;

    Activity activity;
    RecommendationRequest recData;
    RecommendationAdapter recAdapter;
    List<ComputedResponse> recList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dst_recomendation);
        context = this;
        activity = this;
        ButterKnife.bind(this);
        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(activityTitle);
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

        fabRetry.setOnClickListener(view -> loadingAndDisplayContent());

        recAdapter = new RecommendationAdapter();

        BuildComputeData buildComputeData = new BuildComputeData(activity);
        recData = buildComputeData.buildRecommendationReq();

        loadingAndDisplayContent();
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    private void loadingAndDisplayContent() {
        lyt_progress.setVisibility(View.VISIBLE);
        lyt_progress.setAlpha(1.0f);
        recyclerView.setVisibility(View.GONE);
        errorLabel.setVisibility(View.GONE);
        errorImage.setVisibility(View.GONE);

        final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        final RestService restService = RestService.getInstance(queue, activity);
        restService.setParameters("v2/recommendations", 45000);


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
                    Crashlytics.log(Log.ERROR, LOG_TAG, "Error processing DST recommendations");
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
            recList.add(computedResponse.createObject("Fertilizer Recommendation", FR));
        }

        if (!Strings.isEmptyOrWhitespace(IC)) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject("Inter-cropping Recommendation", IC));
        }

        if (!Strings.isEmptyOrWhitespace(PP)) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject("Planting Practices", PP));
        }

        if (!Strings.isEmptyOrWhitespace(SP)) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject("Scheduled Planting", SP));
        }

        if (recList.size() <= 0) {
            computedResponse = new ComputedResponse();
            recList.add(computedResponse.createObject("There are no recommendations available", "No recommendations, please provide more information to help us get better predictions"));
        }
        return recList;
    }
}
