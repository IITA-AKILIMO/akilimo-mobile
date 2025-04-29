package com.akilimo.mobile.utils

import android.content.Context
import com.akilimo.mobile.dao.AppDatabase.Companion.getDatabase
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.rest.request.ComputeRequest
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.request.UserInfo
import com.google.android.gms.common.util.Strings
import io.sentry.Sentry
import org.modelmapper.ModelMapper
import org.modelmapper.TypeToken

class BuildComputeData(context: Context) {

    companion object {
        private val LOG_TAG: String = BuildComputeData::class.java.simpleName

        private const val DEFAULT_CASSAVA_PD = "roots"
        private const val DEFAULT_MAIZE_PD = "fresh_cob"
        private const val DEFAULT_SWEET_POTATO_PD = "tubers"
        private const val DEFAULT_UNAVAILABLE = "NA"
        private const val DEFAULT_FALLOW_TYPE = "none"
        private const val DEFAULT_MAIZE_PERFORMANCE_VALUE = "3"
        private const val DEFAULT_PRACTICE_METHOD = "NA"

        private const val DEFAULT_FIELD_YIELD = 11
        private const val DEFAULT_UNAVAILABLE_INT = 0
        private const val DEFAULT_UNIT_WEIGHT = 50
        private const val DEFAULT_LMNO_BASIS = "areaUnit"
        private const val DEFAULT_USERNAME = "Akilimo Farmer"
        private const val DEFAULT_FIELD_DESC = "Akilimo field"
    }

    private var smsRequired = false
    private var emailRequired = false
    private var countryCode: String? = DEFAULT_UNAVAILABLE
    private var emailAddress: String? = DEFAULT_UNAVAILABLE
    private var mobileNumber: String? = DEFAULT_UNAVAILABLE
    private var fullPhoneNumber: String? = DEFAULT_UNAVAILABLE
    private var mobileCountryCode: String? = DEFAULT_UNAVAILABLE


    private var cassavaUnitWeight = DEFAULT_UNIT_WEIGHT
    private val cassavaUnitPriceLocal = 0.0
    private val maizeUnitPriceLocal = 0.0
    private val potatoUnitPriceLocal = 0.0

    private var maxInvestmentAmountLocal = 0.0
    private val unitOfSale: String? = null
    private var areaUnits: String? = DEFAULT_UNAVAILABLE
    private var fieldArea = 0.0

    private val interCroppingType = DEFAULT_UNAVAILABLE
    private val interCroppingRec = false
    private val fertilizerRec = false
    private val plantingPracticesRec = false
    private val scheduledPlantingRec = false
    private val scheduledHarvestRec = false

    private var harvestDate: String? = DEFAULT_UNAVAILABLE
    private var plantingDate: String? = DEFAULT_UNAVAILABLE

    private var plantingDateWindow = 0
    private var harvestDateWindow = 0
    private var currentFieldYield = DEFAULT_FIELD_YIELD

    private val fallowType = DEFAULT_FALLOW_TYPE
    private val fallowGreen = false
    private val fallowHeight = 100
    private val problemWeeds = false


    private val costLmoAreaBasis = DEFAULT_LMNO_BASIS
    private var costTractorPlough = 0.0
    private var costTractorHarrow = 0.0

    private var costTractorRidging = 0.0
    private var costManualPloughing = 0.0
    private var costManualHarrowing = 0.0
    private var costManualRidging = 0.0

    private var costWeedingOne = 0.0
    private var costWeedingTwo = 0.0

    private var performsPloughing = false
    private var performsHarrowing = false
    private var performsRidging = false
    private var sellToStarchFactory = false

    private var methodHarrowing: String? = DEFAULT_PRACTICE_METHOD
    private var methodPloughing: String? = DEFAULT_PRACTICE_METHOD
    private var methodRidging: String? = DEFAULT_PRACTICE_METHOD
    private var methodWeeding: String? = DEFAULT_PRACTICE_METHOD

    private var maizeProdType = DEFAULT_MAIZE_PD
    private var maizeUnitWeight = DEFAULT_UNIT_WEIGHT
    private var maizeUnitPrice = 0.0
    private var currentMaizePerformance: String? = DEFAULT_MAIZE_PERFORMANCE_VALUE

    private var sweetPotatoProdType = DEFAULT_SWEET_POTATO_PD
    private var sweetPotatoUnitWeight = DEFAULT_UNIT_WEIGHT
    private var sweetPotatoUnitPrice = 0.0

