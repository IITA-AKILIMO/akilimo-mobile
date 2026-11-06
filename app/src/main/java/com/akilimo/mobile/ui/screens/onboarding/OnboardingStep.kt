package com.akilimo.mobile.ui.screens.onboarding

import androidx.annotation.StringRes
import com.akilimo.mobile.R

/**
 * The pages of first-run onboarding. Each page asks about one topic, and Next only checks the
 * fields on that page. [fields] are the error keys used by OnboardingViewModel.
 */
enum class OnboardingStep(@param:StringRes val titleRes: Int, val fields: Set<String>) {
    ABOUT_YOU(
        R.string.lbl_step_about_you,
        setOf("firstName", "lastName", "email", "phone", "gender", "interest", "rec_language"),
    ),
    FARM_LOCATION(R.string.lbl_step_farm_location, setOf("country", "location")),
    FIELD(R.string.lbl_step_your_field, setOf("areaUnit", "farmSize", "plantingDate", "harvestDate")),
    PRACTICES(R.string.lbl_step_practices, setOf("weedControl", "investmentPref")),
    REVIEW(R.string.lbl_step_review, emptySet());

    companion object {
        /** The page that shows the field behind an error key */
        fun forField(field: String): OnboardingStep =
            entries.firstOrNull { field in it.fields } ?: ABOUT_YOU
    }
}
