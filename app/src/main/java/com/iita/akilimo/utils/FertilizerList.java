package com.iita.akilimo.utils;

import android.os.Build;

import com.iita.akilimo.models.Fertilizer;

import java.util.Iterator;
import java.util.List;

public class FertilizerList {


    @Deprecated
    public static List<Fertilizer> filterSelectedFertilizers(List<Fertilizer> fertilizerTypeList, List<Fertilizer> selectedFertilizers) {
        for (Fertilizer selectedFertilizer : selectedFertilizers) {
            Iterator itr = fertilizerTypeList.iterator();
            while (itr.hasNext()) {
                Fertilizer x = (Fertilizer) itr.next();
                if (x.getName().equalsIgnoreCase(selectedFertilizer.getName())) {
                    itr.remove(); //remove from fertilizer name list to prevent duplicate when we merge them
                }
            }
        }
        fertilizerTypeList.addAll(selectedFertilizers);

        return fertilizerTypeList;
    }

    public static List<Fertilizer> removeFertilizerByType(List<Fertilizer> fertilizerTypeList, final String fertilizerTypeToRemove) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fertilizerTypeList.removeIf(obj -> obj.getType().equalsIgnoreCase(fertilizerTypeToRemove));
        } else {
            //legacy android version
            for (Iterator<Fertilizer> iterator = fertilizerTypeList.iterator(); iterator.hasNext(); ) {
                if (iterator.next().getType().equalsIgnoreCase(fertilizerTypeToRemove)) {
                    iterator.remove();
                }
            }
        }

        return fertilizerTypeList;
    }
}
