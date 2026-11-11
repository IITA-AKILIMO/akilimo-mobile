package com.akilimo.mobile.network

import com.akilimo.mobile.network.dto.CassavaPriceDto
import com.akilimo.mobile.network.dto.CassavaUnitDto
import com.akilimo.mobile.network.dto.FeedbackResponse
import com.akilimo.mobile.network.dto.FertilizerDto
import com.akilimo.mobile.network.dto.FertilizerPriceDto
import com.akilimo.mobile.network.dto.InvestmentAmounts
import com.akilimo.mobile.network.dto.PagedResponse
import com.akilimo.mobile.network.dto.StarchFactoryDto
import com.akilimo.mobile.network.dto.UserFeedBackRequest
import com.akilimo.mobile.network.dto.RecommendationResponse
import com.akilimo.mobile.network.request.RecommendationRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AkilimoApi {

    companion object {
        private const val CLIENT_ID_HEADER = "Client-Id: akilimo-mobile"
        private const val DEFAULT_PAGE = 1
        private const val DEFAULT_PER_PAGE = 50
    }

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/fertilizers")
    suspend fun getFertilizers(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): PagedResponse<FertilizerDto>

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/fertilizer-prices")
    suspend fun getFertilizerPrices(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): PagedResponse<FertilizerPriceDto>

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/fertilizer-prices/{key}")
    suspend fun getFertilizerPricesByKey(
        @Path("key") key: String,
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): PagedResponse<FertilizerPriceDto>

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/investment-amounts")
    suspend fun getInvestments(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): PagedResponse<InvestmentAmounts>

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/starch-factories")
    suspend fun getStarchFactories(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): PagedResponse<StarchFactoryDto>

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/cassava-units")
    suspend fun getCassavaUnits(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): PagedResponse<CassavaUnitDto>


    @Headers(CLIENT_ID_HEADER)
    @GET("v1/cassava-prices")
    suspend fun getCassavaMarketPrices(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PER_PAGE
    ): PagedResponse<CassavaPriceDto>

    /**
     * GeoJSON of the area where AKILIMO can compute a recommendation for [country] (ISO alpha-2).
     * Answers 304 when [etag] still matches, 404 when the API has no area for the country.
     */
    @Headers(CLIENT_ID_HEADER)
    @GET("v1/coverage/{country}")
    suspend fun getCoverage(
        @Path("country") country: String,
        @Header("If-None-Match") etag: String? = null,
    ): Response<ResponseBody>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/recommendations/compute")
    suspend fun computeRecommendations(@Body payload: RecommendationRequest): Response<RecommendationResponse>

    @POST("v1/user-feedback")
    suspend fun submitUserFeedback(
        @Body userFeedback: UserFeedBackRequest
    ): Response<FeedbackResponse>
}
