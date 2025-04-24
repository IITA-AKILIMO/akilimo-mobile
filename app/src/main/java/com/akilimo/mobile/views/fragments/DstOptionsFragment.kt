package com.akilimo.mobile.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.AdapterListAnimation
import com.akilimo.mobile.databinding.FragmentDstOptionsBinding
import com.akilimo.mobile.inherit.BaseFragment
import com.akilimo.mobile.models.Recommendations
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
    var recyclerView: RecyclerView? = null
    private var _binding: FragmentDstOptionsBinding? = null
    private val binding get() = _binding!!

    var frString: String? = null
    var icString: String? = null
    var sphString: String? = null
    var bppString: String? = null

    private var mAdapter: AdapterListAnimation? = null
    private var items: MutableList<Recommendations> = ArrayList()

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

        frString = context.getString(R.string.lbl_fertilizer_recommendations)
        icString = context.getString(R.string.lbl_intercropping)
        sphString = context.getString(R.string.lbl_scheduled_planting_and_harvest)
        bppString = context.getString(R.string.lbl_best_planting_practices)


        recyclerView = binding.recyclerView

        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.setHasFixedSize(true)
        mAdapter = AdapterListAnimation(context)
        recyclerView!!.adapter = mAdapter

        items = ArrayList()
        val FR = Recommendations()
        FR.recCode = EnumAdvice.FR
        FR.recommendationName = frString
        items.add(FR)

        val IC = Recommendations()
        IC.recCode = EnumAdvice.IC_MAIZE
        IC.recommendationName = icString
        items.add(IC)

        val SPH = Recommendations()
        SPH.recCode = EnumAdvice.SPH
        SPH.recommendationName = sphString
        items.add(SPH)

        val BPP = Recommendations()
        BPP.recCode = EnumAdvice.BPP
        BPP.recommendationName = bppString
        items.add(BPP)

        initComponent()
    }


    private fun initComponent() {
        //set data and list adapter
        mAdapter!!.setItems(items, TheItemAnimation.FADE_IN)
        // on item list clicked
        mAdapter!!.setOnItemClickListener { view: View?, obj: Recommendations, position: Int ->
            //let us process the data
            var intent: Intent? = null
            val advice = obj.recCode ?: return@setOnItemClickListener
            when (advice) {
                EnumAdvice.FR -> intent = Intent(context, FertilizerRecActivity::class.java)
                EnumAdvice.BPP -> intent = Intent(context, PlantingPracticesActivity::class.java)
                EnumAdvice.IC_MAIZE -> intent = Intent(context, InterCropRecActivity::class.java)
                EnumAdvice.SPH -> intent = Intent(context, ScheduledPlantingActivity::class.java)
                else -> {}
            }
            if (intent != null) {
                startActivity(intent)
                Animatoo.animateSlideRight(context)
            }
        }
    }
}
