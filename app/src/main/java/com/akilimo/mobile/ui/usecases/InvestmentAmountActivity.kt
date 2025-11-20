package com.akilimo.mobile.ui.usecases

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.adapters.InvestmentAmountAdapter
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityInvestmentAmountBinding
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.SelectedInvestment
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.InvestmentRepo
import com.akilimo.mobile.repos.SelectedInvestmentRepo
import com.akilimo.mobile.ui.components.ToolbarHelper
import com.akilimo.mobile.utils.MathHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InvestmentAmountActivity : BaseActivity<ActivityInvestmentAmountBinding>() {

    private lateinit var userRepo: AkilimoUserRepo
    private lateinit var investmentRepo: InvestmentRepo
    private lateinit var selectedInvestmentRepo: SelectedInvestmentRepo
    private lateinit var adapter: InvestmentAmountAdapter

    override fun inflateBinding() = ActivityInvestmentAmountBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepo = AkilimoUserRepo(database.akilimoUserDao())
        investmentRepo = InvestmentRepo(database.investmentAmountDao())
        selectedInvestmentRepo = SelectedInvestmentRepo(database.selectedInvestmentDao())

        ToolbarHelper(this, binding.lytToolbar.toolbar)
            .showBackButton(true)
            .onNavigationClick { finish() }
            .build()

        setupRecycler()
    }

    private fun setupRecycler() {
        adapter = InvestmentAmountAdapter { selected, amount ->
            saveInvestment(selected, amount)
        }

        binding.rvInvestments.apply {
            layoutManager = LinearLayoutManager(this@InvestmentAmountActivity)
            adapter = this@InvestmentAmountActivity.adapter
            setHasFixedSize(true)
        }
    }

    override fun observeSyncWorker() {
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: 0
            val country = user.farmCountry.orEmpty()
            val enumAreaUnit = EnumAreaUnit.entries.firstOrNull {
                it == user.enumAreaUnit
            } ?: EnumAreaUnit.ACRE
            val farmSize = user.farmSize ?: 1.0 // fallback to 1 acre

            launch {
                investmentRepo.observeAllByCountry(countryCode = country).collectLatest { list ->
                    val computedList = list.map { item ->
                        val base = MathHelper.convertFromAcres(farmSize, enumAreaUnit)
                        val investmentAmount = base * item.investmentAmount
                        item.copy(investmentAmount = investmentAmount)
                    }

                    adapter.submitList(computedList)
                    binding.rvInvestments.visibility =
                        if (computedList.isEmpty()) View.GONE else View.VISIBLE
                }
            }

            launch {
                selectedInvestmentRepo.observeSelected(userId).collectLatest { selected ->
                    if (selected == null) return@collectLatest
                    adapter.updateSelection(
                        investment = selected,
                        enumAreaUnit = enumAreaUnit,
                        farmSize = farmSize
                    )
                }
            }
        }
    }

    private fun saveInvestment(investmentAmount: InvestmentAmount, selectedAmount: Double) =
        safeScope.launch {
            val user = userRepo.getUser(sessionManager.akilimoUser) ?: return@launch
            val userId = user.id ?: return@launch
            val investment = selectedInvestmentRepo.getSelectedSync(userId)?.copy(
                investmentId = investmentAmount.id,
                chosenAmount = selectedAmount,
                isExactAmount = investmentAmount.exactAmount,
            ) ?: SelectedInvestment(
                userId = userId,
                investmentId = investmentAmount.id,
                chosenAmount = selectedAmount,
                isExactAmount = investmentAmount.exactAmount,
            )

            selectedInvestmentRepo.saveOrUpdate(investment)
        }
}
