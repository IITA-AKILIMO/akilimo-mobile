package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import androidx.activity.viewModels
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityMySurveyBinding
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.utils.ui.SnackBarMessage
import com.akilimo.mobile.viewmodels.MySurveyViewModel
import com.akilimo.mobile.viewmodels.factory.MySurveyViewModelFactory
import com.google.android.material.snackbar.Snackbar


class MySurveyActivity : BindBaseActivity<ActivityMySurveyBinding>() {
    companion object {
        const val REQUEST_CODE: Int = 2
    }

    private val viewModel: MySurveyViewModel by viewModels {
        MySurveyViewModelFactory(application = this.application, preferenceManager = sessionManager)
    }

    override fun inflateBinding() = ActivityMySurveyBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(binding) {
            rdgAkilimoUser.setOnCheckedChangeListener { _, checkedId ->
                val radioButton: RadioButton = rdgAkilimoUser.findViewById(checkedId)
                viewModel.setAkilimoUsage(radioButton.text.toString())
            }

            rdgRecommend.setOnCheckedChangeListener { _, checkedId ->
                val radioButton: RadioButton = rdgRecommend.findViewById(checkedId)
                viewModel.setRecRating(rdgRecommend.indexOfChild(radioButton) + 1)
            }

            rdgUseful.setOnCheckedChangeListener { _, checkedId ->
                val radioButton: RadioButton = rdgUseful.findViewById(checkedId)
                viewModel.setUsefulRating(rdgUseful.indexOfChild(radioButton) + 1)
            }

            btnFinish.setOnClickListener {
                viewModel.submitSurvey()
            }
        }
        setupObservers()
    }

    override fun setupObservers() {
        viewModel.showSnackBarEvent.observe(this) { message ->
            message?.let {
                val message = when (it) {
                    is SnackBarMessage.Text -> it.message
                    is SnackBarMessage.Resource -> getString(it.resId)
                }
                Snackbar.make(binding.btnFinish, message, Snackbar.LENGTH_SHORT).show()
                viewModel.clearSnackBarEvent()
            }
        }

        viewModel.successEvent.observe(this) {
            val intent = Intent().apply {
                putExtra("MESSAGE", getString(R.string.lbl_thank_feedback_you))
            }
            setResult(REQUEST_CODE, intent)
            closeActivity(false)
        }
    }

    override fun validate(backPressed: Boolean) {
        closeActivity(backPressed)
    }
}
