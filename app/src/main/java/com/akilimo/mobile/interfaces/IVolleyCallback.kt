package com.akilimo.mobile.interfaces

import com.android.volley.VolleyError

import org.json.JSONArray
import org.json.JSONObject

/**
 * Interface for volley actions callbacks
 */
interface IVolleyCallback {


    /**
     * @param jsonStringResult
     */
    fun onSuccessJsonString(jsonStringResult: String)

    /**
     * @param jsonArray
     */
    fun onSuccessJsonArr(jsonArray: JSONArray)

    /**
     * @param jsonObject
     */
    fun onSuccessJsonObject(jsonObject: JSONObject)


    /**
     * @param  ex
     */
    fun onError( ex: VolleyError)
}
