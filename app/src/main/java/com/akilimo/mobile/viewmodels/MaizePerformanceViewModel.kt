package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.viewmodels.base.BaseViewModel
import kotlinx.coroutines.withContext

class MaizePerformanceViewModel(
    private val application: Application,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : BaseViewModel(application) {
    private val _items = MutableLiveData<List<CropPerformance>>()
    val items: LiveData<List<CropPerformance>> = _items

    private val _selectedPerformanceScore = MutableLiveData(-1)
    val selectedPerformanceScore: LiveData<Int> = _selectedPerformanceScore

    private val _showMessage = MutableLiveData<String>()
    val showMessage: LiveData<String> = _showMessage

    private val _closeEvent = MutableLiveData<Boolean>()
    val closeEvent: LiveData<Boolean> = _closeEvent

    private val performanceImages = arrayOf(
        R.drawable.ic_maize_1,
        R.drawable.ic_maize_2,
        R.drawable.ic_maize_3,
        R.drawable.ic_maize_4,
        R.drawable.ic_maize_5,
    )

    init {
        loadInitialData()
    }

    private fun loadInitialData() = launchWithState {
        val record = database.maizePerformanceDao().findOne()
        _selectedPerformanceScore.value = record?.performanceScore ?: -1

        val poorSoil = application.getString(R.string.lbl_maize_performance_poor)
        val richSoil = application.getString(R.string.lbl_maize_performance_rich)

        val list = mutableListOf<CropPerformance>().apply {
            add(
                createPerformanceObject(
                    performanceImages[0],
                    poorSoil,
                    1,
                    "50",
                    R.string.lbl_knee_height
                )
            )
            add(
                createPerformanceObject(
                    performanceImages[1],
                    null,
                    2,
                    "150",
                    R.string.lbl_chest_height
                )
            )
            add(
                createPerformanceObject(
                    performanceImages[2],
                    null,
                    3,
                    "yellow",
                    R.string.lbl_yellowish_leaves
                )
            )
            add(
                createPerformanceObject(
                    performanceImages[3],
                    null,
                    4,
                    "green",
                    R.string.lbl_green_leaves
                )
            )
            add(
                createPerformanceObject(
                    performanceImages[4],
                    richSoil,
                    5,
                    "dark green",
                    R.string.lbl_dark_green_leaves
                )
            )
        }
        _items.postValue(list)
    }

    fun onPerformanceConfirmed(cropPerformance: CropPerformance, position: Int) = launchWithState {
        withContext(dispatchers.io) {
            val savedCropPerformance = database.maizePerformanceDao().findOne() ?: CropPerformance()
            savedCropPerformance.maizePerformance = cropPerformance.maizePerformance
            savedCropPerformance.performanceScore = cropPerformance.performanceScore
            database.maizePerformanceDao().insert(savedCropPerformance)
        }
        _selectedPerformanceScore.postValue(cropPerformance.performanceScore)
    }

    fun validateSelection(backPressed: Boolean = false) {
        if ((_selectedPerformanceScore.value ?: -1) < 0) {
            showSnackBar(R.string.lbl_maize_performance_prompt)
            return
        }
        _closeEvent.postValue(backPressed)
    }

    private fun createPerformanceObject(
        yieldImage: Int,
        performanceDesc: String?,
        performanceValue: Int,
        maizePerformanceDesc: String,
        maizePerformanceLabel: Int
    ): CropPerformance {
        val performance = CropPerformance()
        performance.imageId = yieldImage
        performance.maizePerformanceDesc = performanceDesc
        performance.performanceScore = performanceValue
        performance.maizePerformance = maizePerformanceDesc
        performance.maizePerformanceLabel = application.getString(maizePerformanceLabel)
        return performance
    }
}