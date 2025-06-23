package com.akilimo.mobile.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase
import com.akilimo.mobile.data.InterestOption
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import com.akilimo.mobile.utils.PreferenceManager
import com.akilimo.mobile.utils.ValidationHelper
import io.sentry.Sentry
import kotlinx.coroutines.launch

class BioDataViewModel(
    private val app: Application,
    private val preferenceManager: PreferenceManager,
    private val database: AppDatabase = AppDatabase.getInstance(app),
    private val dispatchers: IDispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(app) {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    val genderOptions = listOf(
        InterestOption(app.getString(R.string.lbl_gender_prompt), ""),
        InterestOption(app.getString(R.string.lbl_female), "F"),
        InterestOption(app.getString(R.string.lbl_male), "M"),
        InterestOption(app.getString(R.string.lbl_prefer_not_to_say), "NA")
    )

    val interestOptions = listOf(
        InterestOption(app.getString(R.string.lbl_akilimo_interest_prompt), ""),
        InterestOption(app.getString(R.string.lbl_interest_farmer), "farmer"),
        InterestOption(app.getString(R.string.lbl_interest_extension_agent), "extension_agent"),
        InterestOption(app.getString(R.string.lbl_interest_agronomist), "agronomist"),
        InterestOption(app.getString(R.string.lbl_interest_curious), "curious")
    )

    fun loadUserProfile() {
        viewModelScope.launch(dispatchers.io) {
            try {
                val profile = database.profileInfoDao().findOne()
                profile?.let {
                    _userProfile.postValue(it)
                }
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
        }
    }

    fun saveUserProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        mobileCode: String,
        gender: String,
        interest: String
    ): String? {
        if (firstName.isBlank()) return app.getString(R.string.lbl_first_name_req)
        if (lastName.isBlank()) return app.getString(R.string.lbl_last_name_req)
        if (email.isNotBlank() && !ValidationHelper().isEmailValid(email))
            return app.getString(R.string.lbl_valid_email_req)
        if (gender.isBlank()) return app.getString(R.string.lbl_gender_prompt)
        if (interest.isBlank()) return app.getString(R.string.lbl_akilimo_interest_prompt)

        viewModelScope.launch(dispatchers.io) {
            try {
                val existing = database.profileInfoDao().findOne() ?: UserProfile()
                existing.apply {
                    this.firstName = firstName
                    this.lastName = lastName
                    this.gender = gender
                    this.akilimoInterest = interest
                    this.email = email
                    this.mobileCode = mobileCode
                    this.phoneNumber = phone
                    this.deviceToken = preferenceManager.deviceToken
                }
                database.profileInfoDao().insert(existing)
                _userProfile.postValue(existing)
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
        }

        return null
    }
}
