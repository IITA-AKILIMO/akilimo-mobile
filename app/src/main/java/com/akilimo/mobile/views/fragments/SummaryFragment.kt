package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MyTimeLineAdapter
import com.akilimo.mobile.databinding.FragmentSummaryBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.akilimo.mobile.models.TimelineAttributes
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.viewmodels.SummaryViewModel
import com.akilimo.mobile.viewmodels.factory.SummaryViewModelFactory
import com.github.vipulasri.timelineview.TimelineView

class SummaryFragment : BindBaseStepFragment<FragmentSummaryBinding>() {

    private lateinit var myAdapter: MyTimeLineAdapter

    private val viewModel: SummaryViewModel by viewModels {
        SummaryViewModelFactory(requireActivity().application, mathHelper)
    }

    companion object {
        fun newInstance(): SummaryFragment = SummaryFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentSummaryBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        setupRecycler()
        setupObservers()
        viewModel.loadSummaryData()
    }

    override fun setupObservers() {
        viewModel.timelineItems.observe(viewLifecycleOwner) { items ->
            myAdapter.submitList(items)
        }
    }

    override fun onSelected() {
        viewModel.loadSummaryData()
    }

    private fun setupRecycler() {
        val attributes = TimelineAttributes(
            markerSize = 48,
            markerCompleteColor = ContextCompat.getColor(
                requireContext(),
                R.color.akilimoLightGreen
            ),
            markerIncompleteColor = ContextCompat.getColor(requireContext(), R.color.red_A400),
            startLineColor = ContextCompat.getColor(requireContext(), R.color.colorAccent),
            endLineColor = ContextCompat.getColor(requireContext(), R.color.colorAccent),
            lineStyle = TimelineView.LineStyle.DASHED
        )
        myAdapter = MyTimeLineAdapter(emptyList(), attributes, TheItemAnimation.FADE_IN)

        binding.timelineRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = myAdapter
        }
    }
}
