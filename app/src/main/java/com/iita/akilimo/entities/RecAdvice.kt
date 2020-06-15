package com.iita.akilimo.entities

import com.orm.SugarRecord

class RecAdvice : SugarRecord<RecAdvice>() {
    var id: Long = 0
    var FR = false
    var CIM = false
    var CIS = false
    var BPP = false
    var SPH = false
    var SPP = false
    var useCase: String? = null
}