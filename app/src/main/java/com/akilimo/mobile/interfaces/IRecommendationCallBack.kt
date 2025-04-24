package com.akilimo.mobile.interfaces

import com.akilimo.mobile.entities.UserProfile

interface IRecommendationCallBack {
    fun onDataReceived(userProfile: UserProfile)
    fun onDismiss()
}
