package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.akilimo.mobile.R
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.databinding.ActivityCassavaMarketBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CassavaMarket
import com.akilimo.mobile.entities.CassavaPricePriceResponse
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.entities.StarchFactoryResponse
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumCassavaProduceType
import com.akilimo.mobile.utils.enums.EnumContext
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.CassavaPriceDialogFragment
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Response

class CassavaMarketActivity : BaseActivity() {

    var myToolbar: Toolbar? = null
    var marketOutLetTitle: AppCompatTextView? = null
    var factoryTitle: AppCompatTextView? = null
    var unitOfSaleTitle: AppCompatTextView? = null
    var rdgMarketOutlet: RadioGroup? = null
    var rdgStarchFactories: RadioGroup? = null
//    var rdgUnitOfSale: RadioGroup? = null

    var rdStarchFactory: RadioButton? = null

    var marketOutletCard: CardView? = null
    var starchFactoryCard: CardView? = null
    var unitOfSaleCard: CardView? = null
    var monthOneWindowCard: CardView? = null
    var monthTwoWindowCard: CardView? = null

    var btnFinish: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null

    private var _binding: ActivityCassavaMarketBinding? = null
    private val binding get() = _binding!!

    private var selectedFactory: String? = "NA"

    var produceType: String? = null
    private var unitPrice = 0.0
    private var unitPriceP1 = 0.0
    private var unitPriceP2 = 0.0
    private var unitPriceM1 = 0.0
    private var unitPriceM2 = 0.0

    var useCase: String = "NA"
    var unitOfSale: String = "NA"
    var unitWeight: Double = 0.0
    private var harvestWindow = 0

    private var factoryRequired = false
    private var otherMarketsRequired = false
    private var dataIsValid = false
    private var selectionMade = false

    companion object {
        var useCaseTag: String = "useCase"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        setContentView(R.layout.activity_cassava_market);
        _binding = ActivityCassavaMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Set ui elements
        binding.apply {
            myToolbar = toolbar
            marketOutLetTitle = contentCassavaMarket.marketOutLetTitle
            factoryTitle = contentCassavaMarket.factoryTitle
            unitOfSaleTitle = contentCassavaMarket.unitOfSaleTitle
            rdgMarketOutlet = contentCassavaMarket.rdgMarketOutlet
            rdgStarchFactories = contentCassavaMarket.rdgStarchFactories

            rdStarchFactory = contentCassavaMarket.rdFactory

            marketOutletCard = contentCassavaMarket.marketOutletCard
            starchFactoryCard = contentCassavaMarket.starchFactoryCard
            unitOfSaleCard = contentCassavaMarket.unitOfSaleCard
            monthOneWindowCard = contentCassavaMarket.monthOneWindowCard
            monthTwoWindowCard = contentCassavaMarket.monthTwoWindowCard

            btnFinish = contentCassavaMarket.twoButtons.btnFinish
            btnCancel = contentCassavaMarket.twoButtons.btnCancel

            contentCassavaMarket.btnUpP1.setOnClickListener { v: View? ->
                showUnitPriceDialog(
                    EnumContext.UNIT_PRICE_P1
                )
            }
            contentCassavaMarket.btnUpP2.setOnClickListener { v: View? ->
                showUnitPriceDialog(
                    EnumContext.UNIT_PRICE_P2
                )
            }
            contentCassavaMarket.btnUpM1.setOnClickListener { v: View? ->
                showUnitPriceDialog(
                    EnumContext.UNIT_PRICE_M1
                )
            }
            contentCassavaMarket.btnUpM2.setOnClickListener { v: View? ->
                showUnitPriceDialog(
                    EnumContext.UNIT_PRICE_M2
                )
            }
        }

        val cassavaMarket = database.cassavaMarketDao().findOne()
        if (cassavaMarket != null) {
            selectedFactory = cassavaMarket.starchFactory
            unitOfSale = cassavaMarket.unitOfSale.toString()
            unitPrice = cassavaMarket.unitPrice
            unitPriceM1 = cassavaMarket.unitPriceM1
            unitPriceM2 = cassavaMarket.unitPriceM2
            unitPriceP1 = cassavaMarket.unitPriceP1
            unitPriceP2 = cassavaMarket.unitPriceP2
        }
        val scheduledDate = database.scheduleDateDao().findOne()
        if (scheduledDate != null) {
            harvestWindow = scheduledDate.harvestWindow
        }

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }
        val intent = intent
        if (intent != null) {
            useCase = intent.getStringExtra(useCaseTag) ?: "NA"
        }

