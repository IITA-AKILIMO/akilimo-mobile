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
import com.akilimo.mobile.databinding.ActivitySweetPotatoMarketBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.PotatoMarket
import com.akilimo.mobile.entities.PotatoPrice
import com.akilimo.mobile.entities.PotatoPriceResponse
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.AkilimoApi
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumPotatoProduceType
import com.akilimo.mobile.utils.enums.EnumUnitOfSale
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.SweetPotatoPriceDialogFragment
import io.sentry.Sentry

class SweetPotatoMarketActivity : BaseActivity() {
    var toolbar: Toolbar? = null
    var unitOfSalePotatoTitle: AppCompatTextView? = null
    var unitOfSalePotatoCard: CardView? = null
    var rdgPotatoProduceType: RadioGroup? = null
    var rdgUnitOfSalePotato: RadioGroup? = null

    var btnFinish: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null

    private var _binding: ActivitySweetPotatoMarketBinding? = null
    private val binding get() = _binding!!
    private var potatoMarket: PotatoMarket? = null
    private var enumPotatoProduceType: String? = null
    private var potatoPriceList: List<PotatoPrice>? = null
    private val selectionMade = false

    private var unitOfSale: String? = null
    private var unitOfSaleEnum = EnumUnitOfSale.FIFTY_KG

    private var produceTypeRadioIndex = 0
    private var potatoUnitOfSaleRadioIndex = 0
    private var potatoUnitPriceRadioIndex = 0


    var unitPriceUSD: Double = 0.0
    var unitPriceLocal: Double = 0.0
    private var unitPrice = 0.0
    private var unitWeight = 0.0

    private val minAmountUSD = 5.00
    private val maxAmountUSD = 500.00

    private val dialogOpen = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySweetPotatoMarketBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        toolbar = binding.toolbar
        unitOfSalePotatoTitle = binding.potatoMarket.unitOfSalePotatoTitle
        unitOfSalePotatoCard = binding.potatoMarket.unitOfSalePotatoCard
        rdgPotatoProduceType = binding.potatoMarket.rdgPotatoProduceType
        rdgUnitOfSalePotato = binding.potatoMarket.rdgUnitOfSalePotato
        btnFinish = binding.potatoMarket.twoButtons.btnFinish
        btnCancel = binding.potatoMarket.twoButtons.btnCancel

