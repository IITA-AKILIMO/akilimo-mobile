package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CropPerformanceAdapter
import com.akilimo.mobile.databinding.ActivityMaizePerformanceActivityBinding
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.inherit.BaseActivity
import com.akilimo.mobile.interfaces.ICropPerformanceListener
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.enums.EnumAdviceTask
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.views.fragments.dialog.MaizePerformanceDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration

class MaizePerformanceActivity : BaseActivity() {
    var poorSoil: String? = null
    var richSoil: String? = null

    private lateinit var mAdapter: CropPerformanceAdapter

    private var _binding: ActivityMaizePerformanceActivityBinding? = null
    private val binding get() = _binding!!


    private var selectedPerformanceScore: Int = -1

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


        val cropPerformanceRecord = database.maizePerformanceDao().findOne()
        if (cropPerformanceRecord != null) {
            selectedPerformanceScore = cropPerformanceRecord.performanceScore
        }


        setupToolbar(binding.toolbar, R.string.title_activity_maize_performance) {
            validate(false)
        }

        poorSoil = getString(R.string.lbl_maize_performance_poor)
        richSoil = getString(R.string.lbl_maize_performance_rich)


        binding.twoButtons.btnFinish.setOnClickListener { _: View? -> validate(false) }
        binding.twoButtons.btnCancel.setOnClickListener { _: View? -> closeActivity(false) }

        val items: MutableList<CropPerformance> = ArrayList()

        items.add(
            createPerformanceObject(
                performanceImages[0],
                poorSoil,
                1,
                "50",
                getString(R.string.lbl_knee_height)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[1],
                null,
                2,
                "150",
                getString(R.string.lbl_chest_height)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[2],
                null,
                3,
                "yellow",
                getString(R.string.lbl_yellowish_leaves)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[3],
                null,
                4,
                "green",
                getString(R.string.lbl_green_leaves)
            )
        )
        items.add(
            createPerformanceObject(
                performanceImages[4],
                richSoil,
                5,
                "dark green",
                getString(R.string.lbl_dark_green_leaves)
            )
        )

        binding.rootYieldRecycler.apply {
            layoutManager = GridLayoutManager(this@MaizePerformanceActivity, 1)
            addItemDecoration(
                SpacingItemDecoration(
                    1,
                    dpToPx(this@MaizePerformanceActivity, 3),
                    true
                )
            )
            setHasFixedSize(true)
            adapter = mAdapter
        }

        mAdapter =
            CropPerformanceAdapter(TheItemAnimation.FADE_IN) { _, clickedCropPerformance, position ->
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
                        cropPerformance: CropPerformance,
                        performanceConfirmed: Boolean
                    ) {
                        if (performanceConfirmed) {
                            val savedCropPerformance =
                                database.maizePerformanceDao().findOne() ?: CropPerformance()

                            val maizePerformance = cropPerformance.maizePerformance
                            selectedPerformanceScore = cropPerformance.performanceScore
                            savedCropPerformance.maizePerformance = maizePerformance
                            savedCropPerformance.performanceScore = selectedPerformanceScore

                            database.maizePerformanceDao().insert(savedCropPerformance)

                            mAdapter.setActiveIndex(position)
                            mAdapter.updateItems(selectedPerformanceScore, items, position)
                        }
                    }

                })

                showDialogFragmentSafely(
                    fragmentManager = supportFragmentManager,
                    dialogFragment = rootYieldDialogFragment,
                    tag = "RootYieldDialogFragment"
                )

            }
        mAdapter.updateItems(selectedPerformanceScore, items)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validate(true)
    }


    private fun createPerformanceObject(
        yieldImage: Int,
        performanceDesc: String?,
        performanceValue: Int,
        maizePerformanceDesc: String,
        maizePerformanceLabel: String
    ): CropPerformance {
        val performance = CropPerformance()
        performance.imageId = yieldImage
        performance.maizePerformanceDesc = performanceDesc
        performance.performanceScore = performanceValue
        performance.maizePerformance = maizePerformanceDesc
        performance.maizePerformanceLabel = maizePerformanceLabel
        return performance
    }

    override fun validate(backPressed: Boolean) {
        if (selectedPerformanceScore < 0) {
            showCustomWarningDialog(
                getString(R.string.lbl_invalid_selection),
                getString(R.string.lbl_maize_performance_prompt)
            )
            return
        }
        database.adviceStatusDao()
            .insert(AdviceStatus(EnumAdviceTask.MAIZE_PERFORMANCE.name, true))
        closeActivity(backPressed)
    }
}
