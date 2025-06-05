package com.akilimo.mobile.inherit

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTasks

abstract class BaseRecommendationActivity<T : ViewBinding> : BindBaseActivity<T>() {

    protected open val displayArrow: Boolean = true
    protected lateinit var mAdapter: RecOptionsAdapter
    protected var dataPositionChanged: Int = -1

    private val recList get() = getRecommendationOptions()


    protected abstract fun getRecommendationOptions(): List<RecommendationOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = RecOptionsAdapter(applicationContext, recList, displayArrow)
    }

    protected fun checkStatus(taskName: EnumAdviceTasks): AdviceStatus {
        val adviceStatus = database.adviceStatusDao().findOne(taskName.name)
        if (adviceStatus != null) {
            return adviceStatus
        }
        return AdviceStatus(taskName.name, false)
    }
}