    private var deviceToken: String? = DEFAULT_USERNAME
    private var fullNames = DEFAULT_USERNAME
    private var gender: String? = DEFAULT_UNAVAILABLE
    private val secondName = DEFAULT_USERNAME
    private var farmName: String? = DEFAULT_FIELD_DESC
    private val riskAtt = DEFAULT_UNAVAILABLE_INT

    private val cassavaUpmOne = 0.0
    private val cassavaUpmTwo = 0.0
    private val cassavaUppOne = 0.0
    private val cassavaUppTwo = 0.0
    private var cassavaUnitPrice = 0.0
    private var cassavaProduceType = DEFAULT_CASSAVA_PD
    private var starchFactoryName: String? = DEFAULT_UNAVAILABLE


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
        buildWeedManagement(computeRequest)
        buildMaizePerformance(computeRequest)
        buildCassavaMarketOutlet(computeRequest)
        buildMaizeMarketOutlet(computeRequest)
        buildSweetPotatoMarketOutlet(computeRequest)

        val fertilizerList: List<Fertilizer>
        val listType = object : TypeToken<List<Fertilizer?>?>() {
        }.type

        if (computeRequest.interCroppingPotatoRec || computeRequest.interCroppingMaizeRec) {
            val interCropFertilizers = database.fertilizerDao().findAllSelectedByCountry(
                countryCode!!
            )
            fertilizerList = modelMapper.map(interCropFertilizers, listType)
        } else {
            fertilizerList = database.fertilizerDao().findAllSelectedByCountry(countryCode!!)
        }


