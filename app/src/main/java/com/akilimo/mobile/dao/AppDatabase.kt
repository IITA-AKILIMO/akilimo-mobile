package com.akilimo.mobile.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.AkilimoCurrency
import com.akilimo.mobile.entities.CassavaMarket
import com.akilimo.mobile.entities.CassavaPrice
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.entities.InterCropFertilizer
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.LocationInfo
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.entities.MaizePrice
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.PotatoMarket
import com.akilimo.mobile.entities.PotatoPrice
import com.akilimo.mobile.entities.ProfileInfo
import com.akilimo.mobile.entities.ScheduledDate
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.entities.UseCases

@Database(
    entities = [
        CassavaMarket::class,
        CassavaPrice::class,
        CurrentPractice::class,
        Fertilizer::class,
        FertilizerPrice::class,
        FieldYield::class,
        FieldOperationCost::class,
        InterCropFertilizer::class,
        InvestmentAmount::class,
        LocationInfo::class,
        MaizeMarket::class,
        MaizePerformance::class,
        MaizePrice::class,
        MandatoryInfo::class,
        PotatoMarket::class,
        PotatoPrice::class,
        ProfileInfo::class,
        ScheduledDate::class,
        StarchFactory::class,
        UseCases::class,
        AkilimoCurrency::class,
        AdviceStatus::class
    ], version = 3, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cassavaMarketDao(): CassavaMarketDao
    abstract fun cassavaPriceDao(): CassavaPriceDao
    abstract fun currentPracticeDao(): CurrentPracticeDao
    abstract fun fertilizerDao(): FertilizerDao
    abstract fun fertilizerPriceDao(): FertilizerPriceDao
    abstract fun fieldOperationCostDao(): FieldOperationCostsDao
    abstract fun fieldYieldDao(): FieldYieldDao

    @Deprecated("To be removed in future release")
    abstract fun interCropFertilizerDao(): InterCropFertilizerDao

    abstract fun investmentAmountDao(): InvestmentAmountDao
    abstract fun locationInfoDao(): LocationInfoDao
    abstract fun maizeMarketDao(): MaizeMarketDao
    abstract fun maizePerformanceDao(): MaizePerformanceDao
    abstract fun maizePriceDao(): MaizePriceDao
    abstract fun mandatoryInfoDao(): MandatoryInfoDao
    abstract fun potatoMarketDao(): PotatoMarketDao
    abstract fun potatoPriceDao(): PotatoPriceDao
    abstract fun profileInfoDao(): ProfileInfoDao
    abstract fun scheduleDateDao(): ScheduleDateDao
    abstract fun starchFactoryDao(): StarchFactoryDao
    abstract fun useCaseDao(): UseCaseDao
    abstract fun adviceStatusDao(): AdviceStatusDao
    abstract fun currencyDao(): CurrencyDao


    companion object {
        // For Singleton instantiation
        @Volatile
        private var database: AppDatabase? = null
        private const val NUMBER_OF_THREADS = 4

        @JvmStatic
        @Synchronized
        fun getDatabase(context: Context): AppDatabase? {
            if (database == null) {
                synchronized(AppDatabase::class.java) {
                    if (database == null) {
                        database = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java, "AKILIMO_22_APR_2025"
                        )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return database
        }
    }
}
