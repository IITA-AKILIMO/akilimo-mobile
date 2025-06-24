package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.LanguageManager
import com.akilimo.mobile.utils.LanguageOption
import com.akilimo.mobile.utils.Locales

class WelcomeViewModel(
    private val application: Application,
    private val database: AppDatabase = AppDatabase.getInstance(application),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    private val _languageOptions = MutableLiveData<List<LanguageOption>>()
    val languageOptions: LiveData<List<LanguageOption>> = _languageOptions

    private val _selectedIndex = MutableLiveData<Int>()
    val selectedIndex: LiveData<Int> = _selectedIndex

    private val _languagePicked = MutableLiveData(false)
    val languagePicked: LiveData<Boolean> = _languagePicked


    fun loadLanguages() {
        val options = Locales.LOCALE_COUNTRIES.map {
            LanguageOption(it.language, it.getDisplayLanguage(it))
        }
        _languageOptions.value = options

        val savedCode = LanguageManager.getLanguage(application)
        val index = options.indexOfFirst { it.code == savedCode }
        _selectedIndex.postValue(if (index >= 0) index else 0)
    }

    fun setLanguagePicked() {
        _languagePicked.postValue(true)
    }

    fun onLanguageSelected(languageCode: String) {
        LanguageManager.saveLanguage(application, languageCode)
        LanguageManager.setLocale(application, languageCode)
    }
}
