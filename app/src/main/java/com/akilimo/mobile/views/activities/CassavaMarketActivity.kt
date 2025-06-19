package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.addCallback
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityCassavaMarketBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CassavaMarket
import com.akilimo.mobile.entities.CassavaPricePriceResponse
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.entities.StarchFactoryResponse
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.enums.EnumCassavaProduceType
import com.akilimo.mobile.utils.enums.EnumContext
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.CassavaPriceDialogFragment
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Response

class CassavaMarketActivity : BindBaseActivity<ActivityCassavaMarketBinding>() {

    private var selectedFactory: String? = "NA"
    private var produceType: String? = null
    private var unitPrice = 0.0
    private var unitPriceP1 = 0.0
    private var unitPriceP2 = 0.0
    private var unitPriceM1 = 0.0
    private var unitPriceM2 = 0.0

    private var useCase: String = "NA"
    private var unitOfSale: String = "NA"
    private var unitWeight: Double = 0.0
    private var harvestWindow = 0

    private var factoryRequired = false
    private var otherMarketsRequired = false
    private var dataIsValid = false
    private var selectionMade = false

    companion object {
        var useCaseTag: String = "useCase"
    }

    override fun inflateBinding() = ActivityCassavaMarketBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadSavedData()
        loadIntentExtras()
        setupBackPressedHandler()
        setupToolbar()
        setupUIControls()
        hideAll()

        showCustomNotificationDialog()

