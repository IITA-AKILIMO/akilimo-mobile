package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.FieldYieldAdapter
import com.akilimo.mobile.databinding.ActivityRootYieldBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.entities.UseCase
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.IFieldYieldDismissListener
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.enums.EnumUseCase
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.RootYieldDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration
import java.util.Locale

class RootYieldActivity : BaseActivity() {

    private var _binding: ActivityRootYieldBinding? = null
    private val binding get() = _binding!!

    private var savedYield: FieldYield? = null
    private var useCase: UseCase? = null
    private var mAdapter: FieldYieldAdapter? = null

    private var selectedYieldAmount = 0.0
    private val yieldImages = arrayOf(
        R.drawable.yield_less_than_7point5,
        R.drawable.yield_7point5_to_15,
        R.drawable.yield_15_to_22point5,
        R.drawable.yield_22_to_30,
        R.drawable.yield_more_than_30,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityRootYieldBinding.inflate(layoutInflater)

        setContentView(binding.root)


        val mandatoryInfo = database.mandatoryInfoDao().findOne()
        useCase = database.useCaseDao().findOne()
        if (mandatoryInfo != null) {
            areaUnit = mandatoryInfo.areaUnit
        }
        val profileInfo = database.profileInfoDao().findOne()
        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            currencyCode = profileInfo.currencyCode
        }

