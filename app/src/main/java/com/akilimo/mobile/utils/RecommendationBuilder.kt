package com.akilimo.mobile.utils

import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.dao.MaizePerformanceRepo
import com.akilimo.mobile.enums.EnumCassavaProduceType
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.enums.EnumProduceType
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.helper.SessionManager
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaYieldRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.repos.InvestmentRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.repos.SelectedInvestmentRepo
import com.akilimo.mobile.rest.request.ComputeRequest
import com.akilimo.mobile.rest.request.FertilizerRequest
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.request.UserInfo
import java.time.LocalDate

class RecommendationBuilder(
    private val database: AppDatabase,
    private val session: SessionManager,
    private val useCase: EnumUseCase
) {

    companion object {
        private const val DEFAULT_UNAVAILABLE = "NA"
    }


    private val profileRepo = AkilimoUserRepo(database.akilimoUserDao())
    private val fertilizerRepo = FertilizerRepo(database.fertilizerDao())
    private val investmentRepo = InvestmentRepo(database.investmentAmountDao())
    private val cassavaYieldRepo = CassavaYieldRepo(database.cassavaYieldDao())
    private val fieldOpsRepo = FieldOperationCostsRepo(database.fieldOperationCostsDao())
    private val currentPracticeRepo = CurrentPracticeRepo(database.currentPracticeDao())
    private val selectedInvestmentRepo = SelectedInvestmentRepo(database.selectedInvestmentDao())
    private val selectedFertilizersRepo = SelectedFertilizerRepo(database.selectedFertilizerDao())
    private val maizePerfRepo = MaizePerformanceRepo(database.maizePerformanceDao())

    private val cassavaMarketRepo = SelectedCassavaMarketRepo(database.selectedCassavaMarketDao())


    suspend fun build(): RecommendationRequest? {
        val profile = profileRepo.getUser(session.akilimoUser) ?: return null
        val cassavaMarketInfo = cassavaMarketRepo.getSelectedByUser(userId = profile.id ?: 0)
        val investment = selectedInvestmentRepo.getSelectedByUser(userId = profile.id ?: 0)
        val fieldYield = cassavaMarketInfo?.cassavaYield
        val starchFactory = cassavaMarketInfo?.starchFactory

        val tillageOperations = profile.tillageOperations
        val fieldOperations = fieldOpsRepo.getCostForUser(profile.id ?: 0)
        val maizePerf = maizePerfRepo.getPerformanceForUser(profile.id ?: 0)


        val tractorCosts = ComputeRequest.TractorCosts(
            hasTractorPlough = fieldOperations?.tractorPloughCost.isPositive(),
            hasTractorHarrow = fieldOperations?.tractorHarrowCost.isPositive(),
            hasTractorRidger = fieldOperations?.tractorRidgeCost.isPositive(),
            costPloughing = fieldOperations?.tractorPloughCost.orZero(),
            costHarrowing = fieldOperations?.tractorHarrowCost.orZero(),
            costRidging = fieldOperations?.tractorRidgeCost.orZero()
        )

        val manualCosts = ComputeRequest.ManualCosts(
            costPloughing = fieldOperations?.manualPloughCost ?: 0.0,
            costHarrowing = fieldOperations?.manualHarrowCost ?: 0.0,
            costRidging = fieldOperations?.manualRidgeCost ?: 0.0
        )
        val practice = currentPracticeRepo.getPracticeForUser(profile.id ?: 0)
        val operationMethods = ComputeRequest.Methods(
            methodPloughing = practice?.ploughingMethod.orUnavailable(DEFAULT_UNAVAILABLE),
            methodHarrowing = practice?.harrowingMethod.orUnavailable(DEFAULT_UNAVAILABLE),
            methodRidging = practice?.ridgingMethod.orUnavailable(DEFAULT_UNAVAILABLE),
            methodWeeding = practice?.weedControlMethod?.name.orUnavailable(DEFAULT_UNAVAILABLE),
        )


        val maizeMarket =
            database.produceMarketDao().findOne(profile.id ?: 0, EnumMarketType.MAIZE_MARKET)
        val potatoMarket =
            database.produceMarketDao().findOne(profile.id ?: 0, EnumMarketType.SWEET_POTATO_MARKET)


        val userInfo = UserInfo(
            deviceToken = session.deviceToken,
            firstName = profile.firstName.orEmpty(),
            lastName = profile.userName.orUnavailable("akilimo"),
            userName = profile.getNames(),
            gender = profile.gender.orUnavailable(DEFAULT_UNAVAILABLE),
            farmName = profile.farmName.orUnavailable("my_farm"),
            emailAddress = profile.email.orEmpty(),
            phoneNumber = profile.mobileNumber.orEmpty(),
            sendSms = profile.sendSms,
            sendEmail = profile.sendEmail,
            riskAttitude = profile.riskAtt,
        )

        val computeRequest = ComputeRequest(
            farmInformation = ComputeRequest.FarmInformation(
                useCase = useCase,
                fieldSize = profile.farmSize,
                mapLat = profile.latitude,
                mapLong = profile.longitude,
                countryCode = profile.enumCountry.name,
                areaUnit = profile.enumAreaUnit,
            ),
            interCropping = ComputeRequest.InterCropping(
                interCroppedCrop = "maize", //TODO evaluate according to country
                interCroppingMaizeRec = useCase == EnumUseCase.CIM,
                interCroppingPotatoRec = useCase == EnumUseCase.CIS
            ),
            recommendations = ComputeRequest.Recommendations(
                fertilizerRec = true,
                plantingPracticesRec = true,
                scheduledPlantingRec = true,
                scheduledHarvestRec = true
            ),
            planting = ComputeRequest.Planting(
                plantingDate = profile.plantingDate ?: LocalDate.ofEpochDay(0),
                harvestDate = profile.harvestDate ?: LocalDate.ofEpochDay(0),
                plantingDateWindow = profile.plantingFlex,
                harvestDateWindow = profile.harvestFlex
            ),
            fallow = ComputeRequest.Fallow(
                fallowType = "none",
                fallowHeight = 100,
                fallowGreen = false
            ),
            tractorCosts = tractorCosts,
            manualCosts = manualCosts,
            weedingCosts = ComputeRequest.WeedingCosts(
                costOne = fieldOperations?.firstWeedingOperationCost ?: 0.0,
                costTwo = fieldOperations?.secondWeedingOperationCost ?: 0.0,
            ),
            operationsDone = ComputeRequest.OperationsDone(
                ploughingDone = practice?.performPloughing == true,
                harrowingDone = practice?.performHarrowing == true,
                ridgingDone = practice?.performRidging == true,
            ),
            methods = operationMethods,
            yieldInfo = ComputeRequest.YieldInfo(
                currentFieldYield = fieldYield?.yieldAmount ?: 0.0,
                currentMaizePerformance = maizePerf?.maizePerformance?.performanceValue ?: 0,
                sellToStarchFactory = starchFactory?.name?.isNotEmpty() == true,
                starchFactoryName = starchFactory?.name ?: DEFAULT_UNAVAILABLE,
            ),
            cassava = ComputeRequest.CropInfo(
                produceType = EnumCassavaProduceType.ROOTS.produce(),
                unitWeight = cassavaMarketInfo?.cassavaUnit?.unitWeight ?: 1000.0,
                unitPrice = cassavaMarketInfo?.selectedCassavaMarket?.unitPrice ?: 0.0,
            ),
            maize = ComputeRequest.CropInfo(
                produceType = maizeMarket?.produceType?.produce()
                    ?: EnumProduceType.MAIZE_GRAIN.produce(),
                unitWeight = maizeMarket?.unitOfSale?.weight ?: 0.0,
                unitPrice = maizeMarket?.unitPrice ?: 0.0,
            ),
            sweetPotato = ComputeRequest.CropInfo(
                produceType = potatoMarket?.produceType?.produce()
                    ?: EnumProduceType.SWEET_POTATO_TUBERS.produce(),
                unitWeight = potatoMarket?.unitOfSale?.weight ?: 0.0,
                unitPrice = potatoMarket?.unitPrice ?: 0.0,
            ),
            maxInvestment = investment?.chosenAmount ?: 0.0,
        )

        val fertilizers = fertilizerRepo.byCountry(profile.enumCountry)
        val selectedFertilizers =
            selectedFertilizersRepo.getSelectedByUser(userId = profile.id ?: 0)
        val fertilizerList = fertilizers.map { fertilizer ->
            fertilizer.apply {
                val selected = selectedFertilizers.find { it.fertilizerId == id }
                if (selected != null) {
                    isSelected = true
                    selectedPrice = selected.fertilizerPrice
                } else {
                    isSelected = false
                    selectedPrice = 0.0
                }
            }
        }


        val fertilizerRequestList = fertilizerList
            .map {
                FertilizerRequest(
                    name = it.name.orEmpty(),
                    fertilizerType = it.type.orEmpty(),
                    weight = it.weight,
                    key = it.key.orEmpty(),
                    selected = it.isSelected,
                    price = it.selectedPrice,
                )
            }

        return RecommendationRequest(
            userInfo = userInfo,
            computeRequest = computeRequest,
            fertilizerList = fertilizerRequestList
        )
    }
}
