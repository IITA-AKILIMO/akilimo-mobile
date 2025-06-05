package com.akilimo.mobile.views.activities

import com.akilimo.mobile.adapters.FertilizerGridAdapter

class FertilizersActivity : BaseFertilizersActivity(minSelection = 2) {
    override val mAdapter: FertilizerGridAdapter by lazy { FertilizerGridAdapter(this@FertilizersActivity) }
}