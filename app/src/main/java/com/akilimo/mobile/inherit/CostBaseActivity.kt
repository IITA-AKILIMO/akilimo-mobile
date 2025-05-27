package com.akilimo.mobile.inherit

import android.widget.Toast
import com.akilimo.mobile.entities.OperationCostResponse
import com.akilimo.mobile.interfaces.AkilimoApi
import io.sentry.Sentry


abstract class CostBaseActivity : BaseActivity() {
    protected fun loadOperationCost(
        operationName: String,
        operationType: String,
        dialogTitle: String,
        hintText: String
    ) {
        val queryParams = mapOf(
            "operation_type" to operationType,
            "operation_name" to operationName,
        )
        val operationCosts = database.operationCostDao().findAllFiltered(
            operationName,
            operationType,
            countryCode
        )

        if (operationCosts.isNotEmpty()) {
            showDialogFullscreen(
                operationName,
                operationType,
                countryCode,
                dialogTitle,
                hintText
            )
            return
        }
        val call = AkilimoApi.apiService.getOperationCosts(countryCode, queryParams)
        call.enqueue(object : retrofit2.Callback<OperationCostResponse> {
            override fun onResponse(
                call: retrofit2.Call<OperationCostResponse>,
                response: retrofit2.Response<OperationCostResponse>
            ) {
                if (response.isSuccessful) {
                    val list = response.body()!!.data
                    database.operationCostDao().insertAll(list)
                    showDialogFullscreen(
                        operationName,
                        operationType,
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
        operationName: String?,
        operationType: String?,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String
    )
}
