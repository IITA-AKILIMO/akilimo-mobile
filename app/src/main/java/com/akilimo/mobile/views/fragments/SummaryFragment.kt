package com.akilimo.mobile.views.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MyTimeLineAdapter
import com.akilimo.mobile.databinding.FragmentSummaryBinding
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.models.TimeLineModel
import com.akilimo.mobile.models.TimelineAttributes
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.enums.StepStatus
import com.github.vipulasri.timelineview.TimelineView

class SummaryFragment : BindBaseStepFragment<FragmentSummaryBinding>() {

    private var mDataList: MutableList<TimeLineModel> = ArrayList()
    private var mAttributes: TimelineAttributes? = null

    private var myAdapter: MyTimeLineAdapter? = null


    private var areaUnit: String? = ""
    private var fieldSize = 0.0
    private var lat = 0.0
    private var lon = 0.0

    companion object {
        fun newInstance(): SummaryFragment = SummaryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAttributes = TimelineAttributes(
            markerSize = 48,
            markerCompleteColor = ContextCompat.getColor(
                requireContext(),
                R.color.akilimoLightGreen
            ),
            markerIncompleteColor = ContextCompat.getColor(requireContext(), R.color.red_A400),
            startLineColor = ContextCompat.getColor(requireContext(), R.color.colorAccent),
            endLineColor = ContextCompat.getColor(requireContext(), R.color.colorAccent),
            lineStyle = TimelineView.LineStyle.DASHED
        )
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSummaryBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setDataListItems()
    }

    private fun setDataListItems() {
        val context = requireContext()

        val location = database.locationInfoDao().findOne()
        val mandatoryInfo = database.mandatoryInfoDao().findOne()

        val cropSchedule = database.scheduleDateDao().findOne()
        val userProfile = database.profileInfoDao().findOne()
        val countryName = userProfile?.countryName.orEmpty()

        val risks = EnumInvestmentPref.entries.map { it.prefName(requireContext()) }.toTypedArray()

        val riskAttitudeName = risks[userProfile?.riskAtt ?: 0]

        val fieldInfo = buildFieldInfo(mandatoryInfo, context)
        val locationString = buildLocationString(location)
        val plantingDate = cropSchedule?.plantingDate.orEmpty()
        val harvestDate = cropSchedule?.harvestDate.orEmpty()

        val currentPractice = database.currentPracticeDao().findOne()
        val ploughStr = buildPloughStr(currentPractice, context)
        val ridgeStr = buildRidgeStr(currentPractice, context)

        mDataList.apply {
            clear()
            addTimeLineItem(R.string.lbl_country, countryName, countryName.isNotEmpty())
            addTimeLineItem(R.string.lbl_farm_place, locationString, locationString.isNotEmpty())
            addTimeLineItem(R.string.lbl_farm_size, fieldInfo, fieldInfo.isNotEmpty())
            addTimeLineItem(R.string.lbl_planting_date, plantingDate, plantingDate.isNotEmpty())
            addTimeLineItem(R.string.lbl_harvesting_date, harvestDate, harvestDate.isNotEmpty())
            addTimeLineItem(R.string.lbl_ploughing, ploughStr, ploughStr.isNotEmpty())
            addTimeLineItem(R.string.lbl_ridging, ridgeStr, ridgeStr.isNotEmpty())
            addTimeLineItem(R.string.lbl_risk_attitude, riskAttitudeName, true)
        }

        initAdapter()
    }

    private fun MutableList<TimeLineModel>.addTimeLineItem(
        labelResId: Int,
        value: String,
        isCompleted: Boolean
    ) {
        add(
            TimeLineModel(
                requireContext().getString(labelResId),
                value,
                if (isCompleted) StepStatus.COMPLETED else StepStatus.INCOMPLETE
            )
        )
    }

    private fun buildFieldInfo(mandatoryInfo: MandatoryInfo?, context: Context): String {
        mandatoryInfo?.let {
            areaUnit = it.displayAreaUnit
            val areaUnitSelected = areaUnit?.isNotEmpty() == true
            fieldSize = it.areaSize

            if (areaUnitSelected) {
                val language = LanguageManager.getLanguage(context)
                return if (language.equals("sw", ignoreCase = true)) {
                    String.format("%s %s", areaUnit, fieldSize)
                } else {
                    String.format("%s %s", fieldSize, areaUnit)
                }
            }
        }
        return ""
    }

    private fun buildLocationString(location: UserLocation?): String {
        location?.let {
            lat = it.latitude
            lon = it.longitude
            return "${mathHelper.removeLeadingZero(lat, "#.####")},${
                mathHelper.removeLeadingZero(
                    lon,
                    "#.####"
                )
            }"
        }
        return ""
    }

    private fun buildPloughStr(currentPractice: CurrentPractice?, context: Context): String {
        return when (currentPractice?.ploughingMethod) {
            EnumOperationMethod.TRACTOR -> context.getString(R.string.lbl_tractor)
            EnumOperationMethod.MANUAL -> context.getString(R.string.lbl_manual)
            else -> context.getString(R.string.lbl_no_ploughing)
        }
    }

    private fun buildRidgeStr(currentPractice: CurrentPractice?, context: Context): String {

        return when (currentPractice?.ridgingMethod) {
            EnumOperationMethod.TRACTOR -> context.getString(R.string.lbl_tractor)
            EnumOperationMethod.MANUAL -> context.getString(R.string.lbl_manual)
            else -> context.getString(R.string.lbl_no_ridging)
        }
    }


    private fun initRecyclerView() {
        binding.timelineRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun initAdapter() {
        myAdapter = MyTimeLineAdapter(
            mDataList,
            mAttributes!!,
            requireContext(),
            TheItemAnimation.FADE_IN
        )
        binding.timelineRecycler.adapter = myAdapter
    }

    override fun onSelected() {
        setDataListItems()
    }
}
