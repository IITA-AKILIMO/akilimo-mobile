package com.akilimo.mobile.inherit

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.adapters.UseCaseTaskAdapter
import com.akilimo.mobile.entities.UseCaseTask
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.viewmodels.UseCaseViewModel
import com.akilimo.mobile.viewmodels.factory.UseCaseViewModelFactory

abstract class BaseUseCaseTaskActivity<T : ViewBinding> : BindBaseActivity<T>() {

    protected open val displayArrow: Boolean = true
    protected val mAdapter: UseCaseTaskAdapter by lazy {
        UseCaseTaskAdapter(applicationContext, displayArrow)
    }
    protected var dataPositionChanged: Int = -1


    protected val viewModel: UseCaseViewModel by viewModels {
        UseCaseViewModelFactory(application, getRecommendationOptions())
    }

    protected abstract fun getRecommendationOptions(): List<UseCaseTask>
    protected abstract fun handleNavigation(adviceCode: UseCaseTask)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter.setAnimationType(TheItemAnimation.BOTTOM_UP)
        mAdapter.submitList(getRecommendationOptions())

        mAdapter.setOnItemClickListener(object : UseCaseTaskAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, task: UseCaseTask, position: Int) {
                handleNavigation(task.taskName)
            }
        })
    }

    protected open fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BaseUseCaseTaskActivity)
            setHasFixedSize(true)
            adapter = mAdapter
        }
    }

    override fun setupObservers() {
        viewModel.useCaseWithTasksList.observe(this) { recList ->
            mAdapter.submitList(recList)
        }
    }
}