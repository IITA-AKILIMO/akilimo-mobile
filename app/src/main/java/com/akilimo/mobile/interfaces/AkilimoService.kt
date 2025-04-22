package com.akilimo.mobile.interfaces


import com.akilimo.mobile.entities.AkilimoCurrencyResponse
import com.akilimo.mobile.entities.FertilizerPriceResponse
import com.akilimo.mobile.entities.FertilizerResponse
import com.akilimo.mobile.entities.InvestmentAmountResponse
import com.akilimo.mobile.rest.request.SurveyRequest
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


object AkilimoApi {
    val apiService: AkilimoService by lazy {
        RetrofitManager.akilimo().create(AkilimoService::class.java)
    }
}

interface AkilimoService {

    @GET("v1/currencies")
    fun listCurrencies(): Call<AkilimoCurrencyResponse>

    @GET("v1/fertilizers")
    fun getFertilizers(@Query("country_code") countryCode: String): Call<FertilizerResponse>

    @GET("v1/fertilizer-prices/{fertilizer_key}")
    fun getFertilizerPrices(@Path("fertilizer_key") fertilizerKey: String): Call<FertilizerPriceResponse>

    @GET("v1/investment-amounts/country/{country_code}")
    fun getInvestmentAmounts(@Path("country_code") countryCode: String): Call<InvestmentAmountResponse>

    @POST("v1/user-reviews")
    fun submitUserReview(@Body surveyRequest: SurveyRequest): Call<ResponseBody>
}
