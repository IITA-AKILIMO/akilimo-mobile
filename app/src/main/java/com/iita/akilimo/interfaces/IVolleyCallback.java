package com.iita.akilimo.interfaces;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Interface for volley actions callbacks
 */
public interface IVolleyCallback {


    /**
     * @param jsonStringResult
     */
    void onSuccessJsonString(String jsonStringResult);

    /**
     * @param jsonArray
     */
    void onSuccessJsonArr(JSONArray jsonArray);

    /**
     * @param jsonObject
     */
    void onSuccessJsonObject(JSONObject jsonObject);


    /**
     * @param volleyError
     */
    void onError(VolleyError volleyError);
}
