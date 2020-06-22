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

import java.util.List;

import io.realm.Realm;

@SuppressWarnings("UnnecessaryLocalVariable")
public class RealmProcessor {
    private Realm realm;

    public RealmProcessor() {
        realm = Realm.getDefaultInstance();
    }

    public RealmProcessor(Realm myRealm) {
        this.realm = myRealm;
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
        List<PotatoPrice> potatoPrices = realm.where(PotatoPrice.class)
                .equalTo("country", countryCode)
                .findAll();

        return potatoPrices;
    }

    public List<InterCropFertilizer> getAllInterCropFertilizersByCountry(String countryCode) {
        List<InterCropFertilizer> interCropFertilizers = realm.where(InterCropFertilizer.class)
                .equalTo("countryCode", countryCode)
                .findAll();

        return interCropFertilizers;
    }

    public List<Fertilizer> getAvailableFertilizersByCountry(String countryCode) {
        List<Fertilizer> fertilizers = realm.where(Fertilizer.class)
                .equalTo("countryCode", countryCode)
                .findAll();

        return fertilizers;
    }

    public RecAdvice getRecAdvice() {
        RecAdvice recAdvice = realm.where(RecAdvice.class)
                .findFirst();

        return recAdvice;
    }

    public CurrentFieldYield getCurrentFieldYield() {
        CurrentFieldYield currentFieldYield = realm.where(CurrentFieldYield.class)
                .findFirst();

        return currentFieldYield;
    }

    public InvestmentAmount getInvestmentAmount() {
        InvestmentAmount investmentAmount = realm.where(InvestmentAmount.class)
                .findFirst();

        return investmentAmount;
    }

    public OperationCosts getOperationCosts() {

        OperationCosts operationCosts = realm.where(OperationCosts.class)
                .findFirst();

        return operationCosts;
    }

    public MaizePerformance getMaizePerformance() {
        MaizePerformance maizePerformance = realm.where(MaizePerformance.class)
                .findFirst();

        return maizePerformance;
    }

    public CassavaMarketOutlet getCassavaMarketOutlet() {
        CassavaMarketOutlet cassavaMarketOutlet = realm.where(CassavaMarketOutlet.class)
                .findFirst();

        return cassavaMarketOutlet;
    }

    public MaizeMarketOutlet getMaizeMarketOutlet() {
        MaizeMarketOutlet maizeMarketOutlet = realm.where(MaizeMarketOutlet.class)
                .findFirst();

        return maizeMarketOutlet;
    }

    public PotatoMarketOutlet getPotatoMarketOutlet() {
        PotatoMarketOutlet potatoMarketOutlet = realm.where(PotatoMarketOutlet.class)
                .findFirst();

        return potatoMarketOutlet;
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
        Fertilizer fertilizer = realm.where(Fertilizer.class)
                .equalTo("fertilizerType", fertilizerType)
                .and()
                .equalTo("countryCode", countryCode)
                .findFirst();

        return fertilizer;
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
        InterCropFertilizer interCropFertilizer = realm.where(InterCropFertilizer.class)
                .equalTo("fertilizerType", fertilizerType)
                .and()
                .equalTo("countryCode", countryCode)
                .and()
                .equalTo("useCase", useCase.name())
                .and()
                .equalTo("selected", true)
                .findFirst();

        return interCropFertilizer;
    }

    @Deprecated
    public List<InterCropFertilizer> getAvailableInterCropFertilizersByCountryUseCase(String countryCode, EnumUseCase useCase) {
        List<InterCropFertilizer> interCropFertilizers = realm.where(InterCropFertilizer.class)
                .equalTo("countryCode", countryCode)
                .and()
                .equalTo("useCase", useCase.name())
                .findAll();

        return interCropFertilizers;
    }


    @Deprecated
    public List<InterCropFertilizer> getSelectedInterCropFertilizers(String countryCode, EnumUseCase useCase) {
        List<InterCropFertilizer> interCropFertilizers = realm.where(InterCropFertilizer.class)
                .equalTo("countryCode", countryCode)
                .and()
                .equalTo("useCase", useCase.name())
                .and()
                .equalTo("selected", true)
                .findAll();

        return interCropFertilizers;
    }
}
