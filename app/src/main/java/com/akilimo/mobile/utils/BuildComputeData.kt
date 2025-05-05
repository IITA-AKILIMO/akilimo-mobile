package com.akilimo.mobile.utils

import android.content.Context
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.rest.request.ComputeRequest
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.request.UserInfo
import org.modelmapper.ModelMapper
import org.modelmapper.TypeToken

class BuildComputeData(context: Context) {

    companion object {
        private val LOG_TAG: String = BuildComputeData::class.java.simpleName

        //        private const val DEFAULT_CASSAVA_PD = "roots"
//        private const val DEFAULT_MAIZE_PD = "fresh_cob"
        private const val DEFAULT_UNAVAILABLE = "NA"

//        private const val DEFAULT_FALLOW_TYPE = "none"

        //        private const val DEFAULT_MAIZE_PERFORMANCE_VALUE = "3"
        private const val DEFAULT_PRACTICE_METHOD = "NA"
        private const val DEFAULT_USERNAME = "akilimo"
    }

    private val modelMapper = ModelMapper()
    private val database = getDatabase(context)

    fun buildRecommendationReq(): RecommendationRequest {
        val userInfo = buildProfileInfo()

        val computeRequest = buildMandatoryInfo()


        buildRequestedRec(computeRequest)
        buildPlantingDates(computeRequest)
        buildInvestmentAmount(computeRequest)
        buildCurrentFieldYield(computeRequest)
        buildCurrentPractice(computeRequest)

        buildOperationCosts(computeRequest)
        buildMaizePerformance(computeRequest)
        buildCassavaMarketOutlet(computeRequest)
        buildMaizeMarketOutlet(computeRequest)
        buildSweetPotatoMarketOutlet(computeRequest)

        val fertilizerList: List<Fertilizer>
        val listType = object : TypeToken<List<Fertilizer?>?>() {}.type

        if (computeRequest.interCroppingPotatoRec || computeRequest.interCroppingMaizeRec) {
            val interCropFertilizers = database.fertilizerDao().findAllSelectedByCountry(
                computeRequest.countryCode
            )
            fertilizerList = modelMapper.map(interCropFertilizers, listType)
        } else {
            fertilizerList =
                database.fertilizerDao().findAllSelectedByCountry(computeRequest.countryCode)
        }


        return RecommendationRequest(userInfo, computeRequest, fertilizerList)
    }

    private fun buildProfileInfo(): UserInfo {

        val profileInfo = database.profileInfoDao().findOne() ?: return UserInfo()

        return UserInfo().apply {
            val firstName = profileInfo.firstName.orIfBlank(DEFAULT_USERNAME)
            val lastName = profileInfo.lastName.orIfBlank(DEFAULT_USERNAME)

            this.firstName = firstName
            this.lastName = lastName
            userName = profileInfo.names().orIfBlank(DEFAULT_USERNAME)
            gender = profileInfo.gender.orIfBlank(DEFAULT_UNAVAILABLE)
            fieldDescription = profileInfo.farmName.orIfBlank(DEFAULT_UNAVAILABLE)
            phoneNumber = profileInfo.fullMobileNumber.orIfBlank(DEFAULT_UNAVAILABLE)
            this.deviceToken = profileInfo.deviceToken.orEmpty()
            this.sendSms = profileInfo.sendSms
            this.sendEmail = profileInfo.sendEmail
        }
    }


    private fun buildMandatoryInfo(): ComputeRequest {
        val mandatoryInfo = database.mandatoryInfoDao().findOne()
        val locationInfo = database.locationInfoDao().findOne()
        val profileInfo = database.profileInfoDao().findOne()


        return ComputeRequest().apply {
            locationInfo?.let {
                mapLat = it.latitude
                mapLong = it.longitude
            }
            mandatoryInfo?.let {
                fieldArea = it.areaSize
                areaUnits = it.areaUnit
            }
            profileInfo?.let {
                currency = it.currencyCode
                countryCode = it.countryCode
                riskAttitude = it.riskAtt
            }
        }
    }

    private fun buildRequestedRec(computeRequest: ComputeRequest): ComputeRequest {
        database.useCaseDao().findOne()?.let { useCases ->
            computeRequest.apply {
                interCroppingMaizeRec = useCases.maizeInterCropping
                interCroppingPotatoRec = useCases.sweetPotatoInterCropping
                useCase = useCases.useCaseName

                fertilizerRec = useCases.fertilizerRecommendation
                plantingPracticesRec = useCases.bestPlantingPractices
                scheduledPlantingRec = useCases.scheduledPlanting
                scheduledHarvestRec = useCases.scheduledPlantingHighStarch
            }
        }
        return computeRequest
    }

