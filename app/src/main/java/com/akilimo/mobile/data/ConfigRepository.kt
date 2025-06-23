package com.akilimo.mobile.data

import com.akilimo.mobile.interfaces.FuelrodApi

class ConfigRepository(private val api: FuelrodApi) {
    suspend fun fetchConfig(configName: String): Map<String, String> {
        val configList = api.readConfig(configName)
        if (configList.isEmpty()) throw Exception("Empty config list")
        return configList.associate { it.configName to it.configValue }
    }
}