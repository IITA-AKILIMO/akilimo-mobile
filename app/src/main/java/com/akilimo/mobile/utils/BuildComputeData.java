package com.akilimo.mobile.utils;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.akilimo.mobile.dao.AppDatabase;
import com.akilimo.mobile.entities.CassavaMarket;
import com.akilimo.mobile.entities.CurrentPractice;
import com.akilimo.mobile.entities.Fertilizer;
import com.akilimo.mobile.entities.FieldOperationCost;
import com.akilimo.mobile.entities.FieldYield;
import com.akilimo.mobile.entities.InterCropFertilizer;
import com.akilimo.mobile.entities.InvestmentAmount;
import com.akilimo.mobile.entities.LocationInfo;
import com.akilimo.mobile.entities.MaizeMarket;
import com.akilimo.mobile.entities.MaizePerformance;
import com.akilimo.mobile.entities.MandatoryInfo;
import com.akilimo.mobile.entities.PotatoMarket;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.entities.ScheduledDate;
import com.akilimo.mobile.entities.UseCases;
import com.akilimo.mobile.rest.request.ComputeRequest;
import com.akilimo.mobile.rest.request.RecommendationRequest;
import com.akilimo.mobile.rest.request.UserInfo;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class BuildComputeData {

    private static final String LOG_TAG = BuildComputeData.class.getSimpleName();

    private static final String DEFAULT_CASSAVA_PD = "roots";
    private static final String DEFAULT_MAIZE_PD = "fresh_cob";
    private static final String DEFAULT_SWEET_POTATO_PD = "tubers";
    private static final String DEFAULT_UNAVAILABLE = "NA";
    private static final String DEFAULT_FALLOW_TYPE = "none";
    private static final String DEFAULT_MAIZE_PERFORMANCE_VALUE = "3";
    private static final String DEFAULT_PRACTICE_METHOD = "NA";

    private static final int DEFAULT_FIELD_YIELD = 11;
    private static final int DEFAULT_UNAVAILABLE_INT = 0;
    private static final int DEFAULT_UNIT_WEIGHT = 50;
    private static final String DEFAULT_LMNO_BASIS = "areaUnit";
    private static final String DEFAULT_USERNAME = "Akilimo Farmer";
    private static final String DEFAULT_FIELD_DESC = "Akilimo field";


    private boolean smsRequired = false;
    private boolean emailRequired = false;
    private String countryCode = DEFAULT_UNAVAILABLE;
    private String emailAddress = DEFAULT_UNAVAILABLE;
    private String mobileNumber = DEFAULT_UNAVAILABLE;
    private String fullPhoneNumber = DEFAULT_UNAVAILABLE;
    private String mobileCountryCode = DEFAULT_UNAVAILABLE;


    private int cassavaUnitWeight = DEFAULT_UNIT_WEIGHT;
    private double cassavaUnitPriceLocal;
    private double maizeUnitPriceLocal;
    private double potatoUnitPriceLocal;

    private double maxInvestmentAmountLocal = 0.0;
    private String unitOfSale;
    private String areaUnits = DEFAULT_UNAVAILABLE;
    private double fieldArea = 0.0;

    private String interCroppingType = DEFAULT_UNAVAILABLE;
    private boolean interCroppingRec = false;
    private boolean fertilizerRec = false;
    private boolean plantingPracticesRec = false;
    private boolean scheduledPlantingRec = false;
    private boolean scheduledHarvestRec = false;

    private String harvestDate = DEFAULT_UNAVAILABLE;
    private String plantingDate = DEFAULT_UNAVAILABLE;

    private int plantingDateWindow = 0;
    private int harvestDateWindow = 0;
    private int currentFieldYield = DEFAULT_FIELD_YIELD;

    private String fallowType = DEFAULT_FALLOW_TYPE;
    private boolean fallowGreen = false;
    private int fallowHeight = 100;
    private boolean problemWeeds = false;


    private String costLmoAreaBasis = DEFAULT_LMNO_BASIS;
    private double costTractorPlough;
    private double costTractorHarrow;

    private double costTractorRidging;
    private double costManualPloughing;
    private double costManualHarrowing;
    private double costManualRidging;

    private double costWeedingOne;
    private double costWeedingTwo;

    private boolean performsPloughing;
    private boolean performsHarrowing;
    private boolean performsRidging;
    private boolean sellToStarchFactory;

    private String methodHarrowing = DEFAULT_PRACTICE_METHOD;
    private String methodPloughing = DEFAULT_PRACTICE_METHOD;
    private String methodRidging = DEFAULT_PRACTICE_METHOD;
    private String methodWeeding = DEFAULT_PRACTICE_METHOD;

    private String maizeProdType = DEFAULT_MAIZE_PD;
    private int maizeUnitWeight = DEFAULT_UNIT_WEIGHT;
    private double maizeUnitPrice;
    private String currentMaizePerformance = DEFAULT_MAIZE_PERFORMANCE_VALUE;

    private String sweetPotatoProdType = DEFAULT_SWEET_POTATO_PD;
    private int sweetPotatoUnitWeight = DEFAULT_UNIT_WEIGHT;
    private double sweetPotatoUnitPrice;

    private String deviceToken = DEFAULT_USERNAME;
    private String fullNames = DEFAULT_USERNAME;
    private String gender = DEFAULT_UNAVAILABLE;
    private String secondName = DEFAULT_USERNAME;
    private String farmName = DEFAULT_FIELD_DESC;
    private int riskAtt = DEFAULT_UNAVAILABLE_INT;

    private double cassavaUpmOne;
    private double cassavaUpmTwo;
    private double cassavaUppOne;
    private double cassavaUppTwo;
    private double cassavaUnitPrice = 0.0;
    private String cassavaProduceType = DEFAULT_CASSAVA_PD;
    private String starchFactoryName = DEFAULT_UNAVAILABLE;


    private MathHelper mathHelper;
    private ModelMapper modelMapper;
    private SessionManager sessionManager;
    private AppDatabase database;

    public BuildComputeData(@NonNull Activity activity) {
        database = AppDatabase.getDatabase(activity);
        mathHelper = new MathHelper(activity);
        modelMapper = new ModelMapper();
        sessionManager = new SessionManager(activity);
    }

    public RecommendationRequest buildRecommendationReq() {
        UserInfo userInfo = buildProfileInfo();

        ComputeRequest computeRequest = buildMandatoryInfo();


        buildRequestedRec(computeRequest);
        buildPlantingDates(computeRequest);
        buildInvestmentAmount(computeRequest);
        buildCurrentFieldYield(computeRequest);
        buildCurrentPractice(computeRequest);

        buildOperationCosts(computeRequest);
        buildWeedManagement(computeRequest);
        buildMaizePerformance(computeRequest);
        buildCassavaMarketOutlet(computeRequest);
        buildMaizeMarketOutlet(computeRequest);
        buildSweetPotatoMarketOutlet(computeRequest);

        List<Fertilizer> fertilizerList;
        Type listType = new TypeToken<List<Fertilizer>>() {
        }.getType();

        if (computeRequest.getInterCroppingPotatoRec() || computeRequest.getInterCroppingMaizeRec()) {
            List<InterCropFertilizer> interCropFertilizers = database.interCropFertilizerDao().findAllByCountry(countryCode);
            fertilizerList = modelMapper.map(interCropFertilizers, listType);
        } else {
            fertilizerList = database.fertilizerDao().findAllByCountry(countryCode);
        }


        return new RecommendationRequest(userInfo, computeRequest, fertilizerList);
    }

    private UserInfo buildProfileInfo() {
        UserInfo userInfo = new UserInfo();
        try {
            ProfileInfo profileInfo = database.profileInfoDao().findOne();

            if (profileInfo != null) {
                String firstName = Strings.isEmptyOrWhitespace(profileInfo.getFirstName()) ? DEFAULT_USERNAME : profileInfo.getFirstName();
                String lastName = Strings.isEmptyOrWhitespace(profileInfo.getLastName()) ? DEFAULT_USERNAME : profileInfo.getLastName();
                fullNames = Strings.isEmptyOrWhitespace(profileInfo.getNames()) ? DEFAULT_USERNAME : profileInfo.getNames();
                gender = Strings.isEmptyOrWhitespace(profileInfo.getGender()) ? DEFAULT_UNAVAILABLE : profileInfo.getGender();
                farmName = Strings.isEmptyOrWhitespace(profileInfo.getFarmName()) ? DEFAULT_UNAVAILABLE : profileInfo.getFarmName();
                mobileNumber = Strings.isEmptyOrWhitespace(profileInfo.getFullMobileNumber()) ? DEFAULT_UNAVAILABLE : profileInfo.getFullMobileNumber();
                fullPhoneNumber = Strings.isEmptyOrWhitespace(profileInfo.getFullMobileNumber()) ? DEFAULT_UNAVAILABLE : profileInfo.getFullMobileNumber();
                mobileCountryCode = Strings.isEmptyOrWhitespace(profileInfo.getMobileCode()) ? DEFAULT_UNAVAILABLE : profileInfo.getMobileCode();
                emailAddress = Strings.isEmptyOrWhitespace(profileInfo.getEmail()) ? DEFAULT_UNAVAILABLE : profileInfo.getEmail();
                deviceToken = profileInfo.getDeviceToken();

                smsRequired = profileInfo.getSendSms();
                emailRequired = profileInfo.getSendEmail();

                userInfo.setDeviceToken(deviceToken);
                userInfo.setFirstName(firstName);
                userInfo.setLastName(lastName);
                userInfo.setUserName(fullNames);
                userInfo.setGender(gender);
                userInfo.setMobileCountryCode(mobileCountryCode);
                userInfo.setMobileNumber(mobileNumber);
                userInfo.setFullPhoneNumber(fullPhoneNumber);
                userInfo.setEmailAddress(emailAddress);
                userInfo.setFieldDescription(farmName);
                userInfo.setSendSms(smsRequired);
                userInfo.setSendEmail(emailRequired);
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        return userInfo;
    }

    private ComputeRequest buildMandatoryInfo() {
        ComputeRequest computeRequest = new ComputeRequest();
        MandatoryInfo mandatoryInfo = database.mandatoryInfoDao().findOne();
        LocationInfo locationInfo = database.locationInfoDao().findOne();
        ProfileInfo profileInfo = database.profileInfoDao().findOne();
        if (locationInfo != null) {
            computeRequest.setMapLat(locationInfo.getLatitude());
            computeRequest.setMapLong(locationInfo.getLongitude());
        }
        if (mandatoryInfo != null) {
            fieldArea = mandatoryInfo.getAreaSize();
            areaUnits = mandatoryInfo.getAreaUnit();
            computeRequest.setRiskAttitude(riskAtt);

            computeRequest.setFieldArea(fieldArea);
            computeRequest.setAreaUnits(areaUnits);
        }

        if (profileInfo != null) {
            countryCode = profileInfo.getCountryCode();
            computeRequest.setCurrency(profileInfo.getCurrency());
            computeRequest.setCountry(countryCode);
            computeRequest.setRiskAttitude(profileInfo.getRiskAtt());
        }
        return computeRequest;
    }

    private ComputeRequest buildRequestedRec(@NonNull ComputeRequest computeRequest) {
        //check for values we have to give recommendations for
        UseCases useCases = database.useCaseDao().findOne();
        if (useCases != null) {
            computeRequest.setInterCroppingMaizeRec(useCases.getCIM());
            computeRequest.setInterCroppingPotatoRec(useCases.getCIS());
            computeRequest.setUseCase(useCases.getName());

            computeRequest.setFertilizerRec(useCases.getFR());
            computeRequest.setPlantingPracticesRec(useCases.getBPP());
            computeRequest.setScheduledPlantingRec(useCases.getSPP());
            computeRequest.setScheduledHarvestRec(useCases.getSPH());
        }
        return computeRequest;
    }

    private ComputeRequest buildCurrentFieldYield(@NonNull ComputeRequest computeRequest) {
        //check for values we have to give recommendations for
        FieldYield fieldYield = database.fieldYieldDao().findOne();
        if (fieldYield != null) {
            currentFieldYield = (int) fieldYield.getYieldAmount();
        }
        computeRequest.setCurrentFieldYield(currentFieldYield);

        return computeRequest;
    }

    private ComputeRequest buildPlantingDates(@NonNull ComputeRequest computeRequest) {
        try {
            ScheduledDate sph = database.scheduleDateDao().findOne();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy");

            if (sph != null) {

                plantingDate = sph.getPlantingDate();
                plantingDateWindow = sph.getPlantingWindow();
                harvestDate = sph.getHarvestDate();
                harvestDateWindow = sph.getHarvestWindow();


                LocalDate PD = formatter.parseLocalDate(plantingDate);
                LocalDate HD = formatter.parseLocalDate(harvestDate);

                computeRequest.setPlantingDate(PD.toString("yyyy-MM-dd"));
                computeRequest.setPlantingDateWindow(plantingDateWindow);

                computeRequest.setHarvestDate(HD.toString("yyyy-MM-dd"));
                computeRequest.setHarvestDateWindow(harvestDateWindow);
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }

        return computeRequest;
    }

    private ComputeRequest buildInvestmentAmount(@NonNull ComputeRequest computeRequest) {
        InvestmentAmount investmentAmount = database.investmentAmountDao().findOne();
        if (investmentAmount != null) {
            maxInvestmentAmountLocal = investmentAmount.getInvestmentAmountLocal();
        }
        computeRequest.setMaxInvestment(maxInvestmentAmountLocal);
        return computeRequest;
    }

    private ComputeRequest buildCurrentPractice(@NonNull ComputeRequest computeRequest) {

        CurrentPractice currentPractice = database.currentPracticeDao().findOne();

        if (currentPractice != null) {
            performsPloughing = currentPractice.getPerformPloughing();
            performsHarrowing = currentPractice.getPerformHarrowing();
            performsRidging = currentPractice.getPerformRidging();

            methodHarrowing = Strings.isEmptyOrWhitespace(currentPractice.getHarrowingMethod()) ? DEFAULT_PRACTICE_METHOD : currentPractice.getHarrowingMethod();
            methodPloughing = Strings.isEmptyOrWhitespace(currentPractice.getPloughingMethod()) ? DEFAULT_PRACTICE_METHOD : currentPractice.getPloughingMethod();
            methodRidging = Strings.isEmptyOrWhitespace(currentPractice.getRidgingMethod()) ? DEFAULT_PRACTICE_METHOD : currentPractice.getRidgingMethod();
            methodWeeding = Strings.isEmptyOrWhitespace(currentPractice.getWeedControlTechnique()) ? DEFAULT_PRACTICE_METHOD : currentPractice.getWeedControlTechnique();
        }

        computeRequest.setPloughingDone(performsPloughing);
        computeRequest.setHarrowingDone(performsHarrowing);
        computeRequest.setRidgingDone(performsRidging);
        if (methodPloughing.equalsIgnoreCase("tractor")) {
            computeRequest.setTractorPlough(true);
        }
        if (methodHarrowing.equalsIgnoreCase("tractor")) {
            computeRequest.setTractorHarrow(true);
        }
        if (methodRidging.equalsIgnoreCase("tractor")) {
            computeRequest.setTractorRidger(true);
        }

        computeRequest.setMethodHarrowing(methodHarrowing);
        computeRequest.setMethodPloughing(methodPloughing);
        computeRequest.setMethodRidging(methodRidging);
        computeRequest.setMethodWeeding(methodWeeding);


        return computeRequest;
    }

    private ComputeRequest buildOperationCosts(@NonNull ComputeRequest computeRequest) {
        FieldOperationCost fieldOperationCost = database.fieldOperationCostDao().findOne();

        if (fieldOperationCost != null) {
            costTractorPlough = fieldOperationCost.getTractorPloughCost();
            costTractorHarrow = fieldOperationCost.getTractorHarrowCost();
            costTractorRidging = fieldOperationCost.getTractorRidgeCost();

            costManualPloughing = fieldOperationCost.getManualPloughCost();
            costManualHarrowing = fieldOperationCost.getManualHarrowCost();
            costManualRidging = fieldOperationCost.getManualRidgeCost();

            costWeedingOne = fieldOperationCost.getFirstWeedingOperationCost();
            costWeedingTwo = fieldOperationCost.getSecondWeedingOperationCost();
        }

        computeRequest.setCostLmoAreaBasis(costLmoAreaBasis);

        computeRequest.setCostTractorPloughing(costTractorPlough);
        computeRequest.setCostTractorHarrowing(costTractorHarrow);
        computeRequest.setCostTractorRidging(costTractorRidging);

        computeRequest.setCostManualPloughing(costManualPloughing);
        computeRequest.setCostManualHarrowing(costManualHarrowing);
        computeRequest.setCostManualRidging(costManualRidging);

        computeRequest.setCostWeedingOne(costWeedingOne);
        computeRequest.setCostWeedingTwo(costWeedingTwo);

        return computeRequest;
    }

    private ComputeRequest buildWeedManagement(@NonNull ComputeRequest computeRequest) {
        computeRequest.setFallowType(fallowType);
        computeRequest.setFallowGreen(fallowGreen);
        computeRequest.setFallowHeight(fallowHeight);
        computeRequest.setProblemWeeds(problemWeeds);

        return computeRequest;
    }

    private ComputeRequest buildMaizePerformance(@NonNull ComputeRequest computeRequest) {
        MaizePerformance maizePerformance = database.maizePerformanceDao().findOne();
        if (maizePerformance != null) {
            currentMaizePerformance = Strings.isEmptyOrWhitespace(maizePerformance.getPerformanceValue()) ? DEFAULT_MAIZE_PERFORMANCE_VALUE : maizePerformance.getPerformanceValue();
        }
        computeRequest.setCurrentMaizePerformance(currentMaizePerformance);

        return computeRequest;
    }

    private ComputeRequest buildCassavaMarketOutlet(@NonNull ComputeRequest computeRequest) {
        CassavaMarket cassavaMarket = database.cassavaMarketDao().findOne();

        if (cassavaMarket != null) {
            sellToStarchFactory = cassavaMarket.isStarchFactoryRequired();
            if (sellToStarchFactory) {
                starchFactoryName = cassavaMarket.getStarchFactory();
            }

            cassavaProduceType = cassavaMarket.getProduceType();
            int uw = cassavaMarket.getUnitWeight();
            cassavaUnitWeight = uw <= 0 ? DEFAULT_UNIT_WEIGHT : uw;

            cassavaUnitPrice = cassavaMarket.getUnitPrice();
        }
        computeRequest.setStarchFactoryName(starchFactoryName);
        computeRequest.setSellToStarchFactory(sellToStarchFactory);

        computeRequest.setCassavaProduceType(cassavaProduceType);
        computeRequest.setCassavaUnitPrice(cassavaUnitPrice);
        computeRequest.setCassavaUnitWeight(cassavaUnitWeight);

        computeRequest.setCassUPM1(cassavaUpmOne);
        computeRequest.setCassUPM2(cassavaUpmTwo);
        computeRequest.setCassUPP1(cassavaUppOne);
        computeRequest.setCassUPP2(cassavaUppTwo);


        return computeRequest;
    }

    private ComputeRequest buildMaizeMarketOutlet(ComputeRequest computeRequest) {
        MaizeMarket maizeMarket = database.maizeMarketDao().findOne();
        if (maizeMarket != null) {
            maizeProdType = maizeMarket.getProduceType();
            maizeUnitWeight = maizeMarket.getUnitWeight();
            maizeUnitPrice = maizeMarket.getExactPrice();

            int uw = maizeMarket.getUnitWeight();
            maizeUnitWeight = uw <= 0 ? DEFAULT_UNIT_WEIGHT : uw;

        }
        computeRequest.setMaizeProduceType(maizeProdType);
        computeRequest.setMaizeUnitWeight(maizeUnitWeight);
        computeRequest.setMaizeUnitPrice(maizeUnitPrice);

        return computeRequest;
    }

    private ComputeRequest buildSweetPotatoMarketOutlet(ComputeRequest computeRequest) {
        PotatoMarket potatoMarket = database.potatoMarketDao().findOne();
        if (potatoMarket != null) {
            sweetPotatoProdType = potatoMarket.getProduceType();
            sweetPotatoUnitWeight = potatoMarket.getUnitWeight();
            sweetPotatoUnitPrice = potatoMarket.getUnitPrice();

            int uw = potatoMarket.getUnitWeight();
            sweetPotatoUnitWeight = uw <= 0 ? DEFAULT_UNIT_WEIGHT : uw;
        }

        computeRequest.setSweetPotatoProduceType(sweetPotatoProdType);
        computeRequest.setSweetPotatoUnitWeight(sweetPotatoUnitWeight);
        computeRequest.setSweetPotatoUnitPrice(sweetPotatoUnitPrice);
        return computeRequest;
    }
}
