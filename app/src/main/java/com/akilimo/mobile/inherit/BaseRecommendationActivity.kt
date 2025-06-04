package com.akilimo.mobile.inherit

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.models.RecommendationOptions
import com.akilimo.mobile.utils.enums.EnumAdviceTasks

abstract class BaseRecommendationActivity<T : ViewBinding> : MyBaseActivity() {

    private var _binding: T? = null
    protected val binding
        get() = _binding ?: throw IllegalStateException("Binding is not initialized yet.")

    private val recList get() = getRecommendationOptions()

    protected var mAdapter: RecOptionsAdapter = RecOptionsAdapter(emptyList())
    protected var dataPositionChanged = 0

    protected abstract fun inflateBinding(): T
    protected abstract fun getRecommendationOptions(): List<RecommendationOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        mAdapter.setData(recList, dataPositionChanged)
    }

    protected fun checkStatus(taskName: EnumAdviceTasks): AdviceStatus {
        val database = getDatabase(this)
        val adviceStatus = database.adviceStatusDao().findOne(taskName.name)

        if (adviceStatus != null) {
            return adviceStatus
        }

        return AdviceStatus(taskName.name, false)
    }
}