        processStarchFactories()
        processCassavaPrices()
        processData()
    }

    private fun loadSavedData() {
        database.cassavaMarketDao().findOne()?.let { cassavaMarket ->
            selectedFactory = cassavaMarket.starchFactory
            unitOfSale = cassavaMarket.unitOfSale.toString()
            unitPrice = cassavaMarket.unitPrice
            unitPriceM1 = cassavaMarket.unitPriceM1
            unitPriceM2 = cassavaMarket.unitPriceM2
            unitPriceP1 = cassavaMarket.unitPriceP1
            unitPriceP2 = cassavaMarket.unitPriceP2
        }

        database.scheduleDateDao().findOne()?.let { scheduledDate ->
            harvestWindow = scheduledDate.harvestWindow
        }

        database.profileInfoDao().findOne()?.let { profileInfo ->
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }
    }

    private fun loadIntentExtras() {
        useCase = intent.getStringExtra(useCaseTag) ?: "NA"
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this) {
            validate(true)
        }
    }

    private fun setupToolbar() {
        setupToolbar(binding.toolbar, R.string.title_activity_cassava_market_outlet) {
            validate(false)
        }
    }

    private fun setupUIControls() = with(binding.contentCassavaMarket) {
        setupUnitPriceButtons()

        setupMarketOutletRadioGroup()

        setupStarchFactoriesRadioGroup()

        setupUnitOfSaleRadioGroup()

        setupTwoButtons()
    }

    private fun setupUnitPriceButtons() = with(binding.contentCassavaMarket) {
        btnUpP1.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_P1) }
        btnUpP2.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_P2) }
        btnUpM1.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_M1) }
        btnUpM2.setOnClickListener { showUnitPriceDialog(EnumContext.UNIT_PRICE_M2) }
    }

    private fun setupMarketOutletRadioGroup() = with(binding.contentCassavaMarket) {
        rdgMarketOutlet.setOnCheckedChangeListener { _, checkedId ->
            factoryRequired = false
            otherMarketsRequired = false
            selectionMade = true

            when (checkedId) {
                R.id.rdFactory -> setupFactorySelection()
                R.id.rdOtherMarket -> setupOtherMarketSelection()
            }
        }
    }

    private fun setupFactorySelection() = with(binding.contentCassavaMarket) {
        hideAll(true)
        starchFactoryCard.visibility = View.VISIBLE
        factoryRequired = true
        produceType = EnumCassavaProduceType.ROOTS.name
        unitOfSale = EnumUnitOfSale.NA.name
        resetUnitPrices()
    }

    private fun setupOtherMarketSelection() = with(binding.contentCassavaMarket) {
        produceType = EnumCassavaProduceType.ROOTS.name
        selectedFactory = "NA"
        otherMarketsRequired = true
        hideAll(false)
        unitOfSaleCard.visibility = View.VISIBLE
        if (harvestWindow > 0) {
            monthOneWindowCard.visibility = View.VISIBLE
            monthTwoWindowCard.visibility = View.VISIBLE
        }
    }

    private fun setupStarchFactoriesRadioGroup() = with(binding.contentCassavaMarket) {
        rdgStarchFactories.setOnCheckedChangeListener { _, checkedRadioButtonId ->
            dataIsValid = validateStarchFactorySelection(checkedRadioButtonId)
        }
    }

    private fun validateStarchFactorySelection(checkedRadioButtonId: Int): Boolean {
        var isValid = false

        if (checkedRadioButtonId != -1) {
            val radioButton = findViewById<RadioButton>(checkedRadioButtonId)
            val factoryNameCountry = radioButton?.tag as? String

            val starchFactory = factoryNameCountry?.let {
                database.starchFactoryDao().findStarchFactoryByNameCountry(it)
            }

            if (starchFactory != null) {
                selectedFactory = starchFactory.factoryName
                isValid = true
            }
        }

        return isValid
    }


    private fun setupUnitOfSaleRadioGroup() = with(binding.contentCassavaMarket) {
        rdgUnitOfSale.setOnCheckedChangeListener { _, checkedId ->
            dataIsValid = false
            when (checkedId) {
                R.id.rd_per_kg -> setUnitOfSale(EnumUnitOfSale.ONE_KG)
                R.id.rd_50_kg_bag -> setUnitOfSale(EnumUnitOfSale.FIFTY_KG)
                R.id.rd_100_kg_bag -> setUnitOfSale(EnumUnitOfSale.HUNDRED_KG)
                R.id.rd_per_tonne -> setUnitOfSale(EnumUnitOfSale.TONNE)
            }
        }
    }

    private fun setUnitOfSale(unit: EnumUnitOfSale) {
        unitOfSale = unit.name
        unitWeight = unit.unitWeight()
    }

    private fun setupTwoButtons() = with(binding.contentCassavaMarket.twoButtons) {
        btnFinish.setOnClickListener { validate(false) }
        btnCancel.setOnClickListener { closeActivity(false) }

        val enumUseCase = EnumUseCase.valueOf(useCase)
        if (enumUseCase == EnumUseCase.CIM) {
            with(binding.contentCassavaMarket) {
                produceType = EnumCassavaProduceType.ROOTS.name
                factoryRequired = false
                otherMarketsRequired = false
                selectionMade = true
                marketOutLetTitle.visibility = View.GONE
                marketOutletCard.visibility = View.GONE
                unitOfSaleCard.visibility = View.VISIBLE
            }
        }
    }


    private fun resetUnitPrices() {
        unitPrice = 0.0
        unitPriceP1 = 0.0
        unitPriceP2 = 0.0
        unitPriceM1 = 0.0
        unitPriceM2 = 0.0
    }


    fun onRadioButtonClicked(radioButton: View?) {
        if (radioButton != null && radioButton.isPressed) {
            showUnitPriceDialog(EnumContext.UNIT_PRICE)
        }
    }

    override fun validate(backPressed: Boolean) {
        if (!validateFactory()) return
        if (!validateOtherMarkets()) return
        if (!validateSelection()) return

        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTask.MARKET_OUTLET_CASSAVA.name, dataIsValid))

        if (dataIsValid) {
            saveCassavaMarket(backPressed)
        }
    }

    private fun validateFactory(): Boolean {
        if (!factoryRequired) return true

        return when {
            selectedFactory.isNullOrEmpty() -> {
                showWarning(R.string.lbl_invalid_factory, R.string.lbl_factory_prompt)
                false
            }

            else -> true
        }
    }

    private fun validateOtherMarkets(): Boolean {
        if (!otherMarketsRequired) return true

        val isValid = when {
            produceType == null -> {
                showWarning(R.string.lbl_invalid_produce, R.string.lbl_produce_prompt)
                false
            }

            unitOfSale.isEmpty() -> {
                showWarning(R.string.lbl_invalid_sale_unit, R.string.lbl_sale_unit_prompt)
                false
            }

            unitPrice <= 0 -> {
                showWarning(R.string.lbl_invalid_unit_price, R.string.lbl_unit_price_prompt)
                false
            }

            else -> true
        }

        dataIsValid = isValid
        return isValid
    }

    private fun validateSelection(): Boolean {
        if (selectionMade) return true

        showWarning(R.string.lbl_nothing, R.string.lbl_nothing_prompt)
        return false
    }

    private fun showWarning(
        titleResId: Int,
        messageResId: Int,
        positiveBtnResId: Int = R.string.lbl_ok
    ) {
        showCustomWarningDialog(
            getString(titleResId),
            getString(messageResId),
            getString(positiveBtnResId)
        )
    }

    private fun saveCassavaMarket(backPressed: Boolean) {
        try {
            var cassavaMarket = database.cassavaMarketDao().findOne() ?: CassavaMarket()

            cassavaMarket.apply {
                starchFactory = selectedFactory
                isStarchFactoryRequired = factoryRequired
                produceType =
                    this@CassavaMarketActivity.produceType ?: EnumCassavaProduceType.ROOTS.name
            }

            database.cassavaMarketDao().insert(cassavaMarket)
            closeActivity(backPressed)
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }
    }


    private fun hideAll(clearMarket: Boolean = false) {
        binding.contentCassavaMarket.apply {
            if (clearMarket) {
                rdgStarchFactories.clearCheck()
            } else {
                rdgUnitOfSale.clearCheck()
            }

            starchFactoryCard.visibility = View.GONE
            unitOfSaleCard.visibility = View.GONE

            monthOneWindowCard.visibility = View.GONE
            monthTwoWindowCard.visibility = View.GONE
        }
    }


    private fun processStarchFactories() {
        val database = getDatabase(this@CassavaMarketActivity)
        val call = AkilimoApi.apiService.getStarchFactories(countryCode)
        call.enqueue(object : retrofit2.Callback<StarchFactoryResponse> {
            override fun onResponse(
                call: Call<StarchFactoryResponse>,
                response: Response<StarchFactoryResponse>
            ) {
                if (response.isSuccessful) {
                    val starchFactoriesList = response.body()!!.data
                    database.starchFactoryDao().insertAll(starchFactoriesList)
                    addFactoriesRadioButtons(starchFactoriesList)
                }

            }

            override fun onFailure(call: Call<StarchFactoryResponse>, t: Throwable) {
                Toast.makeText(this@CassavaMarketActivity, t.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(t)
            }
        })
    }

    private fun processCassavaPrices() {
        val database = getDatabase(this@CassavaMarketActivity)
        val call = AkilimoApi.apiService.getCassavaPrices(countryCode)
        call.enqueue(object : retrofit2.Callback<CassavaPricePriceResponse> {
            override fun onResponse(
                call: Call<CassavaPricePriceResponse>,
                response: Response<CassavaPricePriceResponse>
            ) {
                if (response.isSuccessful) {
                    val cassavaPriceList = response.body()!!.data
                    database.cassavaPriceDao().insertAll(cassavaPriceList)
                }
            }

            override fun onFailure(call: Call<CassavaPricePriceResponse>, t: Throwable) {
                Toast.makeText(this@CassavaMarketActivity, t.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(t)
            }

        })
    }


    private fun processData() {
        val database = getDatabase(this@CassavaMarketActivity)
        val starchFactoriesList =
            database.starchFactoryDao().findStarchFactoriesByCountry(countryCode)
        addFactoriesRadioButtons(starchFactoriesList)

        val cassavaMarket = database.cassavaMarketDao().findOne()
        if (cassavaMarket != null) {
            val sfRequired = cassavaMarket.isStarchFactoryRequired
            binding.contentCassavaMarket.rdgMarketOutlet.check(if (sfRequired) R.id.rdFactory else R.id.rdOtherMarket)
            if (!sfRequired) {
                produceType = cassavaMarket.produceType
                unitOfSale = cassavaMarket.unitOfSale ?: "NA"
                unitPrice = cassavaMarket.unitPrice
                val unitOfSaleEnum = EnumUnitOfSale.valueOf(unitOfSale)

                binding.contentCassavaMarket.apply {
                    when (unitOfSaleEnum) {
                        EnumUnitOfSale.ONE_KG -> rdgUnitOfSale.check(R.id.rd_per_kg)
                        EnumUnitOfSale.FIFTY_KG -> rdgUnitOfSale.check(R.id.rd_50_kg_bag)
                        EnumUnitOfSale.HUNDRED_KG -> rdgUnitOfSale.check(R.id.rd_100_kg_bag)
                        EnumUnitOfSale.TONNE -> rdgUnitOfSale.check(R.id.rd_per_tonne)
                        else -> {
                            /** DO Nothing **/
                        }
                    }
                }
            }
        }
    }

    private fun addFactoriesRadioButtons(starchFactoryList: List<StarchFactory>) {
        binding.contentCassavaMarket.rdgStarchFactories.removeAllViews()

        val params = RadioGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val dimension = this.resources.getDimension(R.dimen.spacing_medium).toInt()
        for (factory in starchFactoryList) {
            val radioLabel = factory.factoryLabel
            val factoryNameCountry = factory.factoryNameCountry
            if (!radioLabel.equals("NA", ignoreCase = true)) {
                val radioButton = RadioButton(this)
                radioButton.id = View.generateViewId()
                radioButton.tag = factoryNameCountry

                params.setMargins(0, 0, 0, dimension)
                radioButton.layoutParams = params

                radioButton.text = radioLabel

                binding.contentCassavaMarket.rdgStarchFactories.addView(radioButton)

                if (factory.factoryName == selectedFactory) {
                    radioButton.isChecked = true
                }
            }
        }
    }

    private fun showUnitPriceDialog(userContext: EnumContext) {
        val arguments = Bundle()
        arguments.putString(CassavaPriceDialogFragment.CURRENCY_CODE, currencyCode)
        arguments.putString(CassavaPriceDialogFragment.COUNTRY_CODE, countryCode)
        arguments.putDouble(CassavaPriceDialogFragment.SELECTED_PRICE, unitPrice)
        arguments.putDouble(CassavaPriceDialogFragment.AVERAGE_PRICE, unitPrice)
        arguments.putString(CassavaPriceDialogFragment.UNIT_OF_SALE, unitOfSale)

        val priceDialogFragment = CassavaPriceDialogFragment()
        priceDialogFragment.arguments = arguments

        priceDialogFragment.setOnDismissListener { selectedPrice: Double, isExactPrice: Boolean ->
            val setPrice =
                if (isExactPrice) selectedPrice else mathHelper.convertToUnitWeightPrice(
                    selectedPrice,
                    unitWeight
                )
            when (userContext) {
                EnumContext.UNIT_PRICE -> unitPrice = setPrice
                EnumContext.UNIT_PRICE_P1 -> unitPriceP1 = setPrice
                EnumContext.UNIT_PRICE_P2 -> unitPriceP2 = setPrice
                EnumContext.UNIT_PRICE_M1 -> unitPriceM1 = setPrice
                EnumContext.UNIT_PRICE_M2 -> unitPriceM2 = setPrice
            }
        }

        showDialogFragmentSafely(
            supportFragmentManager,
            priceDialogFragment,
            CassavaPriceDialogFragment.ARG_ITEM_ID
        )
    }

}
