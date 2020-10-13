package com.iita.akilimo.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iita.akilimo.entities.*

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
        UseCases::class
    ], version = 5, exportSchema = false
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


    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val NUMBER_OF_THREADS = 4

        @JvmStatic
        @Synchronized
        fun getDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java, "AKILIMO_13_OCT"
                        )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}