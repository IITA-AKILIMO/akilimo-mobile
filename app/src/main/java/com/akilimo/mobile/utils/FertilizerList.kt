package com.akilimo.mobile.utils

import android.os.Build

import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.InterCropFertilizer


object FertilizerList {

    fun removeFertilizerByType(
        fertilizerTypeList: MutableList<Fertilizer>,
        fertilizerTypeToRemove: String
    ): MutableList<Fertilizer> {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> fertilizerTypeList.removeIf { obj ->
                obj.fertilizerType.equals(fertilizerTypeToRemove, ignoreCase = true)
            }
            else -> {
                val iterator = fertilizerTypeList.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().fertilizerType.equals(
                            fertilizerTypeToRemove,
                            ignoreCase = true
                        )
                    ) {
                        iterator.remove()
                    }
                }
            }
        }

        return fertilizerTypeList
    }



    @Deprecated("Replae with removeFertilizerByType")
    fun removeIntercropFertilizerByType(
        fertilizerTypeList: MutableList<Fertilizer>,
        fertilizerTypeToRemove: String
    ): MutableList<Fertilizer> {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> fertilizerTypeList.removeIf { obj ->
                obj.fertilizerType.equals(fertilizerTypeToRemove, ignoreCase = true)
            }
            else -> {
                val iterator = fertilizerTypeList.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().fertilizerType.equals(
                            fertilizerTypeToRemove,
                            ignoreCase = true
                        )
                    ) {
                        iterator.remove()
                    }
                }
            }
        }

        return fertilizerTypeList
    }
}
