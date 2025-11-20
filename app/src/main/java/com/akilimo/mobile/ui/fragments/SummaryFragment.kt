package com.akilimo.mobile.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentSummaryBinding
import com.akilimo.mobile.databinding.ItemSummaryRowBinding
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class SummaryFragment : BaseStepFragment<FragmentSummaryBinding>() {

    companion object {
        fun newInstance() = SummaryFragment()
    }

    private lateinit var userRepository: AkilimoUserRepo

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSummaryBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        userRepository = AkilimoUserRepo(database.akilimoUserDao())
    }

    override fun prefillFromEntity() {
        safeScope.launch {
            val user = userRepository.getUser(sessionManager.akilimoUser) ?: return@launch
            binding.containerSummary.removeAllViews()

            addSection("👤 Personal Info") {
                addRow("Name", user.getNames())
                addRow("Email", user.email)
                addRow("Phone", "${user.mobileCountryCode ?: ""} ${user.mobileNumber ?: ""}")
                addRow("Gender", user.gender)
                addRow("Language", user.languageCode)
            }

            addSection("🏡 Farm Details") {
                addRow("Farm Name", user.farmName)
                addRow("Country", user.farmCountry)
                addRow("Size", "${user.farmSize ?: 0.0} ${user.enumAreaUnit ?: ""}")
                addRow("Description", user.farmDescription)
                addRow(
                    "Location",
                    "Lat: ${user.latitude}, Lng: ${user.longitude}, Alt: ${user.altitude}"
                )
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