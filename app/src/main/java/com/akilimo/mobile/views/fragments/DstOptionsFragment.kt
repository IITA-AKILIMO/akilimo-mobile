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
 * A simple [BaseFragment] subclass.
 */
class DstOptionsFragment : BaseFragment<FragmentDstOptionsBinding>() {

    companion object {
        fun newInstance(): DstOptionsFragment {
            return DstOptionsFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentDstOptionsBinding.inflate(inflater, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        val items = recommendationItems()
        binding.dstOptionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            val mAdapter = AdapterListAnimation()
            adapter = mAdapter

            //set data and list adapter
            mAdapter.setAnimationType(TheItemAnimation.FADE_IN)
            mAdapter.submitList(items)
            mAdapter.setOnItemClickListener { _: View?, recommendation: Recommendation, _: Int ->
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

                    else -> {
                        throw IllegalArgumentException("Invalid recommendation code")
                    }
                }
                startActivity(intent)
                Animatoo.animateSlideRight(context)
            }
        }
    }

    /**
     * Creates and returns a list of recommendation items.
     *
     * This function initializes a list of `Recommendation` objects. Each recommendation
     * is created with a specific `recCode` from the `EnumAdvice` enum and a
     * localized `recommendationName` retrieved from string resources.
     *
     * The following recommendations are included:
     * - Fertilizer Recommendations (FR)
     * - Intercropping (IC)
     * - Scheduled Planting and Harvest (SPH)
     * - Best Planting Practices (BPP)
     *
     * @return A `MutableList` of `Recommendation` objects.
     */
    private fun recommendationItems(): MutableList<Recommendation> {
        val context = requireContext()
        val frString = context.getString(R.string.lbl_fertilizer_recommendations)
        val icString = context.getString(R.string.lbl_intercropping)
        val sphString = context.getString(R.string.lbl_scheduled_planting_and_harvest)
        val bppString = context.getString(R.string.lbl_best_planting_practices)


        val items: MutableList<Recommendation> = ArrayList()
        val fertilizerRec = Recommendation(
            recCode = EnumAdvice.FR,
            recommendationName = frString
        )


        val interCropping = Recommendation(
            recCode = EnumAdvice.IC_MAIZE,
            recommendationName = icString
        )


        val scheduledPlanting = Recommendation(
            recCode = EnumAdvice.SPH,
            recommendationName = sphString
        )


        val bestPlantingPractices = Recommendation(
            recCode = EnumAdvice.BPP,
            recommendationName = bppString
        )

        items.add(fertilizerRec)
        items.add(interCropping)
        items.add(scheduledPlanting)
        items.add(bestPlantingPractices)

        return items
    }

}
