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
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.entities.InterCropFertilizer
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.entities.MaizePrice
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.PotatoMarket
import com.akilimo.mobile.entities.PotatoPrice
import com.akilimo.mobile.entities.ScheduledDate
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.entities.UseCases
import com.akilimo.mobile.entities.UserLocation
import com.akilimo.mobile.entities.UserProfile

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
        UserLocation::class,
        MaizeMarket::class,
        CropPerformance::class,
        MaizePrice::class,
        MandatoryInfo::class,
        PotatoMarket::class,
        PotatoPrice::class,
        UserProfile::class,
        ScheduledDate::class,
        StarchFactory::class,
        UseCases::class,
        AkilimoCurrency::class,
        AdviceStatus::class
    ], version = 1,
    exportSchema = false
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
    abstract fun locationInfoDao(): UserLocationDao
    abstract fun maizeMarketDao(): MaizeMarketDao
    abstract fun maizePerformanceDao(): CropPerformanceDao
    abstract fun maizePriceDao(): MaizePriceDao
    abstract fun mandatoryInfoDao(): MandatoryInfoDao
    abstract fun potatoMarketDao(): PotatoMarketDao
    abstract fun potatoPriceDao(): PotatoPriceDao
    abstract fun profileInfoDao(): UserProfileDao
    abstract fun scheduleDateDao(): ScheduleDateDao
    abstract fun starchFactoryDao(): StarchFactoryDao
    abstract fun useCaseDao(): UseCaseDao
    abstract fun adviceStatusDao(): AdviceStatusDao
    abstract fun currencyDao(): CurrencyDao


    companion object {
        // For Singleton instantiation
        @Volatile
        private var database: AppDatabase? = null
        private const val DATABASE_NAME = "AKILIMO_APR_2025"

        @JvmStatic
        fun getDatabase(context: Context): AppDatabase {
            return database ?: synchronized(this) {
                database ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, DATABASE_NAME
                )
                    .allowMainThreadQueries() //TODO migrate to coroutines later
                    .fallbackToDestructiveMigration()
                    .build().also { database = it }
            }
        }
    }

}
