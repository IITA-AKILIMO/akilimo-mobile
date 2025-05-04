package com.akilimo.mobile.views.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.DialogRecommendationsChannelBinding
import com.akilimo.mobile.entities.UserProfile
import com.akilimo.mobile.inherit.BaseDialogFragment
import com.akilimo.mobile.interfaces.IRecommendationCallBack
import com.akilimo.mobile.utils.ValidationHelper
import org.jetbrains.annotations.NotNull

class RecommendationChannelDialog(
    private val callbackListener: @NotNull IRecommendationCallBack,
    private val myUserProfile: @NotNull UserProfile
) : BaseDialogFragment() {

    companion object {
        const val TAG: String = "rec_dialog"
    }

    private var _binding: DialogRecommendationsChannelBinding? = null
    private val binding get() = _binding!!

    private var dataIsValid = false
    private var numberIsValid = false
    private var email: String? = null
    private var mobileCode: String? = null
    private var fullMobileNumber: String? = null
    private var sendEmail: Boolean = false
    private var sendSms: Boolean = false

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setWindowAnimations(R.style.DialogSlideAnimation)
    }

    override fun getTheme(): Int = R.style.AppTheme_FullScreenDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogRecommendationsChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.apply {
                toolbar.title = getString(R.string.title_recommendations_delivery)
                toolbar.setNavigationOnClickListener { dismiss() }
            }
            chkEmail.isChecked = sendEmail
            chkSms.isChecked = sendSms

            ccp.setPhoneNumberValidityChangeListener { isValidNumber ->
                numberIsValid = isValidNumber
                lytPhone.error = if (!isValidNumber) {
                    getString(R.string.lbl_valid_number_req)
                } else null
            }

            ccp.setOnCountryChangeListener {
                mobileCode = ccp.selectedCountryCodeWithPlus
            }


            twoButtons.btnCancel.setOnClickListener {
                callbackListener.onDismiss()
                dismiss()
            }

            twoButtons.btnFinish.setOnClickListener {
                val validationHelper = ValidationHelper()
                email = lytEmail.editText?.text.toString()
                if (!validationHelper.isValidEmail(email!!) && sendEmail) {
                    dataIsValid = false
                    lytEmail.error = getString(R.string.lbl_valid_email_req)
                } else {
                    dataIsValid = true
                    lytEmail.error = null
                }

                if (sendSms) {
                    fullMobileNumber = ccp.fullNumber
                    dataIsValid = numberIsValid
                }

                if (dataIsValid) {
                    var userProfile = database.profileInfoDao().findOne()
                    if (userProfile == null) {
                        userProfile = UserProfile()
                    }
                    userProfile.apply {
                    }

                    callbackListener.onDataReceived(userProfile)
                    dismiss()
                }
            }

            chkEmail.setOnCheckedChangeListener { _, isChecked ->
                sendEmail = isChecked
                flagVisibility(sendSms, sendEmail)
            }

            chkSms.setOnCheckedChangeListener { _, isChecked ->
                sendSms = isChecked
                flagVisibility(sendSms, sendEmail)
            }

            ccp.registerCarrierNumberEditText(edtPhone)
            ccp.fullNumber = fullMobileNumber
            lytEmail.editText?.setText(email)
            flagVisibility(sendSms, sendEmail)
        }
        val userProfile = database.profileInfoDao().findOne()
        if (userProfile != null) {
            fullMobileNumber = userProfile.fullMobileNumber
            email = userProfile.email
            sendEmail = userProfile.sendEmail
            sendSms = userProfile.sendSms
        }

    }

    private fun flagVisibility(sendSms: Boolean, sendEmail: Boolean) {
        with(binding) {
            lytEmail.visibility = if (sendEmail) View.VISIBLE else View.GONE
            lytPhone.visibility = if (sendSms) View.VISIBLE else View.GONE
            ccp.visibility = if (sendSms) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
