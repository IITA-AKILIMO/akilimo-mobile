package com.akilimo.mobile.interfaces

import com.akilimo.mobile.entities.ProfileInfo

interface IRecommendationCallBack {
    fun onDataReceived(profileInfo: ProfileInfo)
    fun onDismiss()
}
