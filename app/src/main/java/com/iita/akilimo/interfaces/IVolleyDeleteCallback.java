package com.iita.akilimo.interfaces;

import com.android.volley.VolleyError;

/**
 * Interface for volley actions callbacks
 */
public interface IVolleyDeleteCallback {
    /**
     * @param result
     */
    void onDeleted(String result);

    /**
     * @param error
     */
    void onError(VolleyError error);
}
