package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentCountryBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.utils.enums.EnumCountry
import com.blongho.country_data.World
import com.stepstone.stepper.VerificationError
import io.sentry.Sentry
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 * Use the [CountryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CountryFragment : BaseStepFragment() {
    var title: AppCompatTextView? = null
    var btnPickCountry: AppCompatButton? = null

    //var countryImage: ImageView? = null
    var txtCountryName: AppCompatTextView? = null

    private var _binding: FragmentCountryBinding? = null
    private val binding get() = _binding!!


    private var userProfile: UserProfile? = null
    private var name: String? = ""
    private var selectedLanguage: String? = ""
    private var selectedCountryIndex = -1


    private var countries = arrayOf(
        EnumCountry.Burundi.name,
        EnumCountry.Ghana.name,
        EnumCountry.Nigeria.name,
        EnumCountry.Tanzania.name,  //            EnumCountry.Rwanda.name(),
    )

    companion object {
        fun newInstance(): CountryFragment {
            return CountryFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCountryBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun refreshData() {
        val context = requireContext()
        try {
            userProfile = database.profileInfoDao().findOne()
            if (userProfile != null) {
                name = userProfile!!.firstName
                countryCode = userProfile!!.countryCode!!
                currency = userProfile!!.currency
                countryName = userProfile!!.countryName
                currency = userProfile!!.currency
                selectedLanguage = userProfile!!.language

                if (countryCode.isNotEmpty()) {
                    selectedCountryIndex = userProfile!!.selectedCountryIndex
                    binding.countryImage.setImageResource(World.getFlagOf(countryCode))
                }
                if (countryName!!.isNotEmpty()) {
                    txtCountryName!!.text = countryName
                }
            }

            val message = context.getString(R.string.lbl_country_location, name)
            title!!.text = message
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            Sentry.captureException(ex)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        title = binding.title
        btnPickCountry = binding.btnPickCountry
        txtCountryName = binding.countryName

        btnPickCountry!!.setOnClickListener { pickerDialog: View? ->
            if (selectedLanguage.equals("sw", ignoreCase = true)) {
                countries = arrayOf(EnumCountry.Tanzania.name)
            }
            val builder =
                AlertDialog.Builder(
                    context
                )
            builder.setTitle(context.getString(R.string.lbl_pick_your_country))
            builder.setSingleChoiceItems(
                countries,
                selectedCountryIndex
            ) { _, i -> selectedCountryIndex = i }

            val countryMap: MutableMap<String, EnumCountry> =
                HashMap()
            countryMap["kenya"] = EnumCountry.Kenya
            countryMap["tanzania"] = EnumCountry.Tanzania
            countryMap["nigeria"] = EnumCountry.Nigeria
            countryMap["ghana"] = EnumCountry.Ghana
            countryMap["rwanda"] = EnumCountry.Rwanda
            countryMap["burundi"] = EnumCountry.Burundi


            builder.setPositiveButton(context.getString(R.string.lbl_ok)) { dialogInterface, whichButton ->
                if (selectedCountryIndex >= 0 && countries.isNotEmpty()) {
                    countryName = countries[selectedCountryIndex]
                    var selectedCountry =
                        countryMap[countryName!!.lowercase(Locale.getDefault())]
                    if (selectedCountry == null) {
                        selectedCountry = EnumCountry.Other
                    }

                    countryName = selectedCountry.name
                    currency = selectedCountry.currency()
                    countryCode = selectedCountry.countryCode()
                    binding.countryImage.setImageResource(World.getFlagOf(countryCode))
                    txtCountryName!!.text = countryName
                    dialogInterface.dismiss()
                    updateSelectedCountry()
                }
            }
            builder.setNegativeButton(
                context.getString(R.string.lbl_cancel),
                ({ dialogInterface, i ->
                    dialogInterface.dismiss()
                })
            )

            val dialog = builder.create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }

    private fun updateSelectedCountry() {
        try {
            if (userProfile == null) {
                userProfile = UserProfile()
            }

            userProfile!!.selectedCountryIndex = selectedCountryIndex
            userProfile!!.countryCode = countryCode
            userProfile!!.countryName = countryName
            userProfile!!.currency = currency

            dataIsValid = !TextUtils.isEmpty(countryCode)
            if (userProfile!!.profileId != null) {
                val id = userProfile!!.profileId!!
                if (id > 0) {
                    database.profileInfoDao().update(userProfile!!)
                }
            } else {
                database.profileInfoDao().insert(userProfile!!)
            }
            sessionManager.setCountry(countryCode)
        } catch (ex: Exception) {
            dataIsValid = false
            Sentry.captureException(ex)
        }
    }

    override fun verifyStep(): VerificationError? {
        updateSelectedCountry()
        if (!dataIsValid) {
            return VerificationError("Please select country")
        }
        return null
    }

    override fun onSelected() {
        refreshData()
    }

    override fun onError(error: VerificationError) {
    }
}
