package com.iita.akilimo.inherit;


import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.OperationCost;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class CostBaseActivity extends BaseActivity {

    protected ArrayList<OperationCost> operationCostList;

    public CostBaseActivity() {
    }


    protected void loadOperationCost(String operationName, String operationType, String dialogTitle) {

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

                    showDialogFullscreen(operationCostList, operationName, countryCode, dialogTitle);
                } catch (Exception ex) {

                    FirebaseCrashlytics.getInstance().log("Error saving price list");
                    FirebaseCrashlytics.getInstance().recordException(ex);
                }
            }

            @Override
            public void onSuccessJsonObject(JSONObject jsonObject) {
            }

            @Override
            public void onError(@NotNull VolleyError volleyError) {
                Toast.makeText(context, "Unable to load OperationCost list", Toast.LENGTH_LONG).show();

                FirebaseCrashlytics.getInstance().log("OperationCost list not able to load");
                FirebaseCrashlytics.getInstance().recordException(volleyError);
            }
        });
    }

    protected abstract void showDialogFullscreen(ArrayList<OperationCost> operationCostList, String operationName, String countryCode, String dialogTitle);

}
