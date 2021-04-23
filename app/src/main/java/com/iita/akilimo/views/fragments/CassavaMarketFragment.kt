package com.iita.akilimo.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iita.akilimo.databinding.FragmentCassavaMarketBinding
import com.iita.akilimo.databinding.FragmentWelcomeBinding
import com.iita.akilimo.inherit.BaseStepFragment
import com.stepstone.stepper.VerificationError

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CassavaMarketFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CassavaMarketFragment : BaseStepFragment() {

    private lateinit var binding: FragmentCassavaMarketBinding

    companion object {
        @JvmStatic
        fun newInstance() = CassavaMarketFragment()
    }


    override fun loadFragmentLayout(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCassavaMarketBinding.inflate(inflater!!, container, false)
        return binding.root
    }

    override fun verifyStep(): VerificationError? {
        return verificationError
    }

    override fun onSelected() {}

    override fun onError(error: VerificationError) {}
}