package com.iita.akilimo.inherit;


import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.models.OperationCost;
import com.iita.akilimo.rest.RestParameters;
import com.iita.akilimo.rest.RestService;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.iita.akilimo.utils.enums.EnumOperation;
import com.iita.akilimo.utils.enums.EnumOperationType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class CostBaseActivity extends BaseActivity {

    protected ArrayList<OperationCost> operationCostList;
    protected EnumCountry enumCountry;

    public CostBaseActivity() {
    }


    protected void loadOperationCost(EnumOperation enumOperation, EnumOperationType operationType, String dialogTitle) {

        final RestService restService = RestService.getInstance(queue, this);
        final RestParameters restParameters = new RestParameters("v3/operation-cost", countryCode);
        restParameters.setOperationName(enumOperation.name());
        restParameters.setOperationType(operationType.operationName());
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

                    showDialogFullscreen(operationCostList, enumOperation, enumCountry, dialogTitle);
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

    protected abstract void showDialogFullscreen(ArrayList<OperationCost> operationCostList, EnumOperation operation, EnumCountry enumCountry, String dialogTitle);

}
