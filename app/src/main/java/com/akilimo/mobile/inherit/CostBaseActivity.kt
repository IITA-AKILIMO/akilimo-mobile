package com.akilimo.mobile.inherit

import android.widget.Toast
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.entities.OperationCostResponse
import com.akilimo.mobile.interfaces.AkilimoApi
import io.sentry.Sentry


abstract class CostBaseActivity : BaseActivity() {
    protected var operationCostList: ArrayList<OperationCost>? = null

    protected fun loadOperationCost(
        operationName: String,
        operationType: String,
        dialogTitle: String,
        hintText: String
    ) {
        val queryParams = mapOf(
            "operation_type" to operationName,
            "operation_name" to operationType,
        )

        val call = AkilimoApi.apiService.getOperationCosts(currencyCode, queryParams)
        call.enqueue(object : retrofit2.Callback<OperationCostResponse> {
            override fun onResponse(
                call: retrofit2.Call<OperationCostResponse>,
                response: retrofit2.Response<OperationCostResponse>
            ) {
                if (response.isSuccessful) {
                    val list = response.body()!!.data
                    operationCostList = list.toCollection(ArrayList())

                    showDialogFullscreen(
                        operationCostList,
                        operationName,
                        countryCode,
                        dialogTitle,
                        hintText
                    )
                }
            }

            override fun onFailure(call: retrofit2.Call<OperationCostResponse>, t: Throwable) {
                Toast.makeText(this@CostBaseActivity, t.message, Toast.LENGTH_LONG)
                    .show()
                Sentry.captureException(t)
            }

        })
    }


    protected abstract fun showDialogFullscreen(
        operationCostList: ArrayList<OperationCost>?,
        operationName: String?,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String?
    )
}
