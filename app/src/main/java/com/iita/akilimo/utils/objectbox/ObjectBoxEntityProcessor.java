package com.iita.akilimo.utils.objectbox;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.Akilimo;
import com.iita.akilimo.entities.CassavaMarketOutlet;
import com.iita.akilimo.entities.CurrentFieldYield_;
import com.iita.akilimo.entities.CurrentPractice;
import com.iita.akilimo.entities.InvestmentAmount_;
import com.iita.akilimo.entities.LocationInfo;
import com.iita.akilimo.entities.MaizeMarketOutlet;
import com.iita.akilimo.entities.MaizePerformance;
import com.iita.akilimo.entities.MaizePerformance_;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.MandatoryInfo_;
import com.iita.akilimo.entities.OperationCosts;
import com.iita.akilimo.entities.PlantingHarvestDates;
import com.iita.akilimo.entities.PotatoMarketOutlet;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.entities.RecAdvice;
import com.iita.akilimo.entities.CurrentFieldYield;
import com.iita.akilimo.models.Fertilizer;
import com.iita.akilimo.models.FertilizerPrices;
import com.iita.akilimo.models.FertilizerPrices_;
import com.iita.akilimo.models.Fertilizer_;
import com.iita.akilimo.entities.InvestmentAmount;
import com.iita.akilimo.models.InterCropFertilizer;
import com.iita.akilimo.models.InterCropFertilizer_;
import com.iita.akilimo.models.StarchFactory;
import com.iita.akilimo.models.StarchFactory_;
import com.iita.akilimo.utils.enums.EnumUseCase;

import java.util.List;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.exception.UniqueViolationException;
import io.objectbox.query.QueryBuilder;

public class ObjectBoxEntityProcessor {
    private static final String LOG_TAG = ObjectBoxEntityProcessor.class.getSimpleName();
    private static ObjectBoxEntityProcessor instance = null;
    private BoxStore boxStore;

    private ObjectBoxEntityProcessor(Context context) {
        boxStore = ((Akilimo) context.getApplicationContext()).getBoxStore();
    }


    public static ObjectBoxEntityProcessor getInstance(Context context) {
        if (instance == null) {
            instance = new ObjectBoxEntityProcessor(context);
        }
        return instance;
    }

    public long saveProfileInfo(ProfileInfo profileInfo) {
        try {
            Box<ProfileInfo> box = boxStore.boxFor(ProfileInfo.class);
            return box.put(profileInfo);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving profile infor");
            Crashlytics.logException(ex);
        }
        return 0;
    }

    public ProfileInfo getProfileInfo() {
        Box<ProfileInfo> box = boxStore.boxFor(ProfileInfo.class);

        return box.query()
                .build()
                .findFirst();
    }

    public long saveLocationInfo(LocationInfo locationInfo) {
        Box<LocationInfo> box = boxStore.boxFor(LocationInfo.class);
        return box.put(locationInfo);
    }

    public LocationInfo getLocationInfo() {
        Box<LocationInfo> box = boxStore.boxFor(LocationInfo.class);
        return box.query()
                .build()
                .findFirst();
    }

    public long saveMandatoryInfo(MandatoryInfo mandatoryInfo) {
        Box<MandatoryInfo> box = boxStore.boxFor(MandatoryInfo.class);
        return box.put(mandatoryInfo);
    }

    public MandatoryInfo getMandatoryInfo() {
        Box<MandatoryInfo> box = boxStore.boxFor(MandatoryInfo.class);

        return box.query()
                .order(MandatoryInfo_.id, QueryBuilder.DESCENDING)
                .build()
                .findFirst();
    }


    public long savePlantingHarvestDates(PlantingHarvestDates plantingHarvestDates) {
        Box<PlantingHarvestDates> box = boxStore.boxFor(PlantingHarvestDates.class);
        return box.put(plantingHarvestDates);
    }

    /**
     * Fetch the planting and harvest dates
     *
     * @return return @PlantingHarvestDates object
     */
    public PlantingHarvestDates getPlantingHarvestDates() {
        Box<PlantingHarvestDates> box = boxStore.boxFor(PlantingHarvestDates.class);
        return box.query()
                .build()
                .findFirst();
    }


    public void saveInvestmentAmount(InvestmentAmount investment) {
        Box<InvestmentAmount> box = boxStore.boxFor(InvestmentAmount.class);
        box.put(investment);
    }

    public InvestmentAmount getInvestmentAmount() {
        Box<InvestmentAmount> box = boxStore.boxFor(InvestmentAmount.class);

        return box.query()
                .order(InvestmentAmount_.id, QueryBuilder.DESCENDING)
                .build()
                .findFirst();
    }


    public long saveCurrentFieldYield(CurrentFieldYield currentFieldYield) {
        Box<CurrentFieldYield> box = boxStore.boxFor(CurrentFieldYield.class);
        return box.put(currentFieldYield);
    }

    public CurrentFieldYield getCurrentFieldYield() {
        Box<CurrentFieldYield> box = boxStore.boxFor(CurrentFieldYield.class);

        return box.query()
                .order(CurrentFieldYield_.id, QueryBuilder.DESCENDING)
                .build()
                .findFirst();
    }

