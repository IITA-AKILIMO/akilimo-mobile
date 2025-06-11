package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityMaizeMarketBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.entities.MaizePrice
import com.akilimo.mobile.entities.MaizePriceResponse
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.enums.EnumMaizeProduceType
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.MaizePriceDialogFragment
import io.sentry.Sentry

class MaizeMarketActivity : BaseActivity() {
    var toolbar: Toolbar? = null

    var unitOfSaleGrainTitle: AppCompatTextView? = null
    var maizeCobPriceTitle: AppCompatTextView? = null
    var lblPricePerCob: AppCompatTextView? = null

    var unitOfSaleGrainCard: CardView? = null
    var maizeCobPriceCard: CardView? = null

    var rdgMaizeProduceType: RadioGroup? = null
    var rdgUnitOfSaleGrain: RadioGroup? = null


    var btnPickCobPrice: AppCompatButton? = null
    var btnFinish: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null

    private var _binding: ActivityMaizeMarketBinding? = null
    private val binding get() = _binding!!

    private var maizeMarket: MaizeMarket? = null
    private var produceType: String? = null
    private var unitPrice: Double = 0.00
    private var maizePriceList: List<MaizePrice>? = null
    private val selectionMade = false

    private var produceRadioIndex = 0
    private var grainUnitRadioIndex = 0
    private var grainUnitPriceRadioIndex = 0
    private var grainPrice: String? = null
    private var cobPrice: String? = null
    private var unitOfSale: String? = null
    private var unitOfSaleEnum = EnumUnitOfSale.ONE_KG
    private val dialogOpen = false
    private var dataIsValid = false
    private val exactPriceSelected = false
    private var grainPriceRequired = false
    private var cobPriceRequired = false

    private val unitPriceUSD = 0.0
    private var unitWeight = 0.0
    private val exactPrice = 0.0
    private val averagePrice = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMaizeMarketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.toolbar
        unitOfSaleGrainTitle = binding.marketContent.unitOfSaleGrainTitle
        maizeCobPriceTitle = binding.marketContent.maizeCobPriceTitle
        lblPricePerCob = binding.marketContent.lblPricePerCob
        unitOfSaleGrainCard = binding.marketContent.unitOfSaleGrainCard
        maizeCobPriceCard = binding.marketContent.maizeCobPriceCard
        rdgMaizeProduceType = binding.marketContent.rdgMaizeProduceType
        rdgUnitOfSaleGrain = binding.marketContent.rdgUnitOfSaleGrain
        btnPickCobPrice = binding.marketContent.btnPickCobPrice
        btnFinish = binding.marketContent.twoButtons.btnFinish
        btnCancel = binding.marketContent.twoButtons.btnCancel