        return RecommendationRequest(userInfo, computeRequest, fertilizerList)
    }

    private fun buildProfileInfo(): UserInfo {
        return try {
            val profileInfo = database.profileInfoDao().findOne() ?: return UserInfo()

            UserInfo().apply {
                val firstName = profileInfo.firstName.orIfBlank(DEFAULT_USERNAME)
                val lastName = profileInfo.lastName.orIfBlank(DEFAULT_USERNAME)

                this.firstName = firstName
                this.lastName = lastName
                this.userName = profileInfo.names().orIfBlank(DEFAULT_USERNAME)
                this.gender = profileInfo.gender.orIfBlank(DEFAULT_UNAVAILABLE)
                this.fieldDescription = profileInfo.farmName.orIfBlank(DEFAULT_UNAVAILABLE)
                this.mobileNumber = profileInfo.fullMobileNumber.orIfBlank(DEFAULT_UNAVAILABLE)
                this.fullPhoneNumber = profileInfo.fullMobileNumber.orIfBlank(DEFAULT_UNAVAILABLE)
                this.mobileCountryCode = profileInfo.mobileCode.orIfBlank(DEFAULT_UNAVAILABLE)
                this.emailAddress = profileInfo.email.orIfBlank(DEFAULT_UNAVAILABLE)
                this.deviceToken = profileInfo.deviceToken.orEmpty()
                this.sendSms = profileInfo.sendSms
                this.sendEmail = profileInfo.sendEmail
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            UserInfo()
        }
    }


    private fun buildMandatoryInfo(): ComputeRequest {
        val computeRequest = ComputeRequest()
        val mandatoryInfo = database.mandatoryInfoDao().findOne()
        val locationInfo = database.locationInfoDao().findOne()
        val profileInfo = database.profileInfoDao().findOne()
        if (locationInfo != null) {
            computeRequest.mapLat = locationInfo.latitude
            computeRequest.mapLong = locationInfo.longitude
        }
        if (mandatoryInfo != null) {
            fieldArea = mandatoryInfo.areaSize
            areaUnits = mandatoryInfo.areaUnit
            computeRequest.riskAttitude = riskAtt

            computeRequest.fieldArea = fieldArea
            computeRequest.areaUnits = areaUnits
        }

        if (profileInfo != null) {
            countryCode = profileInfo.countryCode
            computeRequest.currency = profileInfo.currencyCode
            computeRequest.country = countryCode
            computeRequest.riskAttitude = profileInfo.riskAtt
        }
        return computeRequest
    }

    private fun buildRequestedRec(computeRequest: ComputeRequest): ComputeRequest {
        //check for values we have to give recommendations for
        val useCases = database.useCaseDao().findOne()
        if (useCases != null) {
            computeRequest.interCroppingMaizeRec = useCases.maizeInterCropping
            computeRequest.interCroppingPotatoRec = useCases.sweetPotatoInterCropping
            computeRequest.useCase = useCases.useCaseName

            computeRequest.fertilizerRec = useCases.fertilizerRecommendation
            computeRequest.plantingPracticesRec = useCases.bestPlantingPractices
            computeRequest.scheduledPlantingRec = useCases.scheduledPlanting
            computeRequest.scheduledHarvestRec = useCases.scheduledPlantingHighStarch
        }
        return computeRequest
    }

    private fun buildCurrentFieldYield(computeRequest: ComputeRequest): ComputeRequest {
        //check for values we have to give recommendations for
        val fieldYield = database.fieldYieldDao().findOne()
        if (fieldYield != null) {
            currentFieldYield = fieldYield.yieldAmount.toInt()
        }
        computeRequest.currentFieldYield = currentFieldYield

        return computeRequest
    }

    private fun buildPlantingDates(computeRequest: ComputeRequest): ComputeRequest {
        try {
            val sph = database.scheduleDateDao().findOne()
            if (sph != null) {
                plantingDate = sph.plantingDate
                plantingDateWindow = sph.plantingWindow
                harvestDate = sph.harvestDate
                harvestDateWindow = sph.harvestWindow

                computeRequest.plantingDate = plantingDate
                computeRequest.plantingDateWindow = plantingDateWindow
                computeRequest.harvestDate = harvestDate
                computeRequest.harvestDateWindow = harvestDateWindow
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }

        return computeRequest
    }

    private fun buildInvestmentAmount(computeRequest: ComputeRequest): ComputeRequest {
        val investmentAmount = database.investmentAmountDao().findOne()
        if (investmentAmount != null) {
            maxInvestmentAmountLocal = investmentAmount.maxInvestmentAmount
        }
        computeRequest.maxInvestment = maxInvestmentAmountLocal
        return computeRequest
    }

    private fun buildCurrentPractice(computeRequest: ComputeRequest): ComputeRequest {
        val currentPractice = database.currentPracticeDao().findOne()

        if (currentPractice != null) {
            performsPloughing = currentPractice.performPloughing
            performsHarrowing = currentPractice.performHarrowing
            performsRidging = currentPractice.performRidging

            methodHarrowing =
                if (Strings.isEmptyOrWhitespace(currentPractice.harrowingMethod)) DEFAULT_PRACTICE_METHOD else currentPractice.harrowingMethod
            methodPloughing =
                if (Strings.isEmptyOrWhitespace(currentPractice.ploughingMethod)) DEFAULT_PRACTICE_METHOD else currentPractice.ploughingMethod
            methodRidging =
                if (Strings.isEmptyOrWhitespace(currentPractice.ridgingMethod)) DEFAULT_PRACTICE_METHOD else currentPractice.ridgingMethod
            methodWeeding =
                if (Strings.isEmptyOrWhitespace(currentPractice.weedControlTechnique)) DEFAULT_PRACTICE_METHOD else currentPractice.weedControlTechnique
        }

        computeRequest.ploughingDone = performsPloughing
        computeRequest.harrowingDone = performsHarrowing
        computeRequest.ridgingDone = performsRidging
        if (methodPloughing.equals("tractor", ignoreCase = true)) {
            computeRequest.tractorPlough = true
        }
        if (methodHarrowing.equals("tractor", ignoreCase = true)) {
            computeRequest.tractorHarrow = true
        }
        if (methodRidging.equals("tractor", ignoreCase = true)) {
            computeRequest.tractorRidger = true
        }

        computeRequest.methodHarrowing = methodHarrowing
        computeRequest.methodPloughing = methodPloughing
        computeRequest.methodRidging = methodRidging
        computeRequest.methodWeeding = methodWeeding


        return computeRequest
    }

    private fun buildOperationCosts(computeRequest: ComputeRequest): ComputeRequest {
        val fieldOperationCost = database.fieldOperationCostDao().findOne()

        if (fieldOperationCost != null) {
            costTractorPlough = fieldOperationCost.tractorPloughCost
            costTractorHarrow = fieldOperationCost.tractorHarrowCost
            costTractorRidging = fieldOperationCost.tractorRidgeCost

            costManualPloughing = fieldOperationCost.manualPloughCost
            costManualHarrowing = fieldOperationCost.manualHarrowCost
            costManualRidging = fieldOperationCost.manualRidgeCost

            costWeedingOne = fieldOperationCost.firstWeedingOperationCost
            costWeedingTwo = fieldOperationCost.secondWeedingOperationCost
        }

        computeRequest.costLmoAreaBasis = costLmoAreaBasis

        computeRequest.costTractorPloughing = costTractorPlough
        computeRequest.costTractorHarrowing = costTractorHarrow
        computeRequest.costTractorRidging = costTractorRidging

        computeRequest.costManualPloughing = costManualPloughing
        computeRequest.costManualHarrowing = costManualHarrowing
        computeRequest.costManualRidging = costManualRidging

        computeRequest.costWeedingOne = costWeedingOne
        computeRequest.costWeedingTwo = costWeedingTwo

        return computeRequest
    }

    private fun buildWeedManagement(computeRequest: ComputeRequest): ComputeRequest {
        computeRequest.fallowType = fallowType
        computeRequest.fallowGreen = fallowGreen
        computeRequest.fallowHeight = fallowHeight
        computeRequest.problemWeeds = problemWeeds

        return computeRequest
    }

    private fun buildMaizePerformance(computeRequest: ComputeRequest): ComputeRequest {
        val maizePerformance = database.maizePerformanceDao().findOne()
        if (maizePerformance != null) {
            currentMaizePerformance =
                if (Strings.isEmptyOrWhitespace(maizePerformance.performanceValue)) DEFAULT_MAIZE_PERFORMANCE_VALUE else maizePerformance.performanceValue
        }
        computeRequest.currentMaizePerformance = currentMaizePerformance

        return computeRequest
    }

    private fun buildCassavaMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        val cassavaMarket = database.cassavaMarketDao().findOne()

        if (cassavaMarket != null) {
            sellToStarchFactory = cassavaMarket.isStarchFactoryRequired
            if (sellToStarchFactory) {
                starchFactoryName = cassavaMarket.starchFactory
            }

            cassavaProduceType = cassavaMarket.produceType
            val uw = cassavaMarket.unitWeight
            cassavaUnitWeight = if (uw <= 0) DEFAULT_UNIT_WEIGHT else uw

            cassavaUnitPrice = cassavaMarket.unitPrice
        }
        computeRequest.starchFactoryName = starchFactoryName
        computeRequest.sellToStarchFactory = sellToStarchFactory

        computeRequest.cassavaProduceType = cassavaProduceType
        computeRequest.cassavaUnitPrice = cassavaUnitPrice
        computeRequest.cassavaUnitWeight = cassavaUnitWeight

        computeRequest.cassUPM1 = cassavaUpmOne
        computeRequest.cassUPM2 = cassavaUpmTwo
        computeRequest.cassUPP1 = cassavaUppOne
        computeRequest.cassUPP2 = cassavaUppTwo


        return computeRequest
    }

    private fun buildMaizeMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        val maizeMarket = database.maizeMarketDao().findOne()
        if (maizeMarket != null) {
            maizeProdType = maizeMarket.produceType
            maizeUnitWeight = maizeMarket.unitWeight
            maizeUnitPrice = maizeMarket.exactPrice

            val uw = maizeMarket.unitWeight
            maizeUnitWeight = if (uw <= 0) DEFAULT_UNIT_WEIGHT else uw
        }
        computeRequest.maizeProduceType = maizeProdType
        computeRequest.maizeUnitWeight = maizeUnitWeight
        computeRequest.maizeUnitPrice = maizeUnitPrice

        return computeRequest
    }

    private fun buildSweetPotatoMarketOutlet(computeRequest: ComputeRequest): ComputeRequest {
        val potatoMarket = database.potatoMarketDao().findOne()
        if (potatoMarket != null) {
            sweetPotatoProdType = potatoMarket.produceType
            sweetPotatoUnitWeight = potatoMarket.unitWeight
            sweetPotatoUnitPrice = potatoMarket.unitPrice

            val uw = potatoMarket.unitWeight
            sweetPotatoUnitWeight = if (uw <= 0) DEFAULT_UNIT_WEIGHT else uw
        }

        computeRequest.sweetPotatoProduceType = sweetPotatoProdType
        computeRequest.sweetPotatoUnitWeight = sweetPotatoUnitWeight
        computeRequest.sweetPotatoUnitPrice = sweetPotatoUnitPrice
        return computeRequest
    }
}
