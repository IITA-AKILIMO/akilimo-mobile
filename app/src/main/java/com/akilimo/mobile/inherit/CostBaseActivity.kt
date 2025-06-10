package com.akilimo.mobile.inherit

import android.widget.Toast
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.entities.OperationCostResponse
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.utils.enums.EnumOperation
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import io.sentry.Sentry


abstract class CostBaseActivity<T : ViewBinding> : BindBaseActivity<T>() {
    protected fun loadOperationCost(
        operationName: EnumOperation,
        operationType: EnumOperationMethod,
        dialogTitle: String,
        hintText: String
    ) {
        val queryParams = mapOf(
            "operation_type" to operationType.name,
            "operation_name" to operationName.name,
        )
        val operationCosts = database.operationCostDao().findAllFiltered(
            operationName.name,
            operationType.name,
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
        operationName: EnumOperation,
        operationType: EnumOperationMethod,
        countryCode: String?,
        dialogTitle: String?,
        hintText: String
    )
}
