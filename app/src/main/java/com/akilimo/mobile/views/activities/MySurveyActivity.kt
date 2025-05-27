package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityMySurveyBinding
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.rest.request.SurveyRequest
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository
import io.sentry.Sentry
import okhttp3.ResponseBody


class MySurveyActivity : BaseActivity() {

    private val prefs: SharedPrefsAppLocaleRepository by lazy {
        SharedPrefsAppLocaleRepository(this@MySurveyActivity)
    }

    private lateinit var binding: ActivityMySurveyBinding

    private var akilimoUsage: String = ""
    private var akilimoRecRating: Int = 0
    private var akilimoUsefulRating: Int = 0

    companion object {
        const val REQUEST_CODE: Int = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    override fun initToolbar() {/*No processing*/
    }

    override fun initComponent() {
        val rdgAkilimoUser = binding.rdgAkilimoUser
        val rdgRecommend = binding.rdgRecommend
        val rdgUseful = binding.rdgUseful
        val btnFinish = binding.btnFinish

        rdgAkilimoUser.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = rdgAkilimoUser.findViewById(checkedId)
            akilimoUsage = radioButton.text.toString()
        }

        rdgRecommend.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = rdgRecommend.findViewById(checkedId)
            val idx = rdgRecommend.indexOfChild(radioButton)
            akilimoRecRating = idx + 1
        }
        rdgUseful.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = rdgUseful.findViewById(checkedId)
            val idx = rdgUseful.indexOfChild(radioButton)
            akilimoUsefulRating = idx + 1
        }


        //now we submit to the API
        btnFinish.setOnClickListener {
            //send data to REST api
            val surveyRequest = SurveyRequest(
                akilimoUsage = akilimoUsage,
                akilimoRecRating = akilimoRecRating,
                akilimoUsefulRating = akilimoUsefulRating,
                language = prefs.desiredLocale?.language ?: "en",
                deviceToken = sessionManager.getDeviceToken()
            )

            submitUserReview(surveyRequest = surveyRequest)
            val intent = Intent()
            intent.putExtra("MESSAGE", getString(R.string.lbl_thank_feedback_you))
            setResult(2, intent)
            closeActivity(false)
        }
    }

    override fun validate(backPressed: Boolean) {
        closeActivity(backPressed)
    }

    private fun submitUserReview(surveyRequest: SurveyRequest) {
        val call = AkilimoApi.apiService.submitUserReview(surveyRequest)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: retrofit2.Call<ResponseBody>,
                response: retrofit2.Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@MySurveyActivity,
                        "Feedback submitted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@MySurveyActivity, t.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(t)
            }

        })

    }
}
