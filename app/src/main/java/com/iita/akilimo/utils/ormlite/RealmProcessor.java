package com.iita.akilimo.utils.ormlite;

import com.iita.akilimo.entities.CassavaMarketOutlet;
import com.iita.akilimo.entities.CurrentFieldYield;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.InvestmentAmount;
import com.iita.akilimo.entities.LocationInfo;
import com.iita.akilimo.entities.MaizeMarketOutlet;
import com.iita.akilimo.entities.MaizePerformance;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.OperationCosts;
import com.iita.akilimo.entities.PlantingHarvestDates;
import com.iita.akilimo.entities.PotatoMarketOutlet;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.models.CassavaPrice;
import com.iita.akilimo.models.Fertilizer;
import com.iita.akilimo.models.FertilizerPrices;
import com.iita.akilimo.models.InterCropFertilizer;
import com.iita.akilimo.models.MaizePrice;
import com.iita.akilimo.models.PotatoPrice;
import com.iita.akilimo.models.StarchFactory;
import com.iita.akilimo.utils.enums.EnumUseCase;

import java.util.List;

import io.realm.Realm;

@SuppressWarnings("UnnecessaryLocalVariable")
public class RealmProcessor {
    private Realm realm;

    public RealmProcessor() {

    }

    public ProfileInfo getProfileInfo() {

        ProfileInfo profileInfo = null;


        return profileInfo;
    }

    public MandatoryInfo getMandatoryInfo() {

        MandatoryInfo mandatoryInfo = null;


        return mandatoryInfo;
    }

    public CurrentPractice getCurrentPractice() {
        CurrentPractice mandatoryInfo = null;

        return mandatoryInfo;
    }

    public PlantingHarvestDates getPlantingHarvestDates() {

        PlantingHarvestDates mandatoryInfo = null;


        return mandatoryInfo;
    }

    public LocationInfo getLocationInfo() {

        LocationInfo mandatoryInfo = null;

        return mandatoryInfo;
    }


    public List<CassavaPrice> getCassavaPrices(String countryCode) {
        List<CassavaPrice> cassavaPrices = null;

        return cassavaPrices;
    }

    public List<FertilizerPrices> getFertilizerPrices(String countryCode) {
        List<FertilizerPrices> fertilizerPrices = null;

        return fertilizerPrices;
    }

    public List<MaizePrice> getMaizePrices(String countryCode) {
        List<MaizePrice> maizePrices = null;

        return maizePrices;
    }

    public List<PotatoPrice> getPotatoPrices(String countryCode) {
        List<PotatoPrice> potatoPrices = null;

        return potatoPrices;
    }

    public List<InterCropFertilizer> getAllInterCropFertilizersByCountry(String countryCode) {
        List<InterCropFertilizer> interCropFertilizers = null;

        return interCropFertilizers;
    }

    public List<Fertilizer> getAvailableFertilizersByCountry(String countryCode) {
        List<Fertilizer> fertilizers = null;

        return fertilizers;
    }

    public RecAdvice getRecAdvice() {
        RecAdvice recAdvice = null;

        return recAdvice;
    }

    public CurrentFieldYield getCurrentFieldYield() {
        CurrentFieldYield currentFieldYield = null;

        return currentFieldYield;
    }

    public InvestmentAmount getInvestmentAmount() {
        InvestmentAmount investmentAmount = null;

        return investmentAmount;
    }

    public OperationCosts getOperationCosts() {

        OperationCosts operationCosts = null;

        return operationCosts;
    }

    public MaizePerformance getMaizePerformance() {
        MaizePerformance maizePerformance = null;
        return maizePerformance;
    }

    public CassavaMarketOutlet getCassavaMarketOutlet() {
        CassavaMarketOutlet cassavaMarketOutlet = null;
        return cassavaMarketOutlet;
    }

    public MaizeMarketOutlet getMaizeMarketOutlet() {
        MaizeMarketOutlet maizeMarketOutlet = null;

        return maizeMarketOutlet;
    }

    public PotatoMarketOutlet getPotatoMarketOutlet() {
        PotatoMarketOutlet potatoMarketOutlet = null;

        return potatoMarketOutlet;
    }

    public StarchFactory getSelectedStarchFactoryByTag(String factoryNameCountry) {
        StarchFactory starchFactory = null;

        return starchFactory;
    }

    public List<StarchFactory> getStarchFactories(String countryCode) {
        List<StarchFactory> starchFactories = null;

        return starchFactories;
    }

    public Fertilizer getSavedFertilizer(String fertilizerType, String countryCode) {
        Fertilizer fertilizer = null;

        return fertilizer;
    }

    public List<Fertilizer> getSelectedFertilizers(String countryCode) {
        List<Fertilizer> selectedFertilizers = null;

        return selectedFertilizers;
    }

    public InterCropFertilizer getSavedInterCropFertilizer(String fertilizerType, String countryCode, EnumUseCase useCase) {
        InterCropFertilizer interCropFertilizer = null;

        return interCropFertilizer;
    }

    @Deprecated
    public List<InterCropFertilizer> getAvailableInterCropFertilizersByCountryUseCase(String countryCode, EnumUseCase useCase) {
        List<InterCropFertilizer> interCropFertilizers = null;

        return interCropFertilizers;
    }


    @Deprecated
    public List<InterCropFertilizer> getSelectedInterCropFertilizers(String countryCode, EnumUseCase useCase) {
        List<InterCropFertilizer> interCropFertilizers = null;

        return interCropFertilizers;
    }
}
