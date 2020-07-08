package com.iita.akilimo.views.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hbb20.CountryCodePicker
import com.iita.akilimo.R
import com.iita.akilimo.entities.ProfileInfo
import com.iita.akilimo.inherit.BaseDialogFragment
import com.iita.akilimo.interfaces.IRecommendationCallBack
import com.iita.akilimo.utils.ValidationHelper
import org.jetbrains.annotations.NotNull


class RecommendationChannelDialog(
    private val callbackListener: @NotNull IRecommendationCallBack,
    private val myProfileInfo: @NotNull ProfileInfo
) : BaseDialogFragment() {


    companion object {
        const val TAG: String = "rec_dialog"
    }


    lateinit var toolbar: Toolbar
    lateinit var lytEmail: TextInputLayout
    lateinit var lytPhone: TextInputLayout
    lateinit var edtPhone: TextInputEditText
    lateinit var ccp: CountryCodePicker
    lateinit var chkEmail: CheckBox
    lateinit var chkSms: CheckBox

    lateinit var btnFinish: AppCompatButton
    lateinit var btnCancel: AppCompatButton

    private var profileInfo: ProfileInfo? = null

    private var dataIsValid = false
    private var numberIsValid = false
    private var email: String? = null
    private var mobileCode: String? = null
    private var fullMobileNumber: String? = null
    private var sendEmail: Boolean = false
    private var sendSms: Boolean = false

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setWindowAnimations(R.style.DialogSlideUpAnimation)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.dialog_recommendations_channel, container, false)

        this.profileInfo = myProfileInfo

        toolbar = view.findViewById(R.id.toolbar)
        lytEmail = view.findViewById(R.id.lytEmail)
        lytPhone = view.findViewById(R.id.lytPhone)
        edtPhone = view.findViewById(R.id.edtPhone)
        ccp = view.findViewById(R.id.ccp)
        chkEmail = view.findViewById(R.id.chkEmail)
        chkSms = view.findViewById(R.id.chkSms)

        btnFinish = view.findViewById(R.id.btnFinish)
        btnCancel = view.findViewById(R.id.btnCancel)

        if (profileInfo != null) {
            fullMobileNumber = profileInfo?.fullMobileNumber
            email = profileInfo?.email
            sendEmail = profileInfo?.sendEmail!!
            sendSms = profileInfo?.sendSms!!
        }

        chkEmail.isChecked = sendEmail
        chkSms.isChecked = sendSms
        return view
    }

    override fun getTheme(): Int {
        return R.style.AppTheme_FullScreenDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.title = getString(R.string.title_recommendations_delivery)

        toolbar.setNavigationOnClickListener { dismiss() }

        ccp.setPhoneNumberValidityChangeListener { isValidNumber ->
            numberIsValid = isValidNumber
            if (!isValidNumber) {
                lytPhone.error = getString(R.string.lbl_valid_number_req)
            } else {
                lytPhone.error = null
            }
        }

        ccp.setOnCountryChangeListener { mobileCode = ccp.selectedCountryCodeWithPlus }

        btnCancel.setOnClickListener {
            callbackListener.onDismiss()
            dismiss()
        }
        btnFinish.setOnClickListener {
            val validationHelper = ValidationHelper()
            if (!validationHelper.isValidEmail(email!!) && sendEmail) {
                dataIsValid = false
                lytEmail.error = getString(R.string.lbl_valid_email_req)
            } else {
                dataIsValid = true
                email = lytEmail.editText!!.text.toString()
            }
            if (sendSms) {
                fullMobileNumber = ccp.fullNumber
                dataIsValid = numberIsValid
            }

            if (dataIsValid) {
                if (profileInfo == null) {
                    profileInfo = ProfileInfo()
                }
                profileInfo?.mobileCode = (mobileCode)
                profileInfo?.email = (email)
                profileInfo?.fullMobileNumber = (fullMobileNumber)
                profileInfo?.sendEmail = (sendEmail)
                profileInfo?.sendSms = (sendSms)

                callbackListener.onDataReceived(profileInfo!!)
                dismiss()
            }

        }

        chkEmail.setOnCheckedChangeListener { _, isChecked ->
            run {
                sendEmail = isChecked
            }
        }
        chkSms.setOnCheckedChangeListener { _, isChecked ->
            run {
                sendSms = isChecked
            }
        }
        ccp.registerCarrierNumberEditText(edtPhone)


        ccp.fullNumber = fullMobileNumber
        lytEmail.editText?.setText(email)

    }
}