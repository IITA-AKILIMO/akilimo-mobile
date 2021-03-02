package com.iita.akilimo.views.activities

import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatButton
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.fasterxml.jackson.databind.ObjectMapper
import com.iita.akilimo.dao.AppDatabase.Companion.getDatabase
import com.iita.akilimo.databinding.ActivityMySurveyBinding
import com.iita.akilimo.inherit.BaseActivity
import com.iita.akilimo.interfaces.IVolleyCallback
import com.iita.akilimo.rest.RestParameters
import com.iita.akilimo.rest.RestService
import com.iita.akilimo.rest.recommendation.RecommendationResponse
import com.iita.akilimo.rest.request.SurveyRequest
import com.iita.akilimo.utils.Tools
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class MySurveyActivity : BaseActivity() {

    private lateinit var binding: ActivityMySurveyBinding

    private lateinit var btnSave: AppCompatButton
    private lateinit var rdgAkilimoUser: RadioGroup

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
            val surveyRequest = SurveyRequest(akilimoUsage = akilimoUsage,
                akilimoRecRating = akilimoRecRating,
                akilimoUsefulRating = akilimoUsefulRating)

            submitUserReview(deviceToken = profileInfo?.deviceToken, surveyRequest = surveyRequest)
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

    private fun submitUserReview(deviceToken: String?, surveyRequest: SurveyRequest) {
        val queue = Volley.newRequestQueue(context.applicationContext)
        val restService = RestService.getInstance(queue, this)
        val restParameters = RestParameters(
            "v1/user-feedback/survey/${deviceToken}",
            countryCode
        )
        restService.setParameters(restParameters)


        //print recommendation data here
        val data = Tools.prepareJsonObject(surveyRequest)
        restService.postJsonObject(data, object : IVolleyCallback {
            override fun onSuccessJsonString(jsonStringResult: String) {}
            override fun onSuccessJsonArr(jsonArray: JSONArray) {}
            override fun onSuccessJsonObject(jsonObject: JSONObject) {
                try {
                    val objectMapper = ObjectMapper()
                    val recommendationResponse = objectMapper.readValue(
                        jsonObject.toString(),
                        RecommendationResponse::class.java
                    )
                } catch (ex: Exception) {
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(volleyError: VolleyError) {
            }
        })
    }
}