        initToolbar()
        initComponent()
        processPotatoPrices()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.lbl_sweet_potato_prices)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> validate(false) }
    }

    override fun initComponent() {
        enumPotatoProduceType = EnumPotatoProduceType.TUBERS.name.lowercase()

        val potatoMarket = database.potatoMarketDao().findOne()
        if (potatoMarket != null) {
            produceTypeRadioIndex = potatoMarket.produceTypeIdx
            potatoUnitOfSaleRadioIndex = potatoMarket.potatoUnitOfSaleIdx
            potatoUnitPriceRadioIndex = potatoMarket.potatoUnitPriceIdx

            unitPrice = potatoMarket.unitPrice
            unitOfSale = potatoMarket.unitOfSale
            unitWeight = potatoMarket.unitWeight
            rdgPotatoProduceType?.check(produceTypeRadioIndex)
            if (unitWeight > 0) {
                rdgUnitOfSalePotato?.check(potatoUnitOfSaleRadioIndex)
            }
        }

        rdgUnitOfSalePotato!!.setOnCheckedChangeListener { group: RadioGroup?, radioIndex: Int ->
            val context = this@SweetPotatoMarketActivity
            when (radioIndex) {
                R.id.rd_per_kg -> {
                    unitOfSale = EnumUnitOfSale.ONE_KG.unitOfSale(context)
                    unitOfSaleEnum = EnumUnitOfSale.ONE_KG
                    unitWeight = EnumUnitOfSale.ONE_KG.unitWeight()
                }

                R.id.rd_50_kg_bag -> {
                    unitOfSale = EnumUnitOfSale.FIFTY_KG.unitOfSale(context)
                    unitOfSaleEnum = EnumUnitOfSale.FIFTY_KG
                    unitWeight = EnumUnitOfSale.FIFTY_KG.unitWeight()
                }

                R.id.rd_100_kg_bag -> {
                    unitOfSale = EnumUnitOfSale.HUNDRED_KG.unitOfSale(context)
                    unitOfSaleEnum = EnumUnitOfSale.HUNDRED_KG
                    unitWeight = EnumUnitOfSale.HUNDRED_KG.unitWeight()
                }

                R.id.rd_1000_kg_bag -> {
                    unitOfSale = EnumUnitOfSale.TONNE.unitOfSale(context)
                    unitOfSaleEnum = EnumUnitOfSale.TONNE
                    unitWeight = EnumUnitOfSale.TONNE.unitWeight()
                }
            }
        }
        btnFinish!!.setOnClickListener { view: View? -> validate(false) }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }

        showCustomNotificationDialog()
    }

    override fun validate(backPressed: Boolean) {
        produceTypeRadioIndex = rdgPotatoProduceType!!.checkedRadioButtonId
        potatoUnitOfSaleRadioIndex = rdgUnitOfSalePotato!!.checkedRadioButtonId
        if (enumPotatoProduceType == null) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_produce),
                getString(R.string.lbl_potato_produce_prompt)
            )
            return
        }
        if (unitOfSale.isNullOrEmpty()) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_sale_unit),
                getString(R.string.lbl_potato_sale_unit_prompt)
            )
            return
        }

        if (unitPrice <= 0) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_tuber_price),
                getString(R.string.lbl_tuber_price_prompt)
            )
            return
        }

        try {

            val market = potatoMarket ?: PotatoMarket()
            potatoMarket = market.apply {
                produceType = enumPotatoProduceType!!
                unitOfSale = this@SweetPotatoMarketActivity.unitOfSale
                unitWeight = this@SweetPotatoMarketActivity.unitWeight
                unitPrice = this@SweetPotatoMarketActivity.unitPrice
                produceTypeIdx = this@SweetPotatoMarketActivity.produceTypeRadioIndex
                potatoUnitPriceIdx = this@SweetPotatoMarketActivity.potatoUnitPriceRadioIndex
                potatoUnitOfSaleIdx = this@SweetPotatoMarketActivity.potatoUnitOfSaleRadioIndex
            }

            database.potatoMarketDao().insert(market)
            database.adviceStatusDao()
                .insert(AdviceStatus(EnumAdviceTasks.MARKET_OUTLET_SWEET_POTATO.name, true))
            closeActivity(backPressed)
        } catch (ex: Exception) {
            Toast.makeText(this@SweetPotatoMarketActivity, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    fun onPotatoUnitRadioButtonClicked(radioButton: View?) {
        if (radioButton != null && radioButton.isPressed) {
            showPotatoUnitPriceDialog()
        }
    }

    private fun processPotatoPrices() {
        potatoPriceList = database.potatoPriceDao().findAll()
        if (potatoPriceList!!.isEmpty()) {
            fetchPotatoPrices()
        }
    }

    private fun fetchPotatoPrices() {
        val call = AkilimoApi.apiService.getPotatoPrices(countryCode = countryCode)
        call.enqueue(object : retrofit2.Callback<PotatoPriceResponse> {
            override fun onResponse(
                call: retrofit2.Call<PotatoPriceResponse>,
                response: retrofit2.Response<PotatoPriceResponse>
            ) {
                if (response.isSuccessful) {
                    potatoPriceList = response.body()!!.data
                    database.potatoPriceDao().insertAll(potatoPriceList!!)
                }
            }

            override fun onFailure(call: retrofit2.Call<PotatoPriceResponse>, t: Throwable) {
                Sentry.captureException(t)
                Toast.makeText(this@SweetPotatoMarketActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPotatoUnitPriceDialog() {
        val arguments = Bundle().apply {
            putString(SweetPotatoPriceDialogFragment.CURRENCY_CODE, currencyCode)
            putString(SweetPotatoPriceDialogFragment.COUNTRY_CODE, countryCode)
            putDouble(SweetPotatoPriceDialogFragment.SELECTED_PRICE, unitPrice)
            putString(SweetPotatoPriceDialogFragment.UNIT_OF_SALE, unitOfSale)
            putParcelable(
                SweetPotatoPriceDialogFragment.ENUM_UNIT_OF_SALE,
                unitOfSaleEnum
            )
        }

        val priceDialogFragment = SweetPotatoPriceDialogFragment()
        priceDialogFragment.arguments = arguments

        priceDialogFragment.setOnDismissListener { selectedPrice: Double, isExactPrice: Boolean ->
            unitPrice = if (isExactPrice) {
                selectedPrice
            } else {
                mathHelper.convertToUnitWeightPrice(selectedPrice, unitWeight)
            }
        }


        showDialogFragmentSafely(
            supportFragmentManager,
            priceDialogFragment,
            SweetPotatoPriceDialogFragment.ARG_ITEM_ID
        )
    }
}
