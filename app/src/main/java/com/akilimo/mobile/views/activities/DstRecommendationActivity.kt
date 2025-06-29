package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityDstRecomendationBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BindBaseActivity
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
class DstRecommendationActivity : BindBaseActivity<ActivityDstRecomendationBinding>(),
    IRecommendationCallBack {

    var recommendationChannelDialog: RecommendationChannelDialog? = null

    override fun inflateBinding() = ActivityDstRecomendationBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            recommendationCard.visibility = View.GONE
            errorContainer.visibility = View.GONE
        }


        val userProfile = database.profileInfoDao().findOne()
        binding.btnRetry.setOnClickListener { _: View? ->
            displayDialog(userProfile)
        }

        binding.singleButton.btnAction.setOnClickListener {
            val surveyIntent = Intent(this, MySurveyActivity::class.java)
            startActivityForResult(surveyIntent, MySurveyActivity.REQUEST_CODE)
        }

        displayDialog(userProfile)
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
            binding.apply {
                lblErrorMessage.setText(R.string.lbl_no_profile_info)
                lytProgress.visibility = View.GONE
                recommendationCard.visibility = View.GONE
                errorContainer.visibility = View.VISIBLE
            }
        }
    }

    override fun onDataReceived(userProfile: UserProfile) {
        database.profileInfoDao().insert(userProfile)
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
            recommendationCard.visibility = View.GONE
            errorContainer.visibility = View.GONE
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
                    binding.recommendationCard.visibility = View.VISIBLE
                } else {

                    // Handle HTTP errors and unreachable server (like 502, 503, etc.)
                    val errorCode = response.code()
                    var errorMessage = when (errorCode) {
                        502 -> "Bad Gateway. The server is currently unavailable."
                        503 -> "Service Unavailable. Please try again later."
                        500 -> "Internal Server Error. Please try again later."
                        else -> "Something went wrong (${response.code()}). Please try again later."
                    }

                    var errorDetail = errorMessage

                    val parsedError = response.parseError()
                    if (parsedError != null) {
                        errorDetail = parsedError.error
                    }


                    binding.apply {
                        binding.lytProgress.visibility = View.GONE
                        lblErrorMessage.text = errorMessage
                        lblErrorDetail.text = errorDetail
                        recommendationCard.visibility = View.GONE
                        errorContainer.visibility = View.VISIBLE
                    }


                    Toast.makeText(
                        this@DstRecommendationActivity,
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                    Sentry.captureMessage("API Error: $errorDetail")
                }
            }

            override fun onFailure(call: retrofit2.Call<RecommendationResponse>, ex: Throwable) {
                binding.apply {
                    lytProgress.visibility = View.GONE
                    recommendationCard.visibility = View.GONE
                    errorContainer.visibility = View.VISIBLE
                }

                val message = when (ex) {
                    is java.net.UnknownHostException -> "No internet connection. Please check your network."
                    is java.net.ConnectException -> "Server is unreachable. Please try again later."
                    is java.net.SocketTimeoutException -> "Request timed out. Please try again."
                    else -> ex.localizedMessage ?: "An unexpected error occurred."
                }

                binding.lblErrorMessage.text = message
                binding.lblErrorDetail.text = ex.message

                Toast.makeText(this@DstRecommendationActivity, message, Toast.LENGTH_SHORT).show()
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