        savedYield = database.fieldYieldDao().findOne()
        if (savedYield != null) {
            selectedYieldAmount = savedYield!!.yieldAmount
        }
        initToolbar()
        initComponent()
    }


    override fun initToolbar() {
        val toolbar = binding.toolbarLayout.toolbar
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_activity_cassava_root_yield)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener { v: View? -> validate(false) }
    }

    override fun initComponent() {
        val recyclerView = binding.rootYieldRecycler

        binding.twoButtons.btnFinish.text = getString(R.string.lbl_finish)
        recyclerView.layoutManager = GridLayoutManager(this, 1)
        recyclerView.addItemDecoration(SpacingItemDecoration(1, dpToPx(this, 3), true))
        recyclerView.setHasFixedSize(true)

        var tonnage = getString(R.string.lbl_acre_yield)
        if (areaUnit.equals("ha", ignoreCase = true) || areaUnit.equals(
                "hekta",
                ignoreCase = true
            )
        ) {
            tonnage = getString(R.string.lbl_ha_yield)
        } else if (areaUnit == "are") {
            tonnage = getString(R.string.lbl_are_yield)
        }

        var title = getString(R.string.lbl_typical_yield_question, tonnage)
        if (useCase != null) {
            if (useCase!!.useCaseName == EnumUseCase.FR.name) {
                title = getString(R.string.lbl_typical_yield_question_fr, tonnage)
            }
        }
        binding.rootYieldTitle.text = title

        val items = setYieldData(areaUnit)
        //set data and list adapter
        mAdapter = FieldYieldAdapter(this@RootYieldActivity, items, TheItemAnimation.FADE_IN)
        recyclerView.adapter = mAdapter
        mAdapter!!.setItems(selectedYieldAmount, items)

        mAdapter!!.setOnItemClickListener(object : FieldYieldAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, fieldYield: FieldYield?, position: Int) {
                val arguments = Bundle().apply {
                    putParcelable(RootYieldDialogFragment.YIELD_DATA, fieldYield)
                }

                val rootYieldDialogFragment = RootYieldDialogFragment()
                rootYieldDialogFragment.arguments = arguments

                rootYieldDialogFragment.setOnDismissListener(object : IFieldYieldDismissListener {
                    override fun onDismiss(fieldYield: FieldYield, yieldConfirmed: Boolean) {
                        if (yieldConfirmed) {
                            val field = savedYield ?: FieldYield()

                            val yieldLabel = fieldYield.fieldYieldLabel
                            selectedYieldAmount = fieldYield.yieldAmount
                            savedYield = field.apply {
                                this.yieldAmount = selectedYieldAmount
                                this.fieldYieldLabel = yieldLabel
                            }

                            database.fieldYieldDao().insert(field)

                            mAdapter!!.setActiveRowIndex(position)
                        }
                        mAdapter!!.setItems(selectedYieldAmount, items)
                    }

                })

                showDialogFragmentSafely(
                    supportFragmentManager,
                    rootYieldDialogFragment,
                    RootYieldDialogFragment.ARG_ITEM_ID
                )
            }

        })

        binding.twoButtons.apply {
            btnFinish.setOnClickListener { validate(true) }
            btnCancel.setOnClickListener { closeActivity(false) }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validate(true)
    }

    override fun validate(backPressed: Boolean) {
        if (!(selectedYieldAmount > 0)) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_yield),
                getString(R.string.lbl_current_field_yield_prompt),
                getString(R.string.lbl_ok)
            )
            return
        }
        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTasks.CURRENT_CASSAVA_YIELD.name, true))

        closeActivity(false)
    }

    private fun setYieldData(areaUnit: String): List<FieldYield> {
        val rd_3_tonnes: String
        val rd_6_tonnes: String
        val rd_9_tonnes: String
        val rd_12_tonnes: String
        val rd_more: String
        when (areaUnit.lowercase(Locale.getDefault())) {
            "acre" -> {
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_acre)
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_acre)
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_acre)
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_acre)
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_acre)
            }

            "ha" -> {
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_hectare)
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_hectare)
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_hectare)
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_hectare)
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_hectare)
            }

            "are" -> {
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_are)
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_are)
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_are)
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_are)
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_are)
            }

            "m2" -> {
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_meter)
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_meter)
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_meter)
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_meter)
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_meter)
            }

            else -> {
                rd_3_tonnes = getString(R.string.yield_less_than_3_tonnes_per_acre)
                rd_6_tonnes = getString(R.string.yield_3_to_6_tonnes_per_acre)
                rd_9_tonnes = getString(R.string.yield_6_to_9_tonnes_per_acre)
                rd_12_tonnes = getString(R.string.yield_9_to_12_tonnes_per_acre)
                rd_more = getString(R.string.yield_more_than_12_tonnes_per_acre)
            }
        }

        val items: MutableList<FieldYield> = ArrayList()
        items.add(
            createYieldObject(
                yieldImages[0],
                getString(R.string.fcy_lower),
                rd_3_tonnes,
                3.75,
                getString(R.string.lbl_low_yield)
            )
        )

        items.add(
            createYieldObject(
                yieldImages[1],
                getString(R.string.fcy_about_the_same),
                rd_6_tonnes,
                11.25,
                getString(R.string.lbl_normal_yield)
            )
        )

        items.add(
            createYieldObject(
                yieldImages[2],
                getString(R.string.fcy_somewhat_higher),
                rd_9_tonnes,
                18.75,
                getString(R.string.lbl_high_yield)
            )
        )

        items.add(
            createYieldObject(
                yieldImages[3],
                getString(R.string.fcy_2_3_times_higher),
                rd_12_tonnes,
                26.25,
                getString(R.string.lbl_very_high_yield)
            )
        )

        items.add(
            createYieldObject(
                yieldImages[4],
                getString(R.string.fcy_more_than_3_times_higher),
                rd_more,
                33.75,
                getString(R.string.lbl_very_high_yield)
            )
        )

        return items
    }

    private fun createYieldObject(
        imageID: Int,
        yieldLabel: String,
        fieldYieldAmountLabel: String,
        fieldYieldAmount: Double,
        fieldYieldDesc: String
    ): FieldYield {
        return FieldYield().apply {
            this.imageId = imageID
            this.yieldAmount = fieldYieldAmount
            this.fieldYieldLabel = yieldLabel
            this.fieldYieldAmountLabel = fieldYieldAmountLabel
            this.fieldYieldDesc = fieldYieldDesc
        }
    }

}