        onBackPressedDispatcher.addCallback(this) {
            validate(true)
        }

        initToolbar()
        initComponent()
        processStarchFactories()
        processCassavaPrices()
        processData()
    }


    override fun initToolbar() {
        myToolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(myToolbar)
        supportActionBar!!.title = getString(R.string.title_activity_cassava_market_outlet)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        myToolbar!!.setNavigationOnClickListener { v: View? -> validate(false) }
    }

    override fun initComponent() {
        rdgMarketOutlet!!.setOnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            factoryRequired = false
            otherMarketsRequired = false
            selectionMade = true
            if (radioIndex == R.id.rdFactory) {
                hideAll(true)
                factoryTitle!!.visibility = View.VISIBLE
                starchFactoryCard!!.visibility = View.VISIBLE
                factoryRequired = true
                produceType = EnumCassavaProduceType.ROOTS.name
                unitOfSale = EnumUnitOfSale.NA.name
                unitPrice = 0.0
                unitPriceP1 = 0.0
                unitPriceP2 = 0.0
                unitPriceM1 = 0.0
                unitPriceM2 = 0.0
            } else if (radioIndex == R.id.rdOtherMarket) {
                produceType = EnumCassavaProduceType.ROOTS.name
                selectedFactory = "NA"
                otherMarketsRequired = true
                hideAll(false)
                unitOfSaleTitle!!.visibility = View.VISIBLE
                unitOfSaleCard!!.visibility = View.VISIBLE
                if (harvestWindow > 0) {
                    monthOneWindowCard!!.visibility = View.VISIBLE
                    monthTwoWindowCard!!.visibility = View.VISIBLE
                }
            }
        }

        rdgStarchFactories!!.setOnCheckedChangeListener { radioGroup: RadioGroup, radioIndex: Int ->
            val radioButtonId = radioGroup.checkedRadioButtonId
            val database = getDatabase(this@CassavaMarketActivity)
            dataIsValid = false
            if (radioButtonId > -1) {
                val radioButton = findViewById<RadioButton>(radioButtonId)
                val factoryNameCountry = radioButton.tag as String
                val selectedStarchFactory = database.starchFactoryDao()
                    .findStarchFactoryByNameCountry(factoryNameCountry)
                if (selectedStarchFactory != null) {
                    selectedFactory = selectedStarchFactory.factoryName
                }
                dataIsValid = true
            }
        }

        binding.contentCassavaMarket.rdgUnitOfSale.setOnCheckedChangeListener { radioGroup: RadioGroup?, radioIndex: Int ->
            dataIsValid = false
            when (radioIndex) {
                R.id.rd_per_kg -> {
                    unitOfSale = EnumUnitOfSale.ONE_KG.name
                    unitWeight = EnumUnitOfSale.ONE_KG.unitWeight()
                }

                R.id.rd_50_kg_bag -> {
                    unitOfSale = EnumUnitOfSale.FIFTY_KG.name
                    unitWeight = EnumUnitOfSale.FIFTY_KG.unitWeight()
                }

                R.id.rd_100_kg_bag -> {
                    unitOfSale = EnumUnitOfSale.HUNDRED_KG.name
                    unitWeight = EnumUnitOfSale.HUNDRED_KG.unitWeight()
                }

                R.id.rd_per_tonne -> {
                    unitOfSale = EnumUnitOfSale.TONNE.name
                    unitWeight = EnumUnitOfSale.TONNE.unitWeight()
                }
            }
        }



        binding.contentCassavaMarket.apply {
            twoButtons.apply {
                btnFinish.setOnClickListener {
                    validate(
                        false
                    )
                }
                btnCancel.setOnClickListener { closeActivity(false) }
                val enumUseCase = EnumUseCase.valueOf(useCase)
                if (enumUseCase == EnumUseCase.CIM) {
                    produceType = EnumCassavaProduceType.ROOTS.name
                    factoryRequired = false
                    otherMarketsRequired = false
                    selectionMade = true
                    marketOutLetTitle.visibility = View.GONE
                    marketOutletCard.visibility = View.GONE
                    unitOfSaleTitle.visibility = View.VISIBLE
                    unitOfSaleCard.visibility = View.VISIBLE
                }
            }
        }

        showCustomNotificationDialog()
    }

    fun onRadioButtonClicked(radioButton: View?) {
        if (radioButton != null && radioButton.isPressed) {
            showUnitPriceDialog(EnumContext.UNIT_PRICE)
        }
    }

    override fun validate(backPressed: Boolean) {
        if (factoryRequired) {
            if (selectedFactory.isNullOrEmpty() || selectedFactory.equals(
                    "NA",
                    ignoreCase = true
                )
            ) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_factory),
                    getString(R.string.lbl_factory_prompt),
                    getString(R.string.lbl_ok)
                )
                return
            }
        } else if (otherMarketsRequired) {
            if (produceType == null) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_produce),
                    getString(R.string.lbl_produce_prompt)
                )
                return
            }
            if (unitOfSale.isEmpty()) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_sale_unit),
                    getString(R.string.lbl_sale_unit_prompt)
                )
                return
            }
            if (unitPrice <= 0) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_unit_price),
                    getString(R.string.lbl_unit_price_prompt)
                )
                return
            }

            dataIsValid = true
        }

        if (!selectionMade) {
            showCustomWarningDialog(
                getString(R.string.lbl_nothing),
                getString(R.string.lbl_nothing_prompt)
            )
            return
        }


        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTasks.MARKET_OUTLET_CASSAVA.name, dataIsValid))

        if (dataIsValid) {
            try {
                var cassavaMarket = database.cassavaMarketDao().findOne()
                if (cassavaMarket == null) {
                    cassavaMarket = CassavaMarket()
                }
                cassavaMarket.apply {
                    starchFactory = selectedFactory
                    isStarchFactoryRequired = factoryRequired
                    produceType = this@CassavaMarketActivity.produceType
                        ?: EnumCassavaProduceType.ROOTS.name
                }

                database.cassavaMarketDao().insert(cassavaMarket)
                closeActivity(backPressed)
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }

        }
    }

    private fun hideAll(clearMarket: Boolean) {
        binding.contentCassavaMarket.apply {
            if (clearMarket) {
                rdgStarchFactories.clearCheck()
            } else {
                rdgUnitOfSale.clearCheck()
            }

            factoryTitle.visibility = View.GONE
            starchFactoryCard.visibility = View.GONE

            unitOfSaleTitle.visibility = View.GONE
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
                } else {
                    rdStarchFactory!!.visibility = View.GONE
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


    protected fun processData() {
        val database = getDatabase(this@CassavaMarketActivity)
        val starchFactoriesList =
            database.starchFactoryDao().findStarchFactoriesByCountry(countryCode)
        addFactoriesRadioButtons(starchFactoriesList)

        val cassavaMarket = database.cassavaMarketDao().findOne()
        if (cassavaMarket != null) {
            val sfRequired = cassavaMarket.isStarchFactoryRequired
            rdgMarketOutlet!!.check(if (sfRequired) R.id.rdFactory else R.id.rdOtherMarket)
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
                        else -> {}
                    }
                }
            }
        }
    }

    private fun addFactoriesRadioButtons(starchFactoryList: List<StarchFactory>) {
        rdgStarchFactories!!.removeAllViews()

        val params = RadioGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val dimension = this.resources.getDimension(R.dimen.spacing_medium).toInt()
        for (factory in starchFactoryList) {
            val radioLabel = factory.factoryLabel
            val factoryNameCountry = factory.factoryNameCountry
            if (!radioLabel.equals("NA", ignoreCase = true)) {
                rdgStarchFactories!!.visibility = View.VISIBLE
                val radioButton = RadioButton(this)
                radioButton.id = View.generateViewId()
                radioButton.tag = factoryNameCountry

                params.setMargins(0, 0, 0, dimension)
                radioButton.layoutParams = params

                radioButton.text = radioLabel

                rdgStarchFactories!!.addView(radioButton)

                if (factory.factoryName == selectedFactory) {
                    radioButton.isChecked = true
                }
            } else {
                rdStarchFactory!!.visibility = View.GONE
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
