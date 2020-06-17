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

public class RealmProcessor {
    Realm realm;

    public RealmProcessor() {
        realm = Realm.getDefaultInstance();
    }

    public void saveProfileInfo(ProfileInfo profileInfo) {
        realm.beginTransaction();
        realm.copyToRealm(profileInfo);
        realm.commitTransaction();
    }

    public void updateProfileInfo() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

            }
        });
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
        return null;
    }

    public List<FertilizerPrices> getFertilizerPrices(String countryCode) {
        return null;
    }

    public List<MaizePrice> getMaizePrices(String countryCode) {
        return null;
    }

    public List<PotatoPrice> getPotatoPrices(String countryCode) {
        return null;
    }

    public List<InterCropFertilizer> getAllInterCropFertilizersByCountry(String countryCode) {
        return null;
    }

    public List<Fertilizer> getAvailableFertilizersByCountry(String countryCode) {
        return null;
    }

    public RecAdvice getRecAdvice() {
        return null;
    }

    public CurrentFieldYield getCurrentFieldYield() {
        return null;
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

    public StarchFactory getSelectedStarchFactoryByTag(String itemTagIndex) {
        return null;
    }

    public List<StarchFactory> getStarchFactories(String countryCode) {
        return null;
    }

    public Fertilizer getSavedFertilizer(String fertilizerType, String countryCode) {
        return null;
    }

    public List<Fertilizer> getSelectedFertilizers(String countryCode) {
        return null;
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
