package com.iita.akilimo.interfaces

import com.iita.akilimo.entities.ProfileInfo

interface IRecommendationCallBack {
    fun onDataReceived(profileInfo: ProfileInfo)
    fun onDismiss()
}
