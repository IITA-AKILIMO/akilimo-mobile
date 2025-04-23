package com.akilimo.mobile.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.RecommendationAdapter
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityDstRecomendationBinding
import com.akilimo.mobile.entities.ProfileInfo
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.interfaces.IRecommendationCallBack
import com.akilimo.mobile.mappers.ComputedResponse
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.response.RecommendationResp
import com.akilimo.mobile.utils.BuildComputeData
import com.akilimo.mobile.views.fragments.dialog.RecommendationChannelDialog
import com.google.android.gms.common.util.Strings
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.sentry.Sentry

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class DstRecommendationActivity : BaseActivity(), IRecommendationCallBack {
    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var fabRetry: FloatingActionButton? = null
    var btnFeedback: AppCompatButton? = null
    var errorImage: ImageView? = null
    var errorLabel: TextView? = null
    var lyt_progress: LinearLayout? = null

    var binding: ActivityDstRecomendationBinding? = null
    var activity: Activity? = null
    var recData: RecommendationRequest? = null
    var recAdapter: RecommendationAdapter? = null
    var recList: List<ComputedResponse>? = null
    var profileInfo: ProfileInfo? = null
    var recommendationChannelDialog: RecommendationChannelDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDstRecomendationBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)

        context = this
        activity = this
        database = getDatabase(context)

        toolbar = binding!!.toolbarLayout.toolbar
        recyclerView = binding!!.recyclerView
        fabRetry = binding!!.fabRetry
        btnFeedback = binding!!.feedbackButton.btnGetRecommendation
        errorImage = binding!!.errorImage
        errorLabel = binding!!.errorLabel
        lyt_progress = binding!!.lytProgress

        btnFeedback!!.setText(R.string.lbl_provide_feedback)
        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.lbl_recommendations)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? ->
            closeActivity(false)
        }
    }

    override fun initComponent() {
        recyclerView!!.visibility = View.GONE
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.setHasFixedSize(true)

        recAdapter = RecommendationAdapter()
        profileInfo = database.profileInfoDao().findOne()

        lyt_progress!!.visibility = View.VISIBLE
        lyt_progress!!.alpha = 1.0f
        recyclerView!!.visibility = View.GONE
        errorLabel!!.visibility = View.GONE
        errorImage!!.visibility = View.GONE


        fabRetry!!.setOnClickListener { view: View? ->
            if (profileInfo != null) {
                displayDialog(profileInfo)
            }
        }

        btnFeedback!!.setOnClickListener { view: View? ->
            //launch the feedback dialog
            val surveyIntent = Intent(this, MySurveyActivity::class.java)
            startActivityForResult(surveyIntent, MySurveyActivity.REQUEST_CODE)
        }

        displayDialog(profileInfo)
    }


    private fun buildRecommendationData() {
        val buildComputeData = BuildComputeData(activity!!)
        recData = buildComputeData.buildRecommendationReq()
        loadingAndDisplayContent()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    private fun displayDialog(profileInfo: ProfileInfo?) {
        if (profileInfo != null) {
            recommendationChannelDialog = RecommendationChannelDialog(this, profileInfo)
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

    override fun onDataReceived(profileInfo: ProfileInfo) {
        //update the profile info
        database.profileInfoDao().update(profileInfo)
        buildRecommendationData()
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

        val call = AkilimoApi.apiService.computeRecommendations(recData)
        call.enqueue(object : retrofit2.Callback<RecommendationResp> {
            override fun onResponse(
                call: retrofit2.Call<RecommendationResp>,
                response: retrofit2.Response<RecommendationResp>
            ) {
                if (response.isSuccessful) {
                    val recommendationResp = response.body()!!
                    lyt_progress!!.visibility = View.GONE
                    recAdapter!!.setData(recList!!)
                    recyclerView!!.adapter = recAdapter
                    recyclerView!!.visibility = View.VISIBLE
                    recList = initializeData(recommendationResp)
                }
            }

            override fun onFailure(call: retrofit2.Call<RecommendationResp>, ex: Throwable) {
                lyt_progress!!.visibility = View.GONE
                errorImage!!.visibility = View.VISIBLE
                errorLabel!!.visibility = View.VISIBLE
                recyclerView!!.visibility = View.GONE

                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }

        })
    }

    private fun initializeData(recommendationResp: RecommendationResp): List<ComputedResponse> {
        val recList: MutableList<ComputedResponse> = ArrayList()

        val FR = recommendationResp.fertilizerRecText
        val IC = recommendationResp.interCroppingRecText
        val PP = recommendationResp.plantingPracticeRecText
        val SP = recommendationResp.scheduledPlantingRect

        var computedResponse: ComputedResponse

        if (!Strings.isEmptyOrWhitespace(FR)) {
            computedResponse = ComputedResponse()
            recList.add(computedResponse.createObject(getString(R.string.lbl_fertilizer_rec), FR))
        }

        if (!Strings.isEmptyOrWhitespace(IC)) {
            computedResponse = ComputedResponse()
            recList.add(computedResponse.createObject(getString(R.string.lbl_intercrop_rec), IC))
        }

        if (!Strings.isEmptyOrWhitespace(PP)) {
            computedResponse = ComputedResponse()
            recList.add(
                computedResponse.createObject(
                    getString(R.string.lbl_planting_practices_rec),
                    PP
                )
            )
        }

        if (!Strings.isEmptyOrWhitespace(SP)) {
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
