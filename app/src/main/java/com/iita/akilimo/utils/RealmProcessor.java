package com.iita.akilimo.utils;

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

import java.util.Collection;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmProcessor {
    private Realm realm;

    public RealmProcessor() {
        realm = Realm.getDefaultInstance();
    }

    public ProfileInfo getProfileInfo() {

        ProfileInfo profileInfo = realm
                .where(ProfileInfo.class)
                .findFirst();


        return profileInfo;
    }

    public MandatoryInfo getMandatoryInfo() {

        MandatoryInfo mandatoryInfo = realm
                .where(MandatoryInfo.class)
                .findFirst();


        return mandatoryInfo;
    }

    public CurrentPractice getCurrentPractice() {
        CurrentPractice mandatoryInfo = realm
                .where(CurrentPractice.class)
                .findFirst();

        return mandatoryInfo;
    }

    public PlantingHarvestDates getPlantingHarvestDates() {

        PlantingHarvestDates mandatoryInfo = realm
                .where(PlantingHarvestDates.class)
                .findFirst();


        return mandatoryInfo;
    }

    public LocationInfo getLocationInfo() {

        LocationInfo mandatoryInfo = realm
                .where(LocationInfo.class)
                .findFirst();


        return mandatoryInfo;
    }

    public List<CassavaPrice> getCassavaPrices(String countryCode) {
        List<CassavaPrice> cassavaPrices = realm.where(CassavaPrice.class)
                .equalTo("country", countryCode)
                .findAll();

        return cassavaPrices;
    }

    public List<FertilizerPrices> getFertilizerPrices(String countryCode) {
        List<FertilizerPrices> fertilizerPrices = realm.where(FertilizerPrices.class)
                .equalTo("country", countryCode)
                .findAll();

        return fertilizerPrices;
    }

    public List<MaizePrice> getMaizePrices(String countryCode) {
        List<MaizePrice> maizePrices = realm.where(MaizePrice.class)
                .equalTo("country", countryCode)
                .findAll();

        return maizePrices;
    }

    public List<PotatoPrice> getPotatoPrices(String countryCode) {
        return null;
    }

    public List<InterCropFertilizer> getAllInterCropFertilizersByCountry(String countryCode) {
        return null;
    }

    public List<Fertilizer> getAvailableFertilizersByCountry(String countryCode) {
        List<Fertilizer> fertilizers = realm.where(Fertilizer.class)
                .equalTo("countryCode", countryCode)
                .findAll();

        return fertilizers;
    }

    public RecAdvice getRecAdvice() {
        return null;
    }

    public CurrentFieldYield getCurrentFieldYield() {
        CurrentFieldYield currentFieldYield = realm.where(CurrentFieldYield.class)
                .findFirst();

        return currentFieldYield;
    }

    public InvestmentAmount getInvestmentAmount() {
        return null;
    }

    public OperationCosts getOperationCosts() {
        return null;
    }

    public MaizePerformance getMaizePerformance() {
        return null;
    }

    public CassavaMarketOutlet getCassavaMarketOutlet() {
        return null;
    }

    public MaizeMarketOutlet getMaizeMarketOutlet() {
        return null;
    }

    public PotatoMarketOutlet getPotatoMarketOutlet() {
        return null;
    }

    public StarchFactory getSelectedStarchFactoryByTag(String factoryName) {
        StarchFactory starchFactory = realm.where(StarchFactory.class)
                .equalTo("factoryNameCountry", factoryName)
                .findFirst();

        return starchFactory;
    }

    public List<StarchFactory> getStarchFactories(String countryCode) {
        List<StarchFactory> starchFactories = realm.where(StarchFactory.class)
                .equalTo("countryCode", countryCode)
                .findAll();

        return starchFactories;
    }

    public Fertilizer getSavedFertilizer(String fertilizerType, String countryCode) {
        return null;
    }

    public List<Fertilizer> getSelectedFertilizers(String countryCode) {
        List<Fertilizer> selectedFertilizers = realm.where(Fertilizer.class)
                .equalTo("countryCode", countryCode)
                .and()
                .equalTo("selected", true)
                .findAll();

        return selectedFertilizers;
    }

    public InterCropFertilizer getSavedInterCropFertilizer(String fertilizerType, String countryCode, EnumUseCase useCase) {
        return null;
    }

    public List<InterCropFertilizer> getAvailableInterCropFertilizersByCountryUseCase(String countryCode, EnumUseCase useCase) {
        return null;
    }

    public List<InterCropFertilizer> getSelectedInterCropFertilizers(String countryCode, EnumUseCase useCase) {
        return null;
    }
}