    public void saveFertilizerList(final List<Fertilizer> selectedFertilizers) {
        try {
            final Box<Fertilizer> box = boxStore.boxFor(Fertilizer.class);
            box.put(selectedFertilizers);
        } catch (UniqueViolationException ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
    }

    public long saveSelectedFertilizer(@NonNull final Fertilizer selectedFertilizer) {
        try {
            final Box<Fertilizer> box = boxStore.boxFor(Fertilizer.class);
            return box.put(selectedFertilizer);
        } catch (UniqueViolationException ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
        return 0;
    }


    public List<Fertilizer> getAvailableFertilizersByCountry(@NonNull String countryCode) {
        Box<Fertilizer> box = boxStore.boxFor(Fertilizer.class);
        QueryBuilder<Fertilizer> fertilizerTypeQueryBuilder = box.query();

        return fertilizerTypeQueryBuilder
                .equal(Fertilizer_.countryCode, countryCode)
                .order(Fertilizer_.type) //default is ASCENDING
                .build()
                .find();
    }

    public List<Fertilizer> getSelectedFertilizers(@NonNull String deviceIdentifier) {
        Box<Fertilizer> box = boxStore.boxFor(Fertilizer.class);
        QueryBuilder<Fertilizer> fertilizerTypeQueryBuilder = box.query();
        fertilizerTypeQueryBuilder.equal(Fertilizer_.selected, true);

        return fertilizerTypeQueryBuilder
                .equal(Fertilizer_.countryCode, deviceIdentifier)
                .order(Fertilizer_.type) //default is ASCENDING
                .build()
                .find();
    }

    public Fertilizer getSavedFertilizer(@NonNull String typeName, @NonNull String deviceIdentifier) {
        Box<Fertilizer> box = boxStore.boxFor(Fertilizer.class);

        return box.query()
                .equal(Fertilizer_.countryCode, deviceIdentifier)
                .equal(Fertilizer_.type, typeName)
                .build()
                .findFirst();
    }

    /* Begin saving for intercrop fertilizer */
    public void saveIntercropFertilizerList(final List<InterCropFertilizer> selectedFertilizers) {
        try {
            final Box<InterCropFertilizer> box = boxStore.boxFor(InterCropFertilizer.class);
            box.put(selectedFertilizers);
        } catch (UniqueViolationException ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
    }

    public long saveSelectedIntercropFertilizer(@NonNull final InterCropFertilizer selectedFertilizer) {
        try {
            final Box<InterCropFertilizer> box = boxStore.boxFor(InterCropFertilizer.class);
            return box.put(selectedFertilizer);
        } catch (UniqueViolationException ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
        return 0;
    }


    public List<InterCropFertilizer> getAllIntercropFertilizersByCountry(@NonNull String countryCode) {
        Box<InterCropFertilizer> box = boxStore.boxFor(InterCropFertilizer.class);
        QueryBuilder<InterCropFertilizer> fertilizerTypeQueryBuilder = box.query();

        return fertilizerTypeQueryBuilder
                .equal(InterCropFertilizer_.countryCode, countryCode)
                .build()
                .find();
    }

    public List<InterCropFertilizer> getAvailableIntercropFertilizersByCountryUseCase(@NonNull String countryCode, EnumUseCase useCase) {
        Box<InterCropFertilizer> box = boxStore.boxFor(InterCropFertilizer.class);
        QueryBuilder<InterCropFertilizer> fertilizerTypeQueryBuilder = box.query();

        return fertilizerTypeQueryBuilder
                .equal(InterCropFertilizer_.useCase, useCase.name())
                .equal(InterCropFertilizer_.countryCode, countryCode)
                .build()
                .find();
    }

    public List<InterCropFertilizer> getSelectedIntercropFertilizers(@NonNull String deviceIdentifier, EnumUseCase useCase) {
        Box<InterCropFertilizer> box = boxStore.boxFor(InterCropFertilizer.class);

        QueryBuilder<InterCropFertilizer> fertilizerTypeQueryBuilder = box.query();
        fertilizerTypeQueryBuilder.equal(InterCropFertilizer_.selected, true);
        fertilizerTypeQueryBuilder.equal(InterCropFertilizer_.useCase, useCase.name());

        return fertilizerTypeQueryBuilder
                .equal(InterCropFertilizer_.countryCode, deviceIdentifier)
                .order(InterCropFertilizer_.type) //default is ASCENDING
                .build()
                .find();
    }

    public Fertilizer getSavedInterCropFertilizer(@NonNull String typeName, @NonNull String countryCode, EnumUseCase useCase) {
        Box<InterCropFertilizer> box = boxStore.boxFor(InterCropFertilizer.class);

        return box.query()
                .equal(InterCropFertilizer_.useCase, useCase.name())
                .equal(InterCropFertilizer_.countryCode, countryCode)
                .equal(InterCropFertilizer_.type, typeName)
                .build()
                .findFirst();
    }
    /* end saving of intercrop fertilizer */

    public long saveMaizePerformanceData(@NonNull MaizePerformance maizePerformanceModel) {
        Box<MaizePerformance> box = boxStore.boxFor(MaizePerformance.class);
        box.removeAll(); //clear first
        return box.put(maizePerformanceModel);
    }

    public MaizePerformance getMaizePerformance() {
        Box<MaizePerformance> box = boxStore.boxFor(MaizePerformance.class);

        return box.query()
                .order(MaizePerformance_.id, QueryBuilder.DESCENDING)
                .build()
                .findFirst();
    }


    public StarchFactory getSelectedStarchFactoryByTag(String factoryNameCountry) {
        Box<StarchFactory> box = boxStore.boxFor(StarchFactory.class);
        return box.query()
                .equal(StarchFactory_.factoryNameCountry, factoryNameCountry)
                .build()
                .findFirst();
    }

    public void saveStarchFactories(@NonNull List<StarchFactory> starchFactoryList) {
        try {
            Box<StarchFactory> box = boxStore.boxFor(StarchFactory.class);
            box.put(starchFactoryList);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving starch factories");
            Crashlytics.logException(ex);
        }
    }

    public List<StarchFactory> getStarchFactories(@NonNull String countryCode) {
        Box<StarchFactory> box = boxStore.boxFor(StarchFactory.class);
        return box.query()
                .equal(StarchFactory_.countryCode, countryCode)
                .build()
                .find();
    }

    public void saveFertilizerPrices(@NonNull List<FertilizerPrices> fertilizerPricesList) {
        try {
            Box<FertilizerPrices> box = boxStore.boxFor(FertilizerPrices.class);
            box.put(fertilizerPricesList);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Error occurred saving fertilizer prices");
            Crashlytics.logException(ex);
        }
    }


    public List<FertilizerPrices> getFertilizerPrices(@NonNull String countryCode) {
        Box<FertilizerPrices> box = boxStore.boxFor(FertilizerPrices.class);

        return box.query()
                .equal(FertilizerPrices_.country, countryCode)
                .build()
                .find();
    }

    public long saveMarketOutlet(@NonNull CassavaMarketOutlet cassavaMarketOutlet) {
        try {
            Box<CassavaMarketOutlet> box = boxStore.boxFor(CassavaMarketOutlet.class);
            return box.put(cassavaMarketOutlet);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving market outlet");
            Crashlytics.logException(ex);
        }
        return 0;
    }

    public CassavaMarketOutlet getCassavaMarketOutlet() {
        Box<CassavaMarketOutlet> box = boxStore.boxFor(CassavaMarketOutlet.class);

        return box.query()
                .build()
                .findFirst();
    }

    public void saveRecAdvice(RecAdvice recAdvice) {
        try {
            Box<RecAdvice> box = boxStore.boxFor(RecAdvice.class);
            box.put(recAdvice);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving recommendation advice");
            Crashlytics.logException(ex);
        }
    }

    public RecAdvice getRecAdvice() {
        Box<RecAdvice> box = boxStore.boxFor(RecAdvice.class);

        return box.query()
                .build()
                .findFirst();
    }

    public void saveCurrentPractice(CurrentPractice currentPractice) {
        try {
            Box<CurrentPractice> box = boxStore.boxFor(CurrentPractice.class);
            box.put(currentPractice);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving CurrentPractice");
            Crashlytics.logException(ex);
        }
    }

    public CurrentPractice getCurrentPractice() {
        Box<CurrentPractice> box = boxStore.boxFor(CurrentPractice.class);

        return box.query()
                .build()
                .findFirst();
    }

    public void saveOperationCosts(OperationCosts operationCosts) {
        try {
            Box<OperationCosts> box = boxStore.boxFor(OperationCosts.class);
            box.put(operationCosts);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving OperationCosts");
            Crashlytics.logException(ex);
        }
    }

    public OperationCosts getOperationCosts() {
        Box<OperationCosts> box = boxStore.boxFor(OperationCosts.class);

        return box.query()
                .build()
                .findFirst();
    }

    public long saveMaizeMarketOutlet(MaizeMarketOutlet maizeMarketOutlet) {
        long id = 0;
        try {
            Box<MaizeMarketOutlet> box = boxStore.boxFor(MaizeMarketOutlet.class);
            id = box.put(maizeMarketOutlet);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving MaizeMarketOutlet");
            Crashlytics.logException(ex);
        }

        return id;
    }

    public MaizeMarketOutlet getMaizeMarketOutlet() {
        Box<MaizeMarketOutlet> box = boxStore.boxFor(MaizeMarketOutlet.class);

        return box.query()
                .build()
                .findFirst();
    }

    public long savePotatoMarketOutlet(PotatoMarketOutlet potatoMarketOutlet) {
        long id = 0;
        try {
            Box<PotatoMarketOutlet> box = boxStore.boxFor(PotatoMarketOutlet.class);
            id = box.put(potatoMarketOutlet);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving PotatoMarketOutlet");
            Crashlytics.logException(ex);
        }

        return id;
    }

    public PotatoMarketOutlet getPotatoMarketOutlet() {
        Box<PotatoMarketOutlet> box = boxStore.boxFor(PotatoMarketOutlet.class);

        return box.query()
                .build()
                .findFirst();
    }
}
