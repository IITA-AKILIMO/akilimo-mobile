package com.iita.akilimo.utils

import android.os.Build

import com.iita.akilimo.entities.Fertilizer
import com.iita.akilimo.entities.InterCropFertilizer
import java.util.stream.Collectors




object FertilizerList {

    fun removeFertilizerByType(
        fertilizerTypeList: MutableList<Fertilizer>,
        fertilizerTypeToRemove: String
    ): List<Fertilizer> {
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


    fun removeIntercropFertilizerByType(
        fertilizerTypeList: MutableList<InterCropFertilizer>,
        fertilizerTypeToRemove: String
    ): List<InterCropFertilizer> {
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