    private fun buildCurrentFieldYield(computeRequest: ComputeRequest): ComputeRequest {
        database.fieldYieldDao().findOne()?.let {
            computeRequest.apply {
                currentFieldYield = it.yieldAmount
            }
        }
        return computeRequest
    }

    private fun buildPlantingDates(computeRequest: ComputeRequest): ComputeRequest {
        database.scheduleDateDao().findOne()?.let { sph ->
            computeRequest.apply {
                plantingDate = sph.plantingDate
                plantingDateWindow = sph.plantingWindow
                harvestDate = sph.harvestDate
                harvestDateWindow = sph.harvestWindow
            }
        }
        return computeRequest
    }

    private fun buildInvestmentAmount(computeRequest: ComputeRequest): ComputeRequest {
        database.investmentAmountDao().findOne()?.let { investmentAmount ->
            computeRequest.apply {
                maxInvestment = investmentAmount.maxInvestmentAmount
            }
        }
        return computeRequest
    }

    private fun buildCurrentPractice(computeRequest: ComputeRequest): ComputeRequest {
        database.currentPracticeDao().findOne()?.let { practice ->
            val methodPloughing = practice.ploughingMethod.orIfBlank(DEFAULT_PRACTICE_METHOD)
            val methodHarrowing = practice.harrowingMethod.orIfBlank(DEFAULT_PRACTICE_METHOD)
            val methodRidging = practice.ridgingMethod.orIfBlank(DEFAULT_PRACTICE_METHOD)
            val methodWeeding = practice.weedControlTechnique.orIfBlank(DEFAULT_PRACTICE_METHOD)

            computeRequest.apply {
                ploughingDone = practice.performPloughing
                harrowingDone = practice.performHarrowing
                ridgingDone = practice.performRidging

                tractorPlough = methodPloughing.equals("tractor", ignoreCase = true)
                tractorHarrow = methodHarrowing.equals("tractor", ignoreCase = true)
                tractorRidger = methodRidging.equals("tractor", ignoreCase = true)

                this.methodPloughing = methodPloughing
                this.methodHarrowing = methodHarrowing
                this.methodRidging = methodRidging
                this.methodWeeding = methodWeeding
            }
        }
        return computeRequest
    }

    private fun buildOperationCosts(computeRequest: ComputeRequest): ComputeRequest {
        database.fieldOperationCostDao().findOne()?.let { cost ->
            computeRequest.apply {
                costTractorPloughing = cost.tractorPloughCost
                costTractorHarrowing = cost.tractorHarrowCost
                costTractorRidging = cost.tractorRidgeCost

                costManualPloughing = cost.manualPloughCost
                costManualHarrowing = cost.manualHarrowCost
                costManualRidging = cost.manualRidgeCost

                costWeedingOne = cost.firstWeedingOperationCost
                costWeedingTwo = cost.secondWeedingOperationCost
            }
        }
        return computeRequest
    }

    private fun buildMaizePerformance(computeRequest: ComputeRequest): ComputeRequest {
        database.maizePerformanceDao().findOne()?.let { maizePerformance ->
            computeRequest.apply {
                currentMaizePerformance = maizePerformance.performanceValue
            }
        }


        return computeRequest
    }

    private fun buildCassavaMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        database.cassavaMarketDao().findOne()?.let { cassavaMarket ->
            computeRequest.apply {
                starchFactoryName = cassavaMarket.starchFactory
                sellToStarchFactory = cassavaMarket.isStarchFactoryRequired
                cassavaProduceType = cassavaMarket.produceType
                cassavaUnitWeight = cassavaMarket.unitWeight
                cassavaUnitPrice = cassavaMarket.unitPrice
            }
        }

        return computeRequest
    }

    private fun buildMaizeMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        database.maizeMarketDao().findOne()?.let { maizeMarket ->
            computeRequest.apply {
                maizeProduceType = maizeMarket.produceType
                maizeUnitWeight = maizeMarket.unitWeight
                maizeUnitPrice = maizeMarket.exactPrice
            }
        }

        return computeRequest
    }

    private fun buildSweetPotatoMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        database.potatoMarketDao().findOne()?.let { potatoMarket ->
            computeRequest.apply {
                sweetPotatoProduceType = potatoMarket.produceType
                sweetPotatoUnitWeight = potatoMarket.unitWeight
                sweetPotatoUnitPrice = potatoMarket.unitPrice
            }
        }


        return computeRequest
    }
}
