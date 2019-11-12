package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.inherit.ComputeFragment;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.rest.request.RecommendationRequest;
import com.iita.akilimo.views.activities.DstRecommendationActivity;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
@Deprecated
public class ComputeRecommendationsFragment extends ComputeFragment {


    @BindView(R.id.btnFR)
    Button btnFR;
    @BindView(R.id.btnBPP)
    Button btnBPP;
    @BindView(R.id.btnIC)
    Button btnIC;
    @BindView(R.id.btnSPH)
    Button btnSPH;

    @BindView(R.id.btnGetRec)
    Button btnGetRec;

    RecAdvice recAdvice;
    private boolean FR;         //: Logical, c(NA, TRUE, FALSE), indicating if fertilizer recommendations are requested, NA if IC == TRUE and country == "TZ"
    private boolean BPP;        // : Logical, c(NA, TRUE, FALSE), indicating if planting practice recommendations are requested, NA if IC == TRUE or country == "TZ"
    private boolean SPP;        // : Logical, c(NA, TRUE, FALSE), indicating if scheduled planting - advice on planting date is requested, NA if IC == TRUE
    private boolean SPH;        //: Logical, c(NA, TRUE, FALSE), indicating if scheduled planting - advice on harvest date is requested, NA if IC == TRUE
    private boolean IC;

    public ComputeRecommendationsFragment() {
        // Required empty public constructor
    }

    public static ComputeRecommendationsFragment newInstance() {
        return new ComputeRecommendationsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compute_recommendations, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnFR.setOnClickListener(v -> {
            FR = !btnFR.isSelected();
            btnFR.setSelected(FR);
        });


        btnBPP.setOnClickListener(v -> {
            BPP = !btnBPP.isSelected();
            btnBPP.setSelected(BPP);
        });

        btnIC.setOnClickListener(v -> {
            IC = !btnIC.isSelected();
            btnIC.setSelected(IC);
        });

        btnSPH.setOnClickListener(v -> {
            SPH = SPP = !btnSPH.isSelected();
            btnSPH.setSelected(SPH);
        });

        btnGetRec.setOnClickListener(v -> {
            recAdvice = objectBoxEntityProcessor.getRecAdvice();
            if (recAdvice == null) {
                recAdvice = new RecAdvice();
            }
            recAdvice.setFR(FR);
            recAdvice.setIC(IC);
            recAdvice.setSPH(SPH);
            recAdvice.setSPP(SPP);
            recAdvice.setBPP(BPP);

            objectBoxEntityProcessor.saveRecAdvice(recAdvice);

            processRecommendations(getActivity());
        });
    }

    private void processRecommendations(FragmentActivity activity) {
        final RequestQueue queue = Volley.newRequestQueue(context.getApplicationContext());
        final RestService restService = RestService.getInstance(queue, getActivity());

        RecommendationRequest postData = buildRecommendationReq();
        if (postData == null) {
            Toast.makeText(activity, "Unable to prepare recommendations data, please try again", Toast.LENGTH_SHORT).show();
        }

        if (!activity.isFinishing()) {
            Intent intent = new Intent(getActivity(), DstRecommendationActivity.class);
//            intent.putExtra(DstRecommendationActivity.REC_TAG, postData);
            activity.startActivity(intent);
        }
    }

    @Override
    public void refreshData() {
        throw new UnsupportedOperationException();
    }

}
