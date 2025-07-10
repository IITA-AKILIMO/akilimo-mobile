package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.UseCaseAdapter
import com.akilimo.mobile.databinding.ActivityUseCaseBinding
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.models.UseCaseWithTasks
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.ui.SnackBarMessage
import com.akilimo.mobile.viewmodels.UseCaseViewModel
import com.akilimo.mobile.viewmodels.factory.UseCaseViewModelFactory
import com.akilimo.mobile.views.activities.usecases.FertilizerRecActivity
import com.akilimo.mobile.views.activities.usecases.InterCropRecActivity
import com.akilimo.mobile.views.activities.usecases.PlantingPracticesActivity
import com.akilimo.mobile.views.activities.usecases.ScheduledPlantingActivity
import com.google.android.material.snackbar.Snackbar

class UseCaseActivity :
    BindBaseActivity<ActivityUseCaseBinding>() {

    private val mAdapter: UseCaseAdapter by lazy {
        UseCaseAdapter(applicationContext)
    }

    private val viewModel: UseCaseViewModel by viewModels {
        UseCaseViewModelFactory(application, getUseCaseList())
    }


    override fun inflateBinding(): ActivityUseCaseBinding {
        return ActivityUseCaseBinding.inflate(layoutInflater)
    }


    fun getUseCaseList(): List<UseCaseWithTasks> {
        val items = mutableListOf<UseCaseWithTasks>()

        fun useCaseWith(code: EnumUseCase, labelRes: Int): UseCaseWithTasks {
            return UseCaseWithTasks(
                useCase = UseCase(useCase = code, useCaseLabel = labelRes),
                useCaseTasks = emptyList()
            )
        }

        items.add(useCaseWith(EnumUseCase.FR, R.string.lbl_fertilizer_recommendations))
        items.add(useCaseWith(EnumUseCase.SP, R.string.lbl_scheduled_planting_and_harvest))

        if (countryCode != EnumCountry.Ghana.countryCode()) {
            items.add(useCaseWith(EnumUseCase.PP, R.string.lbl_best_planting_practices))
        }

        when (countryCode) {
            EnumCountry.Nigeria.countryCode() -> {
                items.add(useCaseWith(EnumUseCase.CIM, R.string.lbl_intercropping_maize))
            }

            EnumCountry.Tanzania.countryCode() -> {
                items.add(
                    useCaseWith(
                        EnumUseCase.CIS,
                        R.string.lbl_intercropping_sweet_potato
                    )
                )
            }
        }

        viewModel.saveUseCaseTask(items)
        return items
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(binding.toolbar, R.string.lbl_recommendations) {
            closeActivity(false)
        }

        mAdapter.setAnimationType(TheItemAnimation.BOTTOM_UP)
        mAdapter.submitList(getUseCaseList())

        mAdapter.setOnItemClickListener(object : UseCaseAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, useCase: UseCaseWithTasks, position: Int) {
                handleNavigation(useCase.useCase)
            }
        })

        binding.recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@UseCaseActivity)
            setHasFixedSize(true)
            adapter = mAdapter
        }
        setupObservers()
    }

    override fun setupObservers() {
        viewModel.useCaseWithTasksList.observe(this) { newList ->
        }
        viewModel.showSnackBarEvent.observe(this) { message ->
            message?.let {
                val message = when (it) {
                    is SnackBarMessage.Text -> it.message
                    is SnackBarMessage.Resource -> getString(it.resId)
                }
                Snackbar.make(binding.scrollContent, message, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSnackBarEvent()
            }
        }
    }

    fun handleNavigation(useCase: UseCase) {
        val adviceCode = useCase.useCase
        val intent = when (adviceCode) {
            EnumUseCase.FR -> Intent(this, FertilizerRecActivity::class.java)
            EnumUseCase.PP -> Intent(this, PlantingPracticesActivity::class.java)
            EnumUseCase.CIM,
            EnumUseCase.CIS -> Intent(this, InterCropRecActivity::class.java)

            EnumUseCase.SP -> Intent(this, ScheduledPlantingActivity::class.java)
            else -> null
        }

        intent?.let {
            openActivity(it)
        }
    }

    override fun onSupportNavigateUp(): Boolean = true


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(
            this@UseCaseActivity,
            R.string.lbl_back_instructions,
            Toast.LENGTH_SHORT
        ).show()
    }
}
