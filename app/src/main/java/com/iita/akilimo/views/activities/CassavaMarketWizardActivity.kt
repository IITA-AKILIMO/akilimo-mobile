package com.iita.akilimo.views.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.iita.akilimo.R
import com.iita.akilimo.adapters.MyStepperAdapter
import com.iita.akilimo.databinding.ActivityCassavaMarketWizardStepperBinding
import com.iita.akilimo.databinding.ActivityHomeStepperBinding
import com.iita.akilimo.views.fragments.*
import com.stepstone.stepper.StepperLayout
import com.stepstone.stepper.VerificationError

class CassavaMarketWizardActivity : AppCompatActivity() {

    private lateinit var activity: Activity
    private lateinit var binding: ActivityCassavaMarketWizardStepperBinding
    private lateinit var mStepperLayout: StepperLayout

    private lateinit var stepperAdapter: MyStepperAdapter

    private val fragmentArray: MutableList<Fragment> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCassavaMarketWizardStepperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mStepperLayout = binding.marketStepperLayout

        createFragmentArray()

        stepperAdapter = MyStepperAdapter(
            supportFragmentManager,
            this@CassavaMarketWizardActivity,
            fragmentArray
        )
        mStepperLayout.adapter = stepperAdapter

        mStepperLayout.setListener(object : StepperLayout.StepperListener {
            override fun onCompleted(completeButton: View?) {
                finish()
            }

            override fun onError(verificationError: VerificationError) {
                Toast.makeText(
                    this@CassavaMarketWizardActivity,
                    verificationError.errorMessage,
                    Toast.LENGTH_SHORT
                ).show();
            }

            override fun onStepSelected(newStepPosition: Int) {}

            override fun onReturn() {
                finish()
            }

        })
    }

    private fun createFragmentArray() {
        fragmentArray.add(CassavaMarketFragment.newInstance())
        fragmentArray.add(WelcomeFragment.newInstance())
        fragmentArray.add(WelcomeFragment.newInstance())
    }
}