package com.akilimo.mobile.interfaces


import com.akilimo.mobile.entities.AkilimoCurrencyResponse
import com.akilimo.mobile.entities.CassavaPricePriceResponse
import com.akilimo.mobile.entities.FertilizerPriceResponse
import com.akilimo.mobile.entities.FertilizerResponse
import com.akilimo.mobile.entities.InvestmentAmountResponse
import com.akilimo.mobile.entities.MaizePriceResponse
import com.akilimo.mobile.entities.OperationCostResponse
import com.akilimo.mobile.entities.PotatoPriceResponse
import com.akilimo.mobile.entities.StarchFactoryResponse
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.request.SurveyRequest
import com.akilimo.mobile.rest.response.RecommendationResponse
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap


object AkilimoApi {
    val apiService: AkilimoService by lazy {
        RetrofitManager.akilimo().create(AkilimoService::class.java)
    }
}

interface AkilimoService {

    @GET("v1/currencies")
    fun listCurrencies(): Call<AkilimoCurrencyResponse>

    @GET("v1/fertilizers/country/{country_code}")
    fun getFertilizers(@Path("country_code") countryCode: String): Call<FertilizerResponse>

    @GET("v1/fertilizer-prices/{fertilizer_key}")
    fun getFertilizerPrices(@Path("fertilizer_key") fertilizerKey: String): Call<FertilizerPriceResponse>

    @GET("v1/investment-amounts/country/{country_code}")
    fun getInvestmentAmounts(@Path("country_code") countryCode: String): Call<InvestmentAmountResponse>

    @POST("v1/user-reviews")
    fun submitUserReview(@Body surveyRequest: SurveyRequest): Call<ResponseBody>

    @POST("v1/recommendations/compute")
    fun computeRecommendations(@Body recommendationRequest: RecommendationRequest): Call<RecommendationResponse>

    @GET("v1/operation-costs/country/{country_code}")
    fun getOperationCosts(
        @Path("country_code") countryCode: String,
        @QueryMap queryParams: Map<String, String>
    ): Call<OperationCostResponse>

    @GET("v1/starch-factories/country/{country_code}")
    fun getStarchFactories(@Path("country_code") countryCode: String): Call<StarchFactoryResponse>

    @GET("v1/cassava-prices/country/{country_code}")
    fun getCassavaPrices(@Path("country_code") countryCode: String): Call<CassavaPricePriceResponse>

    @GET("v1/potato-prices/country/{country_code}")
    fun getPotatoPrices(@Path("country_code") countryCode: String?): Call<PotatoPriceResponse>

    @GET("v1/maize-prices/country/{country_code}")
    fun getMaizePrices(@Path("country_code") countryCode: String): Call<MaizePriceResponse>
}
