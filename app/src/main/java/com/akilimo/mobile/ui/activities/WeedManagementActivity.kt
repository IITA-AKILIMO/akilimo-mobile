package com.akilimo.mobile.ui.activities

import android.os.Bundle
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityWeedManagementBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeedManagementActivity : BaseActivity<ActivityWeedManagementBinding>() {

    override fun inflateBinding() = ActivityWeedManagementBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // TODO: implement weed management UI
    }
}
