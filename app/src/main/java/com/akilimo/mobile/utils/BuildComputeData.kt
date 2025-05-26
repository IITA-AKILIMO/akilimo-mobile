package com.akilimo.mobile.utils

import android.content.Context
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.rest.request.ComputeRequest
import com.akilimo.mobile.rest.request.FertilizerRequest
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.request.UserInfo
import org.modelmapper.ModelMapper
import org.modelmapper.TypeToken

class BuildComputeData(val context: Context) {

    companion object {
        private val LOG_TAG: String = BuildComputeData::class.java.simpleName
        private const val DEFAULT_UNAVAILABLE = "NA"
        private const val DEFAULT_PRACTICE_METHOD = "NA"
        private const val DEFAULT_USERNAME = "akilimo"
    }

    private val modelMapper = ModelMapper()

    private val database = getDatabase(context)
    val session = SessionManager(context = context)

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


        val countryCode = computeRequest.countryCode.toString()
        val fertilizerDao = database.fertilizerDao()
        val useCases = database.useCaseDao().getAllUseCases()
        val fertilizerList =
            when {
                computeRequest.interCroppingPotatoRec -> {
                    fertilizerDao.findAllSelectedByCountryAndUseCases(countryCode, useCases)
                    computeRequest.interCroppedCrop = "sweetpotato"
                }

                computeRequest.interCroppingMaizeRec -> {
                    fertilizerDao.findAllSelectedByCountryAndUseCases(countryCode, useCases)
                    computeRequest.interCroppedCrop = "maize"
                }

                else -> {
                    fertilizerDao.findAllSelectedByCountry(countryCode)
                }
            }

        val result: List<FertilizerRequest> = modelMapper.map(
            fertilizerList,
            object : TypeToken<List<FertilizerRequest>>() {}.type
        )


        return RecommendationRequest(
            userInfo = userInfo,
            computeRequest = computeRequest,
            fertilizerList = result
        )
    }

    private fun buildProfileInfo(): UserInfo {
        val userInfo = UserInfo(
            deviceToken = session.getDeviceToken()
        )

        database.profileInfoDao().findOne()?.let { profile ->
            userInfo.apply {
                firstName = profile.firstName.orIfBlank(DEFAULT_USERNAME)
                lastName = profile.lastName.orIfBlank(DEFAULT_USERNAME)
                userName = firstName.plus(" ").plus(lastName)
                gender = profile.gender.orIfBlank(DEFAULT_UNAVAILABLE)
                farmName = profile.farmName.orIfBlank("my_farm")
                emailAddress = profile.email.orIfBlank("na@mail.com")
                phoneNumber = profile.phoneNumber.orIfBlank(DEFAULT_UNAVAILABLE)
                sendSms = profile.sendSms
                sendEmail = profile.sendEmail
            }
        }
        return userInfo
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
                fieldSize = it.areaSize
                areaUnit = it.areaUnit
            }
            profileInfo?.let {
                currencyCode = it.currencyCode
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
            val myPlantingDate = DateHelper.formatToLocalDate(sph.plantingDate)
            val myHarvestDate = DateHelper.formatToLocalDate(sph.harvestDate)
            computeRequest.apply {
                plantingDate = myPlantingDate.toString()
                harvestDate = myHarvestDate.toString()
                plantingDateWindow = sph.plantingWindow
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
            val myMethodPloughing = practice.ploughingMethod.orIfBlank(DEFAULT_PRACTICE_METHOD)
            val myMethodHarrowing = practice.harrowingMethod.orIfBlank(DEFAULT_PRACTICE_METHOD)
            val myMethodRidging = practice.ridgingMethod.orIfBlank(DEFAULT_PRACTICE_METHOD)
            val myMethodWeeding = practice.weedControlTechnique.orIfBlank(DEFAULT_PRACTICE_METHOD)

            computeRequest.apply {
                ploughingDone = practice.performPloughing
                harrowingDone = practice.performHarrowing
                ridgingDone = practice.performRidging

                tractorPlough = myMethodPloughing.equals("tractor", ignoreCase = true)
                tractorHarrow = myMethodHarrowing.equals("tractor", ignoreCase = true)
                tractorRidger = myMethodRidging.equals("tractor", ignoreCase = true)
                methodPloughing = myMethodPloughing
                methodHarrowing = myMethodHarrowing
                methodRidging = myMethodRidging
                methodWeeding = myMethodWeeding
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
                currentMaizePerformance = maizePerformance.performanceScore
                interCroppedCrop = "maize"
            }
        }


        return computeRequest
    }

    private fun buildCassavaMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        database.cassavaMarketDao().findOne()?.let { cassavaMarket ->
            computeRequest.apply {
                starchFactoryName = cassavaMarket.starchFactory.orIfBlank(DEFAULT_UNAVAILABLE)
                sellToStarchFactory = cassavaMarket.isStarchFactoryRequired
                cassavaProduceType = cassavaMarket.produceType.lowercase()
                cassavaUnitWeight = cassavaMarket.unitWeight
                cassavaUnitPrice = cassavaMarket.unitPrice
            }
        }

        return computeRequest
    }

    private fun buildMaizeMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        database.maizeMarketDao().findOne()?.let { maizeMarket ->
            computeRequest.apply {
                maizeProduceType = maizeMarket.produceType.lowercase()
                maizeUnitWeight = maizeMarket.unitWeight
                maizeUnitPrice = maizeMarket.exactPrice
            }
        }

        return computeRequest
    }

    private fun buildSweetPotatoMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        database.potatoMarketDao().findOne()?.let { potatoMarket ->
            computeRequest.apply {
                sweetPotatoProduceType = potatoMarket.produceType.lowercase()
                sweetPotatoUnitWeight = potatoMarket.unitWeight
                sweetPotatoUnitPrice = potatoMarket.unitPrice
            }
        }


        return computeRequest
    }
}
