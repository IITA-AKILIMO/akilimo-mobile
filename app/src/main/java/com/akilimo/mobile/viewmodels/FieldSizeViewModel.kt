package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.utils.enums.EnumFieldArea
import io.sentry.Sentry
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FieldSizeViewModel(
    private val application: Application,
    private val mathHelper: MathHelper,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private val mandatoryInfoDao = database.mandatoryInfoDao()

    private val _areaSizeInput = MutableLiveData<Double>(0.0)
    val areaSizeInput: LiveData<Double> = _areaSizeInput

    private val _isExactArea = MutableLiveData<Boolean>(false)
    val isExactArea: LiveData<Boolean> = _isExactArea

    private val _areaUnitInput = MutableLiveData<String>(EnumAreaUnit.ACRE.name)
    val areaUnitInput: LiveData<String> = _areaUnitInput

    private val _displayAreaUnitInput = MutableLiveData<String>("")
    val displayAreaUnitInput: LiveData<String> = _displayAreaUnitInput

    private val _dataIsValid = MutableLiveData<Boolean>(false)
    val dataIsValid: LiveData<Boolean> = _dataIsValid

    private val _myFieldSize = MutableLiveData<String?>()
    val myFieldSize: LiveData<String?> = _myFieldSize

    private val _radioCheckId = MutableLiveData<Int?>()
    val radioCheckId: LiveData<Int?> = _radioCheckId

    fun loadInitialData() {
        viewModelScope.launch(dispatchers.io) {
            try {
                mandatoryInfoDao.findOne()?.let { info ->
                    withContext(dispatchers.main) {
                        _areaUnitInput.postValue(info.areaUnit)
                        _displayAreaUnitInput.postValue(info.displayAreaUnit)
                        _isExactArea.postValue(info.exactArea)
                        _areaSizeInput.postValue(info.areaSize)
                        _myFieldSize.postValue(info.areaSize.toString())
                        _dataIsValid.postValue(info.areaSize > 0.0)

                        _radioCheckId.value = when (info.areaSize) {
                            EnumFieldArea.QUARTER_ACRE.areaValue() -> R.id.rd_field_size_quarter_acre
                            EnumFieldArea.HALF_ACRE.areaValue() -> R.id.rd_field_size_half_acre
                            EnumFieldArea.ONE_ACRE.areaValue() -> R.id.rd_field_size_one_acre
                            EnumFieldArea.TWO_HALF_ACRE.areaValue() -> R.id.rd_field_size_two_half_acre
                            else -> R.id.rd_field_size_specify_area
                        }
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
            }
        }
    }

    fun onRadioSelected(checkedId: Int) {
        _isExactArea.postValue(checkedId == R.id.rd_field_size_specify_area)

        val inputValue = when (checkedId) {
            R.id.rd_field_size_quarter_acre -> EnumFieldArea.QUARTER_ACRE.areaValue()
            R.id.rd_field_size_half_acre -> EnumFieldArea.HALF_ACRE.areaValue()
            R.id.rd_field_size_one_acre -> EnumFieldArea.ONE_ACRE.areaValue()
            R.id.rd_field_size_two_half_acre -> EnumFieldArea.TWO_HALF_ACRE.areaValue()
            R.id.rd_field_size_specify_area -> EnumFieldArea.EXACT_AREA.areaValue()
            else -> 0.0
        }

        _areaSizeInput.postValue(inputValue)
        _radioCheckId.postValue(checkedId)

        if (isExactArea.value != true) {
            val converted =
                mathHelper.convertFromAcreToSpecifiedArea(inputValue, areaUnitInput.value ?: "")
            saveFieldSize(converted)
        }
    }

    fun saveExactArea(input: String) {
        val value = input.toDoubleOrNull() ?: return
        _areaSizeInput.postValue(value)
        _myFieldSize.postValue(input)
        _dataIsValid.postValue(value > 0.0)
        saveFieldSize(value)
    }


    private fun saveFieldSize(areaSize: Double) {
        viewModelScope.launch(dispatchers.io) {
            try {
                mandatoryInfoDao.findOne()?.let { info ->
                    info.areaSize = areaSize
                    info.oldAreaUnit = areaUnitInput.value ?: ""
                    info.exactArea = isExactArea.value == true
                    mandatoryInfoDao.insert(info)
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
            }
        }
    }
}