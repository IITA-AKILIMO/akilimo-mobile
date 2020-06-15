package com.iita.akilimo.entities

import com.orm.SugarRecord


class MaizePerformance : SugarRecord<MaizePerformance>() {
    var id: Long = 0
    var performanceRadioIndex = 0
    var maizePerformance: String? = null
    var performanceValue: String? = null
}