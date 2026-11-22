package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRecommendationViewModelTest {

    @Test
    fun `validation errors map to the check-your-answers message`() {
        assertEquals(R.string.error_rec_invalid_input, GetRecommendationViewModel.errorMessageForHttpCode(400))
        assertEquals(R.string.error_rec_invalid_input, GetRecommendationViewModel.errorMessageForHttpCode(422))
    }

    @Test
    fun `server and throttling errors map to the try-later message`() {
        assertEquals(R.string.error_rec_server_busy, GetRecommendationViewModel.errorMessageForHttpCode(500))
        assertEquals(R.string.error_rec_server_busy, GetRecommendationViewModel.errorMessageForHttpCode(503))
        assertEquals(R.string.error_rec_server_busy, GetRecommendationViewModel.errorMessageForHttpCode(429))
        assertEquals(R.string.error_rec_server_busy, GetRecommendationViewModel.errorMessageForHttpCode(408))
    }

    @Test
    fun `other errors fall back to the generic message`() {
        assertEquals(R.string.error_fetch_recommendation, GetRecommendationViewModel.errorMessageForHttpCode(404))
        assertEquals(R.string.error_fetch_recommendation, GetRecommendationViewModel.errorMessageForHttpCode(401))
    }

    @Test
    fun `known reasons from the compute service get their own message`() {
        assertEquals(R.string.error_recommendation_outside_coverage, GetRecommendationViewModel.errorMessageForCode("outside_coverage"))
        assertEquals(R.string.error_rec_type_unavailable, GetRecommendationViewModel.errorMessageForCode("rec_type_unavailable"))
        assertEquals(R.string.error_rec_no_fertilizer, GetRecommendationViewModel.errorMessageForCode("no_fertilizer"))
    }

    @Test
    fun `unknown or missing reasons leave the decision to the http status`() {
        assertEquals(null, GetRecommendationViewModel.errorMessageForCode("invalid_input"))
        assertEquals(null, GetRecommendationViewModel.errorMessageForCode(null))
    }
}
