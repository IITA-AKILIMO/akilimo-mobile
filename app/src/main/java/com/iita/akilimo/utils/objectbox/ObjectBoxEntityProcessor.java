package com.iita.akilimo.utils.objectbox;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
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

@Deprecated
public class ObjectBoxEntityProcessor {
    private static final String LOG_TAG = ObjectBoxEntityProcessor.class.getSimpleName();
    private static ObjectBoxEntityProcessor instance = null;


    private ObjectBoxEntityProcessor(Context context) {

    }


    public static ObjectBoxEntityProcessor getInstance(Context context) {
        if (instance == null) {
            instance = new ObjectBoxEntityProcessor(context);
        }
        return instance;
    }

    public long saveProfileInfo(ProfileInfo profileInfo) {
        return 0;
    }

    public ProfileInfo getProfileInfo() {
        return null;
    }

    public long saveLocationInfo(LocationInfo locationInfo) {
        return 0;
    }

    public LocationInfo getLocationInfo() {
        return null;
    }

    public long saveMandatoryInfo(MandatoryInfo mandatoryInfo) {
        return 0;
    }

    public MandatoryInfo getMandatoryInfo() {
        return null;
    }


    public long savePlantingHarvestDates(PlantingHarvestDates plantingHarvestDates) {
        return 0;
    }

    /**
     * Fetch the planting and harvest dates
     *
     * @return return @PlantingHarvestDates object
     */
    public PlantingHarvestDates getPlantingHarvestDates() {
        return null;
    }


    public void saveInvestmentAmount(InvestmentAmount investment) {

    }

    public InvestmentAmount getInvestmentAmount() {
        return null;
    }


    public long saveCurrentFieldYield(CurrentFieldYield currentFieldYield) {
        return 0;
    }

    public CurrentFieldYield getCurrentFieldYield() {
        return null;
    }

    public void saveFertilizerList(final List<Fertilizer> selectedFertilizers) {
        try {
            for (Fertilizer selectedFertilizer : selectedFertilizers) {
                saveSelectedFertilizer(selectedFertilizer);
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
    }

    public long saveSelectedFertilizer(@NonNull final Fertilizer selectedFertilizer) {
        try {
            selectedFertilizer.save();
            return 1;
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
        return 0;
    }


    public List<Fertilizer> getAvailableFertilizersByCountry(@NonNull String countryCode) {
        List<Fertilizer> fertilizerList = Fertilizer.find(Fertilizer.class, "countryCode=?", countryCode);
        return fertilizerList;
    }

    public List<Fertilizer> getSelectedFertilizers(@NonNull String deviceIdentifier) {
        List<Fertilizer> fertilizerList = Fertilizer.find(Fertilizer.class, "selected=? and countryCode=?", "1", deviceIdentifier);
        return fertilizerList;
    }

    public Fertilizer getSavedFertilizer(@NonNull String fertilizerType, @NonNull String deviceIdentifier) {
        return null;
    }

    /* Begin saving for intercrop fertilizer */
    public void saveInterCropFertilizerList(final List<InterCropFertilizer> selectedFertilizers) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
    }

    public long saveSelectedInterCropFertilizer(@NonNull final InterCropFertilizer selectedFertilizer) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Unique fertilizer saving violation!");
            Crashlytics.logException(ex);
        }
        return 0;
    }


    public List<InterCropFertilizer> getAllInterCropFertilizersByCountry(@NonNull String countryCode) {
        return null;
    }

    public List<InterCropFertilizer> getAvailableInterCropFertilizersByCountryUseCase(@NonNull String countryCode, @NonNull EnumUseCase useCase) {
        return null;
    }

    public List<InterCropFertilizer> getSelectedInterCropFertilizers(@NonNull String deviceIdentifier, @NonNull EnumUseCase useCase) {
        return null;
    }

    public Fertilizer getSavedInterCropFertilizer(@NonNull String fertilizerType, @NonNull String countryCode, @NonNull EnumUseCase useCase) {
        return null;
    }
    /* end saving of intercrop fertilizer */

    public long saveMaizePerformanceData(@NonNull MaizePerformance maizePerformanceModel) {
        return 0;
    }


    //@TODO Consider moving data source to the API
    @Deprecated
    public MaizePerformance getMaizePerformance() {
        return null;
    }


    public StarchFactory getSelectedStarchFactoryByTag(String factoryNameCountry) {
        return null;
    }

    public void saveStarchFactories(@NonNull List<StarchFactory> starchFactoryList) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving starch factories");
            Crashlytics.logException(ex);
        }
    }

    public List<StarchFactory> getStarchFactories(@NonNull String countryCode) {
        return null;
    }

    public void saveFertilizerPrices(@NonNull List<FertilizerPrices> fertilizerPricesList) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "Error occurred saving fertilizer prices");
            Crashlytics.logException(ex);
        }
    }


    public List<FertilizerPrices> getFertilizerPrices(@NonNull String countryCode) {
        return null;
    }

    public long saveMarketOutlet(@NonNull CassavaMarketOutlet cassavaMarketOutlet) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving market outlet");
            Crashlytics.logException(ex);
        }
        return 0;
    }

    public CassavaMarketOutlet getCassavaMarketOutlet() {
        return null;
    }

    public void saveRecAdvice(RecAdvice recAdvice) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving recommendation advice");
            Crashlytics.logException(ex);
        }
    }

    public RecAdvice getRecAdvice() {
        return null;
    }

    public void saveCurrentPractice(CurrentPractice currentPractice) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving CurrentPractice");
            Crashlytics.logException(ex);
        }
    }

    public CurrentPractice getCurrentPractice() {
        return null;
    }

    public void saveOperationCosts(OperationCosts operationCosts) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving OperationCosts");
            Crashlytics.logException(ex);
        }
    }

    public OperationCosts getOperationCosts() {
        return null;
    }

    public long saveMaizeMarketOutlet(MaizeMarketOutlet maizeMarketOutlet) {
        long id = 0;

        return id;
    }

    public MaizeMarketOutlet getMaizeMarketOutlet() {
        return null;
    }

    public long savePotatoMarketOutlet(PotatoMarketOutlet potatoMarketOutlet) {
        long id = 0;
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving PotatoMarketOutlet");
            Crashlytics.logException(ex);
        }

        return id;
    }

    public PotatoMarketOutlet getPotatoMarketOutlet() {
        return null;
    }

    public void saveCassavaPrice(@NonNull List<CassavaPrice> cassavaPriceList) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving cassava prices");
            Crashlytics.logException(ex);
        }
    }

    public List<CassavaPrice> getCassavaPrices(@NonNull String countryCode) {
        return null;
    }

    public CassavaPrice getSelectedCassavaPriceByTag(String priceTag) {
        return null;
    }

    public void saveMaizePrice(List<MaizePrice> maizePriceList) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving maize prices");
            Crashlytics.logException(ex);
        }
    }

    public List<MaizePrice> getMaizePrices(@NonNull String countryCode) {
        return null;
    }

    public MaizePrice getSelectedMaizePriceByTag(String priceTag) {
        return null;
    }


    /* Sweet potato prices */
    public void savePotatoPrice(List<PotatoPrice> potatoPriceList) {
        try {

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred saving maize prices");
            Crashlytics.logException(ex);
        }
    }

    public List<PotatoPrice> getPotatoPrices(@NonNull String countryCode) {
        return null;

    }

    public PotatoPrice getSelectedPotatoPriceByTag(String priceTag) {
        return null;
    }
}
