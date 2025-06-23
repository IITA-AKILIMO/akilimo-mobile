package com.akilimo.mobile.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.entities.AkilimoCurrency
import com.akilimo.mobile.entities.CassavaMarket
import com.akilimo.mobile.entities.CassavaPrice
import com.akilimo.mobile.entities.CropPerformance
import com.akilimo.mobile.entities.CropSchedule
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.FieldYield
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.MaizeMarket
import com.akilimo.mobile.entities.MaizePrice
import com.akilimo.mobile.entities.MandatoryInfo
import com.akilimo.mobile.entities.OperationCost
import com.akilimo.mobile.entities.PotatoMarket
import com.akilimo.mobile.entities.PotatoPrice
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.entities.UseCase
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
        OperationCost::class,
        InvestmentAmount::class,
        UserLocation::class,
        MaizeMarket::class,
        CropPerformance::class,
        MaizePrice::class,
        MandatoryInfo::class,
        PotatoMarket::class,
        PotatoPrice::class,
        UserProfile::class,
        CropSchedule::class,
        StarchFactory::class,
        UseCase::class,
        AkilimoCurrency::class,
        AdviceStatus::class
    ],
    version = 5,
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
    abstract fun investmentAmountDao(): InvestmentAmountDao
    abstract fun locationInfoDao(): UserLocationDao
    abstract fun maizeMarketDao(): MaizeMarketDao
    abstract fun maizePerformanceDao(): CropPerformanceDao
    abstract fun maizePriceDao(): MaizePriceDao
    abstract fun mandatoryInfoDao(): MandatoryInfoDao
    abstract fun operationCostDao(): OperationCostDao
    abstract fun potatoMarketDao(): PotatoMarketDao
    abstract fun potatoPriceDao(): PotatoPriceDao
    abstract fun profileInfoDao(): UserProfileDao
    abstract fun scheduleDateDao(): CropScheduleDao
    abstract fun starchFactoryDao(): StarchFactoryDao
    abstract fun useCaseDao(): UseCaseDao
    abstract fun adviceStatusDao(): AdviceStatusDao
    abstract fun currencyDao(): CurrencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "AKILIMO_JUNE_2025"

        fun getInstance(applicationContext: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(applicationContext).also { INSTANCE = it }
            }

        private fun buildDatabase(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // ✅ Optional pre-population logic here
                        // db.execSQL("INSERT INTO ...")
                    }
                })
                .build()
        }
    }
}