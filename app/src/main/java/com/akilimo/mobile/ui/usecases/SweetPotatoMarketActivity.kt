package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.dao.ProduceMarketRepo
import com.akilimo.mobile.databinding.ActivitySweetPotatoMarketBinding
import com.akilimo.mobile.repos.AkilimoUserRepo

class SweetPotatoMarketActivity : BaseActivity<ActivitySweetPotatoMarketBinding>() {

    private val userRepo by lazy { AkilimoUserRepo(database.akilimoUserDao()) }
    private val marketRepo by lazy { ProduceMarketRepo(database.produceMarketDao()) }

    override fun inflateBinding() = ActivitySweetPotatoMarketBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {

    }

}