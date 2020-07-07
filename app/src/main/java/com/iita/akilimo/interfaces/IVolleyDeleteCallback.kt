package com.iita.akilimo.interfaces

import com.android.volley.VolleyError

/**
 * Interface for volley actions callbacks
 */
interface IVolleyDeleteCallback {
    /**
     * @param result
     */
    fun onDeleted(result: String)

    /**
     * @param error
     */
    fun onError(error: VolleyError)
}
