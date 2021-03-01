package com.iita.akilimo.views.activities

import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatButton
import com.iita.akilimo.Locales
import com.iita.akilimo.R
import com.iita.akilimo.databinding.ActivityMySurveyBinding
import com.iita.akilimo.inherit.BaseActivity
import java.util.*


class MySurveyActivity : BaseActivity() {

    private lateinit var binding: ActivityMySurveyBinding

    private lateinit var btnSave: AppCompatButton
    private lateinit var rdgAkilimoUser: RadioGroup

    private var akilimoUsage: String = ""
    private var akilimoRecRating: Int = 0
    private var akilimoUsefulRating: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMySurveyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }

    override fun initToolbar() {
    }

    override fun initComponent() {
        val rdgAkilimoUser = binding.rdgAkilimoUser
        val rdgRecommend = binding.rdgRecommend
        val rdgUseful = binding.rdgUseful
        val btnFinish = binding.btnFinish

        rdgAkilimoUser.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton = rdgAkilimoUser.findViewById(checkedId) as RadioButton
            akilimoUsage = radioButton.text.toString()
        })

        rdgRecommend.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton = rdgRecommend.findViewById(checkedId) as RadioButton
            val idx = rdgRecommend.indexOfChild(radioButton)
            akilimoRecRating = idx + 1
        })
        rdgUseful.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { group, checkedId ->
            val radioButton = rdgUseful.findViewById(checkedId) as RadioButton
            val idx = rdgUseful.indexOfChild(radioButton)
            akilimoUsefulRating = idx + 1
        })

        //now we submit to the API
        btnFinish.setOnClickListener {
            //send data to REST api
        }
    }

    override fun validate(backPressed: Boolean) {
        closeActivity(backPressed)
    }

    fun getStringByLocale(
        @StringRes stringRes: Int,
        locale: Locale,
        vararg formatArgs: Any
    ): String {
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)
        return createConfigurationContext(configuration).resources.getString(stringRes, *formatArgs)
    }
}