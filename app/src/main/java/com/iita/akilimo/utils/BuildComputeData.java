package com.iita.akilimo.utils;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.iita.akilimo.entities.ComputeRequest;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.MarketOutlet;
import com.iita.akilimo.entities.PlantingHarvestDates;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.entities.TillageOperations;
import com.iita.akilimo.models.Fertilizer;
import com.iita.akilimo.models.InvestmentAmount;
import com.iita.akilimo.models.MaizePerformance;
import com.iita.akilimo.rest.request.RecommendationRequest;
import com.iita.akilimo.utils.enums.EnumProduceType;
import com.iita.akilimo.utils.enums.EnumUnitOfSale;
import com.iita.akilimo.utils.enums.EnumUnitPrice;
import com.iita.akilimo.utils.objectbox.ObjectBoxEntityProcessor;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import javax.annotation.Nonnull;

@SuppressWarnings("FieldCanBeLocal")
public class BuildComputeData {

    private static final String LOG_TAG = BuildComputeData.class.getSimpleName();

    private static final String DEFAULT_CASSAVA_PD = "roots";
    private static final String DEFAULT_MAIZE_PD = "fresh_cob";
    private static final String DEFAULT_SWEET_POTATO_PD = "tubers";
    private static final String DEFAULT_UNAVAILABLE = "NA";
    private static final String DEFAULT_FALLOW_TYPE = "none";
    private static final String DEFAULT_MAIZE_PERFORMANCE_VALUE = "3";
    private static final String DEFAULT_PLOUGHING_METHOD = "manual";

    private static final int DEFAULT_FIELD_YIELD = 11;
    private static final int DEFAULT_UNAVAILABLE_INT = 0;
    private static final String DEFAULT_LMNO_BASIS = "areaUnit";
    private static final String DEFAULT_USERNAME = "Akilimo";
    private static final String DEFAULT_FIELD_DESC = "Field description";


    private boolean smsRequired = false;
    private boolean emailRequired = false;
    private String emailAddress = DEFAULT_UNAVAILABLE;
    private String mobileNumber = DEFAULT_UNAVAILABLE;
    private String fullPhoneNumber = DEFAULT_UNAVAILABLE;
    private String mobileCountryCode = DEFAULT_UNAVAILABLE;


    private int cassavaUnitWeight = DEFAULT_UNAVAILABLE_INT;
    private double unitPriceLocal;
    private double maxInvestmentAmountLocal = 0.0;
    private String unitOfSale;
    private String areaUnits;
    private double fieldArea = 0.0;

    private String interCroppingType = DEFAULT_UNAVAILABLE;
    private boolean interCroppingRec = false;
    private boolean fertilizerRec = false;
    private boolean plantingPracticesRec = false;
    private boolean scheduledPlantingRec = false;
    private boolean scheduledHarvestRec = false;
    private String harvestDate;
    private String plantingDate;

    private int plantingDateWindow = 0;
    private int harvestDateWindow = 0;
    private int currentFieldYield = DEFAULT_FIELD_YIELD;

    private String fallowType = DEFAULT_FALLOW_TYPE;
    private boolean fallowGreen = false;
    private int fallowHeight = 100;
    private boolean problemWeeds = false;

    private boolean hasTractorPlough;
    private boolean hasTractorHarrow;
    private boolean hasTractorRidger;


    private String costLmoAreaBasis = DEFAULT_LMNO_BASIS;
    private String costTractorPlough = DEFAULT_UNAVAILABLE;
    private String costTractorHarrow = DEFAULT_UNAVAILABLE;

    private String costTractorRidging = DEFAULT_UNAVAILABLE;
    private String costManualPloughing = DEFAULT_UNAVAILABLE;
    private String costManualHarrowing = DEFAULT_UNAVAILABLE;
    private String costManualRidging = DEFAULT_UNAVAILABLE;

    String costWeedingOne = DEFAULT_UNAVAILABLE;
    String costWeedingTwo = DEFAULT_UNAVAILABLE;

    boolean performsPloughing;
    boolean performsHarrowing;
    boolean performsRidging;
    boolean sellToStarchFactory;

