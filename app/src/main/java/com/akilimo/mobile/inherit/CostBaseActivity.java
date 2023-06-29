package com.akilimo.mobile.inherit;


import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.akilimo.mobile.interfaces.IVolleyCallback;
import com.akilimo.mobile.models.OperationCost;
import com.akilimo.mobile.rest.RestParameters;
import com.akilimo.mobile.rest.RestService;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.sentry.Sentry;

public abstract class CostBaseActivity extends BaseActivity {

    protected ArrayList<OperationCost> operationCostList;

    public CostBaseActivity() {
    }


    protected void loadOperationCost(String operationName, String operationType, String dialogTitle, String hintText) {

        final RestService restService = RestService.getInstance(queue, this);
        final RestParameters restParameters = new RestParameters("v3/operation-cost", countryCode);
        restParameters.setOperationName(operationName);
        restParameters.setOperationType(operationType);
        restService.setParameters(restParameters);

        restService.getJsonArrList(new IVolleyCallback() {
            @Override
            public void onSuccessJsonString(String jsonStringResult) {

            }

            @Override
            public void onSuccessJsonArr(JSONArray jsonArray) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    operationCostList = objectMapper.readValue(jsonArray.toString(), new TypeReference<ArrayList<OperationCost>>() {
                    });

                    showDialogFullscreen(operationCostList, operationName, countryCode, dialogTitle, hintText);
                } catch (Exception ex) {
                    Sentry.captureException(ex);
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(@NotNull VolleyError ex) {
                Toast.makeText(context, "Unable to load OperationCost list", Toast.LENGTH_LONG).show();
                Sentry.captureException( ex);
            }
        });
    }

    protected abstract void showDialogFullscreen(ArrayList<OperationCost> operationCostList, String operationName, String countryCode, String dialogTitle, String hintText);

}
