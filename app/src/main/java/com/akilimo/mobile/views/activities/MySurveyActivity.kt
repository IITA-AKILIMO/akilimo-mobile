package com.akilimo.mobile.views.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import androidx.annotation.StringRes
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityMySurveyBinding
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.IVolleyCallback
import com.akilimo.mobile.rest.RestParameters
import com.akilimo.mobile.rest.RestService
import com.akilimo.mobile.rest.request.SurveyRequest
import com.akilimo.mobile.utils.Tools
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class MySurveyActivity : BaseActivity() {

    private lateinit var binding: ActivityMySurveyBinding

    private var akilimoUsage: String = ""
    private var akilimoRecRating: Int = 0
    private var akilimoUsefulRating: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = getDatabase(this)

        initComponent()
    }

    override fun initToolbar() {
    }

    override fun initComponent() {
        val rdgAkilimoUser = binding.rdgAkilimoUser
        val rdgRecommend = binding.rdgRecommend
        val rdgUseful = binding.rdgUseful
        val btnFinish = binding.btnFinish

        val profileInfo = database.profileInfoDao().findOne()

        rdgAkilimoUser.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = rdgAkilimoUser.findViewById(checkedId) as RadioButton
            akilimoUsage = radioButton.text.toString()
        }

        rdgRecommend.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = rdgRecommend.findViewById(checkedId) as RadioButton
            val idx = rdgRecommend.indexOfChild(radioButton)
            akilimoRecRating = idx + 1
        }
        rdgUseful.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = rdgUseful.findViewById(checkedId) as RadioButton
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
                language = profileInfo?.language!!,
                deviceToken = profileInfo.deviceToken!!
            )

            submitUserReview(surveyRequest = surveyRequest)
            val intent = Intent()
            intent.putExtra("MESSAGE", getString(R.string.lbl_thank_feedback_you));
            setResult(2, intent)
            closeActivity(false)
        }
    }

    override fun validate(backPressed: Boolean) {
        closeActivity(backPressed)
    }

    fun getStringByLocale(
        @StringRes stringRes: Int,
        locale: Locale,
        vararg formatArgs: Any,
    ): String {
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        return createConfigurationContext(configuration).resources.getString(stringRes, *formatArgs)
    }

    private fun submitUserReview(surveyRequest: SurveyRequest) {
        val queue = Volley.newRequestQueue(this)
        val restService = RestService.getInstance(queue, this)
        val restParameters = RestParameters(
            "v1/user-feedback/survey",
            countryCode
        )
        restService.setParameters(restParameters)


        //print recommendation data here
        val data = Tools.prepareJsonObject(surveyRequest)
        restService.postJsonObject(data, object : IVolleyCallback {
            override fun onSuccessJsonString(jsonStringResult: String) {}
            override fun onSuccessJsonArr(jsonArray: JSONArray) {}
            override fun onSuccessJsonObject(jsonObject: JSONObject) {
            }

            override fun onError(volleyError: VolleyError) {
            }
        })
    }
}