    String methodHarrowing = DEFAULT_PLOUGHING_METHOD;
    String methodPloughing = DEFAULT_PLOUGHING_METHOD;
    String methodRidging = DEFAULT_PLOUGHING_METHOD;

    double cassavaUnitPrice = 0.0;

    String maizeProdType = DEFAULT_MAIZE_PD;
    String maizeUnitWeight = DEFAULT_UNAVAILABLE;
    String maizeUnitPrice = DEFAULT_UNAVAILABLE;
    String currentMaizePerformance = DEFAULT_MAIZE_PERFORMANCE_VALUE;

    String sweetPotatoProdType = DEFAULT_SWEET_POTATO_PD;
    String sweetPotatoUnitWeight = DEFAULT_UNAVAILABLE;
    String sweetPotatoUnitPrice = DEFAULT_UNAVAILABLE;

    String userName = DEFAULT_USERNAME;
    String fieldDesc = DEFAULT_FIELD_DESC;
    int riskAtt = DEFAULT_UNAVAILABLE_INT;

    String cassavaUpmOne = DEFAULT_UNAVAILABLE;
    String cassavaUpmTwo = DEFAULT_UNAVAILABLE;
    String cassavaUppOne = DEFAULT_UNAVAILABLE;
    String cassavaUppTwo = DEFAULT_UNAVAILABLE;

    String cassavaProduceType = DEFAULT_CASSAVA_PD;

    String starchFactoryName = DEFAULT_UNAVAILABLE;

    private ObjectBoxEntityProcessor objectBoxEntityProcessor;

    public BuildComputeData(@NonNull Activity context) {
        objectBoxEntityProcessor = ObjectBoxEntityProcessor.getInstance(context);
    }

    public RecommendationRequest buildRecommendationReq() {


        ComputeRequest computeRequest = buildMandatoryInfo();
        computeRequest = buildRequestedRec(computeRequest);

        computeRequest = buildPlantingDates(computeRequest);

        computeRequest = buildInvestmentAmount(computeRequest);

        computeRequest = buildPlantingPractices(computeRequest);

        computeRequest = buildOperationCosts(computeRequest);

        computeRequest = buildWeedManagement(computeRequest);

        computeRequest = buildMaizePerformance(computeRequest);

        computeRequest = buildMarketOutlet(computeRequest);


        List<Fertilizer> fertilizerList = objectBoxEntityProcessor.getSelectedFertilizers(computeRequest.getCountry());


        return new RecommendationRequest(computeRequest, fertilizerList);
    }

    private ComputeRequest buildMandatoryInfo() {
        ComputeRequest computeRequest = new ComputeRequest();
        MandatoryInfo mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();

        fieldArea = mandatoryInfo.getAreaSize();
        areaUnits = mandatoryInfo.getAreaUnitsEnum().unitString();

        computeRequest.setSendSms(smsRequired);
        computeRequest.setSendEmail(emailRequired);
        computeRequest.setMobileCountryCode(mobileCountryCode);
        computeRequest.setMobileNumber(mobileNumber);
        computeRequest.setFullPhoneNumber(fullPhoneNumber);
        computeRequest.setUserName(userName);
        computeRequest.setUserEmail(emailAddress);
        computeRequest.setRiskAttitude(riskAtt);

        computeRequest.setCurrency(mandatoryInfo.getCurrency());
        computeRequest.setCountry(mandatoryInfo.getCountryCode());
        computeRequest.setMapLat(mandatoryInfo.getLatitude());
        computeRequest.setMapLong(mandatoryInfo.getLongitude());
        computeRequest.setFieldArea(fieldArea);
        computeRequest.setAreaUnits(areaUnits);
        computeRequest.setFieldDescription(fieldDesc);

        return computeRequest;
    }

    private ComputeRequest buildRequestedRec(@Nonnull ComputeRequest computeRequest) {
        //check for values we have to give recommendations for
        RecAdvice recAdvice = objectBoxEntityProcessor.getRecAdvice();

        computeRequest.setInterCroppingRec(recAdvice.isIC());
        computeRequest.setFertilizerRec(recAdvice.isFR());
        computeRequest.setPlantingPracticesRec(recAdvice.isBPP());
        computeRequest.setScheduledPlantingRec(recAdvice.isSPP());
        computeRequest.setScheduledHarvestRec(recAdvice.isSPH());

        return computeRequest;
    }

