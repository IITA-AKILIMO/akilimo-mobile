package com.akilimo.mobile.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.akilimo.mobile.R
import com.akilimo.mobile.adapters.MySpinnerAdapter
import com.akilimo.mobile.databinding.FragmentWelcomeBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseStepFragment
import com.akilimo.mobile.interfaces.IFragmentCallBack
import com.akilimo.mobile.utils.Locales
import com.akilimo.mobile.utils.enums.EnumCountry
import com.akilimo.mobile.views.activities.HomeStepperActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import com.stepstone.stepper.VerificationError
import dev.b3nedikt.app_locale.AppLocale.appLocaleRepository
import dev.b3nedikt.app_locale.AppLocale.desiredLocale
import dev.b3nedikt.app_locale.AppLocale.supportedLocales
import dev.b3nedikt.app_locale.SharedPrefsAppLocaleRepository
import dev.b3nedikt.reword.Reword.reword
import java.util.Locale


/**
 * A simple [Fragment] subclass.
 */
class WelcomeFragment : BaseStepFragment() {
    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private var fragmentCallBack: IFragmentCallBack? = null

    private var prefs: SharedPrefsAppLocaleRepository? = null
    private var userProfile: UserProfile? = null
    private var selectedLanguageIndex = -1
    private var selectedLocale: Locale? = null
    private var selectedLanguage: String = "en"
    private var languagePicked = false

    companion object {
        fun newInstance(): WelcomeFragment {
            return WelcomeFragment()
        }
    }

    override fun loadFragmentLayout(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userProfile = database.profileInfoDao().findOne()

        prefs = SharedPrefsAppLocaleRepository(requireContext())

        binding.apply {
            languagePicker.setOnTouchListener { touchView: View, motionEvent: MotionEvent? ->
                languagePicked = true
                touchView.performClick()
                touchView.onTouchEvent(motionEvent)
            }
            languagePicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (languagePicked) {
                        selectedLanguageIndex = position
                        selectedLocale = supportedLocales[selectedLanguageIndex]

                        desiredLocale = selectedLocale!!
                        prefs = SharedPrefsAppLocaleRepository(context!!)
                        prefs!!.desiredLocale = selectedLocale
                        appLocaleRepository = prefs

                        val rootView =
                            activity!!.window.decorView.findViewById<View>(android.R.id.content)
                        reword(rootView)
                        val intent = Intent(requireContext(), HomeStepperActivity::class.java)
                        val snackBar = Snackbar
                            .make(
                                lytParent,
                                getString(R.string.lbl_restart_app_prompt),
                                BaseTransientBottomBar.LENGTH_INDEFINITE
                            )
                            .setAction(context!!.getString(R.string.lbl_ok)) { snackView ->
                                ProcessPhoenix.triggerRebirth(
                                    requireContext(),
                                    intent
                                )
                            }
                        snackBar.show()
                        initSpinnerItems()
                    }
                    languagePicked = false
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //not implemented
                }
            }
        }

        initSpinnerItems()
    }

    private fun initSpinnerItems() {
        selectedLocale = prefs!!.desiredLocale

        val localeStrings: MutableList<String> = ArrayList()
        val localeDisplayName: MutableList<String> = ArrayList()
        for (locale in Locales.APP_LOCALES) {
            val languageCountry = locale.country
            localeDisplayName.add(locale.displayLanguage)
            if (languageCountry.equals(EnumCountry.Tanzania.countryCode(), ignoreCase = true)) {
                localeStrings.add(getString(R.string.lbl_kiswahili))
            } else {
                localeStrings.add(locale.displayLanguage)
            }
        }
        if (selectedLocale != null) {
            selectedLanguageIndex = localeDisplayName.indexOf(selectedLocale!!.displayLanguage)
        }
        val spinnerAdapter = MySpinnerAdapter(context, localeStrings)
        binding.apply {
            languagePicker.adapter = spinnerAdapter
            languagePicker.setSelection(selectedLanguageIndex)
        }
    }

    override fun verifyStep(): VerificationError? {
        userProfile = database.profileInfoDao().findOne()
        if (userProfile == null) {
            userProfile = UserProfile()
        }
        if (selectedLocale != null) {
            selectedLanguage = selectedLocale!!.language
        }
        userProfile!!.language = selectedLanguage
        if (userProfile!!.profileId != null) {
            val id = userProfile!!.profileId!!
            if (id > 0) {
                database.profileInfoDao().update(userProfile!!)
            }
        } else {
            database.profileInfoDao().insert(userProfile!!)
        }

        return verificationError
    }

    override fun onSelected() {
        //not implemented
    }

    override fun onError(error: VerificationError) {
        //not implemented
    }

    fun setOnFragmentCloseListener(callBack: IFragmentCallBack?) {
        this.fragmentCallBack = callBack
    }
}
