package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CropPerformanceAdapter
import com.akilimo.mobile.databinding.ActivityMaizePerformanceActivityBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.ICropPerformanceListener
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.MaizePerformanceDialogFragment
import com.akilimo.mobile.views.fragments.dialog.RootYieldDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration

class MaizePerformanceActivity : BaseActivity() {
    var activityTitle: String? = null
    var poorSoil: String? = null
    var richSoil: String? = null

    var toolbar: Toolbar? = null
    var recyclerView: RecyclerView? = null
    var viewPos: View? = null

    var btnFinish: AppCompatButton? = null
    var btnCancel: AppCompatButton? = null
    var exceptionTitle: TextView? = null

    private var mAdapter: CropPerformanceAdapter? = null

    private var _binding: ActivityMaizePerformanceActivityBinding? = null
    private val binding get() = _binding!!


    private var savedCropPerformance: CropPerformance? = null

    private var selectedPerformanceValue: String = ""
    private var maizePerformanceValue: String = ""

    private val performanceImages = arrayOf(
        R.drawable.ic_maize_1,
        R.drawable.ic_maize_2,
        R.drawable.ic_maize_3,
        R.drawable.ic_maize_4,
        R.drawable.ic_maize_5,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMaizePerformanceActivityBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        toolbar = binding.toolbar
        recyclerView = binding.rootYieldRecycler
        viewPos = binding.coordinatorLayout

        exceptionTitle = binding.exceptionTitle
        btnFinish = binding.twoButtons.btnFinish
        btnCancel = binding.twoButtons.btnCancel

        savedCropPerformance = database.maizePerformanceDao().findOne()
        if (savedCropPerformance != null) {
            selectedPerformanceValue = savedCropPerformance!!.maizePerformance
        }
        initToolbar()
        initComponent()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validate(true)
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_activity_maize_performance)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar!!.setNavigationOnClickListener { v: View? -> validate(false) }
    }

    override fun initComponent() {
        poorSoil = getString(R.string.lbl_maize_performance_poor)
        richSoil = getString(R.string.lbl_maize_performance_rich)


        btnFinish!!.setOnClickListener { view: View? -> validate(false) }
        btnCancel!!.setOnClickListener { view: View? -> closeActivity(false) }
        recyclerView!!.layoutManager = GridLayoutManager(this, 1)
        recyclerView!!.addItemDecoration(SpacingItemDecoration(1, dpToPx(this, 3), true))
        recyclerView!!.setHasFixedSize(true)

        val items: MutableList<CropPerformance> = ArrayList()

        items.add(
            createPerformanceObject(
                performanceImages[0],
                poorSoil,
                "1",
                "50",
                getString(R.string.lbl_knee_height)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[1],
                null,
                "2",
                "150",
                getString(R.string.lbl_chest_height)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[2],
                null,
                "3",
                "yellow",
                getString(R.string.lbl_yellowish_leaves)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[3],
                null,
                "4",
                "green",
                getString(R.string.lbl_green_leaves)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[4],
                richSoil,
                "5",
                "dark green",
                getString(R.string.lbl_dark_green_leaves)
            )
        )

        mAdapter =
            CropPerformanceAdapter(this@MaizePerformanceActivity, items, TheItemAnimation.FADE_IN)
        recyclerView!!.adapter = mAdapter
        mAdapter!!.setItems(selectedPerformanceValue, items)

        mAdapter!!.setOnItemClickListener(object : CropPerformanceAdapter.OnItemClickListener {
            override fun onItemClick(
                view: View?,
                clickedCropPerformance: CropPerformance?,
                position: Int
            ) {
                val arguments = Bundle()
                arguments.putParcelable(
                    MaizePerformanceDialogFragment.PERFORMANCE_DATA,
                    clickedCropPerformance
                )

                val rootYieldDialogFragment = MaizePerformanceDialogFragment()
                rootYieldDialogFragment.arguments = arguments
                rootYieldDialogFragment.setOnDismissListener(object :
                    ICropPerformanceListener {
                    override fun onDismiss(
                        performance: CropPerformance,
                        performanceConfirmed: Boolean
                    ) {
                        if (performanceConfirmed) {
                            if (savedCropPerformance == null) {
                                savedCropPerformance = CropPerformance()
                            }
                            val maizePerformance = performance.maizePerformance
                            selectedPerformanceValue = performance.performanceValue!!
                            savedCropPerformance!!.maizePerformance = maizePerformance
                            savedCropPerformance!!.performanceValue = selectedPerformanceValue

                            database.maizePerformanceDao().insert(savedCropPerformance!!)

                            mAdapter!!.setActiveRowIndex(position)
                            maizePerformanceValue = selectedPerformanceValue
                        }
                        mAdapter!!.setItems(selectedPerformanceValue, items)
                    }

                })


                showDialogFragmentSafely(
                    fragmentManager = supportFragmentManager,
                    dialogFragment = rootYieldDialogFragment,
                    tag = RootYieldDialogFragment.ARG_ITEM_ID
                )
            }

        })
    }

    private fun createPerformanceObject(
        yieldImage: Int,
        performanceDesc: String?,
        performanceValue: String,
        maizePerformance: String,
        maizePerformanceLabel: String
    ): CropPerformance {
        val performance = CropPerformance()
        performance.imageId = yieldImage
        performance.maizePerformanceDesc = performanceDesc
        performance.performanceValue = performanceValue
        performance.maizePerformance = maizePerformance
        performance.maizePerformanceLabel = maizePerformanceLabel
        return performance
    }

    override fun validate(backPressed: Boolean) {
        if (selectedPerformanceValue.isNullOrEmpty()) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_selection),
                getString(R.string.lbl_maize_performance_prompt)
            )
            return
        }
        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTasks.MAIZE_PERFORMANCE.name, true))
        closeActivity(backPressed)
    }
}
