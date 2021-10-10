package com.akilimo.mobile.inherit;


import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
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
                    Crashlytics.log(Log.ERROR, LOG_TAG, "Error saving price list");
                    Crashlytics.logException(ex);
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(@NotNull VolleyError volleyError) {
                Toast.makeText(context, "Unable to load OperationCost list", Toast.LENGTH_LONG).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, "OperationCost list not able to load");
                Crashlytics.logException(volleyError);
            }
        });
    }

    protected abstract void showDialogFullscreen(ArrayList<OperationCost> operationCostList, String operationName, String countryCode, String dialogTitle, String hintText);

}
