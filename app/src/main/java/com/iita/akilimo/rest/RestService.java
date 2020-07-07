package com.iita.akilimo.rest;


import android.app.Activity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.iita.akilimo.interfaces.IVolleyCallback;
import com.iita.akilimo.interfaces.IVolleyDeleteCallback;
import com.iita.akilimo.utils.SessionManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RestService {
    private static final String LOG_TAG = RestService.class.getSimpleName();

    private static RestService restServiceObj;
    private String url;
    private RequestQueue queue;
    private SessionManager sessionManager;
    private RestParameters restParameters;

    private RestService(RequestQueue requestQueue, Activity activity) {
        queue = requestQueue;
        sessionManager = new SessionManager(activity);
    }

    public static RestService getInstance(RequestQueue requestQueue, Activity activity) {
        if (restServiceObj == null) {
            restServiceObj = new RestService(requestQueue, activity);
        }
        return restServiceObj;
    }

    public void setParameters(RestParameters restParameters) {
        String baseUrl = sessionManager.getApiEndPoint();

        this.restParameters = restParameters;
        this.url = String.format("%s%s", baseUrl, restParameters.getUrl());
    }

    public void postJsonObject(final JSONObject postData, final IVolleyCallback callback) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, postData,
                callback::onSuccessJsonObject,
                callback::onError) {

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };

        jsonObjReq.setRetryPolicy(retryPolicy());

        queue.add(jsonObjReq);
    }


    public void putJsonObject(final HashMap<String, String> putData, final IVolleyCallback callback) {
        StringRequest putRequest = new StringRequest(Request.Method.PUT, url,
                callback::onSuccessJsonString,
                callback::onError
        ) {

            @Override
            protected Map<String, String> getParams() {
                return putData;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };

        putRequest.setRetryPolicy(retryPolicy());
        queue.add(putRequest);
    }

    public void getJsonObjList(final IVolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                callback::onSuccessJsonObject,
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };

        queue.add(jsonObjectRequest);
    }

    public void getJsonArrList(final IVolleyCallback callback) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                callback::onSuccessJsonArr,
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };

        jsonArrayRequest.setRetryPolicy(retryPolicy());
        queue.add(jsonArrayRequest);
    }

    public void deleteObject(final IVolleyDeleteCallback callback) {
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                callback::onDeleted,
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };

        stringRequest.setRetryPolicy(retryPolicy());
        queue.add(stringRequest);
    }

    private RetryPolicy retryPolicy() {
        return new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return restParameters.getInitialTimeout();
            }

            @Override
            public int getCurrentRetryCount() {
                return restParameters.getMaxRetries();
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        };
    }


    private Map<String, String> setHeaderParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Content-Type", "application/json; charset=utf-8");
        params.put("user-id", restParameters.getUserId());
        params.put("country-code", restParameters.getCountryCode());
        params.put("locale-lang", restParameters.getLocale().getLanguage());
        params.put("locale-country", restParameters.getLocale().getCountry());
        params.put("op-type", restParameters.getOperationType());
        params.put("op-name", restParameters.getOperationName());
        params.put("context", restParameters.getContext());

        if (sessionManager != null) {
            params.put("api-token", sessionManager.getDeviceToken());
            params.put("build-date", sessionManager.getAppBuildDate());
        }
        return params;
    }
}