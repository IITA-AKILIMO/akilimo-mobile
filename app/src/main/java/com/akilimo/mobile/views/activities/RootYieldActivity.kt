package com.akilimo.mobile.views.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.FieldYieldAdapter
import com.akilimo.mobile.databinding.ActivityRootYieldBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.enums.EnumAreaUnit
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.RootYieldDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration
import java.util.Locale

class RootYieldActivity : BindBaseActivity<ActivityRootYieldBinding>() {

    private lateinit var yieldDataHolder: YieldDataHolder
    private lateinit var mAdapter: FieldYieldAdapter

    companion object {
        private val yieldImages = arrayOf(
            R.drawable.yield_less_than_7point5,
            R.drawable.yield_7point5_to_15,
            R.drawable.yield_15_to_22point5,
            R.drawable.yield_22_to_30,
            R.drawable.yield_more_than_30
        )
    }

    override fun inflateBinding() = ActivityRootYieldBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        yieldDataHolder = initProfileData()

        setupToolbar(binding.toolbarLayout.toolbar, R.string.title_activity_cassava_root_yield) {
            validate(false)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                validate(true)
            }
        })

        setupTitle(yieldDataHolder.useCase, yieldDataHolder.areaUnit)

        val yieldOptions = generateYieldData(yieldDataHolder.areaUnit)
        var selectedIndex =
            yieldOptions.indexOfFirst { it.yieldAmount == yieldDataHolder.selectedYieldAmount }
        mAdapter = FieldYieldAdapter(
            showImage = false,
            isItemSelected = { it.yieldAmount == yieldDataHolder.selectedYieldAmount },
        ) { _, fieldYield, position ->
            val dialogFragment = RootYieldDialogFragment.newInstance(
                selectedFieldYield = fieldYield,
                onConfirmClick = {
                    val updatedYield = (yieldDataHolder.savedYield ?: FieldYield()).apply {
                        this.yieldAmount = fieldYield.yieldAmount
                        this.fieldYieldLabel = fieldYield.fieldYieldLabel
                    }

                    database.fieldYieldDao().insert(updatedYield)

                    selectedIndex = position
                    yieldDataHolder = yieldDataHolder.copy(
                        savedYield = updatedYield,
                        selectedYieldAmount = updatedYield.yieldAmount
                    )
                    mAdapter.notifyDataSetChanged()
                }
            )

            showDialogFragmentSafely(
                supportFragmentManager,
                dialogFragment,
                "RootYieldDialogFragment"
            )
        }

        binding.rootYieldRecycler.apply {
            layoutManager = GridLayoutManager(this@RootYieldActivity, 1)
            addItemDecoration(SpacingItemDecoration(1, dpToPx(this@RootYieldActivity, 3), true))
            setHasFixedSize(true)
            adapter = mAdapter
        }

        mAdapter.submitList(yieldOptions)

        binding.twoButtons.apply {
            btnFinish.text = getString(R.string.lbl_finish)
            btnFinish.setOnClickListener { validate(true) }
            btnCancel.setOnClickListener { closeActivity(false) }
        }
    }

    private fun initProfileData(): YieldDataHolder {
        val areaUnit = database.mandatoryInfoDao().findOne()?.areaUnit.orEmpty()
        val countryCode = database.profileInfoDao().findOne()?.countryCode.orEmpty()
        val currencyCode = database.profileInfoDao().findOne()?.currencyCode.orEmpty()
        val useCase = database.useCaseDao().findOne()
        val savedYield = database.fieldYieldDao().findOne()
        val selectedYieldAmount = savedYield?.yieldAmount ?: 0.0

        return YieldDataHolder(
            areaUnit,
            countryCode,
            currencyCode,
            useCase,
            savedYield,
            selectedYieldAmount
        )
    }

    private fun setupTitle(useCase: UseCase?, areaUnit: String) {
        val unitLabel = when (areaUnit.lowercase(Locale.getDefault())) {
            "ha", "hekta" -> getString(R.string.lbl_ha_yield)
            "are" -> getString(R.string.lbl_are_yield)
            else -> getString(R.string.lbl_acre_yield)
        }

        val titleRes = when (useCase?.useCaseName) {
            EnumUseCase.FR.name -> R.string.lbl_typical_yield_question_fr
            else -> R.string.lbl_typical_yield_question
        }

        binding.rootYieldTitle.text = getString(titleRes, unitLabel)
    }

    override fun validate(backPressed: Boolean) {
        if (yieldDataHolder.selectedYieldAmount <= 0) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_yield),
                getString(R.string.lbl_current_field_yield_prompt),
                getString(R.string.lbl_ok)
            )
            return
        }

        database.adviceStatusDao().insert(
            AdviceStatus(EnumAdviceTask.CURRENT_CASSAVA_YIELD.name, true)
        )

        closeActivity(false)
    }

    private fun generateYieldData(areaUnit: String): List<FieldYield> {
        val unitEnum = EnumAreaUnit.valueOf(areaUnit)
        val yieldLabels = unitEnum.yieldLabelIds()

        val yieldDefinitions = listOf(
            Triple(R.string.fcy_lower, R.string.lbl_low_yield, 3.75),
            Triple(R.string.fcy_about_the_same, R.string.lbl_normal_yield, 11.25),
            Triple(R.string.fcy_somewhat_higher, R.string.lbl_high_yield, 18.75),
            Triple(R.string.fcy_2_3_times_higher, R.string.lbl_very_high_yield, 26.25),
            Triple(R.string.fcy_more_than_3_times_higher, R.string.lbl_very_high_yield, 33.75)
        )

        return yieldDefinitions.mapIndexed { index, (labelRes, descRes, amount) ->
            createYieldObject(
                imageId = yieldImages[index],
                yieldLabel = labelRes,
                fieldYieldAmountLabel = yieldLabels[index],
                fieldYieldAmount = amount,
                fieldYieldDesc = descRes
            )
        }
    }

    private fun createYieldObject(
        imageId: Int,
        yieldLabel: Int,
        fieldYieldAmountLabel: Int,
        fieldYieldAmount: Double,
        fieldYieldDesc: Int
    ): FieldYield {
        return FieldYield().apply {
            this.imageId = imageId
            yieldAmount = fieldYieldAmount
            yieldLabel.let { this.fieldYieldLabel = getString(it) }
            fieldYieldAmountLabel.let { this.fieldYieldAmountLabel = getString(it) }
            fieldYieldDesc.let { this.fieldYieldDesc = getString(it) }
        }
    }

    data class YieldDataHolder(
        val areaUnit: String,
        val countryCode: String,
        val currencyCode: String,
        val useCase: UseCase?,
        val savedYield: FieldYield?,
        val selectedYieldAmount: Double
    )
}