package com.akilimo.mobile.views.activities

import com.akilimo.mobile.adapters.FertilizerGridAdapter

class InterCropFertilizersActivity : BaseFertilizersActivity(1) {

    override val mAdapter: FertilizerGridAdapter by lazy { FertilizerGridAdapter(this@InterCropFertilizersActivity) }
}
