package com.akilimo.mobile.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.AdapterListAnimation
import com.akilimo.mobile.databinding.FragmentDstOptionsBinding
import com.akilimo.mobile.inherit.BaseFragment
import com.akilimo.mobile.models.Recommendation
import com.akilimo.mobile.utils.TheItemAnimation
import com.akilimo.mobile.utils.enums.EnumAdvice
import com.akilimo.mobile.views.activities.usecases.FertilizerRecActivity
import com.akilimo.mobile.views.activities.usecases.InterCropRecActivity
import com.akilimo.mobile.views.activities.usecases.PlantingPracticesActivity
import com.akilimo.mobile.views.activities.usecases.ScheduledPlantingActivity
import com.blogspot.atifsoftwares.animatoolib.Animatoo


/**
 * A simple [Fragment] subclass.
 */
class DstOptionsFragment : BaseFragment() {
    private var _binding: FragmentDstOptionsBinding? = null
    private val binding get() = _binding!!


    companion object {
        fun newInstance(): DstOptionsFragment {
            return DstOptionsFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDstOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun refreshData() {
        throw UnsupportedOperationException()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        val frString = context.getString(R.string.lbl_fertilizer_recommendations)
        val icString = context.getString(R.string.lbl_intercropping)
        val sphString = context.getString(R.string.lbl_scheduled_planting_and_harvest)
        val bppString = context.getString(R.string.lbl_best_planting_practices)


        val items: MutableList<Recommendation> = ArrayList()
        val FR = Recommendation(
            recCode = EnumAdvice.FR,
            recommendationName = frString
        )
        items.add(FR)

        val IC = Recommendation(
            recCode = EnumAdvice.IC_MAIZE,
            recommendationName = icString
        )
        items.add(IC)

        val SPH = Recommendation(
            recCode = EnumAdvice.SPH,
            recommendationName = sphString
        )
        items.add(SPH)

        val BPP = Recommendation(
            recCode = EnumAdvice.BPP,
            recommendationName = bppString
        )
        items.add(BPP)
        binding.dstOptionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            val mAdapter = AdapterListAnimation()
            adapter = mAdapter

            //set data and list adapter
            mAdapter.setAnimationType(TheItemAnimation.FADE_IN)
            mAdapter.submitList(items)
            mAdapter.setOnItemClickListener { view: View?, recommendation: Recommendation, position: Int ->
                var intent: Intent? = null
                val advice = recommendation.recCode
                when (advice) {
                    EnumAdvice.FR -> intent = Intent(context, FertilizerRecActivity::class.java)
                    EnumAdvice.BPP -> intent =
                        Intent(context, PlantingPracticesActivity::class.java)

                    EnumAdvice.IC_MAIZE -> intent =
                        Intent(context, InterCropRecActivity::class.java)

                    EnumAdvice.SPH -> intent =
                        Intent(context, ScheduledPlantingActivity::class.java)

                    else -> {}
                }
                if (intent != null) {
                    startActivity(intent)
                    Animatoo.animateSlideRight(context)
                }
            }
        }
    }

}
