package com.akilimo.mobile

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.akilimo.mobile.dao.AdviceCompletionDao
import com.akilimo.mobile.dao.AkilimoUserDao
import com.akilimo.mobile.dao.CassavaMarketPriceDao
import com.akilimo.mobile.dao.CassavaUnitDao
import com.akilimo.mobile.dao.CassavaYieldDao
import com.akilimo.mobile.dao.CurrentPracticeDao
import com.akilimo.mobile.dao.FertilizerDao
import com.akilimo.mobile.dao.FertilizerPriceDao
import com.akilimo.mobile.dao.FieldOperationCostDao
import com.akilimo.mobile.dao.InvestmentAmountDao
import com.akilimo.mobile.dao.MaizePerformanceDao
import com.akilimo.mobile.dao.ProduceMarketDao
import com.akilimo.mobile.dao.SelectedCassavaMarketDao
import com.akilimo.mobile.dao.SelectedFertilizerDao
import com.akilimo.mobile.dao.SelectedInvestmentDao
import com.akilimo.mobile.dao.StarchFactoryDao
import com.akilimo.mobile.entities.AdviceCompletion
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.SelectedFertilizer
import com.akilimo.mobile.entities.SelectedInvestment
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.utils.Converters
import com.akilimo.mobile.utils.EnumAdviseConverter
import com.akilimo.mobile.utils.EnumAdviseTaskConverter
import com.akilimo.mobile.utils.EnumAreaUnitConverter
import com.akilimo.mobile.utils.EnumCountryConverter
import com.akilimo.mobile.utils.EnumMaizePerfConverter
import com.akilimo.mobile.utils.EnumMarketTypeConverter
import com.akilimo.mobile.utils.EnumProduceTypeConverter
import com.akilimo.mobile.utils.EnumStepStatusConverter
import com.akilimo.mobile.utils.EnumTillageConverters
import com.akilimo.mobile.utils.EnumUnitOfSaleConverter
import com.akilimo.mobile.utils.EnumUseCaseConverter
import com.akilimo.mobile.utils.EnumWeedControlConverter

@Database(
    entities = [
        Fertilizer::class,
        FertilizerPrice::class,
        SelectedFertilizer::class,
        AkilimoUser::class,
        InvestmentAmount::class,
        SelectedInvestment::class,
        StarchFactory::class,
        CassavaMarketPrice::class,
        SelectedCassavaMarket::class,
        ProduceMarket::class,
        CassavaUnit::class,
        CassavaYield::class,
        AdviceCompletion::class,
        FieldOperationCost::class,
        CurrentPractice::class,
        MaizePerformance::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    Converters::class,
    EnumTillageConverters::class,
    EnumUseCaseConverter::class,
    EnumCountryConverter::class,
    EnumAreaUnitConverter::class,
    EnumAdviseConverter::class,
    EnumAdviseTaskConverter::class,
    EnumStepStatusConverter::class,
    EnumWeedControlConverter::class,
    EnumUnitOfSaleConverter::class,
    EnumProduceTypeConverter::class,
    EnumMarketTypeConverter::class,
    EnumMaizePerfConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fertilizerDao(): FertilizerDao
    abstract fun fertilizerPriceDao(): FertilizerPriceDao
    abstract fun akilimoUserDao(): AkilimoUserDao

    abstract fun starchFactoryDao(): StarchFactoryDao

    abstract fun selectedFertilizerDao(): SelectedFertilizerDao

    abstract fun investmentAmountDao(): InvestmentAmountDao
    abstract fun selectedInvestmentDao(): SelectedInvestmentDao

    abstract fun cassavaMarketPriceDao(): CassavaMarketPriceDao
    abstract fun selectedCassavaMarketDao(): SelectedCassavaMarketDao

    abstract fun cassavaUnitDao(): CassavaUnitDao

    abstract fun cassavaYieldDao(): CassavaYieldDao

    abstract fun adviceCompletionDao(): AdviceCompletionDao

    abstract fun fieldOperationCostsDao(): FieldOperationCostDao

    abstract fun currentPracticeDao(): CurrentPracticeDao

    abstract fun produceMarketDao(): ProduceMarketDao
    abstract fun maizePerformanceDao(): MaizePerformanceDao


    companion object {
        private const val DATABASE_NAME = "AKILIMO_28_DEC_2025"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).allowMainThreadQueries() // TODO: remove this line when we are ready to go live


            return builder.build()
        }
    }
}