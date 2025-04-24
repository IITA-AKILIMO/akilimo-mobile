package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MaizePerformanceAdapter
import com.akilimo.mobile.databinding.ActivityMaizePerformanceActivityBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.IMaizePerformanceDismissListener
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumAdviceTasks
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.MaizePerformanceDialogFragment
import com.akilimo.mobile.views.fragments.dialog.RootYieldDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration
import io.sentry.Sentry

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

    private var mAdapter: MaizePerformanceAdapter? = null

    private var _binding: ActivityMaizePerformanceActivityBinding? = null
    private val binding get() = _binding!!


    private var savedMaizePerformance: MaizePerformance? = null

    private var selectedPerformanceValue: String? = null
    private var maizePerformanceValue: String? = null
    private val performanceRadioIndex = 0

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

        savedMaizePerformance = database.maizePerformanceDao().findOne()
        if (savedMaizePerformance != null) {
            selectedPerformanceValue = savedMaizePerformance!!.maizePerformance
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

        val items: MutableList<MaizePerformance> = ArrayList()

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

        mAdapter = MaizePerformanceAdapter(this, items, TheItemAnimation.FADE_IN)
        recyclerView!!.adapter = mAdapter
        mAdapter!!.setItems(selectedPerformanceValue, items)


        mAdapter!!.setOnItemClickListener { view: View?, clickedMaizePerformance: MaizePerformance?, position: Int ->
            try {
                //show a popup dialog here
                val arguments = Bundle()
                arguments.putParcelable(
                    MaizePerformanceDialogFragment.PERFORMANCE_DATA,
                    clickedMaizePerformance
                )

                val rootYieldDialogFragment = MaizePerformanceDialogFragment()
                rootYieldDialogFragment.arguments = arguments
                rootYieldDialogFragment.setOnDismissListener(object :
                    IMaizePerformanceDismissListener {
                    override fun onDismiss(
                        performance: MaizePerformance,
                        performanceConfirmed: Boolean
                    ) {
                        if (performanceConfirmed) {
                            if (savedMaizePerformance == null) {
                                savedMaizePerformance = MaizePerformance()
                            }
                            val maizePerformance = performance.maizePerformance
                            selectedPerformanceValue = performance.performanceValue
                            savedMaizePerformance!!.maizePerformance = maizePerformance
                            savedMaizePerformance!!.performanceValue = selectedPerformanceValue

                            database.maizePerformanceDao().insert(savedMaizePerformance!!)

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
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
        }
    }

    private fun createPerformanceObject(
        yieldImage: Int,
        performanceDesc: String?,
        performanceValue: String,
        maizePerformance: String,
        maizePerformanceLabel: String
    ): MaizePerformance {
        val performance = MaizePerformance()
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
