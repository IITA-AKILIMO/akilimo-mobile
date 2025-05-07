package com.akilimo.mobile.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityDstRecomendationBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.IRecommendationCallBack
import com.akilimo.mobile.mappers.ComputedResponse
import com.akilimo.mobile.rest.response.RecommendationResponse
import com.akilimo.mobile.utils.BuildComputeData
import com.akilimo.mobile.views.fragments.dialog.RecommendationChannelDialog
import io.sentry.Sentry

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class DstRecommendationActivity : BaseActivity(), IRecommendationCallBack {
    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var errorImage: ImageView? = null
    var errorLabel: TextView? = null
    var lyt_progress: LinearLayout? = null

    private var _binding: ActivityDstRecomendationBinding? = null
    private val binding get() = _binding!!

    var activity: Activity? = null

    var recAdapter: RecommendationAdapter? = null
    var responseList: List<ComputedResponse> = emptyList()
    var userProfile: UserProfile? = null
    var recommendationChannelDialog: RecommendationChannelDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDstRecomendationBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        toolbar = binding.toolbarLayout.toolbar
        recyclerView = binding.recyclerView
        errorImage = binding.errorImage
        errorLabel = binding.errorLabel
        lyt_progress = binding.lytProgress

        binding.singleButton.apply {
            btnAction.setText(R.string.lbl_provide_feedback)
        }

        setupToolbar(binding.toolbarLayout.toolbar, R.string.lbl_recommendations) {
            closeActivity(false)
        }

        recyclerView!!.visibility = View.GONE
        recyclerView!!.layoutManager = LinearLayoutManager(this@DstRecommendationActivity)
        recyclerView!!.setHasFixedSize(true)

        recAdapter = RecommendationAdapter()
        userProfile = database.profileInfoDao().findOne()

        lyt_progress!!.visibility = View.VISIBLE
        lyt_progress!!.alpha = 1.0f
        recyclerView!!.visibility = View.GONE
        errorLabel!!.visibility = View.GONE
        errorImage!!.visibility = View.GONE


        binding.fabRetry.setOnClickListener { view: View? ->
            if (userProfile != null) {
                displayDialog(userProfile)
            }
        }

        binding.singleButton.btnAction.setOnClickListener {
            val surveyIntent = Intent(this, MySurveyActivity::class.java)
            startActivityForResult(surveyIntent, MySurveyActivity.REQUEST_CODE)
        }

//        displayDialog(userProfile)
        loadingAndDisplayContent()
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
            errorLabel!!.setText(R.string.lbl_no_profile_info)
            lyt_progress!!.visibility = View.GONE
            errorImage!!.visibility = View.VISIBLE
            errorLabel!!.visibility = View.VISIBLE
            recyclerView!!.visibility = View.GONE
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
        lyt_progress!!.visibility = View.VISIBLE
        lyt_progress!!.alpha = 1.0f
        recyclerView!!.visibility = View.GONE
        errorLabel!!.visibility = View.GONE
        errorImage!!.visibility = View.GONE

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
                    lyt_progress!!.visibility = View.GONE
                    if (!responseList.isEmpty()) {
                        recAdapter!!.setData(responseList)
                        recyclerView!!.adapter = recAdapter
                        responseList = initializeData(recommendationResp)
                    }
                    recyclerView!!.visibility = View.VISIBLE
                } else {
                    binding.apply {
                        lytProgress.visibility = View.GONE
                        errorImage.visibility = View.VISIBLE
                        errorLabel.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }
            }

            override fun onFailure(call: retrofit2.Call<RecommendationResponse>, ex: Throwable) {
                lyt_progress!!.visibility = View.GONE
                errorImage!!.visibility = View.VISIBLE
                errorLabel!!.visibility = View.VISIBLE
                recyclerView!!.visibility = View.GONE

                Toast.makeText(this@DstRecommendationActivity, ex.message, Toast.LENGTH_SHORT)
                    .show()
                Sentry.captureException(ex)
            }

        })
    }

    private fun initializeData(recommendationResponse: RecommendationResponse): List<ComputedResponse> {
        val recList: MutableList<ComputedResponse> = ArrayList()

        val FR = recommendationResponse.fertilizerRecText
        val IC = recommendationResponse.interCroppingRecText
        val PP = recommendationResponse.plantingPracticeRecText
        val SP = recommendationResponse.scheduledPlantingRecText

        var computedResponse: ComputedResponse

        if (!FR.isNullOrEmpty()) {
            computedResponse = ComputedResponse()
            recList.add(computedResponse.createObject(getString(R.string.lbl_fertilizer_rec), FR))
        }

        if (!IC.isNullOrEmpty()) {
            computedResponse = ComputedResponse()
            recList.add(computedResponse.createObject(getString(R.string.lbl_intercrop_rec), IC))
        }

        if (!PP.isNullOrEmpty()) {
            computedResponse = ComputedResponse()
            recList.add(
                computedResponse.createObject(
                    getString(R.string.lbl_planting_practices_rec),
                    PP
                )
            )
        }

        if (!SP.isNullOrEmpty()) {
            computedResponse = ComputedResponse()
            recList.add(
                computedResponse.createObject(
                    getString(R.string.lbl_scheduled_planting_rec),
                    SP
                )
            )
        }

        if (recList.size <= 0) {
            computedResponse = ComputedResponse()
            recList.add(
                computedResponse.createObject(
                    getString(R.string.lbl_no_recommendations),
                    getString(R.string.lbl_no_recommendations_prompt)
                )
            )
        }
        return recList
    }
}
