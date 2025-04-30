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
import com.akilimo.mobile.entities.CropSchedule
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.models.TimeLineModel
import com.akilimo.mobile.models.TimelineAttributes
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.akilimo.mobile.utils.enums.StepStatus
import com.github.vipulasri.timelineview.TimelineView
import com.stepstone.stepper.VerificationError


/**
 * A simple [Fragment] subclass.
 * Use the [SummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SummaryFragment : BaseStepFragment() {
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private var mDataList: MutableList<TimeLineModel> = ArrayList()
    private var mAttributes: TimelineAttributes? = null
    private var location: UserLocation? = null
    private var mandatoryInfo: MandatoryInfo? = null
    private var currentPractice: CurrentPractice? = null
    private var cropSchedule: CropSchedule? = null
    private var userProfile: UserProfile? = null

    private var adapter: MyTimeLineAdapter? = null


    private var countrySelected = false
    private var areaUnitSelected = false
    private var fieldSizeSelected = false
    private var locationPicked = false
    private var plantingDateProvided = false
    private var harvestDateProvided = false
    private var currentPracticeSelected = false
    private var performPloughing = false
    private var performRidging = false

    private var areaUnit: String? = ""
    private var fieldSize = 0.0
    private var pickedLocation = ""
    private var risks: Array<String>? = null

    companion object {
        fun newInstance(): SummaryFragment {
            return SummaryFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        risks = arrayOf(
            EnumInvestmentPref.Rarely.prefName(context),
            EnumInvestmentPref.Sometimes.prefName(context),
            EnumInvestmentPref.Often.prefName(context)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAttributes = TimelineAttributes(
            48,
            ContextCompat.getColor(requireContext(), R.color.akilimoLightGreen),
            ContextCompat.getColor(
                requireContext(), R.color.red_A400
            ),
            true,
            0,
            0,
            0,
            0,
            0,
            0,
            ContextCompat.getColor(requireContext(), R.color.colorAccent),
            ContextCompat.getColor(
                requireContext(), R.color.colorAccent
            ),
            TimelineView.LineStyle.DASHED,
            4,
            2
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
    }

    private fun setDataListItems() {
        var plantingDate: String? = ""
        var harvestDate: String? = ""
        var fieldInfo = ""
        val fieldName = ""
        var summaryTitle: String? = requireContext().getString(R.string.lbl_summary_title)
        var riskAttitude = 0
        var riskAttitudeName = ""
        var lat = 0.0
        var lon = 0.0
        val ploughStr = StringBuilder()
        val ridgeStr = StringBuilder()
        val stepStatus = StepStatus.WARNING

        location = database.locationInfoDao().findOne()
        mandatoryInfo = database.mandatoryInfoDao().findOne()
        currentPractice = database.currentPracticeDao().findOne()
        cropSchedule = database.scheduleDateDao().findOne()

        userProfile = database.profileInfoDao().findOne()
        if (userProfile != null) {
            countryName = userProfile!!.countryName
            countryCode = userProfile!!.countryCode
            countrySelected = countryName.isNotEmpty()
            riskAttitude = userProfile!!.riskAtt
        }
        riskAttitudeName = risks!![riskAttitude]

        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo!!.displayAreaUnit
            areaUnitSelected = areaUnit != null && areaUnit!!.isNotEmpty()

            summaryTitle = java.lang.String.format(
                requireContext().getString(R.string.lbl_summary_text),
                userProfile!!.names(),
                userProfile!!.farmName
            )
            if (areaUnitSelected) {
                fieldSize = mandatoryInfo!!.areaSize
                fieldSizeSelected = fieldSize > 0.0
                fieldInfo = String.format("%s %s", fieldSize, areaUnit)
                try {
                    val locale = getCurrentLocale()
                    fieldInfo = if (locale!!.language.equals("sw", ignoreCase = true)) {
                        String.format("%s %s", areaUnit, fieldSize)
                    } else {
                        String.format("%s %s", fieldSize, areaUnit)
                    }
                } catch (ignored: Exception) {
                }
            }
        }

        binding.txtSummaryTitle.text = summaryTitle
        if (location != null) {
            pickedLocation = formatLocationInfo(location)
            lat = location!!.latitude
            lon = location!!.longitude
            locationPicked = lat != 0.0 || lon != 0.0
        }

        if (cropSchedule != null) {
            plantingDate = cropSchedule!!.plantingDate
            harvestDate = cropSchedule!!.harvestDate
            plantingDateProvided = !plantingDate.isNullOrEmpty()
            harvestDateProvided = !harvestDate.isNullOrEmpty()
        }

        if (currentPractice != null) {
            currentPracticeSelected = true
            performPloughing = currentPractice!!.performPloughing
            performRidging = currentPractice!!.performRidging

            val _ploughMethod = currentPractice!!.ploughingMethod
            val _ridgeMethod = currentPractice!!.ridgingMethod
            if (performPloughing && _ploughMethod != null) {
                var ploughMethod = requireContext().getString(R.string.lbl_manual)
                if (_ploughMethod == "tractor") {
                    ploughMethod = requireContext().getString(R.string.lbl_tractor)
                }
                ploughStr.append(ploughMethod)
            } else {
                ploughStr.append(requireContext().getString(R.string.lbl_no_ploughing))
            }


            if (performRidging && _ridgeMethod != null) {
                var ridgeMethod = requireContext().getString(R.string.lbl_manual)
                if (_ridgeMethod == "tractor") {
                    ridgeMethod = requireContext().getString(R.string.lbl_tractor)
                }
                ridgeStr.append(ridgeMethod)
            } else {
                ridgeStr.append(requireContext().getString(R.string.lbl_no_ridging))
            }
        }

        val location =
            mathHelper.removeLeadingZero(lat, "#.####") + "," + mathHelper.removeLeadingZero(
                lon,
                "#.####"
            )

        mDataList = ArrayList()
        mDataList.add(
            TimeLineModel(
                requireContext().getString(R.string.lbl_country),
                countryName,
                if (countrySelected) StepStatus.COMPLETED else StepStatus.INCOMPLETE
            )
        )
        mDataList.add(
            TimeLineModel(
                requireContext().getString(R.string.lbl_farm_place),
                location,
                if (locationPicked) StepStatus.COMPLETED else StepStatus.INCOMPLETE
            )
        )
        mDataList.add(
            TimeLineModel(
                requireContext().getString(R.string.lbl_farm_size),
                fieldInfo,
                if (fieldSizeSelected) StepStatus.COMPLETED else StepStatus.INCOMPLETE
            )
        )

        mDataList.add(
            TimeLineModel(
                requireContext().getString(R.string.lbl_planting_date),
                plantingDate,
                if (plantingDateProvided) StepStatus.COMPLETED else StepStatus.INCOMPLETE
            )
        )
        mDataList.add(
            TimeLineModel(
                requireContext().getString(R.string.lbl_harvesting_date),
                harvestDate,
                if (harvestDateProvided) StepStatus.COMPLETED else StepStatus.INCOMPLETE
            )
        )

        if (countryCode != EnumCountry.Ghana.countryCode()) {
            mDataList.add(
                TimeLineModel(
                    requireContext().getString(R.string.lbl_ploughing),
                    ploughStr.toString(),
                    if (currentPracticeSelected) StepStatus.COMPLETED else StepStatus.INCOMPLETE
                )
            )
            mDataList.add(
                TimeLineModel(
                    requireContext().getString(R.string.lbl_ridging),
                    ridgeStr.toString(),
                    if (currentPracticeSelected) StepStatus.COMPLETED else StepStatus.INCOMPLETE
                )
            )
        }

        mDataList.add(
            TimeLineModel(
                requireContext().getString(R.string.lbl_risk_attitude),
                riskAttitudeName,
                StepStatus.COMPLETED
            )
        )
        initAdapter()
    }

    private fun initRecyclerView() {
        val mLayoutManager = LinearLayoutManager(context)
        binding.timelineRecycler.apply {
            layoutManager = mLayoutManager
            setHasFixedSize(true)
        }
    }

    private fun initAdapter() {
        adapter =
            MyTimeLineAdapter(mDataList, mAttributes!!, requireContext(), TheItemAnimation.FADE_IN)
        binding.timelineRecycler.adapter = adapter
    }

    override fun verifyStep(): VerificationError? {
        return null
    }

    override fun onSelected() {
        setDataListItems()
    }

    override fun onError(error: VerificationError) {
    }
}
