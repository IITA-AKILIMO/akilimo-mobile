import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCassavaProduceType
import com.akilimo.mobile.enums.EnumMaizeProduceType
import com.akilimo.mobile.enums.EnumPotatoProduceType
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.helper.SessionManager
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaYieldRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.InvestmentRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.repos.SelectedInvestmentRepo
import com.akilimo.mobile.rest.request.*
import java.time.LocalDate

class RecommendationBuilder(
    private val database: AppDatabase,
    private val session: SessionManager
) {
//
//    companion object {
//        private const val DEFAULT_UNAVAILABLE = "NA"
//    }
//
//
//    private val profileRepo = AkilimoUserRepo(database.akilimoUserDao())
//    private val fertilizerRepo = FertilizerRepo(database.fertilizerDao())
//    private val investmentRepo = InvestmentRepo(database.investmentAmountDao())
//    private val cassavaYieldRepo = CassavaYieldRepo(database.cassavaYieldDao())
//    private val selectedInvestmentRepo = SelectedInvestmentRepo(database.selectedInvestmentDao())
//    private val selectedFertilizersRepo = SelectedFertilizerRepo(database.selectedFertilizerDao())
//
//    private val cassavaMarketRepo = SelectedCassavaMarketRepo(database.selectedCassavaMarketDao())
//
//
//    suspend fun build(): RecommendationRequest? {
//        val profile = profileRepo.getUser(session.akilimoUser) ?: return null
//        val cassavaMarketInfo = cassavaMarketRepo.getSelectedByUser(userId = profile.id ?: 0)
//        val investment = selectedInvestmentRepo.getSelectedByUser(userId = profile.id ?: 0)
//
//
//        val fieldYield = cassavaMarketInfo?.cassavaYield
//        val starchFactory = cassavaMarketInfo?.starchFactory
//
////        val practice = database.currentPracticeDao().findOne()
////        val costs = database.fieldOperationCostDao().findOne()
////        val maizePerf = database.maizePerformanceDao().findOne()
//
//
//        val maizeMarket = database.maizeMarketDao().findOne()
//        val potatoMarket = database.potatoMarketDao().findOne()
//
//
//        val userInfo = UserInfo(
//            deviceToken = session.deviceToken,
//            firstName = profile.firstName.orEmpty(),
//            lastName = profile.lastName.orEmpty(),
//            userName = profile.getNames(),
//            gender = profile.gender ?: DEFAULT_UNAVAILABLE,
//            farmName = profile.farmName.orEmpty(),
//            emailAddress = profile.email.orEmpty(),
//            phoneNumber = profile.mobileNumber ?: DEFAULT_UNAVAILABLE,
//            sendSms = profile.sendSms,
//            sendEmail = profile.sendEmail
//        )
//
//        val computeRequest = ComputeRequest(
//            riskAttitude = profile.riskAtt,
//            farmLocation = ComputeRequest.FarmLocation(
//                mapLat = profile.latitude,
//                mapLong = profile.longitude,
//                countryCode = profile.farmCountry.orEmpty(),
//                useCase = EnumUseCase.FR,
//            ),
//            farmInformation = ComputeRequest.FarmInformation(
//                fieldSize = profile.farmSize ?: 0.0,
//                areaUnit = profile.enumAreaUnit ?: EnumAreaUnit.ACRE,
//            ),
//            interCropping = ComputeRequest.InterCropping(
//                interCroppedCrop = "maize",
//                interCroppingMaizeRec = false, //TODO evaluate according to country
//                interCroppingPotatoRec = false,//TODO evaluate according to country
//            ),
//            recommendations = ComputeRequest.Recommendations(
//                fertilizerRec = true,
//                plantingPracticesRec = true,
//                scheduledPlantingRec = true,
//                scheduledHarvestRec = true
//            ),
//            planting = ComputeRequest.Planting(
//                plantingDate = profile.plantingDate ?: LocalDate.ofEpochDay(0),
//                harvestDate = profile.harvestDate ?: LocalDate.ofEpochDay(0),
//                plantingDateWindow = profile.plantingFlex,
//                harvestDateWindow = profile.harvestFlex
//            ),
//            fallow = ComputeRequest.Fallow(
//                fallowType = "none",
//                fallowHeight = 100,
//                fallowGreen = false
//            ),
//            tractorCosts = ComputeRequest.TractorCosts(
//                tractorPlough = true,
//                tractorHarrow = true,
//                tractorRidger = true,
//                costPloughing = costs?.tractorPloughCost,
//                costHarrowing = 0.0,
//                costRidging = 0.0,
//            ),
//            manualCosts = ComputeRequest.ManualCosts(
//                costPloughing = 0.0,
//                costHarrowing = 0.0,
//                costRidging = 0.0,
//            ),
//            weedingCosts = ComputeRequest.WeedingCosts(
//                costOne = 0.0,
//                costTwo = 0.0,
//            ),
//            operationsDone = ComputeRequest.OperationsDone(
//                ploughingDone = true,
//                harrowingDone = true,
//                ridgingDone = true,
//            ),
//            methods = ComputeRequest.Methods(
//                methodPloughing = practice?.ploughingMethod?.name,
//                methodHarrowing = practice?.harrowingMethod?.name,
//                methodRidging = practice?.ridgingMethod?.name,
//                methodWeeding = practice?.weedControlMethod?.name,
//            ),
//            yieldInfo = ComputeRequest.YieldInfo(
//                currentFieldYield = fieldYield?.yieldAmount ?: 0.0,
//                currentMaizePerformance = maizePerf?.performanceScore,
//                sellToStarchFactory = starchFactory?.name?.isNotEmpty() == true,
//                starchFactoryName = starchFactory?.name.orEmpty(),
//            ),
//            cassava = ComputeRequest.CropInfo(
//                produceType = EnumCassavaProduceType.ROOTS.produce(),
//                unitWeight = cassavaMarketInfo?.cassavaUnit?.unitWeight ?: 1000.0,
//                unitPrice = cassavaMarketInfo?.selectedCassavaMarket?.unitPrice ?: 0.0,
//            ),
//            maize = ComputeRequest.CropInfo(
//                produceType = EnumMaizeProduceType.GRAIN.produce(),
//                unitWeight = cassavaMarketInfo?.cassavaUnit?.unitWeight ?: 0.0,
//                unitPrice = cassavaMarketInfo?.selectedCassavaMarket?.unitPrice ?: 0.0,
//            ),
//            sweetPotato = ComputeRequest.CropInfo(
//                produceType = EnumPotatoProduceType.TUBERS.produce(),
//                unitWeight = cassavaMarketInfo?.cassavaUnit?.unitWeight ?: 0.0,
//                unitPrice = cassavaMarketInfo?.selectedCassavaMarket?.unitPrice ?: 0.0,
//            ),
//            maxInvestment = investment?.chosenAmount ?: 0.0,
//        )
//
//        val fertilizers = fertilizerRepo.byCountry(profile.farmCountry.orEmpty())
//        val selectedFertilizers =
//            selectedFertilizersRepo.getSelectedByUser(userId = profile.id ?: 0)
//        val fertilizerList = fertilizers.map { fertilizer ->
//            fertilizer.apply {
//                val selected = selectedFertilizers.find { it.fertilizerId == id }
//                selectedPrice = if (isSelected) selected?.fertilizerPrice ?: 0.0 else 0.0
//            }
//        }
//
//        val fertilizerRequestList = fertilizerList
//            .map {
//                FertilizerRequest(
//                    name = it.name.orEmpty(),
//                    fertilizerType = it.type.orEmpty(),
//                    weight = it.weight,
//                    key = it.key.orEmpty(),
//                    selected = it.isSelected,
//                    price = it.selectedPrice,
//                )
//            }
//
//        return RecommendationRequest(
//            userInfo = userInfo,
//            computeRequest = computeRequest,
//            fertilizerList = fertilizerRequestList
//        )
//    }
}