        maizeMarket = database.maizeMarketDao().findOne()

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
            val myAkilimoCurrency = database.currencyDao().findOneByCurrencyCode(currencyCode)
            if (myAkilimoCurrency != null) {
                currencyName = myAkilimoCurrency.currencyName
            }
        }

        setupToolbar(binding.toolbar, R.string.title_activity_maize_market_outlet) {
            validate(false)
        }

        lblPricePerCob!!.text = currencyCode
        rdgMaizeProduceType!!.setOnCheckedChangeListener { group: RadioGroup?, radioIndex: Int ->
            grainPriceRequired = false
            cobPriceRequired = false
            when (radioIndex) {
                R.id.rdDryGrain -> {
                    unitOfSaleGrainTitle!!.visibility = View.VISIBLE
                    unitOfSaleGrainCard!!.visibility = View.VISIBLE
                    maizeCobPriceTitle!!.visibility = View.GONE
                    maizeCobPriceCard!!.visibility = View.GONE
                    produceType = EnumMaizeProduceType.GRAIN.name.lowercase()
                    grainPriceRequired = true
                }

                R.id.rdFreshCobs -> {
                    unitOfSaleGrainTitle!!.visibility = View.GONE
                    unitOfSaleGrainCard!!.visibility = View.GONE
                    maizeCobPriceTitle!!.visibility = View.VISIBLE
                    maizeCobPriceCard!!.visibility = View.VISIBLE
                    produceType = EnumMaizeProduceType.FRESH_COB.name.lowercase()
                    unitOfSale = EnumUnitOfSale.NA.unitOfSale(this@MaizeMarketActivity)
                    unitOfSaleEnum = EnumUnitOfSale.NA
                    unitPrice = -1.0
                    cobPriceRequired = true

                    maizeCobPriceTitle!!.text =
                        getString(R.string.lbl_price_per_cob_in_currency_unit, currencyName)
                }
            }
        }

        rdgUnitOfSaleGrain!!.setOnCheckedChangeListener { group: RadioGroup?, radioIndex: Int ->
            when (radioIndex) {
                R.id.rd_per_kg -> {
                    unitOfSale = EnumUnitOfSale.ONE_KG.unitOfSale(this@MaizeMarketActivity)
                    unitOfSaleEnum = EnumUnitOfSale.ONE_KG
                    unitWeight = EnumUnitOfSale.ONE_KG.unitWeight()
                }

                R.id.rd_50_kg_bag -> {
                    unitOfSale = EnumUnitOfSale.FIFTY_KG.unitOfSale(this@MaizeMarketActivity)
                    unitOfSaleEnum = EnumUnitOfSale.FIFTY_KG
                    unitWeight = EnumUnitOfSale.FIFTY_KG.unitWeight()
                }

                R.id.rd_100_kg_bag -> {
                    unitOfSale = EnumUnitOfSale.HUNDRED_KG.unitOfSale(this@MaizeMarketActivity)
                    unitOfSaleEnum = EnumUnitOfSale.HUNDRED_KG
                    unitWeight = EnumUnitOfSale.HUNDRED_KG.unitWeight()
                }
            }
        }

        btnFinish!!.setOnClickListener { view: View? -> validate(false) }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }
        btnPickCobPrice!!.setOnClickListener { view: View? ->
            unitOfSale = EnumUnitOfSale.FRESH_COB.unitOfSale(this@MaizeMarketActivity)
            unitWeight = EnumUnitOfSale.FRESH_COB.unitWeight()
            unitOfSaleEnum = EnumUnitOfSale.FRESH_COB
            showUnitGrainPriceDialog("cob")
        }

        //@TODO Ensure you cater for dry cob and fresh cob selections
        if (maizeMarket != null) {
            produceType = maizeMarket!!.produceType
            unitOfSale = maizeMarket!!.unitOfSale
            unitPrice = maizeMarket!!.unitPrice
            unitWeight = maizeMarket!!.unitWeight

            grainUnitRadioIndex = maizeMarket!!.grainUnitIdx
            produceRadioIndex = maizeMarket!!.produceIdx
            grainUnitPriceRadioIndex = maizeMarket!!.grainUnitPriceIdx


            grainPrice = maizeMarket!!.exactPrice.toString()
            cobPrice = maizeMarket!!.exactPrice.toString()

            rdgMaizeProduceType!!.check(produceRadioIndex)

            if (produceType == EnumMaizeProduceType.GRAIN.name.lowercase()) {
                rdgUnitOfSaleGrain!!.check(grainUnitRadioIndex)
            }
        }

        showCustomNotificationDialog()
        
        processMaizePrices()
    }

    override fun validate(backPressed: Boolean) {
        dataIsValid = false
        if (produceType == null) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_produce),
                getString(R.string.lbl_maize_produce_prompt)
            )
            return
        }

        produceRadioIndex = rdgMaizeProduceType!!.checkedRadioButtonId

        if (grainPriceRequired) {
            if (unitOfSale.isNullOrEmpty()) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_sale_unit),
                    getString(R.string.lbl_maize_sale_unit_prompt)
                )
                return
            }
            if (exactPriceSelected) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_grain_price),
                    getString(R.string.lbl_grain_price_prompt)
                )
                return
            }
            grainUnitRadioIndex = rdgUnitOfSaleGrain!!.checkedRadioButtonId
            dataIsValid = true
        }

        if (cobPriceRequired) {
            if (unitPrice <= 0.0) {
                showCustomWarningDialog(
                    getString(R.string.lbl_invalid_cob_price),
                    getString(R.string.lbl_cob_price_prompt)
                )
                return
            }
            dataIsValid = true
        }


        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTask.MARKET_OUTLET_MAIZE.name, dataIsValid))
        if (dataIsValid) {
            try {

                val market = maizeMarket ?: MaizeMarket()

                maizeMarket = market.apply {
                    produceType = this@MaizeMarketActivity.produceType!!
                    unitPrice = this@MaizeMarketActivity.unitPrice
                    unitOfSale = this@MaizeMarketActivity.unitOfSale
                    unitWeight = this@MaizeMarketActivity.unitWeight
                    exactPrice = this@MaizeMarketActivity.exactPrice

                    grainUnitPriceIdx = this@MaizeMarketActivity.grainUnitPriceRadioIndex
                    grainUnitIdx = this@MaizeMarketActivity.grainUnitRadioIndex
                    produceIdx = this@MaizeMarketActivity.produceRadioIndex
                }
                database.maizeMarketDao().insert(market)
                closeActivity(backPressed)
            } catch (ex: Exception) {
                Toast.makeText(this@MaizeMarketActivity, ex.message, Toast.LENGTH_SHORT).show()
                Sentry.captureException(ex)
            }
        }
    }

    fun onGrainUnitRadioButtonClicked(radioButton: View?) {
        if (radioButton != null && radioButton.isPressed) {
            showUnitGrainPriceDialog("grain")
        }
    }

    private fun showUnitGrainPriceDialog(produceType: String) {
        val arguments = Bundle()
        arguments.putString(MaizePriceDialogFragment.CURRENCY_CODE, currencyCode)
        arguments.putString(MaizePriceDialogFragment.CURRENCY_NAME, currencyName)
        arguments.putString(MaizePriceDialogFragment.COUNTRY_CODE, countryCode)
        arguments.putDouble(MaizePriceDialogFragment.SELECTED_PRICE, exactPrice)
        arguments.putDouble(MaizePriceDialogFragment.AVERAGE_PRICE, averagePrice)
        arguments.putString(MaizePriceDialogFragment.UNIT_OF_SALE, unitOfSale)
        arguments.putString(MaizePriceDialogFragment.PRODUCE_TYPE, produceType)
        arguments.putParcelable(MaizePriceDialogFragment.ENUM_UNIT_OF_SALE, unitOfSaleEnum)

        val priceDialogFragment = MaizePriceDialogFragment()
        priceDialogFragment.arguments = arguments

        priceDialogFragment.setOnDismissListener { selectedPrice: Double, isExactPrice: Boolean ->
            unitPrice = if (isExactPrice) selectedPrice else mathHelper.convertToUnitWeightPrice(
                selectedPrice,
                unitWeight
            )
            binding.marketContent.btnPickCobPrice.text =
                getString(R.string.lbl_cob_price, unitPrice, currencyCode)
        }
        showDialogFragmentSafely(
            fragmentManager = supportFragmentManager,
            dialogFragment = priceDialogFragment,
            tag = MaizePriceDialogFragment.ARG_ITEM_ID
        )
    }


    private fun processMaizePrices() {
        maizePriceList = database.maizePriceDao().findAll()
        if (maizePriceList!!.isEmpty()) {
            fetchMaizePrices()
        }
    }

    private fun fetchMaizePrices() {
        val call = AkilimoApi.apiService.getMaizePrices(countryCode = countryCode)
        call.enqueue(object : retrofit2.Callback<MaizePriceResponse> {
            override fun onResponse(
                call: retrofit2.Call<MaizePriceResponse>,
                response: retrofit2.Response<MaizePriceResponse>
            ) {
                if (response.isSuccessful) {
                    maizePriceList = response.body()!!.data
                    database.maizePriceDao().insertAll(maizePriceList!!)
                }
            }

            override fun onFailure(call: retrofit2.Call<MaizePriceResponse>, t: Throwable) {
                Sentry.captureException(t)
                Toast.makeText(this@MaizeMarketActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