    private ComputeRequest buildPlantingDates(@Nonnull ComputeRequest computeRequest) {
        PlantingHarvestDates sph = objectBoxEntityProcessor.getPlantingHarvestDates();
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
            sph.setHarvestWindow(harvestDateWindow);
        }

        return computeRequest;
    }

    private ComputeRequest buildInvestmentAmount(@Nonnull ComputeRequest computeRequest) {
        InvestmentAmount inv = objectBoxEntityProcessor.getInvestmentAmount();
        if (inv != null) {
            maxInvestmentAmountLocal = inv.getInvestmentAmountLocal();
        }
        computeRequest.setMaxInvestment(maxInvestmentAmountLocal);
        return computeRequest;
    }

    private ComputeRequest buildPlantingPractices(@Nonnull ComputeRequest computeRequest) {

        TillageOperations tillageOperations = objectBoxEntityProcessor.getTillageOperation();
        if (tillageOperations != null) {
            boolean hasTractor = tillageOperations.getTractorAvailable();
            hasTractorHarrow = tillageOperations.getTractorHarrow();
            hasTractorRidger = tillageOperations.getTractorRidger();
            hasTractorPlough = tillageOperations.getTractorPlough();
            performsPloughing = hasTractorPlough;
            performsHarrowing = hasTractorHarrow;
            performsRidging = hasTractorRidger;
        }


        computeRequest.setPloughingDone(performsPloughing);
        computeRequest.setHarrowingDone(performsHarrowing);
        computeRequest.setRidgingDone(performsRidging);
        computeRequest.setMethodHarrowing(methodHarrowing);
        computeRequest.setMethodPloughing(methodPloughing);
        computeRequest.setMethodRidging(methodRidging);


        return computeRequest;
    }

    private ComputeRequest buildOperationCosts(@Nonnull ComputeRequest computeRequest) {
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

    private ComputeRequest buildWeedManagement(@Nonnull ComputeRequest computeRequest) {
        computeRequest.setFallowType(fallowType);
        computeRequest.setFallowGreen(fallowGreen);
        computeRequest.setFallowHeight(fallowHeight);
        computeRequest.setProblemWeeds(problemWeeds);
        return computeRequest;
    }

    private ComputeRequest buildMaizePerformance(@Nonnull ComputeRequest computeRequest) {
        MaizePerformance maizePerformance = objectBoxEntityProcessor.getMaizePerformance();
        if (maizePerformance != null) {
            currentMaizePerformance = maizePerformance.getPerformanceValue();
        }
        computeRequest.setCurrentMaizePerformance(currentMaizePerformance);
        return computeRequest;
    }

    private ComputeRequest buildMarketOutlet(@Nonnull ComputeRequest computeRequest) {
        MarketOutlet marketOutlet = objectBoxEntityProcessor.getMarketOutlet();

        String currency = computeRequest.getCurrency();
        if (marketOutlet != null) {
            sellToStarchFactory = marketOutlet.isStarchFactoryRequired();
            if (sellToStarchFactory) {
                starchFactoryName = marketOutlet.getStarchFactory();
            }

            EnumUnitPrice up = marketOutlet.getEnumUnitPrice();
            unitPriceLocal = up.convertToLocal(currency) <= 0 ? marketOutlet.getExactPrice() : up.convertToLocal(currency);

            EnumProduceType produce = marketOutlet.getEnumProduceType();
            cassavaProduceType = produce.produce();

            EnumUnitOfSale uos = marketOutlet.getEnumUnitOfSale();
            cassavaUnitWeight = uos.unitWeight();
            cassavaUnitPrice = unitPriceLocal;
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


        computeRequest.setMaizeProduceType(maizeProdType);
        computeRequest.setMaizeUnitWeight(maizeUnitWeight);
        computeRequest.setMaizeUnitPrice(maizeUnitPrice);

        computeRequest.setSweetPotatoProduceType(sweetPotatoProdType);
        computeRequest.setSweetPotatoUnitWeight(sweetPotatoUnitWeight);
        computeRequest.setSweetPotatoUnitPrice(sweetPotatoUnitPrice);

        return computeRequest;
    }
}