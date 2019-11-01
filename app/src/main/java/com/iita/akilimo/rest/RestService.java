package com.iita.akilimo.rest;


import android.app.Activity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
    private static String apiToken = "akilimo";
    private static String userId = "akilimo";
    private String url;
    private String countryCode;
    private RequestQueue queue;
    private SessionManager sessionManager;
    private int initialTimeout = 1000;

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

    public void setParameters(String endPoint) {
        String baseUrl = sessionManager.getApiEndPoint();
        url = String.format("%s%s", baseUrl, endPoint);
    }

    public void setParameters(String endPoint, int initialTimeout) {
        String baseUrl = sessionManager.getApiEndPoint();
        this.url = String.format("%s%s", baseUrl, endPoint);
        this.initialTimeout = initialTimeout;
    }

    public void setParameters(String endPoint, String countryCode) {
        String baseUrl = sessionManager.getApiEndPoint();
        this.url = String.format("%s%s", baseUrl, endPoint);
        this.countryCode = countryCode;
    }

    public void setParameters(String endPoint, String countryCode, int initialTimeout) {
        String baseUrl = sessionManager.getApiEndPoint();
        this.url = String.format("%s%s", baseUrl, endPoint);
        this.countryCode = countryCode;
        this.initialTimeout = initialTimeout;
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
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                //headers.put("api-token", apiToken);
                //headers.put("user-id", userId);
                return headers;
            }


        };

        //jsonObjReq.setTag(LOG_TAG);
        // Adding request to request queue

        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(
                initialTimeout, //69 seconds the R endpoint takes a bit of time to return results approx 39 seconds
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonObjReq);
    }


    public void putJsonObject(final HashMap<String, String> putData, final IVolleyCallback callback) {
        // response
        // error
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

        putRequest.setRetryPolicy(new DefaultRetryPolicy(
                initialTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(putRequest);
    }

    public void getJsonObjList(final IVolleyCallback callback) {
        // prepare the Request
        // Log.d("Error.Response", error.toString());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                callback::onSuccessJsonObject,
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };

// add it to the RequestQueue

        queue.add(jsonObjectRequest);
    }

    public void getJsonArrList(final IVolleyCallback callback) {
        // prepare the Request
        //Log.d("Error.Response", error.toString());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                callback::onSuccessJsonArr,
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };
// add it to the RequestQueue
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                initialTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonArrayRequest);
    }

    public void deleteObject(final IVolleyDeleteCallback callback) {
        // response
        // error.
        StringRequest dr = new StringRequest(Request.Method.DELETE, url,
                callback::onDeleted,
                callback::onError
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return setHeaderParameters();
            }
        };

        dr.setRetryPolicy(new DefaultRetryPolicy(
                initialTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(dr);
    }


    private Map<String, String> setHeaderParameters() {
        Map<String, String> params = new HashMap<String, String>();
//        params.put("Content-Type", "application/x-www-form-urlencoded");
        params.put("Content-Type", "application/json; charset=utf-8");
        params.put("api-token", apiToken);
        params.put("user-id", userId);
        params.put("country-code", countryCode);
        return params;
    }
}