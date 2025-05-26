package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityDstRecomendationBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.IRecommendationCallBack
import com.akilimo.mobile.rest.response.RecommendationResponse
import com.akilimo.mobile.rest.retrofit.parseError
import com.akilimo.mobile.utils.BuildComputeData
import com.akilimo.mobile.views.fragments.dialog.RecommendationChannelDialog
import io.sentry.Sentry

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class DstRecommendationActivity : BaseActivity(), IRecommendationCallBack {

    private var _binding: ActivityDstRecomendationBinding? = null
    private val binding get() = _binding!!

    var recommendationChannelDialog: RecommendationChannelDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDstRecomendationBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)


        binding.singleButton.apply {
            btnAction.setText(R.string.lbl_provide_feedback)
        }

        setupToolbar(binding.toolbarLayout.toolbar, R.string.lbl_recommendations) {
            closeActivity(false)
        }




        binding.apply {
            lytProgress.apply {
                visibility = View.VISIBLE
                alpha = 1.0f
            }

            errorLabel.visibility = View.GONE
            errorImage.visibility = View.GONE
            recommendationLayout.visibility = View.GONE
        }


        val userProfile = database.profileInfoDao().findOne()
        binding.fabRetry.setOnClickListener { view: View? ->
            displayDialog(userProfile)
        }

        binding.singleButton.btnAction.setOnClickListener {
            val surveyIntent = Intent(this, MySurveyActivity::class.java)
            startActivityForResult(surveyIntent, MySurveyActivity.REQUEST_CODE)
        }

        displayDialog(userProfile)
//        loadingAndDisplayContent()
    }

    override fun initComponent() {}
    override fun initToolbar() {
    }


    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    private fun displayDialog(userProfile: UserProfile?) {
        if (userProfile != null) {
            recommendationChannelDialog = RecommendationChannelDialog(this, userProfile)
            recommendationChannelDialog!!.show(
                supportFragmentManager,
                RecommendationChannelDialog.TAG
            )
            recommendationChannelDialog!!.isCancelable = false
        } else {
            //show a message
            binding.apply {
                errorLabel.setText(R.string.lbl_no_profile_info)
                lytProgress.visibility = View.GONE
                errorImage.visibility = View.VISIBLE
                errorLabel.visibility = View.VISIBLE
                recommendationLayout.visibility = View.GONE
            }
        }
    }

    override fun onDataReceived(userProfile: UserProfile) {
        val database = getDatabase(this@DstRecommendationActivity)
        database.profileInfoDao().update(userProfile)
        loadingAndDisplayContent()
    }

    override fun onDismiss() {
        closeActivity(false)
    }


    private fun loadingAndDisplayContent() {
        binding.apply {
            lytProgress.apply {
                visibility = View.VISIBLE
                alpha = 1.0f
            }
            recommendationLayout.visibility = View.GONE
            errorLabel.visibility = View.GONE
            errorImage.visibility = View.GONE
        }

        val buildComputeData = BuildComputeData(this@DstRecommendationActivity)
        val recData = buildComputeData.buildRecommendationReq()

        val call = AkilimoApi.apiService.computeRecommendations(recData)
        call.enqueue(object : retrofit2.Callback<RecommendationResponse> {
            override fun onResponse(
                call: retrofit2.Call<RecommendationResponse>,
                response: retrofit2.Response<RecommendationResponse>
            ) {
                if (response.isSuccessful) {
                    val recommendationResp = response.body()!!
                    binding.lytProgress.visibility = View.GONE
                    initializeData(recommendationResp)
                    binding.recommendationLayout.visibility = View.VISIBLE
                } else {

                    val error = response.parseError()
                    if (error != null) {
                        binding.errorLabel.text = error.message
                    }

                    binding.apply {
                        lytProgress.visibility = View.GONE
                        errorImage.visibility = View.VISIBLE
                        errorLabel.visibility = View.VISIBLE
                        recommendationLayout.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<RecommendationResponse>, ex: Throwable) {
                binding.apply {
                    lytProgress.visibility = View.GONE
                    errorImage.visibility = View.VISIBLE
                    errorLabel.visibility = View.VISIBLE
                    recommendationLayout.visibility = View.GONE

                    errorLabel.text = ex.message
                }

                Toast.makeText(this@DstRecommendationActivity, ex.message, Toast.LENGTH_SHORT)
                    .show()
                Sentry.captureException(ex)
            }

        })
    }

    private fun initializeData(recommendationResponse: RecommendationResponse) {

        var label = getString(R.string.lbl_no_recommendations)

        var recText = recommendationResponse.recommendation
        var recType = recommendationResponse.recType

        if (recText.isNullOrEmpty()) {
            recText = getString(R.string.lbl_no_recommendations_prompt)
        }

        if (recType.equals("FR")) {
            label = getString(R.string.lbl_fertilizer_rec)
        }

        if (recType.equals("IC")) {
            label = getString(R.string.lbl_intercrop_rec)
        }
        if (recType.equals("PP")) {
            label = getString(R.string.lbl_planting_practices_rec)
        }
        if (recType.equals("SP")) {
            label = getString(R.string.lbl_scheduled_planting_rec)
        }


        binding.txtRecommendation.text = recText
        binding.txtRecType.text = label
    }
}
