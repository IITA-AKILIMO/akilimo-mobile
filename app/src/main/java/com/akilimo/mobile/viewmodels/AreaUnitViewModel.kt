package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.utils.enums.EnumCountry
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AreaUnitViewModel(
    private val app: Application,
    private val database: AppDatabase = AppDatabase.getInstance(app),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(app) {

    private val profileDao = database.profileInfoDao()
    private val mandatoryInfoDao = database.mandatoryInfoDao()

    private val _areaUnit = MutableLiveData<String>(EnumAreaUnit.ACRE.name)
    val areaUnit: LiveData<String> = _areaUnit

    private val _areaUnitDisplay = MutableLiveData<String>()
    val areaUnitDisplay: LiveData<String> = _areaUnitDisplay

    private val _oldAreaUnit = MutableLiveData<String?>()
    val oldAreaUnit: LiveData<String?> = _oldAreaUnit

    private val _showAreUnit = MutableLiveData<Boolean>(false)
    val showAreUnit: LiveData<Boolean> = _showAreUnit


    fun loadData() {
        viewModelScope.launch {
            try {
                val mandatoryInfo = withContext(dispatchers.io) {
                    mandatoryInfoDao.findOne()
                }
                val profileInfo = withContext(dispatchers.io) {
                    profileDao.findOne()
                }

                val defaultUnit = mandatoryInfo?.areaUnit ?: EnumAreaUnit.ACRE.name
                _areaUnit.postValue(defaultUnit)
                _oldAreaUnit.postValue(mandatoryInfo?.oldAreaUnit)
                _areaUnitDisplay.postValue(mandatoryInfo?.displayAreaUnit ?: "")

                if (profileInfo?.countryCode == EnumCountry.Rwanda.countryCode()) {
                    _showAreUnit.postValue(true)
                }

            } catch (e: Exception) {
                // You can emit error state here if needed
            }
        }
    }

    fun updateUnit(unit: EnumAreaUnit, displayName: String) {
        _areaUnit.value = unit.name
        _areaUnitDisplay.value = displayName

        viewModelScope.launch(dispatchers.io) {
            val dao = database.mandatoryInfoDao()
            val current = dao.findOne() ?: MandatoryInfo()
            current.areaUnit = unit.name
            current.displayAreaUnit = displayName
            dao.insert(current)
        }
    }
}