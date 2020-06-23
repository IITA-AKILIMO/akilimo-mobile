package com.iita.akilimo.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iita.akilimo.entities.*
import java.util.concurrent.Executors

@Database(
    entities = [
        CassavaMarket::class,
        CassavaPrice::class,
        FieldYield::class,
        InterCropFertilizer::class,
        InvestmentAmount::class,
        LocationInfo::class,
        MaizeMarket::class,
        MaizePerformance::class,
        MaizePrice::class,
        MandatoryInfo::class,
        PotatoMarket::class,
        ProfileInfo::class,
        ScheduledDate::class,
        StarchFactory::class,
        UseCases::class
    ], version = 1, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cassavaMarketDao(): CassavaMarketDao
    abstract fun cassavaPriceDao(): CassavaPriceDao
    abstract fun fieldYieldDao(): FieldYieldDao
    abstract fun interCropFertilizerDao(): InterCropFertilizerDao
    abstract fun investmentAmountDao(): InvestmentAmountDao
    abstract fun locationInfoDao(): LocationInfoDao
    abstract fun maizeMarketDao(): MaizeMarketDao
    abstract fun maizePerformanceDao(): MaizePerformanceDao
    abstract fun maizePriceDao(): MaizePriceDao
    abstract fun mandatoryInfoDao(): MandatoryInfoDao
    abstract fun potatoMarketDao(): PotatoMarketDao
    abstract fun profileInfoDao(): ProfileInfoDao
    abstract fun scheduleDateDao(): ScheduleDateDao
    abstract fun starchFactoryDao(): StarchFactoryDao
    abstract fun useCaseDao(): UseCaseDao


    companion object {
        // For Singleton instantiation
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val NUMBER_OF_THREADS = 4
        val databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        @JvmStatic
        @Synchronized
        fun getDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java, "akilimo_db"
                        )
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE
        }
    }
}