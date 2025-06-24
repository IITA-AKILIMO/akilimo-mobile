package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.data.RiskOption
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumInvestmentPref
import io.sentry.Sentry
import kotlinx.coroutines.launch

class InvestmentPrefViewModel(
    private val application: Application,
    private val riskOptions: List<RiskOption>,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private val profileDao = database.profileInfoDao()

    private val _selectedRiskValue = MutableLiveData<String>(EnumInvestmentPref.RARELY.name)
    val selectedRiskValue: LiveData<String> = _selectedRiskValue

    fun loadInitialSelection() {
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                val profile = profileDao.findOne()
                val riskIndex = profile?.riskAtt

                val selected = if (riskIndex != null && riskIndex in riskOptions.indices) {
                    riskOptions[riskIndex]
                } else {
                    riskOptions.firstOrNull()
                }

                selected?.value?.let { _selectedRiskValue.postValue(it) }
            }.onFailure { Sentry.captureException(it) }
        }
    }


    fun selectRisk(riskOption: RiskOption) {
        _selectedRiskValue.postValue(riskOption.value)

        viewModelScope.launch(dispatchers.io) {
            try {
                val profile = database.profileInfoDao().findOne() ?: return@launch
                if (profile.riskAtt != riskOption.riskAtt) {
                    profile.riskAtt = riskOption.riskAtt
                    profileDao.update(profile)
                }

            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
        }
    }
}