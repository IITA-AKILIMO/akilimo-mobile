package com.akilimo.mobile.views.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.CropPerformanceAdapter
import com.akilimo.mobile.databinding.ActivityMaizePerformanceActivityBinding
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.inherit.BindBaseActivity
import com.akilimo.mobile.interfaces.ICropPerformanceListener
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.Tools.dpToPx
import com.akilimo.mobile.utils.showDialogFragmentSafely
import com.akilimo.mobile.viewmodels.MaizePerformanceViewModel
import com.akilimo.mobile.viewmodels.factory.MaizePerformanceViewFactory
import com.akilimo.mobile.views.fragments.dialog.MaizePerformanceDialogFragment
import com.akilimo.mobile.widget.SpacingItemDecoration

class MaizePerformanceActivity : BindBaseActivity<ActivityMaizePerformanceActivityBinding>() {

    private lateinit var mAdapter: CropPerformanceAdapter

    private val viewModel: MaizePerformanceViewModel by viewModels {
        MaizePerformanceViewFactory(application = this.application)
    }

    override fun inflateBinding() = ActivityMaizePerformanceActivityBinding.inflate(
        layoutInflater
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(binding.toolbar, R.string.title_activity_maize_performance) {
            validate(false)
        }

        setupRecycler()
        setupObservers()
        binding.twoButtons.btnFinish.setOnClickListener { _: View? -> viewModel.validateSelection() }
        binding.twoButtons.btnCancel.setOnClickListener { _: View? -> closeActivity(false) }

    }

    override fun setupObservers() {
        viewModel.items.observe(this) { list ->
            mAdapter.updateItems(viewModel.selectedPerformanceScore.value ?: -1, list)
        }

        viewModel.selectedPerformanceScore.observe(this) { score ->
            viewModel.items.value?.let { list ->
                mAdapter.updateItems(score, list)
            }
        }

        viewModel.showMessage.observe(this) { message ->
            showCustomWarningDialog(getString(R.string.lbl_invalid_selection), message)
        }

        viewModel.closeEvent.observe(this) { backPressed ->
            closeActivity(backPressed)
        }
    }

    private fun setupRecycler() {
        mAdapter =
            CropPerformanceAdapter(TheItemAnimation.FADE_IN) { _, cropPerformance, position ->
                val dialog = MaizePerformanceDialogFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(
                            MaizePerformanceDialogFragment.PERFORMANCE_DATA,
                            cropPerformance
                        )
                    }
                    setOnDismissListener(object : ICropPerformanceListener {
                        override fun onDismiss(
                            cropPerformance: CropPerformance,
                            performanceConfirmed: Boolean
                        ) {
                            if (performanceConfirmed) {
                                viewModel.onPerformanceConfirmed(cropPerformance, position)
                            }
                        }
                    })
                }
                showDialogFragmentSafely(
                    supportFragmentManager,
                    dialog,
                    "MaizePerformanceDialogFragment"
                )
            }

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
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        validate(true)
    }
}
