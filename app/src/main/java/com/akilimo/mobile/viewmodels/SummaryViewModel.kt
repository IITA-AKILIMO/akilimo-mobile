package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.models.TimeLineModel
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import com.akilimo.mobile.utils.enums.EnumOperationMethod
import com.akilimo.mobile.utils.enums.StepStatus
import kotlinx.coroutines.launch


class SummaryViewModel(
    private val application: Application,
    private val mathHelper: MathHelper,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private val _timelineItems = MutableLiveData<List<TimeLineModel>>()
    val timelineItems: LiveData<List<TimeLineModel>> = _timelineItems

    fun loadSummaryData() {
        viewModelScope.launch(dispatchers.io) {
            val location = database.locationInfoDao().findOne()
            val mandatoryInfo = database.mandatoryInfoDao().findOne()
            val cropSchedule = database.scheduleDateDao().findOne()
            val userProfile = database.profileInfoDao().findOne()
            val currentPractice = database.currentPracticeDao().findOne()

            val countryName = userProfile?.countryName.orEmpty()
            val risks = EnumInvestmentPref.entries.map { it.prefName(application) }.toTypedArray()
            val riskAttitudeName = risks.getOrElse(userProfile?.riskAtt ?: 0) { "" }

            val fieldInfo = buildFieldInfo(mandatoryInfo)
            val locationString = buildLocationString(location)
            val plantingDate = cropSchedule?.plantingDate.orEmpty()
            val harvestDate = cropSchedule?.harvestDate.orEmpty()
            val ploughStr = buildPloughStr(currentPractice)
            val ridgeStr = buildRidgeStr(currentPractice)

            val items = mutableListOf<TimeLineModel>().apply {
                addItem(application.getString(R.string.lbl_country), countryName)
                addItem(application.getString(R.string.lbl_farm_place), locationString)
                addItem(application.getString(R.string.lbl_farm_size), fieldInfo)
                addItem(application.getString(R.string.lbl_planting_date), plantingDate)
                addItem(application.getString(R.string.lbl_harvesting_date), harvestDate)
                addItem(application.getString(R.string.lbl_ploughing), ploughStr)
                addItem(application.getString(R.string.lbl_ridging), ridgeStr)
                addItem(application.getString(R.string.lbl_risk_attitude), riskAttitudeName)
            }

            _timelineItems.postValue(items)
        }
    }

    private fun MutableList<TimeLineModel>.addItem(label: String, value: String) {
        add(
            TimeLineModel(
                label,
                value,
                if (value.isNotEmpty()) StepStatus.COMPLETED else StepStatus.INCOMPLETE
            )
        )
    }

    private fun buildFieldInfo(info: MandatoryInfo?): String {
        if (info == null) return ""
        val lang = LanguageManager.getLanguage(application)
        return if (lang.equals("sw", ignoreCase = true)) {
            "${info.displayAreaUnit} ${info.areaSize}"
        } else {
            "${info.areaSize} ${info.displayAreaUnit}"
        }
    }

    private fun buildLocationString(location: UserLocation?): String {
        return location?.let {
            "${mathHelper.removeLeadingZero(it.latitude, "#.####")}," +
                    mathHelper.removeLeadingZero(it.longitude, "#.####")
        } ?: ""
    }

    private fun buildPloughStr(practice: CurrentPractice?): String {
        return when (practice?.ploughingMethod) {
            EnumOperationMethod.TRACTOR -> application.getString(com.akilimo.mobile.R.string.lbl_tractor)
            EnumOperationMethod.MANUAL -> application.getString(com.akilimo.mobile.R.string.lbl_manual)
            else -> application.getString(com.akilimo.mobile.R.string.lbl_no_ploughing)
        }
    }

    private fun buildRidgeStr(practice: CurrentPractice?): String {
        return when (practice?.ridgingMethod) {
            EnumOperationMethod.TRACTOR -> application.getString(com.akilimo.mobile.R.string.lbl_tractor)
            EnumOperationMethod.MANUAL -> application.getString(com.akilimo.mobile.R.string.lbl_manual)
            else -> application.getString(com.akilimo.mobile.R.string.lbl_no_ridging)
        }
    }
}