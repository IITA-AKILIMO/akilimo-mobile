package com.akilimo.mobile.views.fragments

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
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.models.TimeLineModel
import com.akilimo.mobile.models.TimelineAttributes
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.akilimo.mobile.utils.enums.StepStatus
import com.github.vipulasri.timelineview.TimelineView
import com.stepstone.stepper.VerificationError

class SummaryFragment : BaseStepFragment() {
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private var mDataList: MutableList<TimeLineModel> = ArrayList()
    private var mAttributes: TimelineAttributes? = null

    private var myAdapter: MyTimeLineAdapter? = null

//    private var countrySelected = false
//    private var areaUnitSelected = false
//    private var fieldSizeSelected = false
//    private var locationPicked = false
//    private var plantingDateProvided = false
//    private var harvestDateProvided = false
//    private var currentPracticeSelected = false
//    private var performPloughing = false
//    private var performRidging = false

    private var areaUnit: String? = ""
    private var fieldSize = 0.0
    private var lat = 0.0
    private var lon = 0.0
    private var pickedLocation = ""

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

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setDataListItems()
    }

    private fun setDataListItems() {
        val location = database.locationInfoDao().findOne()
        val mandatoryInfo = database.mandatoryInfoDao().findOne()
        val currentPractice = database.currentPracticeDao().findOne()
        val cropSchedule = database.scheduleDateDao().findOne()
        val userProfile = database.profileInfoDao().findOne()
        val countryName = userProfile?.countryName.orEmpty()

        val risks = EnumInvestmentPref.entries.map { it.prefName(requireContext()) }.toTypedArray()

        val riskAttitudeName = risks[userProfile?.riskAtt ?: 0]

        val fieldInfo = buildFieldInfo(mandatoryInfo)
        val locationString = buildLocationString(location)
        val plantingDate = cropSchedule?.plantingDate.orEmpty()
        val harvestDate = cropSchedule?.harvestDate.orEmpty()
        val ploughStr = buildPloughStr(currentPractice)
        val ridgeStr = buildRidgeStr(currentPractice)

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

    private fun buildFieldInfo(mandatoryInfo: MandatoryInfo?): String {
        mandatoryInfo?.let {
            areaUnit = it.displayAreaUnit
            val areaUnitSelected = areaUnit?.isNotEmpty() == true
            fieldSize = it.areaSize

            if (areaUnitSelected) {
                val language = LanguageManager.getLanguage(requireContext())
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

    private fun buildPloughStr(currentPractice: CurrentPractice?): String {
        currentPractice?.let {
            val ploughMethod = it.ploughingMethod
            return when {
                ploughMethod != null -> {
                    if (ploughMethod == "tractor") requireContext().getString(R.string.lbl_tractor)
                    else requireContext().getString(R.string.lbl_manual)
                }

                else -> requireContext().getString(R.string.lbl_no_ploughing)
            }
        }
        return ""
    }

    private fun buildRidgeStr(currentPractice: CurrentPractice?): String {
        currentPractice?.let {
            val ridgeMethod = it.ridgingMethod
            return when {
                ridgeMethod != null -> {
                    if (ridgeMethod == "tractor") requireContext().getString(R.string.lbl_tractor)
                    else requireContext().getString(R.string.lbl_manual)
                }

                else -> requireContext().getString(R.string.lbl_no_ridging)
            }
        }
        return ""
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

    override fun verifyStep(): VerificationError? = null

    override fun onSelected() {
        setDataListItems()
    }

    override fun onError(error: VerificationError) {}
}
