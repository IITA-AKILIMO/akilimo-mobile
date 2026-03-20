package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentSummaryBinding
import com.akilimo.mobile.databinding.ItemSummaryRowBinding
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SummaryFragment : BaseStepFragment<FragmentSummaryBinding>() {

    companion object {
        fun newInstance() = SummaryFragment()
    }

    private val onboardingViewModel: OnboardingViewModel by activityViewModels()

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSummaryBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // No setup needed; data loaded in prefillFromEntity
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = onboardingViewModel.getUser(sessionManager.akilimoUser) ?: return@launch
            val prefs = onboardingViewModel.getPreferences()
            binding.containerSummary.removeAllViews()

            addSection("👤 Personal Info") {
                addRow("Name", user.getNames().ifBlank { "${prefs.firstName ?: ""} ${prefs.lastName ?: ""}".trim() })
                addRow("Email", user.email ?: prefs.email)
                addRow("Phone", if (!user.mobileNumber.isNullOrBlank()) "${user.mobileCountryCode ?: ""} ${user.mobileNumber ?: ""}" else "${prefs.phoneCountryCode ?: ""} ${prefs.phoneNumber ?: ""}")
                addRow("Gender", user.gender ?: prefs.gender)
                addRow("Language", user.languageCode ?: prefs.languageCode)
            }

            addSection("🏡 Farm Details") {
                addRow("Farm Name", user.farmName)
                addRow("Country", user.enumCountry.countryName)
                addRow("Size", "${user.farmSize} ${user.enumAreaUnit}")
                addRow("Description", user.farmDescription)
                addRow("Location", "Lat: ${user.latitude}, Lng: ${user.longitude}, Alt: ${user.altitude}")
            }

            addSection("📅 Planting & Harvest") {
                addRow("Planting Date", user.plantingDate?.let { formatDate(it) })
                addRow("Harvest Date", user.harvestDate?.let { formatDate(it) })
                addRow("Planting Flexibility", "${user.plantingFlex} Months")
                addRow("Harvest Flexibility", "${user.harvestFlex} Months")
            }

            addSection("🚜 Tillage Operations") {
                user.tillageOperations.forEach {
                    addRow(it.operation.displayLabel, it.method.displayLabel)
                }
            }

            addSection("📈 Investment Profile") {
                addRow("Risk Preference", user.investmentPref?.label(requireContext()))
            }

            addSection("📤 Communication Preferences") {
                addRow("Send Email", if (user.sendEmail) "Yes" else "No")
                addRow("Send SMS", if (user.sendSms) "Yes" else "No")
                addRow("Device Token", user.deviceToken)
            }
        }
    }

    private fun addSection(title: String, content: LinearLayout.() -> Unit) {
        val card = MaterialCardView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
            radius = dp(12).toFloat()
            cardElevation = dp(4).toFloat()
        }

        val sectionLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        val titleView = TextView(requireContext()).apply {
            text = title
            setPadding(0, 0, 0, dp(8))
        }

        sectionLayout.addView(titleView)
        sectionLayout.content()
        card.addView(sectionLayout)
        binding.containerSummary.addView(card)
    }

    private fun LinearLayout.addRow(label: String, value: String?) {
        val rowBinding = ItemSummaryRowBinding.inflate(layoutInflater)
        rowBinding.label.text = label
        rowBinding.value.text = value ?: "-"
        this.addView(rowBinding.root)
    }

    private fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        return date.format(formatter)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
