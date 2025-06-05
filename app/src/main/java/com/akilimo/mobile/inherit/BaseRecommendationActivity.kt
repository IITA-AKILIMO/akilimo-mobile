package com.akilimo.mobile.inherit

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTasks

abstract class BaseRecommendationActivity<T : ViewBinding> : BindBaseActivity<T>() {

    private val recList get() = getRecommendationOptions()

    protected lateinit var mAdapter: RecOptionsAdapter
    protected var dataPositionChanged: Int = -1


    protected abstract fun getRecommendationOptions(): List<RecommendationOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = RecOptionsAdapter(applicationContext, recList)
    }


    override fun onResume() {
        super.onResume()
        mAdapter.setData(recList)
    }

    protected fun checkStatus(taskName: EnumAdviceTasks): AdviceStatus {
        val adviceStatus = database.adviceStatusDao().findOne(taskName.name)
        if (adviceStatus != null) {
            return adviceStatus
        }
        return AdviceStatus(taskName.name, false)
    }
}